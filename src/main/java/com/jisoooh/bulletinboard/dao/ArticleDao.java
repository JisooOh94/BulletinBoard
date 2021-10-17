package com.jisoooh.bulletinboard.dao;

import static com.jisoooh.bulletinboard.util.ParsingUtil.parse;
import static com.jisoooh.bulletinboard.util.RetryUtil.*;
import static java.time.Duration.*;

import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.r2dbc.core.DatabaseClient;
import com.jisoooh.bulletinboard.model.Article;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.retry.RetryBackoffSpec;

public class ArticleDao {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final DatabaseClient databaseClient;
	private final RetryBackoffSpec retryCondition;

	public ArticleDao(DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
		retryCondition = getRetryCondition(3, ofSeconds(2), "# ArticleDao error. Query execute failed.", DataAccessException.class);
	}

	public Mono<Article> get(Mono<Integer> articleNoMono) {
		return articleNoMono
				.switchIfEmpty(Mono.error(new InvalidParameterException("# ArticleDao.get error. articleNoMono is empty")))
				.flatMap(articleNo ->
						databaseClient
								.sql("SELECT * FROM article WHERE no = :articleNo")
								.bind("articleNo", articleNo)
								.fetch()
								.one()
				).retryWhen(retryCondition)
				.flatMap(queryResult -> parse(queryResult, Article.class, "# ArticleDao.get error. Query result parsing faile."))
				.onErrorResume(throwable -> Mono.empty());
	}

	public Flux<Article> getList(Mono<Tuple2<Long, Integer>> tuple) {
		return tuple
				.switchIfEmpty(Mono.error(new InvalidParameterException("# ArticleDao.getList error. tuple is empty")))
				.flatMapMany(tuple2 ->
						databaseClient
								.sql("SELECT * FROM article LIMIT :limit OFFSET :offset")
								.bind("offset", tuple2.getT1())
								.bind("limit", tuple2.getT2())
								.fetch()
								.all()
				).retryWhen(retryCondition)
				.flatMap(queryResult -> parse(queryResult, Article.class, "# ArticleDao.getList error. Query result parsing failed."))
				.onErrorResume(throwable -> Mono.empty());
	}

	public Mono<Void> add(Mono<Article> articleMono) {
		return articleMono
				.switchIfEmpty(Mono.error(new InvalidParameterException("# ArticleDao.add error. articleMono is empty")))
				.flatMap(article ->
						databaseClient
								.sql("INSERT INTO article (title, content, registYmdt, modifyYmdt) VALUES (:title, :content, :registYmdt, :modifyYmdt)")
								.bind("title", article.getTitle())
								.bind("content", article.getContent())
								.bind("registYmdt", article.getRegistYmdt())
								.bind("modifyYmdt", article.getModifyYmdt())
								.fetch()
								.rowsUpdated()
				).retryWhen(retryCondition)
				.doOnError(throwable -> logger.error("# ArticleDao.add error.", throwable))
				.onErrorResume(throwable -> Mono.empty())
				.as(queryResult -> queryResult.then());
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
				).retryWhen(retryCondition)
				.doOnError(throwable -> logger.error("# ArticleDao.update error.", throwable))
				.onErrorResume(throwable -> Mono.empty())
				.as(queryResult -> queryResult.then());
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
				).retryWhen(retryCondition)
				.doOnError(throwable -> logger.error("# ArticleDao.delete error.", throwable))
				.onErrorResume(throwable -> Mono.empty())
				.as(queryResult -> queryResult.then());
	}
}
