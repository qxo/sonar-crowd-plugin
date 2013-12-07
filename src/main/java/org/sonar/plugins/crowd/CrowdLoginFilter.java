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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.web.ServletFilter;

/**
 * 
 * @version $Id$
 * @author qxo
 * @since 2013-12-8
 */
public class CrowdLoginFilter extends ServletFilter {
    /**
     * Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(CrowdLoginFilter.class);

    private CrowdRealm crowdRealm;

    public CrowdLoginFilter(CrowdRealm crowdRealm) {
        super();
        this.crowdRealm = crowdRealm;
    }

    @Override
    public UrlPattern doGetPattern() {
        return UrlPattern.create("/sessions/login");
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }

    static ThreadLocal<HttpServletResponse> httpServletResponseThreadLocal = new ThreadLocal<HttpServletResponse>() {
    };

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponseThreadLocal.set(httpServletResponse);
        try {
            chain.doFilter(httpServletRequest, response);

        } finally {

            httpServletResponseThreadLocal.set(null);
        }
    }

    public void destroy() {
    }

}
