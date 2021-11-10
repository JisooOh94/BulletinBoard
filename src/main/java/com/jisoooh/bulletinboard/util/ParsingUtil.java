package com.jisoooh.bulletinboard.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import reactor.core.publisher.Mono;

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

	public static <T> Mono<T> parse(Object source, Class<T> targetClass, String parseFailMessage) {
		return Mono.just(mapper.convertValue(source, targetClass))
				.doOnError(IllegalArgumentException.class, throwable -> logger.error(parseFailMessage, throwable));
	}
}
