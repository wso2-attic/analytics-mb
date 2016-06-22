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

package org.wso2.analytics.mb.bean;

/**
 * Bean class for different set of time units with ranges
 */
public class TimeRange {

    private String unit;
    private long[] range;

    /**
     * Get time unit i.e. SECOND, MINUTE, HOUR, DAY, MONTH
     * @return time unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Set time unit i.e. SECOND, MINUTE, HOUR, DAY, MONTH
     * @param unit time unit
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Get time range i.e. from - to
     * @return time range
     */
    public long[] getRange() {
        return range;
    }

    /**
     * Set time ragne i.e from - to
     * @param range time range
     */
    public void setRange(long[] range) {
        this.range = range;
    }

    /**
     * Time range constructor
     */
    public TimeRange() {
    }

    /**
     * Time range constructor
     * @param unit time unit
     * @param range time range
     */
    public TimeRange(String unit, long[] range) {
        this.unit = unit;
        this.range = range;
    }
}
