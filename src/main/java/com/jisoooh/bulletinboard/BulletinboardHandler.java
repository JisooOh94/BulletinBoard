package com.jisoooh.bulletinboard;

import java.security.InvalidParameterException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import com.jisoooh.bulletinboard.bo.ArticleBo;
import com.jisoooh.bulletinboard.model.Article;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class BulletinboardHandler {
	@Autowired
	public ArticleBo articleBo;

	public Mono<ServerResponse> create(ServerRequest request) {
		return request.bodyToMono(String.class).flatMap(body -> {
			articleBo.add(body);
			return ServerResponse.ok().build();
		});
	}

	public Mono<ServerResponse> get(ServerRequest request) {
		int articleNo = Integer.parseInt(request.queryParam("articleNo").orElseThrow(() -> new InvalidParameterException()));
		return ServerResponse.ok().body(BodyInserters.fromPublisher(articleBo.get(articleNo), Article.class));
	}

	public Mono<ServerResponse> getList(ServerRequest request) {
		int pageNum = Integer.parseInt(request.queryParam("pageNum").orElseThrow(() -> new InvalidParameterException()));
		Flux<Article> articleList = articleBo.getList(pageNum);

		return ServerResponse.ok().body(BodyInserters.fromPublisher(articleList, Article.class));
	}

	public Mono<ServerResponse> update(ServerRequest request) {
		return request.bodyToMono(String.class)
				.flatMap(body -> {
					articleBo.update(body);
					return ServerResponse.ok().build();
				});
	}

	public Mono<ServerResponse> delete(ServerRequest request) {
		int articleNo = Integer.parseInt(request.queryParam("articleNo").orElseThrow(() -> new InvalidParameterException()));
		return Mono.just(articleNo).flatMap(no -> {
			articleBo.delete(no);
			return ServerResponse.ok().build();
		});
	}
}
