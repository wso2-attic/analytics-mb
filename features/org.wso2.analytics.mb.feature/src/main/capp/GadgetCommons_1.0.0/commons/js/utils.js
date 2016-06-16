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

var CONTEXT = "/portal/apis/data";

function getQueryString() {
    var queryStringKeyValue = window.parent.location.search.replace('?', '').split('&');
    var qsJsonObject = {};
    if (queryStringKeyValue != '') {
        for (i = 0; i < queryStringKeyValue.length; i++) {
            qsJsonObject[queryStringKeyValue[i].split('=')[0]] = queryStringKeyValue[i].split('=')[1];
        }
    }
    return qsJsonObject;
}

function fetchData(params, callback, error) {
    console.log("++ Fetching data from: " + new Date(params.timeFrom) + " To: " + new Date(params.timeTo));
    $.ajax({
        url: "/portal/apis/mbanalytics" + "?type=" + params.type + "&timeFrom=" + params.timeFrom + "&timeTo=" + params.timeTo,
        type: "GET",
        success: function(data) {
            callback(data);
        },
        error: function(msg) {
            error(msg);
        }
    });
}
