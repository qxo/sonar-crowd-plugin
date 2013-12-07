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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.security.ExternalUsersProvider;
import org.sonar.api.security.UserDetails;
import org.sonar.api.utils.SonarException;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.integration.rest.service.RestCrowdClient;
import com.atlassian.crowd.model.user.User;

/**
 * 
 *
 * @version $Id$
 * @author qxo 
 * @since  2013-12-7
 */
public class CrowdUsersProvider extends ExternalUsersProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CrowdUsersProvider.class);
    private final RestCrowdClient restCrowdClient;

    public CrowdUsersProvider(RestCrowdClient restCrowdClient) {
        super();
        this.restCrowdClient = restCrowdClient;
    }

    /**
     * @return details for specified user, or null if such user doesn't exist
     * @throws SonarException
     *             if unable to retrieve details
     */
    public UserDetails doGetUserDetails(String username) {
        LOG.debug("Requesting details for user {}", username);
       try {
           if( null == username){
               return null;
           }
           User user = restCrowdClient.getUser(username);
           return CrowdHelper.toUserdetails(user);
        } catch (UserNotFoundException e) {
            CrowdHelper.wrapException(e);
        } catch (ApplicationPermissionException e) {
            CrowdHelper.wrapException(e);
        } catch (InvalidAuthenticationException e) {
            CrowdHelper.wrapException(e);
        } catch (OperationFailedException e) {
            CrowdHelper.wrapException(e);
        }
        return null;
    }
}
