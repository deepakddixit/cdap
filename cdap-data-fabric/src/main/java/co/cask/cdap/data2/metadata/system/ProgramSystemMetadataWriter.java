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

package co.cask.cdap.data2.metadata.system;

import co.cask.cdap.api.ProgramSpecification;
import co.cask.cdap.api.workflow.WorkflowSpecification;
import co.cask.cdap.data2.metadata.store.MetadataStore;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.ProgramType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Map;

/**
 * A {@link AbstractSystemMetadataWriter} for a {@link Id.Program program}.
 */
public class ProgramSystemMetadataWriter extends AbstractSystemMetadataWriter {
  private final Id.Program programId;
  private final ProgramSpecification programSpec;

  public ProgramSystemMetadataWriter(MetadataStore metadataStore, Id.Program programId,
                                     ProgramSpecification programSpec) {
    super(metadataStore, programId);
    this.programId = programId;
    this.programSpec = programSpec;
  }

  @Override
  protected Map<String, String> getSystemPropertiesToAdd() {
    return ImmutableMap.of();
  }

  @Override
  protected String[] getSystemTagsToAdd() {
    List<String> tags = ImmutableList.<String>builder()
      .add(programId.getId())
      .add(programId.getType().getPrettyName())
      .add(getMode())
      .addAll(getWorkflowNodes())
      .build();
    return tags.toArray(new String[tags.size()]);
  }

  private String getMode() {
    switch (programId.getType()) {
      case MAPREDUCE:
      case SPARK:
      case WORKFLOW:
      case CUSTOM_ACTION:
        return "Batch";
      case FLOW:
      case WORKER:
      case SERVICE:
        return "Realtime";
      default:
        throw new IllegalArgumentException("Unknown program type " + programId.getType());
    }
  }

  private Iterable<String> getWorkflowNodes() {
    if (ProgramType.WORKFLOW != programId.getType()) {
      return ImmutableSet.of();
    }
    Preconditions.checkArgument(programSpec instanceof WorkflowSpecification,
                                "Expected programSpec %s to be of type WorkflowSpecification", programSpec);
    return ((WorkflowSpecification) programSpec).getNodeIdMap().keySet();
  }
}
