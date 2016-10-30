/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.internal.file;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.file.*;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.StopExecutionException;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.util.ConfigureUtil;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractFileTree extends AbstractFileCollection implements FileTree {
    public Set<File> getFiles() {
        final Set<File> files = new LinkedHashSet<File>();
        visit(new FileVisitor() {
            public void visitDir(FileVisitDetails dirDetails) {
            }

            public void visitFile(FileVisitDetails fileDetails) {
                files.add(fileDetails.getFile());
            }
        });
        return files;
    }

    public FileCollection stopExecutionIfEmpty() {
        final AtomicBoolean found = new AtomicBoolean();
        visit(new FileVisitor() {
            public void visitDir(FileVisitDetails dirDetails) {
            }

            public void visitFile(FileVisitDetails fileDetails) {
                found.set(true);
                fileDetails.stopVisiting();
            }
        });
        if (!found.get()) {
            throw new StopExecutionException(String.format("%s does not contain any files.", getCapDisplayName()));
        }
        return this;
    }

    public FileTree matching(Closure filterConfigClosure) {
        PatternSet patternSet = new PatternSet();
        ConfigureUtil.configure(filterConfigClosure, patternSet);
        return matching(patternSet);
    }

    public FileTree matching(PatternFilterable patterns) {
        PatternSet patternSet = new PatternSet();
        patternSet.copyFrom(patterns);
        return new FilteredFileTree(this, patternSet.getAsSpec());
    }

    public Map<String, File> getAsMap() {
        final Map<String, File> map = new LinkedHashMap<String, File>();
        visit(new FileVisitor() {
            public void visitDir(FileVisitDetails dirDetails) {
            }

            public void visitFile(FileVisitDetails fileDetails) {
                map.put(fileDetails.getRelativePath().getPathString(), fileDetails.getFile());
            }
        });
        return map;
    }

    @Override
    protected void addAsResourceCollection(Object builder, String nodeName) {
        new AntFileTreeBuilder(getAsMap()).addToAntBuilder(builder, nodeName);
    }

    @Override
    public FileTree getAsFileTree() {
        return this;
    }

    public FileTree plus(FileTree fileTree) {
        return new UnionFileTree(this, fileTree);
    }

    public FileTree visit(Closure closure) {
        return visit((FileVisitor) DefaultGroovyMethods.asType(closure, FileVisitor.class));
    }

    private static class FilteredFileTree extends AbstractFileTree {
        private final AbstractFileTree fileTree;
        private final Spec<RelativePath> spec;

        public FilteredFileTree(AbstractFileTree fileTree, Spec<RelativePath> spec) {
            this.fileTree = fileTree;
            this.spec = spec;
        }

        @Override
        public String getDisplayName() {
            return fileTree.getDisplayName();
        }

        public FileTree visit(final FileVisitor visitor) {
            fileTree.visit(new FileVisitor() {
                public void visitDir(FileVisitDetails dirDetails) {
                    if (spec.isSatisfiedBy(dirDetails.getRelativePath())) {
                        visitor.visitDir(dirDetails);
                    }
                }

                public void visitFile(FileVisitDetails fileDetails) {
                    if (spec.isSatisfiedBy(fileDetails.getRelativePath())) {
                        visitor.visitFile(fileDetails);
                    }
                }
            });
            return this;
        }
    }

}
