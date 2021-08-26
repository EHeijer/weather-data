package com.edheijer.weatherclient.routes;

import com.edheijer.weatherclient.canonical.WeatherData;
import com.edheijer.weatherclient.models.WeatherDataDB;
import com.edheijer.weatherclient.models.smhi.SMHIResponse;
import com.edheijer.weatherclient.repositories.WeatherDataDBRepository;
import com.edheijer.weatherclient.repositories.WeatherDataRepository;
import com.edheijer.weatherclient.util.DateTimeUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Component
public class I001_Process_CanonicalToDBFormat implements Processor {

    @Autowired
    private WeatherDataDBRepository weatherDataRepository;

    @Override
    public void process(Exchange exchange) throws Exception {
        WeatherData canonicalWeatherData = exchange.getIn().getBody(WeatherData.class);
        WeatherData.Reading reading = canonicalWeatherData.getReading().get(0);

        WeatherDataDB dbFOrmat = new WeatherDataDB();
        dbFOrmat.setDirection("123");
        dbFOrmat.setTemp("99");
        dbFOrmat.setSpeed("456");
        dbFOrmat.setStation_id(reading.getStationId() + "");
        dbFOrmat.setTimestamp(Instant.now());

        weatherDataRepository.save(dbFOrmat);

    }
}
