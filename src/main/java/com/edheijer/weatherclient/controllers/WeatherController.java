package com.edheijer.weatherclient.controllers;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.edheijer.weatherclient.models.WeatherData;
import com.edheijer.weatherclient.services.WeatherDataService;

/**
* This controller class mostly works as a bridge to the service layer. It also serve data to the template engine
* @author Edvard Heijer
* 
*/

@Controller
@RequestMapping
public class WeatherController {

	@Autowired
	private WeatherDataService weatherDataService;
	
	@GetMapping("/weather")
	public String getRepsonse(Model model) {
		model.addAttribute("weatherData", weatherDataService.handleWeatherData());
		return "weather";
	}
	
	@PostMapping("/weather")
	public String saveDataToTxtFile() {
		weatherDataService.createOrAppendTxtFileFromXmlData();
		return "redirect:/weather";
	}
	
	@PostMapping("/weather/update")
	public String updateWeatherData() {
		WeatherData data = weatherDataService.handleWeatherData();
		return "redirect:/weather";
	}
	
	/*This method just call getResponse method with a frequency
	 * that is decided in application.properies
	 */
	@Scheduled(cron="${app.apiCallFrequency}")
	public void CallGetRepsonse() {
		Model model = new Model() {
			@Override
			public Model mergeAttributes(Map<String, ?> attributes) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object getAttribute(String attributeName) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean containsAttribute(String attributeName) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public Map<String, Object> asMap() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Model addAttribute(String attributeName, Object attributeValue) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Model addAttribute(Object attributeValue) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Model addAllAttributes(Map<String, ?> attributes) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Model addAllAttributes(Collection<?> attributeValues) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		getRepsonse(model);
	}
	
	@GetMapping("")
	public String viewHomePage() {
		return "redirect:/weather";
	}
	
}
