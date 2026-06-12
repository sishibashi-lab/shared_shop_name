package jp.co.sss.shop.controller.client.user;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.UserForm;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.util.Constant;

@Controller
public class ClientUserUpdateController {
	
	@Autowired
	private UserRepository repository;
	
	@Autowired
	HttpSession session;
	
	
	//処理1(変更ボタン 押下時処理) 、(確認画面-戻るボタン 押下時処理)

	@RequestMapping(path = "/client/user/update/input", method = RequestMethod.POST)
	public String userUpdateInput() {
		
		//セッションスコープに入力フォーム情報があるかを確認
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		
		//なければ下記の処理を実施
		if (userForm == null) {
			
			//セッションに保存されたログイン情報を取得
			UserBean loginUser = (UserBean)session.getAttribute("user");
			
			User user = repository.findByIdAndDeleteFlag(loginUser.getId(), Constant.NOT_DELETED);
			
			//取得データを元に入力画面書記表示用の入力フォームを新規生成
			userForm = new UserForm();
			BeanUtils.copyProperties(user, userForm);
			
			//入力フォーム情報をセッションスコープに保存
			session.setAttribute("userForm", userForm);
			
		}
		
		return "redirect:/client/user/update/input";
		
	}

	//処理2(変更入力画面表示処理)
	
	@RequestMapping(path = "/client/user/update/input", method = RequestMethod.GET)
	public String userUpdateInputView(Model model) { 
		
		//セッションスコープから入力フォーム情報を取得
		UserForm userForm = (UserForm)session.getAttribute("userForm");
		
		//入力フォーム情報をリクエストスコープに設定
		model.addAttribute("userForm", userForm);
		
		//セッションスコープに入力エラー情報がある場合
		BindingResult result = (BindingResult)session.getAttribute("result");
		
		if(result != null) {
			//1.取得した入力エラー情報をリクエストスコープに設定
			model.addAttribute("org.springframework.validation.BindingResult.userForm", result);
			//2.セッションスコープから、入力エラー情報を削除
			session.removeAttribute("result");
		}
		//変更入力画面表示
		return "client/user/update_input";
	}
	
	//処理3(確認ボタン 押下時処理)
	
	@RequestMapping(path = "/client/user/update/check", method = RequestMethod.POST)
	public String userUpdateCheck(@Valid @ModelAttribute UserForm form, BindingResult result) {
		
		//セッションスコープからフォーム情報を取得
		UserForm sessionForm = (UserForm)session.getAttribute("userForm");
		
		//入力フォーム情報に不足がある場合、セッションスコープから取得した値をセット
			if(sessionForm != null) {
				if(form.getId() == null) {
					form.setId(sessionForm.getId());
				}
				
				if(form.getPassword() == null || form.getPassword().isEmpty()) {
					form.setPassword(sessionForm.getPassword());
				}
			
			}
			
		//画面から入力された入力フォームを、セッションスコープに入力フォーム情報として保存
		session.setAttribute("userForm", form);
		
		
		//BindingResultオブジェクトに入力エラー情報がある場合
			if(result.hasErrors()) {
		//1.入力エラー情報をセッションスコープに設定
				session.setAttribute("result", result);
		//2.変更入力画面表示処理にリダイレクト
				return "redirect:/client/user/update/input";
		
			}
		//入力エラーがない場合
		//変更確認画面表示処理にリダイレクト
			return "redirect:/client/user/update/check";
	}
	
	//処理4(変更画面表示処理)
	
	@RequestMapping(path = "/client/user/update/check", method = RequestMethod.GET)
	public String userUpdateCheckView(Model model) {
		
		//セッションスコープから入力フォーム情報を取得
		UserForm userForm = (UserForm)session.getAttribute("userForm");
		
		//入力フォーム情報をリクエストスコープに設定
		model.addAttribute("userForm", userForm);
		
		//登録確認画面表示
		return "client/user/update_check";
	}
	
	//処理5(登録ボタン 押下時処理)
	@RequestMapping(path = "/client/user/update/complete", method = RequestMethod.POST)
	public String userUpdateComplete(Model model) {
		
		//セッションスコープから入力フォーム情報を取得
		UserForm userForm = (UserForm)session.getAttribute("userForm");
		
		//入力フォーム情報を元にDB登録用エンティティオブジェクトを生成
		User user = repository.findById(userForm.getId()).get();
		BeanUtils.copyProperties(userForm, user);
		
		//DB更新実施
		repository.save(user);
		
		//セッションスコープの入力フォーム情報削除
		session.removeAttribute("userForm");
		
		//-- ログインユーザの会員変更の場合、セッションスコープの会員情報を更新
		UserBean loginUser = new UserBean();
		BeanUtils.copyProperties(user, loginUser);
		session.setAttribute("user", loginUser);
		
		//変更完了画面表示処理にリダイレクト
		return "redirect:/client/user/update/complete";
	}
	
	//処理6(変更完了画面表示処理)
	@RequestMapping(path = "/client/user/update/complete", method = RequestMethod.GET)
	public String userUpdateComplete() {
		return "client/user/update_complete";
	}
	
}

