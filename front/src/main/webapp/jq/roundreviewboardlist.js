$(function () {
    //1) 페이지 요청될 때 리스트 로드하기
	//showList함수로 구현 -> 재사용
	function showList(url, data) {
		$.ajax({
		url: url,
		method: "get",
		success: function (jsonObj) {
			if (jsonObj.status == 1) {
			//back의 resultbean에서 세팅된 status값 사용 
			let pageableContentObj = jsonObj.t.content;
			let $board = $("div.board-list").first();
			$board.show();

			$("div.board-list").not($board).remove();
			let $boardParent = $board.parent();

			$(pageableContentObj).each(function (index, board) {
				let $boardCopy = $board.clone();

				$boardCopy.find("div.board-list__content__no").html(board.roundReviewBoardNo);
				//첫번째 썸네일을 이미지로 사용  //image_1.PNG
				$boardCopy.find("img.board-list__content__image").attr("src","../roundreview_images/" +	board.roundReviewBoardNo + "/s_1.PNG");
				$boardCopy.find("div.board-list__content__title").html(board.roundReviewBoardTitle);
				$boardCopy.find("div.board-list__content__cmt-cnt").html(board.roundReviewBoardCmtCnt);
				$boardCopy.find("div.board-list__content__nickname").html(board.userNickname);
				$boardCopy.find("div.board-list__content__dt").html(board.roundReviewBoardDt);
				$boardCopy.find("div.board-list__content__view-cnt").html(board.roundReviewBoardViewCnt);
				$boardCopy.find("div.board-list__content__like-cnt").html(board.roundReviewBoardLikeCnt);

				$boardParent.append($boardCopy);
			});

			$("div.board-list").first().hide();

			//페이징처리
			let $pageGroup = $("div.page-group");
			let $pageGroupHtml = "";

			//pageable에서 받아온 정보들을 토대로
			//현재페이지, 총 페이지, 크기, 시작페이지번호, 끝페이지번호 등을 계산 
			let pageableObj = jsonObj.t;
			let currentPage = pageableObj.number + 1;
			let totalPage = pageableObj.totalPages;
			let size = pageableObj.size;
			let endPageNo = Math.ceil(currentPage / size) * size;
			let startPageNo = endPageNo - size + 1;
			if (totalPage < endPageNo) {
				endPageNo = totalPage;
			}
			
			//startPageNo가 1보다 클경우 (즉 2번째 페이지 그룹일 경우) ◁를 표시하여 이전 5페이지를 볼 수 있음 
			if (startPageNo > 1) {
				$pageGroupHtml += '<span class="prev">◁</span>';
			}

			//startPage부터 endPage까지 숫자 넣어주기
			//현재페이지면 disable시켜서 누르지 못하게 하기 
			for (let i = startPageNo; i <= endPageNo; i++) {
				$pageGroupHtml += "&nbsp;&nbsp&nbsp&nbsp;";
				if (currentPage == i) {
				$pageGroupHtml += '<span class="disabled">' + i + "</span>";
				} else {
				$pageGroupHtml += "<span>" + i + "</span>";
				}
			}
			//back에서 보내준 endPage 값이 totalPage값보다 작으면 ▷나오게 
			if (endPageNo < totalPage) {
				$pageGroupHtml += "&nbsp;&nbsp&nbsp&nbsp;";
				$pageGroupHtml += '<span class="next">▷</span>';
			}
			//pageGroupHtml에 받아놨던 정보를 pageGroup selector에 넣어주기
			$pageGroup.html($pageGroupHtml);
			} else {
			alert(jsonObj.msg); //rb에 set된 메시지
			}
		},
		error: function (jqXHR) {
			alert("에러:" + jqXHR.status);
		},
		});
	}

	//위에서 만든함수 호출 
	showList("http://localhost:1125/backroundreview/board/list");

	//2) 페이지그룹 페이지 클릭
	//span 태그들 중에서 disabled가 아닌 요소 찾기
	$("div.page-group").on("click", "span:not(.disabled)", function () {
		// let orderType = data.split("=");
		let orderType = 0;
		$("ul.order>li").each(function (index, element) {
		let $aObj = $(element).find("a");
		//클릭한 페이지 번호의 색이 빨간색인경우 
		if ($aObj.css("color") == "rgb(255, 0, 0)") {
			orderType = index;
			return false;
		}
		});

		let pageNo = 0;
		//클릭한 것이 ◁ 인 경우 
		if ($(this).hasClass("prev")) {
		pageNo = parseInt($(this).next().html()) - 2;
		//클릭한 것이 ▷ 인 경우 
		} else if ($(this).hasClass("next")) {
		pageNo = parseInt($(this).prev().html());
		//일반 번호인경우 -> pageable에서는 1번페이지가 0으로 시작 
		} else {
		pageNo = parseInt($(this).html()) - 1;
		}
		//trim() 양끝의 공백을 제거
		let word = $("div.search>input[name=search-box]").val().trim();
		let url = "";
		let data = "";
		//검색어가 없는 경우 최신순으로 리스트를 보여주기
		if (word == "") {
		url ="http://localhost:1125/backroundreview/board/list/" + orderType + "/" + pageNo;
		} else {
		//검색어가 있는 경우 검색어를 path에 넣어주고 back 에 보낼 data를 만들기
		url ="http://localhost:1125/backroundreview/search/" + word + "/" + pageNo;
		data = "currentPage=" + pageNo + "&word=" + word;
		}
		showList(url, data);
		return false;
	});

	//3) 검색하기
	$("div.search>button.searchBtn").click(function () {
		let word = $("div.search>input[name=search-box]").val().trim();
		//검색어 PathVariable로 보내기 
		let url = "http://localhost:1125/backroundreview/search/" + word;
		let data = "";
		showList(url, data);
		return false;
	});

	//4) 최신순 정렬하기
	$("ul.order>li.order__recent>a").click(function () {
		let orderType = 0;
		//정렬방식 PathVariable로 보내기
		let url = "http://localhost:1125/backroundreview/board/list/" + orderType;
		let data = "";

		$("ul.order>li>a").css("color", "#64DB99"); //기본
		$(this).css("color", "red"); //클릭된경우
		showList(url, data);
	});

	//5) 조회순 정렬하기
	$("ul.order>li.order__view-cnt>a").click(function () {
		let orderType = 1;
		//정렬방식 PathVariable로 보내기
		let url = "http://localhost:1125/backroundreview/board/list/" + orderType;
		let data = "";

		$("ul.order>li>a").css("color", "#64DB99"); //기본
		$(this).css("color", "red"); //클릭된경우
		showList(url, data);
	});

	//6) 좋아요순 정렬하기
	$("ul.order>li.order__like-cnt>a").click(function () {
		let orderType = 2;
		//정렬방식 PathVariable로 보내기
		let url = "http://localhost:1125/backroundreview/board/list/" + orderType;
		let data = "";

		$("ul.order>li>a").css("color", "#64DB99"); //기본
		$(this).css("color", "red"); //클릭된경우
		showList(url, data);
	});

	//7) 글 작성 페이지로 이동하기 
	$("header button.write").click(function () {
		$(location).attr("href", "../html/roundreviewboardwrite.html");
	});

	//8) 제목이나 사진 눌렀을때 해당 게시글로 이동하기
	$("div.board").on("click","img.board-list__content__image, div.board-list__content__title",function () {
		let $roundReviewBoardNoObj = $(this).parent().find("div.board-list__content__no");
		let round_review_board_no = $roundReviewBoardNoObj.html();

		//쿼리스트링으로 클릭한 게시글번호 보내주기 
		location.href =	"../html/roundreviewdetail.html?round_review_board_no=" + round_review_board_no;
		}
	);

	//9) 최상단 라운드리뷰 글자 클릭하면 리스트페이지 재로딩 
	$("h2").on("click", function () {
		location.href = "";
	});
});