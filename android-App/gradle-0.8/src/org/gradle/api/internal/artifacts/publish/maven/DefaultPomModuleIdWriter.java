/*
 * Copyright 2007-2008 the original author or authors.
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
package org.gradle.api.internal.artifacts.publish.maven;

import org.gradle.api.artifacts.maven.MavenPom;

import java.io.PrintWriter;

/**
 * @author Hans Dockter
 */
public class DefaultPomModuleIdWriter implements PomModuleIdWriter {

    public void convert(MavenPom pom, PrintWriter out) {
        assert pom != null;
        assert pom.getGroupId() != null && pom.getArtifactId() != null;
        out.println(enclose(PomWriter.GROUP_ID, pom.getGroupId()));
        out.println(enclose(PomWriter.ARTIFACT_ID, pom.getArtifactId()));
        if (pom.getVersion() != null) {
            out.println(enclose(PomWriter.VERSION, pom.getVersion()));
        }
        if (pom.getPackaging() != null) {
            out.println(enclose(PomWriter.PACKAGING, pom.getPackaging()));
        }
        if (pom.getClassifier() != null) {
            out.println(enclose(PomWriter.CLASSIFIER, pom.getClassifier()));
        }
    }

    private String enclose(String tagValue, String text) {
        return XmlHelper.enclose(PomWriter.DEFAULT_INDENT, tagValue, text);
    }
}
