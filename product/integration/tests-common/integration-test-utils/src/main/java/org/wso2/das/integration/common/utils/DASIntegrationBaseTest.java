/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.das.integration.common.utils;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

/**
 * Every test class must extends this base class and call init() method to initialize necessary objects.
 */
public class DASIntegrationBaseTest {

    protected static final Log log = LogFactory.getLog(DASIntegrationBaseTest.class);
    private static final String ANALYTICS_SERVICE_NAME = "AnalyticsProcessorAdminService";
    protected AutomationContext dasServer;
    protected String backendURL;
    protected String webAppURL;
    protected LoginLogoutClient loginLogoutClient;
    protected User userInfo;
    protected String thriftURL;
    protected AnalyticsDataAPI analyticsDataAPI;
    protected ServerConfigurationManager serverManager = null;

    /**
     * Initialize analytics API
     *
     * @throws Exception
     */
    protected void init() throws Exception {
        init(TestUserMode.SUPER_TENANT_ADMIN);
        String apiConf = new File(this.getClass().getClassLoader().getResource("dasconfig" + File.separator + "api" +
                File.separator + "analytics-data-config.xml").toURI()).getAbsolutePath();
        this.analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
    }

    /**
     * Initialize automation context, context URL, webapp URL and user context
     *
     * @param testUserMode User mode for automation context
     * @throws Exception
     */
    protected void init(TestUserMode testUserMode) throws Exception {
        dasServer = new AutomationContext("DAS", testUserMode);
        loginLogoutClient = new LoginLogoutClient(dasServer);
        backendURL = dasServer.getContextUrls().getBackEndUrl();
        webAppURL = dasServer.getContextUrls().getWebAppURL();
        userInfo = dasServer.getContextTenant().getContextUser();
    }

    /**
     * Get session cookie
     *
     * @return session cookie
     * @throws Exception
     */
    protected String getSessionCookie() throws Exception {
        return loginLogoutClient.login();
    }

    /**
     * Read resource from registry and return content
     *
     * @param testClass    class
     * @param resourcePath registry resource path
     * @return resource content
     * @throws Exception
     */
    protected String getResourceContent(Class testClass, String resourcePath) throws Exception {
        String content = "";
        URL url = testClass.getClassLoader().getResource(resourcePath);
        if (url != null) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    new File(url.toURI()).getAbsolutePath()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content += line;
            }
            return content;
        } else {
            throw new Exception("No resource found in the given path : " + resourcePath);
        }
    }

    /**
     * Get stub of analytics processor
     *
     * @return {@link AnalyticsProcessorAdminServiceStub} object
     * @throws Exception
     */
    protected AnalyticsProcessorAdminServiceStub getAnalyticsProcessorStub(int timeout) throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);
        String loggedInSessionCookie = getSessionCookie();
        AnalyticsProcessorAdminServiceStub analyticsStub = new AnalyticsProcessorAdminServiceStub(configContext,
                backendURL + ANALYTICS_SERVICE_NAME);
        ServiceClient client = analyticsStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);
        option.setTimeOutInMilliSeconds(timeout);
        return analyticsStub;
    }

    /**
     * Cleanup all Analytics Tables and restart the server to clean in memory events.
     *
     * @throws AnalyticsException
     */
    protected void cleanupTables() throws AnalyticsException {
        String[] tables = new String[]{
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_COUNTER_STATS_MINUTE,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_GAUGE_STATS_MINUTE,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_METER_STATS_MINUTE,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_TIMER_STATS_MINUTE,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_COUNTER_STATS_HOUR,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_GAUGE_STATS_HOUR,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_METER_STATS_HOUR,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_TIMER_STATS_HOUR,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_COUNTER_STATS_DAY,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_GAUGE_STATS_DAY,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_METER_STATS_DAY,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_TIMER_STATS_DAY,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_COUNTER_STATS_MONTH,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_GAUGE_STATS_MONTH,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_METER_STATS_MONTH,
                TestConstants.ORG_WSO2_MB_ANALYTICS_STREAM_TIMER_STATS_MONTH,
                TestConstants.ORG_WSO2_METRICS_STREAM_COUNTER,
                TestConstants.ORG_WSO2_METRICS_STREAM_GAUGE,
                TestConstants.ORG_WSO2_METRICS_STREAM_HISTOGRAM,
                TestConstants.ORG_WSO2_METRICS_STREAM_METER,
                TestConstants.ORG_WSO2_METRICS_STREAM_TIMER
        };
        for (String table : tables) {
            int recordsCount = 0;
            recordsCount = this.analyticsDataAPI.searchCount(-1234, table, "*:*");
            if (recordsCount > 0) {
                this.analyticsDataAPI.delete(-1234, table, Long.MIN_VALUE, Long.MAX_VALUE);
            }
        }
    }

    /**
     * Returns JMX RMI Server port based on automation.xml configurations
     *
     * @throws XPathExpressionException
     */
    protected Integer getJMXServerPort() throws XPathExpressionException {
        return Integer.parseInt(dasServer.getInstance().getPorts().get("jmxserver"));
    }

    /**
     * Returns JMX RMI Registry port based on automation.xml configurations
     *
     * @throws XPathExpressionException
     */
    protected Integer getRMIRegistryPort() throws XPathExpressionException {
        return Integer.parseInt(dasServer.getInstance().getPorts().get("rmiregistry"));
    }
}

