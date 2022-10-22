package com.golflearn.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.golflearn.domain.LessonLineRepository;
import com.golflearn.dto.Lesson;
import com.golflearn.dto.LessonLine;

import com.golflearn.dto.PageBean;
import com.golflearn.exception.FindException;

@Service(value = "lessonLineService")
public class LessonLineService {
	@Autowired
	private LessonLineRepository repository;
	
	public List<LessonLine> myLessonList(String userId) throws FindException{
		List<LessonLine> list = repository.selectById(userId);
		return list;
	}
	
	public List<Lesson> proLessonList(String userId) throws FindException{
		List<Lesson> lessonList = repository.selectByProdId(userId);
		return lessonList; 
	}
}
