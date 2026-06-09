package jp.co.sss.shop.controller.client.favorite;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * お気に入り一覧画面 表示確認用コントローラクラス
 *
 * DB連携なし
 */
@Controller
public class ClientFavoriteTestController {

	/**
	 * お気に入り一覧画面 表示処理
	 * URLパターン: /client/favorite/list
	 *
	 * @param model Viewとの値受渡し
	 * @return "client/favorite/list" お気に入り一覧画面
	 */
	@RequestMapping(path = "/client/favorite/list", method = { RequestMethod.GET, RequestMethod.POST })
	public String list(Model model) {
		List<FavoriteTestItem> favoriteItems = new ArrayList<>();

		favoriteItems.add(new FavoriteTestItem(1, "ウェーブリング", "Accessory", "シルバー925", 12100,
				"https://images.unsplash.com/photo-1668619323457-e7306bb4fc91?auto=format&fit=crop&fm=jpg&q=80&w=1200",
				"NEW"));
		favoriteItems.add(new FavoriteTestItem(2, "ホースシューネックレス", "Accessory", "シルバー925 / レザーコード", 15400,
				"https://unsplash.com/photos/y2ErhoE92KA/download?force=true", "LIMITED"));
		favoriteItems.add(new FavoriteTestItem(3, "レザーキーケース", "Lifestyle", "本革 / ブラック", 7700,
				"https://unsplash.com/photos/E8juLzDOqpc/download?force=true", "NEW"));

		model.addAttribute("favoriteItems", favoriteItems);
		model.addAttribute("favoriteCount", favoriteItems.size());

		return "client/favorite/list";
	}

	/**
	 * お気に入り画面デザイン確認用。
	 */
	public static class FavoriteTestItem {
		private Integer id;
		private String name;
		private String categoryName;
		private String material;
		private Integer price;
		private String image;
		private String badge;

		public FavoriteTestItem(Integer id, String name, String categoryName, String material, Integer price,
				String image, String badge) {
			this.id = id;
			this.name = name;
			this.categoryName = categoryName;
			this.material = material;
			this.price = price;
			this.image = image;
			this.badge = badge;
		}

		public Integer getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public String getMaterial() {
			return material;
		}

		public Integer getPrice() {
			return price;
		}

		public String getImage() {
			return image;
		}

		public String getBadge() {
			return badge;
		}
	}
}
