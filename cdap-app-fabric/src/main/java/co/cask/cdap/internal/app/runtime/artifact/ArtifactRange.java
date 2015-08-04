/*
 * Copyright © 2015 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.internal.app.runtime.artifact;

import co.cask.cdap.internal.artifact.ArtifactVersion;
import co.cask.cdap.proto.Id;

import java.util.Objects;

/**
 * Represents a range of versions for an artifact. The lower version is inclusive and the upper version is exclusive.
 */
public class ArtifactRange {
  private final Id.Namespace namespace;
  private final String name;
  private final ArtifactVersion lower;
  private final ArtifactVersion upper;
  private final boolean isLowerInclusive;
  private final boolean isUpperInclusive;

  public ArtifactRange(Id.Namespace namespace, String name, ArtifactVersion lower, ArtifactVersion upper) {
    this(namespace, name, lower, true, upper, false);
  }

  public ArtifactRange(Id.Namespace namespace, String name, ArtifactVersion lower, boolean isLowerInclusive,
                       ArtifactVersion upper, boolean isUpperInclusives) {
    this.namespace = namespace;
    this.name = name;
    this.lower = lower;
    this.upper = upper;
    this.isLowerInclusive = isLowerInclusive;
    this.isUpperInclusive = isUpperInclusives;
  }

  public Id.Namespace getNamespace() {
    return namespace;
  }

  public String getName() {
    return name;
  }

  public ArtifactVersion getLower() {
    return lower;
  }

  public ArtifactVersion getUpper() {
    return upper;
  }

  public boolean versionIsInRange(ArtifactVersion version) {
    int lowerCompare = version.compareTo(lower);
    boolean lowerSatisfied = isLowerInclusive ? lowerCompare >= 0 : lowerCompare > 0;
    int upperCompare = version.compareTo(upper);
    boolean upperSatisfied = isUpperInclusive ? upperCompare <= 0 : upperCompare < 0;
    return lowerSatisfied && upperSatisfied;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ArtifactRange that = (ArtifactRange) o;

    return Objects.equals(namespace, that.namespace) &&
      Objects.equals(name, that.name) &&
      Objects.equals(lower, that.lower) &&
      Objects.equals(upper, that.upper) &&
      isLowerInclusive == that.isLowerInclusive &&
      isUpperInclusive == that.isUpperInclusive;
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespace, name, lower, isLowerInclusive, upper, isUpperInclusive);
  }

  @Override
  public String toString() {
    return new StringBuilder()
      .append(name)
      .append(isLowerInclusive ? '[' : '(')
      .append(lower.getVersion())
      .append(',')
      .append(upper.getVersion())
      .append(isUpperInclusive ? ']' : ')')
      .toString();
  }

  /**
   * Parses a string representation of an artifact range. It is expected to be of the form:
   * {name}[{lower-version},{upper-version}]. Square brackets are inclusive, and parantheses are exclusive.
   * For example, my-functions[1.0.0,2.0.0) will correspond to an artifact name of my-functions with a
   * lower version of 1.0.0 and an upper version of 2.0.0.
   *
   * @param namespace the namespace of the artifact range
   * @param artifactRangeStr the string representation to parse
   * @return the ArtifactRange corresponding to the given string
   * @throws InvalidArtifactRangeException if the string is malformed, or if the lower version is higher than the upper
   */
  public static ArtifactRange parse(Id.Namespace namespace,
                                    String artifactRangeStr) throws InvalidArtifactRangeException {
    artifactRangeStr = artifactRangeStr.trim();

    // search for the '[' or '(' between the artifact name and lower version
    int versionStartIndex = indexOf(artifactRangeStr, '[', '(', 0);
    if (versionStartIndex < 0) {
      throw new InvalidArtifactRangeException(String.format("Malformed artifact range %s. " +
        "Could not find '[' or '(' indicating start of artifact lower version.", artifactRangeStr));
    }
    String name = artifactRangeStr.substring(0, versionStartIndex);
    boolean isLowerInclusive = artifactRangeStr.charAt(versionStartIndex) == '[';

    // search for the comma separating versions
    int commaIndex = artifactRangeStr.indexOf(',', versionStartIndex + 1);
    if (commaIndex < 0) {
      throw new InvalidArtifactRangeException(String.format("Malformed artifact range %s. " +
        "Could not find ',' separating lower and upper verions.", artifactRangeStr));
    }
    String lowerStr = artifactRangeStr.substring(versionStartIndex + 1, commaIndex);
    ArtifactVersion lower = new ArtifactVersion(lowerStr);
    if (lower.getVersion() == null) {
      throw new InvalidArtifactRangeException(String.format(
        "Malformed artifact range %s. Lower version %s is invalid.", artifactRangeStr, lowerStr));
    }

    // search for the ']' or ')' marking the end of the upper version
    int versionEndIndex = indexOf(artifactRangeStr, ']', ')', commaIndex + 1);
    if (versionEndIndex < 0) {
      throw new InvalidArtifactRangeException(String.format(
        "Malformed artifact range %s. Could not find enclosing ']' or ')'.", artifactRangeStr));
    }
    String upperStr = artifactRangeStr.substring(commaIndex + 1, versionEndIndex);
    ArtifactVersion upper = new ArtifactVersion(upperStr);
    if (upper.getVersion() == null) {
      throw new InvalidArtifactRangeException(String.format(
        "Malformed artifact range %s. Upper version %s is invalid.", artifactRangeStr, upperStr));
    }
    boolean isUpperInclusive = artifactRangeStr.charAt(versionEndIndex) == ']';

    // for example: 'Artifact-Extends: etl-batch-1.0.0:2.0.0,etl-realtime-1.0.0:3.0.0
    return new ArtifactRange(namespace, name, lower, isLowerInclusive, upper, isUpperInclusive);
  }

  // like String's indexOf(char, int), except it looks for either one of 2 characters
  private static int indexOf(String str, char option1, char option2, int startIndex) {
    for (int i = startIndex; i < str.length(); i++) {
      char charAtIndex = str.charAt(i);
      if (charAtIndex == option1 || charAtIndex == option2) {
        return i;
      }
    }
    return -1;
  }
}