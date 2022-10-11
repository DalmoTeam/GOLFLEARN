package com.golflearn.control;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.golflearn.dto.Lesson;
import com.golflearn.dto.LessonLine;
import com.golflearn.dto.ResultBean;
import com.golflearn.exception.FindException;
import com.golflearn.service.LessonLineService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("mypage/*")
public class LessonLineController {
	@Autowired
	private LessonLineService service;
	
	/**
	 * 수강생의 아이디에 해당되는 레슨내역 리스트를 보여줌 
	 * @param userId
	 * @return
	 */
	@GetMapping(value = "student")
	public ResultBean<LessonLine> myPage(@RequestParam("userId") String userId) {
		ResultBean<LessonLine> rb = new ResultBean<>();
		try {
			List<LessonLine> lessonLine = service.myLessonList(userId);
			rb.setStatus(1);
			rb.setLt(lessonLine);
		} catch (FindException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb;
	}
	/**
	 * 프로의 아이디에 해당되는 레슨 리스트를 보여줌 
	 * @param userId
	 * @return
	 */
	@GetMapping(value = "pro")
	public ResultBean<Lesson> myProPage(@RequestParam("userId") String userId){
		ResultBean<Lesson> rb = new ResultBean<>();
		try {
			List<Lesson> lesson = service.proLessonList(userId);
			rb.setStatus(1);
			rb.setLt(lesson);
		} catch (FindException e) {
			e.printStackTrace();
			rb.setStatus(0);
			rb.setMsg(e.getMessage());
		}
		return rb;
	}
}
