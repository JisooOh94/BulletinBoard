package com.jisoooh.webfluxapp.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jisoooh.webfluxapp.bo.ArticleBo;
import com.jisoooh.webfluxapp.model.Article;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ArticleController {
	@Autowired
	public ArticleBo articleBo;

	@PostMapping("/api/v1/create")
	public Mono<Void> create(@RequestBody Mono<Article> articleMono) {
		return articleBo.add(articleMono);
	}

	@GetMapping("/api/v1/get")
	public Mono<Article> get(@RequestParam int articleNo) {
		return articleBo.get(Mono.just(articleNo));
	}

	@GetMapping("/api/v1/list")
	public Flux<Article> getList(@RequestParam(defaultValue = "1") int pageNum) {
		return articleBo.getList(Mono.just(pageNum));
	}

	@PutMapping("/api/v1/update")
	public Mono<Void> update(@RequestBody Mono<Article> articleMono) {
		return articleBo.update(articleMono);
	}

	@DeleteMapping("/api/v1/delete")
	public Mono<Void> delete(@RequestParam int articleNo) {
		return articleBo.delete(Mono.just(articleNo));
	}
}
