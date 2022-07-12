package com.golflearn.control;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golflearn.dto.Lesson;
import com.golflearn.exception.FindException;
import com.golflearn.repository.LessonOracleRepository;
import com.golflearn.repository.LessonRepository;

@WebServlet("/main")
public class ViewMainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json;charset=UTF-8");	
		PrintWriter out = response.getWriter();
		String result = "";
		LessonRepository repo = new LessonOracleRepository();
		try {
			// json 컨텐츠를 java 객체로 역직렬화하거나 json으로 직렬화할때 사용하는 라이브러리임
			ObjectMapper mapper = new ObjectMapper(); 
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			
			Map<String, Object> map = new HashMap<>();
			List<Lesson> lsnList = repo.selectAll();
			System.out.println("--------------------test ----------------");
//			map.put("status", 1);
			map.put("lsns", lsnList);//
			
			request.setAttribute("lsns", lsnList);
			// String타입으로 변환
			String jsonValue = mapper.writeValueAsString(map);
			System.out.println("jsonValue :"+ jsonValue);
			
 			result = mapper.writeValueAsString(map);
			System.out.println("result :" + result);
			
			out.print(result);
		} catch (FindException e) {
			e.printStackTrace();
			Map<String, Object> map = new HashMap<>();
//			map.put("status", 0);
			map.put("msg", e.getMessage());
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.writeValueAsString(map);
			System.out.println("result: " + result);
			out.print(result);
		}
		
	}

}
