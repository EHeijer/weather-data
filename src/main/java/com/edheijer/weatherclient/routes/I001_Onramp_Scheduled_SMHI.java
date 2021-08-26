package com.edheijer.weatherclient.routes;

import com.edheijer.weatherclient.models.smhi.SMHIResponse;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class I001_Onramp_Scheduled_SMHI extends RouteBuilder {

    @Autowired
    private I001_Process_SMHIPollToCanonical combineSmhiResponsesToCanonicalWeatherData;

    @Override
    public void configure() throws Exception {

        from("timer://foo?fixedRate=true&period=10000")
                .log("START - I001 - SMHI Poll Timer")
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                .log("Getting temperature")
                // TEMPERATURE
                .to("https://opendata-download-metobs.smhi.se/api/version/1.0/parameter/{{app.temp}}/station/{{app.stationId}}/period/latest-hour/data.json")
                .convertBodyTo(String.class)
                .to("log:i001.weatherclient.smhi.timer?showBody=true&showHeaders=true")
                .unmarshal().json(JsonLibrary.Jackson, SMHIResponse.class)
                .setProperty("temp_response", body())
                // DIRECTION
                .to("https://opendata-download-metobs.smhi.se/api/version/1.0/parameter/{{app.direction}}/station/{{app.stationId}}/period/latest-hour/data.json")
                .convertBodyTo(String.class)
                .to("log:i001.weatherclient.smhi.timer?showBody=true&showHeaders=true")
                .unmarshal().json(JsonLibrary.Jackson, SMHIResponse.class)
                .setProperty("direction_response", body())
                // SPEED
                .to("https://opendata-download-metobs.smhi.se/api/version/1.0/parameter/{{app.speed}}/station/{{app.stationId}}/period/latest-hour/data.json")
                .convertBodyTo(String.class)
                .to("log:i001.weatherclient.smhi.timer?showBody=true&showHeaders=true")
                .unmarshal().json(JsonLibrary.Jackson, SMHIResponse.class)
                .setProperty("speed_response", body())
                // Combine all responses into canonical format
                .process(combineSmhiResponsesToCanonicalWeatherData)
                .marshal().jaxb()
                .to("log:i001.weatherclient.smhi.timer.mapresult?showBody=true&showHeaders=false")
                .multicast().parallelProcessing().to("seda:weatherDataCanonicalToXML", "seda:weatherDataCanonicalToDB")
                .log("END - I001")
                .routeId("i001.weatherclient.smhi.timer");
    }

}
