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

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.security.UserDetails;
import org.sonar.api.utils.SonarException;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.integration.rest.service.RestCrowdClient;
import com.atlassian.crowd.model.authentication.CookieConfiguration;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.authentication.ValidationFactor;
import com.atlassian.crowd.model.user.User;

/**
 * 
 * 
 * @version $Id$
 * @author qxo
 * @since 2013-12-7
 */
public final class CrowdHelper {
    public static final Logger LOG = LoggerFactory.getLogger("org.sonar.plugins.crowd");

    /**
     * Hide utility-class constructor.
     */
    private CrowdHelper() {
    }

    public static UserAuthenticationContext createCrowdUserAuthContext(final String username,String password) {
        UserAuthenticationContext authenticationContext = new UserAuthenticationContext();
        authenticationContext.setName(username);
        authenticationContext.setCredential(new PasswordCredential(password));
        authenticationContext.setValidationFactors(new ValidationFactor[0]);
        return authenticationContext;
    }
    

  public static void removeCookie(final HttpServletResponse httpServletResponse,
            final RestCrowdClient restCrowdClient) throws ApplicationPermissionException,
            InvalidAuthenticationException, OperationFailedException {
        final CookieConfiguration cookieConfig = restCrowdClient.getCookieConfiguration();
        Cookie cookie = new Cookie(cookieConfig.getName(), "");
        cookie.setDomain(cookieConfig.getDomain());
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setComment("EXPIRING COOKIE at " + System.currentTimeMillis());
        httpServletResponse.addCookie(cookie);
    }

    public static String getCrowdCookie(ServletRequest request, final RestCrowdClient restCrowdClient)
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException {
        final String crowdCookieName = restCrowdClient.getCookieConfiguration().getName();
        final Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        String ssoToken = null;
        if (crowdCookieName != null) {
            for (Cookie cookie : cookies) {
                if (crowdCookieName.equals(cookie.getName())) {
                    ssoToken = cookie.getValue();
                }
            }
        }
        return ssoToken;
    }

    public static UserDetails toUserdetails(User user) {
        UserDetails details = new UserDetails();

        String displayName = user.getDisplayName();
        if (displayName != null) {
            final String[] arr = displayName.split("[ ]+");
            if (arr.length == 2 && arr[0].equals(arr[1])) {
                displayName = arr[0];
            }
        } else {
            displayName = user.getName();
        }

        details.setName(displayName);
        details.setEmail(user.getEmailAddress());
        return details;
    }

    public static void wrapException(Exception e) throws SonarException {
        final String message = e.getMessage();
        LOG.error(message, e);
        throw new SonarException(message, e);
    }
}
