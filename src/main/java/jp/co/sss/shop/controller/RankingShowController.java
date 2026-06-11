package jp.co.sss.shop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jp.co.sss.shop.repository.ItemRepository;

@Controller
public class RankingShowController {
	@Autowired
    private  ItemRepository itemRepository;
	
	@GetMapping("/client/ranking/list")
	public String ranking(Model model) {
		model.addAttribute("viewCountRanking",itemRepository.findByViewCountDesc());
		List<Integer> count=itemRepository.findViewCount();
		model.addAttribute("viewCount",count);
		model.addAttribute("favoriteCountRanking",itemRepository.findByFavoriteCountDesc());
		count=itemRepository.findFavoriteCount();
		model.addAttribute("favoriteCount",count);

		// レビューランキングは今後の接続用に空のモデルだけ用意
		model.addAttribute("reviewCountRanking", java.util.Collections.emptyList());
		model.addAttribute("reviewCount", java.util.Collections.emptyList());
		
		return "client/ranking/list";
		
	}

}
