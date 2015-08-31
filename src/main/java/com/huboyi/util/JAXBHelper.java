package com.huboyi.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * 以XStream和Jackson框架为核心，为测试用例提供JAXB功能的帮助类。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2014/2/28
 * @version 1.0
 */
public class JAXBHelper {
	
	/**
	 * 把JavaBean转换为默认格式的XML。 
	 * 
	 * @param obj JavaBean
	 * @return String
	 */
	public static String javaToDefaultXml (Object obj) {
		XStream xstream = new XStream(new DomDriver());
		xstream.autodetectAnnotations(true);
		xstream.ignoreUnknownElements();
		return xstream.toXML(obj);
	}
	
	/**
	 * 把JavaBean转换为mimi格式的XML，建议在开发中采用此方法，以提升部分性能。 
	 * 
	 * @param obj JavaBean
	 * @return String
	 */
	public static String javaToMiniXml (Object obj) {
		XStream xstream = new XStream(new DomDriver());
		xstream.autodetectAnnotations(true);
		xstream.ignoreUnknownElements();
		
		Writer writer = new StringWriter();
		xstream.marshal(obj, new CompactWriter(writer));
		
		return writer.toString();
	}

	/**
	 * 把XML转换为JavaBean。
	 * 
	 * @param xml xml信息
	 * @param clazz 将要转换为的JavaBean
	 * @return <T>
	 */
	public static <T> T xmlToJava (String xml, Class<T> clazz) {
		XStream xstream = new XStream(new DomDriver());
		xstream.alias("xml" , clazz);
		xstream.processAnnotations(clazz);
		xstream.autodetectAnnotations(true);
		xstream.ignoreUnknownElements();
		
		return clazz.cast(xstream.fromXML(xml));
	}
	
	/**
	 * 把JavaBean转换为默认格式的JSON。 
	 * 
	 * @param obj JavaBean
	 * @return String
	 * @throws JsonProcessingException
	 */
	public static String
	javaToDefaultJson (Object obj) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		// 为保险起见，统一在次进行“@JsonInclude(Include.NON_NULL)”注解的设置，表明当属性值为null不进行映射。
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		// 为保险起见，统一在次进行“@JsonInclude(Include.NON_EMPTY)”注解的设置，表明当集合不为null同时存在值时才进行映射。
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		String message = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		return message;
	}
	
	/**
	 * 把JavaBean转换为mimi格式的JSON，建议在开发中采用此方法，以提升部分性能。 
	 * 
	 * @param obj JavaBean
	 * @return String
	 * @throws JsonProcessingException
	 */
	public static String 
	javaToMiniJson (Object obj) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		// 为保险起见，统一在次进行“@JsonInclude(Include.NON_NULL)”注解的设置，表明当属性值为null不进行映射。
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		// 为保险起见，统一在次进行“@JsonInclude(Include.NON_EMPTY)”注解的设置，表明当集合不为null同时存在值时才进行映射。
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		// 当没有使用jackson标签的类进行转换时不报错。
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String message = mapper.writer(new MinimalPrettyPrinter()).writeValueAsString(obj);
		return message;
	}
	
	/**
	 * 把JSON转换为JavaBean。
	 * 
	 * @param json json信息
	 * @param clazz 将要转换为的JavaBean
	 * @return <T>
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T 
	jsonToJava (String json, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		// 当仅在JSON字符串中存在，但Java对象中不存在该属性时不报错。
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		return mapper.readValue(json, clazz);
	}
}