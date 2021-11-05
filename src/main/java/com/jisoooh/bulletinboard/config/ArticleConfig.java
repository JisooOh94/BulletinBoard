package com.jisoooh.bulletinboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jisoooh.bulletinboard.bo.ArticleBo;
import com.jisoooh.bulletinboard.dao.ArticleDao;

@Configuration
public class ArticleConfig {
	@Value("${article.pageLimit}")
	private int pageLimit;

	@Bean
	public ArticleDao articleDao(R2dbcEntityTemplate r2dbcEntityTemplate) {
		return new ArticleDao(r2dbcEntityTemplate);
	}

	@Bean
	public ArticleBo articleBo(ArticleDao articleDao) {
		return new ArticleBo(pageLimit, articleDao);
	}
}
