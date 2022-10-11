$(function(){
	let $btPayment = $("button[name=payment]");
	
	//입력된 객체 찾기
	let lsn_title = "" ;
	let pay_method = "";
	let name = localStrage.getItem("loginedName");
	let nickname = localStrage.getItem("loginedNickname");
	let email = localStrage.getItem("loginedId");
	let phone = localStrage.getItem("loginedPhone");


	$($btPayment).click(function () {
		var IMP = window.IMP; // 생략가능
		IMP.init("imp84404721"); // 가맹점 식별코드(부여받음)

		IMP.request_pay({
			pg: "html5_inicis", //이니시스 결제 사용
			pay_method: "card",
			merchant_uid: "merchant_" + new Date().getTime(),
			lsn_title: "주문명:결제테스트", //결제창에서 보여질 이름
			amount: 1000, //가격
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
				msg += "고유ID : " + rsp.imp_uid;
				msg += "상점 거래ID : " + rsp.merchant_uid;
				msg += "결제 금액 : " + rsp.paid_amount;
				msg += "카드 승인번호 : " + rsp.apply_num;
				msg += "결제 수단 : " + rsp.pay_method;
			} else {
				var msg = "결제에 실패하였습니다.";
				msg += "에러내용 : " + rsp.error_msg;
			}
			alert(msg);
		}
		);
	});

	// // 수강신청 버튼 찾기
	// let $btPayment =$("button[name=payment]");	
	// //버튼 클릭 시
	// $btPayment.click(function(jsonObj){
		
	// 	// DOM 객체들에서 사용할 데이터 뽑기
		
	// 	//레슨 정보 서블릿에서 가져오기
	// 	// let lsn_title = jsonarr.lsnTitle;
	// 	// let pro_name = jsonarr.userInfo.user.userName;	
	// 	// let lsn_no = jsonarr.lsnNo;
	// 	// let lsn_price = jsonarr.lsnPrice;
		
		
	// 	// $.ajax({
	// 	// 	url:"/back/user/login",
	// 	// 	method: "get",
	// 	// 	// data:,
	// 	// 	success :function(jsonObj){
	// 	// 		//결제 고객 정보 가져오기
	// 	// 		let user_id = jsonarr.userId;
	// 	// 		let user_nickname = jsonarr.userNickname;
	// 	// 		let user_phone = jsonarr.userPhone;
	// 	// 		let user_name = jsonarr.userName;
	// 	// 	},
	// 	// 	error : function(jqXHR){
	// 	// 		return ("정보 가져오기 실패"+jqXHR.status);
	// 	// 	}
	// 	// });

		

		
	// 	// function iamport() {
	// 		// 주문 페이지에 가맹점 식별코드 이용하여 IMP 객체 초기화
	// 		var IMP = window.IMP // 생략 가능
	// 		//가맹점 식별코드
	// 		IMP.init("imp84404721"); // IMP의 내 식별코드
			
	// 		let queue = new Array();
	// 		let num = queue.shift();
	// 		// 결제 시스템을 실행시키는 함수
	// 		IMP.request_pay({
	// 			pg: "html5_inicis",
	// 			pay_method: "card",
	// 			merchant_uid: 'payment_' + num ,// 상점에서 관리하는 주문 번호
	// 			name: "주문명 : 충분한 상체회전으로 예쁜 백스윙 만들기",
	// 			amount: 100000,
	// 			buyer_id: "hanmr@nate.com",
	// 			buyer_name: "한미래",
	// 			buyer_nickname: "데빌",
	// 			buyer_tel: "010-1234-3949",
				
	// 		},
	// 		function (rsp) { // callback
	// 			if (rsp.success) {
	// 		//[1] 서버단에서 결제정보 조회를 위해 jQuery ajax로 imp_uid 전달하기
	// 		jQuery.ajax({
	// 			url: "/payments/complete", //cross-domain error가 발생하지 않도록 주의해주세요
	// 			type: "POST",
	// 			dataType: "json",
	// 			data: {
	// 				imp_uid: rsp.imp_uid,
	// 				payment_method:rsp.pay_method
	// 				//기타 필요한 데이터가 있으면 추가 전달
	// 			},
	// 		})
	// 		.done(function (data) {
	// 			//[2] 서버에서 REST API로 결제정보확인 및 서비스루틴이 정상적인 경우
	// 			if (everythings_fine) {
	// 				var msg = "결제가 완료되었습니다.";
	// 				msg += "\n고유ID : " + rsp.imp_uid;
	// 				msg += "\n상점 거래ID : " + rsp.merchant_uid;
	// 				msg += "결제 금액 : " + rsp.paid_amount;
	// 				msg += "카드 승인번호 : " + rsp.apply_num;
	// 				// msg += "결제 수단 : "+ rsp.method;
					
	// 				alert(msg);
	// 			} else {
	// 				//[3] 아직 제대로 결제가 되지 않았습니다.
	// 				//[4] 결제된 금액이 요청한 금액과 달라 결제를 자동취소처리하였습니다.
	// 			}
	// 		});
	// 	} else {
	// 		var msg = "결제에 실패하였습니다.";
	// 		msg += "에러내용 : " + rsp.error_msg;
			
	// 		alert(msg);
	// 	}
	// }
	// );
	// // }
	
	// });		
});