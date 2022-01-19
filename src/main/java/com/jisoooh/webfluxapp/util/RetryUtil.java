package com.jisoooh.webfluxapp.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

public class RetryUtil {
	private static Logger logger = LoggerFactory.getLogger(RetryUtil.class);

	private RetryUtil() {
		throw new AssertionError();
	}

	public static final RetryBackoffSpec CommonRetryCondition = getRetryCondition(3, Duration.ofSeconds(2), StringUtils.EMPTY, IOException.class);

	public static RetryBackoffSpec getRetryCondition(int maxAttepts, Duration delay, String retryFailMessage, Class<? extends Throwable>... targetExceptions) {
		return Retry.backoff(maxAttepts, delay)
				.filter(throwable -> Stream.of(targetExceptions).anyMatch(exception -> exception.isInstance(throwable)))
				.doBeforeRetry(retrySignal -> logger.info("# execute Retry"))
				.onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> {
					logger.error(retryFailMessage, retrySignal.failure());
					return new UncheckedIOException(retryFailMessage, new IOException(retrySignal.failure()));
				}));
	}
}
