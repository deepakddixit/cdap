/*
 * Copyright © 2015 Cask Data, Inc.
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

package co.cask.cdap.etl.batch;

import co.cask.cdap.common.utils.Networks;
import co.cask.cdap.etl.mock.batch.MockSink;
import co.cask.cdap.etl.mock.batch.MockSource;
import co.cask.cdap.etl.proto.v2.ETLBatchConfig;
import co.cask.cdap.etl.proto.v2.ETLPlugin;
import co.cask.cdap.etl.proto.v2.ETLStage;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.artifact.AppRequest;
import co.cask.cdap.test.ApplicationManager;
import co.cask.cdap.test.WorkflowManager;
import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Test for {@link EmailAction}
 */
public class ETLEmailActionTestRun extends ETLBatchTestBase {

  private SimpleSmtpServer server;
  private int port;

  @Before
  public void beforeTest() {
    port = Networks.getRandomPort();
    server = SimpleSmtpServer.start(port);
  }

  @Test
  public void testEmailAction() throws Exception {

    ETLPlugin actionConfig = new ETLPlugin("Email",
                                           "action",
                                           ImmutableMap.of(EmailAction.RECIPIENT_EMAIL_ADDRESS, "to@test.com",
                                                           EmailAction.FROM_ADDRESS, "from@test.com",
                                                           EmailAction.MESSAGE, "testing body",
                                                           EmailAction.SUBJECT, "Test",
                                                           EmailAction.PORT, Integer.toString(port)),
                                           null);

    ETLBatchConfig etlConfig = ETLBatchConfig.builder("* * * * *")
      .addStage(new ETLStage("source", MockSource.getPlugin("inputTable")))
      .addStage(new ETLStage("sink", MockSink.getPlugin("outputTable")))
      .addAction(new ETLStage("action", actionConfig))
      .addConnection("source", "sink")
      .build();

    AppRequest<ETLBatchConfig> appRequest = new AppRequest<>(APP_ARTIFACT, etlConfig);
    Id.Application appId = Id.Application.from(Id.Namespace.DEFAULT, "actionTest");
    ApplicationManager appManager = deployApplication(appId, appRequest);

    WorkflowManager manager = appManager.getWorkflowManager(ETLWorkflow.NAME);
    manager.start();
    manager.waitForFinish(5, TimeUnit.MINUTES);

    server.stop();

    Assert.assertEquals(1, server.getReceivedEmailSize());
    Iterator emailIter = server.getReceivedEmail();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    Assert.assertEquals("Test", email.getHeaderValue("Subject"));
    Assert.assertTrue(email.getBody().startsWith("testing body"));
    Assert.assertFalse(emailIter.hasNext());
  }
}
