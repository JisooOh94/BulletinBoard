package com.jisoooh.bulletinboard.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ParsingUtil {
	private static Logger logger = LoggerFactory.getLogger(ParsingUtil.class);
	private static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.registerModule(new JavaTimeModule());
	}

	private ParsingUtil() {
		throw new AssertionError();
	}

	public static <T> T parse(Object source, Class<T> targetClass) {
		return mapper.convertValue(source, targetClass);
	}

	public static <T> T parse(Object source, TypeReference<T> targetClass) {
		return mapper.convertValue(source, targetClass);
	}
}
