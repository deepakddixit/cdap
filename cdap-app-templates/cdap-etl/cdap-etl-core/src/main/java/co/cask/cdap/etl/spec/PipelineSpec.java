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

package co.cask.cdap.etl.spec;

import co.cask.cdap.api.Resources;
import co.cask.cdap.etl.proto.Connection;
import co.cask.cdap.etl.proto.v2.ETLConfig;
import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.Set;

/**
 * Specification for a pipeline. The application should get this from the {@link PipelineSpecGenerator} in order
 * to ensure that the spec is validated and created correctly.
 *
 * This is like an {@link ETLConfig} but its stages contain additional information calculated at configure time of
 * the application, like input and output schemas of each stage and the artifact selected for each plugin.
 */
public class PipelineSpec {
  private final Set<StageSpec> stages;
  private final Set<Connection> connections;
  private final Resources resources;
  private final boolean stageLoggingEnabled;

  public PipelineSpec(Set<StageSpec> stages,
                      Set<Connection> connections,
                      Resources resources,
                      boolean stageLoggingEnabled) {
    this.stages = ImmutableSet.copyOf(stages);
    this.connections = ImmutableSet.copyOf(connections);
    this.resources = resources;
    this.stageLoggingEnabled = stageLoggingEnabled;
  }

  public Set<StageSpec> getStages() {
    return stages;
  }

  public Set<Connection> getConnections() {
    return connections;
  }

  public Resources getResources() {
    return resources;
  }

  public boolean isStageLoggingEnabled() {
    return stageLoggingEnabled;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PipelineSpec that = (PipelineSpec) o;

    return Objects.equals(stages, that.stages) &&
      Objects.equals(connections, that.connections) &&
      Objects.equals(resources, that.resources) &&
      Objects.equals(stageLoggingEnabled, that.stageLoggingEnabled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stages, connections, resources, stageLoggingEnabled);
  }

  @Override
  public String toString() {
    return "PipelineSpec{" +
      "stages=" + stages +
      ", connections=" + connections +
      ", resources=" + resources +
      ", stageLoggingEnabled=" + stageLoggingEnabled +
      '}';
  }
}
