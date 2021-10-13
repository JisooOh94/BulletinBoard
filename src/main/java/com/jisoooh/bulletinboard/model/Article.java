package com.jisoooh.bulletinboard.model;

import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Article {
	public static final String NO = "no";
	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	public static final String REGIST_YMDT = "registYmdt";
	public static final String MODIFY_YMDT = "modifyYmdt";

	private long no;
	private String title;
	private String content;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime registYmdt;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime modifyYmdt;

	public Article() {
	}

	public Article(String title, String content, LocalDateTime registYmdt, LocalDateTime modifyYmdt) {
		this.title = title;
		this.content = content;
		this.registYmdt = registYmdt;
		this.modifyYmdt = modifyYmdt;
	}

	public Article(long no, String title, String content, LocalDateTime registYmdt, LocalDateTime modifyYmdt) {
		this.no = no;
		this.title = title;
		this.content = content;
		this.registYmdt = registYmdt;
		this.modifyYmdt = modifyYmdt;
	}

	public long getNo() {
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
