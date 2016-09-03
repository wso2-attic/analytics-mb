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

public class TestConstants {

    public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String BASE64_ADMIN_ADMIN = "Basic YWRtaW46YWRtaW4=";
    public static final String BASE64_TENANT_ADMIN_ADMIN = "Basic YWRtaW5Ad3NvMi5jb206YWRtaW4=";
    public static final String ANALYTICS_JS_ENDPOINT = "https://localhost:11843/portal/apis/analytics";

    // Minute Tables
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_COUNTER_STATS_MINUTE = "ORG_WSO2_MB_ANALYTICS_STREAM_COUNTERSTATSMINUTE";
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_GAUGE_STATS_MINUTE = "ORG_WSO2_MB_ANALYTICS_STREAM_GAUGESTATSMINUTE";
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_METER_STATS_MINUTE = "ORG_WSO2_MB_ANALYTICS_STREAM_METERSTATSMINUTE";
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_TIMER_STATS_MINUTE = "ORG_WSO2_MB_ANALYTICS_STREAM_TIMERSTATSMINUTE";

    // Hour Tables
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_COUNTER_STATS_HOUR = "ORG_WSO2_MB_ANALYTICS_STREAM_COUNTERSTATSHOUR";
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_GAUGE_STATS_HOUR = "ORG_WSO2_MB_ANALYTICS_STREAM_GAUGESTATSHOUR";
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_METER_STATS_HOUR = "ORG_WSO2_MB_ANALYTICS_STREAM_METERSTATSHOUR";
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_TIMER_STATS_HOUR = "ORG_WSO2_MB_ANALYTICS_STREAM_TIMERSTATSHOUR";

    // Day Tables
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_COUNTER_STATS_DAY = "ORG_WSO2_MB_ANALYTICS_STREAM_COUNTERSTATSDAY";
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_GAUGE_STATS_DAY = "ORG_WSO2_MB_ANALYTICS_STREAM_GAUGESTATSDAY";
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_METER_STATS_DAY = "ORG_WSO2_MB_ANALYTICS_STREAM_METERSTATSDAY";
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_TIMER_STATS_DAY = "ORG_WSO2_MB_ANALYTICS_STREAM_TIMERSTATSDAY";

    // Month Tables
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_COUNTER_STATS_MONTH = "ORG_WSO2_MB_ANALYTICS_STREAM_COUNTERSTATSMONTH";
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_GAUGE_STATS_MONTH = "ORG_WSO2_MB_ANALYTICS_STREAM_GAUGESTATSMONTH";
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_METER_STATS_MONTH = "ORG_WSO2_MB_ANALYTICS_STREAM_METERSTATSMONTH";
    public static final String ORG_WSO2_MB_ANALYTICS_STREAM_TIMER_STATS_MONTH = "ORG_WSO2_MB_ANALYTICS_STREAM_TIMERSTATSMONTH";

    // Carbon metric streams
    public static final String ORG_WSO2_METRICS_STREAM_COUNTER = "ORG_WSO2_METRICS_STREAM_COUNTER";
    public static final String ORG_WSO2_METRICS_STREAM_GAUGE = "ORG_WSO2_METRICS_STREAM_GAUGE";
    public static final String ORG_WSO2_METRICS_STREAM_HISTOGRAM = "ORG_WSO2_METRICS_STREAM_HISTOGRAM";
    public static final String ORG_WSO2_METRICS_STREAM_METER = "ORG_WSO2_METRICS_STREAM_METER";
    public static final String ORG_WSO2_METRICS_STREAM_TIMER = "ORG_WSO2_METRICS_STREAM_TIMER";
}
