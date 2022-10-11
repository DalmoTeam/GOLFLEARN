package com.golflearn.domain;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.golflearn.dto.Lesson;
import com.golflearn.dto.LessonLine;
import com.golflearn.dto.LessonReview;
import com.golflearn.exception.AddException;
import com.golflearn.exception.FindException;

@Repository(value = "lessonReviewRepository")
public class LessonReviewOracleRepository implements LessonReviewRepository {

	@Autowired
	private SqlSessionFactory sqlSessionFactory;
	
	@Override
	public Lesson selectTitleByLsnLineNo(int lsnLineNo) throws FindException {
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			Lesson lesson = session.selectOne("com.golflearn.mapper.LessonReviewMapper.selectTitleByLsnLineNo", lsnLineNo);
			return lesson;
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
	public LessonLine selectReviewByLsnLineNo(int lsnLineNo) throws FindException {
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			LessonLine ll = session.selectOne("com.golflearn.mapper.LessonReviewMapper.selectReviewByLsnLineNo", lsnLineNo);
			return ll;
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
	public void insertReview(LessonReview lsnReview) throws AddException {
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			session.insert("com.golflearn.mapper.LessonReviewMapper.insertReview", lsnReview);
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(session != null) {
				session.close();
			}
		}
	}

	@Override
	public void modifyReview(LessonReview lsnReview) {
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			session.update("com.golflearn.mapper.LessonReviewMapper.modifyReview", lsnReview);
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(session != null) {
				session.close();
			}
		}
	}

}
