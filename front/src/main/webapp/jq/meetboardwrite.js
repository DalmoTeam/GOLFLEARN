$(function () {
    let loginedNickname = localStorage.getItem("loginedNickname");

    //-----모임 카테고리 셀렉트 옵션으로 불러오기 START-----
    $.ajax({
		url: "http://localhost:1129/backmeet/meet/board/selectctg",
		success: function (jsonObj) {
			let $meetCtgOpt = $('select[name=meetCategory]');
			let meetCtgHTML = '';
            meetCtgHTML += '<option name="category"> 선택 </option>';
            $(jsonObj).each(function (index, item) {//
                meetCtgHTML += '<option name="category" value="' + item.meetCtgNo + '"';
                meetCtgHTML += '>' + item.meetCtgTitle + '</option>';
			})
			$meetCtgOpt.html(meetCtgHTML);
			return false;
		},
		error: function (jqXHR) {
			// alert("error: " + jqXHR.status);
		}
	});
    //-----모임 카테고리 셀렉트 옵션으로 불러오기 END-----

    //-----summernote 실행 START-----
    $('#summernote').summernote({
        placeholder: '내용을 입력해 주세요',
        height: 200, // 에디터 높이
        minHeight: null, // 최소 높이
        maxHeight: null, // 최대 높이
        focus: true, // 에디터 로딩후 포커스를 맞출지 여부
        lang: "ko-KR", // 한글 설정
        disableResizeEditor: true, // 크기 조절 기능 삭제
        fontNames: ['Arial', 'Arial Black', 'Comic Sans MS', 'Courier New', 'Helvetica neue', 'Helvetica', 'Impact', 'Lucida Grande', 'Tahoma', 'Times New Roman', 'Verdana', 'Tahoma', 'Courier New', '맑은 고딕', '굴림', '돋움'],
        fontNamesIgnoreCheck: ['Arial', 'Arial Black', 'Comic Sans MS', 'Courier New', 'Helvetica neue', 'Helvetica', 'Impact', 'Lucida Grande', 'Tahoma', 'Times New Roman', 'Verdana', 'Tahoma', 'Courier New', '맑은 고딕', '굴림', '돋움'],
        toolbar: [
            ['fontname', ['fontname']],
            ['fontsize', ['fontsize']],
            ['style', ['bold', 'italic', 'underline', 'clear']],
            ['color', ['color']],
            ['table', ['table']],
            ['para', ['paragraph']],
            ['insert', ['link']],
            ['view', []]
        ],
        fontSizes: ['8', '9', '10', '11', '12', '14', '16', '18', '20', '22', '24', '28', '30', '36', '50', '72'],
    });
    //-----summernote 실행 END-----

    //----등록 버튼 클릭 START----
    let $btObj = $('button[name=register]');
    $btObj.click(function () {
        if (!confirm('글을 등록하시겠습니까?')) {
            return false;
        } else {
            let text = $('#summernote').summernote('code');

            let meetBoardTitle = $('input[name=meetBoardTitle').val();
            let meetBoardMeetDt = $('input[name=meetBoardMeetDt]').val();
            let meetBoardMaxCnt = $('select[name=meetBoardMaxCnt]').val();
            let meetBoardLocation = $('input[name=meetBoardLocation]').val()
            let meetBoardContent = text;
            meetBoardMeetDt = meetBoardMeetDt.replaceAll('-', '/'); //날짜값 형태 변환 : 22/09/14
            let meetCtgNo = $('select[name=meetCategory]').val();
            console.log(meetCtgNo);
            let data = {
                "userNickname": loginedNickname,
                "meetCategory": { "meetCtgNo": meetCtgNo },
                "meetBoardTitle": meetBoardTitle,
                "meetBoardMeetDt": meetBoardMeetDt,
                "meetBoardMaxCnt": meetBoardMaxCnt,
                "meetBoardLocation": meetBoardLocation,
                "meetBoardContent": meetBoardContent
            };

            var jsonData = JSON.stringify(data);
            console.log(jsonData);

            $.ajax({
                url: "http://localhost:1129/backmeet/meet/board/write",
                method: "post",
                data: jsonData,
                headers: {
                    "content-Type": "application/json",
                },
                success: (jsonObj) => {
                    alert("글이 작성되었습니다");
                    window.location = '../html/meetboardlist.html';
                },
                error: function (jqXHR) {//응답실패
                    alert(jqXHR.responseText);
                }
            });
            return false;
        }
    });

    //----취소버튼 클릭하가 START----
    let $btCancelObj = $("button[name='cancel']");
    $btCancelObj.click(function () {//취소버튼 클릭시
        if (!confirm('취소하시겠습니까?\n 취소하시면 지금까지 작성하신 내용은 저장되지 않습니다.')) {
            return false;
        } else {
            window.location = '../html/meetboardlist.html';
        }
    });
    //----취소버튼 클릭하기 END----

});