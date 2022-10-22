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

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.golflearn.dto.ResultBean;
import com.golflearn.dto.RoundReviewBoardDto;
import com.golflearn.dto.RoundReviewCommentDto;
import com.golflearn.dto.RoundReviewLikeDto;
import com.golflearn.exception.AddException;
import com.golflearn.exception.FindException;
import com.golflearn.exception.ModifyException;
import com.golflearn.exception.RemoveException;
import com.golflearn.service.RoundReviewBoardService;

import net.coobird.thumbnailator.Thumbnailator;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
//@RequestMapping("roundreview/*")
public class RoundReviewBoardController {
	@Autowired
	private RoundReviewBoardService service;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	//파일 저장 경로
	//이전 이미지저장 경로 설정 시 사용
//	@Value("${spring.servlet.multipart.location}")
	
	//도커로 구동 시 이미지저장 경로 
//	String uploadDirectory = "/images/";
	
	//local에서 구동 시 이미지저장 경로 
	String uploadDirectory = "C:\\Project\\Golflearn\\BackRoundReview\\images";

	/**
	 * 라운딩리뷰게시판 로딩 시 보여줄 리스트 
	 * board/list/{정렬순서}/{현재페이지번호} 
	 * 정렬순서 : 0 - 최신순  /  1 - 조회순  /  2 - 좋아요순 
	 * @param session
	 * @param optOrderType
	 * @param optCp
	 * @param pageable
	 * @return 응답상태
	 * @throws FindException
	 */
	@GetMapping(value = {"board/list", "board/list/{optOrderType}", "board/list/{optOrderType}/{optCp}"})
	public ResultBean<Page<RoundReviewBoardDto>> list (HttpSession session, @PathVariable Optional<Integer> optOrderType, @PathVariable Optional<Integer> optCp, 
					@PageableDefault(page = 0, size = 5, sort = "roundReviewBoardNo", direction = Direction.DESC) Pageable pageable) throws FindException{
		ResultBean<Page<RoundReviewBoardDto>> rb = new ResultBean<Page<RoundReviewBoardDto>>();
		try {
			//현재 페이지 값이 전달되지 않으면 0값으로 세팅 // 'board/list'만 호출한 경우 맨 첫 페이지 로드
			int currentPage;
			if(optCp.isPresent()) {
				currentPage = optCp.get();
			} else {
				currentPage = 0;
			}
			//정렬순서 
			//프론트에서 orderType를 보내줌 
			//정렬순서값이 전달되지 않으면 0값으로 세팅(최신순) 
			int orderType;
			if(optOrderType.isPresent()) {
				orderType = optOrderType.get();
			} else {
				orderType = 0;
			}
			//orderType에 따라 정렬기준 요소를 정해줌 이 요소는 Pageable 정렬기준이 됨 
			String orderCriteria = "";
			if(orderType == 0) {
				orderCriteria = "roundReviewBoardNo";
			} else if (orderType == 1) {
				orderCriteria = "roundReviewBoardViewCnt";
			} else {
				orderCriteria = "roundReviewBoardLikeCnt";
			}
			//정렬기준에 따라 5개씩 내림차순으로 정렬함 
			//정상작동일경우 Status -> 1 , 오류가 나서 catch된경우 Status -> 0;
			pageable = PageRequest.of(currentPage, 5, Sort.by(Sort.Direction.DESC, orderCriteria));
			Page<RoundReviewBoardDto> dto = service.boardList(currentPage, orderType, pageable);
			rb.setStatus(1);
			rb.setT(dto);
		} catch (FindException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb;
	}
	/**
	 * 라운딩리뷰 게시판 목록에서 검색할 수 있는 기능
	 * search/{검색어}/{현재페이지}
	 * @param optWord
	 * @param optCp
	 * @param pageable
	 * @return 응답상태
	 */
	@GetMapping(value = {"search", "search/{optWord}", "search/{optWord}/{optCp}"})
	public ResultBean<Page<RoundReviewBoardDto>> search(@PathVariable Optional<String> optWord, @PathVariable Optional<Integer> optCp, 
			Pageable pageable){
		ResultBean<Page<RoundReviewBoardDto>> rb = new ResultBean<>();
		Page<RoundReviewBoardDto> pb;
		String word = "";
		try {
			//검색어가 존재하지 않는 경우 검색어를 ""로 설정
			if (optWord.isPresent()) {
				word = optWord.get();
			} else {
				word = "";
			}
			int currentPage = 0;
			if(optCp.isPresent()) {
				currentPage = optCp.get();
			}
			//검색어가 ""와 같은 경우(존재하지 않는 경우) -> orderType을 0값 설정 (최신순) 
			//검색어가 없는 경우 최신순으로 리스트를 불러옴 
			//검색어가 있는 경우 정상적으로 seachBoard 메서드 실행 
			if("".equals(word)) {
				int orderType = 0; //나중에 아마 빼도 될듯?
				String orderCriteria = "roundReviewBoardNo";
				pageable = PageRequest.of(currentPage, 5, Sort.by(Sort.Direction.DESC, orderCriteria));
				pb = service.boardList(currentPage, orderType, pageable);
			}else {
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
	 * 게시글 상세보기 기능 
	 * board/{게시글번호}
	 * @param roundReviewBoardNo
	 * @return 응답상태
	 */
	@GetMapping(value = "board/{roundReviewBoardNo}")
	public ResultBean<Map<String, Object>> viewBoard(@PathVariable Long roundReviewBoardNo){
		Map<String, Object> map = new HashMap<>();
		
		//viewBoard 메서드를 호출하여 map에 넣기
		//map에는 roundReviewBoard / status / imageFileNames가 들어감 
		try {
			RoundReviewBoardDto roundReviewBoard = service.viewBoard(roundReviewBoardNo);
			map.put("roundReviewBoard", roundReviewBoard);
			//map.put("status", 1);
		} catch (FindException e) {
			e.printStackTrace();
			map.put("status", 0);
		}

		// 저장된 이미지 파일의 이름을 가지고 오는 것 -> 사진 불러올 때 저장된 개수만큼 불러와야함
		String saveDirectory = uploadDirectory +"/"+ "roundReview_images" + "/" + roundReviewBoardNo + "/";
		File dir = new File(saveDirectory);

		//경로에서 image라는 이름을 포함한 이미지명들 반환 -> 배열에 넣어서 map에 넣어줌 
		String[] imageFiles = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.contains("image_");
			} 
		});
		map.put("imageFileNames", imageFiles);
		
		ResultBean<Map<String, Object>> rb = new ResultBean<>();
		rb.setStatus(1);
		rb.setT(map);
		
		return rb;
	}
	/**
	 * 게시글 수정하기 기능 
	 * board/{글번호}
	 * @param roundReviewBoardNo
	 * @param roundReviewBoard
	 * @return 응답상태
	 */
	@PutMapping(value = "board/{roundReviewBoardNo}")
	public ResponseEntity<Object> modifyBoard(@PathVariable Long roundReviewBoardNo, @RequestBody RoundReviewBoardDto roundReviewBoard){
		try {
			//내용 or 제목이 null이거나 ""인 경우 BAD_REQUEST를 반환 
			if(roundReviewBoard.getRoundReviewBoardContent() == null || roundReviewBoard.getRoundReviewBoardContent().equals("") ||
					roundReviewBoard.getRoundReviewBoardTitle() == null || roundReviewBoard.getRoundReviewBoardTitle().equals("")){
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			roundReviewBoard.setRoundReviewBoardNo(roundReviewBoardNo);
			service.modifyBoard(roundReviewBoard);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (ModifyException e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);			
		}
	}
	/**
	 * 게시글 삭제하기 기능 
	 * board/{글번호}
	 * @param roundReviewBoardNo
	 * @return 응답상태
	 */
	@Transactional
	@DeleteMapping(value = "board/{roundReviewBoardNo}")
	public ResponseEntity<String> removeBoard(@PathVariable Long roundReviewBoardNo){
		try {
			service.removeBoard(roundReviewBoardNo);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (RemoveException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	/**
	 * 게시글 작성하기 기능 
	 * 이미지도 첨부해야함. 여러 이미지 첨부 기능도 추가 
	 * @param imageFiles
	 * @param dto
	 * @return 응답상태
	 */
	@PostMapping(value = "board")
	public ResponseEntity<?> writeBoard(@RequestPart(required = false)List<MultipartFile> imageFiles, RoundReviewBoardDto dto){
		RoundReviewBoardDto boardDto = new RoundReviewBoardDto();
		try {
			//테스트용 dt
//			java.util.Date utilDate = new java.util.Date();
//			java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
//			dto.setRoundReviewBoardDt(sqlDate);
//			dto.setUserNickname("데빌");
			boardDto = service.writeBoard(dto);
		} catch (AddException e) {
			e.printStackTrace();
		}
		
		Long roundReviewBoardNo = boardDto.getRoundReviewBoardNo();
		
		//파일 저장 폴더
		String saveDirectory = uploadDirectory + "roundreview_images/" + roundReviewBoardNo;
		//파일 경로 생성
		//파일 경로에 폴더가 없으면 폴더 생성 
		if(!new File(saveDirectory).exists()) {
			new File(saveDirectory).mkdir();
		}
		
		//이미지 저장
		int savedImgFileCnt = 0; //저장된 파일 수
		File thumbnailFile = null;
		if(!imageFiles.isEmpty()) {
			for(MultipartFile imageFile : imageFiles) {
				Long imageFileSize = imageFile.getSize();
				if(imageFileSize > 0) { //파일이 첨부되었을 경우 
					String originFileName = imageFile.getOriginalFilename(); //파일 확장자 가져오기
					
					//파일 확장자를 자유롭게 설정할 경우 
					
					//.뒤의 파일확장자만 자르기
					//String fileExtension = originFileName.substring(originFileName.lastIndexOf("."));
					//String savedImageFileName = "image_" + (savedImgFileCnt+1) + fileExtension;
					
					//저장파일생성 -> PNG확장자로 변경 
					String savedImageFileName = "image_" + (savedImgFileCnt+1) + ".PNG";
					//이미지 파일 생성
					File savedImageFile = new File(saveDirectory, savedImageFileName);
					
					try {
						//파일 저장
						FileCopyUtils.copy(imageFile.getBytes(), savedImageFile);
						
						//파일 타입 확인
						String contentType = imageFile.getContentType();
						if(contentType.contains("image/*")) {
							return new ResponseEntity<> ("이미지 파일이 아닙니다", HttpStatus.INTERNAL_SERVER_ERROR);
						}
						savedImgFileCnt++;
						
						//썸네일 만들기
//						String thumbnailName = "s_" + savedImageFileName;
						String thumbnailName = "s_" + (savedImgFileCnt+1) + ".PNG";
						thumbnailFile = new File(saveDirectory, thumbnailName);
						FileOutputStream thumbnailOS = new FileOutputStream(thumbnailFile);
						InputStream imageFileIS = imageFile.getInputStream();
						int width = 100;
						int height = 100;
						Thumbnailator.createThumbnail(imageFileIS, thumbnailOS, width, height);
						
					} catch(IOException e ) {
						e.printStackTrace();
						return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}else {
					return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
				}
				
			}
		}
		return new ResponseEntity<>("저장 완료", HttpStatus.OK);
	}
	/**
	 * 댓글 작성하기 기능
	 * comment/{글번호}
	 * @param roundReviewBoardNo
	 * @param dto
	 * @return 응답상태
	 */
	@PostMapping(value = "comment/{roundReviewBoardNo}")
	public ResponseEntity<?> addComment(@PathVariable Long roundReviewBoardNo,@RequestBody RoundReviewCommentDto dto){
		try {
			//테스트 DT
//			java.util.Date utilDate = new java.util.Date();
//			java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
//			dto.setRoundReviewCmtDt(sqlDate);
			service.addComment(roundReviewBoardNo, dto);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (AddException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	/**
	 * 댓글 수정하기 기능
	 * comment/{댓글번호}
	 * @param roundReviewCmtNo
	 * @param dto
	 * @return 응답상태
	 */
	@PutMapping(value = "comment/{roundReviewCmtNo}")
	public ResponseEntity<?> modifyComment(@PathVariable Long roundReviewCmtNo, @RequestBody RoundReviewCommentDto dto){
		try {
			//댓글 내용이 null이거나 공백인 경우 
			if(dto.getRoundReviewCmtContent() == null || dto.getRoundReviewCmtContent().equals("")){
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			service.modifyComment(dto);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (ModifyException e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);			
		}
	}
	/**
	 * 댓글 삭제하기 기능 
	 * comment/{글번호}/{댓글번호}
	 * @param roundReviewBoardNo
	 * @param roundReviewCmtNo
	 * @return 응답상태
	 */
	@Transactional
	@DeleteMapping(value = "comment/{roundReviewBoardNo}/{roundReviewCmtNo}")
	public ResponseEntity<String> removeComment(@PathVariable Long roundReviewBoardNo, @PathVariable Long roundReviewCmtNo){
		try {
			service.removeComment(roundReviewBoardNo, roundReviewCmtNo);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (RemoveException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
//	/**
//	 * 대댓글 수정하기 기능
//	 * recomment/{댓글번호}
//	 * @param roundReviewCmtNo
//	 * @param dto
//	 * @return 응답상태
//	 */
//	@PutMapping(value = "recomment/{roundReviewCmtNo}")
//	public ResponseEntity<?> modifyRecomment(@PathVariable Long roundReviewCmtNo, @RequestBody RoundReviewCommentDto dto){
//		try {
//			//댓글 내용이 null이거나 공백인 경우 
//			if(dto.getRoundReviewCmtContent() == null || dto.getRoundReviewCmtContent().equals("")){
//				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//			}
//			service.modifyRecomment(dto);
//			return new ResponseEntity<>(HttpStatus.OK);
//		} catch (ModifyException e) {
//			e.printStackTrace();
//			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);			
//		}
//	}
	/**
	 * 대댓글 삭제하기 기능
	 * recomment/{글번호}/{댓글번호}
	 * @param roundReviewBoardNo
	 * @param roundReviewCmtNo
	 * @return 응답상태
	 */
	@DeleteMapping(value = "recomment/{roundReviewBoardNo}/{roundReviewCmtNo}")
	public ResponseEntity<String> removeRecomment(@PathVariable Long roundReviewBoardNo, @PathVariable Long roundReviewCmtNo){
		try {
			service.removeRecomment(roundReviewBoardNo, roundReviewCmtNo);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (RemoveException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	/**
	 * 좋아요 추가하기 기능
	 * like/{글번호}
	 * @param roundReviewBoardNo
	 * @param userNickname
	 * @return 응답상태
	 */
	@PostMapping(value = "like/{roundReviewBoardNo}")
	public ResponseEntity<?> addlike(@PathVariable Long roundReviewBoardNo, @RequestParam("userNickname") String userNickname){
		//테스트닉네임
//		String loginedNickName = "데빌";
		RoundReviewBoardDto boardDto = new RoundReviewBoardDto(); 
		RoundReviewLikeDto likeDto = new RoundReviewLikeDto();
		boardDto.setRoundReviewBoardNo(roundReviewBoardNo);
		likeDto.setUserNickname(userNickname);
		likeDto.setRoundReviewBoard(boardDto);
		try {
			service.addLike(roundReviewBoardNo, likeDto);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (AddException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	/**
	 * 좋아요 삭제하기 기능
	 * like/{글번호}
	 * @param roundReviewBoardNo
	 * @param userNickname
	 * @return 응답상태
	 */
	@Transactional
	@DeleteMapping(value = "like/{roundReviewBoardNo}")
	public ResponseEntity<?> removeLike(@PathVariable Long roundReviewBoardNo, @RequestParam("userNickname") String userNickname){
		try {
			service.removeLike(roundReviewBoardNo, userNickname);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (RemoveException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	/**
	 * 게시글 목록 
	 * 썸네일 파일 다운로드(노출)
	 * @param roundReviewBoardNo
	 * @return 응답상태
	 */
	@GetMapping(value ="/downloadimage")
	public ResponseEntity<?> downloadImage(String roundReviewBoardNo){
		//게시글 목록이기에 첫번째 썸네일파일을 노출
		File thumbnailFile = new File(uploadDirectory+"/roundreview_images/"+roundReviewBoardNo, "s_1.PNG");
		HttpHeaders responseHeaders = new HttpHeaders();
		try {
			//파일상세 설정
			responseHeaders.set(HttpHeaders.CONTENT_LENGTH, thumbnailFile.length()+"");
	    	responseHeaders.set(HttpHeaders.CONTENT_TYPE, Files.probeContentType(thumbnailFile.toPath()));
		   	responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename="+URLEncoder.encode("a", "UTF-8"));
	    	return new ResponseEntity<>(FileCopyUtils.copyToByteArray(thumbnailFile), responseHeaders, HttpStatus.OK);
		}catch(IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>("이미지파일 다운로드 실패" , HttpStatus.INTERNAL_SERVER_ERROR);
		}		
	}
	/**
	 * 게시글 상세
	 * 썸네일 파일 다운로드(노출)
	 * @param fileName
	 * @param roundReviewBoardNo
	 * @return 응답상태
	 */
	@GetMapping(value ="/downloadimage/detail")
	public ResponseEntity<?> downloadImage(String fileName, String roundReviewBoardNo){
		//게시글 상세이기에 모든 썸네일 파일을 노출 
		File thumbnailFile = new File(uploadDirectory+"/roundreview_images/"+roundReviewBoardNo, fileName);
		HttpHeaders responseHeaders = new HttpHeaders();
		try {
			//파일상세 설정
			responseHeaders.set(HttpHeaders.CONTENT_LENGTH, thumbnailFile.length()+"");
	    	responseHeaders.set(HttpHeaders.CONTENT_TYPE, Files.probeContentType(thumbnailFile.toPath()));
		   	responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename="+URLEncoder.encode("a", "UTF-8"));
	    	return new ResponseEntity<>(FileCopyUtils.copyToByteArray(thumbnailFile), responseHeaders, HttpStatus.OK);
		}catch(IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>("이미지파일 다운로드 실패" , HttpStatus.INTERNAL_SERVER_ERROR);
		}		
	}	
}
