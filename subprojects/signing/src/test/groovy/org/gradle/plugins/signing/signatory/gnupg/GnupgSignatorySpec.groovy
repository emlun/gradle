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
import spock.lang.Specification

class GnupgSignatorySpec extends Specification {

  def 'Constructor requires a nonempty keyName.'() {
    when:
    new GnupgSignatory(Mock(Project), null, Mock(GnupgSettings))

    then:
    thrown(IllegalArgumentException)

    when:
    new GnupgSignatory(Mock(Project), null, Mock(GnupgSettings) {
        getKeyName() >> ""
    })

    then:
    thrown(IllegalArgumentException)
  }

  def 'getInputProperty() returns the keyName.'() {
    when:
    def signatory = new GnupgSignatory(Mock(Project), null, Mock(GnupgSettings) {
        getKeyName() >> 'C001C0DE'
    })

    then:
    signatory.name == 'C001C0DE'
  }

}
