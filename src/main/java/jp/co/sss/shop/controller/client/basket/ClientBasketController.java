package jp.co.sss.shop.controller.client.basket;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.BasketBean;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.repository.ItemRepository;

@Controller
public class ClientBasketController {
	/**
	 * 商品情報
	 */
	@Autowired
	ItemRepository itemRepository;
	
	@Autowired
    HttpSession session;
	
	/**
	 * 買い物かご画面表示処理
	 * @param model
	 * @return
	 */
	@RequestMapping(path = "/client/basket/list", method = RequestMethod.GET)
	public String showBasket(Model model) {
		List<BasketBean> basketList = (List<BasketBean>) session.getAttribute("basketBeans");
		if (basketList != null) {
            if (basketList.isEmpty() == false) {
            	
                List<String> itemNameListZero = new ArrayList<String>();     //在庫切れ
                List<String> itemNameListLessThan = new ArrayList<String>(); //在庫不足

                for (BasketBean bean : basketList) {
                    Item item = itemRepository.getReferenceById(bean.getId());
                    if (item != null) {
                        bean.setStock(item.getStock());

                        if (item.getStock() == 0) {
                            itemNameListZero.add(bean.getName());
                        }else if (item.getStock() < bean.getOrderNum()) {
                            itemNameListLessThan.add(bean.getName());
                        }
                    }
                }

                if (itemNameListZero.isEmpty() == false) {
                    model.addAttribute("itemNameListZero", itemNameListZero);
                }
                if (itemNameListLessThan.isEmpty() == false) {
                    model.addAttribute("itemNameListLessThan", itemNameListLessThan);
                }
            }
        }
		return "client/basket/list";
	}
	
	/**
	 * 商品をかごに追加する処理
	 * @param id
	 * @param orderNum
	 * @return
	 */
	@RequestMapping(path = "/client/basket/add", method = RequestMethod.POST)
	public String addBasketItem(@RequestParam Integer id, @RequestParam Integer orderNum) {
		List<BasketBean> basketList = (List<BasketBean>) session.getAttribute("basketBeans");
		Item item = itemRepository.getReferenceById(id);
		if (basketList == null) {
			basketList = new ArrayList<BasketBean>();
		}
		boolean isExisting = false;   //既にかごに存在するかどうか
		for (BasketBean bean : basketList) {
			if (bean.getId().equals(id)) {
				bean.setOrderNum(bean.getOrderNum() + orderNum);
				isExisting = true;
				break;
			}
		}
		if (isExisting == false) {
			BasketBean newBean = new BasketBean();
			newBean.setId(item.getId());
			newBean.setName(item.getName());
			newBean.setOrderNum(orderNum);
			newBean.setStock(item.getStock());
			
			basketList.add(newBean);
		}
		session.setAttribute("basketBeans", basketList);
		return "redirect:/client/basket/list";
	}
	
	/**
	 * 削除ボタンで指定の商品の注文数を減らす
	 * @param id
	 * @return
	 */
	@RequestMapping(path = "/client/basket/delete", method = RequestMethod.POST)
	public String deleteBasketItem(@RequestParam Integer id) {
		List<BasketBean> basketList = (List<BasketBean>) session.getAttribute("basketBeans");
		if (basketList != null) {
			for (int i = 0; i < basketList.size(); i++) {
                BasketBean bean = basketList.get(i);
                if (bean.getId().equals(id)) {
                	if (bean.getOrderNum() == 1) {
                    	basketList.remove(i);
                    }else {
                    	bean.setOrderNum(bean.getOrderNum() - 1);
                    }
                    break;
                }
            }
			//削除することによってカゴが空になった場合
	        if (basketList.isEmpty()) {
	            session.removeAttribute("basketBeans");
	        } else {
	            session.setAttribute("basketBeans", basketList);
	        }
		}
        return "redirect:/client/basket/list";
	}
	
	/**
	 * かごの中身全部削除
	 * @return
	 */
    @RequestMapping(path = "/client/basket/allDelete", method = RequestMethod.POST)
    public String allDeleteBasketItem() {
        session.removeAttribute("basketBeans");
        return "redirect:/client/basket/list";
    }
}