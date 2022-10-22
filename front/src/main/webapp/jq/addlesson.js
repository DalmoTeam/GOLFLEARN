$(function () {
	let loginedId = localStorage.getItem("loginedId");

	//-----레슨지역 목록 불러오기 START-----
	//시도 목록
	$.ajax({
		url: "http://localhost:1124/back/seeksidosigu",
		success: function (jsonObj) {
			$sidoObj = $('select[name=sd]');
			var arr = [];
			let sido = '';
			$(jsonObj.sido).each(function (key, item) {
				$keyObj = Object.keys(item);
				$itemObj = Object.values(item);
				for (let i = 0; i < $itemObj.length; i++) {
					arr.push($itemObj[i]);
					sido += '<option class="sido" name="sido" value="' + $keyObj[i] + '"';
					sido += '>' + $itemObj[i] + '</option>';
				}
			})
			$sidoObj.append(sido);
			return false;
		},
		error: function (jqXHR) {
		}
	});

	let $sidoCombo = $('select[name=sd]'); 
	let $sigunguCombo = $('select[name=sgg]');
	$sidoCombo.change(function () {
		
		$sigunguCombo.empty();
		let $sidoVal = $(this);
		let sigungu = '';
		//시군구목록
		$.ajax({
			url: "http://localhost:1124/back/seeksidosigu/" + $sidoVal.val(),
			success: function (jsonObj) {
				$(jsonObj.sigungu).each(function (index, item) {
					$keyObj = Object.keys(item);
					$itemObj = Object.values(item);
					for (let i = 0; i < $itemObj.length; i++) {
						sigungu += '<option class="sigungu" name="sigungu" value="' + $keyObj[i] + '"';
						sigungu += '>' + $itemObj[i] + '</option>';
					}
				})
				$sigunguCombo.append(sigungu);
				$sigunguCombo.change(function () {
					let $sigunguVal = $(this);
					console.log($sigunguVal.val());
				})
				return false;
			},
			error: function (jqXHR) {
			}

		})
	});
	//-----레슨지역 목록 불러오기 END-----

	//-----summernote 실행 START-----
	$("#summernote").summernote({
		placeholder: "레슨에 대한 소개를 자세히 입력해주세요",
		height: 200, // 에디터 높이
		minHeight: null, // 최소 높이
		maxHeight: null, // 최대 높이
		focus: true, // 에디터 로딩후 포커스를 맞출지 여부
		lang: "ko-KR", // 한글 설정
		disableResizeEditor: true, // 크기 조절 기능 삭제
		fontNames: [
			"Arial",
			"Arial Black",
			"Comic Sans MS",
			"Courier New",
			"Helvetica neue",
			"Helvetica",
			"Impact",
			"Lucida Grande",
			"Tahoma",
			"Times New Roman",
			"Verdana",
			"Tahoma",
			"Courier New",
			"맑은 고딕",
			"굴림",
			"돋움",
		],
		fontNamesIgnoreCheck: [
			"Arial",
			"Arial Black",
			"Comic Sans MS",
			"Courier New",
			"Helvetica neue",
			"Helvetica",
			"Impact",
			"Lucida Grande",
			"Tahoma",
			"Times New Roman",
			"Verdana",
			"Tahoma",
			"Courier New",
			"맑은 고딕",
			"굴림",
			"돋움",
		],
		toolbar: [
			["fontname", ["fontname"]],
			["fontsize", ["fontsize"]],
			["style", ["bold", "italic", "underline", "clear"]],
			["color", ["color"]],
			["table", ["table"]],
			["para", ["paragraph"]],
			["insert", ["link"]],
			["view", []],
		],
		fontSizes: [
			"8",
			"9",
			"10",
			"11",
			"12",
			"14",
			"16",
			"18",
			"20",
			"22",
			"24",
			"28",
			"30",
			"36",
			"50",
			"72",
		],
		fontSizes: ['8', '9', '10', '11', '12', '14', '16', '18', '20', '22', '24', '28', '30', '36', '50', '72']
	});
	//-----summernote 실행 END-----

	// ------------레슨정보 등록버튼 클릭 START------------
	$("button[name='register']").click(function () {
		let $formObj = $('form');
		let formData = new FormData();//new FormData($FormObj[0]);

		//레슨정보
		let lesson = {};
		lesson.locNo = $('option[name=sigungu]').val();
		lesson.lsnTitle = $('input[name=lsn_title]').val();
		lesson.lsnDays = $('input[name=lsn_days]').val();
		lesson.lsnPrice = $('input[name=lsn_price]').val();
		lesson.lsnLv = $('select[name=lsn_lv]').val();
		lesson.lsnTitle = $('input[name=lsn_title]').val();
		lesson.lsnCntSum = $('input[name=lsn_cnt_sum]').val();
		lesson.lsnPerTime = $('input[name=lsn_per_time]').val();

		let text = $('#summernote').summernote('code');//summernote->text 데이터타입 변환
		lesson.lsnIntro = text;

		//레슨분류정보
		let lsnClassifications = [];
		let clubNos = $("input[name=club_no]:checked");
		for (let i = 0; i < clubNos.length; i++) {
			lsnClassifications[i] = { clubNo: $(clubNos[i]).val() };
		}
		lesson.lsnClassifications = lsnClassifications;
		
		//유저정보
		let userInfo = {};
		userInfo.userId = loginedId;
		lesson.userInfo = userInfo;

		let strLesson = JSON.stringify(lesson);
		formData.append("strLesson", strLesson);

		//파일업로드
		let file = $("input[name=lsn_file]")[0];
		formData.append("file", file.files[0]);

		// console.log(formData.get("strLesson"));
		// console.log(formData.get("file"));
		// console.log('------------');

		$.ajax({
			url: "http://localhost:1124/back/lesson/request",
			method: 'post',
			data: formData,//파일업로드용 설정
			processData: false,//파일업로드용 설정
			contentType: false,//파일업로드용 설정
			success: function (responseData) {
				alert("등록하신 레슨이 승인요청되었습니다.");
				location.replace("http://localhost:1123/front/html/main.html");
			},
			error: function (jqXHR, textStatus) {//응답실패
				alert('에러: ' + jqXHR.responseText);
			}
		});
		return false;
	});
	// ------------레슨정보 등록버튼 클릭 END------------

	//--------------이미지파일업로드 미리보기 START--------------
	$("input[name=lsn_file]").on("change", function (event) {
		// #lessonThumbnail
		var file = event.target.files[0];
		var reader = new FileReader();

		reader.onload = function (e) {
			$("#preview").attr("src", e.target.result);
		}

		reader.readAsDataURL(file);
	});
	// 확장자가 이미지 파일인지 확인
	function isImageFile(file) {
		var ext = file.name.split(".").pop().toLowerCase(); // 파일명에서 확장자를 가져온다. 

		return ($.inArray(ext, ["jpg", "jpeg", "gif", "png"]) === -1) ? false : true;
	}
	// 파일의 최대 사이즈 확인
	function isOverSize(file) {
		var maxSize = 3 * 1024 * 1024; // 3MB로 제한 

		return (file.size > maxSize) ? true : false;
	}

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
	//-----------상단이동버튼 클릭 END-------------

});
