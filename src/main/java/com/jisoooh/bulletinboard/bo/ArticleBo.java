package com.jisoooh.bulletinboard.bo;

import java.security.InvalidParameterException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jisoooh.bulletinboard.dao.ArticleDao;
import com.jisoooh.bulletinboard.model.Article;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ArticleBo {
	private final int pageLimit;
	private final ArticleDao articleDao;
	private final ObjectMapper objectMapper;

	public ArticleBo(int pageLimit, ArticleDao articleDao, ObjectMapper objectMapper) {
		this.pageLimit = pageLimit;
		this.articleDao = articleDao;
		this.objectMapper = objectMapper;
	}

	public Mono<Article> get(int articleNo) {
		Mono<Article> articleMono = Mono.just(articleDao.get(articleNo));
		return articleMono;
	}

	public Flux<Article> getList(int pageNum) {
		Mono<List<Article>> listMono = Mono.just(articleDao.getList((pageNum - 1) * pageLimit, pageLimit));
		return listMono.flatMapMany(Flux::fromIterable);
	}

	public void add(String articleStr) {
		Article article = null;
		try {
			article = objectMapper.readValue(articleStr, Article.class);
		} catch (JsonProcessingException e) {
			throw new InvalidParameterException();
		}
		articleDao.add(article);
	}

	public void update(String articleStr) {
		Article article = null;
		try {
			article = objectMapper.readValue(articleStr, Article.class);
		} catch (JsonProcessingException e) {
			throw new InvalidParameterException();
		}
		articleDao.update(article);
	}

	public void delete(int articleNo) {
		articleDao.delete(articleNo);
	}
}
