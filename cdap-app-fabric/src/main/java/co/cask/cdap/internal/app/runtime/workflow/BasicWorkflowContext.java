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
package co.cask.cdap.internal.app.runtime.workflow;

import co.cask.cdap.api.metrics.Metrics;
import co.cask.cdap.api.metrics.MetricsCollectionService;
import co.cask.cdap.api.metrics.MetricsContext;
import co.cask.cdap.api.plugin.Plugin;
import co.cask.cdap.api.workflow.WorkflowActionSpecification;
import co.cask.cdap.api.workflow.WorkflowContext;
import co.cask.cdap.api.workflow.WorkflowNodeState;
import co.cask.cdap.api.workflow.WorkflowSpecification;
import co.cask.cdap.api.workflow.WorkflowToken;
import co.cask.cdap.app.metrics.ProgramUserMetrics;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.app.runtime.Arguments;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.internal.app.runtime.AbstractContext;
import co.cask.cdap.internal.app.runtime.plugin.PluginInstantiator;
import co.cask.tephra.TransactionSystemClient;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.twill.api.RunId;
import org.apache.twill.discovery.DiscoveryServiceClient;

import java.util.HashSet;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Default implementation of a {@link WorkflowContext}.
 */
final class BasicWorkflowContext extends AbstractContext implements WorkflowContext {

  private final WorkflowSpecification workflowSpec;
  private final WorkflowActionSpecification specification;
  private final ProgramWorkflowRunner programWorkflowRunner;
  private final Map<String, String> runtimeArgs;
  private final WorkflowToken token;
  private final Metrics userMetrics;
  private final Map<String, WorkflowNodeState> nodeStates;
  private boolean success = false;

  BasicWorkflowContext(WorkflowSpecification workflowSpec, @Nullable WorkflowActionSpecification spec,
                       @Nullable ProgramWorkflowRunner programWorkflowRunner,
                       Arguments arguments, WorkflowToken token, Program program, RunId runId,
                       MetricsCollectionService metricsCollectionService,
                       DatasetFramework datasetFramework, TransactionSystemClient txClient,
                       DiscoveryServiceClient discoveryServiceClient, Map<String, WorkflowNodeState> nodeStates,
                       @Nullable PluginInstantiator pluginInstantiator) {
    super(program, runId, arguments,
          (spec == null) ? new HashSet<String>() : spec.getDatasets(),
          getMetricCollector(program, runId.getId(), metricsCollectionService),
          datasetFramework, txClient, discoveryServiceClient, false, pluginInstantiator);
    this.workflowSpec = workflowSpec;
    this.specification = spec;
    this.programWorkflowRunner = programWorkflowRunner;
    this.runtimeArgs = ImmutableMap.copyOf(arguments.asMap());
    this.token = token;
    if (metricsCollectionService != null) {
      this.userMetrics = new ProgramUserMetrics(getProgramMetrics());
    } else {
      this.userMetrics = null;
    }
    this.nodeStates = nodeStates;
  }

  @Nullable
  private static MetricsContext getMetricCollector(Program program, String runId,
                                                   @Nullable MetricsCollectionService service) {
    if (service == null) {
      return null;
    }
    Map<String, String> tags = Maps.newHashMap(getMetricsContext(program, runId));

    return service.getContext(tags);
  }

  @Override
  public WorkflowSpecification getWorkflowSpecification() {
    return workflowSpec;
  }

  @Override
  public WorkflowActionSpecification getSpecification() {
    if (specification == null) {
      throw new UnsupportedOperationException("Operation not allowed.");
    }
    return specification;
  }

  @Override
  public Runnable getProgramRunner(String name) {
    if (programWorkflowRunner == null) {
      throw new UnsupportedOperationException("Operation not allowed.");
    }
    return programWorkflowRunner.create(name);
  }

  @Override
  public Metrics getMetrics() {
    return userMetrics;
  }

  @Override
  public Map<String, String> getRuntimeArguments() {
    return runtimeArgs;
  }

  @Override
  public Map<String, Plugin> getPlugins() {
    return null;
  }

  @Override
  public WorkflowToken getToken() {
    return token;
  }

  @Override
  public Map<String, WorkflowNodeState> getNodeStates() {
    return ImmutableMap.copyOf(nodeStates);
  }

  /**
   * Sets the success flag if execution of the program associated with current context succeeds.
   */
  void setSuccess() {
    success = true;
  }

  @Override
  public boolean isSuccessful() {
    return success;
  }
}
