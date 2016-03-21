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

package org.apache.hadoop.mapred;

import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.guice.ConfigModule;
import co.cask.cdap.common.guice.IOModule;
import co.cask.cdap.common.guice.KafkaClientModule;
import co.cask.cdap.common.guice.ZKClientModule;
import co.cask.cdap.common.logging.LoggingContextAccessor;
import co.cask.cdap.internal.app.runtime.batch.MapReduceContextConfig;
import co.cask.cdap.logging.appender.LogAppenderInitializer;
import co.cask.cdap.logging.context.MapReduceLoggingContext;
import co.cask.cdap.logging.guice.LoggingModules;
import co.cask.cdap.proto.Id;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.util.ExitUtil;
import org.apache.twill.internal.Services;
import org.apache.twill.kafka.client.KafkaClientService;
import org.apache.twill.zookeeper.ZKClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Wrapper for YarnChild
 */
public class YarnChildWrapper {

  private static final Logger LOG = LoggerFactory.getLogger(YarnChildWrapper.class);
  private static ZKClientService zkClient;
  private static KafkaClientService kafkaClient;
  private static LogAppenderInitializer logAppenderInitializer;
  private static Injector injector;

  public static void initialize() {
    MapReduceContextConfig contextConfig = new MapReduceContextConfig(
      new JobConf(new Path(MRJobConfig.JOB_CONF_FILE)));
    Id.Program programId = Id.fromString(contextConfig.getProgramId(), Id.Program.class);
    MapReduceLoggingContext loggingContext = new MapReduceLoggingContext(programId.getNamespaceId(),
                                                                         programId.getApplicationId(),
                                                                         programId.getId(),
                                                                         contextConfig.getRunId().getId());
    injector = createGuiceInjector(contextConfig.getCConf(), contextConfig.getHConf());
    injector.getInstance(LogAppenderInitializer.class).initialize();
    LoggingContextAccessor.setLoggingContext(loggingContext);
    zkClient = injector.getInstance(ZKClientService.class);
    kafkaClient = injector.getInstance(KafkaClientService.class);
    logAppenderInitializer = injector.getInstance(LogAppenderInitializer.class);
  }

  public static void main (String[] args) {
    try {
      LOG.info("Starting services for {}", YarnChildWrapper.class.getCanonicalName());
      initialize();
      startServices();
      ExitUtil.disableSystemExit(); // Throw Exception on error in YarnChild
      YarnChild.main(args);
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
      throw Throwables.propagate(t);
    } finally {
      try {
        if (logAppenderInitializer != null) {
          logAppenderInitializer.close();
        }
        LOG.info("Stopping services for {}", YarnChildWrapper.class.getCanonicalName());
        stopServices();
      } catch (Throwable t) {
        LOG.error(t.getMessage(), t);
        throw Throwables.propagate(t);
      }
    }
  }

  private static void startServices() throws Exception {

    try {
      List<ListenableFuture<Service.State>> startFutures = Services.chainStart(zkClient, kafkaClient).get();
      // All services should be started
      for (ListenableFuture<Service.State> future : startFutures) {
        Preconditions.checkState(future.get() == Service.State.RUNNING,
                                 "Failed to start services: zkClient %s, kafkaClient %s",
                                 zkClient.state(), kafkaClient.state());
      }
      logAppenderInitializer.initialize();
    } catch (Exception e) {
      // Try our best to stop services. Chain stop guarantees it will stop everything, even some of them failed.
      try {
        stopServices();
      } catch (Exception stopEx) {
        e.addSuppressed(stopEx);
      }
      throw e;
    }
  }

  public static void stopServices() throws Exception {
    Exception failure = null;
    try {
      logAppenderInitializer.close();
    } catch (Exception e) {
      failure = e;
    }
    try {
      Services.chainStop(kafkaClient, zkClient).get();
    } catch (Exception e) {
      if (failure != null) {
        failure.addSuppressed(e);
      } else {
        failure = e;
      }
    }
    if (failure != null) {
      throw failure;
    }
  }

  public static Injector createGuiceInjector(CConfiguration cConf, Configuration hConf) {
    return Guice.createInjector(
      new ConfigModule(cConf, hConf),
      new IOModule(),
      new ZKClientModule(),
      new KafkaClientModule(),
      new LoggingModules().getDistributedModules()
    );
  }

}
