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

package org.wso2.das.integration.tests.mb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.metrics.common.MetricsXMLConfiguration;
import org.wso2.carbon.metrics.impl.MetricServiceImpl;
import org.wso2.carbon.metrics.impl.MetricsLevelConfiguration;
import org.wso2.carbon.metrics.impl.util.DASReporterBuilder;
import org.wso2.carbon.metrics.manager.Counter;
import org.wso2.carbon.metrics.manager.Gauge;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Meter;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.Timer;
import org.wso2.das.integration.common.utils.ConfigurationEditor;
import org.wso2.das.integration.common.utils.DASIntegrationBaseTest;
import org.wso2.das.integration.common.utils.TestConstants;

import javax.management.MalformedObjectNameException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Class contains test cases related to statistics
 */
public class MBAnalyticsStatisticsAggregationTestCase extends DASIntegrationBaseTest {

    protected static final Log log = LogFactory.getLog(MBAnalyticsStatisticsAggregationTestCase.class);
    private static final int WAIT_FOR_INDEXING = 120000;
    private static final int TIMEOUT = 120000;
    private MetricService metricService;

    /**
     * Initialize carbon metrics DAS reporter and metric service. Then publish sample data to event streams and execute
     * spark script to aggregate data
     *
     * @throws Exception
     */
    @BeforeClass(groups = "wso2.das4mb.stats", alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        // Updating the redelivery attempts to 1 to speed up the test case.
        super.serverManager = new ServerConfigurationManager(dasServer);
        String defaultMBConfigurationPath = ServerConfigurationManager.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf" + File.separator + "metrics.xml";
        ConfigurationEditor configurationEditor = new ConfigurationEditor(defaultMBConfigurationPath);
        // Changing "maximumRedeliveryAttempts" value to "1" in broker.xml
        configurationEditor.updateProperty("Reporting/JDBC/Enabled", "false");
        configurationEditor.updateProperty("Reporting/DAS/Enabled", "false");
        configurationEditor.updateProperty("Reporting/DAS/ReceiverURL", "tcp://localhost:10011");
        configurationEditor.updateProperty("Reporting/DAS/DataAgentConfigPath",
                ServerConfigurationManager.getCarbonHome() + File.separator + "repository" +
                        File.separator + "conf" + File.separator + "data-bridge" + File.separator + "data-agent-config.xml");
        // Restarting server
        configurationEditor.applyUpdatedConfigurationAndRestartServer(serverManager);
        MetricsXMLConfiguration xmlConfiguration = new MetricsXMLConfiguration();
        xmlConfiguration.load(defaultMBConfigurationPath);
        // Load metrics properties
        MetricsLevelConfiguration levelConfiguration = new MetricsLevelConfiguration();
        String propertiesFilePath = ServerConfigurationManager.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf" + File.separator + "metrics.properties";
        levelConfiguration.load(propertiesFilePath);
        // Initialize DAS reporter
        DASReporterBuilder dasReporterBuilder = new DASReporterBuilder();
        dasReporterBuilder.configure(xmlConfiguration);
        dasReporterBuilder.setEnabled(true);
        // Initialize metric service
        metricService = new MetricServiceImpl.Builder().configure(xmlConfiguration)
                .addReporterBuilder(dasReporterBuilder).build(levelConfiguration);
        // Publish sample data to metrics event streams
        log.info("Publishing events");
        publishSampleData();
        // Wait for index data by Lucene
        log.info("Publishing complete. Waiting for indexing...");
        Thread.sleep(WAIT_FOR_INDEXING);
        // Execute spark script to aggregate data
        log.info("Indexing complete. Executing the spark scripts...");
        AnalyticsProcessorAdminServiceStub analyticsStub = getAnalyticsProcessorStub(TIMEOUT);
        analyticsStub.executeScript("mb_stat_analytics");
    }

    /**
     * Check message receive data exist in ORG_WSO2_MB_METER_STATS_HOUR
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test message receive hour data publishing")
    public void testMessageReceiveHourData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_METER_STATS_HOUR, "org.wso2.mb.message.receive", 2);
    }

    /**
     * Check message receive data exist in ORG_WSO2_MB_METER_STATS_DAY
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test message receive day data publishing")
    public void testMessageReceiveDayData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_METER_STATS_DAY, "org.wso2.mb.message.receive", 2);
    }

    /**
     * Check message receive data exist in ORG_WSO2_MB_METER_STATS_MONTH
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test message receive month data publishing")
    public void testMessageReceiveMonthData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_METER_STATS_MONTH, "org.wso2.mb.message.receive", 2);
    }

    /**
     * Check message sent data exist in ORG_WSO2_MB_METER_STATS_HOUR
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test message sent hour data publishing")
    public void testMessageSentHourData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_METER_STATS_HOUR, "org.wso2.mb.message.sent", 2);
    }

    /**
     * Check message sent data exist in ORG_WSO2_MB_METER_STATS_DAY
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test message sent day data publishing")
    public void testMessageSentDayData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_METER_STATS_DAY, "org.wso2.mb.message.sent", 2);
    }

    /**
     * Check message sent data exist in ORG_WSO2_MB_METER_STATS_MONTH
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test message sent month data publishing")
    public void testMessageSentMonthData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_METER_STATS_MONTH, "org.wso2.mb.message.sent", 2);
    }

    /**
     * Check ack receive data exist in ORG_WSO2_MB_METER_STATS_HOUR
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test ack receive hour data publishing")
    public void testAckReceiveHourData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_METER_STATS_HOUR, "org.wso2.mb.ack.receive", 2);
    }

    /**
     * Check ack receive data exist in ORG_WSO2_MB_METER_STATS_DAY
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test ack receive day data publishing")
    public void testAckReceiveDayData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_METER_STATS_DAY, "org.wso2.mb.ack.receive", 2);
    }

    /**
     * Check ack receive data exist in ORG_WSO2_MB_METER_STATS_MONTH
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test ack receive month data publishing")
    public void testAckReceiveMonthData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_METER_STATS_MONTH, "org.wso2.mb.ack.receive", 2);
    }

    /**
     * Check reject receive data exist in ORG_WSO2_MB_METER_STATS_HOUR
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test reject receive hour data publishing")
    public void testRejectReceiveHourData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_METER_STATS_HOUR, "org.wso2.mb.ack.receive", 2);
    }

    /**
     * Check reject receive data exist in ORG_WSO2_MB_METER_STATS_DAY
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test reject receive day data publishing")
    public void testRejectReceiveDayData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_METER_STATS_DAY, "org.wso2.mb.reject.receive", 2);
    }

    /**
     * Check reject receive data exist in ORG_WSO2_MB_METER_STATS_MONTH
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test reject receive month data publishing")
    public void testRejectReceiveMonthData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_METER_STATS_MONTH, "org.wso2.mb.reject.receive", 2);
    }

    /**
     * Check enqueue count data exist in ORG_WSO2_MB_COUNTER_STATS_HOUR
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test enqueue count hour data publishing")
    public void testEnqueueCountHourData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_COUNTER_STATS_HOUR, "org.wso2.mb.enqueue.count", 2);
    }

    /**
     * Check enqueue count data exist in ORG_WSO2_MB_COUNTER_STATS_DAY
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test enqueue count day data publishing")
    public void testEnqueueCountDayData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_COUNTER_STATS_DAY, "org.wso2.mb.enqueue.count", 2);
    }

    /**
     * Check enqueue count data exist in ORG_WSO2_MB_COUNTER_STATS_MONTH
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test enqueue count month data publishing")
    public void testEnqueueCountMonthData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_COUNTER_STATS_MONTH, "org.wso2.mb.enqueue.count", 2);
    }

    /**
     * Check dequeue count data exist in ORG_WSO2_MB_COUNTER_STATS_HOUR
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test dequeue count hour data publishing")
    public void testDequeueCountHourData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_COUNTER_STATS_HOUR, "org.wso2.mb.dequeue.count", 2);
    }

    /**
     * Check dequeue count data exist in ORG_WSO2_MB_COUNTER_STATS_DAY
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test dequeue count day data publishing")
    public void testDequeueCountDayData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_COUNTER_STATS_DAY, "org.wso2.mb.dequeue.count", 2);
    }

    /**
     * Check dequeue count data exist in ORG_WSO2_MB_COUNTER_STATS_MONTH
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test dequeue count month data publishing")
    public void testDequeueCountMonthData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_COUNTER_STATS_MONTH, "org.wso2.mb.dequeue.count", 2);
    }

    /**
     * Check ack count data exist in ORG_WSO2_MB_COUNTER_STATS_HOUR
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test ack count hour data publishing")
    public void testAckCountHourData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_COUNTER_STATS_HOUR, "org.wso2.mb.ack.count", 2);
    }

    /**
     * Check ack count data exist in ORG_WSO2_MB_COUNTER_STATS_DAY
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test ack count day data publishing")
    public void testAckCountDayData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_COUNTER_STATS_DAY, "org.wso2.mb.ack.count", 2);
    }

    /**
     * Check ack count data exist in ORG_WSO2_MB_COUNTER_STATS_MONTH
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test ack count month data publishing")
    public void testAckCountMonthData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_COUNTER_STATS_MONTH, "org.wso2.mb.ack.count", 2);
    }

    /**
     * Check reject count data exist in ORG_WSO2_MB_COUNTER_STATS_HOUR
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test reject count hour data publishing")
    public void testRejectCountHourData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_COUNTER_STATS_HOUR, "org.wso2.mb.reject.count", 2);
    }

    /**
     * Check reject count data exist in ORG_WSO2_MB_COUNTER_STATS_DAY
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test reject count day data publishing")
    public void testRejectCountDayData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_COUNTER_STATS_DAY, "org.wso2.mb.reject.count", 2);
    }

    /**
     * Check reject count data exist in ORG_WSO2_MB_COUNTER_STATS_MONTH
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test reject count month data publishing")
    public void testRejectCountMonthData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_COUNTER_STATS_MONTH, "org.wso2.mb.reject.count", 2);
    }

    /**
     * Check database read data exist in ORG_WSO2_MB_TIMER_STATS_HOUR
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test database read hour data publishing")
    public void testDatabaseReadHourData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_TIMER_STATS_HOUR, "org.wso2.mb.database.read", 2);
    }

    /**
     * Check database read data exist in ORG_WSO2_MB_TIMER_STATS_DAY
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test database read day data publishing")
    public void testDatabaseReadDayData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_TIMER_STATS_DAY, "org.wso2.mb.database.read", 2);
    }

    /**
     * Check database write data exist in ORG_WSO2_MB_TIMER_STATS_MONTH
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test database read month data publishing")
    public void testDatabaseReadMonthData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_TIMER_STATS_MONTH, "org.wso2.mb.database.read", 2);
    }

    /**
     * Check database write data exist in ORG_WSO2_MB_TIMER_STATS_HOUR
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test database write hour data publishing")
    public void testDatabaseWriteHourData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_TIMER_STATS_HOUR, "org.wso2.mb.database.write", 2);
    }

    /**
     * Check database write data exist in ORG_WSO2_MB_TIMER_STATS_DAY
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test database write day data publishing")
    public void testDatabaseWriteDayData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_TIMER_STATS_DAY, "org.wso2.mb.database.write", 2);
    }

    /**
     * Check database write data exist in ORG_WSO2_MB_TIMER_STATS_MONTH
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test database write month data publishing")
    public void testDatabaseWriteMonthData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_TIMER_STATS_MONTH, "org.wso2.mb.database.write", 2);
    }

    /**
     * Check active channel data exist in ORG_WSO2_MB_TIMER_STATS_HOUR
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test active channel hour data publishing")
    public void testActiveChannelHourData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_GAUGE_STATS_HOUR, "org.wso2.mb.channels.active.count", 2);
    }

    /**
     * Check active channel data exist in ORG_WSO2_MB_TIMER_STATS_DAY
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test active channel day data publishing")
    public void testActiveChannelDayData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_GAUGE_STATS_DAY, "org.wso2.mb.channels.active.count", 2);
    }

    /**
     * Check active channel data exist in ORG_WSO2_MB_GAUGE_STATS_MONTH
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test active channel month data publishing")
    public void testActiveChannelMonthData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_GAUGE_STATS_MONTH, "org.wso2.mb.channels.active.count", 2);
    }

    /**
     * Check queue subscriber data exist in ORG_WSO2_MB_TIMER_STATS_HOUR
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test queue subscriber hour data publishing")
    public void testQueueSubscriberHourData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_GAUGE_STATS_HOUR, "org.wso2.mb.queue.subscribers.count", 2);
    }

    /**
     * Check queue subscriber data exist in ORG_WSO2_MB_TIMER_STATS_DAY
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test queue subscriber day data publishing")
    public void testQueueSubscriberDayData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_GAUGE_STATS_DAY, "org.wso2.mb.queue.subscribers.count", 2);
    }

    /**
     * Check queue subscriber data exist in ORG_WSO2_MB_GAUGE_STATS_MONTH
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test queue subscriber month data publishing")
    public void testQueueSubscriberMonthData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_GAUGE_STATS_MONTH, "org.wso2.mb.queue.subscribers.count", 2);
    }

    /**
     * Check topic subscriber data exist in ORG_WSO2_MB_TIMER_STATS_HOUR
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test topic subscriber hour data publishing")
    public void testTopicSubscriberHourData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_GAUGE_STATS_HOUR, "org.wso2.mb.topic.subscribers.count", 2);
    }

    /**
     * Check topic subscriber data exist in ORG_WSO2_MB_TIMER_STATS_DAY
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test topic subscriber day data publishing")
    public void testTopicSubscriberDayData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_GAUGE_STATS_DAY, "org.wso2.mb.topic.subscribers.count", 2);
    }

    /**
     * Check topic subscriber data exist in ORG_WSO2_MB_GAUGE_STATS_MONTH
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.stats", description = "Test topic subscriber month data publishing")
    public void testTopicSubscriberMonthData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsException, InterruptedException {
        testCounts(TestConstants.ORG_WSO2_MB_GAUGE_STATS_MONTH, "org.wso2.mb.topic.subscribers.count", 2);
    }

    /**
     * Restore config to default, restart server and clean up all tables
     *
     * @throws IOException
     * @throws AutomationUtilException
     * @throws InterruptedException
     * @throws XPathExpressionException
     * @throws LoginAuthenticationExceptionException
     */
    @AfterClass(groups = "wso2.das4mb.stats", alwaysRun = true)
    public void tearDown() throws IOException, AutomationUtilException, InterruptedException,
            XPathExpressionException, LoginAuthenticationExceptionException, AnalyticsException {
        // Clean up tables
        cleanupTables();
    }

    /**
     * Publish sample data
     */
    private void publishSampleData() throws InterruptedException {
        // publish meter stats
        Meter messageReceive = metricService.meter("org.wso2.mb.message.receive", Level.INFO);
        messageReceive.mark();
        Meter messageSent = metricService.meter("org.wso2.mb.message.sent", Level.INFO);
        messageSent.mark();
        Meter ackReceive = metricService.meter("org.wso2.mb.ack.receive", Level.INFO);
        ackReceive.mark();
        Meter rejectReceive = metricService.meter("org.wso2.mb.reject.receive", Level.INFO);
        rejectReceive.mark();
        // publish counter stats
        Counter enqueueCounter = metricService.counter("org.wso2.mb.enqueue.count", Level.INFO);
        enqueueCounter.inc();
        Counter dequeueCounter = metricService.counter("org.wso2.mb.dequeue.count", Level.INFO);
        dequeueCounter.inc();
        Counter ackCounter = metricService.counter("org.wso2.mb.ack.count", Level.INFO);
        ackCounter.inc();
        Counter rejectCounter = metricService.counter("org.wso2.mb.reject.count", Level.INFO);
        rejectCounter.inc();
        //publish timer stats
        Timer.Context dbReadTimerContext = metricService.timer("org.wso2.mb.database.read", Level.INFO).start();
        dbReadTimerContext.stop();
        Timer.Context dbWriteTimerContext = metricService.timer("org.wso2.mb.database.write", Level.INFO).start();
        dbWriteTimerContext.stop();
        // publish gauge stats
        metricService.gauge("org.wso2.mb.channels.active.count", Level.INFO, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 1;
            }
        });
        metricService.gauge("org.wso2.mb.queue.subscribers.count", Level.INFO, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 1;
            }
        });
        metricService.gauge("org.wso2.mb.topic.subscribers.count", Level.INFO, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 1;
            }
        });
        // report above data to event streams
        metricService.report();
        TimeUnit.SECONDS.sleep(15);
    }


    /**
     * Test counts in a given table
     *
     * @param table         Table name
     * @param name          Value of field name to query
     * @param expectedCount Expected count
     * @throws AnalyticsException
     */
    private void testCounts(String table, String name, int expectedCount) throws AnalyticsException {
        int records = this.analyticsDataAPI.searchCount(-1234,
                table, "_name :\"" + name + "\"");
        Assert.assertEquals(records, expectedCount, "");
    }
}
