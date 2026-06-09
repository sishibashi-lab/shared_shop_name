package jp.co.sss.shop.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.entity.Favorite;
import jp.co.sss.shop.repository.FavoriteRepository;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.repository.UserRepository;

@Controller
public class FavoriteController {
	@Autowired
	FavoriteRepository favoriteRepository;
	
	@Autowired
	ItemRepository itemRepository;

	@Autowired
	UserRepository userRepository;
	
	//お気に入り一覧表示
	@GetMapping("/favorite/list")
    public String list(Model model,HttpSession session) {
        model.addAttribute("message", "TODO: FAVORITESテーブルからお気に入り商品を取得して表示してください。");
        Integer userId=(Integer)session.getAttribute("loginUserId");
        model.addAttribute("favoriteItems", favoriteRepository.findByUserIdAndDeleteFlagOrderByFavoriteDateDesc(userId, 0));
        return "/client/favorite/list";
    }
	
	//お気に入り登録/削除---商品一覧
    @PostMapping("/list/favorite/regist/{id}")
    public String addItemList(@RequestParam("id") Integer id, RedirectAttributes redirectAttributes,HttpSession session) {
        redirectAttributes.addFlashAttribute("message", "TODO: 商品ID「" + id + "」をFAVORITESテーブルへ登録してください。");
        Integer userId=(Integer) session.getAttribute("loginUserId");

        Favorite favoriteItem = favoriteRepository.findByUserIdAndItemId(userId, id);

		if (favoriteItem == null) {

			Favorite favorite = new Favorite();
			Favorite maxId = favoriteRepository.findFirstByOrderByIdDesc();

			if (maxId == null) {
				favorite.setId(1);
			} else {
				favorite.setId(maxId.getId() + 1);
			}

			favorite.setDeleteFlag(0);
			favorite.setUser(userRepository.getReferenceById(userId));
			favorite.setItem(itemRepository.getReferenceById(id));
			favorite.setFavoriteDate(new Date());
			favorite = favoriteRepository.save(favorite);

		} else if (favoriteItem.getDeleteFlag() == 1) {

			favoriteItem.setDeleteFlag(0);
			favoriteItem = favoriteRepository.save(favoriteItem);

		} else if (favoriteItem.getDeleteFlag() == 0) {

			favoriteItem.setDeleteFlag(1);
			favoriteItem = favoriteRepository.save(favoriteItem);
		}
		
        return "redirect:/client/item/list";
    }

	//お気に入り登録/削除---商品一覧
    @PostMapping("/detail/favorite/regist/{id}")
    public String addItemDetail(@RequestParam("id") Integer id, RedirectAttributes redirectAttributes,HttpSession session) {
        redirectAttributes.addFlashAttribute("message", "TODO: 商品ID「" + id + "」をFAVORITESテーブルへ登録してください。");
        Integer userId=(Integer) session.getAttribute("loginUserId");

        Favorite favoriteItem = favoriteRepository.findByUserIdAndItemId(userId, id);

		if (favoriteItem == null) {

			Favorite favorite = new Favorite();
			Favorite maxId = favoriteRepository.findFirstByOrderByIdDesc();

			if (maxId == null) {
				favorite.setId(1);
			} else {
				favorite.setId(maxId.getId() + 1);
			}

			favorite.setDeleteFlag(0);
			favorite.setUser(userRepository.getReferenceById(userId));
			favorite.setItem(itemRepository.getReferenceById(id));
			favorite.setFavoriteDate(new Date());
			favorite = favoriteRepository.save(favorite);

		} else if (favoriteItem.getDeleteFlag() == 1) {

			favoriteItem.setDeleteFlag(0);
			favoriteItem = favoriteRepository.save(favoriteItem);

		} else if (favoriteItem.getDeleteFlag() == 0) {

			favoriteItem.setDeleteFlag(1);
			favoriteItem = favoriteRepository.save(favoriteItem);
		}
		
        return "redirect:/client/item/detail";
    }
    
	//お気に入り登録/削除---トップ
    @PostMapping("/index/favorite/regist/{id}")
    public String addItemTop(@RequestParam("id") Integer id, RedirectAttributes redirectAttributes,HttpSession session) {
        redirectAttributes.addFlashAttribute("message", "TODO: 商品ID「" + id + "」をFAVORITESテーブルへ登録してください。");
        Integer userId=(Integer) session.getAttribute("loginUserId");

        Favorite favoriteItem = favoriteRepository.findByUserIdAndItemId(userId, id);

		if (favoriteItem == null) {

			Favorite favorite = new Favorite();
			Favorite maxId = favoriteRepository.findFirstByOrderByIdDesc();

			if (maxId == null) {
				favorite.setId(1);
			} else {
				favorite.setId(maxId.getId() + 1);
			}

			favorite.setDeleteFlag(0);
			favorite.setUser(userRepository.getReferenceById(userId));
			favorite.setItem(itemRepository.getReferenceById(id));
			favorite.setFavoriteDate(new Date());
			favorite = favoriteRepository.save(favorite);

		} else if (favoriteItem.getDeleteFlag() == 1) {

			favoriteItem.setDeleteFlag(0);
			favoriteItem = favoriteRepository.save(favoriteItem);

		} else if (favoriteItem.getDeleteFlag() == 0) {

			favoriteItem.setDeleteFlag(1);
			favoriteItem = favoriteRepository.save(favoriteItem);
		}
		
        return "redirect:/";
    }
   
    //お気に入り削除
    @PostMapping("/favorite/delete")
    public String delete(@RequestParam("id") Integer id, RedirectAttributes redirectAttributes,HttpSession session) {
        redirectAttributes.addFlashAttribute("message", "TODO: 商品ID「" + id + "」をFAVORITESテーブルから削除してください。");
        Integer userId=(Integer) session.getAttribute("loginUserId");

        boolean isItemId = favoriteRepository.existsById(id);
        if (isItemId) {
			List<Favorite> favorite = favoriteRepository.findByItemId(id);
			favoriteRepository.deleteAll(favorite);
		}
        
        return "redirect:/client/favorite/list";
    }
}
