package com.jisoooh.bulletinboard.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import com.jisoooh.bulletinboard.bo.ArticleBo;
import com.jisoooh.bulletinboard.dao.ArticleDao;
import reactor.core.publisher.Flux;

@RestController
public class TestController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ArticleBo articleBo;
	@Autowired
	private ArticleDao articleDao;

	public static class FormParam {
		private String key_1;
		private String key_2;

		public String getKey_1() {
			return key_1;
		}

		public void setKey_1(String key_1) {
			this.key_1 = key_1;
		}

		public String getKey_2() {
			return key_2;
		}

		public void setKey_2(String key_2) {
			this.key_2 = key_2;
		}
	}

	public Flux<String> test() {
		return Flux.just("1","2","3")
				.doOnCancel(() -> logger.info("# Cancel called 1"))
				.map(val -> {
					logger.info("1");
					return Integer.parseInt(val);
				})
				.doOnCancel(() -> logger.info("# Cancel called 2"))
				.map(val -> {
					logger.info("2");
					return String.valueOf(val);
				})
				.doOnCancel(() -> logger.info("# Cancel called 3"));
	}
}
