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

package org.gradle.internal.execution

import org.gradle.api.DefaultTask
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.BuildOperationsFixture

class ExecuteTaskBuildOperationTypeIntegrationTest extends AbstractIntegrationSpec {

    def operations = new BuildOperationsFixture(executer, temporaryFolder)

    def "emits operation for task execution"() {
        when:
        buildScript """
            task t {}
        """
        succeeds "t"

        then:
        def op = operations.first(ExecuteTaskBuildOperationType) {
            it.details.taskPath == ":t"
        }
        op.details.buildPath == ":"
        op.details.taskClass == DefaultTask.name
        op.details.taskId != null

        op.result.cachingDisabledReasonCategory == "UNKNOWN"
        op.result.cachingDisabledReasonMessage == "Cacheability was not determined"
        op.result.skipMessage == "UP-TO-DATE"
        op.result.actionable == false
        op.result.originBuildInvocationId == null
        op.result.upToDateMessages == null
    }

    def "emits operation result for failed task execution"() {
        when:
        buildScript """
            task t {
                doLast {
                    throw new RuntimeException("!")
                }
            }
        """
        fails "t"

        then:
        def op = operations.first(ExecuteTaskBuildOperationType) {
            it.details.taskPath == ":t"
        }

        op.result.upToDateMessages == ["Task has not declared any outputs."]
        op.failure == "org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':t'."
    }

    def "does not emit result for beforeTask failure"() {
        when:
        buildScript """
            task t {
                doLast {}   
            }
            
            gradle.taskGraph.beforeTask {
                throw new RuntimeException("!")
            }
        """
        fails "t"

        then:
        def op = operations.first(ExecuteTaskBuildOperationType) {
            it.details.taskPath == ":t"
        }

        op.result == null
        op.failure == "java.lang.RuntimeException: !"
    }

    def "does emit result for afterTask failure"() {
        when:
        buildScript """
            task t {
                doLast {}   
            }
            
            gradle.taskGraph.afterTask {
                throw new RuntimeException("!")
            }
        """
        fails "t"

        then:
        def op = operations.first(ExecuteTaskBuildOperationType) {
            it.details.taskPath == ":t"
        }

        op.result != null
        op.failure == "java.lang.RuntimeException: !"
    }

    def "afterTask failure supersedes task failure"() {
        when:
        buildScript """
            task t {
                doLast {
                    throw new RuntimeException("!")
                }   
            }
            
            gradle.taskGraph.afterTask {
                throw new RuntimeException("2")
            }
        """
        fails "t"

        then:
        def op = operations.first(ExecuteTaskBuildOperationType) {
            it.details.taskPath == ":t"
        }

        op.result != null
        op.failure == "java.lang.RuntimeException: 2"
    }

}
