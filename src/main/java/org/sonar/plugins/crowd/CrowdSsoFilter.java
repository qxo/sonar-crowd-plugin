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

import com.atlassian.crowd.exception.InvalidTokenException;
import com.atlassian.crowd.integration.rest.service.RestCrowdClient;
import com.atlassian.crowd.model.user.User;

/**
 * 
 * @version $Id$
 * @author qxo
 * @since 2013-12-8
 */
public class CrowdSsoFilter extends ServletFilter {
    /**
     * Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(CrowdSsoFilter.class);

    private CrowdRealm crowdRealm;

    public CrowdSsoFilter(CrowdRealm crowdRealm) {
        super();
        this.crowdRealm = crowdRealm;
    }

    @Override
    public UrlPattern doGetPattern() {
        return UrlPattern.create("/sessions/new");
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }


    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
          try {

            final HttpSession session = httpServletRequest.getSession(true);
          //  String ssoLogin = httpServletRequest.getParameter("ssoLogin");
            
            Long ssoLoginTs = (Long)session.getAttribute("ssoLoginTs");
            if ( ssoLoginTs != null  && ( System.currentTimeMillis()- ssoLoginTs < 60000 )) {
                chain.doFilter(httpServletRequest, response);
                return;
            }
            final RestCrowdClient restCrowdClient = crowdRealm.getRestCrowdClient();
            try {
                try {
                    final String ssoToken = CrowdHelper.getCrowdCookie(request, restCrowdClient);
                    LOG.debug("ssoToken:{}", ssoToken);
                    if (ssoToken != null) {
                        session.setAttribute("ssoLoginTs",System.currentTimeMillis());
                        final User user = restCrowdClient.findUserFromSSOToken(ssoToken);
                        if (user != null) {
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath()
                                    + "/crowd/validate?ssoLogin=true&login=" + user.getName());
                            return;
                        }
                    }

                } catch (InvalidTokenException ex) {
                    CrowdHelper.removeCookie(httpServletResponse, restCrowdClient);
                    throw ex;
                }
            } catch (Exception ex) {
                LOG.error("{}", ex, ex);
                // session.setAttribute("ssoLogin", false);
            }

            chain.doFilter(request, response);
            // restCrowdClient.authenticateSSOUser(arg0)
        } finally {
        }
    }


    public void destroy() {
    }

}
