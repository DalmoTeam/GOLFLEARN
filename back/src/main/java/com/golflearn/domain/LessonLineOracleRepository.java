package com.golflearn.domain;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.golflearn.dto.Lesson;
import com.golflearn.dto.LessonLine;
import com.golflearn.exception.AddException;
import com.golflearn.exception.FindException;

@Repository(value = "lessonLineRepository")
public class LessonLineOracleRepository implements LessonLineRepository {
	
	@Autowired
	private SqlSessionFactory sqlSessionFactory;
	
	@Override
	public List<LessonLine> selectById(String userId) throws FindException{
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			List<LessonLine> list = session.selectList("com.golflearn.mapper.LessonLineMapper.selectById", userId);
			return list;
		}catch (Exception e) {
			e.printStackTrace();
			throw new FindException(e.getMessage());
		}finally {
			if(session != null) {
				session.close();
			}
		}
	}
	@Override
	public List<Lesson> selectByProdId(String userProId) throws FindException {
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			List<Lesson> list = session.selectList("com.golflearn.mapper.LessonLineMapper.selectByProId", userProId);
			return list;
		}catch (Exception e) {
			e.printStackTrace();
			throw new FindException(e.getMessage());
		}finally {
			if(session != null) {
				session.close();
			}
		}
	}
}
