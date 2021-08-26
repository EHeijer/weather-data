package com.edheijer.weatherclient.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class I001_Offramp_XMLFile extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        from("seda:weatherDataCanonicalToXML")
                .log("Kolla här nu kör vi till XMLfilen")
                .to("file:xmlOutput?fileName=weatherData-${date:now:yyyyMMddHHmmss}.xml");
    }
}
