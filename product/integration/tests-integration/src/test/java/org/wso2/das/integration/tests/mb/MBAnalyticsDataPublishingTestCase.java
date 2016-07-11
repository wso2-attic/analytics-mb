package org.wso2.das.integration.tests.mb;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das4mb.integration.common.clients.DataPublisherClient;

/**
 * Test cases to check data publishing to analytics-mb through carbon metrics DAS reporter
 */
public class MBAnalyticsDataPublishingTestCase extends DASIntegrationTest {

    private DataPublisherClient dataPublisherClient;

    @BeforeClass(groups = "wso2.das4mb.publishing", dependsOnGroups = "wso2.das", alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        this.dataPublisherClient = new DataPublisherClient();
    }

    @Test(groups = "wso2.das4mb.publishing", description = "Test metrics meter data publishing")
    public void testMetricsMeterData() {

    }

    @AfterClass(alwaysRun = true, groups = "wso2.das4esb.publishing")
    public void cleanUpTables() throws Exception {
        restartAndCleanUpTables(120000);
    }
}
