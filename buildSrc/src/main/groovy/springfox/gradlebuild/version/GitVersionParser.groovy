package springfox.gradlebuild.version

import com.google.common.base.Optional
import com.google.common.base.Splitter
import com.google.common.collect.Iterables

import static com.google.common.base.Strings.nullToEmpty

// Lifted from plugin 'com.cinnober.gradle:semver-git:2.2.0'
// https://github.com/cinnober/semver-git
trait GitVersionParser {

  def patchComponents(String versionPart) {
    def pattern = /^([0-9]+)(-([0-9]+)-g([0-9a-f]+))?$/
    def matcher = versionPart =~ pattern
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Not a valid version. Expecting a version of form <MAJOR.MINOR.PATCH> where " +
          "e.g. 1.0.0-SNAPSHOT, 1.0.0-1-g10a2eg: $versionPart")
    }
    def patchComponents = matcher.collect { it }[0]
    if (patchComponents.size() < 4) {
      throw new IllegalArgumentException("Not a valid version. Expecting a version of form <MAJOR.MINOR.PATCH> where " +
          "e.g. 1.0.0-SNAPSHOT, 1.0.0-1-g10a2eg: $versionPart")
    }
    Integer patch = patchComponents[1].toInteger()
    Integer count = Optional.fromNullable(patchComponents[3]).or("0").toInteger()
    String sha = patchComponents[4]
    String build = patchComponents[2]?.substring(1)
    [patch, build, count, sha]
  }

  SemanticVersion parseTransform(String version, String buildSuffix) {
    def components = Splitter.on('.').split(version)
    if (Iterables.size(components) < 3) {
      throw new IllegalArgumentException("Not a valid version. Expecting a version of form <MAJOR.MINOR.PATCH> where " +
          "e.g. 1.0.0-SNAPSHOT, 1.0.0-1-g10a2eg: ${version}")
    }
    def versions = components.iterator()
    Integer major = versions.next().toInteger()
    Integer minor = versions.next().toInteger()
    def (Integer patch, String build, Integer count, String sha) = patchComponents(versions.next())
    SemanticVersion parsedVersion = new SemanticVersion(major, minor, patch, "")
    String suffix = buildSuffix;
    if (count == 0) {
      suffix = ""
    } else {
      suffix = suffix.replaceAll("<count>", "$count")
      suffix = suffix.replaceAll("<sha>", nullToEmpty(sha))
    }
    return new SemanticVersion(parsedVersion.major, parsedVersion.minor, patch, suffix)
  }


}
