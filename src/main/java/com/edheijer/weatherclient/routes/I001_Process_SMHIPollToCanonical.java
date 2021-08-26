package com.edheijer.weatherclient.routes;

import com.edheijer.weatherclient.canonical.WeatherData;
import com.edheijer.weatherclient.models.smhi.SMHIResponse;
import com.edheijer.weatherclient.util.DateTimeUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Component
public class I001_Process_SMHIPollToCanonical implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        SMHIResponse tempJson = exchange.getProperty("temp_response", SMHIResponse.class);
        SMHIResponse directionJson = exchange.getProperty("direction_response", SMHIResponse.class);
        SMHIResponse speedJson = exchange.getProperty("speed_response", SMHIResponse.class);


        WeatherData weatherData = new WeatherData();
        WeatherData.Reading reading = new WeatherData.Reading();

        reading.setStationId(new BigInteger(tempJson.getStation().getKey()));
        reading.setStationName(tempJson.getStation().getName());
        LocalDateTime updatedTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(tempJson.getUpdated()), TimeZone.getDefault().toZoneId());
        reading.setTimestamp(DateTimeUtils.asXmlGreGorianCalendar(updatedTime));

        WeatherData.Reading.Parameter tempParam = new WeatherData.Reading.Parameter();
        tempParam.setName(tempJson.getParameter().getName());
        tempParam.setValue(tempJson.getValue().get(0).getValue());

        reading.getParameter().add(tempParam);

        WeatherData.Reading.Parameter windParam = new WeatherData.Reading.Parameter();
        windParam.setName(directionJson.getParameter().getName());
        windParam.setValue(directionJson.getValue().get(0).getValue());
        reading.getParameter().add(windParam);

        WeatherData.Reading.Parameter speedParam = new WeatherData.Reading.Parameter();
        speedParam.setName(speedJson.getParameter().getName());
        speedParam.setValue(speedJson.getValue().get(0).getValue());
        reading.getParameter().add(speedParam);

        weatherData.getReading().add(reading);

        exchange.getIn().setBody(weatherData);
    }
}
