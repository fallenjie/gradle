/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.api.internal.initialization.loadercache

import org.gradle.api.internal.file.TestFiles
import org.gradle.api.internal.hash.DefaultFileHasher
import org.gradle.internal.classpath.DefaultClassPath
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.junit.Rule
import spock.lang.Specification

class HashClassPathSnapshotterTest extends Specification {

    @Rule
    TestNameTestDirectoryProvider temp = new TestNameTestDirectoryProvider()

    def snapshotter = new HashClassPathSnapshotter(new DefaultFileHasher(), TestFiles.fileSystem())

    def "classpaths are different if file hashes are different"() {
        def file = temp.file("a.txt")

        file << "a"; def a = snapshotter.snapshot(new DefaultClassPath(file))
        file << "b"; def b = snapshotter.snapshot(new DefaultClassPath(file))

        expect:
        a != b
        a.hashCode() != b.hashCode()
        a.strongHash != b.strongHash
    }

    def "classpaths are different when there are extra files"() {
        def files = [temp.file("a.txt") << "a", temp.file("b.txt") << "b"]
        def a = snapshotter.snapshot(new DefaultClassPath(files))
        def fileC = temp.file("c.txt") << "c"
        def b = snapshotter.snapshot(new DefaultClassPath(files + [fileC]))

        expect:
        a != b
        a.hashCode() != b.hashCode()
        a.strongHash != b.strongHash
    }

    def "classpaths are equal when file names don't match but content is the same"() {
        def fa = temp.file("a.jar") << "a"
        def fb = temp.file("b.jar") << "a" //same content

        def a = snapshotter.snapshot(new DefaultClassPath(fa))
        def b = snapshotter.snapshot(new DefaultClassPath(fb))

        expect:
        a == b
        a.hashCode() == b.hashCode()
        a.strongHash == b.strongHash
    }

    def "classpaths are different when files have different order"() {
        def fa = temp.file("a.txt") << "a"
        def fb = temp.file("b.txt") << "ab"

        def a = snapshotter.snapshot(new DefaultClassPath(fa, fb))
        def b = snapshotter.snapshot(new DefaultClassPath(fb, fa))

        expect:
        a != b
        a.hashCode() != b.hashCode()
        a.strongHash != b.strongHash
    }

    def "classpaths are equal if all files are the same"() {
        def files = [temp.file("a.txt") << "a", temp.file("b.txt") << "b"]
        def a = snapshotter.snapshot(new DefaultClassPath(files))
        def b = snapshotter.snapshot(new DefaultClassPath(files))

        expect:
        a == b
        a.hashCode() == b.hashCode()
        a.strongHash == b.strongHash
    }

    def "classpaths are equal if dirs are the same"() {
        temp.file("dir/a.txt") << "a"
        temp.file("dir/b.txt") << "b"
        temp.file("dir/dir2/c.txt") << "c"
        def a = snapshotter.snapshot(new DefaultClassPath(temp.createDir("dir")))
        def b = snapshotter.snapshot(new DefaultClassPath(temp.createDir("dir")))

        expect:
        a == b
        a.hashCode() == b.hashCode()
        a.strongHash == b.strongHash
    }

    def "classpaths are different if dirs contain extra files"() {
        temp.file("dir/a.txt") << "a"
        temp.file("dir/b.txt") << "b"
        temp.file("dir/dir2/c.txt") << "c"
        def a = snapshotter.snapshot(new DefaultClassPath(temp.createDir("dir")))
        temp.file("dir/b2.txt") << "b"
        def b = snapshotter.snapshot(new DefaultClassPath(temp.createDir("dir")))

        expect:
        a != b
        a.hashCode() != b.hashCode()
        a.strongHash != b.strongHash
    }

    def "classpaths are equal if dir names are different but content is the same"() {
        temp.file("dir1/a.txt") << "a"
        temp.file("dir2/a.txt") << "a"
        def a = snapshotter.snapshot(new DefaultClassPath(temp.createDir("dir1")))
        def b = snapshotter.snapshot(new DefaultClassPath(temp.createDir("dir2")))

        expect:
        a == b
        a.hashCode() == b.hashCode()
        a.strongHash == b.strongHash
    }

    def "classpaths are the same for 2 empty dirs"() {
        def a = snapshotter.snapshot(new DefaultClassPath(temp.createDir("dir1")))
        def b = snapshotter.snapshot(new DefaultClassPath(temp.createDir("dir2")))

        expect:
        a == b
        a.hashCode() == b.hashCode()
        a.strongHash == b.strongHash
    }

    def "ignores empty directories"() {
        temp.createFile("dir1/a.txt") << "a"
        def a = snapshotter.snapshot(new DefaultClassPath(temp.createDir("dir1")))
        temp.createDir("dir1/empty/child").createDir()
        def b = snapshotter.snapshot(new DefaultClassPath(temp.createDir("empty1"), temp.createDir("dir1"), temp.createDir("empty2")))

        expect:
        a == b
        a.hashCode() == b.hashCode()
        a.strongHash == b.strongHash
    }

    def "snapshots are the same for 2 missing files"() {
        when:
        def s1 = snapshotter.snapshot(new DefaultClassPath(new File(temp.createDir("dir1"), "missing")));
        def s2 = snapshotter.snapshot(new DefaultClassPath(new File(temp.createDir("dir2"), "missing")));

        then:
        s1 == s2
        s1.hashCode() == s2.hashCode()
        s1.strongHash == s2.strongHash
    }

    def "ignores missing files"() {
        temp.createFile("dir1/a.txt") << "a"
        def a = snapshotter.snapshot(new DefaultClassPath(temp.createDir("dir1")))
        def b = snapshotter.snapshot(new DefaultClassPath(temp.file("missing1"), temp.createDir("dir1"), temp.file("missing2")))

        expect:
        a == b
        a.hashCode() == b.hashCode()
        a.strongHash == b.strongHash
    }
}