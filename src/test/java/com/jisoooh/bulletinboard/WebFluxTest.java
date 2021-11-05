package com.jisoooh.bulletinboard;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestExecutionListeners;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.POJOPropertiesCollector;
import com.jisoooh.bulletinboard.model.Article;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

public class WebFluxTest {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void and() {
		Mono<String> mono_1 = Mono.just("mono_1").map(data -> {
			logger.info("mono_1 start");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.info("mono_1 finish");
			return data;
		});

		Mono<String> mono_2 = Mono.just("mono_2").map(data -> {
			logger.info("mono_2 start");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.info("mono_2 finish");
			return data;
		});

		Mono<Void> combinedResultMono = mono_1.and(mono_2);
		combinedResultMono.doOnSuccess(result -> logger.info("result finished")).block();
	}

	private Mono<String> getId(Mono<Integer> noMono) {
		return Mono.just("id");
	}

	private Flux<String> getClassList(Mono<Integer> noMono) {
		return Flux.just("math", "science");
	}

	@Test
	public void as() {
		Mono<String> idMono = Mono.just(1)
				.flatMap(no -> getId(Mono.just(no)));

		Mono<String> idMono_2 = Mono.just(1)
				.as(no -> getId(no));

		Flux<String> classFlux = Mono.just(1)
				.as(no -> getClassList(no));
	}

	@Test
	public void cast() {
		Mono<Integer> intMono = Mono.just("1234").cast(Integer.class);

		logger.info(intMono.block().toString());
	}

	@Test
	public void ofType() {
		Mono<Integer> intMono = Mono.just("1234").ofType(Integer.class);

		logger.info("result : " + intMono.hasElement().block());
	}

	@Test
	public void cache() throws InterruptedException {
		Mono<String> mono = Mono.just(1)
				.map(val -> {
					logger.info("do mult");
					return String.valueOf(val * 2);
				});
		Mono<String> cachedMono = mono.cache();
		Mono<String> durationCachedMono = mono.cache(Duration.ofSeconds(1));

		logger.info("Mono");
		mono.subscribe(logger::info);
		mono.subscribe(logger::info);

		logger.info("cachedMono");
		cachedMono.subscribe(logger::info);
		cachedMono.subscribe(logger::info);

		logger.info("durationCachedMono");
		durationCachedMono.subscribe(logger::info);
		durationCachedMono.subscribe(logger::info);
		Thread.sleep(2000);
		durationCachedMono.subscribe(logger::info);
	}

	@Test
	public void stackTraceTest_blocking() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4);
		list.get(5);
	}

	@Test
	public void stackTraceTest_non_blocking_checkpoint() {
		Flux<Integer> flux = Flux.just(1, 2, 3, 4).checkpoint("createFlux");
		Mono<Integer> mono = flux.elementAt(5).checkpoint("getElement");
		mono.block();
	}

	@Test
	public void non_blockingTest_onOperatorDebug() {
		Hooks.onOperatorDebug();
		Flux<Integer> flux = Flux.just(1, 2, 3, 4);
		Mono<Integer> mono = flux.elementAt(5);
		mono.block();
	}

	@Test
	public void subscribeOn() {
		Mono.just(1)
				.publishOn(Schedulers.newSingle("Publisher Thread"))
				.map(item -> item * 10)
				.log()
				.subscribeOn(Schedulers.newSingle("Subscriber Thread"))
				.subscribe(item -> logger.info("current thread : " + Thread.currentThread().getName()));
	}

	@Test
	public void mapTest() {
		Mono<Integer> result = Mono.just("1234").map(value -> Integer.parseInt(value));
		Mono<Integer> mono2mono = Mono.just("1234")
				.flatMap(value -> Mono.just(Integer.parseInt(value)));

		Flux<Integer> flux2flux = Flux.just("1", "2", "3", "4")
				.flatMap(value -> Mono.just(Integer.parseInt(value)));

		Flux<Integer> mono2flux = Mono.just("1234")
				.flatMapMany(value ->
						Flux.fromStream(
								Arrays.stream(StringUtils.split(value))
										.map(Integer::parseInt)));

		Mono<Integer> flux2mono = Flux.just("1", "2", "3", "4")
				.flatMap(value -> Mono.just(Integer.parseInt(value)))
				.collect(Collectors.summingInt(val -> val));

		Flux<Integer> result_5 = Flux.just("1", "2", "3", "4").flatMap(value -> Mono.just(Integer.parseInt(value)));

		System.out.println("start");

		result_5.subscribe(value -> logger.info("# val : {}", value));

		System.out.println("end");

		Flux<String> result_6 = Flux.range(1, 100).flatMap(value -> Flux.just(String.valueOf(value)));

		List<String> result_7 = result_6.collectList().block();
		result_7.forEach(System.out::println);

		Flux<String> result_8 = Flux.range(1, 100).map(value -> String.valueOf(value));
		List<String> result_9 = result_8.collectList().block();
		result_9.forEach(System.out::println);
	}

	@Test
	public void test() {
		Flux<String> flux = Flux.just(1,2,3,4)
				.map(val -> {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						return StringUtils.EMPTY;
					}
					return String.valueOf(val);
				});


		System.out.println("start");

		flux.subscribe(new BaseSubscriber<String>() {
			@Override
			protected void hookOnSubscribe(Subscription subscription) {
				System.out.println("Subscribed to paged flux processing items");
				super.hookOnSubscribe(subscription);
			}

			protected void hookOnNext(Integer value) {
				System.out.println("Processing item with value: " + value);
			}

			@Override
			protected void hookOnComplete() {
				System.out.println("Processing complete.");
			}
		});

		System.out.println("end");
	}

	@Test
	public void flatMapMany() {
		int[] arr = new int[4];
		Flux<String> result = Mono.just("1,2,3,4").flatMapMany(val -> Flux.fromArray(StringUtils.split(val, ",")));
		List<String> result_2 = result.collectList().block();
		result_2.forEach(System.out::println);
	}


	@Test
	public void doTest() {
		//doFirst
//		Flux.just(1,2,3).doFirst(() -> logger.info("# doFirst called"))
//				.map(String::valueOf)
//				.log()
//				.blockLast();

		//doFinally
//		Flux.just(1,2,3)
//				.doFinally(signalType -> logger.info("# doFinally Called. signalType : {}", signalType.toString()))
//				.map(String::valueOf)
//				.log()
//				.blockLast();
//
//		Flux.just(1,2,3)
//				.doFinally(signalType -> logger.info("# doFinally Called. signalType : {}", signalType.toString()))
//				.flatMap(value -> Mono.error(new RuntimeException("myError")))
//				.log()
//				.blockLast();

		//doOnTerminate
//		Flux.just(1,2,3)
//				.flatMap(value -> value == 2 ? Flux.error(new RuntimeException("myError")) : Flux.just(value))
//				.doOnTerminate(() -> logger.info("# doOnTerminate Called. Thread name : {}", Thread.currentThread().getName()))
//				.log()
//				.blockLast();

//		Flux.just(1,2,3)
//				.doOnTerminate(() -> logger.info("# doOnTerminate Called. Thread name : {}", Thread.currentThread().getName()))
//				.flatMap(value -> value == 2 ? Flux.error(new RuntimeException("myError")) : Flux.just(value))
//				.log()
//				.blockLast();

//		Flux.just(1,2,3)
//				.map(String::valueOf)
//				.doOnTerminate(() -> logger.info("# doOnTerminate Called. Thread name : {}", Thread.currentThread().getName()))
//				.log()
//				.blockLast();

		//doAfterTerminate
//		Flux.just(1, 2, 3)
//				.flatMap(value -> value == 2 ? Flux.error(new RuntimeException("myError")) : Flux.just(value))
//				.doAfterTerminate(() -> logger.info("# doAfterTerminate Called. Thread name : {}", Thread.currentThread().getName()))
//				.log()
//				.blockLast();

		Mono.just(1)
				.map(String::valueOf)
				.doOnSubscribe(elem -> {
					logger.info("going sleep");
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					logger.info("end sleep");
				}).map(val -> {
					logger.info("doind mapping");
					return Integer.parseInt(val);
			}).block();
	}

	@Test
	public void onTest() {
		Flux.just(1,2,3)
				.flatMap(value -> value == 2 ? Flux.error(new RuntimeException("myError")) : Flux.just(value))
				.onErrorMap(IOException.class, throwable -> new InvalidParameterException("mappedException"))
				.log()
				.blockLast();

//		Flux.just(1,2,3)
//				.flatMap(value -> value == 2 ? Flux.error(new RuntimeException("myError")) : Flux.just(value))
//				.onErrorResume(exception -> Flux.just(-1))
//				.log()
//				.blockLast();

//		Flux.just(1,2,3)
//				.flatMap(value -> value == 2 ? Flux.error(new RuntimeException("myError")) : Flux.just(value))
//				.onErrorReturn(-1)
//				.log()
//				.blockLast();

//		Flux.just(1,2,3)
//				.flatMap(value -> value == 2 ? Flux.error(new RuntimeException("myError")) : Flux.just(value))
//				.onErrorContinue((exception, element) -> logger.error("exception : {}, failed element : {}", exception, element))
//				.log()
//				.blockLast();

//		Flux.just(1, 2, 3)
//				.flatMap(value -> value == 2 ? Flux.error(new RuntimeException("myError")) : Flux.just(value))
//				.onErrorStop()
//				.log()
//				.blockLast();

		int a = 0;
	}

	@Test
	public void create() {
//		Mono.just(1).log().block();

		Flux.just(1, 2, 3).log().blockLast();

		Flux.fromArray(new Integer[]{1, 2, 3}).log().blockLast();

		Flux.range(1, 3).log().blockLast();

		Mono.empty().log().block();
	}

	@Test
	public void subscribeTest() {
		Flux.just(1, 2, 3)
				.map(String::valueOf)
				.subscribe(
						value -> logger.info("# value : {}", value));

		Flux.just(1, 2, 3)
				.flatMap(value -> {
					if (value == 2) return Mono.error(new RuntimeException("My Exception"));
					else return Mono.just(String.valueOf(value));
				})
				.subscribe(
						value -> logger.info("# value : {}", value),
						error -> logger.error("# error : {}", error.getMessage()));

		Flux.just(1, 2, 3)
				.map(String::valueOf)
				.subscribe(
						value -> logger.info("# value : {}", value),
						error -> logger.error("# error : {}", error.getMessage()),
						() -> logger.info("# complete called"));
	}

	@Test
	public void zipTest() {
		Mono<Integer> mono_1 = Mono.just(1);
		Mono<Integer> mono_2 = Mono.just(2);
		Mono.zip(arr -> Arrays.stream(arr).collect(Collectors.summingInt(val -> (int)val)), mono_1, mono_2);

		Mono.zip(Mono.just(1), Mono.just("2"))
				.log()
				.subscribe(tuple2 ->
						logger.info("# val_1: {}, val_2 : {}", tuple2.getT1(), tuple2.getT2())
				);
	}
}