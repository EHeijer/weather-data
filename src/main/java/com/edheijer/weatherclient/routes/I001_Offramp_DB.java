package com.edheijer.weatherclient.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class I001_Offramp_DB extends RouteBuilder {

    @Autowired
    I001_Process_CanonicalToDBFormat mapToDBFormat;

    @Override
    public void configure() throws Exception {

        from("seda:weatherDataCanonicalToDB")
                .log("Kolla här nu kör vi till DB!")
                .unmarshal().jaxb("com.edheijer.weatherclient.canonical")
                .process(mapToDBFormat);
    }
}
