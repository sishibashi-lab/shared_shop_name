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
 * 会員退会処理のコントローラークラス
 */
@Controller
public class ClientUserDeleteController {
	
    @Autowired
    private HttpSession session;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * ログインユーザの詳細を取得し、確認画面へ遷移する処理
     * @param model
     * @return
     */
    @RequestMapping(path = "/client/user/delete/check", method = RequestMethod.POST)
    public String userDeleteCheck(Model model) {
        
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
        
        // ユーザ情報をリクエストスコープに保存
        model.addAttribute("userForm", userEntity);
        
        // 退会確認画面に遷移
        return "client/user/delete_check";
    }
    
    /**
     * 退会するユーザの削除フラグを更新する処理
     */
    @RequestMapping(path = "/client/user/delete/complete", method = RequestMethod.POST)
    public String userDelete() {
        
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
            
        // 取得出来たら削除フラグを変更し保存。その後いらなくなったセッションを破棄
        } else {
        	userEntity.setDeleteFlag(1);
        	userRepository.save(userEntity);
        	session.invalidate();
        }
        
        // 退会完了画面に遷移
        return "client/user/delete_complete";
    }
}