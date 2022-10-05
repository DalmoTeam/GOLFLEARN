package com.golflearn.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.golflearn.domain.LessonHistoryRepository;
import com.golflearn.domain.LessonRepository;
import com.golflearn.domain.PaymentRepository;
import com.golflearn.dto.Lesson;
import com.golflearn.dto.LessonLine;
import com.golflearn.dto.Payment;
import com.golflearn.exception.AddException;
import com.golflearn.exception.FindException;

@Service(value="lessonService")
public class LessonService {
	@Autowired
	private LessonHistoryRepository lsnHistoryRepository;

	@Autowired
	private LessonRepository lsnRepository;
	
	@Autowired
	private PaymentRepository paymentRepository;
	
	/**
	 * 레슨번호의 레슨을 반환한다
	 * @param lsnNo
	 * @return
	 * @throws FindException
	 */
	public Lesson viewLessonDetail(int lsnNo) throws FindException{
			Lesson l = lsnRepository.selectByLsnNo(lsnNo);
			return l;
	}
	
	/**
	 * 레슨을 승인요청한다
	 * @param lesson
	 * @throws AddException
	 */
	public void addLesson(Lesson lesson) throws AddException{
		//레슨정보를 추가한다
		lsnRepository.insertLsnInfo(lesson);

		//레슨분류정보를 추가한다
		//lsnRepository.insertLsnClassification(lesson);
	}

  	public List<LessonLine> viewLessonHistory(int lsnNo) throws FindException{
		return lsnHistoryRepository.selectLessonHistoryByLsnNo(lsnNo);
	}
	
	public List<Lesson> viewMain() throws FindException {
		return lsnRepository.selectAll();
	}
	
	/**
	 * 결제 요청한다.
	 */
	public void addPayment(Payment payment) throws AddException{
		paymentRepository.insertPayment(payment);
	}
	
}
