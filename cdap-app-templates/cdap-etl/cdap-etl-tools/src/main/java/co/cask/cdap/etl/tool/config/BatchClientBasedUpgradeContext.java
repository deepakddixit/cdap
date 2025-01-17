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

package co.cask.cdap.etl.tool.config;

import co.cask.cdap.client.ArtifactClient;
import co.cask.cdap.etl.tool.ETLVersion;
import co.cask.cdap.proto.Id;

/**
 * Uses an ArtifactClient to get the artifact for a specific plugin.
 */
public class BatchClientBasedUpgradeContext extends ClientBasedUpgradeContext {

  public BatchClientBasedUpgradeContext(ArtifactClient artifactClient) {
    super(artifactClient, Id.Artifact.from(Id.Namespace.SYSTEM, "cdap-etl-batch", ETLVersion.getVersion()));
  }

}
