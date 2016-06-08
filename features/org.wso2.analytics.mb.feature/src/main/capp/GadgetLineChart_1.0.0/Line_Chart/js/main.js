var views = [{
    id: "chart-0",
    schema: [{
        "metadata": {
            "names": ["time", "count"],
            "types": ["time", "linear"]
        }
    }],
    chartConfig: {
        x: "time",
        charts: [{
            type: "line",
            y: "count"
        }],
        padding: {
            "top": 20,
            "left": 80,
            "bottom": 20,
            "right": 80
        },
        range: false,
        height: 300
    },
    callbacks: [{
        type: "click",
        callback: function() {
            //wso2gadgets.load("chart-1");
            alert("Clicked on bar chart of chart-0. You can implement your own callback handler for this.");
        }
    }],
    subscriptions: [{
        topic: "range-selected",
        callback: function(topic, data, subscriberData) {
            //do some stuff
        }
    }],
    data: function() {
        var SERVER_URL = "/portal/apis/analytics";
        var client = new AnalyticsClient().init(null, null, SERVER_URL);
        var queryInfo = {
            tableName: "ORG_WSO2_METRICS_STREAM_METER", //table being queried
            searchParams: {
                query: "name: \"org.wso2.mb.message.receive\"", //lucene query to search the records
                start: 0, //starting index of the matching record set
                count: 100, //page size for pagination
                sortBy: [{
                    field: "meta_timestamp",
                    sortType: "ASC", // This can be ASC, DESC
                }]
            }
        };
        client.search(
            queryInfo,
            function(response) {
                var results = [];
                var data = JSON.parse(response.message);
                data.forEach(function(record, i) {
                    var values = record.values;
                    var result = [values["meta_timestamp"], values["count"]];
                    results.push(result);
                });
                //results.sort(function(a,b) {
                //    return a[2] - b[2];
                //});
                console.log(results);
                //Call the framework to draw the chart with received data.
                //Note that data should be in VizGrammar ready format
                wso2gadgets.onDataReady(results);
            },
            function(e) {
                //throw it to upper level
                onError(e);
            }
        );
    }
}];

$(function() {
    try {
        wso2gadgets.init("#canvas", views);
        var view = wso2gadgets.load("chart-0");
    } catch (e) {
        console.error(e);
    }

});