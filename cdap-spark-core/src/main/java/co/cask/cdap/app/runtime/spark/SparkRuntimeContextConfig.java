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

package co.cask.cdap.app.runtime.spark;

import co.cask.cdap.api.app.ApplicationSpecification;
import co.cask.cdap.common.app.RunIds;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.internal.app.ApplicationSpecificationAdapter;
import co.cask.cdap.internal.app.runtime.workflow.WorkflowProgramInfo;
import co.cask.cdap.proto.id.ProgramId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.hadoop.conf.Configuration;
import org.apache.twill.api.RunId;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Helper class for getting and setting configurations that are needed for recreating the runtime context
 * in each Spark executor.
 */
public class SparkRuntimeContextConfig {

  private static final Gson GSON = ApplicationSpecificationAdapter.addTypeAdapters(new GsonBuilder()).create();
  private static final Type ARGS_TYPE = new TypeToken<Map<String, String>>() { }.getType();

  /**
   * Configuration key for boolean value to tell whether Spark program is executed on a cluster or not.
   */
  public static final String HCONF_ATTR_CLUSTER_MODE = "cdap.spark.cluster.mode";

  private static final String HCONF_ATTR_APP_SPEC = "cdap.spark.app.spec";
  private static final String HCONF_ATTR_PROGRAM_ID = "cdap.spark.program.id";
  private static final String HCONF_ATTR_RUN_ID = "cdap.spark.run.id";
  private static final String HCONF_ATTR_ARGS = "cdap.spark.program.args";
  private static final String HCONF_ATTR_WORKFLOW_INFO = "cdap.spark.program.workflow.info";
  private static final String HCONF_ATTR_LOCAL_RESOURCES = "cdap.spark.local.resources";

  private final Configuration hConf;

  /**
   * Returns {@code true} if running in local mode.
   */
  static boolean isLocal(Configuration hConf) {
    return !hConf.getBoolean(HCONF_ATTR_CLUSTER_MODE, false);
  }

  /**
   * Creates a new instance from the given configuration.
   */
  SparkRuntimeContextConfig(Configuration hConf) {
    this.hConf = hConf;
  }

  /**
   * Returns the configuration.
   */
  public Configuration getConfiguration() {
    return hConf;
  }

  /**
   * Returns true if in local mode.
   */
  public boolean isLocal() {
    return isLocal(hConf);
  }

  /**
   * Sets configurations based on the given context.
   */
  public SparkRuntimeContextConfig set(SparkRuntimeContext context, Set<String> localizeResourceNames,
                                       @Nullable File pluginArchive) {
    setApplicationSpecification(context.getApplicationSpecification());
    setProgramId(context.getProgram().getId().toEntityId());
    setRunId(context.getRunId().getId());
    setArguments(context.getRuntimeArguments());
    setWorkflowProgramInfo(context.getWorkflowInfo());
    setLocalizedResourceNames(localizeResourceNames);
    setPluginArchive(pluginArchive);

    return this;
  }

  /**
   * @return the {@link ApplicationSpecification} stored in the configuration.
   */
  public ApplicationSpecification getApplicationSpecification() {
    return GSON.fromJson(hConf.get(HCONF_ATTR_APP_SPEC), ApplicationSpecification.class);
  }

  /**
   * @return the {@link ProgramId} stored in the configuration.
   */
  public ProgramId getProgramId() {
    return GSON.fromJson(hConf.get(HCONF_ATTR_PROGRAM_ID), ProgramId.class);
  }

  /**
   * @return the {@link RunId} stored in the configuration.
   */
  public RunId getRunId() {
    return RunIds.fromString(hConf.get(HCONF_ATTR_RUN_ID));
  }

  /**
   * @return the runtime arguments stored in the configuration.
   */
  public Map<String, String> getArguments() {
    return GSON.fromJson(hConf.get(HCONF_ATTR_ARGS), ARGS_TYPE);
  }

  /**
   * @return the {@link WorkflowProgramInfo} if it is running inside Workflow or {@code null} if not.
   */
  @Nullable
  public WorkflowProgramInfo getWorkflowProgramInfo() {
    String info = hConf.get(HCONF_ATTR_WORKFLOW_INFO);
    if (info == null) {
      return null;
    }
    WorkflowProgramInfo workflowProgramInfo = GSON.fromJson(info, WorkflowProgramInfo.class);
    workflowProgramInfo.getWorkflowToken().disablePut();
    return workflowProgramInfo;
  }

  /**
   * @return the set of localized resource names stored in the configuration.
   */
  public Set<String> getLocalizedResourceNames() {
    return GSON.fromJson(hConf.get(HCONF_ATTR_LOCAL_RESOURCES), new TypeToken<Set<String>>() { }.getType());
  }

  /**
   * @return the name of the plugin archive file stored in the configuration.
   */
  @Nullable
  public String getPluginArchive() {
    return hConf.get(Constants.Plugin.ARCHIVE);
  }

  /**
   * Serialize the {@link ApplicationSpecification} to the configuration.
   */
  private void setApplicationSpecification(ApplicationSpecification spec) {
    hConf.set(HCONF_ATTR_APP_SPEC, GSON.toJson(spec, ApplicationSpecification.class));
  }

  /**
   * Serialize the {@link ProgramId} to the configuration.
   */
  private void setProgramId(ProgramId programId) {
    hConf.set(HCONF_ATTR_PROGRAM_ID, GSON.toJson(programId));
  }

  /**
   * Serialize the {@link RunId} to the configuration.
   */
  private void setRunId(String runId) {
    hConf.set(HCONF_ATTR_RUN_ID, runId);
  }

  /**
   * Serialize the runtime arguments to the configuration.
   */
  private void setArguments(Map<String, String> runtimeArgs) {
    hConf.set(HCONF_ATTR_ARGS, GSON.toJson(runtimeArgs, ARGS_TYPE));
  }

  /**
   * Serialize the {@link WorkflowProgramInfo} to the configuration if available.
   */
  private void setWorkflowProgramInfo(@Nullable WorkflowProgramInfo info) {
    if (info != null) {
      hConf.set(HCONF_ATTR_WORKFLOW_INFO, GSON.toJson(info));
    }
  }

  /**
   * Serialize the set of localize resource names to the configuration.
   */
  private void setLocalizedResourceNames(Set<String> names) {
    hConf.set(HCONF_ATTR_LOCAL_RESOURCES, GSON.toJson(names));
  }

  /**
   * Saves the plugin archive file name to the configuration.
   */
  private void setPluginArchive(@Nullable File pluginArchive) {
    if (pluginArchive != null) {
      hConf.set(Constants.Plugin.ARCHIVE, pluginArchive.getName());
    }
  }
}
