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
package org.gradle.execution;

import org.gradle.api.Task;
import org.gradle.util.GFileUtils;

import java.io.File;

/**
 * @author Hans Dockter
 */
public class DefaultOutputHistoryWriter implements OutputHistoryWriter {
    public void taskSuccessfullyExecuted(Task task) {
        File historyFile = createHistoryFile(task);
        GFileUtils.writeStringToFile(historyFile, "" + System.currentTimeMillis());
    }

    public void taskFailed(Task task) {
        createHistoryFile(task).delete();
    }

    private File createHistoryFile(Task task) {
        return new File(task.getProject().getBuildDir(), OutputHistoryWriter.HISTORY_DIR_NAME + "/" +
                HistoryPathConverter.convertTaskPath(task.getPath()));
    }
}
