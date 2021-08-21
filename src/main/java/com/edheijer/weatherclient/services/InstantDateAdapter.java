package com.edheijer.weatherclient.services;

import java.time.Instant;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class InstantDateAdapter extends XmlAdapter<String, Instant>{

	@Override
	public Instant unmarshal(String v) throws Exception {
		return Instant.parse(v);
	}

	@Override
	public String marshal(Instant v) throws Exception {
		return v.toString();
	}

}
