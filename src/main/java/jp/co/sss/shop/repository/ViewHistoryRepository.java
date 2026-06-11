package jp.co.sss.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.sss.shop.entity.ViewHistory;


public interface ViewHistoryRepository  extends JpaRepository<ViewHistory, Integer> {
	ViewHistory findByUserIdAndItemId(Integer userId,Integer itemId);

	ViewHistory findFirstByOrderByIdDesc();
	
	List<ViewHistory>findAllByUserIdOrderByViewDateDesc(Integer userId);

	ViewHistory findFirstByOrderByViewDateDesc();

	ViewHistory findFirstByUserIdOrderByViewDateDesc(Integer userId);
}
