package org.gradle.api.internal.project

import org.gradle.api.internal.project.ant.BasicAntBuilder
import org.gradle.api.internal.project.ant.AntLoggingAdapter
import org.gradle.util.*

class DefaultIsolatedAntBuilder implements IsolatedAntBuilder {
    private final Map<List<File>, Map<String, Object>> classloaders = [:]

    void execute(Iterable<File> classpath, Closure antClosure) {
        List<File> normalisedClasspath = classpath as List
        Map<String, Object> classloadersForPath = classloaders[normalisedClasspath]
        ClassLoader antLoader
        ClassLoader gradleLoader
        if (!classloadersForPath) {
            // Need tools.jar for compile tasks
            List<File> fullClasspath = normalisedClasspath
            File toolsJar = ClasspathUtil.toolsJar
            if (toolsJar) {
                fullClasspath += toolsJar
            }

            Closure converter = {File file -> file.toURI().toURL() }
            URL[] classpathUrls = fullClasspath.collect(converter)
            URL[] gradleCoreUrls = BootstrapUtil.gradleCoreFiles.collect(converter)

            FilteringClassLoader loggingLoader = new FilteringClassLoader(getClass().classLoader)
            loggingLoader.allowPackage('org.slf4j')

            antLoader = new URLClassLoader(classpathUrls, ClassLoader.systemClassLoader.parent)
            gradleLoader = new URLClassLoader(gradleCoreUrls, new MultiParentClassLoader(antLoader, loggingLoader))

            classloaders[normalisedClasspath] = [antLoader: antLoader, gradleLoader: gradleLoader]
        } else {
            antLoader = classloadersForPath.antLoader
            gradleLoader = classloadersForPath.gradleLoader
        }

        ClassLoader originalLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = antLoader
        try {
            Object antBuilder = gradleLoader.loadClass(BasicAntBuilder.class.name).newInstance()

            Object antLogger = gradleLoader.loadClass(AntLoggingAdapter.class.name).newInstance()
            antBuilder.project.removeBuildListener(antBuilder.project.getBuildListeners()[0])
            antBuilder.project.addBuildListener(antLogger)

            // Ideally, we'd delegate directly to the AntBuilder, but it's Closure class is different to our caller's
            // Closure class, so the AntBuilder's methodMissing() doesn't work. It just converts our Closures to String
            // because they are not an instanceof it's Closure class
            Object delegate = new AntBuilderDelegate(antBuilder)
            ConfigureUtil.configure(antClosure, delegate)
        } finally {
            Thread.currentThread().contextClassLoader = originalLoader
        }
    }
}

class AntBuilderDelegate extends BuilderSupport {
    def Object builder

    def AntBuilderDelegate(builder) {
        this.builder = builder;
    }

    def propertyMissing(String name) {
        builder."$name"
    }

    protected Object createNode(Object name) {
        builder.createNode(name)
    }

    protected Object createNode(Object name, Map attributes) {
        builder.createNode(name, attributes)
    }

    protected Object createNode(Object name, Map attributes, Object value) {
        builder.createNode(name, attributes, value)
    }

    protected Object createNode(Object name, Object value) {
        builder.createNode(name, value)
    }

    protected void setParent(Object parent, Object child) {
        builder.setParent(parent, child)
    }

    protected void nodeCompleted(Object parent, Object node) {
        builder.nodeCompleted(parent, node)
    }

    protected Object postNodeCompletion(Object parent, Object node) {
        builder.postNodeCompletion(parent, node)
    }
}
