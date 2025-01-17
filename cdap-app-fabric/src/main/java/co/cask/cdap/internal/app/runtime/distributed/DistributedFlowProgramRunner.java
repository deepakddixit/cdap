/*
 * Copyright © 2014-2015 Cask Data, Inc.
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
package co.cask.cdap.internal.app.runtime.distributed;

import co.cask.cdap.api.app.ApplicationSpecification;
import co.cask.cdap.api.flow.Flow;
import co.cask.cdap.api.flow.FlowSpecification;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.app.runtime.ProgramController;
import co.cask.cdap.app.runtime.ProgramOptions;
import co.cask.cdap.app.runtime.ProgramRunner;
import co.cask.cdap.common.app.RunIds;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.queue.QueueName;
import co.cask.cdap.common.twill.AbortOnTimeoutEventHandler;
import co.cask.cdap.data2.transaction.queue.QueueAdmin;
import co.cask.cdap.data2.transaction.stream.StreamAdmin;
import co.cask.cdap.internal.app.runtime.ProgramOptionConstants;
import co.cask.cdap.internal.app.runtime.flow.FlowUtils;
import co.cask.cdap.proto.ProgramType;
import co.cask.cdap.security.TokenSecureStoreUpdater;
import co.cask.tephra.TransactionExecutorFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.twill.api.EventHandler;
import org.apache.twill.api.RunId;
import org.apache.twill.api.TwillController;
import org.apache.twill.api.TwillRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * A {@link ProgramRunner} to start a {@link Flow} program in distributed mode.
 */
public final class DistributedFlowProgramRunner extends AbstractDistributedProgramRunner {

  private static final Logger LOG = LoggerFactory.getLogger(DistributedFlowProgramRunner.class);

  private final QueueAdmin queueAdmin;
  private final StreamAdmin streamAdmin;
  private final TransactionExecutorFactory txExecutorFactory;

  @Inject
  DistributedFlowProgramRunner(TwillRunner twillRunner, YarnConfiguration hConf,
                               CConfiguration cConfig, QueueAdmin queueAdmin, StreamAdmin streamAdmin,
                               TransactionExecutorFactory txExecutorFactory,
                               TokenSecureStoreUpdater tokenSecureStoreUpdater) {
    super(twillRunner,  hConf, cConfig, tokenSecureStoreUpdater);
    this.queueAdmin = queueAdmin;
    this.streamAdmin = streamAdmin;
    this.txExecutorFactory = txExecutorFactory;
  }

  @Override
  protected ProgramController launch(Program program, ProgramOptions options,
                                     Map<String, LocalizeResource> localizeResources,
                                     File tempDir, ApplicationLauncher launcher) {
    // Extract and verify parameters
    ApplicationSpecification appSpec = program.getApplicationSpecification();
    Preconditions.checkNotNull(appSpec, "Missing application specification.");

    ProgramType processorType = program.getType();
    Preconditions.checkNotNull(processorType, "Missing processor type.");
    Preconditions.checkArgument(processorType == ProgramType.FLOW, "Only FLOW process type is supported.");

    try {
      FlowSpecification flowSpec = appSpec.getFlows().get(program.getName());
      Preconditions.checkNotNull(flowSpec, "Missing FlowSpecification for %s", program.getName());

      LOG.info("Configuring flowlets queues");
      Multimap<String, QueueName> flowletQueues = FlowUtils.configureQueue(program, flowSpec,
                                                                           streamAdmin, queueAdmin, txExecutorFactory);

      // Launch flowlet program runners
      LOG.info("Launching distributed flow: " + program.getName() + ":" + flowSpec.getName());

      TwillController controller = launcher.launch(new FlowTwillApplication(program, flowSpec,
                                                                            localizeResources, eventHandler));
      DistributedFlowletInstanceUpdater instanceUpdater =
        new DistributedFlowletInstanceUpdater(program, controller, queueAdmin,
                                              streamAdmin, flowletQueues, txExecutorFactory);
      RunId runId = RunIds.fromString(options.getArguments().getOption(ProgramOptionConstants.RUN_ID));
      return new FlowTwillProgramController(program.getId(), controller, instanceUpdater, runId).startListen();
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  protected EventHandler createEventHandler(CConfiguration cConf) {
    return new AbortOnTimeoutEventHandler(
      cConf.getLong(Constants.CFG_TWILL_NO_CONTAINER_TIMEOUT, Long.MAX_VALUE), true);
  }
}
