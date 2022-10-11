$(function() {


  
	let $inputId = $('input[name=user_id]');
	let $inputPhone = $('input[name=user_phone]');

	let $form = $('form');
	$form.submit(function (event) {
		let url = "http://localhost:1124/back/user/find/pwd";
		let inputIdValue, inputPhoneValue;

		inputIdValue = $inputId.val();
		inputPhoneValue = $inputPhone.val();
		//console.log(inputIdValue,inputEmailValue);

		//let data = "user_id=" + inputIdValue + "&user_email=" + inputEmailValue;
		//console.log(data);
		$.ajax({
			url : url,
			method: "POST",
			data:{
				userId : $inputId.val(),		
				userPhone : $inputPhone.val()
				},
			success: function(data) {
				if(data.status == 1){
					//localStorage.setItem("sessionId", $.cookie("JSESSIONID"));
					
					localStorage.setItem("userId",$inputId.val());
					localStorage.setItem("authenticationKey", data.t);
					

					$('#content.modal-body').text("고객님의 번호로 인증번호가 전송되었습니다. 인증번호를 확인해주세요");
					$('button#btn-secondary').click(function () {
						// location.href = "http://localhost:1123/front/html/changepwd.html";
						location.href = "../html/changepwd.html";
					});
				}else {
					$('#content.modal-body').text("인증번호 발송에 실패했습니다. 아이디와 번호를 정확히 입력해주세요.");
					$('button#btn-secondary').click(function () {
						// location.href = "http://localhost:1123/front/html/findpwd.html";
						location.href = "../html/findpwd.html";
					});
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				$('#content.modal-body').text("인증번호 발송에 실패했습니다. 아이디와 번호를 정확히 입력해주세요.");
					$('button#btn-secondary').click(function () {
						// location.href = "http://localhost:1123/front/html/findpwd.html";
						location.href = "../html/findpwd.html";
				});
			},
		});
		event.preventDefault();
		//return false;
	});
});