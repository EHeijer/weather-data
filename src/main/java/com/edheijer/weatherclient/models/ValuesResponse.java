package com.edheijer.weatherclient.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValuesResponse {
	
	@JsonProperty("value")
	@JsonDeserialize(using = CustomStringDeserializer.class)
	private List<String> value;
	
	@JsonDeserialize(using = CustomStringDeserializer.class)
	@JsonProperty("parameter")
	private List<String> parameter;
	
	@JsonDeserialize(using = CustomStringDeserializer.class)
	@JsonProperty("station")
	private List<String> station;

	public List<String> getValues() {
		return value;
	}

	public void setValues(List<String> values) {
		this.value = values;
	}

	public List<String> getParameter() {
		return parameter;
	}

	public void setParameter(List<String> parameter) {
		this.parameter = parameter;
	}
	
	public List<String> getStation() {
		return station;
	}

	public void setStation(List<String> station) {
		this.station = station;
	}

	@Override
	public String toString() {
		return "ValuesResponse [value=" + value + ", parameter=" + parameter + ", station=" + station + "]";
	}

	

}




