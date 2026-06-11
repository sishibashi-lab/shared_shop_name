package jp.co.sss.shop.controller.client.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.repository.UserRepository;

/**
 * 会員詳細画面のコントローラークラス
 */
@Controller
public class ClientUserShowController {

	// ユーザ情報のリポジトリ
    @Autowired
    private UserRepository userRepository;

    /**
     * ヘッダーのユーザ名をクリックしたときにユーザの詳細を表示する処理
     * @param model
     * @param session
     * @return
     */
    @RequestMapping(path = "/client/user/detail", method = RequestMethod.GET)
    public String showUserDetail(Model model, HttpSession session) {
        
    	// セッションに保存されているログイン中のユーザの情報を取得
        UserBean loginUser = (UserBean) session.getAttribute("user");
        
        // 取得できなかったらログイン画面にリダイレクト
        if (loginUser == null) {
            return "redirect:/login";
        }
        
        // ログイン中のユーザのIDと一致し、且つ削除フラグが0のユーザを取得
        User userEntity = userRepository.findByIdAndDeleteFlag(loginUser.getId(), 0);
        
        // 取得できなかったらログイン画面にリダイレクト
        if (userEntity == null) {
            return "redirect:/login";
        }
        
        // ユーザの情報を入れる箱を作る
        UserBean userBean = new UserBean();
        
        // 箱に情報を詰め込む
        userBean.setId(userEntity.getId());
        userBean.setEmail(userEntity.getEmail());
        userBean.setName(userEntity.getName());
        userBean.setPostalCode(userEntity.getPostalCode());
        userBean.setAddress(userEntity.getAddress());
        userBean.setPhoneNumber(userEntity.getPhoneNumber());

        // 箱をリクエストスコープに保存
        model.addAttribute("userBean", userBean);
        
        // 会員詳細画面に遷移
        return "client/user/detail"; 
    }
}