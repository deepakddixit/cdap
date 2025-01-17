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

import co.cask.cdap.api.RuntimeContext;
import co.cask.cdap.api.common.RuntimeArguments;
import co.cask.cdap.api.common.Scope;
import co.cask.cdap.api.workflow.NodeStatus;
import co.cask.cdap.api.workflow.WorkflowNodeState;
import co.cask.cdap.api.workflow.WorkflowSpecification;
import co.cask.cdap.api.workflow.WorkflowToken;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.app.program.Programs;
import co.cask.cdap.app.runtime.Arguments;
import co.cask.cdap.app.runtime.ProgramController;
import co.cask.cdap.app.runtime.ProgramOptions;
import co.cask.cdap.app.runtime.ProgramRunner;
import co.cask.cdap.app.runtime.ProgramRunnerFactory;
import co.cask.cdap.app.runtime.WorkflowTokenProvider;
import co.cask.cdap.common.app.RunIds;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.lang.ProgramClassLoader;
import co.cask.cdap.internal.app.program.ForwardingProgram;
import co.cask.cdap.internal.app.runtime.AbstractListener;
import co.cask.cdap.internal.app.runtime.BasicArguments;
import co.cask.cdap.internal.app.runtime.ProgramOptionConstants;
import co.cask.cdap.internal.app.runtime.SimpleProgramOptions;
import co.cask.cdap.proto.ProgramType;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.Gson;
import org.apache.twill.common.Threads;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * An Abstract class implementing {@link ProgramWorkflowRunner}, providing a {@link Runnable} of
 * the programs to run in a workflow.
 * <p>
 * Programs that extend this class (such as {@link MapReduceProgramWorkflowRunner} or
 * {@link SparkProgramWorkflowRunner}) can execute their associated programs through the
 * {@link AbstractProgramWorkflowRunner#blockForCompletion} by providing the {@link ProgramController} and
 * {@link RuntimeContext} that they obtained through the {@link ProgramRunner}.
 * </p>
 * The {@link RuntimeContext} is blocked until completion of the associated program.
 */
abstract class AbstractProgramWorkflowRunner implements ProgramWorkflowRunner {

  private static final Gson GSON = new Gson();

  private final CConfiguration cConf;
  private final Arguments userArguments;
  private final Arguments systemArguments;
  private final Program workflowProgram;
  private final String nodeId;
  private final Map<String, WorkflowNodeState> nodeStates;
  protected final WorkflowSpecification workflowSpec;
  protected final ProgramRunnerFactory programRunnerFactory;
  protected final WorkflowToken token;

  AbstractProgramWorkflowRunner(CConfiguration cConf, Program workflowProgram, ProgramOptions workflowProgramOptions,
                                ProgramRunnerFactory programRunnerFactory, WorkflowSpecification workflowSpec,
                                WorkflowToken token, String nodeId, Map<String, WorkflowNodeState> nodeStates) {
    this.cConf = cConf;
    this.userArguments = workflowProgramOptions.getUserArguments();
    this.workflowProgram = workflowProgram;
    this.programRunnerFactory = programRunnerFactory;
    this.workflowSpec = workflowSpec;
    this.systemArguments = workflowProgramOptions.getArguments();
    this.token = token;
    this.nodeId = nodeId;
    this.nodeStates = nodeStates;
  }

  /**
   * Returns the {@link ProgramType} supported by this runner.
   */
  protected abstract ProgramType getProgramType();

  /**
   * Rewrites the given {@link Program} to a {@link Program} that represents the {@link ProgramType} as
   * returned by {@link #getProgramType()}.
   */
  protected abstract Program rewriteProgram(String name, Program program);

  @Override
  public final Runnable create(String name) {
    try {
      ProgramRunner programRunner = programRunnerFactory.create(getProgramType());
      Program program = rewriteProgram(name, createProgram(programRunner, workflowProgram));
      return getProgramRunnable(name, programRunner, program);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Gets a {@link Runnable} for the {@link Program}.
   *
   * @param name    name of the {@link Program}
   * @param program the {@link Program}
   * @return a {@link Runnable} for this {@link Program}
   */
  private Runnable getProgramRunnable(String name, final ProgramRunner programRunner, final Program program) {
    Map<String, String> systemArgumentsMap = Maps.newHashMap();
    systemArgumentsMap.putAll(systemArguments.asMap());
    // Generate the new RunId here for the program running under Workflow
    systemArgumentsMap.put(ProgramOptionConstants.RUN_ID, RunIds.generate().getId());

    // Add Workflow specific system arguments to be passed to the underlying program
    systemArgumentsMap.put(ProgramOptionConstants.WORKFLOW_NAME, workflowSpec.getName());
    systemArgumentsMap.put(ProgramOptionConstants.WORKFLOW_RUN_ID,
                           systemArguments.getOption(ProgramOptionConstants.RUN_ID));
    systemArgumentsMap.put(ProgramOptionConstants.WORKFLOW_NODE_ID, nodeId);
    systemArgumentsMap.put(ProgramOptionConstants.PROGRAM_NAME_IN_WORKFLOW, name);
    systemArgumentsMap.put(ProgramOptionConstants.WORKFLOW_TOKEN, GSON.toJson(token));

    final ProgramOptions options = new SimpleProgramOptions(
      program.getName(),
      new BasicArguments(ImmutableMap.copyOf(systemArgumentsMap)),
      new BasicArguments(RuntimeArguments.extractScope(Scope.scopeFor(program.getType().getCategoryName()), name,
                                                       userArguments.asMap()))
    );

    return new Runnable() {
      @Override
      public void run() {
        try {
          runAndWait(programRunner, program, options);
        } catch (Exception e) {
          throw Throwables.propagate(e);
        }
      }
    };
  }

  /**
   * Creates a new {@link Program} instance for the execution by the given {@link ProgramRunner}.
   */
  private Program createProgram(ProgramRunner programRunner, Program originalProgram) throws IOException {
    ClassLoader classLoader = originalProgram.getClassLoader();
    if (!(classLoader instanceof ProgramClassLoader)) {
      // If for some reason that the ClassLoader of the original program is not ProgramClassLoader,
      // we don't own the program, hence don't close it when execution of the program completed.
      return new ForwardingProgram(originalProgram) {
        @Override
        public void close() throws IOException {
          // no-op
        }
      };
    }

    return Programs.create(cConf, programRunner, originalProgram.getJarLocation(),
                           ((ProgramClassLoader) classLoader).getDir());
  }

  private void runAndWait(ProgramRunner programRunner, Program program, ProgramOptions options) throws Exception {
    ProgramController controller;
    try {
      controller = programRunner.run(program, options);
    } catch (Throwable t) {
      // If there is any exception when running the program, close the program to release resources.
      // Otherwise it will be released when the execution completed.
      Closeables.closeQuietly(program);
      throw t;
    }
    blockForCompletion(program, controller);

    if (controller instanceof WorkflowTokenProvider) {
      updateWorkflowToken(((WorkflowTokenProvider) controller).getWorkflowToken());
    } else {
      // This shouldn't happen
      throw new IllegalStateException("No WorkflowToken available after program completed: " + program.getId());
    }
  }

  /**
   * Adds a listener to the {@link ProgramController} and blocks for completion.
   *
   * @param program the {@link Program} in execution. It will get closed when the execution completed
   * @param controller the {@link ProgramController} for the program
   * @throws Exception if the execution failed
   */
  private void blockForCompletion(final Program program, final ProgramController controller) throws Exception {
    // Execute the program.
    final SettableFuture<Void> completion = SettableFuture.create();
    controller.addListener(new AbstractListener() {
      @Override
      public void completed() {
        Closeables.closeQuietly(program);
        nodeStates.put(nodeId, new WorkflowNodeState(nodeId, NodeStatus.COMPLETED, controller.getRunId().getId(),
                                                     null));
        completion.set(null);
      }

      @Override
      public void killed() {
        Closeables.closeQuietly(program);
        nodeStates.put(nodeId, new WorkflowNodeState(nodeId, NodeStatus.KILLED, controller.getRunId().getId(), null));
        completion.set(null);
      }

      @Override
      public void error(Throwable cause) {
        Closeables.closeQuietly(program);
        nodeStates.put(nodeId, new WorkflowNodeState(nodeId, NodeStatus.FAILED, controller.getRunId().getId(), cause));
        completion.setException(cause);
      }
    }, Threads.SAME_THREAD_EXECUTOR);

    // Block for completion.
    try {
      completion.get();
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof Exception) {
        throw (Exception) cause;
      }
      throw Throwables.propagate(cause);
    } catch (InterruptedException e) {
      try {
        Futures.getUnchecked(controller.stop());
      } catch (Throwable t) {
        // no-op
      }
      // reset the interrupt
      Thread.currentThread().interrupt();
    }
  }

  private void updateWorkflowToken(WorkflowToken workflowToken) throws Exception {
    ((BasicWorkflowToken) token).mergeToken(workflowToken);
  }
}
