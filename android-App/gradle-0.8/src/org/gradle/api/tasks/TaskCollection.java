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
package org.gradle.api.tasks;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.specs.Spec;

/**
 * A {@code TaskCollection} contains a set of {@link Task} instances, and provides a number of query methods.
 */
public interface TaskCollection<T extends Task> extends DomainObjectCollection<T> {

    /**
     * {@inheritDoc}
     */
    TaskCollection<T> matching(Spec<? super T> spec);

    /**
     * {@inheritDoc}
     */
    T getByName(String name, Closure configureClosure) throws UnknownTaskException;

    /**
     * {@inheritDoc}
     */
    T getByName(String name) throws UnknownTaskException;

    /**
     * {@inheritDoc}
     */
    <S extends T> TaskCollection<S> withType(Class<S> type);

    /**
     * Adds an {@code Action} to be executed when a task is added to this collection.
     *
     * @param action The action to be executed
     * @return the supplied action
     */
    Action<? super T> whenTaskAdded(Action<? super T> action);

    /**
     * Adds a closure to be called when a task is added to this collection. The task is passed to the closure as the
     * parameter.
     *
     * @param closure The closure to be called
     */
    void whenTaskAdded(Closure closure);

    /**
     * Executes the given action against all tasks in this collection, and any tasks subsequently added to this
     * collection.
     *
     * @param action The action to be executed
     */
    void allTasks(Action<? super T> action);

    /**
     * Executes the given closure against all tasks in this collection, and any tasks subsequently added to this
     * collection.
     *
     * @param closure The closure to be called
     */
    void allTasks(Closure closure);

    /**
     * {@inheritDoc}
     */
    T getAt(String name) throws UnknownTaskException;
}
