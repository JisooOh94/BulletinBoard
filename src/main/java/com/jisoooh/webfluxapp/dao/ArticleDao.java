package com.jisoooh.webfluxapp.dao;

import static com.jisoooh.webfluxapp.util.ParsingUtil.parse;
import static com.jisoooh.webfluxapp.util.RetryUtil.*;
import static java.time.Duration.*;
import static org.springframework.data.relational.core.query.Criteria.*;
import static org.springframework.data.relational.core.query.Query.*;

import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import com.jisoooh.webfluxapp.model.Article;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.retry.RetryBackoffSpec;

public class ArticleDao {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final R2dbcEntityTemplate r2dbcEntityTemplate;
	private final RetryBackoffSpec retryCondition;

	public ArticleDao(R2dbcEntityTemplate r2dbcEntityTemplate) {
		this.r2dbcEntityTemplate = r2dbcEntityTemplate;
		retryCondition = getRetryCondition(3, ofSeconds(2), "# ArticleDao error. Query execute failed.", DataAccessException.class);
	}

	public Mono<Article> get(Mono<Integer> articleNoMono) {
		return articleNoMono
				.switchIfEmpty(Mono.error(new InvalidParameterException("# ArticleDao.get error. articleNoMono is empty")))
				.flatMap(articleNo -> r2dbcEntityTemplate.select(Article.class)
							.from("article")
							.matching(query(where("no").is(articleNo))).first()
				).retryWhen(retryCondition)
				.onErrorResume(throwable -> Mono.empty());
	}

	public Flux<Article> getList(Mono<Tuple2<Long, Integer>> tuple) {
		return tuple
				.switchIfEmpty(Mono.error(new InvalidParameterException("# ArticleDao.getList error. tuple is empty")))
				.flatMapMany(tuple2 -> r2dbcEntityTemplate.select(Article.class)
							.from("article")
							.matching(Query.empty()
									.offset(tuple2.getT1())
									.limit(tuple2.getT2())
							).all()
				).retryWhen(retryCondition)
				.map(queryResult -> parse(queryResult, Article.class))
				.onErrorResume(throwable -> Mono.empty());
	}

	public Mono<Void> add(Mono<Article> articleMono) {
		return articleMono
				.switchIfEmpty(Mono.error(new InvalidParameterException("# ArticleDao.add error. articleMono is empty")))
				.flatMap(article -> r2dbcEntityTemplate.insert(Article.class).into("article").using(article))
				.retryWhen(retryCondition)
				.doOnError(throwable -> logger.error("# ArticleDao.add error.", throwable))
				.onErrorResume(throwable -> Mono.empty())
				.as(queryResult -> queryResult.then());
	}

	public Mono<Void> update(Mono<Article> articleMono) {
		return articleMono
				.switchIfEmpty(Mono.error(new InvalidParameterException()))
				.flatMap(article -> r2dbcEntityTemplate.update(Article.class)
						.inTable("article")
						.matching(query(where("no").is(article.getNo())))
						.apply(Update.update(Article.TITLE, article.getTitle())
								.set(Article.CONTENT, article.getContent())
								.set(Article.MODIFY_YMDT, article.getModifyYmdt()))
				).retryWhen(retryCondition)
				.doOnError(throwable -> logger.error("# ArticleDao.update error.", throwable))
				.onErrorResume(throwable -> Mono.empty())
				.as(queryResult -> queryResult.then());
	}

	public Mono<Void> delete(Mono<Integer> articleNoMono) {
		return articleNoMono
				.switchIfEmpty(Mono.error(new InvalidParameterException()))
				.flatMap(articleNo -> r2dbcEntityTemplate.delete(Article.class)
						.from("article")
						.matching(query(
								where("no").is(articleNo))
						).all()
				).retryWhen(retryCondition)
				.doOnError(throwable -> logger.error("# ArticleDao.delete error.", throwable))
				.onErrorResume(throwable -> Mono.empty())
				.as(queryResult -> queryResult.then());
	}
}
