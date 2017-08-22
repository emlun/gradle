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
package org.gradle.plugins.signing.signatory.gnupg

import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class GnupgSignatoryIntegrationSpec extends Specification {

  private static final String KEY_ID = "test@test.org"

  @Rule
  public final TemporaryFolder tmpDir = new TemporaryFolder();

  private File gnupgHome;

  def setup() {
    gnupgHome = tmpDir.newFolder("gnupg")

    ProcessBuilder gpgProcessBuilder = new ProcessBuilder("gpg", "--batch", "--gen-key")
    gpgProcessBuilder.redirectErrorStream(true)
    gpgProcessBuilder.environment().put("GNUPGHOME", gnupgHome.absolutePath)

    Process gpgProcess = gpgProcessBuilder.start()
    IOUtils.copy(getClass().getResourceAsStream("/genkey.gpgbatch"), gpgProcess.getOutputStream())
    try {
      gpgProcess.getOutputStream().close()
    } catch (IOException e) {
      // Ignore
    }

    gpgProcess.waitFor()
  }

  def 'The written signature is valid.'() {
    setup:
    Project project = ProjectBuilder.builder().build()

    URL payload = getClass().getResource("/payload.txt")
    File signatureFile = tmpDir.newFile("payload.txt.sig")

    GnupgSettings settings = new GnupgSettings().with {
        homeDir = gnupgHome
        keyName = KEY_ID
        return it
    }

    OutputStream signatureStream = new FileOutputStream(signatureFile)

    when:
    new GnupgSignatory(project, null, settings).sign(payload.openStream(), signatureStream)

    InputStream gpgOutputInputStream = new PipedInputStream()
    OutputStream gpgOutputStream = new PipedOutputStream(gpgOutputInputStream)
    project.exec {
        executable "gpg"
        args(
            "--homedir", gnupgHome.absolutePath,
            "--verify", signatureFile.getAbsolutePath(), payload.getPath()
        )
        standardOutput = gpgOutputStream
        errorOutput = gpgOutputStream
    }

    String gpgOutput = IOUtils.toString(gpgOutputInputStream, "UTF-8")

    then:
    gpgOutput.contains('Good signature from "Test Testsson <' + KEY_ID + '>"')
  }

}
