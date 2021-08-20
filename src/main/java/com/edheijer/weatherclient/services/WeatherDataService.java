package com.edheijer.weatherclient.services;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.edheijer.weatherclient.models.Parameter;
import com.edheijer.weatherclient.models.ParameterType;
import com.edheijer.weatherclient.models.ValuesResponse;
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

	public WeatherData getWeatherData() {
		// For some reason I can't declare this variable with "stationId" on class level
		// because in that case is "stationId" null.
		String endApiUrl = "/station/" + stationId + "/period/latest-hour/data.json";
		String getTemp = getResponse(startApiUrl + temp + endApiUrl, temp);
		String getDirection = getResponse(startApiUrl + direction + endApiUrl, direction);
		String getSpeed = getResponse(startApiUrl + speed + endApiUrl, speed);
		WeatherData weatherData = new WeatherData("159880", timestamp, getTemp, getDirection, getSpeed, parameterList, stationName);
		javaObjectToXML(weatherData);
		String textString = readXmlFile();
		createTxtFile(textString);
		weatherData = weatherDataRepository.save(weatherData);
		parameterList.clear();
		return weatherData;
	}

	private String getResponse(String url, String parameter) {
		RestTemplate restTemplate = new RestTemplate();
		ValuesResponse response = restTemplate.getForObject(url, ValuesResponse.class);
		String jsonValuesStr = JSONArray.toJSONString(response.getValues());
		String jsonStationStr = JSONArray.toJSONString(response.getStation());
		JSONObject jsonValueObj;
		JSONObject jsonStationObj;
		String result = "";
		try {
			jsonValueObj = new JSONObject(jsonValuesStr.substring(1, jsonValuesStr.length() - 1));
			result = jsonValueObj.get("value").toString();
			jsonStationObj = new JSONObject(jsonStationStr.substring(1, jsonStationStr.length() - 1));
			stationName = jsonStationObj.get("name").toString();
			if(parameter.matches("1")) {
				parameterInstance = new Parameter(ParameterType.TEMP, result);
			}else if(parameter.matches("3")) {
				parameterInstance = new Parameter(ParameterType.WIND_DIRECTION, result);
			} else {
				parameterInstance = new Parameter(ParameterType.WIND_SPEED, result);
			}
			parameterList.add(parameterInstance);
			timestamp = Instant.ofEpochMilli(Long.parseLong(jsonValueObj.getString("date")));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * private static Element generateXmlFile(WeatherData weatherData) { try {
	 * Document doc = new Document(); doc.setRootElement(new
	 * Element("WeatherData")); doc. doc.getRootElement().addContent("");
	 * doc.getRootElement().addContent(createUserXMLElement("2", "Tom", "Cruise",
	 * "45", "Male")); doc.getRootElement().addContent(createUserXMLElement("3",
	 * "Tony", "Stark", "40", "Male"));
	 * doc.getRootElement().addContent(createUserXMLElement("3", "Amir", "Khan",
	 * "50", "Male"));
	 * 
	 * // new XMLOutputter().output(doc, System.out); XMLOutputter xmlOutput = new
	 * XMLOutputter();
	 * 
	 * // xmlOutput.output(doc, System.out); // display nice nice
	 * xmlOutput.setFormat(Format.); xmlOutput.output(doc, new
	 * FileWriter("create_jdom_users.xml"));
	 * 
	 * System.out.println("File Saved!"); } catch (IOException io) {
	 * System.out.println(io.getMessage()); } Element user = new Element("User");
	 * user.setAttribute(new Attribute("id", id)); user.addContent(new
	 * Element("firstName").setText(firstName)); user.addContent(new
	 * Element("lastName").setText(lastName)); user.addContent(new
	 * Element("age").setText(age)); user.addContent(new
	 * Element("gender").setText(gender)); return user; }
	 */

	@Async
	private void javaObjectToXML(WeatherData weatherData) { 
		File xmlFile = new File(xmlFileString);
		File xsdFile = new File("weather-data.xsd");
		try { 
			
			//Create JAXB
			JAXBContext jaxbContext = JAXBContext.newInstance(WeatherData.class);

			//Create xsd-file
//			jaxbContext.generateSchema(new SchemaOutputResolver() {
//				
//				@Override
//				public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
//					StreamResult result = new StreamResult(new FileOutputStream(xsdFile));
//					result.setSystemId(xsdFile.getAbsolutePath());
//					return result;
//				}
//			});
			
			
			//Create Xml-file
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			  
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			jaxbMarshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION,"weather-data.xsd");
			jaxbMarshaller.marshal(weatherData, xmlFile); 
			
		  
//		  SchemaOutputResolver sor = new MySchemaOutputResolver();
//		  sor.createOutput("weather-data.xml", "weather-data.xsd");
//			jaxbContext.generateSchema(sor);
		  }
		  catch (JAXBException e) 
		  { 
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
			
			NodeList nodeList = document.getElementsByTagName("weatherData");
			Map<String,String> values = new HashMap<>();
			
			for(int i = 0; i < nodeList.getLength(); i++) {
				Node weatherDataNode = nodeList.item(i);
				
				if(weatherDataNode.getNodeType() == Node.ELEMENT_NODE) {
					Element weatherData = (Element) weatherDataNode;
					
					NodeList fields = weatherDataNode.getChildNodes();
					for(int j = 0; j < fields.getLength(); j++) {
						Node fieldNode = fields.item(j);
						
						if(fieldNode.getNodeType() == Node.ELEMENT_NODE) {
							Element field = (Element) fieldNode;
							
							if(field.getNodeName().equalsIgnoreCase("parameter")) {
								NodeList parameterList = document.getElementsByTagName("Parameter");
								for(int k = 0; k < parameterList.getLength(); k++) {
									Node parameterNode = parameterList.item(k);
									
									if(parameterNode.getNodeType() == Node.ELEMENT_NODE) {
										Element parameterField = (Element) parameterNode;
										NodeList parameterChilds = parameterNode.getChildNodes();
										for(int l = 0; l < parameterChilds.getLength(); l++) {
											Node childNode = parameterChilds.item(l);
											
											if(childNode.getNodeType() == Node.ELEMENT_NODE) {
												Element childField = (Element) childNode;
												if (isNumeric(childField.getTextContent())) {
													values.put(childField.getNodeName()+k,childField.getTextContent().trim());
											
												} else {
												    continue;
												}	
											}
											j++;
										}
									}
								}
							}else {
								
								values.put(field.getNodeName(),field.getTextContent().trim());
							}
							
						}
					}
				}
			}
			values.entrySet().forEach(x -> System.out.println(x.getKey()));
			resultText += values.get("timestamp");
			resultText += ","+ values.get("StationName");
			resultText += ","+ values.get("value0");
			resultText += ","+ values.get("value1");
			resultText += ","+ values.get("value2");
			
			System.out.println(resultText);
			
		} catch (Exception e) {
			System.out.println("Error reading configuration file:");
	        System.out.println(e.getMessage());
		}
		
		return resultText;
	}
	
	// Checks if the provided string is a numeric by applying a regular expression on it.
	private boolean isNumeric(String string) {
	    String regex = "[0-9]+[\\.]?[0-9]*";
	    return Pattern.matches(regex, string);
	}
	
	private void createTxtFile(String textString) {
		
		
	}
	
	
}
