$(function () {
	//---------------화면 로딩되자마자----------------
	//레슨정보 서블릿에 보내기
	// let queryString = location.search.substring(1); 
	// let src = "../resale_images/";
	let queryString = location.search.split("=")[1];
	console.log(queryString);
	let lsn_title;
	let lsn_price;
	$.ajax({
		url: "http://localhost:1124/back/lesson/" + queryString,
		method: 'get',
		async: false,
		// data: queryString, 
		success: function (jsonObj) {
			if (jsonObj.status == 1) {
				console.log("------------");
				// let lesson = jsonObj.t;
				let loc_no = jsonObj.t.Lesson.locNo; //지역코드(ex:11011에 해당하는 주소지역 다 문자로 표현)
				lsn_title = jsonObj.t.Lesson.lsnTitle; //레슨명
				let lsn_star_score = jsonObj.t.Lesson.lsnStarScore; //레슨별점
				let lsn_review_cnt = jsonObj.t.Lesson.lsnStarPplCnt; //리뷰갯수
				let pro_star_score = jsonObj.t.Lesson.proStarScore; //프로별점
				//레슨상세정보(레슨소개, 프로소개) 서블릿에서 가져오기
				let lsn_intro = jsonObj.t.Lesson.lsnIntro;//레슨소개
				let user_name = jsonObj.t.Lesson.userInfo.userName; //프로명
				let pro_intro = jsonObj.t.Lesson.userInfo.proInfo.proCareer;//프로소개
				lsn_price = jsonObj.t.Lesson.lsnPrice;
				let lsn_cnt_sum = jsonObj.t.Lesson.lsnCntSum;
				let lsn_per_time = jsonObj.t.Lesson.lsnPerTime + '분';
				let lsn_days = jsonObj.t.Lesson.lsnDays + '일';
				let lsn_lv = jsonObj.t.Lesson.lsnLv;

				//파일불러오기-------------
				let fileNameArr = jsonObj.t.imageFileNames;
				console.log("파일명 : " + fileNameArr);
				console.log("----");
				// console.log(dto);
				// console.log("저장된 파일 개수는 : " + fileNameArr.length);
				let insertHtml = "";
				let $parent = $("div.viewlesson-img");
				// for (let i = 0; i < fileNameArr.length; i++) {
				  insertHtml += "<img src=''";
				  // insertHtml += src + detailObj.resaleBoardNo + "/" + fileNameArr[i];
				  insertHtml += " alt='' width='300px;' height=' auto;'/>";
				  insertHtml += "&nbsp;&nbsp";
				  //-----------------
				// }
				$parent.append(insertHtml);

				$('div.viewlesson>div.viewlesson-img>img').attr('src', '../lsn_images/' + queryString + '_LessonThumbnail.jpg').attr('alt', lsn_title);
				$('div.viewlesson ul>div>li>span.loc_no').html(loc_no);	//지역은 api로 넘어갈때 수정
				$('div.viewlesson ul>div>li>span.lsn_title').html(lsn_title);
				$('div.viewlesson ul>div>li>span.lsn_star_score').html(lsn_star_score);
				$('div.viewlesson ul>div>li>span.lsn_review_cnt').html(lsn_review_cnt);
				$('div.viewlesson ul>div>li>span.user_name').html(user_name);
				$('div.viewlesson ul>div>li>span.pro_star_score').html(pro_star_score);
				$('div.viewlesson ul>div>li>span.lsn_cnt_sum').html(lsn_cnt_sum);
				$('div.viewlesson ul>div>li>span.lsn_per_time').html(lsn_per_time);
				$('div.viewlesson ul>div>li>span.lsn_days').html(lsn_days);
				$('div.viewlesson ul>div>li>span.lsn_lv').html(lsn_lv);
				$('div.viewlesson ul>div>li>span.lsn_price').html(lsn_price);
				$('div.lsn_intro').html(lsn_intro);
				$('div.pro_intro').html(pro_intro);
				//-----------레슨후기 list START-------------
				let lessonLines = jsonObj.t.Lesson.lsnLines;
				let $lsnObj = $('div.reviewlist');
				$(lessonLines).each(function (i, element) {
					let reviewNickname = element.lsnReview.stdtNickname;
					if (reviewNickname != null) {//레슨리뷰가 있으면 append
						$copyObj = $lsnObj.clone();
						let reviewDt = element.lsnReview.reviewDt;
						let review = element.lsnReview.review;
						let lessonReview = '<ul>';
						lessonReview += '<li><div>작성자아이디: <span class = "reviewId">' + reviewNickname + '</span></div></li>'
						lessonReview += '<li><div>작성날짜: <span class = "reviewDt">' + reviewDt + '</span></div></li>'
						lessonReview += '<li><div>리뷰: <span class = "review">' + review + '</span></div></li>'
						lessonReview += '</ul>'
						$copyObj.find('div.reviewdetail').html(lessonReview);
						$('div.lsn').append($copyObj);
					}
				});
				// lsnPrice = lsn_price;
				// lsnTitle = lsn_title;
			}// if문
		}, //success
		error: function (jqXHR) {
			alert('오류:' + jqXHR.status);
		},
	});
	// console.log(lsn_price);
	//------------수강신청 버튼 클릭 START-------------
	//수강신청 버튼 찾기
	let $btPayment = $("button[name=payment]");
	//입력된 객체 찾기
	let pay_method = "";
	let name = localStorage.getItem("loginedName");
	// console.log(name);
	let nickname = localStorage.getItem("loginedNickname");
	let email = localStorage.getItem("loginedId");
	let phone = localStorage.getItem("loginedPhone");
	$(document).ready(function () {
		// $("div.viewlesson ul>div>li").on("click","div.payment>button",function () {
		$($btPayment).on("click", function () {
			console.log(lsn_price);
			console.log(lsn_title);
			// lsn_price= 5000;
			// lsn_title= "아아"
			var IMP = window.IMP; // 생략가능
			IMP.init("imp84404721"); // 가맹점 식별코드(부여받음)
			IMP.request_pay({
				pg: "html5_inicis", //이니시스 결제 사용
				pay_method: "card",
				merchant_uid: "merchant_" + new Date().getTime(),
				name: lsn_title, //결제창에서 보여질 이름
				amount: lsn_price, //가격
				buyer_id: email,
				buyer_name: name,
				buyer_nickname: nickname,
				buyer_tel: phone,
				m_redirect_url: "https://localhost:1124/front/html/mypage.html", // 결제 이후 돌아가는 페이지
			},
				function (rsp) {
					console.log(rsp);
					if (rsp.success) {
						var msg = "결제가 완료되었습니다.";
						// msg += "고유ID : " + rsp.imp_uid;
						// msg += "상점 거래ID : " + rsp.merchant_uid;
						// msg += "결제 금액 : " + rsp.paid_amount;
						// msg += "카드 승인번호 : " + rsp.apply_num;
						// msg += "결제 수단 : " + rsp.pay_method;
						$.ajax({
							url:"http://localhost:1124/back/lesson/payment",
							method:"post",
							headers: { "Content-Type": "application/json" },
							data:{paymentNo: merchant_uid,paymentMethod : pay_method},
							success:"",
							error:"",
						});
					} else {
						var msg = "결제에 실패하였습니다.";
						msg += "에러내용 : " + rsp.error_msg;
					}
					alert(msg);
				}
			);
		});
	});
	//------------레슨상세정보 네비바 클릭 START-------------
	$(document).ready(function () {
		$('#lsnIntroLink').click(function () {
			var offset = $('div.lsn_intro').offset(); //선택한 태그의 위치를 반환                
			//animate()메서드를 이용해서 선택한 태그의 스크롤 위치를 지정해서 0.4초 동안 부드럽게 해당 위치로 이동함
			$('html').animate({ scrollTop: offset.top }, 400);
		});
	});
	$(document).ready(function () {
		$('#proIntroLink').click(function () {
			var offset = $('div.pro_intro').offset(); //선택한 태그의 위치를 반환                
			//animate()메서드를 이용해서 선택한 태그의 스크롤 위치를 지정해서 0.4초 동안 부드럽게 해당 위치로 이동함
			$('html').animate({ scrollTop: offset.top }, 400);
		});
	});
	$(document).ready(function () {
		$('#reviewLink').click(function () {
			var offset = $('div.lsn').offset(); //선택한 태그의 위치를 반환                
			//animate()메서드를 이용해서 선택한 태그의 스크롤 위치를 지정해서 0.4초 동안 부드럽게 해당 위치로 이동함
			$('html').animate({ scrollTop: offset.top }, 400);
		});
	});

	//-----------상단이동버튼 클릭 START-------------
	$(window).scroll(function () {
		if ($(this).scrollTop() > 300) {
			$('.btn_gotop').show();
		} else {
			$('.btn_gotop').hide();
		}
	});
	$('.btn_gotop').click(function () {
		$('html, body').animate({ scrollTop: 0 }, 400);
		return false;
	});

	//------------레슨등록 프로회원만 보이도록 만들기 START-------------

});


$(function () {
	//---------------화면 로딩되자마자----------------
	//레슨정보 서블릿에 보내기
	// let queryString = location.search.substring(1); 
	
	let queryString = location.search.split("=")[1];
	console.log(queryString);
	let lsn_title;
	let lsn_price;
	$.ajax({
		url: "http://localhost:1124/back/lesson/" + queryString,
		method: 'get',
		// data: queryString, 
		success: function (jsonObj) {
			if (jsonObj.status == 1) {
				let lesson = jsonObj.t;
				let loc_no = lesson.locNo; //지역코드(ex:11011에 해당하는 주소지역 다 문자로 표현)
				lsn_title = lesson.lsnTitle; //레슨명
				let lsn_star_score = lesson.lsnStarScore; //레슨별점
				let lsn_review_cnt = lesson.lsnStarPplCnt; //리뷰갯수
				let user_name = lesson.userInfo.userName; //프로명
				let pro_star_score = lesson.proStarScore; //프로별점
				//레슨상세정보(레슨소개, 프로소개) 서블릿에서 가져오기
				let lsn_intro = lesson.lsnIntro;//레슨소개
				let pro_intro = lesson.userInfo.proInfo.proCareer;//프로소개
				lsn_price = lesson.lsnPrice;
				let lsn_cnt_sum = lesson.lsnCntSum;
				let lsn_per_time = lesson.lsnPerTime + '분';
				let lsn_days = lesson.lsnDays + '일';
				let lsn_lv = lesson.lsnLv;
				$('div.viewlesson>div.viewlesson-img>img').attr('src', '../lsn_images/' + queryString + '_LessonThumbnail.jpg').attr('alt', lsn_title);
				$('div.viewlesson ul>div>li>span.loc_no').html(loc_no);	//지역은 api로 넘어갈때 수정
				$('div.viewlesson ul>div>li>span.lsn_title').html(lsn_title);
				$('div.viewlesson ul>div>li>span.lsn_star_score').html(lsn_star_score);
				$('div.viewlesson ul>div>li>span.lsn_review_cnt').html(lsn_review_cnt);
				$('div.viewlesson ul>div>li>span.user_name').html(user_name);
				$('div.viewlesson ul>div>li>span.pro_star_score').html(pro_star_score);
				$('div.viewlesson ul>div>li>span.lsn_cnt_sum').html(lsn_cnt_sum);
				$('div.viewlesson ul>div>li>span.lsn_per_time').html(lsn_per_time);
				$('div.viewlesson ul>div>li>span.lsn_days').html(lsn_days);
				$('div.viewlesson ul>div>li>span.lsn_lv').html(lsn_lv);
				$('div.viewlesson ul>div>li>span.lsn_price').html(lsn_price);
				$('div.lsn_intro').html(lsn_intro);
				$('div.pro_intro').html(pro_intro);
				//-----------레슨후기 list START-------------
				let lessonLines = lesson.lsnLines;
				let $lsnObj = $('div.reviewlist');
				$(lessonLines).each(function (i, element) {
					let reviewNickname = element.lsnReview.stdtNickname;
					if (reviewNickname != null) {//레슨리뷰가 있으면 append
						$copyObj = $lsnObj.clone();
						let reviewDt = element.lsnReview.reviewDt;
						let review = element.lsnReview.review;
						let lessonReview = '<ul>';
						lessonReview += '<li><div>작성자아이디: <span class = "reviewId">' + reviewNickname + '</span></div></li>'
						lessonReview += '<li><div>작성날짜: <span class = "reviewDt">' + reviewDt + '</span></div></li>'
						lessonReview += '<li><div>리뷰: <span class = "review">' + review + '</span></div></li>'
						lessonReview += '</ul>'
						$copyObj.find('div.reviewdetail').html(lessonReview);
						$('div.lsn').append($copyObj);
					}
				});
				// lsnPrice = lsn_price;
				// lsnTitle = lsn_title;
			}// if문
		}, //success
		error: function (jqXHR) {
			alert('오류:' + jqXHR.status);
		},
	});
	// console.log(lsn_price);
	//------------수강신청 버튼 클릭 START-------------
	//수강신청 버튼 찾기
	let $btPayment = $("button[name=payment]");
	//입력된 객체 찾기
	let pay_method = "";
	let name = localStorage.getItem("loginedName");
	// console.log(name);
	let nickname = localStorage.getItem("loginedNickname");
	let email = localStorage.getItem("loginedId");
	let phone = localStorage.getItem("loginedPhone");
	$(document).ready(function () {
		$("div.viewlesson ul>div>li").on("click","div.payment>button",function () {
		// $($btPayment).on("click", function () {
			console.log(lsn_price);
			console.log(lsn_title);
			// lsn_price= 5000;
			// lsn_title= "아아"
			var IMP = window.IMP; // 생략가능
			IMP.init("imp84404721"); // 가맹점 식별코드(부여받음)
			IMP.request_pay({
				pg: "html5_inicis", //이니시스 결제 사용
				pay_method: "card",
				merchant_uid: "merchant_" + new Date().getTime(),
				name: lsn_title, //결제창에서 보여질 이름
				amount: lsn_price, //가격
				buyer_id: email,
				buyer_name: name,
				buyer_nickname: nickname,
				buyer_tel: phone,
				m_redirect_url: "https://localhost:1124/front/html/mypage.html", // 결제 이후 돌아가는 페이지
			},
				function (rsp) {
					console.log(rsp);
					if (rsp.success) {
						var msg = "결제가 완료되었습니다.";
						// msg += "고유ID : " + rsp.imp_uid;
						// msg += "상점 거래ID : " + rsp.merchant_uid;
						// msg += "결제 금액 : " + rsp.paid_amount;
						// msg += "카드 승인번호 : " + rsp.apply_num;
						// msg += "결제 수단 : " + rsp.pay_method;
					} else {
						var msg = "결제에 실패하였습니다.";
						msg += "에러내용 : " + rsp.error_msg;
					}
					alert(msg);
				}
			);
		});
	});
	//------------레슨상세정보 네비바 클릭 START-------------
	$(document).ready(function () {
		$('#lsnIntroLink').click(function () {
			var offset = $('div.lsn_intro').offset(); //선택한 태그의 위치를 반환                
			//animate()메서드를 이용해서 선택한 태그의 스크롤 위치를 지정해서 0.4초 동안 부드럽게 해당 위치로 이동함
			$('html').animate({ scrollTop: offset.top }, 400);
		});
	});
	$(document).ready(function () {
		$('#proIntroLink').click(function () {
			var offset = $('div.pro_intro').offset(); //선택한 태그의 위치를 반환                
			//animate()메서드를 이용해서 선택한 태그의 스크롤 위치를 지정해서 0.4초 동안 부드럽게 해당 위치로 이동함
			$('html').animate({ scrollTop: offset.top }, 400);
		});
	});
	$(document).ready(function () {
		$('#reviewLink').click(function () {
			var offset = $('div.lsn').offset(); //선택한 태그의 위치를 반환                
			//animate()메서드를 이용해서 선택한 태그의 스크롤 위치를 지정해서 0.4초 동안 부드럽게 해당 위치로 이동함
			$('html').animate({ scrollTop: offset.top }, 400);
		});
	});

	//-----------상단이동버튼 클릭 START-------------
	$(window).scroll(function () {
		if ($(this).scrollTop() > 300) {
			$('.btn_gotop').show();
		} else {
			$('.btn_gotop').hide();
		}
	});
	$('.btn_gotop').click(function () {
		$('html, body').animate({ scrollTop: 0 }, 400);
		return false;
	});

	//------------레슨등록 프로회원만 보이도록 만들기 START-------------

});