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

package co.cask.cdap.client;

import co.cask.cdap.api.annotation.Beta;
import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.client.config.ClientConfig;
import co.cask.cdap.client.util.RESTClient;
import co.cask.cdap.common.FeatureDisabledException;
import co.cask.cdap.common.UnauthenticatedException;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.proto.codec.EntityIdTypeAdapter;
import co.cask.cdap.proto.id.EntityId;
import co.cask.cdap.proto.security.Action;
import co.cask.cdap.proto.security.GrantRequest;
import co.cask.cdap.proto.security.Principal;
import co.cask.cdap.proto.security.Privilege;
import co.cask.cdap.proto.security.RevokeRequest;
import co.cask.cdap.proto.security.Role;
import co.cask.cdap.security.spi.authorization.AbstractAuthorizer;
import co.cask.cdap.security.spi.authorization.RoleAlreadyExistsException;
import co.cask.cdap.security.spi.authorization.RoleNotFoundException;
import co.cask.cdap.security.spi.authorization.UnauthorizedException;
import co.cask.common.http.HttpRequest;
import co.cask.common.http.HttpResponse;
import co.cask.common.http.ObjectResponse;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Provides ways to interact with the CDAP authorization system.
 */
@Beta
public class AuthorizationClient extends AbstractAuthorizer {

  private static final Gson GSON = new GsonBuilder()
    .registerTypeAdapter(EntityId.class, new EntityIdTypeAdapter())
    .create();
  public static final String AUTHORIZATION_BASE = "security/authorization/";
  private static final TypeToken<Set<Privilege>> TYPE_OF_PRIVILEGE_SET = new TypeToken<Set<Privilege>>() { };
  private static final TypeToken<Set<Role>> TYPE_OF_ROLE_SET = new TypeToken<Set<Role>>() { };
  private final RESTClient restClient;
  private final ClientConfig config;

  @Inject
  public AuthorizationClient(ClientConfig config, RESTClient restClient) {
    this.config = config;
    this.restClient = restClient;
  }

  public AuthorizationClient(ClientConfig config) {
    this(config, new RESTClient(config));
  }

  @Override
  public void enforce(EntityId entity, Principal principal, Action action) throws Exception {
    throw new UnsupportedOperationException("Enforcement is not supported via Java Client. Please instead use the " +
                                              "listPrivileges method to view the privileges for a principal.");
  }

  @Override
  public void grant(EntityId entity, Principal principal, Set<Action> actions) throws IOException,
    UnauthenticatedException, FeatureDisabledException, UnauthorizedException {

    GrantRequest grantRequest = new GrantRequest(entity, principal, actions);

    URL url = config.resolveURLV3(AUTHORIZATION_BASE + "/privileges/grant");
    HttpRequest request = HttpRequest.post(url).withBody(GSON.toJson(grantRequest)).build();
    executeRequest(request, HttpURLConnection.HTTP_FORBIDDEN, HttpURLConnection.HTTP_NOT_IMPLEMENTED);
  }

  @Override
  public void revoke(EntityId entity) throws IOException, UnauthenticatedException, FeatureDisabledException,
    UnauthorizedException {
    revoke(entity, null, null);
  }

  @Override
  public void revoke(EntityId entity, @Nullable Principal principal, @Nullable Set<Action> actions) throws IOException,
    UnauthenticatedException, FeatureDisabledException, UnauthorizedException {
    revoke(new RevokeRequest(entity, principal, actions));
  }

  @Override
  public Set<Privilege> listPrivileges(Principal principal) throws IOException, FeatureDisabledException,
    UnauthenticatedException, UnauthorizedException {
    URL url = config.resolveURLV3(String.format(AUTHORIZATION_BASE + "%s/%s/privileges", principal.getType(),
                                                principal.getName()));
    HttpRequest request = HttpRequest.get(url).build();
    HttpResponse response = executeRequest(request, HttpURLConnection.HTTP_FORBIDDEN,
                                           HttpURLConnection.HTTP_NOT_IMPLEMENTED);
    if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
      return ObjectResponse.fromJsonBody(response, TYPE_OF_PRIVILEGE_SET, GSON).getResponseObject();
    }
    throw new IOException(String.format("Cannot list privileges. Reason: %s", response.getResponseBodyAsString()));
  }

  @Override
  public void createRole(Role role) throws IOException, FeatureDisabledException, UnauthenticatedException,
    UnauthorizedException, RoleAlreadyExistsException {
    URL url = config.resolveURLV3(String.format(AUTHORIZATION_BASE + "roles/%s", role.getName()));
    HttpRequest request = HttpRequest.put(url).build();
    HttpResponse httpResponse = executeRequest(request, HttpURLConnection.HTTP_FORBIDDEN,
                                               HttpURLConnection.HTTP_NOT_IMPLEMENTED, HttpURLConnection.HTTP_CONFLICT);
    if (httpResponse.getResponseCode() == HttpURLConnection.HTTP_CONFLICT) {
      throw new RoleAlreadyExistsException(role);
    }
  }

  @Override
  public void dropRole(Role role) throws IOException, FeatureDisabledException, UnauthenticatedException,
    UnauthorizedException, RoleNotFoundException {
    URL url = config.resolveURLV3(String.format(AUTHORIZATION_BASE + "roles/%s", role.getName()));
    HttpRequest request = HttpRequest.delete(url).build();
    executeRoleRequest(role, request);
  }

  @Override
  public Set<Role> listAllRoles() throws FeatureDisabledException, UnauthenticatedException, UnauthorizedException,
    IOException {
    return listRolesHelper(null);
  }

  @Override
  public Set<Role> listRoles(Principal principal) throws FeatureDisabledException, UnauthenticatedException,
    UnauthorizedException,
    IOException {
    return listRolesHelper(principal);
  }

  @Override
  public void addRoleToPrincipal(Role role, Principal principal) throws IOException, FeatureDisabledException,
    UnauthenticatedException, UnauthorizedException, RoleNotFoundException {
    URL url = config.resolveURLV3(String.format(AUTHORIZATION_BASE + "%s/%s/roles/%s", principal.getType(),
                                                principal.getName(), role.getName()));
    HttpRequest request = HttpRequest.put(url).build();
    executeRoleRequest(role, request);
  }

  @Override
  public void removeRoleFromPrincipal(Role role, Principal principal) throws IOException, FeatureDisabledException,
    UnauthenticatedException, UnauthorizedException, RoleNotFoundException {
    URL url = config.resolveURLV3(String.format(AUTHORIZATION_BASE + "%s/%s/roles/%s", principal.getType(),
                                                principal.getName(), role.getName()));
    HttpRequest request = HttpRequest.delete(url).build();
    executeRoleRequest(role, request);
  }

  private void revoke(RevokeRequest revokeRequest)
    throws IOException, UnauthenticatedException, FeatureDisabledException, UnauthorizedException {
    URL url = config.resolveURLV3(AUTHORIZATION_BASE + "/privileges/revoke");
    HttpRequest request = HttpRequest.post(url).withBody(GSON.toJson(revokeRequest)).build();
    executeRequest(request, HttpURLConnection.HTTP_FORBIDDEN, HttpURLConnection.HTTP_NOT_IMPLEMENTED);
  }

  private Set<Role> listRolesHelper(@Nullable Principal principal) throws IOException, FeatureDisabledException,
    UnauthenticatedException, UnauthorizedException {
    URL url = principal == null ? config.resolveURLV3(AUTHORIZATION_BASE + "roles") :
      config.resolveURLV3(String.format(AUTHORIZATION_BASE + "%s/%s/roles", principal.getType(), principal.getName()));
    HttpRequest request = HttpRequest.get(url).build();
    HttpResponse response = executeRequest(request, HttpURLConnection.HTTP_FORBIDDEN,
                                           HttpURLConnection.HTTP_NOT_IMPLEMENTED);

    if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
      return ObjectResponse.fromJsonBody(response, TYPE_OF_ROLE_SET).getResponseObject();
    }
    throw new IOException(String.format("Cannot list roles. Reason: %s", response.getResponseBodyAsString()));
  }

  private void executeRoleRequest(Role role, HttpRequest request) throws IOException, UnauthenticatedException,
    FeatureDisabledException, UnauthorizedException, RoleNotFoundException {
    HttpResponse httpResponse = executeRequest(request, HttpURLConnection.HTTP_FORBIDDEN,
                                               HttpURLConnection.HTTP_NOT_IMPLEMENTED,
                                               HttpURLConnection.HTTP_NOT_FOUND);
    if (httpResponse.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new RoleNotFoundException(role);
    }
  }

  private HttpResponse executeRequest(HttpRequest request, int... allowedErrorCodes)
    throws IOException, UnauthenticatedException, FeatureDisabledException, UnauthorizedException {
    HttpResponse response = restClient.execute(request, config.getAccessToken(), allowedErrorCodes);
    if (HttpURLConnection.HTTP_FORBIDDEN == response.getResponseCode()) {
      // TODO:(CDAP-5302) Include the logged in username here
      InputSupplier<? extends InputStream> requestBody = request.getBody();
      String msg = requestBody == null ?
        String.format("Unauthorized to perform %s %s", request.getMethod(), request.getURL()) :
        String.format("Unauthorized to perform %s %s with body %s",
                      request.getMethod(), request.getURL(), Bytes.toString(ByteStreams.toByteArray(requestBody)));
      throw new UnauthorizedException(msg);
    }
    if (HttpURLConnection.HTTP_NOT_IMPLEMENTED == response.getResponseCode()) {
      throw new FeatureDisabledException("Authorization", "cdap-site.xml", Constants.Security.Authorization.ENABLED,
                                         "true");
    }
    return response;
  }
}
