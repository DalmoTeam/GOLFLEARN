$(function () {
    //로딩 되자마자 글 목록 불러오기(1 페이지)
    // let url = "http://localhost:1126/backresale/resale/board/list/1";
    showList("http://localhost:1128/noticeboard/notice/list");
    let type = localStorage.getItem("loginedUserType");
    function showList(url) {
        $.ajax({
        url: url,
        success: function (jsonObj) {
            console.log("3--" + url);
            if (jsonObj.status == 1) {
            
            let pageBeanObj = jsonObj.t;
            //게시글 div를 원본으로 함
            // 게시글 목록을 찾아 복붙 해 넣는 작업
            let $board1 = $("div.board-list").first();
            // let $board= $("div.boardlist__content").first();
            $board1.show();
            // 원본 하나 선택 후 나머지 게시글의 div삭제하는 작업
            $("div.board-list").not($board1).remove();
            let $board = $("div.boardlist__content").first();
            let src = "../notice_image/";
            $board1.empty();
            $(pageBeanObj.list).each(function (index, board) {
                let $boardCopy = $board.clone();
                console.log(board.userNickname); // 출력됨
                console.log(board.noticeBoardNo);
                $.ajax({
                    url: "http://localhost:1128/noticeboard/notice/downloadimage",
                    data: "boardNo=" + board.noticeBoardNo,
                    method: "get",
                    // credentials:true,
                    cache: false,
                    xhrFields: {
                        responseType: "blob", //이미지 다운로드 문법
                        // withCredentials: true,
                    },
                    success: function (responseData) { // 받아온 이미지들 객체를 넣어줌
                        let url = URL.createObjectURL(responseData);
                        $boardCopy
                        .find("div>img.board-list__content__thumbnail")
                        .attr("src", url);
                    },
                });
                $boardCopy
                .find("div.board-list__content__no")
                .html(board.noticeBoardNo);
                $boardCopy
                .find("div.board-list__content__title")
                .html(board.noticeBoardTitle);
                $boardCopy
                .find("div.board-list__content__nickname")
                .html(board.userNickname);
                $boardCopy
                .find("div.board-list__content__dt")
                .html(board.noticeBoardDt);
                $boardCopy
                .find("div.board-list__content__view-cnt")
                .html(board.noticeBoardViewCnt);
                $boardCopy
                .find("div.board-list__content__cmt-cnt")
                .html(board.noticeBoardCmtCnt);
                $boardCopy
                .find("div.board-list__content__like-cnt")
                .html(board.noticeBoardLikeCnt);
                // $boardCopy
                // .find("div>img.board-list__content__thumbnail")
                // .attr(
                //     "src",
                //     src  + board.noticeBoardNo +  "/" + board.noticeBoardNo + "_image_" + "s_1" + ".jpeg"
                // );
                $board1.append($boardCopy);
            });

            $board.hide();
            // 페이지 그룹
            let $pagegroup = $("div.page-group");
            let $pagegroupHtml = "";

            let currentPage = pageBeanObj.currentPage;
            let startPage = pageBeanObj.startPage;
            let endPage = pageBeanObj.endPage;
            let totalPage = pageBeanObj.totalPage;
            let cntPerPageGroup = pageBeanObj.cntPerPageGroup;
            let cntPerPage = 5;

            if (endPage > totalPage) {
                endPage = totalPage;
            }
            let next = endPage + 1; // 화면에 보여질 마지막 페이지 번호
            let prev = startPage - 1; // 화면에 보여질 첫번째 페이지 번호

            if (totalPage < 1) {
                startPage = endPage;
            }
            let pages = $("ul.page-group__pages");
            pages.empty();
            if (startPage > 5) {
                pages.append('<li class="pagination--item"></li>');
            }
            //내용 채워주는 것
            if (pageBeanObj.startPage > 5) {
                $pagegroupHtml += '<span class="prev"> ◁ </span>';
            }
            for (let i = pageBeanObj.startPage; i <= pageBeanObj.endPage; i++) {
                $pagegroupHtml += "&nbsp;&nbsp;";
                if (pageBeanObj.currentPage == i) {
                // 현재페이지인 경우
                $pagegroupHtml += '<span class="disabled">' + i + "</span>";
                } else {
                $pagegroupHtml += "<span>" + i + "</span>";
                }
            }
            if (pageBeanObj.endPage < pageBeanObj.totalPage) {
                $pagegroupHtml += "&nbsp;&nbsp;";
                $pagegroupHtml += '<span class="next"> ▷ </span>';
            }

            $pagegroup.html($pagegroupHtml);
            } else {
            alert(jsonObj.msg);
            }
        },
        error: function (jqXHR) {
            alert("에러 : " + jqXHR.status);
        },
        });
    }
    
    // 글쓰기 버튼 클릭 시 글쓰기 페이지로 이동
    let loginedUserType = localStorage.getItem("loginedUserType");
    let $btWrite = $("div.write > button");
    if (type == 2) {
        $btWrite.click(function () {
            $btWrite.show();
            location.href = "../html/noticeboardwrite.html";
        });
    } else {
        $btWrite.hide();
    }

    //클릭한 해당 게시물로 이동
    $("div.board").on("click", "div.boardlist__content", function () {
        let $board = "div.board-list__content__no";
        $boardNo = $(this).children($board).text();
        // alert($boardNo);
        location.href = "../html/noticedetail.html?notice_board_no=" + $boardNo;
    });

    // 검색창 클릭
    let $btSearch = $("div.search-container > button.search__button");
    $btSearch.click(function(){
        let word = $("div.search>div.search__box>input.search__input").val().trim();
        console.log(word);
        let url = "";
        if (word == "") {
            url = "http://localhost:1128/noticeboard/notice/list/" + pageNo;
            // data = "currentPage=" + pageNo;
        } else {
            url = "http://localhost:1128/noticeboard/notice/board/search/"+ word;
        }
        let data = "word=" + word;
        // let data = "currentPage=" + pageNo + "&word=" + word;
        showList(url);
        return false;
    });

    // ----- 페이지 그룹의 페이지를 클릭 START -----
    $("div.page-group").on("click", "span:not(.disabled)", function () {
        //span 태그들 중에서 class 태그 속성이 disabled 가 아닌 요소들 찾기
        let pageNo = 1;
        if ($(this).hasClass("prev")) {
        pageNo = parseInt($(this).next().html()) - 1; // 문자 타입을 숫자타입으로 변환해야 NEXT 클릭 시 3페이지 노출
        // parseInt 해 주지 않으면 next 페이지 21로 노출됨
        } else if ($(this).hasClass("next")) {
        pageNo = parseInt($(this).prev().html()) + 1; //prev()까지는 객체 .html()까지는 객체의 내용
        } else {
        pageNo = parseInt($(this).html());
        }
        // alert("보려는 페이지번호: " + pageNo);
        let word = $("div.search>div.search__box>input.search__input").val().trim(); // 문자열 공백 제거 -> trim
        console.log(pageNo);
        console.log(word);
        let url = "";
        let data = "";
        if (word == "") {
        url = "http://localhost:1128/noticeboard/notice/list/" + pageNo;
        // data = "currentPage=" + pageNo;
        } else {
        // console.log("--" + word);
        word = decodeURI(word);
        url = "http://localhost:1128/noticeboard/notice/board/search/" + word+ "/" + pageNo;
        // url = encodeURI(url);
        console.log(url);
        // url = decodeURI(url);
        // data = "currentPage=" + pageNo + "&word=" + word;
        }
        showList(url);
        return false;
    });
    // --페이지 그룹의 페이지를 클릭 END

    $.ajax({
        url: "http://localhost:1124/back/user/loginstatus",
        method: "get",
        success: function (jsonObj) {
          let $tabObj = $("div#content>div#content-right");
          let $tabObjHtml = "";
          let loginedUserType = localStorage.getItem("loginedUserType");
    
          console.log(jsonObj);
          if (loginedUserType == 1 || loginedUserType == 0 ) {
            // $('header div#logined').show();
            $tabObjHtml += '<div id="logined"><div id="logout" onclick="logout()">로그아웃</div>';
            if (loginedUserType== 1) {
              $tabObjHtml +=
                '<div id="addlsn"><a id="mypage" href="../html/addlesson.html">레슨등록</a></div>';
            }
            $tabObjHtml +=
              '<div id="mypage" onclick="mypage()">마이페이지</div></div>';
          } else {
            // $('header div#normal').show();
            $tabObjHtml +=
              ' <div id="normal"><a href="../html/login.html">로그인</a>';
            $tabObjHtml +=
              '<a href="../html/signuptype.html">회원가입</a></div>';
          }
          $tabObj.html($tabObjHtml);
    
          // return false;
        },
        error: function (jqXHR) {
          alert(jqXHR.status);
        },
      }); 
}); // 맨 위의 funcion
