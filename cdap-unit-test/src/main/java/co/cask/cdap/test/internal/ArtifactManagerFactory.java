/*
 * Copyright © 2016 Cask Data, Inc.
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

package co.cask.cdap.test.internal;

import co.cask.cdap.proto.id.NamespacedArtifactId;
import co.cask.cdap.test.ArtifactManager;
import com.google.inject.Guice;
import com.google.inject.assistedinject.Assisted;

/**
 * A {@link Guice} factory to create {@link ArtifactManager}.
 */
public interface ArtifactManagerFactory {
  /**
   * Creates a {@link ArtifactManager} for the specified {@link NamespacedArtifactId artifact}.
   */
  ArtifactManager create(@Assisted("artifactId") NamespacedArtifactId artifactId);
}
