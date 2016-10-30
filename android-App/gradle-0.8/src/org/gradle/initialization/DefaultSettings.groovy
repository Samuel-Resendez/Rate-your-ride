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

package org.gradle.initialization

import org.gradle.StartParameter
import org.gradle.groovy.scripts.ScriptSource
import org.gradle.api.internal.project.StandardOutputRedirector

/**
 * @author Hans Dockter
 */
public class DefaultSettings extends BaseSettings {
    public DefaultSettings() {}

    DefaultSettings(IProjectDescriptorRegistry projectDescriptorRegistry,
                    URLClassLoader classloader, File settingsDir,
                    ScriptSource settingsScript, StartParameter startParameter, StandardOutputRedirector standardOutputRedirector) {
      super(projectDescriptorRegistry, classloader, settingsDir, settingsScript, startParameter, standardOutputRedirector)
    }

    def propertyMissing(String property) {
        return dynamicObjectHelper.getProperty(property)
    }

    void setProperty(String name, value) {
        dynamicObjectHelper.setProperty(name, value) 
    }
}
