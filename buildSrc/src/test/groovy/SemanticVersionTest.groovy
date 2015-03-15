/*
 * Copyright 2015 the original author or authors.
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



import spock.lang.Specification

class SemanticVersionTest extends Specification {

  def "should calculate the next version number"() {
    def semVersion = new SemanticVersion(0, 0, 0)
    semVersion.snapshot = false
    def next = semVersion.next(releaseType)
    next.snapshot = false
    expect:
      next.asText() == expected
    where:
      releaseType       | expected
      ReleaseType.MAJOR | '1.0.0'
      ReleaseType.MINOR | '0.1.0'
      ReleaseType.PATCH | '0.0.1'
      ReleaseType.PATCH | '0.0.1'
  }

  def "should output string versions"() {
    def semVersion = new SemanticVersion(1, 1, 1)
    when:
      semVersion.snapshot = false
    then:
      semVersion.asText() == '1.1.1'
    when:
      semVersion.snapshot = true
    then:
      semVersion.asText() == '1.1.1-SNAPSHOT'
  }

  def "should load from a prop file"() {
    def tempDirLocation = System.getProperty('java.io.tmpdir')
    File propFile = new File(tempDirLocation + 'p.properties')
    propFile << '''
major=1
minor=1
patch=1
'''
    expect:
      def semanticVersion = SemanticVersion.get(propFile)
      semanticVersion.major == 1
      semanticVersion.minor == 1
      semanticVersion.patch == 1
  }
}
