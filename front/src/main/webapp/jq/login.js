$(function () {
	//아이디 입력 객체 찾기
	let $inputId = $("input[name=user_id]");

	//비밀번호 입력 객체 찾기
	let $inputPwd = $("input[name=user_pwd]");

	// 데이터 한번에 전송 해 줄 form 객체 생성
	let $form = $("form");
	$form.submit(function () {
		let url = "http://localhost:1124/back/user/login";
		// let inputIdValue = $inputId.val();
		// let inputPwdValue = $inputPwd.val();
		// let data = "userId=" + inputIdValue + "&userPwd=" + inputPwdValue;
		console.log($inputId.val());
		console.log($inputPwd.val());
    $.ajax({
		url: url,
		method: "post",
		data: {
			userId : $inputId.val(),
			userPwd : $inputPwd.val()
		},
		success: function (jsonObj) {
			if (jsonObj.status == 1) {
				alert(jsonObj.msg);
				let userObj = jsonObj.t;
				localStorage.setItem("loginedUserType", userObj.userType);
				localStorage.setItem("loginedNickname", userObj.userNickname);
				localStorage.setItem("loginedId", userObj.userId);

				localStorage.setItem("loginedPhone", userObj.userPhone);
				localStorage.setItem("loginedName", userObj.userName);

				
				// location.replace("http://localhost:1123/front/html/main.html");
				location.replace("../html/main.html");
				
			
			} else {
				alert("로그인에 실패했습니다");
			}
		},
		error: function (jqXHR, statusText, errorThrown) {
			alert("로그인에 실패했습니다");
		},
    });
    return false;
	});
});