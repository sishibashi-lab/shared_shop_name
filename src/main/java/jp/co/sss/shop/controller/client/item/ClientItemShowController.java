package jp.co.sss.shop.controller.client.item;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jp.co.sss.shop.bean.ItemBean;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.repository.ItemRepository;
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
	 * 商品情報
	 */
	@Autowired
	ItemRepository itemRepository;

	/**
	 * Entity、Form、Bean間のデータコピーサービス
	 */
	@Autowired
	BeanTools beanTools;
	
	/**
	 * トップ画面 表示処理（http://localhost:55000/shared_shop/ アクセス時）
	 * URLパターン: /
	 *
	 * @param model    Viewとの値受渡し
	 * @return "index" トップ画面
	 */
	@RequestMapping(path = "/", method = { RequestMethod.GET, RequestMethod.POST })
	public String index(Model model) {
		// リポジトリの売れ筋順メソッドを使用して、未削除の商品を全件取得
		List<Item> items = itemRepository.findListByPopular(Constant.NOT_DELETED);
		
		// ループ処理で1件ずつ確実にItemBeanへコピー
		List<ItemBean> itemBeans = new ArrayList<>();
		for (Item item : items) {
			itemBeans.add(beanTools.copyEntityToItemBean(item));
		}
		
		// 商品一覧画面のHTMLと互換性を持たせるため、同じ属性名 "items" でモデルに登録
		model.addAttribute("items", itemBeans);
		
		// クエリパラメータ（sortTypeやcategoryId）の初期値として、売れ筋順(1)・全件(0)を画面に送る
		model.addAttribute("sortType", 2);
		model.addAttribute("categoryId", 0);
		
		return "index";
	}

	/**
	 * 商品一覧画面 表示処理
	 *
	 * @param sortType   ソートタイプ (1:新着順, 2:売れ筋順)
	 * @param categoryId カテゴリID (任意、指定がない場合、または0の場合は全件)
	 * @param model      Viewとの値受渡し
	 * @return "client/item/list" 商品一覧画面
	 */
	@RequestMapping(path = "/client/item/list/{sortType}", method = { RequestMethod.GET, RequestMethod.POST })
	public String list(@PathVariable Integer sortType, @RequestParam(name = "categoryId", required = false) Integer categoryId, Model model) {
		
		List<Item> finalItems = new ArrayList<>();

		// カテゴリ指定がない、または「0」の場合は全商品を表示
		if (categoryId == null || categoryId == 0) {
			if (sortType == 1) {
				// 新着順で全件取得
				finalItems = itemRepository.findListByNewest(Constant.NOT_DELETED);
			} else if (sortType == 2) {
				// 【修正】売れ筋順で全件取得（削除フラグを追加）
				finalItems = itemRepository.findListByPopular(Constant.NOT_DELETED);
			}
		} 
		else {
			// カテゴリIDが0以外で指定されている場合は、条件を絞ってデータベースから直接取得
			if (sortType == 1) {
				// 新着順かつカテゴリ指定
				finalItems = itemRepository.findLatestByCategory(categoryId, Constant.NOT_DELETED);
			} else if (sortType == 2) {
				// 【修正】売れ筋順かつカテゴリ指定（削除フラグを追加）
				finalItems = itemRepository.findPopularByCategory(categoryId, Constant.NOT_DELETED);
			} else {
				// 標準のカテゴリ検索（並び順：ID昇順）
				finalItems = itemRepository.findActiveByCategoryId(categoryId, Constant.NOT_DELETED);
			}
		}

		// ループ処理で1件ずつ確実にItemBeanへコピー
		List<ItemBean> itemBeans = new ArrayList<>();
		for (Item item : finalItems) {
			itemBeans.add(beanTools.copyEntityToItemBean(item));
		}

		// 画面に必要なデータを一式モデルに登録
		model.addAttribute("items", itemBeans);
		model.addAttribute("sortType", sortType);
		model.addAttribute("categoryId", categoryId);

		return "client/item/list";
	}

	/**
	 * 商品詳細画面 表示処理
	 * URLパターン: /client/item/detail/{id}
	 *
	 * @param id    表示する商品のID
	 * @param model Viewとの値受渡し
	 * @return "client/item/detail" 商品詳細画面
	 */
	@RequestMapping(path = "/client/item/detail/{id}", method = { RequestMethod.GET, RequestMethod.POST })
	public String detail(@PathVariable Integer id, Model model) {
		// リポジトリからIDを指定して未削除の商品を1件取得
		Item item = itemRepository.findByIdAndDeleteFlag(id, Constant.NOT_DELETED);

		// 対象の商品が存在しない場合は、商品一覧（新着順・全件）へリダイレクト
		if (item == null) {
			return "redirect:/client/item/list/1";
		}

		// EntityをItemBeanにコピー
		ItemBean itemBean = beanTools.copyEntityToItemBean(item);

		// 画面用のHTMLと互換性を持たせるため、属性名 "item" でモデルに登録
		model.addAttribute("item", itemBean);

		return "client/item/detail";
	}
}