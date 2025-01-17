/*
 * Copyright © 2014-2016 Cask Data, Inc.
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
package co.cask.cdap.api.workflow;

import co.cask.cdap.api.Predicate;
import co.cask.cdap.api.RuntimeContext;
import co.cask.cdap.api.ServiceDiscoverer;
import co.cask.cdap.api.annotation.Beta;
import co.cask.cdap.api.data.DatasetContext;
import co.cask.cdap.api.plugin.PluginContext;

import java.util.Map;

/**
 * Represents the runtime context of a {@link Workflow}. This context is also
 * available to {@link WorkflowAction}.
 */
public interface WorkflowContext extends RuntimeContext, ServiceDiscoverer, DatasetContext, PluginContext {

  WorkflowSpecification getWorkflowSpecification();

  /**
   * @throws UnsupportedOperationException if it is called from {@link Predicate}
   */
  WorkflowActionSpecification getSpecification();

  long getLogicalStartTime();

  /**
   * Returns a {@link Runnable} that launches the associated program with the specified name when
   * the {@link Runnable#run() run} method is invoked.
   *
   * @throws IllegalArgumentException if no program with the specified name is defined in the workflow
   * @throws UnsupportedOperationException if it is called from {@link Predicate}
   */
  @Deprecated
  Runnable getProgramRunner(String name);

  /**
   * @return A map of the argument's key and value.
   */
  Map<String, String> getRuntimeArguments();

  /**
   * @return a {@link WorkflowToken}
   */
  WorkflowToken getToken();

  /**
   * Return the {@link Map} of node ids to {@link WorkflowNodeState}. This can be used
   * from {@link AbstractWorkflow#destroy} method to determine the status of all nodes
   * executed by the Workflow in the current run.
   */
  @Beta
  Map<String, WorkflowNodeState> getNodeStates();

  /**
   * Return true if the execution was successful, false otherwise. This method can be
   * used from {@link AbstractWorkflow#destroy} to determine the status of the {@link Workflow}.
   * It can also be used from {@link WorkflowAction#destroy} method to determine the status
   * of the {@link WorkflowAction}. If it is called before the execution is completed, false is returned.
   */
  @Beta
  boolean isSuccessful();
}
