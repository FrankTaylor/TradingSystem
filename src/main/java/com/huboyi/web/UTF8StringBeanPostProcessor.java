package com.huboyi.web;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;

/**
 * spring返回中文乱码修正类。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2014/3/4
 * @version 1.0
 */
public class UTF8StringBeanPostProcessor implements BeanPostProcessor {
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		if (bean instanceof StringHttpMessageConverter) {
			MediaType mediaType = new MediaType("text", "plain",
					Charset.forName("UTF-8"));
			List<MediaType> types = new ArrayList<MediaType>();
			types.add(mediaType);
			((StringHttpMessageConverter) bean).setSupportedMediaTypes(types);
		}
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}
}