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
            "names": ["Time", "Rate / Second"],
            "types": ["time", "linear"]
        },
        "data": []
    }],
    chartConfig: {
        x: "Time",
        charts: [{ type: "line", y: "Rate / Second"}],
        padding: { "top": 30, "left": 60, "bottom": 60, "right": 110 },
        range: false
    },
    types: [
        { name: TYPE_MSG_RECEIVE_RATE, type: 1 },
        { name: TYPE_MSG_SEND_RATE, type: 2 },
        { name: TYPE_DB_READ_RATE, type: 6 },
        { name: TYPE_DB_WRITE_RATE, type: 7 },
        { name: TYPE_MSG_ACK_RATE, type: 9 },
        { name: TYPE_MSG_REJECT_RATE, type: 10 }
    ],
    processData: function(data) {
        var result = [];
        data.forEach(function(row, i) {
            var timestamp = row['timestamp'];
            var oneMinuteRate = parseFloat(row["oneMinuteRate"]);
            //add result to visualize
            result.push([timestamp, oneMinuteRate]);
        });
        return result;
    }
}, {
    name: ROLE_TIME,
    schema: [{
        "metadata": {
        "names": ["Time", "Type", "Duration / Millisecond"],
        "types": ["time", "ordinal", "linear"]
        },
          "data": []
    }],
    chartConfig: {
        x: "Time",
        charts: [{ type: "line", y: "Duration / Millisecond", color: "Type"}],
        padding: { "top": 30, "left": 60, "bottom": 60, "right": 110 },
        range: false
    },
    types: [
        { name: TYPE_DB_READ_TIME, type: 4 },
        { name: TYPE_DB_WRITE_TIME, type: 5 }
    ],
    processData: function(data) {
        var result = [];
        data.forEach(function(row, i) {
            var timestamp = row['timestamp'];
            var max = parseFloat(row["max"]);
            var mean = parseFloat(row["mean"]);
            //add result to visualize
            result.push([timestamp, "Max", max]);
            result.push([timestamp, "Mean", mean]);
        });
        return result;
    }
}];