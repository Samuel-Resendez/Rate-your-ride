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

package org.gradle.api.tasks.javadoc;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This task generates html api doc for Groovy classes. It uses Groovy's Groovydoc tool for this. Please note that
 * the Groovydoc tool has some severe limitations at the moment (for example no doc for properties comments). The
 * version of the Groovydoc that is used, is the one from the Groovy defined in the build script. Please note also,
 * that the Groovydoc tool prints to System.out for many of its statements and does circumvents our logging currently.
 *  
 * @author Hans Dockter
 */
public class Groovydoc extends SourceTask {
    private FileCollection groovyClasspath;

    private File destinationDir;

    private AntGroovydoc antGroovydoc = new AntGroovydoc();

    private boolean use;

    private String windowTitle;

    private String docTitle;

    private String header;

    private String footer;

    private File overview;

    boolean includePrivate;

    public Groovydoc() {
        captureStandardOutput(LogLevel.INFO);
    }

    @TaskAction
    protected void generate() {
        List<File> taskClasspath = new ArrayList<File>(getGroovyClasspath().getFiles());
        throwExceptionIfTaskClasspathIsEmpty(taskClasspath);
        ProjectInternal project = (ProjectInternal) getProject();
        antGroovydoc.execute(getSource(), getDestinationDir(), isUse(), getWindowTitle(),
                getDocTitle(), getHeader(), getFooter(), getOverview(), isIncludePrivate(),
                project.getGradle().getIsolatedAntBuilder(), taskClasspath, project);
    }

    private void throwExceptionIfTaskClasspathIsEmpty(List taskClasspath) {
        if (taskClasspath.size() == 0) {
            throw new InvalidUserDataException("You must assign a Groovy library to the groovy configuration!");
        }
    }

    /**
     * <p>Returns the directory to generate the documentation into.</p>
     *
     * @return The directory.
     */
    @OutputDirectory
    public File getDestinationDir() {
        return destinationDir;
    }

    /**
     * <p>Sets the directory to generate the documentation into.</p>
     */
    public void setDestinationDir(File destinationDir) {
        this.destinationDir = destinationDir;
    }

    /**
     * <p>Returns the classpath to use to locate classes referenced by the documented source.</p>
     *
     * @return The classpath.
     */
    @InputFiles
    public FileCollection getGroovyClasspath() {
        return groovyClasspath;
    }

    /**
     * <p>Sets the classpath to use to locate classes referenced by the documented source.</p>
     */
    public void setGroovyClasspath(FileCollection groovyClasspath) {
        this.groovyClasspath = groovyClasspath;
    }

    public AntGroovydoc getAntGroovydoc() {
        return antGroovydoc;
    }

    public void setAntGroovydoc(AntGroovydoc antGroovydoc) {
        this.antGroovydoc = antGroovydoc;
    }

    /**
     * Returns whether to create class and package usage pages.
     */
    public boolean isUse() {
        return use;
    }

    /**
     * Set's whether to create class and package usage pages. Defaults to false.
     *
     * @param use
     */
    public void setUse(boolean use) {
        this.use = use;
    }

    /**
     * Returns the browser window title for the documentation.
     */
    public String getWindowTitle() {
        return windowTitle;
    }

    /**
     * Set's the browser window title for the documentation.
     *
     * @param windowTitle A text for the windows title
     */
    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    /**
     * Returns the title for the package index(first) page. Returns null if not set.
     */
    public String getDocTitle() {
        return docTitle;
    }

    /**
     * Set's title for the package index(first) page (optional).
     * 
     * @param docTitle the docTitle as html-code
     */
    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    /**
     * Returns the html header for each page. Returns null if not set.
     */
    public String getHeader() {
        return header;
    }

    /**
     * Set's header text for each page (optional).
     * 
     * @param header the header as html-code
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * Returns the html footer for each page. Returns null if not set.
     */
    public String getFooter() {
        return footer;
    }

    /**
     * Set's footer text for each page (optional).
     *
     * @param footer the footer as html-code
     */
    public void setFooter(String footer) {
        this.footer = footer;
    }

    /**
     * Returns a html file to be used for overview documentation. Returns null if such a file is not set.
     */
    @InputFile
    @Optional
    public File getOverview() {
        return overview;
    }

    /**
     * Set's a html file to be used for overview documentation (optional).
     *
     * @param overview
     */
    public void setOverview(File overview) {
        this.overview = overview;
    }

    /**
     * Returns whether to include all classes and members (i.e. including private ones).
     */
    public boolean isIncludePrivate() {
        return includePrivate;
    }

    /**
     * Set's whether to include all classes and members (i.e. including private ones) if set to true. Defaults to false.
     * 
     * @param includePrivate 
     */
    public void setIncludePrivate(boolean includePrivate) {
        this.includePrivate = includePrivate;
    }
}
