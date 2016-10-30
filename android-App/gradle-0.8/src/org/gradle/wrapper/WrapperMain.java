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

package org.gradle.wrapper;

/**
 * @author Hans Dockter
 */
public class WrapperMain {
    public static final String WRAPPER_DEBUG_ENV = "GRADLE_WRAPPER_DEBUG_ENV";
    public static final String ALWAYS_UNPACK_ENV = "GRADLE_WRAPPER_ALWAYS_UNPACK";
    public static final String ALWAYS_DOWNLOAD_ENV = "GRADLE_WRAPPER_ALWAYS_DOWNLOAD";
    public static final String DEFAULT_GRADLE_USER_HOME = System.getProperty("user.home") + "/.gradle";
    public static final String GRADLE_USER_HOME_PROPERTY_KEY = "gradle.user.home";
    public static final String GRADLE_USER_HOME_ENV_KEY = "GRADLE_USER_HOME";
    public static final String DEBUG_PROPERTY_KEY = "gradle.bootstrap.debug";

    public static void main(String[] args) throws Exception {
        if (System.getenv(WRAPPER_DEBUG_ENV) != null) {
            System.out.println(ALWAYS_UNPACK_ENV + " env variable: " + System.getenv(ALWAYS_UNPACK_ENV));
            System.out.println(ALWAYS_DOWNLOAD_ENV + " env variable: " + System.getenv(ALWAYS_DOWNLOAD_ENV));
        }
        boolean alwaysDownload = Boolean.parseBoolean(System.getenv(ALWAYS_DOWNLOAD_ENV));
        boolean alwaysUnpack = Boolean.parseBoolean(System.getenv(ALWAYS_UNPACK_ENV));

        new Wrapper().execute(
                args,
                new Install(alwaysDownload, alwaysUnpack, new Download(), new PathAssembler(gradleUserHome())),
                new BootstrapMainStarter());
    }

    private static String gradleUserHome() {
        String gradleUserHome = System.getProperty(GRADLE_USER_HOME_PROPERTY_KEY);
        if (gradleUserHome != null) {
            return gradleUserHome;
        } else if((gradleUserHome = System.getenv(GRADLE_USER_HOME_ENV_KEY)) != null) {
            return gradleUserHome;
        } else {
            return DEFAULT_GRADLE_USER_HOME;
        }
    }

    static boolean isDebug() {
        String prop = System.getProperty(DEBUG_PROPERTY_KEY);
        return prop != null && !prop.toUpperCase().equals("FALSE");
    }
}
