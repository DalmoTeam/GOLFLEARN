package com.golflearn.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.golflearn.dto.ResaleBoardDto;
import com.golflearn.dto.ResaleCommentDto;
import com.golflearn.dto.ResaleLikeDto;
import com.golflearn.dto.ResultBean;
import com.golflearn.exception.AddException;
import com.golflearn.exception.FindException;
import com.golflearn.exception.ModifyException;
import com.golflearn.exception.RemoveException;
import com.golflearn.service.ResaleBoardService;

import net.coobird.thumbnailator.Thumbnailator;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("resale/*")
public class ResaleBoardContoller {
	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ResaleBoardService service;

	@Autowired
	private ServletContext sc;

	// 파일 저장 경로
	String uploadDirectory = "C://Project/GolfLearn/BackResale/images/";
	// 도커 사용 시 - 상대 경로
//	String uploadDirectory = "/images/";

	/**
	 * 게시글 목록보기
	 * @param optCp
	 * @return
	 */
	@GetMapping(value={"board/list","board/list/{optCp}"})
	public ResultBean<Page<ResaleBoardDto>> boardList(@PathVariable Optional<Integer> optCp,
			@PageableDefault(page = 0, size = 5, sort = "resaleBoardNo", direction = Direction.DESC) Pageable pageable){
		// 요청전달데이터 전달 되지 않을 때를 대비하여 사용하는 RESTful의  @PathVariable은 Optional로 설정 

		ResultBean<Page<ResaleBoardDto>> rb = new ResultBean<>();
		try {
			int currentPage;
			if(optCp.isPresent()) { //currentPage 가 있으면
				currentPage = optCp.get() -1 ; // pageable -> 0페이지가 시작
			}else { // 없으면
				currentPage = 0; // 1페이지
			}
			Page<ResaleBoardDto> pb = service.boardList(currentPage);
			rb.setStatus(1);
			rb.setT(pb);			
		} catch (FindException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb;
	}

	/**
	 * 게시글 상세보기
	 * @param resaleBoardNo
	 * @return
	 */
	@GetMapping(value = "board/{resaleBoardNo}")
	public ResultBean<Map<String, Object>> viewBoardDetail(@PathVariable Long resaleBoardNo){

		Map<String, Object> map = new HashMap<>();

		try {
			ResaleBoardDto resaleBoard = service.boardDetail(resaleBoardNo);
			map.put("resaleBoard", resaleBoard);
			//map.put("status", 1);
		} catch (FindException e) {
			e.printStackTrace();
			map.put("status", 0);
		}

		// 저장된 이미지 파일의 이름을 가지고 오는 것 -> 사진 불러올 때 저장된 개수만큼 불러와야함
		String saveDirectory = uploadDirectory +"/"+ "resale_images" + "/" +resaleBoardNo + "/";
		
		File dir = new File(saveDirectory);
		
		// 이미지명 반환
		String[] imageFiles = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.contains("image_");
			} //image라는 이름을 포함한 이미지명들 반환
		});

		map.put("imageFileNames", imageFiles);

		ResultBean<Map<String,Object>> rb = new ResultBean<>();
		rb.setStatus(1);
		rb.setT(map);	

		return rb;
	}

	/**
	 * 검색어로 게시글 조회
	 * @param optWord
	 * @param optCp
	 * @return
	 */
	@GetMapping(value = {"board/search/{optWord}/{optCp}", "board/search/{optWord}", "board/search"})
	public ResultBean<Page<ResaleBoardDto>> search(
			@PathVariable Optional<String> optWord,
			@PathVariable Optional<Integer> optCp,
			@PageableDefault(page = 0, size = 5, sort = "resaleBoardNo", direction = Direction.DESC) Pageable pageable){
		// 5페이지씩, 5개 게시글씩, 최신순으로 나열
		ResultBean<Page<ResaleBoardDto>> rb = new ResultBean<>();

		try {
			Page<ResaleBoardDto> pb ; 
			String word = ""; 
			if(optWord.isPresent()) {
				word = optWord.get();
			} else { 
				word = "";
			}

			int currentPage;
			if(optCp.isPresent()) {
				currentPage = optCp.get()-1; 
			} else {
				currentPage = 0;
			}

			if("".equals(word)) {
				pb = service.boardList(currentPage);
			} else {
				pb = service.searchBoard(word, currentPage);
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

	/**
	 * 게시글 등록
	 * @param imageFiles
	 * @param dto
	 * @param session
	 * @return
	 * @throws AddException 
	 */
	@PostMapping(value = "board/write", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> writeBoard (@RequestPart(required = false)List<MultipartFile> imageFiles,
			ResaleBoardDto dto) {
		logger.error("board/write start");
		// 입력 내용 게시글 저장
		ResaleBoardDto boardDto = new ResaleBoardDto();
		//		String loginedNickname = "땡초"; // 테스트용 로그인 정보 
		try {
			dto.setUserNickname(dto.getUserNickname());
			boardDto = service.writeBoard(dto);
		} catch (AddException e1) {
			e1.printStackTrace();
		}
//		logger.error("setUserNickName");
		Long resaleBoardNo = boardDto.getResaleBoardNo();
		//		logger.error("글번호는"+boardDto.getResaleBoardNo());

		// 파일 저장 폴더
		String saveDirectory = uploadDirectory + "resale_images/"+ resaleBoardNo;
		//파일 경로 생성
		if(!new File(saveDirectory).exists()) {
			new File(saveDirectory).mkdirs(); //파일 경로에 폴더 없으면 저장
		}
		
		logger.error("saveDirectory : " + new File(saveDirectory).getAbsolutePath());
		//이미지 저장
		int savedImgFileCnt = 0; // 서버에 저장된 파일 수
		if(!imageFiles.isEmpty()) {
		File thumbnailFile = null;
			for(MultipartFile imageFile : imageFiles) {
				Long imageFileSize = imageFile.getSize(); // 파일 크기
				if(imageFileSize > 0) { // 파일이 첨부되었을 경우

					// 파일 확장자 가지고 오기
					String originFileName = imageFile.getOriginalFilename();
					logger.error("파일이름은 " + originFileName);
					String fileExtension = originFileName.substring(originFileName.lastIndexOf("."));
					logger.error("파일 확장자는" + fileExtension);

					//저장파일 이름 생성
					String savedImageFileName = "image_"+ (savedImgFileCnt+1) + fileExtension;
					//이미지 파일 생성
					File savedImageFile = new File(saveDirectory, savedImageFileName);

					try {
						// 파일 저장
						FileCopyUtils.copy(imageFile.getBytes(), savedImageFile);

						// 파일 타입 확인
						String contentType = imageFile.getContentType();
						if(contentType.contains("image/*")) {
							System.out.println("파일타입" + imageFile.getContentType());
							return new ResponseEntity<> ("이미지 파일이 아닙니다", HttpStatus.INTERNAL_SERVER_ERROR) ;
						}
						savedImgFileCnt++; 

						//썸네일 만들기
						String thumbnailName = "s_" + (savedImgFileCnt) + ".jpg";
						thumbnailFile = new File(saveDirectory, thumbnailName);
						
						FileOutputStream thumbnailOS = new FileOutputStream(thumbnailFile);
						InputStream imageFileIS = imageFile.getInputStream();
						int width = 100;
						int height = 100;
						Thumbnailator.createThumbnail(imageFileIS, thumbnailOS , width, height);
					} catch (IOException e) {
						e.printStackTrace();
						return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
					}
					//						
				}else {
					logger.error("이미지 파일이 없습니다");
					return new ResponseEntity<>("이미지 파일이 없습니다", HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		} 
		return new ResponseEntity<>("저장 완료", HttpStatus.OK);

	}


	/**
	 * 게시글 수정 
	 * 로그인 닉네임과 게시글 작성자 닉네임과 같으면 수정 가능
	 * @param resaleBoardNo
	 * @param dto
	 * @return
	 */
	@PutMapping(value="board/{resaleBoardNo}",produces = MediaType.APPLICATION_JSON_VALUE) 
	public ResponseEntity<?> modifyBoard(@PathVariable Long resaleBoardNo, 
			@RequestBody ResaleBoardDto dto){

		if(dto.getResaleBoardTitle() == null || dto.getResaleBoardTitle().equals("")|| 
				dto.getResaleBoardContent() == null || dto.getResaleBoardContent().equals("")) {
			return new ResponseEntity<>("글 내용이나 제목은 반드시 입력하세요", HttpStatus.INTERNAL_SERVER_ERROR);
		}else { // 제목, 내용 있으면
			try {
				dto.setResaleBoardNo(resaleBoardNo);
				service.modifyBoard(dto);
				return new ResponseEntity<>(HttpStatus.OK);
			} catch (ModifyException e) {
				e.printStackTrace();
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	} 
	

	/**
	 * 게시글 삭제
	 * @param resaleBoardNo
	 * @param dto
	 * @param session
	 * @return
	 */
	@DeleteMapping(value="board/{resaleBoardNo}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResultBean<ResaleBoardDto> removeBoard (@PathVariable Long resaleBoardNo,
			@RequestBody ResaleBoardDto dto) { 
		//		String loginedNickname = (String) session.getAttribute("loginNickname");
		//테스트용 닉네임
		logger.error("닉네임"+dto.getUserNickname());
		ResultBean<ResaleBoardDto> rb = new ResultBean<>();
		try {
			dto.setResaleBoardNo(resaleBoardNo);
			service.removeBoard(resaleBoardNo);
			rb.setStatus(1);
			rb.setMsg("삭제 성공");
		} catch (RemoveException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb; 
	}


	/**
	 * 댓글 등록
	 * @param boardDto
	 * @param commentDto
	 * @param session
	 * @return
	 */
	@PostMapping(value= "comment/write")
	public ResultBean<ResaleCommentDto> writeComment(@RequestBody ResaleCommentDto commentDto){
		ResultBean<ResaleCommentDto> rb = new ResultBean<>();
		try {
			//commentDto.setUserNickname(loginedNickname); commentDto에 localStorage에 저장된 userNickname을 넣어서 보내줌
			service.writeComment(commentDto);
			logger.error("부모댓글"+commentDto.getResaleCmtParentNo());
			logger.error("원글번호는 "+commentDto.getResaleBoard().getResaleBoardNo());

			rb.setStatus(1);
			rb.setMsg("댓글 등록 완료");
		} catch (AddException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg("댓글 등록 실패");
		}
		return rb;
	}

	
	/**
	 * 대댓글 등록
	 * @param boardDto
	 * @param commentDto
	 * @param session
	 * @return
	 */
	@PostMapping(value= "recomment/write")
	public ResultBean<ResaleCommentDto> writeReComment(@RequestBody ResaleCommentDto commentDto){
		ResultBean<ResaleCommentDto> rb = new ResultBean<>();
		try {
			//commentDto.setUserNickname(loginedNickname); commentDto에 localStorage에 저장된 userNickname을 넣어서 보내줌
			service.writeReComment(commentDto);
			logger.error("부모댓글"+commentDto.getResaleCmtParentNo());
			logger.error("원글번호는 "+commentDto.getResaleBoard().getResaleBoardNo());

			rb.setStatus(1);
			rb.setMsg("대댓글 등록 완료");
		} catch (AddException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg("대댓글 등록 실패");
		}
		return rb;
	}
	
	/**
	 * 댓글 삭제
	 * @param commentDto
	 * @param session
	 * @return
	 */
	@DeleteMapping(value = "comment/{resaleCmtNo}")
	public ResultBean<ResaleCommentDto> deleteComments(@PathVariable Long resaleCmtNo,
			@RequestBody ResaleCommentDto commentDto){
		// String loginedNickname = (String) session.getAttribute("loginNickname");
		//		String loginedNickname = "개발자";

		ResultBean<ResaleCommentDto> rb = new ResultBean<>();

		try {
			service.deleteComment(commentDto);
			rb.setStatus(1);
			rb.setMsg("댓글 삭제 성공");
		} catch (RemoveException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg("댓글 삭제 실패");
		}
		return rb;
	}


	/**
	 * (대)댓글 수정(완성)
	 * @param resaleCmtNo
	 * @param dto
	 * @param session
	 * @return
	 */
	@PutMapping(value = "comment/{resaleCmtNo}" ,produces = MediaType.APPLICATION_JSON_VALUE)
	public ResultBean<ResaleCommentDto> modifyComment(@PathVariable Long resaleCmtNo,
			@RequestBody ResaleCommentDto dto){
		//	String loginedNickname = (String)session.getAttribute("loginNickname");
		//		String loginedNickname = "쩐승";

		ResultBean<ResaleCommentDto> rb = new ResultBean<>();
		try {
			if(dto.getResaleCmtContent() == null || dto.getResaleCmtContent().equals("")) {
				rb.setMsg("내용을 반드시 입력해주세요");
			}else {
				dto.setResaleCmtNo(resaleCmtNo);
				service.modifyComment(dto);
				rb.setStatus(1);
				rb.setMsg("수정 성공");
			}
		}catch(ModifyException e){
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg("수정 실패");
		}
		return rb;
	}


	/**
	 * 좋아요 추가
	 * @param likeDto
	 * @param boardDto
	 * @param session
	 * @return
	 */
	@PostMapping(value = "like/add", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResultBean<ResaleLikeDto> addLike(@RequestBody ResaleLikeDto likeDto){

		//		String loginedNickname = (String) session.getAttribute("loginNickname");
//		String loginedNickname = "데빌";
		ResultBean<ResaleLikeDto> rb = new ResultBean<>();
		try {
			service.addLike(likeDto);
			rb.setStatus(1);
			rb.setMsg("좋아요 추가 성공");
		} catch (AddException e) {
			e.printStackTrace();
			rb.setStatus(1);
			rb.setMsg("좋아요 추가 실패");
		}
		return rb;
	}

	/**
	 * 좋아요 삭제
	 * @param resaleLikeNo
	 * @param dto
	 * @param session
	 * @return
	 */
	@DeleteMapping(value = "like/{resaleLikeNo}",produces = MediaType.APPLICATION_JSON_VALUE) //Json 형태로 return?!
	public ResultBean<ResaleLikeDto> removeLike(@PathVariable Long resaleLikeNo,//Long resaleLikeNo, 
			@RequestBody ResaleLikeDto likeDto){

		//String loginedNickname = (String)session.getAttribute("loginNickname");
		//		userNickname = "케빈";
		logger.error("이름은 " + likeDto.getUserNickname());
		//		logger.error("번호는" + likeDto.getResaleLikeNo());
		logger.error("원글번호" + likeDto.getResaleBoard().getResaleBoardNo()); // null에러
		ResultBean<ResaleLikeDto> rb = new ResultBean<>(); // 객체 생성
		try {
			likeDto.getResaleBoard().getResaleBoardNo(); // null에러
			logger.error("원글 번호는"+likeDto.getResaleBoard().getResaleBoardNo()); // null

			likeDto.setUserNickname(likeDto.getUserNickname());
			service.removeLike(likeDto);
			rb.setStatus(1);
			rb.setMsg("좋아요 삭제 성공");
		} catch (RemoveException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg("좋아요 삭제 실패");
		}
		return rb;
	}
	
	/*
	 * 게시글 목록 - 썸네일 파일 다운로드
	 */
	@GetMapping(value ="/downloadimage")///{resaleBoardNo}") //GetMapping 사용 가능
	public ResponseEntity<?>  downloadImage(String resaleBoardNo){//@PathVariable String resaleBoardNo){//String imageFileName) {
		File thumbnailFile = new File(uploadDirectory+"/resale_images/"+resaleBoardNo, "s_1.jpg");
		HttpHeaders responseHeaders = new HttpHeaders();
		try {
			responseHeaders.set(HttpHeaders.CONTENT_LENGTH, thumbnailFile.length()+"");
	    	responseHeaders.set(HttpHeaders.CONTENT_TYPE, Files.probeContentType(thumbnailFile.toPath()));
		   	responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename="+URLEncoder.encode("a", "UTF-8"));
			logger.info("썸네일파일 다운로드");
	    	return new ResponseEntity<>(FileCopyUtils.copyToByteArray(thumbnailFile), responseHeaders, HttpStatus.OK);
		}catch(IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>("이미지파일 다운로드 실패" , HttpStatus.INTERNAL_SERVER_ERROR);
		}		
	}
	
	/*
	 * 게시글 상세 - 썸네일 파일 다운로드
	 */
	@GetMapping(value ="/downloadimage/detail")///{resaleBoardNo}") //GetMapping 사용 가능
	public ResponseEntity<?>  downloadImage(String fileName, String resaleBoardNo){//@PathVariable String resaleBoardNo){//String imageFileName) {
		File thumbnailFile = new File(uploadDirectory+"/resale_images/"+resaleBoardNo, fileName);
		HttpHeaders responseHeaders = new HttpHeaders();
		try {
			responseHeaders.set(HttpHeaders.CONTENT_LENGTH, thumbnailFile.length()+"");
	    	responseHeaders.set(HttpHeaders.CONTENT_TYPE, Files.probeContentType(thumbnailFile.toPath()));
		   	responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename="+URLEncoder.encode("a", "UTF-8"));
			logger.info("썸네일파일 다운로드");
	    	return new ResponseEntity<>(FileCopyUtils.copyToByteArray(thumbnailFile), responseHeaders, HttpStatus.OK);
		}catch(IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>("이미지파일 다운로드 실패" , HttpStatus.INTERNAL_SERVER_ERROR);
		}		
	}
}

