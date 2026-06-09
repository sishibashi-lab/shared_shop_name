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

@Controller
public class ClientUserRegistController {

	@Autowired
	UserRepository repository;

	//処理1(新規登録リンク クリック時処理)
	@RequestMapping(path = "/client/user/regist/input/init", method = RequestMethod.GET)
	public String userInput(HttpSession session) {

		//入力フォーム情報を新規生成
		UserForm form = new UserForm();

		//入力フォーム情報をセッションスコープに保存
		session.setAttribute("userForm", form);

		return "redirect:/client/user/regist/input";
	}

	//処理2(新規登録ボタン 押下時処理)、(確認画面-戻るボタン 押下時処理)

	@RequestMapping(path = "/client/user/regist/input", method = RequestMethod.POST)
	public String resistInput(
			@ModelAttribute UserForm form, HttpSession session) {

		//セッションスコープに入力フォーム情報があるかを確認
		UserForm sessionForm = (UserForm) session.getAttribute("userForm");

		//なければ新規生成してセッションに保存
		if (sessionForm == null) {

			sessionForm = new UserForm();
		}

		//画面から受け取ったformの値をsessionFormにコピー
		BeanUtils.copyProperties(form, sessionForm);

		//入力フォーム情報をセッションスコープに保存
		session.setAttribute("userForm", sessionForm);

		return "redirect:/client/user/regist/input";
	}

	//処理3(登録画面表示処理)

	@RequestMapping(path = "/client/user/regist/input", method = RequestMethod.GET)
	public String resistInputView(HttpSession session, Model model) {

		//セッションスコープから入力フォームを取得
		UserForm form = (UserForm) session.getAttribute("userForm");

		//入力フォーム情報をリクエストスコープに設定
		model.addAttribute("userForm", form);

		//セッションスコープに入力エラーがある場合
		if (session.getAttribute("result") != null) {

			model.addAttribute("org.springframework.validation.BindingResult.userForm", session.getAttribute("result"));
			session.removeAttribute("result");
		}
		return "client/user/regist_input";
	}

	//処理4(確認ボタン 押下時処理)
	@RequestMapping(path = "/client/user/regist/check", method = RequestMethod.POST)
	public String resistCheck(
			@Valid @ModelAttribute UserForm form,
			BindingResult result, HttpSession session) {

		//セッションスコープからフォーム情報を取得
		UserForm sessionForm = (UserForm) session.getAttribute("userForm");

		//入力フォーム情報に不足がある場合、セッションスコープから取得した値をセット
		if (sessionForm != null) {

			if (form.getId() == null) {
				form.setId(sessionForm.getId());
			}

			if (form.getEmail() == null) {
				form.setEmail(sessionForm.getEmail());
			}

			if (form.getPassword() == null) {
				form.setPassword(sessionForm.getPassword());
			}

			if (form.getName() == null) {
				form.setName(sessionForm.getName());
			}

			if (form.getPostalCode() == null) {
				form.setPostalCode(sessionForm.getPostalCode());
			}

			if (form.getAddress() == null) {
				form.setAddress(sessionForm.getAddress());
			}

			if (form.getPhoneNumber() == null) {
				form.setPhoneNumber(sessionForm.getPhoneNumber());
			}

			if (form.getAuthority() == null) {
				form.setAuthority(sessionForm.getAuthority());
			}
		}

		//入力された値をセッションスコープに保存
		session.setAttribute("userForm", form);

		//BindingResultオブジェクトに入力エラー情報がある場合
		if (result.hasErrors()) {

			//入力エラー情報と入力フォーム情報を設定
			session.setAttribute("result", result);
			session.setAttribute("userForm", form);

			//登録入力画面表示処理にリダイレクト
			return "redirect:/client/user/regist/input";

		} else {
			//入力エラーがない場合、登録確認画面表示処理にリダイレクト
			return "redirect:/client/user/regist/check";
		}
	}

	//処理5(登録確認画面表示処理)
	@RequestMapping(path = "/client/user/regist/check", method = RequestMethod.GET)
	public String resistCheckView(HttpSession session, Model model) {

		//セッションスコープから入力フォーム情報を取得
		UserForm form = (UserForm) session.getAttribute("userForm");

		//入力フォーム情報をリクエストスコープに設定
		model.addAttribute("userForm", form);

		//登録確認画面表示
		return "client/user/regist_check";
	}

	//処理6(登録ボタン押下時処理)
	@RequestMapping(path = "/client/user/regist/complete", method = RequestMethod.POST)
	public String registComplete(HttpSession session) {

		//セッションスコープから入力フォーム情報を取得
		UserForm form = (UserForm) session.getAttribute("userForm");

		//入力フォーム情報を元にDB登録用エンティティオブジェクトを生成
		User user = new User();
		BeanUtils.copyProperties(form, user);
		
		//最新の成功データをsaveUserで受け取る
		User savedUser = repository.save(user); 
		
		//セッションに残された入力途中のデータ(userForm)を消去
		session.removeAttribute("userForm");

		//ログイン状態を維持するためのもう一つのオブジェクト
		UserBean userBean = new UserBean();
		
		BeanUtils.copyProperties(savedUser, userBean); 

		session.setAttribute("user", userBean);

		//登録完了画面表示処理にリダイレクト
		return "redirect:/client/user/regist/complete";
	}

	//処理7(登録完了画面表示処理)
	@RequestMapping(path = "/client/user/regist/complete", method = RequestMethod.GET)
	public String registCompleteView() {

		//登録完了画面表示
		return "client/user/regist_complete";
	}
}