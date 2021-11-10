package com.jisoooh.bulletinboard.bo;

import com.jisoooh.bulletinboard.dao.ArticleDao;
import com.jisoooh.bulletinboard.model.Article;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ArticleBo {
	private final int pageLimit;
	private final ArticleDao articleDao;

	public ArticleBo(int pageLimit, ArticleDao articleDao) {
		this.pageLimit = pageLimit;
		this.articleDao = articleDao;
	}

	public Mono<Article> get(Mono<Integer> articleNoMono) {
		return articleDao.get(articleNoMono);
	}

	public Flux<Article> getList(Mono<Integer> pageNumMono) {
		return pageNumMono.map(pageNum -> (long)(pageNum - 1) * pageLimit)
				.as(pageNumM -> Mono.zip(pageNumM, Mono.just(pageLimit)))
				.as(tuple2Mono -> articleDao.getList(tuple2Mono));
	}

	public Mono<Void> add(Mono<Article> articleMono) {
		return articleDao.add(articleMono);
	}

	public Mono<Void> update(Mono<Article> articleMono) {
		return articleDao.update(articleMono);
	}

	public Mono<Void> delete(Mono<Integer> articleNoMono) {
		return articleDao.delete(articleNoMono);
	}
}
