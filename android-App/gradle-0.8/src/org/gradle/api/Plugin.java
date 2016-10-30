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
package org.gradle.api;

import org.gradle.api.plugins.ProjectPluginsContainer;

/**
 * <p>A <code>Plugin</code> represents an extension to Gradle, which you can use to add behaviour your {@link
 * org.gradle.api.Project}.</p>
 *
 * @author Hans Dockter
 */
public interface Plugin {
    void use(Project project, ProjectPluginsContainer projectPluginsHandler);
}
