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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarPlugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * 
 * 
 * @version $Id$
 * @author qxo
 * @since 2013-12-7
 */
public class CrowdPlugin extends SonarPlugin {
    
    /**
     * Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(CrowdPlugin.class);


    public List<?> getExtensions() {
        try{
            return ImmutableList.of(CrowdRealm.class,CrowdConfiguration.class,CrowdSsoFilter.class,CrowdLoginFilter.class,CrowdLogoutFilter.class);
        }catch(java.lang.Throwable  ex){
            LOG.warn("org.sonar.plugins.crowd.CrowdPlugin.getExtensions() exception:{}",ex.getMessage());            
            return Lists.newArrayList();
        }
    }
}
