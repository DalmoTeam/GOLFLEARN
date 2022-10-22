package com.golflearn.control;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.golflearn.dto.MeetBoardDto;
import com.golflearn.dto.MeetCategoryDto;
import com.golflearn.dto.PageBean;
import com.golflearn.dto.ResultBean;
import com.golflearn.exception.AddException;
import com.golflearn.exception.FindException;
import com.golflearn.exception.ModifyException;
import com.golflearn.exception.RemoveException;
import com.golflearn.service.MeetBoardService;

@CrossOrigin(origins = "*")//모든포트에서 접속가능 //메서드마다 각각 설정가능
@RestController
@RequestMapping("/backmeet/meet/board/*")
public class MeetBoardController {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private MeetBoardService service;
	
	//게시글 목록보기
	@GetMapping(value = { "list", "list/{optCp}" }) 
	public ResultBean<PageBean<MeetBoardDto>> list(@PathVariable Optional<Integer> optCp) {
		ResultBean<PageBean<MeetBoardDto>> rb = new ResultBean<>();
		try {
			int currentPage;
			if (optCp.isPresent()) {
				currentPage = optCp.get();
			} else {
				currentPage = 1;
			}
			PageBean<MeetBoardDto> pb = service.meetBoardList(currentPage);
			rb.setStatus(1);
			rb.setT(pb);
		} catch (FindException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb;
	}
	
	//모집상태에 따른 게시글 목록보기
	@GetMapping(value = { "filter", "filter/{optStatus}", "filter/{optStatus}/{optCp}" })
	public ResultBean<PageBean<MeetBoardDto>> filter(@PathVariable Optional<Long> optStatus, @PathVariable Optional<Integer> optCp) {
		ResultBean<PageBean<MeetBoardDto>> rb = new ResultBean<>();
		try {
			PageBean<MeetBoardDto> pb;
			Long meetBoardStatus ;
			if (optStatus.isPresent()) {//선택한 모집상태가 있으면
				meetBoardStatus = optStatus.get();
			} else {
				meetBoardStatus = null;
			}
			int currentPage = 1;
			if (optCp.isPresent()) {//선택한 페이지가 있으면
				currentPage = optCp.get();
			}
			if (meetBoardStatus == null) {//모집상태필터를 선택하지 않은 경우
				pb = service.meetBoardList(currentPage);
			} else {//선택한 경우
				pb = service.filterMeetBoard(meetBoardStatus, currentPage);
			}
			rb.setStatus(1);
			rb.setT(pb);
		} catch (FindException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb;
	}

	// 게시글목록에서 제목 검색하기
	// GET /meet/search/검색어/페이지번호 
	// GET /meet/search/검색어 
	@GetMapping(value = { "search", "search/{optWord}", "search/{optWord}/{optCp}" })
	public ResultBean<PageBean<MeetBoardDto>> search(@PathVariable Optional<Integer> optCp, @PathVariable Optional<String> optWord) {
		ResultBean<PageBean<MeetBoardDto>> rb = new ResultBean<>();
		try {
			PageBean<MeetBoardDto> pb;
			String word;
			if (optWord.isPresent()) {//검색어 확인
				word = optWord.get();
			} else {
				word = "";
			}
			int currentPage = 1;
			if (optCp.isPresent()) {//선택한 페이지 확인
				currentPage = optCp.get();
			}
			if ("".equals(word)) {//검색어 없는 경우
				pb = service.meetBoardList(currentPage);
			} else {//있는경우
				pb = service.searchMeetBoard(word, currentPage);
			}
			rb.setStatus(1);
			rb.setT(pb);
		} catch (FindException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb;
	}
	
	//참여중인 모임글 목록보기
	@GetMapping(value = { "mylist", "mylist/{optCp}"})
	public ResultBean<PageBean<MeetBoardDto>> myList(@PathVariable Optional<Integer> optCp, @RequestParam String loginedNickname) {
		ResultBean<PageBean<MeetBoardDto>> rb = new ResultBean<>();
		try {
			PageBean<MeetBoardDto> pb;
			int currentPage = 1;
			if(loginedNickname == null){//로그인하지 않은 경우
			rb.setStatus(0);
				throw new FindException("로그인이 필요합니다.");
			} else if (optCp.isPresent()) {//선택한 페이지가 있는 경우
				rb.setStatus(1);
				currentPage = optCp.get();
				pb = service.viewMyMeetBoard(loginedNickname, currentPage);
			} else {//선택한 페이지가 없는 경우	
				rb.setStatus(1);
				currentPage = 1;
				pb = service.viewMyMeetBoard(loginedNickname, currentPage);
			}
			rb.setT(pb);
		} catch (FindException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb;
	}

	//게시글 상세보기
	@GetMapping("{meetBoardNo}")
	public ResultBean<MeetBoardDto> viewBoard(@PathVariable long meetBoardNo) {
		ResultBean<MeetBoardDto> rb = new ResultBean<>();
		try {
			MeetBoardDto m = service.viewMeetBoard(meetBoardNo);
			rb.setStatus(1);
			rb.setT(m);
		} catch (FindException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb;
	}
	
	//게시글 작성시 모임유형 드롭다운으로 받아오기
	@GetMapping("selectctg")
	public  List<MeetCategoryDto> getoption() throws FindException{
			 List<MeetCategoryDto> optionList = service.meetCategoryList();
			 return optionList;
	}	
	
	//게시글 작성하기
	@PostMapping("write")
	public ResponseEntity<Object> write(@RequestBody MeetBoardDto m){
//		logger.error("title: " + m.getMeetBoardTitle() );
		if(m.getUserNickname() == null) {//로그인하지 않은 경우
			return new ResponseEntity<>("로그인이 필요합니다", HttpStatus.BAD_REQUEST);
		}else {
			//입력내용 유효성검사
			if (m.getMeetBoardTitle() == null || m.getMeetBoardTitle().equals("") || m.getMeetBoardContent() == null
					|| m.getMeetBoardLocation() == null || m.getMeetBoardMaxCnt() == null || m.getMeetBoardMeetDt() == null) {
				return new ResponseEntity<>("항목을 모두 입력하세요", HttpStatus.BAD_REQUEST);
			}
			try {
				long meetCtgNo = m.getMeetCategory().getMeetCtgNo();//모임유형번호
				service.writeMeetBoard(m, meetCtgNo);
				return new ResponseEntity<>(HttpStatus.OK);
			} catch (AddException e) {
				e.printStackTrace();
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	//게시글 삭제하기
	@DeleteMapping(value = "{meetBoardNo}")
	public ResponseEntity<String> remove(@PathVariable Long meetBoardNo, @RequestParam String loginedNickname) {
		try {
			service.removeMeetBoard(loginedNickname, meetBoardNo);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (RemoveException e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	//게시글 수정하기
	@PutMapping(value = "{meetBoardNo}")
	public ResponseEntity<Object> modify(@PathVariable long meetBoardNo, @RequestBody MeetBoardDto m) {
		try {
			if (m.getMeetBoardContent() == null || m.getMeetBoardContent().equals("") || m.getMeetBoardTitle() == null
				|| m.getMeetBoardLocation() == null || m.getMeetBoardMaxCnt() == null) {//글내용 입력확인
				return new ResponseEntity<>("항목을 모두 입력하세요", HttpStatus.BAD_REQUEST);
			}else {
				m.setMeetBoardNo(meetBoardNo);//수정하는 글의 글번호
				service.modifyMeetBoard(m);//글 내용 저장
				return new ResponseEntity<>(HttpStatus.OK);
			}
		} catch (ModifyException e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 글 작성자가 모집종료하기
	@PutMapping(value = "update/{meetBoardNo}")	
	public ResponseEntity<String> modifySatus(@PathVariable Long meetBoardNo, @RequestBody MeetBoardDto meetBoardDto) {
		try {
			String loginedNickname = meetBoardDto.getUserNickname();
			Long meetBoardStatus = 1L;
			service.modifyStatus(loginedNickname, meetBoardNo, meetBoardStatus);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (ModifyException e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 모임에 참여한다
	@PostMapping(value = "add/{meetBoardNo}")
	public ResponseEntity<String> addMember(@PathVariable long meetBoardNo, @RequestParam String loginedNickname) {
		if (loginedNickname == null) {//로그인하지 않은 경우 
			return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		} else {
			try {
				service.addMember(loginedNickname, meetBoardNo);//모임멤버에 추가 
				return new ResponseEntity<>(HttpStatus.OK);
			} catch (AddException e) {
				e.printStackTrace();
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	//모임에서 나간다
	@DeleteMapping(value = "leave/{meetBoardNo}")
	public ResponseEntity<Object>  deleteMember(@PathVariable Long meetBoardNo, @RequestParam String loginedNickname){
		try {
			service.removeMeetMember(meetBoardNo, loginedNickname);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (RemoveException e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
