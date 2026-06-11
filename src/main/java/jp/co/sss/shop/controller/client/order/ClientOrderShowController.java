package jp.co.sss.shop.controller.client.order;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.OrderBean;
import jp.co.sss.shop.bean.OrderItemBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Order;
import jp.co.sss.shop.entity.OrderItem;
import jp.co.sss.shop.repository.OrderRepository;
import jp.co.sss.shop.service.BeanTools;
import jp.co.sss.shop.service.PriceCalc;

@Controller
public class ClientOrderShowController {

	/**
	 * 注文情報
	 */
	@Autowired
	OrderRepository orderRepository;

	/**
	 * セッション
	 */
	@Autowired
	HttpSession session;

	/**
	 * 合計金額計算サービス
	 */
	@Autowired
	PriceCalc priceCalc;

	/**
	 * Entity、Form、Bean間のデータ生成、コピーサービス
	 */
	@Autowired
	BeanTools beanTools;

	@GetMapping("/client/order/list")
	public String showOrderList(Model model) {

		UserBean user = (UserBean) session.getAttribute("user");
		List<Order> orders = orderRepository.findByUserIdOrderByInsertDateDescIdDesc(user.getId());

		List<OrderBean> orderBeans = new ArrayList<OrderBean>();
		for (Order order : orders) {

			OrderBean orderBean = beanTools.copyEntityToOrderBean(order);

			List<OrderItem> orderItems = order.getOrderItemsList();
			int total = priceCalc.orderItemPriceTotal(orderItems);

			orderBean.setTotal(total);

			orderBeans.add(orderBean);
		}

		model.addAttribute("order", orderBeans);

		return "client/order/list";
	}

	/**
	 * 注文詳細表示処理 (GET /client/order/detail/{id})
	 * * @param id 詳細表示対象の注文ID
	 * @param model Viewとの値受渡し
	 * @return "client/order/detail" 詳細画面表示
	 */
	@GetMapping("/client/order/detail/{id}")
	public String showOrderDetail(@PathVariable int id, Model model) {

		// 2. 選択された注文情報をDBから取得
		Order order = orderRepository.getReferenceById(id);
		// 3. 表示する注文情報（OrderBean）を生成
		OrderBean orderBean = beanTools.copyEntityToOrderBean(order);

		// 4. 注文商品情報を取得・生成（OrderItemBeanのリスト）
		List<OrderItemBean> orderItemBeans = beanTools.generateOrderItemBeanList(order.getOrderItemsList());

		// 5. 合計金額を算出
		int total = priceCalc.orderItemBeanPriceTotalUseSubtotal(orderItemBeans);

		model.addAttribute("order", orderBean);
		model.addAttribute("orderItemBean", orderItemBeans);
		model.addAttribute("total", total);

		return "client/order/detail";

	}

	@PostMapping("/client/order/list")
	public String backToOrderList() {

		return "redirect:/client/order/list";
	}
}
