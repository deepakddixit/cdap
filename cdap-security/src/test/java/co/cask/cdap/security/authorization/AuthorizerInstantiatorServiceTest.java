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

package co.cask.cdap.security.authorization;

import co.cask.cdap.api.Transactional;
import co.cask.cdap.api.TxRunnable;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.twill.LocalLocationFactory;
import co.cask.cdap.internal.test.AppJarHelper;
import co.cask.cdap.proto.id.EntityId;
import co.cask.cdap.proto.security.Action;
import co.cask.cdap.proto.security.Principal;
import co.cask.cdap.proto.security.Privilege;
import co.cask.cdap.proto.security.Role;
import co.cask.cdap.security.spi.authorization.AbstractAuthorizer;
import co.cask.cdap.security.spi.authorization.AuthorizationContext;
import co.cask.cdap.security.spi.authorization.Authorizer;
import co.cask.cdap.security.spi.authorization.NoOpAuthorizer;
import co.cask.cdap.security.spi.authorization.RoleAlreadyExistsException;
import co.cask.cdap.security.spi.authorization.RoleNotFoundException;
import co.cask.cdap.security.spi.authorization.UnauthorizedException;
import co.cask.tephra.TransactionFailureException;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.gson.Gson;
import org.apache.twill.filesystem.Location;
import org.apache.twill.filesystem.LocationFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import javax.annotation.Nullable;

/**
 * Tests for {@link AuthorizerInstantiatorService}.
 */
public class AuthorizerInstantiatorServiceTest {

  @ClassRule
  public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();
  private static final CConfiguration cConf = CConfiguration.create();
  private static final AuthorizationContextFactory factory = new AuthorizationContextFactory() {
    @Override
    public AuthorizationContext create(Properties extensionProperties) {
      Transactional txnl = new Transactional() {
        @Override
        public void execute(TxRunnable runnable) throws TransactionFailureException {
          //no-op
        }
      };
      return new DefaultAuthorizationContext(extensionProperties, new NoOpDatasetContext(), new NoOpAdmin(), txnl);
    }
  };
  private static LocationFactory locationFactory;

  @BeforeClass
  public static void setup() throws IOException {
    cConf.set(Constants.CFG_LOCAL_DATA_DIR, TEMPORARY_FOLDER.newFolder().getAbsolutePath());
    cConf.setBoolean(Constants.Security.Authorization.ENABLED, true);
    locationFactory = new LocalLocationFactory(TEMPORARY_FOLDER.newFolder());
  }

  @Test
  public void testAuthorizationDisabled() throws IOException {
    CConfiguration cConf = CConfiguration.create();
    cConf.set(Constants.CFG_LOCAL_DATA_DIR, TEMPORARY_FOLDER.newFolder().getAbsolutePath());
    AuthorizerInstantiatorService instantiator = new AuthorizerInstantiatorService(cConf, factory);
    try {
      instantiator.startAndWait();
      Authorizer authorizer = instantiator.get();
      Assert.assertTrue("When authorization is disabled, a NoOpAuthorizer must be returned, but got " +
                          authorizer.getClass().getName(), authorizer instanceof NoOpAuthorizer);
    } finally {
      instantiator.stopAndWait();
    }
  }

  @Test(expected = InvalidAuthorizerException.class)
  public void testNonExistingAuthorizerJarPath() throws Throwable {
    cConf.set(Constants.Security.Authorization.EXTENSION_JAR_PATH, "/path/to/external-test-authorizer.jar");
    AuthorizerInstantiatorService instantiator = new AuthorizerInstantiatorService(cConf, factory);
    try {
      instantiator.startAndWait();
    } catch (UncheckedExecutionException e) {
      throw Throwables.getRootCause(e);
    }
    Assert.assertFalse("Authorizer Instantiator should not have started because extension jar does not exist",
                       instantiator.isRunning());
  }

  @Test(expected = InvalidAuthorizerException.class)
  public void testAuthorizerJarPathIsDirectory() throws Throwable {
    cConf.set(Constants.Security.Authorization.EXTENSION_JAR_PATH, TEMPORARY_FOLDER.newFolder().getPath());
    AuthorizerInstantiatorService instantiator = new AuthorizerInstantiatorService(cConf, factory);
    try {
      instantiator.startAndWait();
    } catch (UncheckedExecutionException e) {
      throw Throwables.getRootCause(e);
    }
    Assert.assertFalse("Authorizer Instantiator should not have started because extension jar is a directory",
                       instantiator.isRunning());
  }

  @Test(expected = InvalidAuthorizerException.class)
  public void testAuthorizerJarPathIsNotJar() throws Throwable {
    cConf.set(Constants.Security.Authorization.EXTENSION_JAR_PATH, TEMPORARY_FOLDER.newFile("abc.txt").getPath());
    AuthorizerInstantiatorService instantiator = new AuthorizerInstantiatorService(cConf, factory);
    try {
      instantiator.startAndWait();
    } catch (UncheckedExecutionException e) {
      throw Throwables.getRootCause(e);
    }
    Assert.assertFalse("Authorizer Instantiator should not have started because extension jar is not a jar file",
                       instantiator.isRunning());
  }

  @Test(expected = InvalidAuthorizerException.class)
  public void testMissingManifest() throws Throwable {
    Location externalAuthJar = createInvalidExternalAuthJar(null);
    cConf.set(Constants.Security.Authorization.EXTENSION_JAR_PATH, externalAuthJar.toString());
    AuthorizerInstantiatorService instantiator = new AuthorizerInstantiatorService(cConf, factory);
    try {
      instantiator.startAndWait();
    } catch (UncheckedExecutionException e) {
      throw Throwables.getRootCause(e);
    }
    Assert.assertFalse("Authorizer Instantiator should not have started because extension jar does not have a manifest",
                       instantiator.isRunning());
  }

  @Test(expected = InvalidAuthorizerException.class)
  public void testMissingAuthorizerClassName() throws Throwable {
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    Location externalAuthJar = createInvalidExternalAuthJar(manifest);
    cConf.set(Constants.Security.Authorization.EXTENSION_JAR_PATH, externalAuthJar.toString());
    AuthorizerInstantiatorService instantiator = new AuthorizerInstantiatorService(cConf, factory);
    try {
      instantiator.startAndWait();
    } catch (UncheckedExecutionException e) {
      throw Throwables.getRootCause(e);
    }
    Assert.assertFalse("Authorizer Instantiator should not have started because extension jar's manifest does not " +
                         "define Authorizer class.", instantiator.isRunning());
  }

  @Test(expected = InvalidAuthorizerException.class)
  public void testDoesNotImplementAuthorizer() throws Throwable {
    Manifest manifest = new Manifest();
    Attributes mainAttributes = manifest.getMainAttributes();
    mainAttributes.put(Attributes.Name.MAIN_CLASS, DoesNotImplementAuthorizer.class.getName());
    Location externalAuthJar = AppJarHelper.createDeploymentJar(locationFactory, DoesNotImplementAuthorizer.class,
                                                                manifest);
    cConf.set(Constants.Security.Authorization.EXTENSION_JAR_PATH, externalAuthJar.toString());
    AuthorizerInstantiatorService instantiator = new AuthorizerInstantiatorService(cConf, factory);
    try {
      instantiator.startAndWait();
    } catch (UncheckedExecutionException e) {
      throw Throwables.getRootCause(e);
    }
    Assert.assertFalse("Authorizer Instantiator should not have started because the Authorizer class defined in " +
                         "the extension jar's manifest does not implement " + Authorizer.class.getName(),
                       instantiator.isRunning());
  }

  @Test(expected = InvalidAuthorizerException.class)
  public void testInitializationThrowsException() throws Throwable {
    Manifest manifest = new Manifest();
    Attributes mainAttributes = manifest.getMainAttributes();
    mainAttributes.put(Attributes.Name.MAIN_CLASS, ExceptionInInitialize.class.getName());
    Location externalAuthJar = AppJarHelper.createDeploymentJar(locationFactory, ExceptionInInitialize.class,
                                                                manifest);
    cConf.set(Constants.Security.Authorization.EXTENSION_JAR_PATH, externalAuthJar.toString());
    AuthorizerInstantiatorService instantiator = new AuthorizerInstantiatorService(cConf, factory);
    try {
      instantiator.startAndWait();
    } catch (UncheckedExecutionException e) {
      throw e.getCause();
    }
    Assert.assertFalse("Authorizer Instantiator should not have started because the Authorizer class defined in " +
                         "the extension jar's manifest does not implement " + Authorizer.class.getName(),
                       instantiator.isRunning());
  }

  @Test
  public void testAuthorizerExtension() throws IOException, ClassNotFoundException {
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, ValidExternalAuthorizer.class.getName());
    Location externalAuthJar = AppJarHelper.createDeploymentJar(locationFactory, ValidExternalAuthorizer.class,
                                                                manifest);
    CConfiguration cConfCopy = CConfiguration.copy(cConf);
    cConfCopy.set(Constants.Security.Authorization.EXTENSION_JAR_PATH, externalAuthJar.toString());
    cConfCopy.set(Constants.Security.Authorization.EXTENSION_CONFIG_PREFIX + "config.path",
                  "/path/config.ini");
    cConfCopy.set(Constants.Security.Authorization.EXTENSION_CONFIG_PREFIX + "service.address",
                  "http://foo.bar.co:5555");
    cConfCopy.set("foo." + Constants.Security.Authorization.EXTENSION_CONFIG_PREFIX + "dont.include",
                  "not.prefix.should.not.be.included");
    AuthorizerInstantiatorService instantiator = new AuthorizerInstantiatorService(cConfCopy, factory);
    try {
      instantiator.startAndWait();
      Assert.assertTrue(instantiator.isRunning());
      // should be able to load the ExternalAuthorizer class via the AuthorizerInstantiatorService
      Authorizer externalAuthorizer1 = instantiator.get();
      Assert.assertNotNull(externalAuthorizer1);
      Authorizer externalAuthorizer2 = instantiator.get();
      Assert.assertNotNull(externalAuthorizer2);
      // verify that get returns the same  instance each time it is called.
      Assert.assertEquals(externalAuthorizer1, externalAuthorizer2);

      ClassLoader authorizerClassLoader = externalAuthorizer1.getClass().getClassLoader();
      ClassLoader parent = authorizerClassLoader.getParent();
      // should be able to load the Authorizer interface via the parent
      parent.loadClass(Authorizer.class.getName());
      // should not be able to load the ExternalAuthorizer class via the parent class loader
      try {
        parent.loadClass(ValidExternalAuthorizer.class.getName());
        Assert.fail("Should not be able to load external authorizer classes via the parent classloader of the " +
                      "Authorizer class loader.");
      } catch (ClassNotFoundException expected) {
        // expected
      }
      // should be able to load the ExternalAuthorizer class via the AuthorizerClassLoader
      authorizerClassLoader.loadClass(ValidExternalAuthorizer.class.getName());

      // have to do this because the external authorizer instance is created in a new classloader, so casting will
      // not work.
      Gson gson = new Gson();
      ValidExternalAuthorizer validAuthorizer = gson.fromJson(gson.toJson(externalAuthorizer1),
                                                              ValidExternalAuthorizer.class);
      Properties expectedProps = new Properties();
      expectedProps.put("config.path", "/path/config.ini");
      expectedProps.put("service.address", "http://foo.bar.co:5555");
      Properties actualProps = validAuthorizer.getProperties();
      Assert.assertEquals(expectedProps, actualProps);
    } finally {
      instantiator.stopAndWait();
    }
  }

  private Location createInvalidExternalAuthJar(@Nullable Manifest manifest) throws IOException {
    String jarName = "external-authorizer";
    Location externalAuthJar = locationFactory.create(jarName).getTempFile(".jar");
    try (
      OutputStream out = externalAuthJar.getOutputStream();
      JarOutputStream jarOutput = manifest == null ? new JarOutputStream(out) : new JarOutputStream(out, manifest)
    ) {
      JarEntry entry = new JarEntry("dummy.class");
      jarOutput.putNextEntry(entry);
      jarOutput.closeEntry();
    }
    return externalAuthJar;
  }

  public static class NoOpAbstractAuthorizer extends AbstractAuthorizer {

    @Override
    public void grant(EntityId entity, Principal principal, Set<Action> actions) {
      // no-op
    }

    @Override
    public void revoke(EntityId entity, Principal principal, Set<Action> actions) {
      // no-op
    }

    @Override
    public void revoke(EntityId entity) {
      // no-op
    }

    @Override
    public Set<Privilege> listPrivileges(Principal principal) {
      return ImmutableSet.of();
    }

    @Override
    public void createRole(Role role) throws RoleAlreadyExistsException {
      // no-op
    }

    @Override
    public void dropRole(Role role) throws RoleNotFoundException {
      // no-op
    }

    @Override
    public void addRoleToPrincipal(Role role, Principal principal) {
      // no-op
    }

    @Override
    public void removeRoleFromPrincipal(Role role, Principal principal) {
      // no-op
    }

    @Override
    public Set<Role> listRoles(Principal principal) {
      return ImmutableSet.of();
    }

    @Override
    public Set<Role> listAllRoles() {
      return ImmutableSet.of();
    }

    @Override
    public void enforce(EntityId entity, Principal principal, Action action) throws UnauthorizedException {
      // no-op
    }
  }

  public static final class ExceptionInInitialize extends NoOpAbstractAuthorizer {
    @Override
    public void initialize(AuthorizationContext context) throws Exception {
      throw new IllegalStateException("Testing exception during initialize");
    }
  }

  public static final class ValidExternalAuthorizer extends NoOpAbstractAuthorizer {
    private Properties properties;
    @Override
    public void initialize(AuthorizationContext context) throws Exception {
      this.properties = context.getExtensionProperties();
    }

    public Properties getProperties() {
      return properties;
    }
  }

  private static final class DoesNotImplementAuthorizer {
  }
}
