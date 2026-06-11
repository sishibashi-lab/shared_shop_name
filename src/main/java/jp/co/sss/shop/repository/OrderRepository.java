package jp.co.sss.shop.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.Order;

/**
 * ordersテーブル用リポジトリ
 *
 * @author System Shared
 */
@Repository

public interface OrderRepository extends JpaRepository<Order, Integer> {

	/**
	 * 注文日付降順で注文情報すべてを検索(管理者機能で利用)
	 * @param pageable ページング情報
	 * @return 注文エンティティのページオブジェクト
	 */
	@Query("SELECT o FROM Order o ORDER BY o.insertDate DESC,o.id DESC")
	Page<Order> findAllOrderByInsertdateDescIdDesc(Pageable pageable);

	/**
	 * ユーザーIDに一致する注文情報を日付の降順で取得（一般ユーザーの履歴用）
	 * @param userId ログイン中のユーザーID
	 * @return 注文エンティティのリスト
	 */
	@Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.insertDate DESC, o.id DESC")
	List<Order> findByUserIdOrderByInsertDateDescIdDesc(@Param("userId") Integer userId);
}
