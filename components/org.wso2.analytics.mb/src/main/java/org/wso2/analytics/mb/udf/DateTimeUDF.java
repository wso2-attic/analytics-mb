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

package org.wso2.analytics.mb.udf;

import org.wso2.carbon.analytics.spark.core.udf.CarbonUDF;

import java.text.ParseException;
import java.util.Calendar;

/**
 * This class supports Spark SQL UDF which has custom UDF of DateTime
 */
public class DateTimeUDF implements CarbonUDF {

    private final Calendar cal = Calendar.getInstance();

    /**
     * Get year from given timestamp
     *
     * @param timeStamp long timestamp
     * @return year of given timestamp
     * @throws ParseException
     */
    public Integer getYear(Long timeStamp) throws ParseException {
        synchronized (cal) {
            cal.setTimeInMillis(timeStamp);
            return cal.get(Calendar.YEAR);
        }
    }

    /**
     * Get month from given timestamp
     *
     * @param timeStamp long timestamp
     * @return month of given timestamp
     * @throws ParseException
     */
    public Integer getMonth(Long timeStamp) throws ParseException {
        synchronized (cal) {
            cal.setTimeInMillis(timeStamp);
            return cal.get(Calendar.MONTH) + 1;
        }
    }

    /**
     * Get day from given timestamp
     *
     * @param timeStamp long timestamp
     * @return day of given timestamp
     * @throws ParseException
     */
    public Integer getDay(Long timeStamp) throws ParseException {
        synchronized (cal) {
            cal.setTimeInMillis(timeStamp);
            return cal.get(Calendar.DAY_OF_MONTH);
        }
    }

    /**
     * Get hour from given timestamp
     *
     * @param timeStamp long timestamp
     * @return hour of given timestamp
     * @throws ParseException
     */
    public Integer getHour(Long timeStamp) throws ParseException {
        synchronized (cal) {
            cal.setTimeInMillis(timeStamp);
            return cal.get(Calendar.HOUR_OF_DAY);
        }
    }

    /**
     * Get minute from given timestamp
     *
     * @param timeStamp long timestamp
     * @return minute of given timestamp
     * @throws ParseException
     */
    public Integer getMinute(Long timeStamp) throws ParseException {
        synchronized (cal) {
            cal.setTimeInMillis(timeStamp);
            return cal.get(Calendar.MINUTE);
        }
    }

    /**
     * Get seconds from given timestamp
     *
     * @param timeStamp long timestamp
     * @return second of given timestamp
     * @throws ParseException
     */
    public Integer getSeconds(Long timeStamp) throws ParseException {
        synchronized (cal) {
            cal.setTimeInMillis(timeStamp);
            return cal.get(Calendar.SECOND);
        }
    }

    /**
     * Get start time to month from given year and month
     *
     * @param year year
     * @param month month
     * @return starting time to month
     */
    public String getMonthStartingTime(Integer year, Integer month) {
        synchronized (cal) {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return String.valueOf(cal.getTimeInMillis());
        }
    }

    /**
     * Get start time to date from given year, month and date
     *
     * @param year year
     * @param month month
     * @param date date
     * @return start time to date
     */
    public String getDateStartingTime(Integer year, Integer month, Integer date) {
        synchronized (cal) {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.DAY_OF_MONTH, date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return String.valueOf(cal.getTimeInMillis());
        }
    }

    /**
     * Get start time to hour from given year, month, date and hour
     *
     * @param year year
     * @param month month
     * @param date date
     * @param hour hour
     * @return start time to hour
     */
    public String getHourStartingTime(Integer year, Integer month, Integer date, Integer hour) {
        synchronized (cal) {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.DAY_OF_MONTH, date);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return String.valueOf(cal.getTimeInMillis());
        }
    }

    /**
     * Get start time to minute from given year, month, date, hour and minute
     *
     * @param year year
     * @param month month
     * @param date date
     * @param hour hour
     * @param minute minute
     * @return start time to minute
     */
    public String getMinuteStartingTime(Integer year, Integer month, Integer date, Integer hour, Integer minute) {
        synchronized (cal) {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.DAY_OF_MONTH, date);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return String.valueOf(cal.getTimeInMillis());
        }
    }

    /**
     * Get start time to second from given year, month, date, hour, minute and second
     *
     * @param year year
     * @param month month
     * @param date date
     * @param hour hour
     * @param minute minute
     * @param second second
     * @return start time to second
     */
    public String getSecondStartingTime(Integer year, Integer month, Integer date, Integer hour, Integer minute,
                                        Integer second) {
        synchronized (cal) {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.DAY_OF_MONTH, date);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, second);
            cal.set(Calendar.MILLISECOND, 0);
            return String.valueOf(cal.getTimeInMillis());
        }
    }
}
