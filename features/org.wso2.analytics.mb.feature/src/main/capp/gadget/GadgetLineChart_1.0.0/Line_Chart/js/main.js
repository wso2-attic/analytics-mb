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
var PUBLISHER_TOPIC = "chart-zoomed";
var page = gadgetUtil.getCurrentPageName();
var qs = gadgetUtil.getQueryString();
var prefs = new gadgets.Prefs();
var type;
var view = prefs.getString(PARAM_GADGET_VIEW);
var chart = gadgetUtil.getChart(prefs.getString(PARAM_GADGET_ROLE));
var rangeStart;
var rangeEnd;

if (chart) {
    type = gadgetUtil.getRequestType(view, chart);
}

$(function() {
    if (!chart || !view) {
        var message = {
            status: "Gadget initialization failed",
            statusText: "Gadget role and Gadget view must be provided"
        };
        $("#canvas").html(gadgetUtil.getErrorText(message));
        return;
    }
    if (page != PAGE_LANDING && qs[PARAM_ID] == null) {
        $("#canvas").html(gadgetUtil.getDefaultText());
        return;
    }
    var timeFrom = gadgetUtil.timeFrom();
    var timeTo = gadgetUtil.timeTo();
    console.log("LINE_CHART[" + view + "]: TimeFrom: " + timeFrom + " TimeTo: " + timeTo);
    gadgetUtil.fetchData(CONTEXT, {
        type: type,
        id: qs.id,
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
    }, onData, onError);
};

function onData(response) {
    try {
        var data = response.message;
        if (data.length == 0) {
            $('#canvas').html(gadgetUtil.getEmptyRecordsText());
            return;
        }
        //perform necessary transformation on input data
        chart.schema[0].data = chart.processData(data);
        //finally draw the chart on the given canvas
        chart.chartConfig.width = $('body').width();
        chart.chartConfig.height = $('body').height();

        var vg = new vizg(chart.schema, chart.chartConfig);
        $("#canvas").empty();
        vg.draw("#canvas",[{type:"range", callback:onRangeSelected}]);
    } catch (e) {
        $('#canvas').html(gadgetUtil.getErrorText(e));
    }
};

function onError(msg) {
    $("#canvas").html(gadgetUtil.getErrorText(msg));
};

document.body.onmouseup = function() {
    if((rangeStart) && (rangeEnd) && (rangeStart.toString() !== rangeEnd.toString())){
        var message = {
            timeFrom: new Date(rangeStart).getTime(),
            timeTo: new Date(rangeEnd).getTime(),
            timeUnit: "Custom"
        };
        gadgets.Hub.publish(PUBLISHER_TOPIC, message);
    }
}

var onRangeSelected = function(start, end) {
    rangeStart = start;
    rangeEnd = end;
};