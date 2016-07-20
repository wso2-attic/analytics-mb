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

var TOPIC = "subscriber";
var page = gadgetUtil.getCurrentPageName();
var qs = gadgetUtil.getQueryString();
var type = 9;

$(function() {

    if (page != PAGE_LANDING && qs[PARAM_ID] == null) {
        $("body").html(gadgetUtil.getDefaultText());
        return;
    }
    else {
        $('#stats').show();
        if(qs[PARAM_ID]) {
            $("#title").html('for ' + qs[PARAM_ID]);
        }
    }
    var timeFrom = gadgetUtil.timeFrom();
    var timeTo = gadgetUtil.timeTo();
    console.log("STATS_CHART[" + page + "]: TimeFrom: " + timeFrom + " TimeTo: " + timeTo);

    gadgetUtil.fetchData(CONTEXT, {
        type: type,
        timeFrom: timeFrom,
        timeTo: timeTo
    }, onData, onError);
});

gadgets.HubSettings.onConnect = function() {
    gadgets.Hub.subscribe(TOPIC, function(topic, data, subscriberData) {
        onTimeRangeChanged(data);
    });
};

function onTimeRangeChanged(data) {
    gadgetUtil.fetchData(CONTEXT, {
        type: type,
        id: qs.id,
        timeFrom: data.timeFrom,
        timeTo: data.timeTo,
        entryPoint: qs.entryPoint
    }, onData, onError);
};

function onData(response) {
    try {
        var data = response.message;
        var enqueue = data.enqueue;
        var dequeue = data.dequeue;
        var acknowledge = data.acknowledge;
        var reject = data.reject;
        if( (!enqueue) || (enqueue < 1) ) {
            $('#gadget-messge').html(gadgetUtil.getEmptyRecordsText());
            $('#gadget-messge').show();
            $('#stats').hide();
            return;
        } else{
            $('#gadget-messge').hide();
            $('#stats').show();
        }

        var success = acknowledge;
        var failedPct = (reject / dequeue) * 100;
        var successPct = 100 - failedPct;

        $("#enqueue").html(Number(enqueue).toLocaleString('en'));
        $("#dequeue").html(Number(dequeue).toLocaleString('en'));
        $("#failedCount").html(Number(reject).toLocaleString('en'));
        $("#failedPercent").html(parseFloat(failedPct).toFixed(2));
        $("#successCount").html(Number(success).toLocaleString('en'));
        $("#successPercent").html(parseFloat(successPct).toFixed(2));

        var successColor = function(){
            return parseFloat(successPct) > 0 ? '#5CB85C' : '#353B48';
        };

        var failColor = function(){
            return parseFloat(failedPct) > 0 ? '#D9534F' : '#353B48';
        };

        //draw donuts
        var dataT = [{
            "metadata": {
                "names": ["rpm", "torque", "horsepower", "EngineType"],
                "types": ["linear", "linear", "ordinal", "ordinal"]
            },
            "data": [
                [0, parseFloat(successPct), 12, "YES"],
                [0, parseFloat(failedPct), 12, "NO"]
            ]
        }];

        var dataF = [{
            "metadata": {
                "names": ["rpm", "torque", "horsepower", "EngineType"],
                "types": ["linear", "linear", "ordinal", "ordinal"]
            },
            "data": [
                [0, parseFloat(failedPct), 12, "YES"],
                [0, parseFloat(successPct), 12, "NO"]
            ]
        }];

        var configT = {
            charts: [{ type: "arc", x: "torque", color: "EngineType" }],
            innerRadius: 0.3,
            tooltip: { "enabled": false },
            padding: { top:0, right:0, bottom:0, left:0 },
            legend: false,
            percentage: true,
            colorScale: [successColor(), "#353B48"],
            width: 220,
            height: 220
        }

        var configF = {
            charts: [{ type: "arc", x: "torque", color: "EngineType" }],
            innerRadius: 0.3,
            tooltip: { "enabled": false },
            padding: { top:0, right:0, bottom:0, left:0 },
            legend: false,
            percentage: true,
            colorScale: [failColor(), "#353B48"],
            width: 220,
            height: 220
        }
        var chartT = new vizg(dataT, configT);
        chartT.draw("#dChartTrue");

        var chartF = new vizg(dataF, configF);
        chartF.draw("#dChartFalse");

    } catch (e) {
        $("#canvas").html(gadgetUtil.getErrorText(e));
    }
};

function onError(msg) {
    $("#canvas").html(gadgetUtil.getErrorText(msg));
}
