/*
 * Copyright 2011 the original author or authors.
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

package org.gradle.plugins.signing

import org.apache.commons.io.IOUtils
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.Sample
import org.gradle.integtests.fixtures.UsesSample
import org.gradle.integtests.fixtures.executer.GradleContextualExecuter
import org.gradle.test.fixtures.maven.MavenFileRepository
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import spock.lang.IgnoreIf

class SigningSamplesSpec extends AbstractIntegrationSpec {
    @Rule public final Sample mavenSample = new Sample(temporaryFolder)

    void setup(){
        using m2
    }

    @UsesSample('signing/maven')
    def "upload attaches signatures"() {
        given:
        sample mavenSample

        when:
        run "uploadArchives"

        then:
        repo.module('gradle', 'maven', '1.0').assertArtifactsPublished('maven-1.0.pom', 'maven-1.0.pom.asc', 'maven-1.0.jar', 'maven-1.0.jar.asc')
    }

    @UsesSample('signing/conditional')
    @IgnoreIf({GradleContextualExecuter.parallel})
    def "conditional signing"() {
        given:
        sample mavenSample

        when:
        run "uploadArchives"

        then:
        ":signArchives" in skippedTasks

        and:
        final module = repo.module('gradle', 'conditional', '1.0-SNAPSHOT')
        module.assertArtifactsPublished("maven-metadata.xml", "conditional-${module.publishArtifactVersion}.pom", "conditional-${module.publishArtifactVersion}.jar")
    }

    @UsesSample('signing/gnupg-signatory')
    def "using gnupg signatory generates valid signatures"() {
        given:
        sample mavenSample

        when:
        run "signArchives"

        InputStream gpgOutputInputStream = new PipedInputStream()
        OutputStream gpgOutputStream = new PipedOutputStream(gpgOutputInputStream)
        ProjectBuilder.builder().build().exec {
            executable "gpg"
            args(
                "--homedir", mavenSample.dir.file('gnupg-home').absolutePath,
                "--verify", file("signing", "gnupg-signatory", "build", "libs", "gnupg-signatory-1.0.jar.asc").absolutePath
            )
            standardOutput = gpgOutputStream
            errorOutput = gpgOutputStream
        }

        String gpgOutput = IOUtils.toString(gpgOutputInputStream, "UTF-8")

        then:
        file("signing", "gnupg-signatory", "build", "libs", "gnupg-signatory-1.0.jar.asc").exists()
        gpgOutput.contains('Gradle Test (This is used for testing the gradle-signing-plugin) <test@gradle.org>')
    }

    MavenFileRepository getRepo() {
        return maven(mavenSample.dir.file("build/repo"))
    }
}
