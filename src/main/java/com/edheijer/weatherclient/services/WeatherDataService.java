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
import java.util.regex.Pattern;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.edheijer.weatherclient.models.Parameter;
import com.edheijer.weatherclient.models.ParameterType;
import com.edheijer.weatherclient.models.Response;
import com.edheijer.weatherclient.models.WeatherData;
import com.edheijer.weatherclient.repositories.WeatherDataRepository;

import net.minidev.json.JSONArray;

@Service
public class WeatherDataService {

	private final String startApiUrl = "https://opendata-download-metobs.smhi.se/api/version/1.0/parameter/";

	@Value("${app.stationId}")
	private String stationId;

	private final String temp = "1";

	private final String direction = "3";

	private final String speed = "4";
	
	private final String xmlFileString = "weather-data.xml";

	private Instant timestamp;
	
	private Parameter parameterInstance;
	
	List<Parameter> parameterList = new ArrayList<>();
	
	private String stationName;

	@Autowired
	private WeatherDataRepository weatherDataRepository;

	public WeatherData handleWeatherData() {
		// For some reason I can't declare this variable with "stationId" on class level
		// because in that case is "stationId" null.
		String endApiUrl = "/station/" + stationId + "/period/latest-hour/data.json";
		
		//Get response for temp, direction and speed
		String getTemp = getResponse(startApiUrl + temp + endApiUrl, temp);
		String getDirection = getResponse(startApiUrl + direction + endApiUrl, direction);
		String getSpeed = getResponse(startApiUrl + speed + endApiUrl, speed);
		
		//Create new java object with all needed info
		WeatherData weatherData = new WeatherData("159880", timestamp, getTemp, getDirection, getSpeed, parameterList, stationName);
		
		createXmlFileFromJavaObject(weatherData);
		
		//Read from xml-file and then return a String with all info we need for txt-file
		String textString = readXmlFile();
		createOrAppendTxtFile(textString);
		
		//Save object to DB
		weatherData = weatherDataRepository.save(weatherData);
		parameterList.clear();
		return weatherData;
	}

	private String getResponse(String url, String parameter) {
		
		//Use RestTemplates getForObject-method to get response from API. 
		//Use Response class to retrive the data you need
		RestTemplate restTemplate = new RestTemplate();
		Response response = restTemplate.getForObject(url, Response.class);
		
		String result = "";
		try {
			//Retrieve value from temp, direction or speed depending on which parameter that's passed in
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

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
		  catch (JAXBException e) 
		  { 
		  e.printStackTrace(); 
		  
		  } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
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
		System.out.println(resultText);
		return resultText;
	}

	/*
	 * Loops through xml-file and then return a String that we later use for the txt-file
	 */
	private String getWeatherDataFromXml(Document document) {
		NodeList nodeList = document.getElementsByTagName("weatherData");
		Map<String,String> values = new HashMap<>();
		
		//Loop inside of weatherData node list and collect the data that we want to use
		for(int i = 0; i < nodeList.getLength(); i++) {
			Node weatherDataNode = nodeList.item(i);
			
			if(weatherDataNode.getNodeType() == Node.ELEMENT_NODE) {
				
				NodeList fields = weatherDataNode.getChildNodes();
				for(int j = 0; j < fields.getLength(); j++) {
					Node fieldNode = fields.item(j);
					
					if(fieldNode.getNodeType() == Node.ELEMENT_NODE) {
						
						if(fieldNode.getNodeName().equalsIgnoreCase("parameter")) {
							handleParameterTags(document, values);
						}else {
							values.put(fieldNode.getNodeName(),fieldNode.getTextContent().trim());
						}
						
					}
				}
			}
		}
		
		return buildStringFromCollectedData(values);
	}

	//Loops trough all parameter-tags to collect data inside of them
	private void handleParameterTags(Document document, Map<String, String> values) {
		NodeList parameterList = document.getElementsByTagName("Parameter");
		for(int k = 0; k < parameterList.getLength(); k++) {
			Node parameterNode = parameterList.item(k);
			
			if(parameterNode.getNodeType() == Node.ELEMENT_NODE) {
				NodeList parameterChilds = parameterNode.getChildNodes();
				for(int l = 0; l < parameterChilds.getLength(); l++) {
					Node childNode = parameterChilds.item(l);
					
					if(childNode.getNodeType() == Node.ELEMENT_NODE) {
		
						if (isNumeric(childNode.getTextContent())) {
							values.put(childNode.getNodeName()+k,childNode.getTextContent().trim());
					
						} else {
						    continue;
						}	
					}
				}
			}
		}
	}
	
	// Checks if the provided string is a numeric by applying a regular expression on it.
	private boolean isNumeric(String string) {
	    String regex = "[0-9]+[\\.]?[0-9]*";
	    return Pattern.matches(regex, string);
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
	
	private void createOrAppendTxtFile(String textString) {
		File file;
		
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
        // if the user cancelled the operation
        else {
           System.out.println("Dialog window have been closed and no file have been saved.");
    	}
	}
}
