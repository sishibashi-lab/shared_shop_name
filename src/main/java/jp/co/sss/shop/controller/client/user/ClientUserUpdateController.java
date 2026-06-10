package jp.co.sss.shop.controller.client.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.form.UserForm;
import jp.co.sss.shop.repository.UserRepository;

@Controller
public class ClientUserUpdateController {
	
	@Autowired
	private UserRepository repository;
	
	//処理1(変更ボタン 押下時処理) 、(確認画面-戻るボタン 押下時処理)

	@RequestMapping(path = "/client/user/update/input", method = RequestMethod.POST)
	public String userUpdateInput(HttpSession session) {
		
		//セッションスコープに入力フォーム情報があるかを確認
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		
		//なければ下記の処理を実施
		if (userForm == null) {
			//パスに指定のIDを条件に変更対象のデータをDBから取得
			UserBean userBean = (UserBean)session.getAttribute("user");
			
			//取得データを元に入力画面書記表示用の入力フォームを新規生成
			
			//
			
		}
		
		return "redirect:/client/user/update/input";
		
	}
	
	

}
