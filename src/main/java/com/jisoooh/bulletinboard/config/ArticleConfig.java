package com.jisoooh.bulletinboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jisoooh.bulletinboard.bo.ArticleBo;
import com.jisoooh.bulletinboard.dao.ArticleDao;

@Configuration
public class ArticleConfig {
	@Value("${article.pageLimit}")
	private int pageLimit;

	@Bean
	public ArticleDao articleDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate, ObjectMapper objectMapper) {
		return new ArticleDao(namedParameterJdbcTemplate, objectMapper);
	}

	@Bean
	public ArticleBo articleBo(ArticleDao articleDao, ObjectMapper objectMapper) {
		return new ArticleBo(pageLimit, articleDao, objectMapper);
	}
}
