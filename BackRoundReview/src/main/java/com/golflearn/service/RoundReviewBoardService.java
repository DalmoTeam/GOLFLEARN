package com.golflearn.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.golflearn.domain.entity.PageBean;
import com.golflearn.domain.entity.RoundReviewBoardEntity;
import com.golflearn.domain.entity.RoundReviewCommentEntity;
import com.golflearn.domain.entity.RoundReviewLikeEntity;
import com.golflearn.domain.repository.RoundReviewBoardRepository;
import com.golflearn.domain.repository.RoundReviewCommentRepository;
import com.golflearn.domain.repository.RoundReviewLikeRepository;
import com.golflearn.dto.RoundReviewBoardDto;
import com.golflearn.dto.RoundReviewCommentDto;
import com.golflearn.dto.RoundReviewLikeDto;
import com.golflearn.exception.AddException;
import com.golflearn.exception.FindException;
import com.golflearn.exception.ModifyException;
import com.golflearn.exception.RemoveException;

@Service
public class RoundReviewBoardService {
	private static final int CNT_PER_PAGE = 5;
	@Autowired
	private RoundReviewBoardRepository boardRepo;
	
	@Autowired
	private RoundReviewCommentRepository commentRepo;
	
	@Autowired
	private RoundReviewLikeRepository likeRepo;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
//	/**
//	 * 정렬 기준에 따라 게시글의 목록을 보여줌 
//	 * @param currentPage 현재 페이지 
//	 * @param orderType 프론트에서 받아옴
//	 * @return
//	 * @throws FindException
//	 */
	public Page<RoundReviewBoardDto> boardList(int currentPage, int orderType, Pageable pageable) throws FindException{
		Page<RoundReviewBoardEntity> entity = boardRepo.findAll(pageable);
		ModelMapper modelMapper = new ModelMapper();
		Page<RoundReviewBoardDto> dto = modelMapper.map(entity, new TypeToken<Page<RoundReviewBoardDto>>(){}.getType());
		
		//---이전 코드 
//		if (orderType == 1) {
//			list = boardRepo.findListByViewCnt(startRow, endRow, pageable);
//		}
//		}else if (orderType == 2) {
//			list = boardRepo.findListByLike(startRow, endRow pageable);
//		}else {
//			list = boardRepo.findListByRecent(startRow, endRow pageable);
//		}
//		ModelMapper modelMapper = new ModelMapper();
//		List<RoundReviewBoardDto> dtoList = list.stream()
//				.map(RoundReviewBoardEntity -> modelMapper
//						.map(RoundReviewBoardEntity, RoundReviewBoardDto.class))
//				.collect(Collectors.toList());
//		PageBean<RoundReviewBoardDto> pb = new PageBean<>(dtoList, totalCnt, currentPage, cntPerPageGroup, CNT_PER_PAGE);
		return dto;
	}
	/**
	 * 게시글을 검색함 
	 * @param word
	 * @param currentPage
	 * @return
	 * @throws FindException
	 */
		public Page<RoundReviewBoardDto> searchBoard(String word, int currentPage) throws FindException{
			Pageable pageable = PageRequest.of(currentPage, CNT_PER_PAGE);
			Page<RoundReviewBoardEntity> entity = boardRepo.findByWord(word, pageable);
			ModelMapper modelMapper = new ModelMapper();
			Page<RoundReviewBoardDto> dto = modelMapper.map(entity, new TypeToken<Page<RoundReviewBoardDto>>(){}.getType());
			
			//이전코드
//			Page<RoundReviewBoardEntity> entity = boardRepo.findByWord(word, startRow, CNT_PER_PAGE, pageable);
//			ModelMapper modelMapper = new ModelMapper();
//			List<RoundReviewBoardDto> dtoList = entityList.stream()
//					.map(RoundReviewBoardEntity -> modelMapper
//							.map(RoundReviewBoardEntity, RoundReviewBoardDto.class))
//					.collect(Collectors.toList());
//			Page<RoundReviewBoardDto> pb = new PageBean<>(dtoList, totalCnt, currentPage, cntPerPageGroup, CNT_PER_PAGE);
			return dto;
		}
	/**
	 * 라운딩리뷰 게시글의 상세내용을 보여줌 
	 * 기존 게시글 목록을 보던 중 게시자가 게시글을 삭제한 경우
	 * '게시글이 없습니다'를 던져주기 
	 * @param roundReviewBoardNo 라운딩리뷰 게시글번호
	 * @return
	 * @throws FindException 
	 */
	
	public RoundReviewBoardDto viewBoard(Long roundReviewBoardNo) throws FindException {
		Optional<RoundReviewBoardEntity> optB = boardRepo.findById(roundReviewBoardNo);
		//조회수 증가
		if(optB.isPresent()) {
			RoundReviewBoardEntity entity = optB.get();
			entity.setRoundReviewBoardViewCnt(entity.getRoundReviewBoardViewCnt()+1);
			boardRepo.save(entity);
		}else {
			throw new FindException("게시글이 없습니다");
		}
		
		RoundReviewBoardEntity entity = optB.get();
		ModelMapper modelMapper = new ModelMapper();
		RoundReviewBoardDto dto = modelMapper.map(entity, RoundReviewBoardDto.class);
		
		//게시글내용반환
		Optional<RoundReviewBoardDto> optB1 = Optional.of(dto);
		if (optB1.isPresent()) {
			RoundReviewBoardDto b1 = optB1.get();
			return b1;
		} else {
			throw new FindException("게시글이 없습니다");
		}
	}
	
	/**
	 * 기존 게시글 내용을 가져온후 제목, 내용, 지도 수정된 값을 다시 update 해줌
	 * @param dto
	 * @throws ModifyException
	 */
	public void modifyBoard(RoundReviewBoardDto dto) throws ModifyException {
		//우선 기존 내용 가져오고 
		Long boardNo = dto.getRoundReviewBoardNo();
		Optional<RoundReviewBoardEntity> optB = boardRepo.findById(boardNo);
		if(!optB.isPresent()) {
			throw new ModifyException("글이 없습니다");
		}else {
			RoundReviewBoardEntity entity = optB.get();
			//제목, 내용, 위도, 경도 새로 set
			entity.setRoundReviewBoardTitle(dto.getRoundReviewBoardTitle());
			entity.setRoundReviewBoardContent(dto.getRoundReviewBoardContent());
			entity.setRoundReviewBoardLatitude(dto.getRoundReviewBoardLatitude());
			entity.setRoundReviewBoardLongitude(dto.getRoundReviewBoardLongitude());
			boardRepo.save(entity);
		}
	}
	/**
	 * 원글번호를 입력하면 해당하는 댓글, 대댓글, 좋아요 모두 삭제 
	 * @param roundReviewBoardNo
	 * @throws RemoveException
	 */
	public void removeBoard (Long roundReviewBoardNo) throws RemoveException {
		Optional<RoundReviewBoardEntity> optB = boardRepo.findById(roundReviewBoardNo);
		if(!optB.isPresent()) {
			throw new RemoveException("삭제할 글이 없습니다");
		}else {
			//JPA cascade설정으로 게시글 삭제하면 댓글, 대댓글도 함께 삭제 
			boardRepo.deleteById(roundReviewBoardNo);
		}
	}
	/**
	 * 댓글 삭제하기 
	 * @param roundReviewBoardNo
	 * @param roundReviewCmtNo
	 * @throws RemoveException
	 */
	public void removeComment (Long roundReviewBoardNo, Long roundReviewCmtNo) throws RemoveException {
		Optional<RoundReviewBoardEntity> optB = boardRepo.findById(roundReviewBoardNo);
		RoundReviewBoardEntity boardEntity = optB.get();
		//댓글 개수 1개 감소시키기 
		boardEntity.setRoundReviewBoardCmtCnt(boardEntity.getRoundReviewBoardCmtCnt()-1);
		boardRepo.save(boardEntity);
		
		Optional<RoundReviewCommentEntity> optC = commentRepo.findById(roundReviewBoardNo);
		if(!optC.isPresent()) {
			throw new RemoveException("삭제할 댓글이 없습니다");
		}else {
			commentRepo.deleteById(roundReviewBoardNo);
		}
	}
	/**
	 * 대댓글 삭제 
	 * @param roundReviewBoardNo
	 * @param roundReviewCmtNo
	 * @throws RemoveException
	 */
	public void removeRecomment (Long roundReviewBoardNo, Long roundReviewCmtNo) throws RemoveException {
		Optional<RoundReviewBoardEntity> optB = boardRepo.findById(roundReviewBoardNo);
		RoundReviewBoardEntity boardEntity = optB.get();
		//댓글 수 감소
		boardEntity.setRoundReviewBoardCmtCnt(boardEntity.getRoundReviewBoardCmtCnt()-1);
		boardRepo.save(boardEntity);
		
		Optional<RoundReviewCommentEntity> optC = commentRepo.findById(roundReviewCmtNo);
		if(!optC.isPresent()) {
			throw new RemoveException("삭제할 대댓글이 없습니다");
		}else {
			commentRepo.deleteById(roundReviewCmtNo);
		}
	}
	/**
	 * 좋아요 추가 + 좋아요 수 증가
	 * @param roundReviewBoardNo
	 * @param dto
	 * @throws AddException
	 */
	public void addLike (Long roundReviewBoardNo, RoundReviewLikeDto dto) throws AddException {
		Optional<RoundReviewBoardEntity> optB = boardRepo.findById(roundReviewBoardNo);
		if (optB.isPresent()) {
			//좋아요 수 증가
			RoundReviewBoardEntity boardEntity = optB.get();
			boardEntity.setRoundReviewBoardLikeCnt(boardEntity.getRoundReviewBoardLikeCnt()+1);
			boardRepo.save(boardEntity);
			
			//좋아요 추가 
			ModelMapper modelMapper = new ModelMapper();
			RoundReviewLikeEntity likeEntity = modelMapper.map(dto, RoundReviewLikeEntity.class);
			likeRepo.save(likeEntity);
		} else {
			throw new AddException("게시글이 존재하지 않습니다");
		}
	}
	/**
	 * 좋아요 취소 좋아요 수 감소 
	 * @param roundReviewBoardNo
	 * @param userNickname
	 * @throws RemoveException
	 */
	public void removeLike (Long roundReviewBoardNo, String userNickname) throws RemoveException{
		Optional<RoundReviewBoardEntity> optB = boardRepo.findById(roundReviewBoardNo);
		RoundReviewBoardEntity boardEntity = optB.get();
		//좋아요 수 감소
		boardEntity.setRoundReviewBoardLikeCnt(boardEntity.getRoundReviewBoardLikeCnt()-1);
		boardRepo.save(boardEntity);
		
		//좋아요 취소
		likeRepo.deleteLike(roundReviewBoardNo, userNickname);
	}
	/**
	 * 게시글 작성하기 
	 * @param dto
	 * @throws AddException
	 */
	public RoundReviewBoardDto writeBoard(RoundReviewBoardDto dto) throws AddException {
		ModelMapper modelMapper = new ModelMapper();
		RoundReviewBoardEntity entity = modelMapper.map(dto, RoundReviewBoardEntity.class);
		boardRepo.save(entity);
		
		Optional<RoundReviewBoardEntity> optRb = boardRepo.findById(entity.getRoundReviewBoardNo());
		RoundReviewBoardEntity boardEntity = optRb.get();
		RoundReviewBoardDto boardDto = modelMapper.map(boardEntity, RoundReviewBoardDto.class);
		return boardDto;
	}
	/**
	 * 댓글 등록 + 댓글 수 증가 
	 * @param dto
	 * @throws AddException
	 */
	public void addComment(Long roundReviewBoardNo, RoundReviewCommentDto dto) throws AddException{
		Optional<RoundReviewBoardEntity> optB = boardRepo.findById(roundReviewBoardNo);
		RoundReviewBoardEntity boardEntity = optB.get();
		//댓글 수 증가
		boardEntity.setRoundReviewBoardCmtCnt(boardEntity.getRoundReviewBoardCmtCnt()+1);
		boardRepo.save(boardEntity);
		
		//댓글 등록 
		ModelMapper modelMapper = new ModelMapper();
		RoundReviewCommentEntity commentEntity = modelMapper.map(dto, RoundReviewCommentEntity.class);
		commentRepo.save(commentEntity);
	}
	/**
	 * 게시글에 달린 댓글 내용 수정하기
	 * @param dto
	 * @throws ModifyException
	 */
	public void modifyComment(RoundReviewCommentDto dto) throws ModifyException{
		ModelMapper modelMapper = new ModelMapper();
		RoundReviewCommentEntity entity = modelMapper.map(dto, RoundReviewCommentEntity.class);
		//기존 댓글 내용 가져오기 
		Optional<RoundReviewCommentEntity> optC = commentRepo.findById(dto.getRoundReviewBoard().getRoundReviewBoardNo());
		if(!optC.isPresent()) {
			throw new ModifyException("댓글이 없습니다");
		}else {
			RoundReviewCommentEntity commentEntity = optC.get();
			commentEntity.setRoundReviewCmtContent(entity.getRoundReviewCmtContent());
			commentRepo.save(commentEntity);
		}
	}
//	/**
//	 * 대댓글 수정하기 
//	 * @param dto
//	 * @throws ModifyException
//	 */
//
//	public void modifyRecomment(RoundReviewCommentDto dto) throws ModifyException{
//		ModelMapper modelMapper = new ModelMapper();
//		RoundReviewCommentEntity entity = modelMapper.map(dto, RoundReviewCommentEntity.class);
//		Optional<RoundReviewCommentEntity> optC = commentRepo.findById(dto.getRoundReviewCmtNo());
//		if(!optC.isPresent()) {
//			throw new ModifyException("대댓글이 없습니다");
//		}else {
//			RoundReviewCommentEntity commentEntity = optC.get();
//			commentEntity.setRoundReviewCmtContent(entity.getRoundReviewCmtContent());
//			commentRepo.save(commentEntity);
//		}
//	}	
}
