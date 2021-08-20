package com.edheijer.weatherclient.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edheijer.weatherclient.models.WeatherData;
import com.edheijer.weatherclient.services.WeatherDataService;

@RestController
public class WeatherController {

	@Autowired
	private WeatherDataService weatherDataService;
	
	@EventListener(ApplicationReadyEvent.class)
	@Scheduled(cron="${app.apiCallFrequencyInSeconds}")
	@GetMapping("weather-data")
	public ResponseEntity<WeatherData> getRepsonse() {
		WeatherData weatherData = weatherDataService.getWeatherData();
		return ResponseEntity.ok().body(weatherData);
	}
	
}
