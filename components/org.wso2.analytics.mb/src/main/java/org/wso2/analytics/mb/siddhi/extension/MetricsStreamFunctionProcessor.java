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

package org.wso2.analytics.mb.siddhi.extension;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.analytics.mb.udf.DateTimeUDF;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * This extension take name field from metrics stream and add more attributes extracting value
 */
public class MetricsStreamFunctionProcessor extends StreamFunctionProcessor {

    private static final Log log = LogFactory.getLog(MetricsStreamFunctionProcessor.class);
    DateTimeUDF dateTimeUDF = new DateTimeUDF();

    /**
     * The process method used when more than one function parameters are provided
     *
     * @param data the data values for the function parameters
     * @return the data for additional output attributes introduced by the function
     */
    @Override
    protected Object[] process(Object[] data) {
        Long timestamp = (Long) data[0];
        String name = (String) data[1];
        int year, month, day, hour, minute;
        String type = "queue";
        String destination = "myQueue";
        String tenantDomain = "carbon.super";
        if (destination.contains("/")) {
            tenantDomain = destination.substring(0, destination.indexOf("/"));
        }
        try {
            year = dateTimeUDF.getYear(timestamp);
            month = dateTimeUDF.getMonth(timestamp);
            day = dateTimeUDF.getDay(timestamp);
            hour = dateTimeUDF.getHour(timestamp);
            minute = dateTimeUDF.getMinute(timestamp);
        } catch (ParseException e) {
            throw new ExecutionPlanCreationException("Error occurred while parsing timestamp");
        }
        return new Object[]{tenantDomain, year, month, day, hour, minute, type, destination};
    }

    /**
     * The process method used when zero or one function parameter is provided
     *
     * @param data null if the function parameter count is zero or runtime data value of the function parameter
     * @return the data for additional output attribute introduced by the function
     */
    @Override
    protected Object[] process(Object data) {
        return new Object[0];
    }

    /**
     * The init method will be called before other methods
     *
     * @param inputDefinition              the incoming stream definition
     * @param attributeExpressionExecutors the executors of each function parameters
     * @param executionPlanContext         the context of the execution plan
     * @return the additional output attributes introduced by the function
     */
    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors,
                                   ExecutionPlanContext executionPlanContext) {
        if(attributeExpressionExecutors.length < 2) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to formatStream() function, " +
                    "required 2: meta_timestamp and name, but found " + attributeExpressionExecutors.length);
        }
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("tenantDomain", Attribute.Type.STRING));
        attributes.add(new Attribute("year", Attribute.Type.INT));
        attributes.add(new Attribute("month", Attribute.Type.INT));
        attributes.add(new Attribute("day", Attribute.Type.INT));
        attributes.add(new Attribute("hour", Attribute.Type.INT));
        attributes.add(new Attribute("minute", Attribute.Type.INT));
        attributes.add(new Attribute("type", Attribute.Type.STRING));
        attributes.add(new Attribute("destination", Attribute.Type.STRING));
        return attributes;
    }

    /**
     * This will be called only once and this can be used to acquire required resources for the processing element.
     * This will be called after initializing the system and before starting to process the events.
     */
    @Override
    public void start() {
    }

    /**
     * This will be called only once and this can be used to release the acquired resources for processing.
     * This will be called before shutting down the system.
     */
    @Override
    public void stop() {
    }

    /**
     * Used to collect the serializable state of the processing element, that need to be
     * persisted for the reconstructing the element to the same state on a different point of time
     *
     * @return stateful objects of the processing element as an array
     */
    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    /**
     * Used to restore serialized state of the processing element, for reconstructing
     * the element to the same state as if was on a previous point of time.
     *
     * @param state the stateful objects of the element as an array on the same order provided by currentState().
     */
    @Override
    public void restoreState(Object[] state) {
    }
}
