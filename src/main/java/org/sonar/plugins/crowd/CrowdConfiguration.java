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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.sonar.api.ServerExtension;
import org.sonar.api.config.Settings;

/**
 * 
 * 
 * @version $Id$
 * @author qxo
 * @since 2013-12-8
 */
public class CrowdConfiguration implements ServerExtension {
    private final Settings settings;
    private Properties clientProperties;

    /**
     * Creates new instance of CrowdConfiguration.
     * 
     * @param configuration
     *            configuration
     */
    public CrowdConfiguration(Settings settings) {
        this.settings = settings;
    }

    /**
     * Returns Crowd client properties.
     * 
     * @return Crowd client properties
     */
    public Properties getClientProperties() {
        if (clientProperties == null) {
            clientProperties = newInstance();
        }
        return clientProperties;
    }

    private String getString(String key) {
        final Properties globalProperties = globalCongfig();
        if (globalProperties.containsKey(key)) {
            return globalProperties.getProperty(key);
        }
        return settings.getString(key);
    }

    private Properties globalProperties;

    protected Properties globalCongfig() {
        if (null == globalProperties) {
            globalProperties = new Properties();
            String globalConfig = System.getProperty("globalConfig", "globalConfig.peroperties");
            InputStream in = CrowdConfiguration.class.getResourceAsStream(globalConfig);
            try {
                if (in == null) {
                    if (new File(globalConfig).exists()) {
                        in = new FileInputStream(globalConfig);
                    }
                }
                if (in != null) {
                    try {
                        globalProperties.load(in);
                    } finally {
                        in.close();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return globalProperties;
    }

    private Properties newInstance() {
        final String crowdUrl = getString("crowd.url");
        String applicationName = getString("crowd.application");
        final String applicationPassword = getString("crowd.password");

        if (crowdUrl == null) {
            throw new IllegalArgumentException("Crowd URL is not set");
        }
        if (applicationName == null) {
            applicationName = "sonar";
        }
        if (applicationPassword == null) {
            throw new IllegalArgumentException("Crowd Application Password is not set");
        }

        if (CrowdHelper.LOG.isInfoEnabled()) {
            CrowdHelper.LOG.info("URL: " + crowdUrl);
            CrowdHelper.LOG.info("Application Name: " + applicationName);
        }

        Properties properties = new Properties();
        properties.setProperty("crowd.server.url", crowdUrl);
        properties.setProperty("application.login.url", crowdUrl);
        properties.setProperty("application.name", applicationName);
        properties.setProperty("application.password", applicationPassword);
        properties.setProperty("session.validationinterval", "5");
        return properties;
    }
}