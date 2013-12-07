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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.security.ExternalGroupsProvider;
import org.sonar.api.utils.SonarException;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.integration.rest.service.RestCrowdClient;
import com.atlassian.crowd.model.group.Group;
import com.google.common.collect.Sets;

/**
 * 
 * 
 * @version $Id$
 * @author qxo
 * @since 2013-12-7
 */
public class CrowdGroupsProvider extends ExternalGroupsProvider {
    /**
     * Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(CrowdGroupsProvider.class);

    private final RestCrowdClient restCrowdClient;

    public CrowdGroupsProvider(RestCrowdClient restCrowdClient) {
        super();
        this.restCrowdClient = restCrowdClient;
    }

    /**
     * @throws SonarException
     *             if unable to retrieve groups
     */
    public Collection<String> doGetGroups(String username) {
        if (LOG.isDebugEnabled()) {
            LOG.info("doGetGroups for user :{}", username);
        }
        Set<String> groupsStr = Sets.newHashSet();
        List<Group> groups;
        try {
            groupsStr.add("sonar-users"); // default group
            groups = restCrowdClient.getGroupsForUser(username, 0, -1);
            for (Group group : groups) {
                groupsStr.add(group.getName());
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("doGetGroups for user :{} groups:{}", username, groupsStr);
            }
        } catch (UserNotFoundException e) {
            CrowdHelper.wrapException(e);
        } catch (ApplicationPermissionException e) {
            CrowdHelper.wrapException(e);
        } catch (InvalidAuthenticationException e) {
            CrowdHelper.wrapException(e);
        } catch (OperationFailedException e) {
            CrowdHelper.wrapException(e);
        }

        return groupsStr;
    }
}
