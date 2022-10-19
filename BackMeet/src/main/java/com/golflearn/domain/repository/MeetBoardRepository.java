package com.golflearn.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.golflearn.domain.entity.MeetBoardEntity;

public interface MeetBoardRepository extends JpaRepository<MeetBoardEntity, Long>{
	
	@Query(value ="SELECT *\r\n"
			+ "FROM (SELECT rownum r, a.*\r\n"
			+ "		  FROM (SELECT * \r\n"
			+ "						FROM meet_board mb JOIN meet_category mc ON mc.meet_ctg_no = mb.meet_ctg_no\r\n"
			+ "						ORDER BY meet_board_no DESC\r\n"
			+ "			   	  ) a\r\n"
			+ "			)\r\n"
			+ "WHERE r BETWEEN ?1 AND ?2"
			,nativeQuery = true)
	List<MeetBoardEntity> findByPage(int startRow, int endRow);
	//모든 목록을 페이징처리하여 최신순으로 보여준다
	
	@Query(value ="SELECT *\r\n"
			+ "FROM (SELECT rownum r, a.*\r\n"
			+ "		  FROM (SELECT * \r\n"
			+ "						FROM meet_board mb JOIN meet_category mc ON mc.meet_ctg_no = mb.meet_ctg_no\r\n"
			+ "						WHERE meet_board_status = ?1 \r\n"
			+ "						ORDER BY meet_board_no DESC\r\n"
			+ "			   	  ) a\r\n"
			+ "			)\r\n"
			+ "WHERE r BETWEEN ?2 AND ?3"
			,nativeQuery = true)
	List<MeetBoardEntity> findByStatusAndPage(Long meetBoardStatus, int startRow, int endRow);
	//모집상태별로 필터링하여 최신순으로 보여준다
	
	@Query(value ="	SELECT COUNT(*)\r\n"
			+ "	FROM meet_board\r\n"
			+ "	WHERE meet_board_status= ?1"
			, nativeQuery = true)
	int countByMeetBoardStatus(Long meetBoardStatus);
	//특정 모집상태인 모임글의 수를 반환한다
	
	@Query(value ="SELECT *\r\n"
			+ "FROM (SELECT rownum r, a.*\r\n"
			+ "		  FROM (SELECT * \r\n"
			+ "						FROM meet_board mb JOIN meet_category mc ON mc.meet_ctg_no = mb.meet_ctg_no\r\n"
			+ "						 WHERE meet_board_title like %?1%\r\n"
			+ "						ORDER BY meet_board_no DESC\r\n"
			+ "			   	  ) a\r\n"
			+ "			)\r\n"
			+ "WHERE r BETWEEN ?2 AND ?3"
			,nativeQuery = true)
	List<MeetBoardEntity> findByWordAndPage(String word, int startRow, int endRow);
	//검색어가 제목에 포함된 모임글의 목록을 불러온다
	
	@Query(value ="	SELECT COUNT(*)\r\n"
			+ "	FROM meet_board\r\n"
			+ "	WHERE meet_board_title LIKE %?1%"
			, nativeQuery = true)
	int countByWord(String word);
	//검색어가 제목에 포함된 모임글의 수를 반환한다
	
	@Query(value="SELECT *\r\n"
			+ "        FROM (SELECT rownum r, a.*\r\n"
			+ "		FROM (SELECT * \r\n"
			+ "		FROM meet_board mb JOIN meet_member mm ON (mb.meet_board_no = mm.meet_board_no)\r\n"
			+ "		WHERE mm.user_nickname = ?1\r\n"
			+ "		ORDER BY mb.meet_board_no DESC\r\n"
			+ "		) a\r\n"
			+ "		)\r\n"
			+ "		WHERE r BETWEEN ?2 AND ?3"
			,nativeQuery = true)
	List<MeetBoardEntity> findByUserNickNameAndPage(String userNickname, int startRow, int endRow);
	//참여중인 모임글의 목록을 불러온다
	
	@Query(value ="	SELECT COUNT(*)\n"
			+ "FROM meet_board b JOIN meet_member m ON (b.meet_board_no = m.meet_board_no)\n"
			+ "WHERE m.user_nickname = ?1"
			, nativeQuery = true)
	int countByUserNicakname(String userNickName);
	//참여중인 모임글의 수를 반환한다
}
