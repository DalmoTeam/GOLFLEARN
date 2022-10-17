package com.golflearn.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.golflearn.domain.entity.MeetBoardEntity;
import com.golflearn.domain.entity.MeetCategoryEntity;
import com.golflearn.domain.entity.MeetMemberEntity;
import com.golflearn.domain.repository.MeetBoardRepository;
import com.golflearn.domain.repository.MeetCategoryRepository;
import com.golflearn.domain.repository.MeetMemberRepository;
import com.golflearn.dto.MeetBoardDto;
import com.golflearn.dto.MeetCategoryDto;
import com.golflearn.dto.PageBean;
import com.golflearn.exception.AddException;
import com.golflearn.exception.FindException;
import com.golflearn.exception.ModifyException;
import com.golflearn.exception.RemoveException;

@Service
public class MeetBoardService {
	private static final int CNT_PER_PAGE = 5; //페이지별 보여줄 목록수
	@Autowired
	private MeetBoardRepository meetBoardRepo;
	@Autowired
	private MeetMemberRepository meetMemberRepo;
	@Autowired
	private MeetCategoryRepository meetCategoryRepo;
	
	/**
	 * 페이지별 모임글 목록과 페이지그룹정보를 반환한다
	 * @param currentPage 검색할 페이지
	 * @return
	 * @throws FindException
	 */
	@Transactional(readOnly = true)
	public PageBean<MeetBoardDto> meetBoardList(int currentPage) throws FindException{
		ModelMapper modelMapper = new ModelMapper();
		
		int endRow = currentPage * CNT_PER_PAGE;
		int startRow = endRow - CNT_PER_PAGE + 1;
		long totalCnt = meetBoardRepo.count();
		int CntPerPageGroup = 5;//페이지별 보여줄 페이지수 
		
		//List타입의 Entity 생성후 findByPage반환
		List<MeetBoardEntity> list = meetBoardRepo.findByPage(startRow, endRow);
		//ModelMapper로 Entity를 Dto로 변환하여 Dto객체에 반환
		List<MeetBoardDto> dtoList = list.stream()
									 .map(MeetBoardEntity -> modelMapper
									      .map(MeetBoardEntity, MeetBoardDto.class))
									 		   .collect(Collectors.toList());
		//Dto객체에 들어있는 모임글목록을 페이징처리
		PageBean<MeetBoardDto>  pb = new PageBean<>(dtoList, totalCnt, currentPage, CntPerPageGroup, CNT_PER_PAGE);
		return pb;	
	}
	
	/**
	 * 검색어를 이용한 게시글 검색 목록과 페이지 그룹정보를 반환한다
	 * @param word 검색어
	 * @param currentPage 검색할 페이지
	 * @return
	 * @throws FindException
	 */
	public PageBean<MeetBoardDto> searchMeetBoard(String word, int currentPage) throws FindException{
		ModelMapper modelMapper = new ModelMapper();

		int endRow = currentPage * CNT_PER_PAGE;
		int startRow = endRow - CNT_PER_PAGE + 1;
		List<MeetBoardEntity> list = meetBoardRepo.findByWordAndPage(word, startRow, endRow);
		long totalCnt = meetBoardRepo.countByWord(word);//총건수
		int CntPerPageGroup = 5;//페이지별 보여줄 페이지수 
		
		List<MeetBoardDto> dtoList = list.stream()
									 	  .map(MeetBoardEntity -> modelMapper
									      .map(MeetBoardEntity, MeetBoardDto.class))
									 		   .collect(Collectors.toList());
		
		PageBean<MeetBoardDto>  pb = new PageBean<>(dtoList, totalCnt, currentPage, CntPerPageGroup, CNT_PER_PAGE);
		return pb;
	}
	

	/**
	 * 모집상태별 게시글 목록과 페이지 그룹정보를 반환한다
	 * @param meetBoardStatus 모집상태
	 * @param currentPage 
	 * @return
	 * @throws FindException
	 */
	public PageBean<MeetBoardDto> filterMeetBoard(Long meetBoardStatus, int currentPage) throws FindException{
		ModelMapper modelMapper = new ModelMapper();

		int endRow = currentPage * CNT_PER_PAGE;
		int startRow = endRow - CNT_PER_PAGE + 1;
		List<MeetBoardEntity> list = meetBoardRepo.findByStatusAndPage(meetBoardStatus, startRow, endRow);
		long totalCnt = meetBoardRepo.countByMeetBoardStatus(meetBoardStatus);//총건수
		int CntPerPageGroup = 5;//페이지별 보여줄 페이지수 
		
		List<MeetBoardDto> dtoList = list.stream()
									 	  .map(MeetBoardEntity -> modelMapper
									      .map(MeetBoardEntity, MeetBoardDto.class))
									 		   .collect(Collectors.toList());
		
		PageBean<MeetBoardDto>  pb = new PageBean<>(dtoList, totalCnt, currentPage, CntPerPageGroup, CNT_PER_PAGE);
		return pb;
	}
	
	/**
	 * 내가 참여한 모임 게시글 목록과 페이지 그룹정보를 반환한다
	 * @param currentPage
	 * @return
	 * @throws FindException
	 */
	public PageBean<MeetBoardDto> viewMyMeetBoard(String userNickname, int currentPage) throws FindException{
		ModelMapper modelMapper = new ModelMapper();
		int endRow = currentPage * CNT_PER_PAGE;
		int startRow = endRow - CNT_PER_PAGE + 1;
		
		List<MeetBoardEntity> list = meetBoardRepo.findByUserNickNameAndPage(userNickname, startRow, endRow);
		long totalCnt = meetBoardRepo.countByUserNicakname(userNickname);//총건수
		int CntPerPageGroup = 5;//페이지별 보여줄 페이지수 
		
		List<MeetBoardDto> dtoList = list.stream()
									 	  .map(MeetBoardEntity -> modelMapper
									      .map(MeetBoardEntity, MeetBoardDto.class))
									 	  .collect(Collectors.toList());
		
		PageBean<MeetBoardDto>  pb = new PageBean<>(dtoList, totalCnt, currentPage, CntPerPageGroup, CNT_PER_PAGE);
		return pb;
	}
	
	/** 
	 * 모임글번호의 조회수를 1증가한다
	 * 게시글번호의 게시글을 반환한다
	 * @param meetBoardNo 게시글번호
	 * @return
	 * @throws FindException
	 */
	public MeetBoardDto viewMeetBoard(Long meetBoardNo) throws FindException{
		ModelMapper modelMapper = new ModelMapper();
		//---------조회수가 1증가한다--------
		Optional<MeetBoardEntity> optE = meetBoardRepo.findById(meetBoardNo);
		if(optE.isPresent()) {
			MeetBoardEntity m = optE.get();
			m.setMeetBoardViewCnt(m.getMeetBoardViewCnt()+1);//조회수를 1증가한다
			meetBoardRepo.save(m);
		}else {
			throw new FindException("게시글이 없습니다.");
		}
		//---------게시글번호의 게시글 조회한다--------
		Optional<MeetBoardEntity> optE1 = meetBoardRepo.findById(meetBoardNo);//조회수가 1 증가된 해당게시글
		if(optE1.isPresent()) {
			MeetBoardEntity entity = optE1.get();//Entity에 해당 게시글 받기
			MeetBoardDto dto = modelMapper.map(entity, MeetBoardDto.class);//Entity -> Dto
			return dto;
		}else {
			throw new FindException("게시글이 없습니다.");
		}
	}

	/**
	 * 글 작성시 모집유형 리스트를 반환한다
	 * @param meetCtgNo
	 * @return
	 * @throws FindException
	 */
	public List<MeetCategoryDto> meetCategoryList() throws FindException{
		ModelMapper modelMapper = new ModelMapper();
		List<MeetCategoryEntity> list = meetCategoryRepo.findAll();
		List<MeetCategoryDto> dtoList = list.stream()
									 	  .map(MeetCategoryEntity -> modelMapper
									      .map(MeetCategoryEntity, MeetCategoryDto.class))
									 		   .collect(Collectors.toList());
		return dtoList;
	}
	
	/**
	 * 글쓰기
	 * @param meetBoard 글내용
	 * @throws AddException
	 */
	@Transactional
	public void writeMeetBoard(MeetBoardDto meetBoardDto, Long meetCtgNo) throws AddException{
		ModelMapper modelMapper = new ModelMapper();
		//------모집유형을 선택한다-------
		Optional<MeetCategoryEntity> optC = meetCategoryRepo.findById(meetCtgNo);
		MeetCategoryEntity meetCtg = optC.get();
		
		//-------글 내용을 작성한다---------
		MeetBoardEntity meetBoardEntity = modelMapper.map(meetBoardDto, MeetBoardEntity.class);//Dto->Entity
		meetBoardEntity.setMeetCategory(meetCtg);//선택한 모집유형 저장 
		meetBoardRepo.save(meetBoardEntity);//글 저장
		
		//-------작성자는 자동으로 모임참가자목록에 추가된다---------
		MeetMemberEntity member = new MeetMemberEntity();
		member.setMeetBoard(meetBoardEntity);//작성된 게시글 번호 저장
		member.setUserNickname(meetBoardEntity.getUserNickname());//세션의 로그인닉네임으로 작성된 게시글의 작성자 저장
		meetMemberRepo.save(member);
	}
	
	/**
	 * 삭제하기
	 * @param meetBoardNo
	 * @throws RemoveException
	 */
	@Transactional
	public void removeMeetBoard(String userNickname, Long meetBoardNo) throws RemoveException{
		Optional<MeetBoardEntity> meetBoard = meetBoardRepo.findById(meetBoardNo);//선택된 게시글 반환
		String writer = meetBoard.get().getUserNickname();//해당 게시글의 작성자 반환
		if(!userNickname.equals(writer)) {//로그인된 유저가 작성자인지 확인 
			throw new RemoveException("해당 글의 작성자만 삭제할 수 있습니다.");
		}else {
			meetMemberRepo.DeleteByBoardNo(meetBoardNo);
			meetBoardRepo.findById(meetBoardNo).orElseThrow(() ->  new RemoveException("존재하지 않는 게시글입니다."));
			meetBoardRepo.deleteById(meetBoardNo);
		}
	}

	/**
	 * 수정하기
	 * @param meetBoardDto
	 * @throws ModifyException
	 */
	public void modifyMeetBoard(MeetBoardDto meetBoardDto) throws ModifyException{
		ModelMapper modelMapper = new ModelMapper();
		
		MeetBoardEntity meetBoardEntity = modelMapper.map(meetBoardDto, MeetBoardEntity.class);
		//수정할 게시글을 가져온다
		Optional<MeetBoardEntity> optM = meetBoardRepo.findById(meetBoardEntity.getMeetBoardNo());
		String writer = optM.get().getUserNickname();//작성자 가져오기
		if(!optM.isPresent()) {//게시글 존재여부 확인
			throw new ModifyException("존재하지 않는 게시글입니다.");
		}else if(!meetBoardDto.getUserNickname().equals(writer)){//작성자여부 확인
			throw new ModifyException("작성자만 수정할 수 있습니다.");
		}else{
			MeetBoardEntity meetBoard = optM.get();
			meetBoard.setMeetBoardTitle(meetBoardDto.getMeetBoardTitle());
			meetBoard.setMeetBoardContent(meetBoardDto.getMeetBoardContent());
			meetBoard.setMeetBoardLocation(meetBoardDto.getMeetBoardLocation());
			meetBoard.setMeetBoardMaxCnt(meetBoardDto.getMeetBoardMaxCnt());

			meetBoardRepo.save(meetBoard);
		}
	}
	
	/**
	 * 글 작성자가 모임을 모집종료한다
	 * @param meetBoardNo 글번호
	 * @param meetBoardStatus 모집상태
	 * @throws ModifyException
	 */
	public void modifyStatus(String userNickname, Long meetBoardNo, Long meetBoardStatus) throws ModifyException{
		Optional <MeetBoardEntity> optM = meetBoardRepo.findById(meetBoardNo);
		MeetBoardEntity meetBoard = optM.get();
		String writer = meetBoard.getUserNickname();//해당 게시글의 작성자 닉네임 반환
		if(!optM.isPresent()){//게시글 존재여부 확인
			throw new ModifyException("존재하지 않는 게시글입니다.");
		}else if(!userNickname.equals(writer)){//작성자 여부 확인
			throw new ModifyException("해당 글의 작성자만 수정할 수 있습니다.");
		}else{
			meetMemberRepo.DeleteByBoardNo(meetBoardNo);//모임멤버 삭제하기
			meetBoard.setMeetBoardStatus(meetBoardStatus); //글의 모집상태 변경하기
			meetBoardRepo.save(meetBoard);
		}
	}
	
	/**
	 * 모임에 참여한다
	 * @param meetBoardNo 참여할 모임의 글번호
	 * @param meetMemberDto
	 * @throws AddException
	 */
	@Transactional
	public void addMember(String userNickname, Long meetBoardNo) throws AddException{
		Optional <MeetBoardEntity> optM = meetBoardRepo.findById(meetBoardNo);
		MeetBoardEntity meetBoard = optM.get();//게시글 가져오기
		int memberCheck = meetMemberRepo.countByUserNicknameMeetBoard(userNickname, meetBoardNo);//참여중인 모임인지 확인
		
		if(!optM.isPresent()) {//글이 없는 경우
			throw new AddException("글이 없습니다.");
		}else if(memberCheck  != 0){//이미 참여중인 경우
			throw new AddException("이미 참여중인 모임입니다.");
		}else if(meetBoard.getMeetBoardStatus() == 1 ){//해당 모임글이 모집마감인 경우
			throw new AddException("모집중인 모임이 아닙니다");
		}else{
			meetBoard.setMeetBaordCurCnt(meetBoard.getMeetBaordCurCnt()+1);//현재인원을 1증가 
			meetBoardRepo.save(meetBoard);
			
			MeetMemberEntity meetMember= new MeetMemberEntity();//DB에 저장할 모임멤버 객체 생성
			meetMember.setMeetBoard(meetBoard);
			meetMember.setUserNickname(userNickname);
			meetMemberRepo.save(meetMember);//모임에 이미 참여인 경우 DB에서 유니크제약조건 발동
		}
	}
	
	/**
	 * 모임에서 나간다
	 * @param meetBoardNo 글 번호
	 * @param userNickname 모임에서 나가는 회원의 닉네임 
	 */
	@Transactional
	public void removeMeetMember(Long meetBoardNo, String userNickname) throws RemoveException{
		Optional <MeetBoardEntity> optM = meetBoardRepo.findById(meetBoardNo);
		MeetBoardEntity meetBoard = optM.get();//게시글 가져오기
		int memberCheck = meetMemberRepo.countByUserNicknameMeetBoard(userNickname, meetBoardNo);//참여중인 모임인지 확인
		if(!optM.isPresent()) {//글이 없는 경우
			throw new RemoveException("글이 없습니다");
		}else if(memberCheck == 0){//참여중인 모임이 아닌 경우
			throw new RemoveException("참여중인 모임이 아닙니다");
		}else{
			meetBoard.setMeetBaordCurCnt(meetBoard.getMeetBaordCurCnt()-1);//현재인원 감소 
			meetBoardRepo.save(meetBoard);
			meetMemberRepo.DeleteByIdAndUserNickName(meetBoardNo, userNickname);
		}
	}
	
	/** 잡 스케쥴러 이용할 것
	 * 모임일이 지난 글의 모집상태가 변화한다
	 * 모임일이 지난 모임의 참가자가 삭제된다
	 * @param meetBoardNo
	 */
	public void endMeetBoard(Long meetBoardNo) throws ModifyException{
		//--------------모집상태가 변화한다----------
		Optional<MeetBoardEntity> optM = meetBoardRepo.findById(meetBoardNo);
		if(!optM.isPresent()) {
			throw new ModifyException("글이 없습니다");
		}else {
			MeetBoardEntity meetBoard = optM.get();
			meetBoard.setMeetBoardStatus(meetBoardNo);
			meetBoardRepo.save(meetBoard);
		}
		//---------모임일 지난 모임의 참가자가 삭제된다-----------
		meetMemberRepo.DeleteByBoardNo(meetBoardNo);
	}
	
}