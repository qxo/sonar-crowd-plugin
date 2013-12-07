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

import java.util.Properties;

import org.sonar.api.security.Authenticator;
import org.sonar.api.security.ExternalGroupsProvider;
import org.sonar.api.security.ExternalUsersProvider;
import org.sonar.api.security.LoginPasswordAuthenticator;
import org.sonar.api.security.SecurityRealm;

import com.atlassian.crowd.integration.rest.service.RestCrowdClient;
import com.atlassian.crowd.service.client.ClientPropertiesImpl;

/**
 * 
 * 
 * @version $Id$
 * @author qxo
 * @since 2013-12-7
 */
public class CrowdRealm extends SecurityRealm {

    private CrowdUsersProvider usersProvider;
    private CrowdGroupsProvider groupsProvider;
    private CrowdAuthenticator authenticator;
    private final CrowdConfiguration settingsManager;

    public CrowdRealm(CrowdConfiguration settingsManager) {// Settings settings
        this.settingsManager = settingsManager;
        // settingsManager = new CrowdConfiguration(settings);
    }

    @Override
    public String getName() {
        return "CROWD";
    }

    private RestCrowdClient restCrowdClient;
    
    public RestCrowdClient getRestCrowdClient() {
        return restCrowdClient;
    }

    /**
     * 
     */
    @Override
    public void init() {

        Properties properties = settingsManager.getClientProperties();
        // /System.out.println("properties:"+properties);
        final ClientPropertiesImpl clientProps = ClientPropertiesImpl.newInstanceFromProperties(properties);
        restCrowdClient = new RestCrowdClient(clientProps);

        this.authenticator = new CrowdAuthenticator(restCrowdClient);
        this.usersProvider = new CrowdUsersProvider(restCrowdClient);
        this.groupsProvider = new CrowdGroupsProvider(restCrowdClient);

    }

    @Override
    public Authenticator doGetAuthenticator() {
        return this.authenticator;
    }

    @Deprecated
    public LoginPasswordAuthenticator getLoginPasswordAuthenticator() {
        return authenticator;
    }

    @Override
    public ExternalUsersProvider getUsersProvider() {
        return usersProvider;
    }

    // @Override
    public ExternalGroupsProvider getGroupsProvider() {
        return groupsProvider;
    }

}
