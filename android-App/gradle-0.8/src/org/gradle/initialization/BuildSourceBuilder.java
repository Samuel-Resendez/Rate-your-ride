/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.initialization;

import org.apache.commons.io.IOUtils;
import org.apache.ivy.core.IvyPatternHelper;
import org.gradle.*;
import org.gradle.api.Project;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolverContainer;
import org.gradle.api.internal.artifacts.configurations.Configurations;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.util.GUtil;
import org.gradle.util.WrapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author Hans Dockter
 */
public class BuildSourceBuilder {
    private static Logger logger = LoggerFactory.getLogger(BuildSourceBuilder.class);

    public static final String BUILD_SRC_ORG = "org.gradle";
    public static final String BUILD_SRC_MODULE = "buildSrc";
    public static final String BUILD_SRC_REVISION = "SNAPSHOT";
    public static final String BUILD_SRC_TYPE = "jar";

    private GradleFactory gradleFactory;
    private CacheInvalidationStrategy cacheInvalidationStrategy;

    private static final String DEFAULT_BUILD_SOURCE_SCRIPT_RESOURCE = "defaultBuildSourceScript.txt";

    public BuildSourceBuilder(GradleFactory gradleFactory, CacheInvalidationStrategy cacheInvalidationStrategy) {
        this.gradleFactory = gradleFactory;
        this.cacheInvalidationStrategy = cacheInvalidationStrategy;
    }

    public URLClassLoader buildAndCreateClassLoader(StartParameter startParameter)
    {
        Set<File> classpath = createBuildSourceClasspath(startParameter);
        Iterator<File> classpathIterator = classpath.iterator();
        URL[] urls = new URL[classpath.size()];
        for (int i = 0; i < urls.length; i++)
        {
            try
            {
                urls[i] = classpathIterator.next().toURI().toURL();
            }
            catch (MalformedURLException e)
            {
                throw new UncheckedIOException(e);
            }
        }
        return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
    }

    public Set<File> createBuildSourceClasspath(StartParameter startParameter) {
        assert startParameter.getCurrentDir() != null && startParameter.getBuildFile() == null;

        logger.debug("Starting to build the build sources.");
        if (!startParameter.getCurrentDir().isDirectory()) {
            logger.debug("Gradle source dir does not exist. We leave.");
            return new HashSet<File>();
        }
        logger.info("================================================" + " Start building buildSrc");
        try {
            Logging.LIFECYCLE.add(Logging.DISABLED);

            // todo Remove this redundancy. We have defined the buildResolverDir already somewhere else.
            // We should get the build resolver dir from the root project. But as we need to get the dir before
            // the build is executed, the GradleLauncher class should offer distinct methods for gettting an evaluated
            // project tree and running a build against such a tree. In the case of a valid cache, this would also
            // save the time to build the dag.
            File buildResolverDir = new File(startParameter.getCurrentDir(), Project.TMP_DIR_NAME + "/" + ResolverContainer.INTERNAL_REPOSITORY_NAME);

            StartParameter startParameterArg = startParameter.newInstance();
            startParameterArg.setProjectProperties(GUtil.addMaps(startParameter.getProjectProperties(), getDependencyProjectProps()));
            startParameterArg.setSearchUpwards(false);
            startParameterArg.setTaskNames(WrapUtil.toList(BasePlugin.CLEAN_TASK_NAME,
                    Configurations.uploadInternalTaskName(Dependency.ARCHIVES_CONFIGURATION)));
            boolean executeBuild = true;

            if (startParameter.getCacheUsage() == CacheUsage.ON && cacheInvalidationStrategy.isValid(buildArtifactFile(buildResolverDir), startParameter.getCurrentDir())) {
                executeBuild = false;
            }

            if (!new File(startParameter.getCurrentDir(), Project.DEFAULT_BUILD_FILE).isFile()) {
                logger.debug("Gradle script file does not exists. Using default one.");
                startParameterArg.useEmbeddedBuildFile(getDefaultScript());
            }

            GradleLauncher gradleLauncher = gradleFactory.newInstance(startParameterArg);
            BuildResult buildResult;
            if (executeBuild) {
                buildResult = gradleLauncher.run();
            } else {
                buildResult = gradleLauncher.getBuildAnalysis();
            }
            buildResult.rethrowFailure();

            Set<File> buildSourceClasspath = new LinkedHashSet<File>();
            File artifactFile = buildArtifactFile(buildResolverDir);
            if (artifactFile.exists()) {
                buildSourceClasspath.add(artifactFile);
            } else {
                logger.info("Building buildSrc has not produced any artifacts.");
            }
            Configuration runtimeConfiguration = buildResult.getGradle().getRootProject().getConfigurations().getByName(
                    JavaPlugin.RUNTIME_CONFIGURATION_NAME);
            buildSourceClasspath.addAll(runtimeConfiguration.getFiles());
            logger.debug("Gradle source classpath is: {}", buildSourceClasspath);
            logger.info("================================================" + " Finished building buildSrc");
            return buildSourceClasspath;
        } finally {
            Logging.LIFECYCLE.remove(Logging.DISABLED);
        }
    }

    static String getDefaultScript() {
        try {
            return IOUtils.toString(BuildSourceBuilder.class.getResourceAsStream(DEFAULT_BUILD_SOURCE_SCRIPT_RESOURCE));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File buildArtifactFile(File buildResolverDir) {
        String path = IvyPatternHelper.substitute(buildResolverDir.getAbsolutePath() + "/" + ResolverContainer.DEFAULT_CACHE_ARTIFACT_PATTERN,
                BUILD_SRC_ORG, BUILD_SRC_MODULE, BUILD_SRC_REVISION, BUILD_SRC_MODULE, BUILD_SRC_TYPE, BUILD_SRC_TYPE);
        return new File(path);
    }

    private Map getDependencyProjectProps() {
        return GUtil.map(
                "group", BuildSourceBuilder.BUILD_SRC_ORG,
                "version", BuildSourceBuilder.BUILD_SRC_REVISION,
                "type", "jar");
    }

    public GradleFactory getGradleFactory() {
        return gradleFactory;
    }

    public void setGradleFactory(GradleFactory gradleFactory) {
        this.gradleFactory = gradleFactory;
    }
}
