package com.golflearn.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golflearn.dto.Lesson;
import com.golflearn.dto.LessonClassification;
import com.golflearn.dto.LessonLine;
import com.golflearn.dto.Payment;
import com.golflearn.dto.ResultBean;
import com.golflearn.dto.UserInfo;
import com.golflearn.exception.AddException;
import com.golflearn.exception.FindException;
import com.golflearn.service.LessonService;

import net.coobird.thumbnailator.Thumbnailator;

@CrossOrigin(origins = "*") // 모든포트에서 접속가능 + 메서드마다 각각 설정도 가능
@RestController
@RequestMapping("lesson/*")
public class LessonController {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private LessonService service;

	@Autowired
	private ServletContext sc;// 파일path설정 시 필요

	@GetMapping("{lsnNo}")
	public ResultBean<Lesson> viewLessonDetail(@PathVariable int lsnNo) {
		ResultBean<Lesson> rb = new ResultBean<>();
		try {
			Lesson lesson = service.viewLessonDetail(lsnNo);
			rb.setStatus(1); // 성공시 satus : 1
			rb.setT(lesson); // lesson객체 담기
		} catch (FindException e) {
			e.printStackTrace();
			rb.setStatus(0); // 성공 실패시 satus : 0
			rb.setMsg(e.getMessage());
		}
		return rb;
	}

	@GetMapping(value = { "" })
	public ResultBean<Lesson> list(@PathVariable Optional<Integer> optCp) { // 로그인 유무와 상관없이 다 볼수 있기때문에 httpSession 필요없음
		ResultBean<Lesson> rb = new ResultBean<>();
		try {
			List<Lesson> lessons = service.viewMain();
			rb.setStatus(1);
			rb.setLt(lessons);
			return rb;
		} catch (FindException e) {
			e.printStackTrace();
			rb.setStatus(-1);
			rb.setMsg(e.getMessage());
			return rb;
		}
	}

	@GetMapping(value = { "history", "history/{optCp}" }) // 프로의 레슨내역에서 레슨번호에 대한 히스토리
	public ResultBean<LessonLine> viewHistory(@PathVariable int optCp, HttpSession session) {
		ResultBean<LessonLine> rb = new ResultBean<>();
		// 로그인 여부를 받아와야한다 HttpSession?
		String loginedId = (String) session.getAttribute("loginInfo");
		if (loginedId == null) {
			rb.setStatus(0);
			rb.setMsg("로그인하세요");
			return rb;
		} else {
			try {
				List<Lesson> lessons = service.viewMain();
				rb.setStatus(1);
//				rb.setLt(lessons);
				return rb;
			} catch (FindException e) {
				e.printStackTrace();
				rb.setStatus(-1);
				rb.setMsg(e.getMessage());
				return rb;
			}
		}
	}

	@Value("${spring.servlet.multipart.location}")
	String saveDirectory;// 파일경로생성
	@PostMapping("request") // list타입 필드가 있는 Lesson전달과 파일첨부를 동시에 하기 위해 String타입으로 Lesson얻기
	public ResponseEntity<?> reuqestLesson(@RequestPart(required = false) MultipartFile file, String strLesson) throws JsonMappingException, JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		Lesson lesson = mapper.readValue(strLesson, Lesson.class);// String타입을 Lesson타입으로 변환
//		String loginedId = lesson.getUserInfo().getUserId();
		String loginedId = "jangpro@gmail.com";
				
		if (loginedId == null) {// 로그인 여부 확인
			return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		} else {
			// -----Lesson DB에 저장-----
			UserInfo userInfo = new UserInfo();
			userInfo.setUserId(loginedId);
			lesson.setUserInfo(userInfo);// lesson객체 내 userInfo 저장

			List<LessonClassification> classifications = lesson.getLsnClassifications();// 클럽분류 저장
			try {
				service.addLesson(lesson);//서비스 호출

				// -----이미지 업로드-----
				String lessonPath = saveDirectory + "lsn_images\\";// 파일경로
				if (!new File(lessonPath).exists()) {
					logger.info("업로드 실제경로생성");
					new File(lessonPath).mkdirs();
				}
				File thumbnailFile = null;

				long fSize = file.getSize();// getSize() => 0 if empty
				if (fSize > 0) {
					// 파일형식 확인
					String contentType = file.getContentType();
					if (!contentType.contains("image/")) { // 이미지파일형식이 아닌 경우
						return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
					}

					// 이미지파일 저장하기
					String imageOrignFileName = file.getOriginalFilename(); // 이미지파일원본이름얻기
					logger.info("이미지 파일이름:" + imageOrignFileName + ", 파일크기: " + file.getSize());

					long wroteLsnNo = lesson.getLsnNo();

					// 저장할 파일이름을 지정한다 "레슨번호.jpg"
					String fName = wroteLsnNo + ".jpg";
					File savedImageFile = new File(lessonPath, fName);// 이미지파일생성

					try {// copy메서드를 이용하여 파일 저장
						FileCopyUtils.copy(file.getBytes(), savedImageFile);
						logger.info("이미지 파일저장:" + savedImageFile.getAbsolutePath());

						// 섬네일파일을 만들기
						String thumbnailName = wroteLsnNo + "_LessonThumbnail.jpg";//섬네일 파일명은 "레슨번호_LessonThumbnail.jpg"
								
						thumbnailFile = new File(lessonPath, thumbnailName);
						FileOutputStream thumbnailOS;
						thumbnailOS = new FileOutputStream(thumbnailFile);
						InputStream imageFileIS = file.getInputStream();
						int width = 140;
						int height = 140;
						Thumbnailator.createThumbnail(imageFileIS, thumbnailOS, width, height);
						logger.info(
								"섬네일파일 저장:" + thumbnailFile.getAbsolutePath() + ", 섬네일파일 크기:" + thumbnailFile.length());

						// 이미지 썸네일다운로드하기
						HttpHeaders responseHeaders = new HttpHeaders();
						responseHeaders.set(HttpHeaders.CONTENT_LENGTH, thumbnailFile.length() + "");
						responseHeaders.set(HttpHeaders.CONTENT_TYPE, Files.probeContentType(thumbnailFile.toPath()));
						responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
								"inline; filename=" + URLEncoder.encode("a", "UTF-8"));
						logger.info("섬네일파일 다운로드");

						return new ResponseEntity<>(FileCopyUtils.copyToByteArray(thumbnailFile), responseHeaders,
								HttpStatus.OK);
					} catch (IOException e2) {
						e2.printStackTrace();
						return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} // end if(imageFileSize > 0)
				else {
					logger.error("이미지파일이 없습니다");
					return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} catch (AddException e) {
				e.printStackTrace();
				return new ResponseEntity<>("오류가 발생하였습니다. 다시 시도해주세요",HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}
	}
//	
//	@PostMapping(value="")
//	public ResultBean<Payment> payment(){
//		
//	}
}