package com.golflearn.control;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.golflearn.dto.Lesson;
import com.golflearn.dto.LessonLine;
import com.golflearn.dto.LessonReview;
import com.golflearn.dto.ResultBean;
import com.golflearn.exception.AddException;
import com.golflearn.exception.FindException;
import com.golflearn.exception.ModifyException;
import com.golflearn.service.LessonReviewService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("review/*")
public class LessonReviewController {
	@Autowired
	private LessonReviewService service;
	
	/**
	 * 클릭한 레슨내역에 대한 리뷰를 작성함 
	 * @param lsnLineNo
	 * @return
	 */
	@GetMapping(value = "new")
	public ResultBean<Lesson> newReview(@RequestParam("lsn_line_no") int lsnLineNo) {
		ResultBean<Lesson> rb = new ResultBean<>();
		try {
			Lesson lesson = service.newReview(lsnLineNo);
			rb.setStatus(1);
			rb.setT(lesson);
		} catch (FindException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb;
	}
	/**
	 * 클릭한 레슨내역에 대한 후기를 수정함 
	 * @param lsnLineNo
	 * @return
	 */
	@GetMapping(value = "previous") 
	public ResultBean<LessonLine> previous(@RequestParam("lsn_line_no") int lsnLineNo){
		ResultBean<LessonLine> rb = new ResultBean<>();
		try {
			LessonLine lessonLine = service.previousReview(lsnLineNo);
			rb.setStatus(1);
			rb.setT(lessonLine);
		} catch (FindException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb;
	}
	/**
	 * 리뷰를 새로 작성함
	 * @param lsnReview
	 * @return
	 */
	@PostMapping(value = "write")
	public ResponseEntity<?> write(@RequestBody LessonReview lsnReview) {
		try {				
			//리뷰의 내용이 null이거나 공백일 경우, 또는 별점이 0일경우에는 에러 메시지 출력
			if(lsnReview.getReview() == null || lsnReview.getReview().equals("") 
					|| lsnReview.getMyStarScore() == 0) {
				return new ResponseEntity<>("글 내용과 별점을 입력해주세요", HttpStatus.BAD_REQUEST);
			}
			service.writeReview(lsnReview);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (AddException e) {
			e.printStackTrace();
			return new ResponseEntity<>("내용은 필수 입력 사항입니다", HttpStatus.BAD_REQUEST);
		}
	}
	/**
	 * 리뷰를 수정함
	 * @param lsnReview
	 * @return
	 */
	@PutMapping(value = "modify")  
	public ResponseEntity<?> modify(@RequestBody LessonReview lsnReview) {
		try {
			//리뷰의 내용이 null이거나 공백일 경우 에러 메시지 출력
			if(lsnReview.getReview() == null || lsnReview.getReview().equals("")) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			service.modifyReview(lsnReview);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (ModifyException e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}		
	}
}
