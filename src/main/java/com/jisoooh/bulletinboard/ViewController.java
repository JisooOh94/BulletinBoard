package com.jisoooh.bulletinboard;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.jisoooh.bulletinboard.model.Article;

@Controller
public class ViewController {
	@Autowired
	private RestTemplate restTemplate;

	@GetMapping(value = "/create")
	public String createView() {
		return "create";
	}

	@GetMapping(value = "/list")
	public String listView(@RequestParam(defaultValue = "1") int pageNum, Model model) {
		String uri = UriComponentsBuilder.newInstance()
				.path("/list")
				.queryParam("pageNum", pageNum)
				.build(true)
				.toUriString();

		List<Article> articleList = restTemplate.getForObject(uri, List.class);

		model.addAttribute("articles", articleList);
		return "list";
	}

	@GetMapping(value = "/view")
	public String view(@RequestParam int articleNo, Model model) {
		model.addAttribute("article", getArticle(articleNo));
		return "view";
	}

	@GetMapping(value = "/update")
	public String update(@RequestParam int articleNo, Model model) {
		model.addAttribute("article", getArticle(articleNo));
		return "update";
	}

	private Article getArticle(int articleNo) {
		String uri = UriComponentsBuilder.newInstance()
				.path("/get")
				.queryParam("articleNo", articleNo)
				.build(true)
				.toUriString();

		return restTemplate.getForObject(uri, Article.class);
	}
}
