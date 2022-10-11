package com.golflearn.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.golflearn.domain.entity.RoundReviewCommentEntity;

public interface RoundReviewCommentRepository extends JpaRepository<RoundReviewCommentEntity, Long> {
	/**
	 * 댓글 삭제 
	 * @param roundReviewCmtNo
	 */
	@Query(value="DELETE FROM round_review_comment "
			+ "WHERE round_review_cmt_parent_no=?1"
			,nativeQuery = true)
	void deleteComment(Long roundReviewCmtNo);
	
	/**
	 * 댓글 및 대댓글 한번에 삭제
	 * @param roundReviewBoardNo
	 */
	@Modifying
	@Query(value = "DELETE FROM round_review_comment "
			+ "WHERE round_review_board_no= ?1"
			,nativeQuery = true)
	void deleteComments(Long roundReviewBoardNo);
	
	/**
	 * 대댓글 삭제
	 * @param roundReviewCmtNo
	 */
	@Modifying
	@Query(value = "DELETE FROM round_review_comment "
			+ "WHERE round_review_cmt_no= ?1"
			,nativeQuery = true)
	void deleteRecomment(Long roundReviewCmtNo);
	
}
