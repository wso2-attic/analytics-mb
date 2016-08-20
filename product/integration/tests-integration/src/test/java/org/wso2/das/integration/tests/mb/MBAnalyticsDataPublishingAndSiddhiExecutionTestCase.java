package org.wso2.das.integration.tests.mb;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
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
 * Test cases to check data publishing to analytics-mb through carbon metrics DAS reporter
 */
public class MBAnalyticsDataPublishingAndSiddhiExecutionTestCase extends DASIntegrationBaseTest {

    private MetricService metricService;

    /**
     * Initialize carbon metrics DAS reporter and metric service
     *
     * @throws Exception
     */
    @BeforeClass(groups = "wso2.das4mb.publish", alwaysRun = true)
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
    }

    /**
     * Publish message receive data to ORG_WSO2_METRICS_STREAM_METER stream
     * FilterStreamExecutionPlan should insert record to ORG_WSO2_MB_METER_STATS_MINUTE table if published data match
     * with filtering criteria
     * Wait 10 seconds until processing execution complete
     * Assert both table and verify records added
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsIndexException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.publish", description = "Test message receive data publishing")
    public void testMessageReceiveData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsIndexException, InterruptedException {
        Meter messageReceive = metricService.meter("org.wso2.mb.message.receive", Level.INFO);
        messageReceive.mark();
        metricService.report();
        TimeUnit.SECONDS.sleep(15);
        int metricsRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_METRICS_STREAM_METER, "*:*");
        int mbRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_MB_METER_STATS_MINUTE, "_name :\"org.wso2.mb.message.receive\"");
        Assert.assertTrue(metricsRecordsCount > 0, "Expected metrics record count > 0 but found " + metricsRecordsCount);
        Assert.assertEquals(mbRecordsCount, 1, "Expected message receive record count is 1 but found " + mbRecordsCount);

    }

    /**
     * Publish message sent data to ORG_WSO2_METRICS_STREAM_METER stream
     * FilterStreamExecutionPlan should insert record to ORG_WSO2_MB_METER_STATS_MINUTE table if published data match
     * with filtering criteria
     * Wait 10 seconds until processing execution complete
     * Assert both table and verify records added
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsIndexException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.publish", description = "Test message sent data publishing")
    public void testMessageSentData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsIndexException, InterruptedException {
        Meter messageSent = metricService.meter("org.wso2.mb.message.sent", Level.INFO);
        messageSent.mark();
        metricService.report();
        TimeUnit.SECONDS.sleep(15);
        int metricsRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_METRICS_STREAM_METER, "*:*");
        int mbRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_MB_METER_STATS_MINUTE, "_name :\"org.wso2.mb.message.sent\"");
        Assert.assertTrue(metricsRecordsCount > 0, "Expected metrics record count > 0 but found " + metricsRecordsCount);
        Assert.assertEquals(mbRecordsCount, 1, "Expected message sent record count is 1 but found " + mbRecordsCount);

    }

    /**
     * Publish ack receive data to ORG_WSO2_METRICS_STREAM_METER stream
     * FilterStreamExecutionPlan should insert record to ORG_WSO2_MB_METER_STATS_MINUTE table if published data match
     * with filtering criteria
     * Wait 10 seconds until processing execution complete
     * Assert both table and verify records added
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsIndexException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.publish", description = "Test ack receive data publishing")
    public void testAckReceiveData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsIndexException, InterruptedException {
        Meter ackReceive = metricService.meter("org.wso2.mb.ack.receive", Level.INFO);
        ackReceive.mark();
        metricService.report();
        TimeUnit.SECONDS.sleep(15);
        int metricsRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_METRICS_STREAM_METER, "*:*");
        int mbRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_MB_METER_STATS_MINUTE, "_name :\"org.wso2.mb.ack.receive\"");
        Assert.assertTrue(metricsRecordsCount > 0, "Expected metrics record count > 0 but found " + metricsRecordsCount);
        Assert.assertEquals(mbRecordsCount, 1, "Expected ack receive record count is 1 but found " + mbRecordsCount);

    }

    /**
     * Publish reject receive data to ORG_WSO2_METRICS_STREAM_METER stream
     * FilterStreamExecutionPlan should insert record to ORG_WSO2_MB_METER_STATS_MINUTE table if published data match
     * with filtering criteria
     * Wait 10 seconds until processing execution complete
     * Assert both table and verify records added
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsIndexException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.publish", description = "Test reject receive data publishing")
    public void testRejectReceiveData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsIndexException, InterruptedException {
        Meter rejectReceive = metricService.meter("org.wso2.mb.reject.receive", Level.INFO);
        rejectReceive.mark();
        metricService.report();
        TimeUnit.SECONDS.sleep(15);
        int metricsRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_METRICS_STREAM_METER, "*:*");
        int mbRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_MB_METER_STATS_MINUTE, "_name :\"org.wso2.mb.reject.receive\"");
        Assert.assertTrue(metricsRecordsCount > 0, "Expected metrics record count > 0 but found " + metricsRecordsCount);
        Assert.assertEquals(mbRecordsCount, 1, "Expected reject receive record count is 1 but found " + mbRecordsCount);

    }

    /**
     * Publish enqueue message count data to ORG_WSO2_METRICS_STREAM_COUNTER stream
     * FilterStreamExecutionPlan should insert record to ORG_WSO2_MB_COUNTER_STATS_MINUTE table if published data match
     * with filtering criteria
     * Wait 10 seconds until processing execution complete
     * Assert both table and verify records added
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsIndexException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.publish", description = "Test enqueue message data publishing")
    public void testEnqueueMessageCountData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsIndexException, InterruptedException {
        Counter enqueueCounter = metricService.counter("org.wso2.mb.enqueue.count", Level.INFO);
        enqueueCounter.inc();
        metricService.report();
        TimeUnit.SECONDS.sleep(15);
        int metricsRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_METRICS_STREAM_COUNTER, "*:*");
        int mbRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_MB_COUNTER_STATS_MINUTE, "_name :\"org.wso2.mb.enqueue.count\"");
        Assert.assertTrue(metricsRecordsCount > 0, "Expected metrics record count > 0 but found " + metricsRecordsCount);
        Assert.assertEquals(mbRecordsCount, 1, "Expected enqueue message record count is 1 but found " + mbRecordsCount);

    }

    /**
     * Publish dequeue message count data to ORG_WSO2_METRICS_STREAM_COUNTER stream
     * FilterStreamExecutionPlan should insert record to ORG_WSO2_MB_COUNTER_STATS_MINUTE table if published data match
     * with filtering criteria
     * Wait 10 seconds until processing execution complete
     * Assert both table and verify records added
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsIndexException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.publish", description = "Test dequeue message count data publishing")
    public void testDequeueMessageCountData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsIndexException, InterruptedException {
        Counter dequeueCounter = metricService.counter("org.wso2.mb.dequeue.count", Level.INFO);
        dequeueCounter.inc();
        metricService.report();
        TimeUnit.SECONDS.sleep(15);
        int metricsRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_METRICS_STREAM_COUNTER, "*:*");
        int mbRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_MB_COUNTER_STATS_MINUTE, "_name :\"org.wso2.mb.dequeue.count\"");
        Assert.assertTrue(metricsRecordsCount > 0, "Expected metrics record count > 0 but found " + metricsRecordsCount);
        Assert.assertEquals(mbRecordsCount, 1, "Expected dequeue message record count is 1 but found " + mbRecordsCount);

    }

    /**
     * Publish ack message count data to ORG_WSO2_METRICS_STREAM_COUNTER stream
     * FilterStreamExecutionPlan should insert record to ORG_WSO2_MB_COUNTER_STATS_MINUTE table if published data match
     * with filtering criteria
     * Wait 10 seconds until processing execution complete
     * Assert both table and verify records added
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsIndexException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.publish", description = "Test ack message count data publishing")
    public void testAckMessageCountData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsIndexException, InterruptedException {
        Counter ackCounter = metricService.counter("org.wso2.mb.ack.count", Level.INFO);
        ackCounter.inc();
        metricService.report();
        TimeUnit.SECONDS.sleep(15);
        int metricsRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_METRICS_STREAM_COUNTER, "*:*");
        int mbRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_MB_COUNTER_STATS_MINUTE, "_name :\"org.wso2.mb.ack.count\"");
        Assert.assertTrue(metricsRecordsCount > 0, "Expected metrics record count > 0 but found " + metricsRecordsCount);
        Assert.assertEquals(mbRecordsCount, 1, "Expected ack message record count is 1 but found " + mbRecordsCount);

    }

    /**
     * Publish reject message count data to ORG_WSO2_METRICS_STREAM_COUNTER stream
     * FilterStreamExecutionPlan should insert record to ORG_WSO2_MB_COUNTER_STATS_MINUTE table if published data match
     * with filtering criteria
     * Wait 10 seconds until processing execution complete
     * Assert both table and verify records added
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsIndexException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.publish", description = "Test reject message count data publishing")
    public void testRejectMessageCountData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsIndexException, InterruptedException {
        Counter rejectCounter = metricService.counter("org.wso2.mb.reject.count", Level.INFO);
        rejectCounter.inc();
        metricService.report();
        TimeUnit.SECONDS.sleep(15);
        int metricsRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_METRICS_STREAM_COUNTER, "*:*");
        int mbRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_MB_COUNTER_STATS_MINUTE, "_name :\"org.wso2.mb.reject.count\"");
        Assert.assertTrue(metricsRecordsCount > 0, "Expected metrics record count > 0 but found " + metricsRecordsCount);
        Assert.assertEquals(mbRecordsCount, 1, "Expected reject message record count is 1 but found " + mbRecordsCount);

    }

    /**
     * Publish database read time data to ORG_WSO2_METRICS_STREAM_TIMER stream
     * FilterStreamExecutionPlan should insert record to ORG_WSO2_MB_TIMER_STATS_MINUTE table if published data match
     * with filtering criteria
     * Wait 10 seconds until processing execution complete
     * Assert both table and verify records added
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsIndexException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.publish", description = "Test database read time data publishing")
    public void testDatabaseReadTimeData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsIndexException, InterruptedException {
        Timer.Context dbReadTimerContext = metricService.timer("org.wso2.mb.database.read", Level.INFO).start();
        dbReadTimerContext.stop();
        metricService.report();
        TimeUnit.SECONDS.sleep(15);
        int metricsRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_METRICS_STREAM_TIMER, "*:*");
        int mbRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_MB_TIMER_STATS_MINUTE, "_name :\"org.wso2.mb.database.read\"");
        Assert.assertTrue(metricsRecordsCount > 0, "Expected metrics record count > 0 but found " + metricsRecordsCount);
        Assert.assertEquals(mbRecordsCount, 1, "Expected, db read record count is 1 but found " + mbRecordsCount);

    }

    /**
     * Publish database write time data to ORG_WSO2_METRICS_STREAM_TIMER stream
     * FilterStreamExecutionPlan should insert record to ORG_WSO2_MB_TIMER_STATS_MINUTE table if published data match
     * with filtering criteria
     * Wait 10 seconds until processing execution complete
     * Assert both table and verify records added
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsIndexException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.publish", description = "Test database write time data publishing")
    public void testDatabaseWriteTimeData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsIndexException, InterruptedException {
        Timer.Context dbWriteTimerContext = metricService.timer("org.wso2.mb.database.write", Level.INFO).start();
        dbWriteTimerContext.stop();
        metricService.report();
        TimeUnit.SECONDS.sleep(15);
        int metricsRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_METRICS_STREAM_TIMER, "*:*");
        int mbRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_MB_TIMER_STATS_MINUTE, "_name :\"org.wso2.mb.database.write\"");
        Assert.assertTrue(metricsRecordsCount > 0, "Expected metrics record count > 0 but found " + metricsRecordsCount);
        Assert.assertEquals(mbRecordsCount, 1, "Expected db write record count is 1 but found " + mbRecordsCount);

    }

    /**
     * Publish active channel count data to ORG_WSO2_METRICS_STREAM_GAUGE stream
     * FilterStreamExecutionPlan should insert record to ORG_WSO2_MB_GAUGE_STATS_MINUTE table if published data match
     * with filtering criteria
     * Wait 10 seconds until processing execution complete
     * Assert both table and verify records added
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsIndexException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.publish", description = "Test active channel count data publishing")
    public void testActiveChannelsData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsIndexException, InterruptedException {
        metricService.gauge("org.wso2.mb.channels.active.count", Level.INFO, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 1;
            }
        });
        metricService.report();
        TimeUnit.SECONDS.sleep(15);
        int metricsRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_METRICS_STREAM_GAUGE, "*:*");
        int mbRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_MB_GAUGE_STATS_MINUTE, "_name :\"org.wso2.mb.channels.active.count\"");
        Assert.assertTrue(metricsRecordsCount > 0, "Expected metrics record count > 0 but found " + metricsRecordsCount);
        Assert.assertEquals(mbRecordsCount, 1, "Expected active channel record count is 1 but found " + mbRecordsCount);

    }

    /**
     * Publish queue subscriber count data to ORG_WSO2_METRICS_STREAM_GAUGE stream
     * FilterStreamExecutionPlan should insert record to ORG_WSO2_MB_GAUGE_STATS_MINUTE table if published data match
     * with filtering criteria
     * Wait 10 seconds until processing execution complete
     * Assert both table and verify records added
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsIndexException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.publish", description = "Test queue subscriber count data publishing")
    public void testQueueSubscriberData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsIndexException, InterruptedException {
        metricService.gauge("org.wso2.mb.queue.subscribers.count", Level.INFO, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 1;
            }
        });
        metricService.report();
        TimeUnit.SECONDS.sleep(15);
        int metricsRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_METRICS_STREAM_GAUGE, "*:*");
        int mbRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_MB_GAUGE_STATS_MINUTE, "_name :\"org.wso2.mb.queue.subscribers.count\"");
        Assert.assertTrue(metricsRecordsCount > 0, "Expected metrics record count > 0 but found " + metricsRecordsCount);
        Assert.assertEquals(mbRecordsCount, 1, "Expected queue subscriber record count is 1 but found " + mbRecordsCount);

    }

    /**
     * Publish topic subscriber count data to ORG_WSO2_METRICS_STREAM_GAUGE stream
     * FilterStreamExecutionPlan should insert record to ORG_WSO2_MB_GAUGE_STATS_MINUTE table if published data match
     * with filtering criteria
     * Wait 10 seconds until processing execution complete
     * Assert both table and verify records added
     *
     * @throws XPathExpressionException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws AnalyticsIndexException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.das4mb.publish", description = "Test topic subscriber count data publishing")
    public void testTopicSubscriberData() throws XPathExpressionException, MalformedObjectNameException, IOException,
            AnalyticsIndexException, InterruptedException {
        metricService.gauge("org.wso2.mb.topic.subscribers.count", Level.INFO, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 1;
            }
        });
        metricService.report();
        TimeUnit.SECONDS.sleep(15);
        int metricsRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_METRICS_STREAM_GAUGE, "*:*");
        int mbRecordsCount = this.analyticsDataAPI.searchCount(-1234,
                TestConstants.ORG_WSO2_MB_GAUGE_STATS_MINUTE, "_name :\"org.wso2.mb.topic.subscribers.count\"");
        Assert.assertTrue(metricsRecordsCount > 0, "Expected metrics record count > 0 but found " + metricsRecordsCount);
        Assert.assertEquals(mbRecordsCount, 1, "Expected topic subscriber record count is 1 but found " + mbRecordsCount);

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
    @AfterClass(groups = "wso2.das4mb.publish", alwaysRun = true)
    public void tearDown() throws IOException, AutomationUtilException, InterruptedException,
            XPathExpressionException, LoginAuthenticationExceptionException, AnalyticsException {
        // Clean up tables
        cleanupTables();
    }
}
