package com.edheijer.weatherclient.models;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "weather_data")
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
@XmlType(namespace = "https://www.example.org/weather-data", propOrder={"station_id", "stationName", "timestamp", "parameter"})
public class WeatherData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String station_id;
	
	@JsonProperty("date")
	@XmlJavaTypeAdapter(value = InstantDateAdapter.class)
	public Instant timestamp;
	
	private String temp;
	
	private String speed;
	
	private String direction;
	
	@JsonInclude()
	@Transient
	@XmlElement(name = "Parameter")
	private List<Parameter> parameter;
	
	@JsonInclude()
	@Transient
	@XmlElement(name = "StationName")
	private String stationName;
	
	public WeatherData() {
		
	}
	
	public WeatherData(String station_id, Instant timestamp, String temp, String direction, String speed, List<Parameter> parameters, String stationName) {
		this.station_id = station_id;
		this.timestamp = timestamp; 
		this.temp = temp;
		this.direction = direction;
		this.speed = speed;
		this.parameter = parameters;
		this.stationName = stationName;
	}
	@XmlAttribute
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getStation_id() {
		return station_id;
	}
	public void setStation_id(String station_id) {
		this.station_id = station_id;
	}
	
	@XmlTransient
	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	@XmlTransient
	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	@XmlTransient
	public String getDirection() {
		return direction;
	}

	public Instant getTimestamp2() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public String getStationName2() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}
	
	@XmlTransient
	public List<Parameter> getParameter() {
		return parameter;
	}

	public void setParameter(List<Parameter> parameter) {
		this.parameter = parameter;
	}

	
	
	@Override
	public String toString() {
		return "WeatherData [id=" + id + ", station_id=" + station_id + ", timestamp=" + timestamp + ", temp=" + temp
				+ ", speed=" + speed + ", direction=" + direction + ", parameter=" + parameter + ", stationName="
				+ stationName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WeatherData other = (WeatherData) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}
