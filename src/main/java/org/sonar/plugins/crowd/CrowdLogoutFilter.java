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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.web.ServletFilter;

import com.atlassian.crowd.integration.rest.service.RestCrowdClient;

public class CrowdLogoutFilter extends ServletFilter {
    /**
     * Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(CrowdLogoutFilter.class);

    private CrowdRealm crowdRealm;

    public CrowdLogoutFilter(CrowdRealm crowdRealm) {
        super();
        this.crowdRealm = crowdRealm;
    }

    @Override
    public UrlPattern doGetPattern() {
        return UrlPattern.create("/sessions/logout");
    }

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        final RestCrowdClient restCrowdClient = crowdRealm.getRestCrowdClient();
        try {
            final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            final String ssoToken = CrowdHelper.getCrowdCookie(request, restCrowdClient);
            if (ssoToken != null) {
                restCrowdClient.invalidateSSOToken(ssoToken);
                final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                CrowdHelper.removeCookie(httpServletResponse, restCrowdClient);
                HttpSession session = ((HttpServletRequest) request).getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                httpServletResponse.sendRedirect(httpServletRequest.getContextPath()
                        + "/sessions/new" );
                return;
            }
        } catch (Exception ex) {
            LOG.error("{}", ex, ex);
        }

       
        chain.doFilter(request, response);
        // restCrowdClient.authenticateSSOUser(arg0)

    }

    public void destroy() {
        // TODO Auto-generated method stub

    }

}
