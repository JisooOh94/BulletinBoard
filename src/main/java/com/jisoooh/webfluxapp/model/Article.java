package com.jisoooh.webfluxapp.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Article {
	public static final String NO = "no";
	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	public static final String REGIST_YMDT = "registYmdt";
	public static final String MODIFY_YMDT = "modifyYmdt";

	@Id
	private Long no;
	private String title;
	private String content;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonProperty("regist_ymdt")
	private LocalDateTime registYmdt;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonProperty("modify_ymdt")
	private LocalDateTime modifyYmdt;

	public Long getNo() {
		return no;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public LocalDateTime getRegistYmdt() {
		return registYmdt;
	}

	public LocalDateTime getModifyYmdt() {
		return modifyYmdt;
	}
}
