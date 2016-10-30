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
package org.gradle.api.internal.project;

import org.gradle.api.Transformer;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * Handles validation, dependency handling, and skipping for a property marked with a given annotation.
 */
public interface PropertyAnnotationHandler {
    /**
     * The annotation type which this handler is responsible for.
     *
     * @return The annotation.
     */
    Class<? extends Annotation> getAnnotationType();

    /**
     * Returns the actions for the given property of a class.
     *
     * @param target The element which the annotation is attached to.
     * @param propertyName The name of the property.
     * @return The actions for the property.
     */
    PropertyActions getActions(AnnotatedElement target, String propertyName);

    interface PropertyActions {
        /**
         * Returns the action used to validate the value of this property. May return null. This action is only called
         * when the property value is not null.
         */
        ValidationAction getValidationAction();

        /**
         * Returns the action used to skip the task based on the value of this property. May return null. This action is
         * called before the validation action.
         */
        ValidationAction getSkipAction();

        /**
         * Returns the transformer used to create the dependency which must be added to the task based on the value of
         * this property. May return null.
         */
        Transformer<Object> getTaskDependency();
    }
}
