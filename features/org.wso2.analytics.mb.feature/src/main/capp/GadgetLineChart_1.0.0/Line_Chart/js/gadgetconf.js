/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var charts = [{
    name: ROLE_RATE,
    schema: [{
        "metadata": {
            "names": ["Time", "Status", "Rate (msg/sec)"],
            "types": ["time", "ordinal", "linear"]
        },
        "data": []
    }],
    chartConfig: {
        x: "Time",
        charts: [{ type: "line", y: "Rate (msg/sec)", color: "Status" }],
        padding: { "top": 30, "left": 60, "bottom": 60, "right": 110 },
        range: false
    },
    types: [
        { name: TYPE_MSG_RECEIVE_RATE, type: 1 },
        { name: TYPE_MSG_SEND_RATE, type: 2 },
        { name: TYPE_DB_READ_RATE, type: 8 },
        { name: TYPE_DB_WRITE_RATE, type: 9 }
    ],
    processData: function(data) {
        var result = [];
        data.forEach(function(row, i) {
            var timestamp = row['timestamp'];
            var meanRate = row["meanRate"];
            var oneMinuteRate = row["oneMinuteRate"];
            var fiveMinuteRate = row["fiveMinuteRate"];
            var fifteenMinuteRate = row["fifteenMinuteRate"];
            //add result to visualize
            result.push([timestamp, "Mean", meanRate]);
            result.push([timestamp, "Last 1 Minute", oneMinuteRate]);
            result.push([timestamp, "Last 5 Minute", fiveMinuteRate]);
            result.push([timestamp, "Last 15 Minute", fifteenMinuteRate]);
        });
        return result;
    }
}, {
    name: ROLE_COUNT,
    schema: [{
        "metadata": {
            "names": ["Time", "Count"],
            "types": ["time", "linear"]
        },
        "data": []
    }],
    chartConfig: {
        x: "Time",
        charts: [{ type: "line", y: "Count"}],
        padding: { "top": 30, "left": 60, "bottom": 60, "right": 110 },
        range: false
    },
    types: [
        { name: TYPE_TOTAL_CHANNEL_COUNT, type: 3 },
        { name: TYPE_TOTAL_QUEUE_SUB_COUNT, type: 4 },
        { name: TYPE_TOTAL_TOPIC_SUB_COUNT, type: 5 }
    ],
    processData: function(data) {
        var result = [];
        data.forEach(function(row, i) {
            var timestamp = row['timestamp'];
            var count = row["count"];
            //add result to visualize
            result.push([timestamp, count]);
        });
        return result;
    }
}, {
    name: ROLE_TIME,
    schema: [{
        "metadata": {
        "names": ["Time", "Status", "Elapse (ms)"],
        "types": ["time", "ordinal", "linear"]
        },
          "data": []
    }],
    chartConfig: {
        x: "Time",
        charts: [{ type: "line", y: "Elapse (ms)", color: "Status"}],
        padding: { "top": 30, "left": 60, "bottom": 60, "right": 110 },
        range: false
    },
    types: [
        { name: TYPE_DB_READ_TIME, type: 6 },
        { name: TYPE_DB_WRITE_TIME, type: 7 }
    ],
    processData: function(data) {
        var result = [];
        data.forEach(function(row, i) {
            var timestamp = row['timestamp'];
            var max = row["max"];
            var mean = row["mean"];
            var min = row["min"];
            var stddev = row["stddev"];
            var median = row["median"];
            var percentile75 = row["percentile75"];
            var percentile95 = row["percentile95"];
            var percentile98 = row["percentile98"];
            var percentile99 = row["percentile99"];
            var percentile999 = row["percentile999"];
            //add result to visualize
            result.push([timestamp, "Max", max]);
            result.push([timestamp, "Mean", mean]);
            result.push([timestamp, "Min", min]);
            result.push([timestamp, "Std Deviation", stddev]);
            result.push([timestamp, "Median", median]);
            result.push([timestamp, "75 Percentile", percentile75]);
            result.push([timestamp, "95 Percentile", percentile95]);
            result.push([timestamp, "98 Percentile", percentile98]);
            result.push([timestamp, "99 Percentile", percentile99]);
            result.push([timestamp, "999 Percentile", percentile999]);
        });
        return result;
    }
}];