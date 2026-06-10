package jp.co.sss.shop.controller.client.item;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.ItemBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Favorite;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.ViewHistory;
import jp.co.sss.shop.repository.FavoriteRepository;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.repository.ViewHistoryRepository;
import jp.co.sss.shop.service.BeanTools;
import jp.co.sss.shop.util.Constant;

/**
 * 商品管理 一覧表示機能(一般会員用)のコントローラクラス
 *
 * @author SystemShared
 */
@Controller
public class ClientItemShowController {
	/**
	 * 商品情報リポジトリ
	 */
	@Autowired
	ItemRepository itemRepository;

	/**
	 * Entity、Form、Bean間のデータコピーサービス
	 */
	@Autowired
	BeanTools beanTools;

	@Autowired
	FavoriteRepository favoriteRepository;
	
	@Autowired
	ViewHistoryRepository viewHistoryRepository;
	@Autowired
	UserRepository userRepository;

	/**
	 * トップ画面 表示処理
	 * URLパターン: /
	 *

	 * @param model   Viewとの値受渡し
	 * @param session セッション情報（検索キーワードのクリア用）
	 * @return "index" トップ画面
	 */
	@RequestMapping(path = "/", method = { RequestMethod.GET, RequestMethod.POST })
	public String index(Model model, HttpSession session) {

		// 【セッション初期化】トップ画面に戻った時は、前回の検索履歴（キーワード）を完全に消去する
		session.removeAttribute("searchKeyword");

		// リポジトリの売れ筋順メソッドを使用して、未削除の商品を全件取得
		List<Item> items = itemRepository.findListByPopular(Constant.NOT_DELETED);
		

		// ループ処理で1件ずつ確実にItemBeanへコピー
		List<ItemBean> itemBeans = new ArrayList<>();
		for (Item item : items) {
			itemBeans.add(beanTools.copyEntityToItemBean(item));
		}

		// 商品一覧画面のHTMLと互換性を持たせるため、同じ属性名 "items" でモデルに登録
		model.addAttribute("items", itemBeans);

		// クエリパラメータ（sortTypeやcategoryId）の初期値として、売れ筋順(2)・全件(0)を画面に送る
		model.addAttribute("sortType", 2);
		model.addAttribute("categoryId", 0);

		//お気に入りされているかどうか
		UserBean user = (UserBean) session.getAttribute("user");
		model.addAttribute("loginUser",user);
		if (user != null) {
			List<Favorite> favorite = favoriteRepository.findByUserIdAndDeleteFlagOrderByFavoriteDateDesc(user.getId(),0);
			List<Boolean> isFavorites = new ArrayList<>();
			for (Item item : items) {
				Boolean isFavorite = false;
				for (Favorite favo : favorite) {
					if (favo.getItem().getId() == item.getId()) {
						isFavorite = true;
					}

				}
				isFavorites.add(isFavorite);
			}
			model.addAttribute("isFavorite", isFavorites);
		}
		
		//閲覧履歴
		if(user != null) {
			List<ViewHistory> viewHistorys=viewHistoryRepository.findAllByUserIdOrderByViewDateDesc(user.getId());
			List<Favorite> favorite = favoriteRepository.findByUserIdAndDeleteFlagOrderByFavoriteDateDesc(user.getId(),0);
			List<Boolean> isFavorites = new ArrayList<>();
			for (ViewHistory viewHistory : viewHistorys) {
				Boolean isFavorite = false;
				for (Favorite favo : favorite) {
					if (favo.getItem().getId() == viewHistory.getItem().getId()) {
						isFavorite = true;
					}

				}
				isFavorites.add(isFavorite);
			}
			model.addAttribute("viewHistory",viewHistorys);
			model.addAttribute("isFavoView", isFavorites);
		}
		
		//おすすめ機能
		if(user != null) {
			ViewHistory latestView=viewHistoryRepository.findFirstByUserIdOrderByViewDateDesc(user.getId());
			List<Item> recommendItem=new ArrayList<>();
			if(latestView!=null) {
				Item latestCategory=itemRepository.getReferenceById(latestView.getItem().getId());

				List<ViewHistory> allView=viewHistoryRepository.findAll();
				List<Item> allRecommendItem=itemRepository.findAllByCategoryName(latestCategory.getCategory().getName());

				for(Item item:allRecommendItem) {
					boolean isViewItem=true;
					for(ViewHistory view:allView) {
						if(item.getId()==view.getItem().getId()) {
							isViewItem=false;
						}
						
						
					}
					if(isViewItem) {
						recommendItem.add(item);
					}
					
				}
				model.addAttribute("recommend", recommendItem);
			}
		}
		
		// 【ヘッダーエラー防止】ヘッダーの検索窓(th:value="${keyword}")でエラーが出ないよう空文字を送る
		model.addAttribute("keyword", "");
		
		return "index";
	}

	/**
	 * 商品一覧画面 表示処理（ヘッダー検索・売れ筋初期化・検索維持対応版）
	 *
	 * @param sortType   ソートタイプ (1:新着順, 2:売れ筋順)
	 * @param categoryId カテゴリID (任意、指定がない場合、または0の場合は全件)
	 * @param keyword    検索キーワード (ヘッダーの入力値をバインド)
	 * @param model      Viewとの値受渡し
	 * @param session    セッション情報（検索キーワードの保持・復元用）
	 * @return "client/item/list" 商品一覧画面
	 */
	@RequestMapping(path = "/client/item/list/{sortType}", method = { RequestMethod.GET, RequestMethod.POST })

	public String list(
			@PathVariable Integer sortType, 
			@RequestParam(name = "categoryId", required = false) Integer categoryId, 
			@RequestParam(name = "keyword", required = false) String keyword, 
			Model model,
			HttpSession session) {
		
		// キーワードの入力判定とセッション管理（検索状態の維持）
		if (keyword != null) {
			// 前後の不要なスペースを除去（トリミング）
			keyword = keyword.trim();
			
			if (keyword.isEmpty()) {
				// 文字列が空（スペースのみだった場合など）は検索なしとして扱う
				keyword = null;
				// 空文字での再検索時は、過去の検索履歴も消去する
				session.removeAttribute("searchKeyword"); 
			} else {
				// 新しいキーワードが入力された場合は、そのワードをセッションに上書き保存
				session.setAttribute("searchKeyword", keyword);
			}
		} else {
			// URLにkeywordが含まれていない場合（一覧のソートリンク等を押した場合）
			// セッションから「さっきまで検索していたワード」を復元して検索状態をキープする
			keyword = (String) session.getAttribute("searchKeyword");
		}

		// 安全対策：categoryIdの初期化（nullやマイナスの値なら全体「0」にする）
		if (categoryId == null || categoryId < 0) {
			categoryId = 0;
		}

		// 最終的に画面に表示するエンティティのリスト

		List<Item> finalItems = new ArrayList<>();

		// 条件に応じたデータベース検索の実行（クエリの分岐）
		if (keyword != null) {
			// パターンA: キーワード（または検索履歴）がある場合（あいまい検索）
			
			// SQLのLIKE検索用に前後に「%」を付与（例: "%りんご%"）
			String likeKeyword = "%" + keyword + "%";

			// 検索実行時はカテゴリ絞り込みを解除し、全体(0)として扱う
			categoryId = 0;

			if (sortType == 1) {

				// キーワードに一致する未削除商品を 【新着順】 で取得
				finalItems = itemRepository.findListByNewestAndKeyword(likeKeyword, Constant.NOT_DELETED);
			} else {
				// キーワードに一致する未削除商品を 【売れ筋順】 で取得（デフォルト）
				finalItems = itemRepository.findListByPopularAndKeyword(likeKeyword, Constant.NOT_DELETED);
				sortType = 2; // 安全のためソートタイプを2に補正
			}
		} 
		else {
			// パターンB: キーワードが一切ない場合（通常のカテゴリ選択や、メニューからの遷移）
			
			if (categoryId == 0) {
				// 全商品が対象の場合
				if (sortType == 1) {
					// 全商品を 【新着順】 で取得
					finalItems = itemRepository.findListByNewest(Constant.NOT_DELETED);
				} else {
					// 全商品を 【売れ筋順】 で取得
					finalItems = itemRepository.findListByPopular(Constant.NOT_DELETED);
					sortType = 2;
				}
			} else {
				// 特定のカテゴリが指定されている場合
				if (sortType == 1) {
					// 指定カテゴリの商品を 【新着順】 で取得
					finalItems = itemRepository.findLatestByCategory(categoryId, Constant.NOT_DELETED);
				} else if (sortType == 2) {
					// 指定カテゴリの商品を 【売れ筋順】 で取得
					finalItems = itemRepository.findPopularByCategory(categoryId, Constant.NOT_DELETED);
				} else {
					// 想定外のソート順の場合は、標準のカテゴリ検索（ID昇順）で取得
					finalItems = itemRepository.findActiveByCategoryId(categoryId, Constant.NOT_DELETED);
				}

			}
		}

		// 取得したデータを画面表示用Beanへ詰め替え
		List<ItemBean> itemBeans = new ArrayList<>();
		for (Item item : finalItems) {
			itemBeans.add(beanTools.copyEntityToItemBean(item));
		}

		// 画面に必要なデータを一式モデルに登録
		model.addAttribute("items", itemBeans);
		model.addAttribute("sortType", sortType);
		model.addAttribute("categoryId", categoryId);
		// nullのときは空文字を送ることで、Thymeleaf（HTML）のvalue属性でのエラーを完全に防ぐ
		model.addAttribute("keyword", keyword != null ? keyword : ""); 

		return "client/item/list";
	}

	/**
	 * 商品詳細画面 表示処理
	 * URLパターン: /client/item/detail/{id}
	 *
	 * @param id      表示する商品のID
	 * @param model   Viewとの値受渡し
	 * @param session セッション情報（ヘッダーの状態維持用）
	 * @return "client/item/detail" 商品詳細画面
	 */
	@RequestMapping(path = "/client/item/detail/{id}", method = { RequestMethod.GET, RequestMethod.POST })

	public String detail(@PathVariable Integer id, Model model, HttpSession session) {

		// リポジトリからIDを指定して未削除の商品を1件取得
		Item item = itemRepository.findByIdAndDeleteFlag(id, Constant.NOT_DELETED);

		// 対象の商品が存在しない場合は、商品一覧（新着順・全件）へリダイレクト
		if (item == null) {
			return "redirect:/client/item/list/1";
		}

		// EntityをItemBeanにコピー
		ItemBean itemBean = beanTools.copyEntityToItemBean(item);

		// 商品詳細データをモデルに登録
		model.addAttribute("item", itemBean);
		

		
		
		//閲覧履歴追加

		UserBean user = (UserBean) session.getAttribute("user");
		if(user!=null) {
			ViewHistory viewHistoryItem = viewHistoryRepository.findByUserIdAndItemId(user.getId(), id);
	    	
	    	if(viewHistoryItem==null) {
	    		ViewHistory viewHistory=new ViewHistory();
	    		
	    		viewHistory.setViewCount(1);
	    		viewHistory.setUser(userRepository.getReferenceById(user.getId()));
				viewHistory.setItem(itemRepository.getReferenceById(id));
				viewHistory.setViewDate(new Date());
				viewHistory=viewHistoryRepository.save(viewHistory);
				
	    	}else {
	    		viewHistoryItem.setViewCount(viewHistoryItem.getViewCount()+1);
	    		viewHistoryItem.setViewDate(new Date());
	    		viewHistoryItem=viewHistoryRepository.save(viewHistoryItem);
	    		
	    	}
		}

		// 【ヘッダーエラー防止処理】
		// 詳細画面を開いたときもヘッダーが崩れたりエラーになったりしないよう、現在の検索状態を引き継ぐ
		String keyword = (String) session.getAttribute("searchKeyword");
		model.addAttribute("keyword", keyword != null ? keyword : "");
		model.addAttribute("categoryId", 0);


		return "client/item/detail";
	}
}