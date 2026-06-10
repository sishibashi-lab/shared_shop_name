package jp.co.sss.shop.controller.client.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jp.co.sss.shop.form.UserForm;

@Controller
public class ClientUserUpdateController {
	
	//処理1(変更ボタン 押下時処理) 、(確認画面-戻るボタン 押下時処理)
	

	@RequestMapping(path = "/client/user/update/input", method = RequestMethod.POST)
	public String userUpdateInput() {
		
		//セッションスコープに入力フォーム情報があるかを確認
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		if (userForm == null) {
			
		}
		
	}

}
