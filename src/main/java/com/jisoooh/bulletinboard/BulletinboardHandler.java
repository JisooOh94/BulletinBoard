package com.jisoooh.bulletinboard;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import com.jisoooh.bulletinboard.model.BulletinBoardModel;
import reactor.core.publisher.Mono;

@Component
public class BulletinboardHandler {
	private static Logger logger = LoggerFactory.getLogger(BulletinBoardModel.class);

	public Mono<ServerResponse> create(ServerRequest request) {
		return ServerResponse.ok().render("create");
	}

	public Mono<ServerResponse> save(ServerRequest request) {
		return request.bodyToMono(String.class).flatMap(body -> ServerResponse.ok().render("list"));
	}

	public Mono<ServerResponse> read(ServerRequest request) {
		return ServerResponse.ok().render("index");
	}

	public Mono<ServerResponse> list(ServerRequest request) {
		return ServerResponse.ok().render("list");
	}

	public Mono<ServerResponse> update(ServerRequest request) {
		Map<String, Object> params = new HashMap<>();
		params.put("param", "value");
		return ServerResponse.ok().render("index", params);
	}

	public Mono<ServerResponse> delete(ServerRequest request) {
		Map<String, Object> params = new HashMap<>();
		params.put("param", "value");
		return ServerResponse.ok().render("index", params);
	}
}
