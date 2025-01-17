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

package co.cask.cdap.client.common;

import co.cask.cdap.StandaloneTester;
import co.cask.cdap.cli.util.InstanceURIParser;
import co.cask.cdap.client.ProgramClient;
import co.cask.cdap.client.config.ClientConfig;
import co.cask.cdap.client.config.ConnectionConfig;
import co.cask.cdap.common.NotFoundException;
import co.cask.cdap.common.ProgramNotFoundException;
import co.cask.cdap.common.UnauthenticatedException;
import co.cask.cdap.internal.test.AppJarHelper;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.ProgramRecord;
import co.cask.cdap.proto.ProgramType;
import co.cask.cdap.test.SingletonExternalResource;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.twill.filesystem.LocalLocationFactory;
import org.apache.twill.filesystem.Location;
import org.apache.twill.filesystem.LocationFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.jar.Manifest;

/**
 *
 */
public abstract class ClientTestBase {

  @ClassRule
  public static final SingletonExternalResource STANDALONE = new SingletonExternalResource(new StandaloneTester());

  @ClassRule
  public static final TemporaryFolder TMP_FOLDER = new TemporaryFolder();

  protected ClientConfig clientConfig;

  @Before
  public void setUp() throws Throwable {
    StandaloneTester standalone = STANDALONE.get();
    ConnectionConfig connectionConfig = InstanceURIParser.DEFAULT.parse(standalone.getBaseURI().toString());
    clientConfig = new ClientConfig.Builder()
      .setDefaultReadTimeout(60 * 1000)
      .setUploadReadTimeout(120 * 1000)
      .setConnectionConfig(connectionConfig).build();
  }

  protected ClientConfig getClientConfig() {
    return clientConfig;
  }

  protected void verifyProgramNames(List<String> expected, List<ProgramRecord> actual) {
    Assert.assertEquals(expected.size(), actual.size());
    for (ProgramRecord actualProgram : actual) {
      Assert.assertTrue(expected.contains(actualProgram.getName()));
    }
  }

  protected void verifyProgramRecords(List<String> expected, Map<ProgramType, List<ProgramRecord>> map) {
    verifyProgramNames(expected, Lists.newArrayList(Iterables.concat(map.values())));
  }

  protected void assertFlowletInstances(ProgramClient programClient, Id.Flow.Flowlet flowlet, int numInstances)
    throws IOException, NotFoundException, UnauthenticatedException {

    int actualInstances;
    int numTries = 0;
    int maxTries = 5;
    do {
      actualInstances = programClient.getFlowletInstances(flowlet);
      numTries++;
    } while (actualInstances != numInstances && numTries <= maxTries);
    Assert.assertEquals(numInstances, actualInstances);
  }

  protected void assertProgramRunning(ProgramClient programClient, Id.Program program)
    throws IOException, ProgramNotFoundException, UnauthenticatedException, InterruptedException {

    assertProgramStatus(programClient, program, "RUNNING");
  }

  protected void assertProgramStopped(ProgramClient programClient, Id.Program program)
    throws IOException, ProgramNotFoundException, UnauthenticatedException, InterruptedException {

    assertProgramStatus(programClient, program, "STOPPED");
  }

  protected void assertProgramStatus(ProgramClient programClient, Id.Program program, String programStatus)
    throws IOException, ProgramNotFoundException, UnauthenticatedException, InterruptedException {

    try {
      programClient.waitForStatus(program, programStatus, 60, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      // NO-OP
    }

    Assert.assertEquals(programStatus, programClient.getStatus(program));
  }

  protected File createAppJarFile(Class<?> cls) throws IOException {
    return createArtifactJarFile(cls, new Manifest());
  }

  protected File createAppJarFile(Class<?> cls, String name, String version) throws IOException {
    return createArtifactJarFile(cls, name, version, new Manifest());
  }

  protected File createArtifactJarFile(Class<?> cls, Manifest manifest) throws IOException {
    return createArtifactJarFile(cls, cls.getSimpleName(),
                                 String.format("1.0.%d-SNAPSHOT", System.currentTimeMillis()), manifest);
  }

  protected File createArtifactJarFile(Class<?> cls, String name,
                                       String version, Manifest manifest) throws IOException {

    File tmpJarFolder = TMP_FOLDER.newFolder();
    LocationFactory locationFactory = new LocalLocationFactory(tmpJarFolder);
    Location deploymentJar = AppJarHelper.createDeploymentJar(locationFactory, cls, manifest);

    File appJarFile = new File(tmpJarFolder, String.format("%s-%s.jar", name, version));
    try {
      Files.createLink(appJarFile.toPath(), Paths.get(deploymentJar.toURI()));
    } catch (Exception e) {
      Files.copy(appJarFile.toPath(), Paths.get(deploymentJar.toURI()));
    }
    return appJarFile;
  }
}
