package com.jisoooh.bulletinboard.model;

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
	private Date registYmdt;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date modifyYmdt;

	public Article() {
	}

	public Article(String title, String content, Date registYmdt, Date modifyYmdt) {
		this.title = title;
		this.content = content;
		this.registYmdt = registYmdt;
		this.modifyYmdt = modifyYmdt;
	}

	public Article(long no, String title, String content, Date registYmdt, Date modifyYmdt) {
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

	public Date getRegistYmdt() {
		return registYmdt;
	}

	public Date getModifyYmdt() {
		return modifyYmdt;
	}
}
