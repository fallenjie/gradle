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

package org.gradle.api.internal.artifacts.transform

import com.google.common.collect.Maps
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ResolvableArtifact
import org.gradle.internal.operations.BuildOperationQueue
import org.gradle.testing.internal.util.Specification

class TransformingAsyncArtifactListenerTest extends Specification {
    def transformer = Mock(ArtifactTransformer)
    def operationQueue = Mock(BuildOperationQueue)
    def listener  = new TransformingAsyncArtifactListener(transformer, null, operationQueue, Maps.newHashMap(), Maps.newHashMap())

    def "runs transforms in parallel if no cached result is available"() {
        given:
        transformer.hasCachedResult(_) >> false

        when:
        listener.artifactAvailable(Stub(ResolvableArtifact))
        listener.fileAvailable(new File("foo"))

        then:
        2 * operationQueue.add(_)
    }

    def "runs transforms immediately if the result is already cached"() {
        given:
        transformer.hasCachedResult(_) >> true

        when:
        listener.artifactAvailable(Stub(ResolvableArtifact))
        listener.fileAvailable(new File("foo"))

        then:
        2 * transformer.transform(_)
    }
}
