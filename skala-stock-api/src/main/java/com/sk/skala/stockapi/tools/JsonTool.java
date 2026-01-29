package com.sk.skala.stockapi.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonTool {

	public static String toString(Object obj) {
		try {
			ObjectMapper om = new ObjectMapper();
			return om.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			log.error("JsonTool.toString: {}", e.toString());
			return new String();
		}
	}

	public static <T> T toObject(String data, Class<T> c) {
		try {
			ObjectMapper om = new ObjectMapper();
			return om.readValue(data, c);
		} catch (JsonProcessingException e) {
			log.error("JsonTool.toObject: {}", e.toString());
			return null;
		}
	}

	public static Map<String, Object> toMap(String data) {
		try {
			ObjectMapper om = new ObjectMapper();
			return om.readValue(data, new TypeReference<Map<String, Object>>() {
			});
		} catch (JsonProcessingException e) {
			log.error("JsonTool.toMap: {}", e.toString());
			return null;
		}
	}

	public static <T> List<T> toList(String data, Class<T> cls) {
		try {
			ObjectMapper om = new ObjectMapper();
			return om.readValue(data, om.getTypeFactory().constructCollectionType(List.class, cls));
		} catch (JsonProcessingException e) {
			log.error("JsonTool.toList: {}", e.toString());
			return new ArrayList<T>();
		}
	}

	public static JsonNode toJsonNode(String data) {
		try {
			ObjectMapper om = new ObjectMapper();
			return om.readTree(data);

		} catch (JsonMappingException e) {
			log.error("JsonTool.toJsonNode: {}", e.toString());

		} catch (JsonProcessingException e) {
			log.error("JsonTool.toJsonNode: {}", e.toString());
		}
		return null;
	}
}
