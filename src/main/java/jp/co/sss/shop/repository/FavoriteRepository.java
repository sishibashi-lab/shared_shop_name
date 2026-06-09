package jp.co.sss.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.sss.shop.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer>{
	boolean existsByItemId(Integer itemId);
	boolean existsByUserId(Integer userId);
	List<Favorite>findByItemId(Integer itemId);
	List<Favorite>findByUserIdAndDeleteFlagOrderByFavoriteDateDesc(Integer userId,Integer deleteFlag);
	
	Favorite findFirstByOrderByIdDesc();
	
	Favorite findByUserIdAndItemId(Integer userId,Integer itemId);
}
