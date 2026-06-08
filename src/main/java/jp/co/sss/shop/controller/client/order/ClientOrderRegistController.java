package jp.co.sss.shop.controller.client.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jp.co.sss.shop.form.OrderForm;
import jp.co.sss.shop.service.BeanTools;

@Controller
public class ClientOrderRegistController {

	@Autowired
	BeanTools beanTools;

	@GetMapping("/address/input")
	public String showAddressInput(Model model, HttpSession session) {
		Integer orderId = (Integer) session.getAttribute("currentOrderId");
		OrderForm form = new OrderForm();
		form.setId(orderId);
		model.addAttribute("orderForm", form);

		return "client/order/address_input";
	}

	@PostMapping("/payment/input")
	public String stepToPayment(@Valid @ModelAttribute("orderForm") OrderForm orderForm, BindingResult bindingResult,
			Model model) {
		if (bindingResult.hasErrors()) {
			return "client/order/address_input";
		}
		return "client/order/payment_input";

	}

	@PostMapping("/client/basket/list")
	public String backToBasket() {
		return "redirect:/client/basket/list";

	}

}
