package com.jisoooh.webfluxapp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFunctions;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.util.CharsetUtil;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@RunWith(MockitoJUnitRunner.class)
public class WebClientTest {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private MockWebServer mockWebServer;
	private String baseUrl;

	@Before
	public void init() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
//		mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("body"));

		baseUrl = "http://localhost:" + mockWebServer.getPort();
	}
	@After
	public void destroy() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	public void defaultUriVariables() throws InterruptedException {
		Map<String, Object> uriVariableMap = new HashMap<>();
		uriVariableMap.put("pathVar", "foo");
		uriVariableMap.put("paramVar", "bar");

		WebClient webClient = WebClient.builder()
				.baseUrl(baseUrl + "/api/{pathVar}")
				.defaultUriVariables(uriVariableMap)
				.build();

		webClient.get()
				.uri("?param={paramVar}")
				.retrieve()
				.bodyToMono(String.class)
				.block();

		logger.info("RequestUrl : " + mockWebServer.takeRequest().getRequestUrl());
	}

	@Test
	public void defaultHeader() throws InterruptedException {
		/*WebClient webClient = WebClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.build();*/
		WebClient webClient = WebClient.builder()
				.baseUrl(baseUrl)
				.defaultHeaders(httpHeaders -> {
					httpHeaders.setContentType(MediaType.APPLICATION_JSON);
					httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
				})
				.build();

		webClient.get()
				.retrieve()
				.bodyToMono(String.class)
				.block();

		logger.info("Accept Header : " + mockWebServer.takeRequest().getHeader(HttpHeaders.ACCEPT));
	}

	@Test
	public void defaultRequest() throws InterruptedException {
		WebClient webClient = WebClient.builder()
				.baseUrl(baseUrl)
				.defaultRequest(requestHeadersSpec -> {
					requestHeadersSpec.accept(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON);
					requestHeadersSpec.cookies(cookieMap -> cookieMap.add("key", "value"));
					requestHeadersSpec.header(HttpHeaders.ACCEPT_CHARSET, CharsetUtil.UTF_8.name());
				})
				.build();

		webClient.get()
				.retrieve()
				.bodyToMono(String.class)
				.block();

		logger.info("Accept Header : " + mockWebServer.takeRequest().getHeader(HttpHeaders.ACCEPT));
	}

	@Test
	public void filter() throws InterruptedException {
		WebClient webClient = WebClient.builder()
				.baseUrl(baseUrl)
				.filter((request, next) -> {
					ClientRequest filtered = ClientRequest.from(request)
							.header("foo", "bar")
							.build();

					return next.exchange(filtered);
				})
				.build();

		webClient.get()
				.retrieve()
				.bodyToMono(String.class)
				.block();

		logger.info("Header : " + mockWebServer.takeRequest().getHeader("foo"));
	}

	@Test
	public void exchangeStrategies() {
		WebClient webClient = WebClient.builder()
				.baseUrl(baseUrl)
				.exchangeStrategies(
						ExchangeStrategies.builder().codecs(clientCodecConfigurer ->
							clientCodecConfigurer.defaultCodecs()
									.jackson2JsonDecoder(new Jackson2JsonDecoder(new ObjectMapper(), MediaType.APPLICATION_JSON))
						).build())
				.build();
	}

	@Test
	public void exchangeFunction() {
		ExchangeStrategies strategies = ExchangeStrategies.builder().codecs(clientCodecConfigurer ->
				clientCodecConfigurer.defaultCodecs()
						.jackson2JsonDecoder(new Jackson2JsonDecoder(new ObjectMapper(), MediaType.APPLICATION_JSON))
		).build();

		HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

		ClientHttpConnector clientHttpConnector = new ReactorClientHttpConnector(httpClient);

		WebClient webClient = WebClient.builder()
				.baseUrl(baseUrl)
				.exchangeFunction(ExchangeFunctions.create(new JettyClientHttpConnector(), strategies))
				.build();
		webClient.mutate();
	}

	@Test
	public void clone_() {
		WebClient.Builder commonWebClientBuilder = WebClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.exchangeStrategies(ExchangeStrategies.builder().codecs(configure -> configure.defaultCodecs()
						.jackson2JsonDecoder(new Jackson2JsonDecoder(new ObjectMapper(), MediaType.APPLICATION_JSON))).build());

		WebClient fooWebClient = commonWebClientBuilder.clone()
				.defaultCookie("userKey", "userValue").build();

		WebClient barWebClient = commonWebClientBuilder.clone()
				.clientConnector(new JettyClientHttpConnector())
				.build();
	}

	@Test
	public void uriBuilderFactor() throws InterruptedException {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(URI.create("localhost"))
				.scheme("http")
				.port(8080)
				.path("/api/");

		WebClient webClient= WebClient.builder()
				.uriBuilderFactory(new DefaultUriBuilderFactory(uriBuilder))
				.build();

		webClient.get()
				.retrieve()
				.bodyToMono(String.class)
				.block();

		logger.info("Accept Header : " + mockWebServer.takeRequest().getRequestUrl());
	}

	@Test
	public void retrieve() {
		mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));
		WebClient webClient= WebClient.builder().baseUrl(baseUrl).build();
		Mono<ResponseEntity<String>> responseEntityMono = webClient.get()
				.retrieve()
				.toEntity(String.class)
				.log();

		/*Mono<String> bodyMono = webClient.get()
				.retrieve()
				.bodyToMono(String.class);*/

		logger.info("Header : " + responseEntityMono.map(responseEntity -> responseEntity.getHeaders().get("foo")).block().get(0));
	}

	@Test
	public void exchange() {
		mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));
		WebClient webClient= WebClient.builder().baseUrl(baseUrl).build();
		Mono<Object> response = webClient.get()
				.exchange()
				.map(clientResponse -> {
					if(clientResponse.statusCode() == HttpStatus.OK) {
						return clientResponse.bodyToMono(String.class);
					} else if(clientResponse.statusCode().is4xxClientError()) {
						return clientResponse.bodyToMono(RuntimeException.class);
					} else {
						return clientResponse.createException();
					}
				});
	}

	@Test
	public void exchange_memoryLeak() {
		mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()));
		WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();

		webClient.get()
				.exchange()
				.map(clientResponse -> {
					if (clientResponse.statusCode().is2xxSuccessful()) {
						return clientResponse.bodyToMono(String.class);
					} else if (clientResponse.statusCode().is4xxClientError()) {
						return Mono.error(new InvalidParameterException(clientResponse.statusCode().getReasonPhrase()));
					} else {
						return Mono.error(new RuntimeException());
					}
				});
	}

	public class ApiResult {
		private int code;
		private String message;
		private Map<String, Object> result;

		public ApiResult(int code, String message) {
			this.code = code;
			this.message = message;
		}

		public ApiResult(int code, String message, Map<String, Object> result) {
			this.code = code;
			this.message = message;
			this.result = result;
		}
	}

	private class ApiInvokeException extends RuntimeException {
		private int code;
		private String message;
		private Object data;

		public ApiInvokeException(int code, String message) {
			this(code, message, null);
		}

		public ApiInvokeException(int code, String message, Object data) {
			this.code = code;
			this.message = message;
			this.data = data;
		}

		public int getCode() {
			return code;
		}

		@Override
		public String getMessage() {
			return message;
		}

		public Object getData() {
			return data;
		}
	}

	@Test
	public void retrieve_exceptionControl_byBody() {
		String successBody = "{\"code\":0,\"message\":\"success\",\"result\":{\"usedSpace\":1091044578188}}";
		String errorBody = "{ \"code\":35, \"message\":\"User Infomation Not Found\" }";
		mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()).setBody(errorBody));
		WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();

		Mono<Map<String,Object>> invokeResult = webClient.get().retrieve().bodyToMono(ApiResult.class).flatMap(apiResult -> {
			if(apiResult.code != 0) {
				return Mono.error(new ApiInvokeException(apiResult.code, apiResult.message));
			} else {
				return Mono.just(apiResult.result);
			}
		});
	}

	@Test
	public void retrieve_exceptionControl_byStatusCode() {
		mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).setBody("value"));
		WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();

		ApiInvokeException expectExcention = new ApiInvokeException(0, "CustomError");
		Mono<String> invokeResult = webClient.get()
				.retrieve()
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(expectExcention)
				).bodyToMono(String.class);

		ApiInvokeException exception = null;
		try {
			invokeResult.block();
		} catch (ApiInvokeException e) {
			exception = e;
		}

		assertNotNull(exception);
		assertEquals(exception.getMessage(), expectExcention.getMessage());
		assertEquals(exception.getCode(), expectExcention.getCode());
	}

	@Test
	public void exchange_exceptionControl() {
		mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).setBody("value"));
		WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();

		Mono<Map<String, Object>> invokeResult = webClient.get()
				.exchangeToMono(clientResponse -> {
					if(clientResponse.statusCode().is4xxClientError()) {
						return Mono.error(new InvalidParameterException());
					} else if(clientResponse.statusCode().is5xxServerError()) {
						return Mono.error(new ApiInvokeException(clientResponse.statusCode().value(), clientResponse.statusCode().getReasonPhrase()));
					}
					return clientResponse.bodyToMono(ApiResult.class)
							.flatMap(apiResult -> {
								if(apiResult.code != 0) {
									return Mono.error(new ApiInvokeException(apiResult.code, apiResult.message));
								} else {
									return Mono.just(apiResult.result);
								}
							});
				});
	}

	@Test
	public void body() throws InterruptedException {
		Mono<String> bodyMono = Mono.just("body");
		mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()).setBody("value"));
		WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
		webClient.post().body(bodyMono, String.class).retrieve().bodyToMono(String.class).block();

		logger.info("Body : " + mockWebServer.takeRequest().getBody().readString(StandardCharsets.UTF_8));
	}

	@Test
	public void bodyValue() throws InterruptedException {
		String body = "body";
		mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()).setBody("value"));
		WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
			webClient.post().bodyValue(body).retrieve().bodyToMono(String.class).block();

		logger.info("Body : " + mockWebServer.takeRequest().getBody().readString(StandardCharsets.UTF_8));
	}

	@Test
	public void bodyInserter() throws InterruptedException {
		String body = "body";
		mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()).setBody("value"));
		WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();

		MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<>();
		multiValueMap.add("foo", "bar");

		MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("fieldPart", "fieldValue", MediaType.TEXT_PLAIN);
		multipartBodyBuilder.part("filePart1", new FileSystemResource("...logo.png"), MediaType.IMAGE_PNG);

		webClient.post()
//				.body(BodyInserters.empty())
//				.body(BodyInserters.fromValue(body))
//				.body(BodyInserters.fromPublisher(Mono.just(body), String.class))
//				.body(BodyInserters.fromResource(new ClassPathResource("test.txt")))
//				.body(BodyInserters.fromFormData(multiValueMap))
//				.body(BodyInserters.fromFormData("foo", "bar").with("foo2", "bar2"))
//				.body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
				.body(BodyInserters.fromMultipartData("fieldPart", "fieldValue").with("filePart", new FileSystemResource("...logo.png")))
				.retrieve()
				.bodyToMono(String.class)
				.block();

		logger.info("#### Body : " + mockWebServer.takeRequest().getBody().readString(StandardCharsets.UTF_8));
	}

	@Test
	public void toStreamTest() {
		mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()).setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody("[\"1\",\"2\",\"3\"]"));
		WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
		Stream<Integer> result_1 = webClient.get()
				.retrieve()
				.bodyToFlux(Integer.class)
				.log()
				.toStream();

		System.out.println("doing");

		result_1.collect(Collectors.toList()).stream().forEach(System.out::println);
	}
}
