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
    var search = gadgetUtil.getGadgetConfig(prefs.getString(PARAM_SEARCH));
    var qs = gadgetUtil.getQueryString();

    $("#txtSearch").attr('placeholder', 'FILTER BY ' + search.placeholder + ' ...');

    if (qs[PARAM_ID] != null) {
        $("#txtSearch").val(qs[PARAM_ID]);
    }

    gadgetUtil.fetchData(CONTEXT, {
        type: search.type,
    }, onData, onError);

    function onData(response) {
        $('.typeahead').typeahead({
            hint: true,
            highlight: true,
            minLength: 0
        }, {
            name: 'proxyName',
            source: substringMatcher(response.message)
        }).on('typeahead:selected', function(evt, item) {
            var href = parent.window.location.href;
            if (qs[PARAM_ID]) {
                href = href.replace(/(id=)[^\&]+/, '$1' + item);
            } else {
                if (href.includes("?")) {
                    href = href + "&" + PARAM_ID + "=" + item;
                } else {
                    href = href + "?" + PARAM_ID + "=" + item;
                }
            }
            parent.window.location = href;
        }).on('typeahead:open', function(evt, item) {
            wso2.gadgets.controls.resizeGadget({
                height: "200px"
            });
        }).on('typeahead:close', function(evt, item) {
            wso2.gadgets.controls.restoreGadget();
        }).focus().blur();
    }

    function onError(error) {

    }

    var substringMatcher = function(strs) {
        return function findMatches(q, cb) {
            var matches, substringRegex;

            // an array that will be populated with substring matches
            matches = [];

            // regex used to determine if a string contains the substring `q`
            substrRegex = new RegExp(q, 'i');

            // iterate through the pool of strings and for any string that
            // contains the substring `q`, add it to the `matches` array
            $.each(strs, function(i, str) {
                if (substrRegex.test(str)) {
                    matches.push(str);
                }
            });

            cb(matches);
        };
    };
});