/*
 * Sonar Crowd Plugin
 * Copyright (C) 2009 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.crowd;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.security.Authenticator;
import org.sonar.api.security.LoginPasswordAuthenticator;

import com.atlassian.crowd.integration.rest.service.RestCrowdClient;
import com.atlassian.crowd.model.authentication.CookieConfiguration;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.user.User;

/**
 * @author Evgeny Mandrikov
 */
public final class CrowdAuthenticator extends Authenticator implements LoginPasswordAuthenticator {
    /**
     * Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(CrowdAuthenticator.class);

    final RestCrowdClient restCrowdClient;

    public CrowdAuthenticator(RestCrowdClient restCrowdClient) {
        super();
        this.restCrowdClient = restCrowdClient;
    }

    public boolean authenticate(String username, String password) {
        try {
            return restCrowdClient.authenticateUser(username, password) != null;
        } catch (Exception e) {
            CrowdHelper.wrapException(e);
        }
        return false;
    }

    @Deprecated
    public void init() {

    }

    @Override
    public boolean doAuthenticate(Context context) {
        String login = context.getUsername();
        String password = context.getPassword();
        try {
            final HttpServletRequest request = context.getRequest();
            if (password == null) {
                final HttpSession session = request.getSession(true);
                // String ssoLogin = request.getParameter("ssoLogin");
                Long ssoLoginTs = (Long) session.getAttribute("ssoLoginTs");
                if (ssoLoginTs != null && (System.currentTimeMillis() - ssoLoginTs < 60000)) {
                    // session.removeAttribute("ssoLogin");
                    final String ssoToken = CrowdHelper.getCrowdCookie(request, restCrowdClient);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("login username:{} ssoToken:{}", login, ssoToken);
                    }
                    if (ssoToken == null) {
                        return false;
                    }

                    session.removeAttribute("ssoLoginTs");
                    final User u1 = restCrowdClient.findUserFromSSOToken(ssoToken);
                    if (u1 != null) {
                        final UserAuthenticationContext authenticationContext = CrowdHelper
                                .createCrowdUserAuthContext(u1.getName(), "dummy");
                        final String ret = restCrowdClient
                                .authenticateSSOUserWithoutValidatingPassword(authenticationContext);
                        LOG.info("sso ret:" + ret);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            UserAuthenticationContext authenticationContext = CrowdHelper.createCrowdUserAuthContext(login,
                    password);
            String ret = restCrowdClient.authenticateSSOUser(authenticationContext);
            if (ret != null) {
                final HttpServletResponse httpServletResponse = CrowdLoginFilter.httpServletResponseThreadLocal
                        .get();
                if (httpServletResponse != null) {
                    final CookieConfiguration cookieConfiguration = restCrowdClient
                            .getCookieConfiguration();
                    final Cookie cookie = new Cookie(cookieConfiguration.getName(), ret);
                    final String domain = cookieConfiguration.getDomain();
                    if (domain != null && domain.length() > 0) {
                        cookie.setDomain(domain);
                    }
                    cookie.setPath("/");
                    httpServletResponse.addCookie(cookie);
                }
                return true;
            }
            // return restCrowdClient.authenticateUser(login, password) != null;
        } catch (Exception e) {
            CrowdHelper.wrapException(e);
        }
        return false;
    }

}
