package com.edheijer.weatherclient.models;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class Parameter {
	
	@Enumerated(EnumType.STRING)
	private ParameterType name;
	
	private String value;
	
	public Parameter(ParameterType name, String value) {
		this.name = name;
		this.value = value;
	}

	public ParameterType getName() {
		return name;
	}

	public void setName(ParameterType name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Parameter [name=" + name + ", value=" + value + "]";
	}
	
	
}

