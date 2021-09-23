package com.jisoooh.bulletinboard;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class BulletinboardRouter {
	@Bean
	public RouterFunction<ServerResponse> route(BulletinboardHandler bulletinBoardHandler) {
		return RouterFunctions.route()
				.GET("/create", bulletinBoardHandler::create)
				.POST("/save", bulletinBoardHandler::save)
				.GET("/read", accept(TEXT_HTML), bulletinBoardHandler::read)
				.GET("/list", bulletinBoardHandler::list)
				.PUT("/update", contentType(TEXT_HTML), bulletinBoardHandler::update)
				.DELETE("/delete", contentType(TEXT_HTML), bulletinBoardHandler::delete)
				.build();
	}
}
