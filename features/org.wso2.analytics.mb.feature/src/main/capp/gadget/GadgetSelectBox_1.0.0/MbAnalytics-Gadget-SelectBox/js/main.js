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
var href = parent.window.location.href,
    hrefLastSegment = href.substr(href.lastIndexOf('/') + 1),
    resolveURI = parent.ues.global.dashboard.id == hrefLastSegment ? '../' : '../../';

var prefs = new gadgets.Prefs();

$(function() {
    var select = gadgetUtil.getGadgetConfig(prefs.getString(PARAM_SELECT));
    var qs = gadgetUtil.getQueryString();

    gadgetUtil.fetchData(CONTEXT, {
        type: select.type,
    }, onData, onError);

    function onData(response) {

        var items = [];
        items.push("<option value='ALL'>ALL</option>");
        $.each(response.message, function(i, el) {
            items.push("<option value='" + el + "'>" + el + "</option>");
        });

        $("<select/>", {
            "id": "selectBox",
            "class": "form-control",
            html: items.join("")
        }).appendTo("#container");

        if (qs[PARAM_ID] != null) {
            $("#selectBox option[value='" + qs[PARAM_ID] + "']").prop('selected', true);
        } else {
            $("#selectBox option[value='ALL']").prop('selected', true);
        }

        $('#selectBox').on('change', function() {
            var href = parent.window.location.href;
            if (qs[PARAM_ID]) {
                href = href.replace(/(id=)[^\&]+/, '$1' + this.value);
            } else {
                if (href.includes("?")) {
                    href = href + "&" + PARAM_ID + "=" + this.value;
                } else {
                    href = href + "?" + PARAM_ID + "=" + this.value;
                }
            }
            parent.window.location = href;
        });
    }

    function onError(error) {
        $("#container").html(gadgetUtil.getErrorText(error));
    }

});