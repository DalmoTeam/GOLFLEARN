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
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

import com.golflearn.domain.entity.NoticeBoardEntity;
import com.golflearn.domain.entity.NoticeCommentEntity;
import com.golflearn.dto.NoticeBoardDto;
import com.golflearn.dto.NoticeCommentDto;
import com.golflearn.dto.NoticeLikeDto;
import com.golflearn.dto.ResultBean;
import com.golflearn.exception.AddException;
import com.golflearn.exception.FindException;
import com.golflearn.exception.ModifyException;
import com.golflearn.exception.RemoveException;
import com.golflearn.service.NoticeBoardService;

import net.coobird.thumbnailator.Thumbnailator;

import com.golflearn.dto.PageBean;


@CrossOrigin(origins="*" , allowedHeaders = "*")
@RestController
@RequestMapping("notice/*")
public class NoticeBoardController {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private NoticeBoardService service;

	@Autowired
	private ServletContext sc;

	//	@Value("${spring.servlet.multipart.location}")
	//	private String saveDirectory;
	// 파일 저장 경로
	String uploadDirectory = "C://Project/GolfLearn/BackNotice/images/";
	// 도커 사용 시 ▼ - 상대 경로로
//	String uploadDirectory = "/images/";
	//			"C:\\Project\\GolfLearn\\front\\src\\main\\webapp\\";

	@GetMapping(value= {"list", "list/{optCp}"}) //두개의값 모두 전달 OK
	public ResultBean<PageBean<NoticeBoardDto>> list(@PathVariable Optional<Integer> optCp) {

		ResultBean<PageBean<NoticeBoardDto>> rb = new ResultBean<>();
		try {
			int currentPage;
			if(optCp.isPresent()) {
				currentPage = optCp.get();
			} else {
				currentPage = 1;
			}
			PageBean<NoticeBoardDto> pb = service.boardList(currentPage);
			rb.setStatus(1);
			rb.setT(pb);
		} catch (FindException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb;
	}

	@GetMapping(value = {"board/search/{optWord}", "board/search/{optWord}/{optCp}", "board/search"})
	public ResultBean<PageBean<NoticeBoardDto>> search(
			@PathVariable Optional<String> optWord,
			@PathVariable Optional<Integer> optCp){
		ResultBean<PageBean<NoticeBoardDto>> rb = new ResultBean<>();

		try {
			PageBean<NoticeBoardDto> pb; 
			String word = ""; 
			if(optWord.isPresent()) {
				word = optWord.get();
			} else { 
				word = "";
			}

			int currentPage = 1;
			if(optCp.isPresent()) {
				currentPage = optCp.get();
			}else {

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

	
	// 상세보기
	//	@GetMapping("{boardNo}")
	//	public ResultBean<NoticeBoardDto> viewBoard(@PathVariable int boardNo) {
	//		ResultBean<NoticeBoardDto> rb = new ResultBean<>();
	//		try {
	//			NoticeBoardDto b = service.viewNoticeBoard(boardNo);
	//			rb.setStatus(1);
	//			rb.setT(b);
	//		} catch (FindException e) {
	//			e.printStackTrace();
	//			rb.setStatus(0);
	//			rb.setMsg(e.getMessage());
	//		}
	//		return rb;
	//	}
	
	@GetMapping("{boardNo}")
	public ResultBean<Map<String, Object>> viewBoard(@PathVariable int boardNo) {
		Map<String, Object> map = new HashMap<>();
		try {
			NoticeBoardDto b = service.viewNoticeBoard(boardNo);
			map.put("noticeBoard", b);
		} catch (FindException e) {
			e.printStackTrace();
			map.put("status", 0);
		}
		// 저장된 이미지 파일의 이름을 가지고 오는 것 -> 사진 불러올 때 저장된 개수만큼 불러와야함
		String saveDirectory = uploadDirectory + "notice_images/" +boardNo + "/";
		//	System.out.println("경로는" + saveDirectory);
		File dir = new File(saveDirectory);

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

// 	// 저장된 이미지 파일의 이름을 가지고 오는 것 -> 사진 불러올 때 저장된 개수만큼 불러와야함
// 	String saveDirectory = uploadDirectory + "notice_images" + "/" +boardNo + "/";
// //	System.out.println("경로는" + saveDirectory);
// 	File dir = new File(saveDirectory);



	@PostMapping(value = "/writeboard")
	public ResponseEntity<?> write(@RequestPart(required = false) MultipartFile imageFile, NoticeBoardDto noticeBoard){

		logger.info("요청전달데이터 title=" + noticeBoard.getNoticeBoardTitle() + ", content=" + noticeBoard.getNoticeBoardContent());
		//		logger.info("letterFiles.size()=" + letterFiles.size());
		logger.info("imageFile.getSize()=" + imageFile.getSize() + ", imageFile.getOriginalFileName()=" + imageFile.getOriginalFilename());
//		HttpHeaders responseHeaders = new HttpHeaders();
		System.out.println("제목" + noticeBoard.getNoticeBoardContent());
		NoticeBoardDto nbDto = new NoticeBoardDto();

		Long noticeBoardNo = noticeBoard.getNoticeBoardNo();
		//		logger.error("글번호는"+boardDto.getResaleBoardNo());

		// 파일 저장 폴더
		//파일 경로 생성
		//		if(!new File(saveDirectory).exists()) {
		//			new File(saveDirectory).mkdirs(); //파일 경로에 폴더 없으면 저장
		//		}
		//게시글내용 DB에 저장
		try {
			//---로그인대신할 샘플데이터--
			//			String loginedId = "id1";
			//----------------------
			//			board.setBoardId(loginedId);

			noticeBoard = NoticeBoardDto.builder()
					.noticeBoardNo(noticeBoard.getNoticeBoardNo())
					.noticeBoardTitle(noticeBoard.getNoticeBoardTitle())
					.noticeBoardContent(noticeBoard.getNoticeBoardContent())
					.userNickname(noticeBoard.getUserNickname())
					.build();
			NoticeBoardEntity nBe = noticeBoard.toEntity();

			nbDto = service.writeBoard(nBe);

		} catch (AddException e1) {
			e1.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		//파일 경로 생성
		//		String saveDirectory = sc.getInitParameter("filePath");
		//		String saveDirectory = "/Users/jasonmilian/Desktop/files";
		Long wroteBoardNo = nbDto.getNoticeBoardNo();
		String saveDirectory = uploadDirectory + "notice_images/"+ wroteBoardNo;
		if (! new File(saveDirectory).exists()) {
			logger.info("업로드 실제경로생성");
			new File(saveDirectory).mkdirs();
		}

		//이미지 저장
		int savedImgFileCnt = 0; // 서버에 저장된 파일 수
		if(!imageFile.isEmpty()) {
		File thumbnailFile = null;	
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
						String thumbnailName = "s_" + "image_1" + ".PNG";
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
		
		return new ResponseEntity<>("저장 완료", HttpStatus.OK);

	}
		
		
//		int savedletterFileCnt = 0;//서버에 저장된 파일수
//		logger.info("저장된 letter 파일개수: " + savedletterFileCnt);
//		File thumbnailFile = null;
//		long imageFileSize = imageFile.getSize();
//		int imageFileCnt = 0;//서버에 저장된 이미지파일수
//		if(imageFileSize > 0) {
//			//이미지파일 저장하기
//			String imageOrignFileName = imageFile.getOriginalFilename(); //이미지파일원본이름얻기
//			String fileExtension = imageOrignFileName.substring(imageOrignFileName.lastIndexOf("."));
//			logger.info("이미지 파일이름:" + imageOrignFileName +", 파일크기: " + imageFile.getSize());
//
//			//저장할 파일이름을 지정한다 ex) 글번호_image_XXXX_원본이름
//			String imageFileName = wroteBoardNo + "_image_1" + ".PNG";
//			//이미지파일생성
//			File savedImageFile = new File(saveDirectory, imageFileName);
//			try {
//				FileCopyUtils.copy(imageFile.getBytes(), savedImageFile);
//				logger.info("이미지 파일저장:" + savedImageFile.getAbsolutePath());
//
//				//파일형식 확인
//				String contentType = imageFile.getContentType();
//				if(!contentType.contains("image/*")) { //이미지파일형식이 아닌 경우
//					return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//				}
//				//이미지파일인 경우 섬네일파일을 만듦
//				String thumbnailName =  "s_"+imageFileName; //섬네일 파일명은 s_글번호_XXXX_원본이름
//				thumbnailFile = new File(saveDirectory,thumbnailName);
//				FileOutputStream thumbnailOS;
//				thumbnailOS = new FileOutputStream(thumbnailFile);
//				InputStream imageFileIS = imageFile.getInputStream();
//				int width = 100;
//				int height = 100;
//				Thumbnailator.createThumbnail(imageFileIS, thumbnailOS, width, height);
//				logger.info("섬네일파일 저장:" + thumbnailFile.getAbsolutePath() + ", 섬네일파일 크기:" + thumbnailFile.length());
//				//				//이미지 썸네일다운로드하기
//				//				HttpHeaders responseHeaders = new HttpHeaders();
//				//				responseHeaders.set(HttpHeaders.CONTENT_LENGTH, thumbnailFile.length()+"");
//				//				responseHeaders.set(HttpHeaders.CONTENT_TYPE, Files.probeContentType(thumbnailFile.toPath()));
//				//				responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename="+URLEncoder.encode("a", "UTF-8"));
//				//				logger.info("섬네일파일 다운로드");
//				return new ResponseEntity<>(FileCopyUtils.copyToByteArray(thumbnailFile), 
////						responseHeaders, 
//						HttpStatus.OK);
//
//			} catch (IOException e2) {
//				e2.printStackTrace();
//				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//			}
//		}//end if(imageFileSize > 0) 
//		else {
//			logger.error("이미지파일이 없습니다");
//			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
	
	
	
	
	

	@PutMapping(value="{boardNo}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> modify(
			@PathVariable long boardNo,
			@RequestBody NoticeBoardDto noticeBoard){

		//		NoticeBoardDto nbDto = new NoticeBoardDto();

		try {
			if(noticeBoard.getNoticeBoardContent() == null || noticeBoard.getNoticeBoardContent().equals("")) {
				return new ResponseEntity<>("글내용은 반드시 입력하세요", HttpStatus.BAD_REQUEST);
			}
			noticeBoard = NoticeBoardDto.builder()
					.noticeBoardNo(boardNo)
					.noticeBoardTitle(noticeBoard.getNoticeBoardTitle())
					.noticeBoardContent(noticeBoard.getNoticeBoardContent())
					.noticeBoardDt(noticeBoard.getNoticeBoardDt())
					.userNickname(noticeBoard.getUserNickname())
					.build();
			NoticeBoardEntity nBe = noticeBoard.toEntity();

			service.modifyBoard(nBe);

			return new ResponseEntity<>(HttpStatus.OK);
		}catch(ModifyException e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("{boardNo}")
	public ResultBean<NoticeBoardDto> deleteBoard(@PathVariable long boardNo) {
		ResultBean<NoticeBoardDto> rb = new ResultBean<>();
		try {
			service.removeBoard(boardNo);
			rb.setStatus(1);
			rb.setMsg("게시글이 삭제되었습니다.");
		} catch (RemoveException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb;
	}

	@PostMapping(value="comment/write", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> reply(
			@RequestBody NoticeCommentDto commentDto) {
		if(commentDto.getNoticeCmtContent() == null || commentDto.getNoticeCmtContent().equals("")){
			return new ResponseEntity<>("글제목이나 글내용은 반드시 입력하세요", HttpStatus.BAD_REQUEST);   
		}
		System.out.println(commentDto.getNoticeBoardDto().getNoticeBoardNo());
		//		String loginedId = (String)session.getAttribute("loginInfo");
		//---로그인대신할 샘플데이터--
		Long u = commentDto.getNoticeBoardDto().getNoticeBoardNo();
		//		String loginedId = "id1";
		commentDto = NoticeCommentDto.builder()
				.userNickname(commentDto.getUserNickname())
				.noticeCmtNo(commentDto.getNoticeCmtNo())
				.noticeCmtDt(commentDto.getNoticeCmtDt())
				.noticeBoardDto(commentDto.getNoticeBoardDto())
				.noticeBoardNo(u)
				.noticeCmtContent(commentDto.getNoticeCmtContent())
				.noticeCmtParentNo(commentDto.getNoticeCmtParentNo())
				.build();

		//		b.setBoardId(loginedId);
		//		b.setBoardParentNo(boardParentNo);
		try {
			service.replyBoard(commentDto.toEntity());
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (AddException e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("comment/{commentNo}")
	public ResultBean<NoticeCommentDto> deleteComment(@PathVariable long commentNo) {
		ResultBean<NoticeCommentDto> rb = new ResultBean<>();
		try {
			service.removeComment(commentNo);
			rb.setStatus(1);
			rb.setMsg("댓글이 삭제되었습니다.");
		} catch (RemoveException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb;
	}

	@PutMapping(value="comment/{commentNo}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResultBean<NoticeCommentDto> modifyComment(
			@PathVariable long commentNo,
			@RequestBody NoticeCommentDto noticeComment,
			HttpSession session){

		// String loginedNickname = (String) session.getAttribute("loginNickname");
		String loginedNickname = "id1";	
		ResultBean<NoticeCommentDto> rb = new ResultBean<>();
		try {
			if(loginedNickname == null) {
				rb.setMsg("로그인하세요");
				return rb;
			} else if(loginedNickname.equals(noticeComment.getUserNickname())) {
				if(noticeComment.getNoticeCmtContent() == null || noticeComment.getNoticeCmtContent().equals("")) {
					rb.setMsg("내용을 반드시 입력해주세요");
				} else {
					System.out.println("-------------------");
					noticeComment.setNoticeCmtNo(commentNo);
					NoticeCommentEntity nCe = noticeComment.toEntity();
					service.modifyComment(nCe);
					//					rb.setT();
					rb.setStatus(1);
					rb.setMsg("수정 성공");
				}

			} else {
				rb.setMsg("사용자가 아닙니다.");
				return rb;
			}
		}catch(ModifyException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg("수정실패");
		}
		return rb;
	}

	@PostMapping(value = "like/add", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResultBean<NoticeLikeDto> addLike(@RequestBody NoticeLikeDto likeDto) {

		ResultBean<NoticeLikeDto> rb = new ResultBean<>();
		try {
			service.addLike(likeDto);
			rb.setStatus(1);
			rb.setMsg("좋아요 추가");
		} catch (AddException e) {
			e.printStackTrace();
			rb.setStatus(1);
			rb.setMsg("좋아요 실패");
		}
		return rb;
	}

	/**
	 * 좋아요 삭제
	 * @param noticeLikeNo
	 * @param dto
	 * @param session
	 * @return
	 */
	@DeleteMapping(value = "like/{noticeLikeNo}", produces = MediaType.APPLICATION_JSON_VALUE) //Json 형태로 return?!
	public ResultBean<NoticeLikeDto> removeLike(@PathVariable Long noticeLikeNo, 
			@RequestBody NoticeLikeDto likeDto){

		ResultBean<NoticeLikeDto> rb = new ResultBean<>(); // 객체 생성

		try {
			logger.error("원글 번호는"+likeDto.getNoticeBoardDto().getNoticeBoardNo());

			//				likeDto.setUserNickname(likeDto.getUserNickname());
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
	 * 게시글 목록 - 썸네일 파일 다운로드(노출)
	 */
	@GetMapping(value ="/downloadimage")
	public ResponseEntity<?>  downloadImage(String boardNo){
		File thumbnailFile = new File(uploadDirectory+"/notice_images/"+boardNo, "s_1.PNG");
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
	 * 게시글 상세 - 썸네일 파일 다운로드(노출)
	 */
	@GetMapping(value ="/downloadimage/detail")
	public ResponseEntity<?>  downloadImage(String fileName, String boardNo){//@PathVariable String resaleBoardNo){//String imageFileName) {
		File thumbnailFile = new File(uploadDirectory+"/notice_images/"+boardNo, fileName);
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
