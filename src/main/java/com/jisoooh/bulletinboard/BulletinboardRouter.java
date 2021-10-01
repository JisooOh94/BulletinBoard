package com.jisoooh.bulletinboard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class BulletinboardRouter {
	@Bean
	public RouterFunction<ServerResponse> route(BulletinboardHandler bulletinBoardHandler) {
		return RouterFunctions.route().path("/api/v1", builder -> builder
				.POST("/create", bulletinBoardHandler::create)
				.GET("/get", bulletinBoardHandler::get)
				.GET("/list", bulletinBoardHandler::getList)
				.PUT("/update", bulletinBoardHandler::update)
				.DELETE("/delete", bulletinBoardHandler::delete)
		).build();
	}
}
