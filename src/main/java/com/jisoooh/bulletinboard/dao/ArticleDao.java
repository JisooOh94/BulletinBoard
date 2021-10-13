package com.jisoooh.bulletinboard.dao;

import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jisoooh.bulletinboard.model.Article;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class ArticleDao {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final DatabaseClient databaseClient;
	private final ObjectMapper objectMapper;

	public ArticleDao(DatabaseClient databaseClient, ObjectMapper objectMapper) {
		this.databaseClient = databaseClient;
		this.objectMapper = objectMapper;
	}

	public Mono<Article> get(Mono<Integer> articleNoMono) {
		return articleNoMono
				.switchIfEmpty(Mono.error(new InvalidParameterException()))
				.flatMap(articleNo ->
						databaseClient
								.sql("SELECT * FROM article WHERE no = :articleNo")
								.bind("articleNo", articleNo)
								.fetch()
								.one()
								.map(queryResult -> objectMapper.convertValue(queryResult, Article.class))
				);
	}

	public Flux<Article> getList(Mono<Tuple2<Long, Integer>> tuple) {
		return tuple
				.switchIfEmpty(Mono.error(new InvalidParameterException()))
				.flatMapMany(tuple2 ->
						databaseClient
								.sql("SELECT * FROM article LIMIT :limit OFFSET :offset")
								.bind("offset", tuple2.getT1())
								.bind("limit", tuple2.getT2())
								.fetch()
								.all()
								.map(queryResult -> objectMapper.convertValue(queryResult, Article.class))
				);
	}

	public Mono<Void> add(Mono<Article> articleMono) {
		return articleMono
				.switchIfEmpty(Mono.error(new InvalidParameterException()))
				.flatMap(article ->
						databaseClient
								.sql("INSERT INTO article (title, content, registYmdt, modifyYmdt) VALUES (:title, :content, :registYmdt, :modifyYmdt)")
								.bind("title", article.getTitle())
								.bind("content", article.getContent())
								.bind("registYmdt", article.getRegistYmdt())
								.bind("modifyYmdt", article.getModifyYmdt())
								.fetch()
								.rowsUpdated()
								.then()
				);
	}

	public Mono<Void> update(Mono<Article> articleMono) {
		return articleMono
				.switchIfEmpty(Mono.error(new InvalidParameterException()))
				.flatMap(article ->
						databaseClient
								.sql("UPDATE article SET title = :title, content = :content, modifyYmdt = :modifyYmdt WHERE no = :articleNo")
								.bind("title", article.getTitle())
								.bind("content", article.getContent())
								.bind("modifyYmdt", article.getModifyYmdt())
								.bind("articleNo", article.getNo())
								.fetch()
								.rowsUpdated()
								.then()
				);
	}

	public Mono<Void> delete(Mono<Integer> articleNoMono) {
		return articleNoMono
				.switchIfEmpty(Mono.error(new InvalidParameterException()))
				.flatMap(articleNo ->
						databaseClient
								.sql("DELETE FROM article WHERE no = :articleNo")
								.bind("articleNo", articleNo)
								.fetch()
								.rowsUpdated()
								.then()
				);
	}
}
