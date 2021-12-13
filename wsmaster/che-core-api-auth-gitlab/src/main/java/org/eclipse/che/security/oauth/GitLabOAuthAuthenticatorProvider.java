/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.security.oauth;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.security.oauth.shared.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides implementation of GitLab {@link OAuthAuthenticator} based on available configuration.
 *
 * @author Pavol Baran
 */
@Singleton
public class GitLabOAuthAuthenticatorProvider implements Provider<OAuthAuthenticator> {
  private static final Logger LOG = LoggerFactory.getLogger(GitLabOAuthAuthenticatorProvider.class);
  private final OAuthAuthenticator authenticator;

  public GitLabOAuthAuthenticatorProvider(
      @Nullable @Named("che.oauth.gitlab.clientid_filepath") String clientIdPath,
      @Nullable @Named("che.oauth.gitlab.clientsecret_filepath") String clientSecretPath,
      @Nullable @Named("che.integration.gitlab.oauth_endpoint") String gitlabEndpoint,
      @Nullable @Named("che.oauth.github.redirecturis") String[] redirectUris)
      throws IOException {
    authenticator =
        getOAuthAuthenticator(clientIdPath, clientSecretPath, gitlabEndpoint, redirectUris);
    LOG.debug("{} GitLab OAuth Authenticator is used.", authenticator);
  }

  @Override
  public OAuthAuthenticator get() {
    return authenticator;
  }

  private OAuthAuthenticator getOAuthAuthenticator(
      String clientIdPath, String clientSecretPath, String gitlabEndpoint, String[] redirectUris)
      throws IOException {
    if (!isNullOrEmpty(clientIdPath)
        && !isNullOrEmpty(clientSecretPath)
        && !isNullOrEmpty(gitlabEndpoint)
        && Objects.nonNull(redirectUris)
        && redirectUris.length != 0) {
      String clientId = Files.readString(Path.of(clientIdPath));
      String clientSecret = Files.readString(Path.of(clientSecretPath));
      if (!isNullOrEmpty(clientId) && !isNullOrEmpty(clientSecret)) {
        return new GitLabOAuthAuthenticator(clientId, clientSecret, gitlabEndpoint, redirectUris);
      }
    }
    return new NoopOAuthAuthenticator();
  }

  static class NoopOAuthAuthenticator extends OAuthAuthenticator {
    @Override
    public User getUser(OAuthToken accessToken) throws OAuthAuthenticationException {
      throw new OAuthAuthenticationException(
          "The fallback noop authenticator cannot be used for GitLab authentication. Make sure OAuth is properly configured.");
    }

    @Override
    public String getOAuthProvider() {
      return "Noop";
    }
  }
}
