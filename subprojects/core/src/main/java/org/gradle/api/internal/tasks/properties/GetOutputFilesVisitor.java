/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.api.internal.tasks.properties;

import com.google.common.collect.ImmutableSortedSet;
import org.gradle.api.NonNullApi;
import org.gradle.api.internal.tasks.TaskOutputFilePropertySpec;
import org.gradle.api.internal.tasks.TaskPropertyUtils;

import java.util.ArrayList;
import java.util.List;

@NonNullApi
public class GetOutputFilesVisitor extends PropertyVisitor.Adapter {
    private List<TaskOutputFilePropertySpec> specs = new ArrayList<TaskOutputFilePropertySpec>();
    private ImmutableSortedSet<TaskOutputFilePropertySpec> fileProperties;
    private boolean hasDeclaredOutputs;

    @Override
    public void visitOutputFileProperty(TaskOutputFilePropertySpec outputFileProperty) {
        hasDeclaredOutputs = true;
        specs.add(outputFileProperty);
    }

    public ImmutableSortedSet<TaskOutputFilePropertySpec> getFileProperties() {
        if (fileProperties == null) {
            fileProperties = TaskPropertyUtils.collectFileProperties("output", specs.iterator());
        }
        return fileProperties;
    }

    public boolean hasDeclaredOutputs() {
        return hasDeclaredOutputs;
    }
}
