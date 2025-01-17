/*
 * Copyright © 2015-2016 Cask Data, Inc.
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

package co.cask.cdap.gateway.handlers;

import co.cask.cdap.api.Transactional;
import co.cask.cdap.api.TxRunnable;
import co.cask.cdap.client.AuthorizationClient;
import co.cask.cdap.client.config.ClientConfig;
import co.cask.cdap.client.config.ConnectionConfig;
import co.cask.cdap.common.FeatureDisabledException;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.http.AuthenticationChannelHandler;
import co.cask.cdap.common.http.CommonNettyHttpServiceBuilder;
import co.cask.cdap.proto.id.EntityId;
import co.cask.cdap.proto.id.Ids;
import co.cask.cdap.proto.id.NamespaceId;
import co.cask.cdap.proto.security.Action;
import co.cask.cdap.proto.security.Principal;
import co.cask.cdap.proto.security.Privilege;
import co.cask.cdap.proto.security.Role;
import co.cask.cdap.security.authorization.AuthorizationContextFactory;
import co.cask.cdap.security.authorization.AuthorizerInstantiatorService;
import co.cask.cdap.security.authorization.DefaultAuthorizationContext;
import co.cask.cdap.security.authorization.NoOpAdmin;
import co.cask.cdap.security.authorization.NoOpDatasetContext;
import co.cask.cdap.security.spi.authorization.AuthorizationContext;
import co.cask.cdap.security.spi.authorization.Authorizer;
import co.cask.cdap.security.spi.authorization.RoleAlreadyExistsException;
import co.cask.cdap.security.spi.authorization.RoleNotFoundException;
import co.cask.cdap.security.spi.authorization.UnauthorizedException;
import co.cask.http.NettyHttpService;
import co.cask.tephra.TransactionFailureException;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Tests for {@link AuthorizationHandler}.
 */
public class AuthorizationHandlerTest {

  private static final String USERNAME_PROPERTY = "cdap.username";
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
  private final Principal admin = new Principal("admin", Principal.PrincipalType.USER);
  private final Properties properties = new Properties();

  private NettyHttpService service;
  private AuthorizationClient client;

  @Before
  public void setUp() throws Exception {
    CConfiguration conf = CConfiguration.create();
    conf.setBoolean(Constants.Security.Authorization.ENABLED, true);
    conf.setBoolean(Constants.Security.ENABLED, true);
    properties.setProperty("superusers", admin.getName());
    final InMemoryAuthorizer auth = new InMemoryAuthorizer();
    auth.initialize(factory.create(properties));
    service = new CommonNettyHttpServiceBuilder(conf)
      .addHttpHandlers(ImmutableList.of(new AuthorizationHandler(
        new AuthorizerInstantiatorService(conf, factory) {
          @Override
          public Authorizer get() {
            return auth;
          }
        }, conf)))
      .modifyChannelPipeline(new Function<ChannelPipeline, ChannelPipeline>() {
        @Override
        public ChannelPipeline apply(ChannelPipeline input) {
          input.addBefore("dispatcher", "usernamesetter", new TestUserNameSetter());
          input.addAfter("usernamesetter", "authenticator", new AuthenticationChannelHandler());
          return input;
        }
      })
      .build();
    service.startAndWait();

    client = new AuthorizationClient(
      ClientConfig.builder()
        .setConnectionConfig(
          ConnectionConfig.builder()
            .setHostname(service.getBindAddress().getHostName())
            .setPort(service.getBindAddress().getPort())
            .setSSLEnabled(false)
            .build())
        .build());
    System.setProperty(USERNAME_PROPERTY, admin.getName());
  }

  @After
  public void tearDown() {
    service.stopAndWait();
  }

  @Test
  public void testDisabled() throws Exception {
    CConfiguration conf = CConfiguration.create();
    conf.setBoolean(Constants.Security.Authorization.ENABLED, false);

    NettyHttpService service = new CommonNettyHttpServiceBuilder(conf)
      .addHttpHandlers(ImmutableList.of(new AuthorizationHandler(
        new AuthorizerInstantiatorService(conf, factory) {
          @Override
          public Authorizer get() {
            return new InMemoryAuthorizer();
          }
        }, conf)))
      .build();
    service.startAndWait();
    try {

      final AuthorizationClient client = new AuthorizationClient(
        ClientConfig.builder()
          .setConnectionConfig(
            ConnectionConfig.builder()
              .setHostname(service.getBindAddress().getHostName())
              .setPort(service.getBindAddress().getPort())
              .setSSLEnabled(false)
              .build())
          .build());

      final NamespaceId ns1 = Ids.namespace("ns1");
      final Role admins = new Role("admins");

      // Test that the right exception is thrown when any Authorization REST API is called with authorization disabled
      verifyFeatureDisabled(new DisabledFeatureCaller() {
        @Override
        public void call() throws Exception {
          client.grant(ns1, admin, ImmutableSet.of(Action.READ));
        }
      });

      verifyFeatureDisabled(new DisabledFeatureCaller() {
        @Override
        public void call() throws Exception {
          client.revoke(ns1, admin, ImmutableSet.of(Action.READ));
        }
      });

      verifyFeatureDisabled(new DisabledFeatureCaller() {
        @Override
        public void call() throws Exception {
          client.revoke(ns1);
        }
      });

      verifyFeatureDisabled(new DisabledFeatureCaller() {
        @Override
        public void call() throws Exception {
          client.listPrivileges(admin);
        }
      });

      verifyFeatureDisabled(new DisabledFeatureCaller() {
        @Override
        public void call() throws Exception {
          client.addRoleToPrincipal(admins, admin);
        }
      });

      verifyFeatureDisabled(new DisabledFeatureCaller() {
        @Override
        public void call() throws Exception {
          client.removeRoleFromPrincipal(admins, admin);
        }
      });

      verifyFeatureDisabled(new DisabledFeatureCaller() {
        @Override
        public void call() throws Exception {
          client.createRole(admins);
        }
      });

      verifyFeatureDisabled(new DisabledFeatureCaller() {
        @Override
        public void call() throws Exception {
          client.dropRole(admins);
        }
      });

      verifyFeatureDisabled(new DisabledFeatureCaller() {
        @Override
        public void call() throws Exception {
          client.listAllRoles();
        }
      });
    } finally {
      service.stopAndWait();
    }
  }

  @Test
  public void testRevokeEntityUserActions() throws Exception {
    NamespaceId ns1 = Ids.namespace("ns1");

    // grant() and revoke(EntityId, String, Set<Action>)
    verifyAuthFailure(ns1, admin, Action.READ);

    client.grant(ns1, admin, ImmutableSet.of(Action.READ));
    verifyAuthSuccess(ns1, admin, Action.READ);

    client.revoke(ns1, admin, ImmutableSet.of(Action.READ));
    verifyAuthFailure(ns1, admin, Action.READ);
  }

  @Test
  public void testRevokeEntityUser() throws Exception {
    NamespaceId ns1 = Ids.namespace("ns1");
    Principal adminGroup = new Principal("admin", Principal.PrincipalType.GROUP);
    Principal bob = new Principal("bob", Principal.PrincipalType.USER);

    // grant() and revoke(EntityId, String)
    client.grant(ns1, adminGroup, ImmutableSet.of(Action.READ));
    client.grant(ns1, bob, ImmutableSet.of(Action.READ));
    verifyAuthSuccess(ns1, adminGroup, Action.READ);
    verifyAuthSuccess(ns1, bob, Action.READ);

    client.revoke(ns1, adminGroup, EnumSet.allOf(Action.class));
    verifyAuthFailure(ns1, adminGroup, Action.READ);
    verifyAuthSuccess(ns1, bob, Action.READ);
  }

  @Test
  public void testRevokeEntity() throws Exception {
    NamespaceId ns1 = Ids.namespace("ns1");
    NamespaceId ns2 = Ids.namespace("ns2");
    Principal adminGroup = new Principal("admin", Principal.PrincipalType.GROUP);
    Principal bob = new Principal("bob", Principal.PrincipalType.USER);

    // grant() and revoke(EntityId)
    client.grant(ns1, adminGroup, ImmutableSet.of(Action.READ));
    client.grant(ns1, bob, ImmutableSet.of(Action.READ));
    client.grant(ns2, adminGroup, ImmutableSet.of(Action.READ));
    verifyAuthSuccess(ns1, adminGroup, Action.READ);
    verifyAuthSuccess(ns1, bob, Action.READ);
    verifyAuthSuccess(ns2, adminGroup, Action.READ);

    client.revoke(ns1);
    verifyAuthFailure(ns1, adminGroup, Action.READ);
    verifyAuthFailure(ns1, bob, Action.READ);
    verifyAuthSuccess(ns2, adminGroup, Action.READ);
  }

  @Test
  public void testRBAC() throws Exception {
    Role admins = new Role("admins");
    Role engineers = new Role("engineers");
    // create a role
    client.createRole(admins);
    // add another role
    client.createRole(engineers);

    // listing role should show the added role
    Set<Role> roles = client.listAllRoles();
    Assert.assertEquals(Sets.newHashSet(admins, engineers), roles);

    // creating a role which already exists should throw an exception
    try {
      client.createRole(admins);
      Assert.fail(String.format("Created a role %s which already exists. Should have failed.", admins.getName()));
    } catch (RoleAlreadyExistsException expected) {
      // expected
    }

    // drop an existing role
    client.dropRole(admins);

    // the list should not have the dropped role
    roles = client.listAllRoles();
    Assert.assertEquals(Sets.newHashSet(engineers), roles);

    // dropping a non-existing role should throw exception
    try {
      client.dropRole(admins);
      Assert.fail(String.format("Dropped a role %s which does not exists. Should have failed.", admins.getName()));
    } catch (RoleNotFoundException expected) {
      // expected
    }

    // add an user to an existing role
    Principal spiderman = new Principal("spiderman", Principal.PrincipalType.USER);
    client.addRoleToPrincipal(engineers, spiderman);

    // add an user to an non-existing role should throw an exception
    try {
      client.addRoleToPrincipal(admins, spiderman);
      Assert.fail(String.format("Added role %s to principal %s. Should have failed.", admins, spiderman));
    } catch (RoleNotFoundException expected) {
      // expected
    }

    // check listing roles for spiderman have engineers role
    Assert.assertEquals(Sets.newHashSet(engineers), client.listRoles(spiderman));

    // authorization checks with roles
    NamespaceId ns1 = Ids.namespace("ns1");

    // check that spiderman who has engineers roles cannot read from ns1
    verifyAuthFailure(ns1, spiderman, Action.READ);

    // give a permission to engineers role
    client.grant(ns1, engineers, ImmutableSet.of(Action.READ));

    // check that a spiderman who has engineers role has access
    verifyAuthSuccess(ns1, spiderman, Action.READ);

    // list privileges for spiderman should have read action on ns1
    Assert.assertEquals(Sets.newHashSet(new Privilege(ns1, Action.READ)), client.listPrivileges(spiderman));

    // revoke action from the role
    client.revoke(ns1, engineers, ImmutableSet.of(Action.READ));

    // now the privileges for spiderman should be empty
    Assert.assertEquals(new HashSet<>(), client.listPrivileges(spiderman));

    // check that the user of this role is not authorized to do the revoked operation
    verifyAuthFailure(ns1, spiderman, Action.READ);

    // remove an user from a existing role
    client.removeRoleFromPrincipal(engineers, spiderman);

    // check listing roles for spiderman should be empty
    Assert.assertEquals(new HashSet<>(), client.listRoles(spiderman));

    // remove an user from a non-existing role should throw exception
    try {
      client.removeRoleFromPrincipal(admins, spiderman);
      Assert.fail(String.format("Removed non-existing role %s from principal %s. Should have failed.", admins,
                                spiderman));
    } catch (RoleNotFoundException expected) {
      // expected
    }
  }

  @Test
  public void testAuthorizationForPrivileges() throws Exception {
    NamespaceId ns = Ids.namespace("ns");
    Principal bob = new Principal("bob", Principal.PrincipalType.USER);
    Principal alice = new Principal("alice", Principal.PrincipalType.USER);
    // olduser has been set as admin in the beginning of this test. admin has been configured as a superuser.
    String oldUser = getCurrentUser();
    setCurrentUser(alice.getName());
    try {
      try {
        client.grant(ns, bob, ImmutableSet.of(Action.ALL));
        Assert.fail(String.format("alice should not be able to grant privileges to bob on namespace %s because she " +
                                    "does not have admin privileges on the namespace.", ns));
      } catch (UnauthorizedException expected) {
        // expected
      }
      setCurrentUser(oldUser);
      // admin should be able to grant since he is a super user
      client.grant(ns, alice, ImmutableSet.of(Action.ADMIN));
      // now alice should be able to grant privileges on ns since she has ADMIN privileges
      setCurrentUser(alice.getName());
      client.grant(ns, bob, ImmutableSet.of(Action.ALL));
      // revoke alice's permissions as admin
      setCurrentUser(oldUser);
      client.revoke(ns);
      // revoking bob's privileges as alice should fail
      setCurrentUser(alice.getName());
      try {
        client.revoke(ns, bob, ImmutableSet.of(Action.ALL));
        Assert.fail(String.format("alice should not be able to revoke bob's privileges on namespace %s because she " +
                                    "does not have admin privileges on the namespace.", ns));
      } catch (UnauthorizedException expected) {
        // expected
      }
      // grant alice privileges as admin again
      setCurrentUser(oldUser);
      client.grant(ns, alice, ImmutableSet.of(Action.ALL));
      // Now alice should be able to revoke bob's privileges
      setCurrentUser(alice.getName());
      client.revoke(ns, bob, ImmutableSet.of(Action.ALL));
    } finally {
      setCurrentUser(oldUser);
    }
  }

  /**
   * Interface to centralize testing the exception thrown when Authorization is disabled.
   */
  private interface DisabledFeatureCaller {
    void call() throws Exception;
  }

  /**
   * Calls a {@link DisabledFeatureCaller} and verifies that the right exception was thrown.
   *
   * @param caller the {@link DisabledFeatureCaller} that wraps the operation to test
   */
  private void verifyFeatureDisabled(DisabledFeatureCaller caller) throws Exception {
    try {
      caller.call();
    } catch (FeatureDisabledException expected) {
      Assert.assertEquals("Authorization", expected.getFeature());
      Assert.assertEquals("cdap-site.xml", expected.getConfigFile());
      Assert.assertEquals(Constants.Security.Authorization.ENABLED, expected.getEnableConfigKey());
      Assert.assertEquals("true", expected.getEnableConfigValue());
    }
  }

  private void verifyAuthSuccess(EntityId entity, Principal principal, Action action) throws Exception {
    Set<Privilege> privileges = client.listPrivileges(principal);
    Privilege privilegeToCheck = new Privilege(entity, action);
    Assert.assertTrue(
      String.format(
        "Expected principal %s to have the privilege %s, but found that it did not.", principal, privilegeToCheck
      ),
      privileges.contains(privilegeToCheck)
    );
  }

  private void verifyAuthFailure(EntityId entity, Principal principal, Action action) throws Exception {
    Set<Privilege> privileges = client.listPrivileges(principal);
    Privilege privilegeToCheck = new Privilege(entity, action);
    Assert.assertFalse(
      String.format(
        "Expected principal %s to not have the privilege %s, but found that it did.", principal, privilegeToCheck
      ),
      privileges.contains(privilegeToCheck)
    );
  }

  private static void setCurrentUser(String username) {
    System.setProperty(USERNAME_PROPERTY, username);
  }

  private static String getCurrentUser() {
    return System.getProperty(USERNAME_PROPERTY);
  }

  /**
   * Test {@link SimpleChannelHandler} to set the username as an HTTP Header
   * {@link co.cask.cdap.common.conf.Constants.Security.Headers#USER_ID}. In production, this is done in the router by
   * SecurityAuthenticationHandler
   */
  private static final class TestUserNameSetter extends SimpleChannelHandler {

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      Object msg = e.getMessage();
      if (!(msg instanceof HttpRequest)) {
        return;
      }
      HttpRequest request = (HttpRequest) msg;
      request.setHeader(Constants.Security.Headers.USER_ID, System.getProperty(USERNAME_PROPERTY));
      super.messageReceived(ctx, e);
    }
  }
}
