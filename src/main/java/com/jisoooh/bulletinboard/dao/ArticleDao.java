package com.jisoooh.bulletinboard.dao;

import static com.jisoooh.bulletinboard.model.Article.*;
import static com.jisoooh.bulletinboard.util.CommonConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jisoooh.bulletinboard.model.Article;

public class ArticleDao {
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final ObjectMapper objectMapper;

	public ArticleDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate, ObjectMapper objectMapper) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
		this.objectMapper = objectMapper;
	}

	public Article get(int articleNo) {
		String query = "SELECT * FROM article WHERE no = :articleNo";

		Map<String, Object> param = Collections.singletonMap("articleNo", articleNo);

		return namedParameterJdbcTemplate.queryForObject(query, param, (row, rowNum) -> new Article(row.getLong(NO), row.getString(TITLE), row.getString(CONTENT), row.getDate(REGIST_YMDT), row.getDate(MODIFY_YMDT)));
	}

	public List<Article> getList(long offset, int limit) {
		String query = "SELECT * FROM article LIMIT :limit OFFSET :offset";

		Map<String, Object> param = new HashMap<>();
		param.put(OFFSET, offset);
		param.put(LIMIT, limit);

		return namedParameterJdbcTemplate.query(query, param, (row, rowNum) -> new Article(row.getLong(NO), row.getString(TITLE), row.getString(CONTENT), row.getDate(REGIST_YMDT), row.getDate(MODIFY_YMDT)));
	}

	public void add(Article article) {
		String query = "INSERT INTO article (title, content, registYmdt, modifyYmdt) VALUES (:title, :content, :registYmdt, :modifyYmdt)";

		Map<String, Object> param = objectMapper.convertValue(article, Map.class);

		namedParameterJdbcTemplate.update(query, param);
	}

	public void update(Article article) {
		String query = "UPDATE article SET title = :title, content = :content, modifyYmdt = :modifyYmdt WHERE no = :no";

		Map<String, Object> param = objectMapper.convertValue(article, Map.class);

		namedParameterJdbcTemplate.update(query, param);
	}

	public void delete(int articleNo) {
		String query = "DELETE FROM article WHERE no = :articleNo";

		Map<String, Object> param = Collections.singletonMap("articleNo", articleNo);

		namedParameterJdbcTemplate.update(query, param);
	}
}
