package jp.co.sss.shop.controller.client.basket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import jp.co.sss.shop.util.Constant;

/**
 * 買い物かご画面のコントローラークラス
 */
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
		
		// 現在のかごの中身を取得
		List<BasketBean> basketList = (List<BasketBean>) session.getAttribute("basketBeans");
		
		// 画面表示用の商品リスト
		List<Map<String, Object>> basketItems = new ArrayList<Map<String, Object>>();
		int basketTotal = 0;
		int basketCount = 0;
		
		// かごがあり、中身もある場合の処理
		if (basketList != null) {
            if (basketList.isEmpty() == false) {
            	
            	// 在庫切れの商品名を入れるリスト
                List<String> itemNameListZero = new ArrayList<String>();
                
                // 在庫不足の商品名を入れるリスト
                List<String> itemNameListLessThan = new ArrayList<String>();

                // かごの中身を1件ずつチェック
                for (BasketBean basketItem : basketList) {
                	
                	// チェックしてる商品のIDから商品の情報を取得
                    Item item = itemRepository.findByIdAndDeleteFlag(basketItem.getId(), Constant.NOT_DELETED);
                    
                    // 商品が存在している場合
                    if (item != null) {
                    	
                    	// 在庫数の最新情報を入れる
                        basketItem.setStock(item.getStock());
                        
                        Integer price = item.getPrice() != null ? item.getPrice() : 0;
                        Integer orderNum = basketItem.getOrderNum() != null ? basketItem.getOrderNum() : 0;
                        Integer subtotal = price * orderNum;
                        
                        Map<String, Object> viewItem = new HashMap<String, Object>();
                        viewItem.put("id", item.getId());
                        viewItem.put("name", item.getName());
                        viewItem.put("image", item.getImage());
                        viewItem.put("categoryName", item.getCategory() != null ? item.getCategory().getName() : "");
                        viewItem.put("description", item.getDescription());
                        viewItem.put("price", price);
                        viewItem.put("orderNum", orderNum);
                        viewItem.put("subtotal", subtotal);
                        viewItem.put("stock", item.getStock());
                        basketItems.add(viewItem);
                        
                        basketTotal += subtotal;
                        basketCount += orderNum;
                        
                        // 今チェックしている商品の在庫が0の場合、在庫切れリストに商品名を入れる
                        if (item.getStock() == 0) {
                            itemNameListZero.add(basketItem.getName());
                        
                        // 今チェックしてる商品が、在庫より注文数の方が多い場合、在庫不足リストに商品名を入れる。
                        } else if (item.getStock() < basketItem.getOrderNum()) {
                            itemNameListLessThan.add(basketItem.getName());
                        }
                    }
                }
                
                // 在庫切れリストに商品名が入っている場合、在庫切れリストをリクエストスコープに保存
                if (itemNameListZero.isEmpty() == false) {
                    model.addAttribute("itemNameListZero", itemNameListZero);
                }
                
                // 在庫不足リストに商品名が入っている場合、在庫不足リストをリクエストスコープに保存
                if (itemNameListLessThan.isEmpty() == false) {
                    model.addAttribute("itemNameListLessThan", itemNameListLessThan);
                }
            }
        }
		
		model.addAttribute("basketItems", basketItems);
		model.addAttribute("basketTotal", basketTotal);
		model.addAttribute("basketCount", basketCount);
		
		// 買い物かご画面へ遷移
		return "client/basket/list";
	}
	
	/**
	 * 商品をかごに追加する処理
	 * @param id
	 * @return
	 */
	@RequestMapping(path = "/client/basket/add", method = RequestMethod.POST)
	public String addBasketItem(@RequestParam Integer id) {
		
		// 現在のかごの中身を取得
		List<BasketBean> basketList = (List<BasketBean>) session.getAttribute("basketBeans");
		
		// かごに入れるボタンを押したときに送られたIDの商品の情報を取得
		Item item = itemRepository.getReferenceById(id);
		
		// かごをまだ持っていない場合、空のかごを作成
		if (basketList == null) {
			basketList = new ArrayList<BasketBean>();
		}
		
		// 既にかごに存在するかチェック(true → 存在する, false → 存在しない)
		boolean isExisting = false;
		
		// かごの中身を1件ずつチェック
		for (BasketBean basketItem : basketList) {
			
			// かごに入れるボタンを押したときに送られたIDが、チェックしてる商品のIDと一致した場合、注文数を1増やし、isExistingをtrueに
			if (basketItem.getId().equals(id)) {
				basketItem.setOrderNum(basketItem.getOrderNum() + 1);
				isExisting = true;
				break;
			}
		}
		
		// チェックし終えた結果、かごにない商品をかごに入れる場合、商品の情報を入れる。
		if (isExisting == false) {
			BasketBean basketItem = new BasketBean();
			basketItem.setId(item.getId());
			basketItem.setName(item.getName());
			basketItem.setOrderNum(1);
			basketItem.setStock(item.getStock());
			
			basketList.add(basketItem);
		}
		
		// かごの中身をセッションに保存し、買い物かご画面にリダイレクト
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
		
		// 現在のかごの中身を取得
		List<BasketBean> basketList = (List<BasketBean>) session.getAttribute("basketBeans");
		
		// かごがある場合、かごの中身を1件ずつチェック
		if (basketList != null) {
			for (int i = 0; i < basketList.size(); i++) {
				
				// 現在チェックしている商品のデータを取得
                BasketBean basketItem = basketList.get(i);
                
                // 削除ボタンを押したときに送られたIDが、チェックしてる商品のIDと一致した場合の処理
                if (basketItem.getId().equals(id)) {
                	
                	// チェックしてる商品の注文数が1の場合、その商品をかごから削除
                	if (basketItem.getOrderNum() == 1) {
                    	basketList.remove(i);
                    
                    // チェックしてる商品の注文数が1でない場合、その商品の注文数を1減らす
                    } else {
                    	basketItem.setOrderNum(basketItem.getOrderNum() - 1);
                    }
                    break;
                }
            }
			
			// 商品をかごから削除することによって、かごが空になった場合はセッションを破棄
	        if (basketList.isEmpty()) {
	            session.removeAttribute("basketBeans");
	        
	        // 空でない場合、かごの中身をセッションスコープに保存
	        } else {
	            session.setAttribute("basketBeans", basketList);
	        }
		}
		
		// 買い物かご画面にリダイレクト
        return "redirect:/client/basket/list";
	}
	
	/**
	 * かごの中身全部削除
	 * @return
	 */
    @RequestMapping(path = "/client/basket/allDelete", method = RequestMethod.POST)
    public String allDeleteBasketItem() {
    	
    	// セッション(かご)を破棄し、買い物かご画面にリダイレクト
        session.removeAttribute("basketBeans");
        return "redirect:/client/basket/list";
    }
}