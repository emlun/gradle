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

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GnupgSignatoryProviderSpec extends Specification {

  def 'The default Signatory gets its keyName from the signing.keyId project property.'() {
    setup:
    Project project = ProjectBuilder.builder().build()
    project.ext.'signing.keyId' = 'C001C0DE'

    expect:
    new GnupgSignatoryProvider().getDefaultSignatory(project).keyName == 'C001C0DE'
  }

  def 'Named signatories use the given name as the keyId.'() {
    expect:
    new GnupgSignatoryProvider().getSignatory('C001C0DE').keyName == 'C001C0DE'
  }

}
