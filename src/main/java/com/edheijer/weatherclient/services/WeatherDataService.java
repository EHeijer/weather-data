package com.edheijer.weatherclient.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.edheijer.weatherclient.models.Parameter;
import com.edheijer.weatherclient.models.ParameterType;
import com.edheijer.weatherclient.models.Response;
import com.edheijer.weatherclient.models.WeatherData;
import com.edheijer.weatherclient.repositories.WeatherDataRepository;

import net.minidev.json.JSONArray;

/**
* This service class do all the business logic in the application. It calls the api, creates xml-files and save data to database.
* @author Edvard Heijer
* 
*/

@Service
public class WeatherDataService {

	private final String startApiUrl = "https://opendata-download-metobs.smhi.se/api/version/1.0/parameter/";

	@Value("${app.stationId}")
	private String stationId;

	@Value("${app.temp}")
	private String temp;

	@Value("${app.direction}")
	private String direction;

	@Value("${app.speed}")
	private String speed;
	
	@Value("${app.xmlFileString}")
	private String xmlFileString;

	private Instant timestamp;
	
	private Parameter parameterInstance;
	
	List<Parameter> parameterList = new ArrayList<>();
	
	private String stationName;
	

	@Autowired
	private WeatherDataRepository weatherDataRepository;
	
	
	/**
	 * Handle all work with weather data. Mostly used to call other methods. Then returns WeatherData to controller  
	 */
	public WeatherData handleWeatherData() {
		// Can't declare this variable with "stationId" on class level, because in that case, "stationId" is null.
		String endApiUrl = "/station/" + stationId + "/period/latest-hour/data.json";
		
		//Get response for temp, direction and speed
		String getTemp = getResponse(startApiUrl + temp + endApiUrl, temp);
		String getDirection = getResponse(startApiUrl + direction + endApiUrl, direction);
		String getSpeed = getResponse(startApiUrl + speed + endApiUrl, speed);
		
		//Create new java object with all needed info
		WeatherData weatherData = new WeatherData("159880", timestamp, getTemp, getDirection, getSpeed, parameterList, stationName);
		
		createXmlFileFromJavaObject(weatherData);
		
		//Save object to DB
		weatherData = weatherDataRepository.save(weatherData);
		parameterList.clear();
		return weatherData;
	}

	/**
	 * Calls the API and collect the response  
	 * @param url and parameter
	 */
	private String getResponse(String url, String parameter) {
		
		//Use RestTemplates getForObject-method to get response from API. 
		RestTemplate restTemplate = new RestTemplate();
		
		//Use Response class to retrive the data you need
		Response response = restTemplate.getForObject(url, Response.class);



		String result = "";
		try {
			//Retrieve value for temp, direction or speed depending on which parameter that's passed in
			String jsonValuesStr = JSONArray.toJSONString(response.getValues());
			JSONObject jsonValueObj = new JSONObject(jsonValuesStr.substring(1, jsonValuesStr.length() - 1));
			result = jsonValueObj.get("value").toString();
			
			//Retrieve timestamp from JSON-object
			timestamp = Instant.ofEpochMilli(Long.parseLong(jsonValueObj.getString("date")));
			
			//Retrieve station name from JSON-object
			String jsonStationStr = JSONArray.toJSONString(response.getStation());
			JSONObject jsonStationObj = new JSONObject(jsonStationStr.substring(1, jsonStationStr.length() - 1));
			stationName = jsonStationObj.get("name").toString();
			
			/*Depending on which parameter number that's passed in, create a new instance 
			 * of Parameter class and pass the result with correct parameter type.
			 */
			if(parameter.matches("1")) {
				parameterInstance = new Parameter(ParameterType.TEMP, result);
			}else if(parameter.matches("3")) {
				parameterInstance = new Parameter(ParameterType.WIND_DIRECTION, result);
			} else {
				parameterInstance = new Parameter(ParameterType.WIND_SPEED, result);
			}
			
			//Add parameter instance to a list that later will be sent to an instance of WeatherData-class.
			parameterList.add(parameterInstance);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Creates a xml-file and xsd-file with JAXB library
	 * @param weatherData
	 */
	private void createXmlFileFromJavaObject(WeatherData weatherData) { 
		File xmlFile = new File(xmlFileString);
		try { 
			
			//Create JAXB
			JAXBContext jaxbContext = JAXBContext.newInstance(WeatherData.class);

			//Create xsd-file
			SchemaOutputResolver schemaResolver = new MySchemaOutputResolver();
			jaxbContext.generateSchema(schemaResolver);
			
			//Create Xml-file
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			  
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			jaxbMarshaller.marshal(weatherData, xmlFile); 
			
		  }
		  catch (JAXBException e) { 
			e.printStackTrace(); 
		  } catch (IOException e) {
			e.printStackTrace();
		  } 
	}
	
	/**
	 * Creates a text-file based on xml-file. Lets a user choose where to save the file. 
	 */
	public void createOrAppendTxtFileFromXmlData() {
		//Read from xml-file and then return a String with all info we need for txt-file
		String textString = readXmlFile();
		
		File file = new File("weather-data.txt");
		
		//This creates a dialog window that gives the user an opportunity to choose where to save the file
		JFileChooser chooser = new JFileChooser(".");
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		FileFilter restrict = new FileNameExtensionFilter("txt", "txt");
		chooser.setFileFilter(restrict);
		int response = chooser.showSaveDialog(null);
		
		if (response == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            
            try	{  
    			
    			/*
    			 * checks if file is already created.
    			 * If it not exist, create file and write message to it.
    			 * if it exist, add a new line and append the message to the file 
    			 */
    			if(file.createNewFile()) {
    				BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
    				writer.write(textString);
    				writer.close();
    			}else{
    				BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
    				writer.newLine();
    				writer.append(textString);
    				writer.close();
    			}  
    		}   
    		catch (IOException e)   {  
    			e.printStackTrace();
    		}         
            
        }
         
		//if the user cancelled the operation
        else {
           System.out.println("Dialog window have been closed and no file have been saved.");
    	}
	}
	
	/**
	 * Creates document and then call the method "getWeatherDataFromXml" that will collect resultText from xml-file. 
	 */
	private String readXmlFile() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		String resultText = "";
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document document = builder.parse(new File(xmlFileString));
			
			document.getDocumentElement().normalize();
			
			resultText = getWeatherDataFromXml(document);
			
		} catch (Exception e) {
			System.out.println("Error reading XML file:");
			e.printStackTrace();
		}
		return resultText;
	}

	/**
	 * Loops through xml-file, collect data that we need and then return a String that we later use for the txt-file. 
	 */
	private String getWeatherDataFromXml(Document document) {
		Map<String,String> values = new HashMap<>();
		
		NodeList weatherDataNodes = document.getElementsByTagName("weatherData");
		
		//Loop inside of weatherData node list and collect the data that we want to use
		for(int i = 0; i < weatherDataNodes.getLength(); i++) {
			Node weatherDataNode = weatherDataNodes.item(i);
			
			if(weatherDataNode.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) weatherDataNode;
				
                String stationName = element.getElementsByTagName("StationName").item(0).getTextContent();
                String timestamp = element.getElementsByTagName("timestamp").item(0).getTextContent();
                values.put("StationName",stationName);
                values.put("timestamp",timestamp);
                
                //Loop inside of parameter node list and collect data 
                NodeList parameterList = document.getElementsByTagName("Parameter");
        		for(int j = 0; j < parameterList.getLength(); j++) {
        			Node parameterNode = parameterList.item(j);
        			
        			if(parameterNode.getNodeType() == Node.ELEMENT_NODE) {
        				Element parameterElement = (Element) parameterNode;
        				String value = parameterElement.getElementsByTagName("value").item(0).getTextContent();
        				values.put("value" + j,value);
        			}
        		}
			}
		}
		
		return buildStringFromCollectedData(values);
	}

	private String buildStringFromCollectedData(Map<String, String> values) {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(values.get("timestamp"));
		strBuilder.append("," + values.get("StationName"));
		strBuilder.append("," + values.get("value0"));
		strBuilder.append("," + values.get("value1"));
		strBuilder.append("," + values.get("value2"));
		return strBuilder.toString();
	}
	
}
