package jp.co.sss.shop.controller.client.order;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jp.co.sss.shop.bean.BasketBean;
import jp.co.sss.shop.bean.OrderItemBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.OrderForm;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.repository.OrderItemRepository;
import jp.co.sss.shop.repository.OrderRepository;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.util.Constant;

/**
 * 一般会員用 注文手続きコントローラー
 */
@Controller
public class ClientOrderRegistController {

	@Autowired
	HttpSession session;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	OrderItemRepository orderItemRepository;

	/**
	 * 注文手続き開始（買い物かご画面からの遷移）
	 * お届け先入力の準備を行い、初期値をセットしてリダイレクトする
	 */
	@PostMapping("/client/order/address/input")
	public String startOrder() {
		// 1. セッションからログインユーザー情報を取得し、最新の会員情報をDBから取得
		UserBean userBean = (UserBean) session.getAttribute("user");
		User user = userRepository.findByIdAndDeleteFlag(userBean.getId(), Constant.NOT_DELETED);

		// 2. 買い物かごの存在チェック（空なら買い物かご一覧へ戻す処理）
		@SuppressWarnings("unchecked")
		List<BasketBean> basketBeans = (List<BasketBean>) session.getAttribute("basketBeans");
		if (basketBeans == null || basketBeans.isEmpty()) {
			return "redirect:/client/basket/list";
		}

		// 3. 購入前在庫チェック（この時点で在庫不足の商品があれば手続きに進ませない）
		for (BasketBean basketBean : basketBeans) {
			Item item = itemRepository.findByIdAndDeleteFlag(basketBean.getId(), Constant.NOT_DELETED);
			if (item == null || item.getStock() == 0 || basketBean.getOrderNum() > item.getStock()) {
				return "redirect:/client/basket/list";
			}
		}

		// 4. 注文入力フォームの初期化と、会員情報（住所・氏名等）のコピー
		OrderForm orderForm = new OrderForm();
		BeanUtils.copyProperties(user, orderForm);

		// 支払方法の初期値として「クレジットカード(1)」を設定
		orderForm.setPayMethod(1);

		// 初期設定したフォームをセッションに保持
		session.setAttribute("orderForm", orderForm);

		return "redirect:/client/order/address/input";
	}

	/**
	 * お届け先入力画面の表示
	 */
	@GetMapping("/client/order/address/input")
	public String showAddressInput(HttpSession session, Model model) {
		OrderForm form = (OrderForm) session.getAttribute("orderForm");

		if (form == null) {
			return "redirect:/syserror";
		}

		// 次の画面（支払方法入力）のバリデーションでエラーがあった場合、
		// セッション経由でBindingResultを受け取って画面に表示させる
		BindingResult bindingResult = (BindingResult) session.getAttribute("result");
		if (bindingResult != null) {
			model.addAttribute("org.springframework.validation.BindingResult.orderForm", bindingResult);
			//一度表示したらセッションから消去
			session.removeAttribute("result");
		}

		model.addAttribute("orderForm", form);

		return "client/order/address_input";
	}

	/**
	 * お届け先入力の確定処理（支払方法入力画面への遷移）
	 */
	@PostMapping("/client/order/payment/input")
	public String stepToPayment(@Valid @ModelAttribute("orderForm") OrderForm orderForm, BindingResult bindingResult,
			HttpSession session) {

		// 入力チェックエラー（住所や電話番号の不備など）がある場合は元の画面へリダイレクト
		if (bindingResult.hasErrors()) {
			session.setAttribute("result", bindingResult);
			return "redirect:/client/order/address/input";
		}
		session.setAttribute("orderForm", orderForm);
		return "redirect:/client/order/payment/input";
	}

	/**
	 * 支払方法選択画面の表示
	 */
	@GetMapping("/client/order/payment/input")
	public String showPaymentInput(HttpSession session, Model model) {
		OrderForm form = (OrderForm) session.getAttribute("orderForm");

		if (form == null) {
			return "redirect:/syserror";
		}

		model.addAttribute("orderForm", form);
		model.addAttribute("payMethod", form.getPayMethod());

		return "client/order/payment_input";
	}

	/**
	 * 支払方法の確定処理（注文確認画面への遷移）
	 */
	@PostMapping("/client/order/check")
	public String stepToCheck(Integer payMethod, HttpSession session) {
		OrderForm form = (OrderForm) session.getAttribute("orderForm");

		if (form != null) {
			// ラジオボタンで選ばれた支払方法をセット
			form.setPayMethod(payMethod);
			session.setAttribute("orderForm", form);
		}
		return "redirect:/client/order/check";
	}

	/**
	 * 注文手続きを中止し、買い物かごに戻る
	 */
	@PostMapping("/client/basket/list")
	public String backToBasket() {
		return "redirect:/client/basket/list";
	}

	/**
	 * 支払方法画面からお届け先入力画面へ戻る
	 */
	@PostMapping("/client/order/payment/back")
	public String backToAddressInput() {
		return "redirect:/client/order/address/input";
	}

	/**
	 * 注文確認画面の表示
	 */
	@GetMapping("/client/order/check")
	public String showOrderCheck(HttpSession session, Model model) {
		OrderForm form = (OrderForm) session.getAttribute("orderForm");
		@SuppressWarnings("unchecked")
		List<BasketBean> basketBeans = (List<BasketBean>) session.getAttribute("basketBeans");

		if (form == null || basketBeans == null) {
			return "redirect:/syserror";
		}

		// セッションが切れている等の不測の事態に対するガード処理
		if (form == null || basketBeans == null || basketBeans.isEmpty()) {
			return "redirect:/client/basket/list";
		}

		// 在庫の有無を反映させた「新しい買い物かごリスト」
		List<BasketBean> newBaskets = new ArrayList<BasketBean>();

		// 画面に「在庫切れ」「数量調整」の警告メッセージを出すための商品名リスト
		List<String> itemNamesZero = new ArrayList<String>();
		List<String> itemNamesLessThan = new ArrayList<String>();

		// --- 最新のDB情報をもとに在庫の最終チェックループ ---
		for (BasketBean basketItem : basketBeans) {
			Item item = itemRepository.findByIdAndDeleteFlag(basketItem.getId(), Constant.NOT_DELETED);

			if (item == null || item.getStock() == 0) {
				//  完全に在庫切れの商品は、新しい買い物かごから除外（自動削除）
				itemNamesZero.add(basketItem.getName());
			} else {
				if (item.getStock() < basketItem.getOrderNum()) {
					//  在庫はあるが注文数に満たない場合、注文数を上限（残り在庫数）まで強制的に引き下げる
					basketItem.setOrderNum(item.getStock());
					itemNamesLessThan.add(basketItem.getName());
				}
				// 最新の在庫数をBeanに反映して、新しい買い物かごリストに維持
				basketItem.setStock(item.getStock());
				newBaskets.add(basketItem);
			}
		}

		// エラー（在庫切れ・不足）があった場合、画面に通知用リストを渡す
		if (!itemNamesZero.isEmpty() || !itemNamesLessThan.isEmpty()) {
			if (!itemNamesZero.isEmpty()) {
				model.addAttribute("itemNameListZero", itemNamesZero);
			}
			if (!itemNamesLessThan.isEmpty()) {
				model.addAttribute("itemNameListLessThan", itemNamesLessThan);
			}

			// 在庫調整の結果、かごの中身がゼロになってしまった場合は強制リダイレクト
			if (newBaskets.isEmpty()) {
				session.removeAttribute("basketBeans");
				return "redirect:/client/basket/list";
			}
		}

		// 在庫状況を反映・調整した買い物かご情報をセッションへ上書き保存
		session.setAttribute("basketBeans", newBaskets);

		// --- 画面表示用データ（OrderItemBean）の組み立てと金額計算 ---
		List<OrderItemBean> orderItemBeans = new ArrayList<OrderItemBean>();
		int total = 0;

		for (BasketBean basketItem : newBaskets) {
			Item item = itemRepository.findByIdAndDeleteFlag(basketItem.getId(), Constant.NOT_DELETED);
			if (item != null) {
				OrderItemBean orderItemBean = new OrderItemBean();
				orderItemBean.setId(item.getId());
				orderItemBean.setName(item.getName());
				orderItemBean.setImage(item.getImage());
				orderItemBean.setPrice(item.getPrice());
				orderItemBean.setOrderNum(basketItem.getOrderNum());

				// 小計の算出（単価 × 調整後の注文数）
				int subtotal = item.getPrice() * basketItem.getOrderNum();
				orderItemBean.setSubtotal(subtotal);

				orderItemBeans.add(orderItemBean);

				// 合計金額へ加算
				total += subtotal;
			}
		}

		// 各種情報をリクエストスコープに詰め込んでビューへ渡す
		model.addAttribute("total", total);
		model.addAttribute("orderItemBeans", orderItemBeans);
		model.addAttribute("orderForm", form);

		return "client/order/check";
	}

	/**
	 * 注文確定処理
	 */
	@PostMapping("/client/order/complete")
	public String completeOrder(HttpSession session) {
		OrderForm form = (OrderForm) session.getAttribute("orderForm");
		@SuppressWarnings("unchecked")
		List<BasketBean> basketList = (List<BasketBean>) session.getAttribute("basketBeans");

		// 1. 同時購入（他ユーザーの滑り込み決済）を考慮した、購入直前の最終在庫チェック
		for (BasketBean basketItem : basketList) {
			Item item = itemRepository.findByIdAndDeleteFlag(basketItem.getId(), Constant.NOT_DELETED);

			if (item == null || item.getStock() == 0 || item.getStock() < basketItem.getOrderNum()) {
				// もし決済直前に在庫が無くなっていたら、確認画面（showOrderCheck）へ戻して再計算させる
				return "redirect:/client/order/check";
			}
		}

		// 2. 注文に紐付けるユーザーEntityを取得
		UserBean userBean = (UserBean) session.getAttribute("user");
		User user = userRepository.findByIdAndDeleteFlag(userBean.getId(), Constant.NOT_DELETED);

		// 3.「orders」へ登録するオブジェクトの作成と保存
		jp.co.sss.shop.entity.Order order = new jp.co.sss.shop.entity.Order();
		BeanUtils.copyProperties(form, order);

		order.setId(null); // 自動採番(AUTO_INCREMENT)させるため明示的にnullをセット
		order.setUser(user);

		// ordersテーブルへインサート実行
		orderRepository.save(order);

		// 4. 「order_items」への登録と、商品在庫の減算（UPDATE）
		for (BasketBean basketItem : basketList) {
			Item item = itemRepository.findByIdAndDeleteFlag(basketItem.getId(), Constant.NOT_DELETED);

			// 商品テーブル(items)の在庫数を減算してUPDATE
			item.setStock(item.getStock() - basketItem.getOrderNum());
			itemRepository.save(item);

			// 注文商品エンティティ(order_items)を生成
			jp.co.sss.shop.entity.OrderItem orderItem = new jp.co.sss.shop.entity.OrderItem();
			orderItem.setOrder(order); // 3で発番された親注文を紐付け
			orderItem.setItem(item); // 対象の商品を紐付け
			orderItem.setQuantity(basketItem.getOrderNum()); // 購入数
			orderItem.setPrice(item.getPrice()); // 購入時点の単価を記録

			// order_itemsテーブルへインサート実行
			orderItemRepository.save(orderItem);
		}

		// 5. 購入が正常完了したため、セッション内の注文用データを削除
		session.removeAttribute("orderForm");
		session.removeAttribute("basketBeans");

		// ブラウザのリロードによる「二重決済」を防ぐため、必ずリダイレクトする
		return "redirect:/client/order/complete";
	}

	/**
	 * 注文完了画面の表示
	 */
	@GetMapping("/client/order/complete")
	public String showComplete() {
		return "client/order/complete";
	}
}