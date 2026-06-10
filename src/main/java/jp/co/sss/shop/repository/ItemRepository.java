package jp.co.sss.shop.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.Item;

/**
 * itemsテーブル用リポジトリ
 *
 * @author System Shared
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

	/**
	 * 商品情報を登録日付順に取得 管理者機能で利用
	 * * @param deleteFlag 削除フラグ
	 * @param pageable   ページング情報
	 * @return 商品エンティティのページオブジェクト
	 */
	@Query("SELECT i FROM Item i INNER JOIN i.category c WHERE i.deleteFlag = :deleteFlag ORDER BY i.insertDate DESC, i.id DESC")
	Page<Item> findByDeleteFlagOrderByInsertDateDescPage(
			@Param(value = "deleteFlag") int deleteFlag, Pageable pageable);

	/**
	 * 商品IDと削除フラグを条件に検索（管理者機能で利用）
	 * * @param id         商品ID
	 * @param deleteFlag 削除フラグ
	 * @return 商品エンティティ
	 */
	public Item findByIdAndDeleteFlag(Integer id, int deleteFlag);

	/**
	 * 商品名と削除フラグを条件に検索 (ItemValidatorで利用)
	 * * @param name       商品名
	 * @param notDeleted 削除フラグ
	 * @return 商品エンティティ
	 */
	public Item findByNameAndDeleteFlag(String name, int notDeleted);
	
	/**

	 * 未削除の商品を売れ筋順に取得（実際に1件以上注文された商品のみ）
	 * * @param deleteFlag 削除フラグ
	 * @return 商品エンティティのリスト
	 */
	@Query("SELECT i FROM Item i INNER JOIN OrderItem oi ON oi.item = i WHERE i.deleteFlag = :deleteFlag GROUP BY i.id, i.name, i.price, i.description, i.image, i.stock, i.deleteFlag, i.insertDate, i.category.id ORDER BY SUM(oi.quantity) DESC, i.id DESC")

	List<Item> findListByPopular(@Param("deleteFlag") int deleteFlag);

	/**
	 * 未削除の商品を新着順（登録日付の新しい順）に取得（一般会員用一覧で利用）
	 * * @param deleteFlag 削除フラグ
	 * @return 商品エンティティのリスト
	 */
	@Query("SELECT i FROM Item i WHERE i.deleteFlag = :deleteFlag ORDER BY i.insertDate DESC, i.id DESC")
	List<Item> findListByNewest(@Param(value = "deleteFlag") int deleteFlag);
	
	/**
	 * 特定のカテゴリに所属する未削除の商品を取得（一般会員用カテゴリ検索で利用）
	 * * @param categoryId カテゴリID
	 * @param deleteFlag 削除フラグ
	 * @return 商品エンティティ的リスト
	 */
	@Query("SELECT i FROM Item i WHERE i.deleteFlag = :deleteFlag AND i.category.id = :categoryId ORDER BY i.id ASC")
	List<Item> findActiveByCategoryId(@Param(value = "categoryId") Integer categoryId, @Param(value = "deleteFlag") int deleteFlag);
	
	/**
	 * 特定のカテゴリに所属する未削除の商品を新着順に取得
	 * * @param categoryId カテゴリID
	 * @param deleteFlag 削除フラグ
	 * @return 商品エンティティのリスト
	 */
	@Query("SELECT i FROM Item i WHERE i.deleteFlag = :deleteFlag AND i.category.id = :categoryId ORDER BY i.insertDate DESC, i.id DESC")
	List<Item> findLatestByCategory(@Param("categoryId") Integer categoryId, @Param("deleteFlag") int deleteFlag);
	
	/**
	 * 特定のカテゴリに所属する未削除の商品を売れ筋順に取得（実際に1件以上注文された商品のみ）
	 * * @param categoryId カテゴリID
	 * @param deleteFlag 削除フラグ
	 * @return 商品エンティティのリスト
	 */
	@Query("SELECT i FROM Item i INNER JOIN OrderItem oi ON oi.item = i WHERE i.deleteFlag = :deleteFlag AND i.category.id = :categoryId GROUP BY i.id, i.name, i.price, i.description, i.image, i.stock, i.deleteFlag, i.insertDate, i.category.id ORDER BY SUM(oi.quantity) DESC, i.id DESC")
	List<Item> findPopularByCategory(@Param("categoryId") Integer categoryId, @Param("deleteFlag") int deleteFlag);
	
	/**
	 * 商品名にあいまい検索キーワードを含む、未削除の商品を売れ筋順に取得（実際に1件以上注文された商品のみ）
	 *
	 * @param keyword    検索キーワード (例: "%りんご%")
	 * @param deleteFlag 削除フラグ
	 * @return 商品エンティティのリスト
	 */
	@Query("SELECT i FROM Item i INNER JOIN OrderItem oi ON oi.item = i WHERE i.deleteFlag = :deleteFlag AND i.name LIKE :keyword GROUP BY i.id, i.name, i.price, i.description, i.image, i.stock, i.deleteFlag, i.insertDate, i.category.id ORDER BY SUM(oi.quantity) DESC, i.id DESC")
	List<Item> findListByPopularAndKeyword(@Param("keyword") String keyword, @Param("deleteFlag") int deleteFlag);
	
	/**
	 * 商品名にあいまい検索キーワードを含む、未削除の商品を新着順（登録日付の新しい順）に取得
	 *
	 * @param keyword    検索キーワード (例: "%りんご%")
	 * @param deleteFlag 削除フラグ
	 * @return 商品エンティティのリスト
	 */
	@Query("SELECT i FROM Item i WHERE i.deleteFlag = :deleteFlag AND i.name LIKE :keyword ORDER BY i.insertDate DESC, i.id DESC")
	List<Item> findListByNewestAndKeyword(@Param("keyword") String keyword, @Param("deleteFlag") int deleteFlag);
	
	List<Item>findAllByCategoryName(String category);
}