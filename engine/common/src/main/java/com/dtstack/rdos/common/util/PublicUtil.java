package com.dtstack.rdos.common.util;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


public class PublicUtil {
	
	private static ObjectMapper objectMapper = new ObjectMapper();
	
	public static <T> T mapToObject(Map<String,Object> params,Class<T> clazz) throws JsonParseException, JsonMappingException, JsonGenerationException, IOException{
		return  objectMapper.readValue(objectMapper.writeValueAsBytes(params),clazz);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String,Object> ObjectToMap(Object obj) throws JsonParseException, JsonMappingException, JsonGenerationException, IOException{
		
		return objectMapper.readValue(objectMapper.writeValueAsBytes(obj), Map.class);
	}
	
	
	public static boolean count(int index,int multiples){
		return index%multiples==0;
	}

}
