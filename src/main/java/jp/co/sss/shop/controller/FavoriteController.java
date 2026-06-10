package jp.co.sss.shop.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.UserBean;
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
	@GetMapping({"/favorite/list", "/client/favorite/list"})
    public String list(Model model,HttpSession session) {
        UserBean user=(UserBean) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Favorite> favoriteItems = favoriteRepository.findByUserIdAndDeleteFlagOrderByFavoriteDateDesc(user.getId(), 0);
        model.addAttribute("favoriteItems", favoriteItems);
        model.addAttribute("favoriteCount", favoriteItems.size());
        return "/client/favorite/list";
    }
	
	//お気に入り登録/削除---商品一覧
    @PostMapping("/list/favorite/regist/{id}/{sortType}")
    public String addItemList(@PathVariable("id") Integer id,@PathVariable("sortType") Integer sortType, RedirectAttributes redirectAttributes,HttpSession session) {
        UserBean user=(UserBean) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Favorite favoriteItem = favoriteRepository.findByUserIdAndItemId(user.getId(), id);

		if (favoriteItem == null) {

			Favorite favorite = new Favorite();


			favorite.setDeleteFlag(0);
			favorite.setUser(userRepository.getReferenceById(user.getId()));
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
        return "redirect:/client/item/list/"+sortType;
    }

	//お気に入り登録/削除---商品一覧
    @PostMapping("/detail/favorite/regist/{id}")
    public String addItemDetail(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes,HttpSession session) {
        UserBean user=(UserBean) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Favorite favoriteItem = favoriteRepository.findByUserIdAndItemId(user.getId(), id);

		if (favoriteItem == null) {

			Favorite favorite = new Favorite();

			favorite.setDeleteFlag(0);
			favorite.setUser(userRepository.getReferenceById(user.getId()));
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
		
        return "redirect:/client/item/detail/"+id;
    }
    
	//お気に入り登録/削除---トップ
    @PostMapping("/index/favorite/regist/{id}")
    public String addItemTop(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes,HttpSession session) {
        UserBean user=(UserBean) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Favorite favoriteItem = favoriteRepository.findByUserIdAndItemId(user.getId(), id);

		if (favoriteItem == null) {

			Favorite favorite = new Favorite();
			favorite.setDeleteFlag(0);
			favorite.setUser(userRepository.getReferenceById(user.getId()));
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
        UserBean user=(UserBean) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Favorite favoriteItem = favoriteRepository.findByUserIdAndItemId(user.getId(), id);
        if (favoriteItem != null) {
            favoriteItem.setDeleteFlag(1);
            favoriteRepository.save(favoriteItem);
        }
        
        return "redirect:/favorite/list";
    }
}
