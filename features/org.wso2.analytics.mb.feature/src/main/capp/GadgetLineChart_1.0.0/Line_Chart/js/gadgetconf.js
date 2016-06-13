var charts = [{
    name: ROLE_RATE,
    schema: [{
        "metadata": {
            "names": ["Time", "Status", "Rate"],
            "types": ["time", "ordinal", "linear"]
        },
        "data": []
    }],
    chartConfig: {
        x: "Time",
        charts: [{ type: "line", y: "Rate", color: "Status" }],
        padding: { "top": 30, "left": 60, "bottom": 60, "right": 110 },
        range: false
    },
    types: [
        { name: TYPE_MSG_RECEIVE, type: 1 },
        { name: TYPE_MSG_SEND, type: 2 }
    ],
    processData: function(data) {
        var result = [];
        data.forEach(function(row, i) {
            var timestamp = row['timestamp'];
            var meanRate = row["meanRate"];
            var oneMinuteRate = row["oneMinuteRate"];
            var fiveMinuteRate = row["fiveMinuteRate"];
            var fifteenMinuteRate = row["fifteenMinuteRate"];

            result.push([timestamp, "Mean", meanRate]);
            result.push([timestamp, "Last 1 Minute", oneMinuteRate]);
            result.push([timestamp, "Last 5 Minute", fiveMinuteRate]);
            result.push([timestamp, "Last 15 Minute", fifteenMinuteRate]);
        });
        return result;
    }
}];