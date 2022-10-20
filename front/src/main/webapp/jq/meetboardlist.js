$(function () {

  function showList(url, data) {//목록얻기
    $.ajax({
      url: url,
      method: 'get',
      data: data,
      success: function (jsonObj) {
        if (jsonObj.status == 1) {
          let pageBeanObj = jsonObj.t;

          //게시글 div를 원본으로 한다. 복제본만든다
          let $board = $('li.board-container').first();
          $board.show();
          //나머지 게시글 div는 삭제한다
          $('li.board-container').not($board).remove();

          let $boardParent = $board.parent();
          $(pageBeanObj.list).each(function (index, board) {
            let $boardCopy = $board.clone();//복제본

            $boardCopy.find("span.board-ctg").html(board.meetCategory.meetCtgTitle);
            $boardCopy.find("span.board-nickname").html(board.userNickname);
            $boardCopy.find("h3.board-title").html(board.meetBoardTitle);
            $boardCopy.find("span.board-dt").html(board.meetBoardDt);
            $boardCopy.find("span.board-meet_dt").html(board.meetBoardMeetDt);
            let meetCnt = board.meetBaordCurCnt + '/' + board.meetBoardMaxCnt + '명';
            $boardCopy.find("span.board-cnt").html(meetCnt);
            $boardCopy.find("span.board-location").html(board.meetBoardLocation);
            $boardCopy.find("span.board-view_cnt").html(board.meetBoardViewCnt);

            //상세보기 링크 걸기
            $boardCopy.find("a.board-link").attr("href", '../html/meetboarddetail.html?meet_board_no=' + board.meetBoardNo);

            //글내용 summernote로 적용된 html태그 삭제
            let meetBoardContent = board.meetBoardContent.replace(/(<([^>]+)>)/ig, "");
            //글내용이 30자 이상이면 30자까지만 노출 후 말줄임표 처리
            if (meetBoardContent.length >= 30) {
              meetBoardContent = meetBoardContent.substr(0, 30) + '…';
            }
            $boardCopy.find("p.board__body-content").html(meetBoardContent);

            //모집유형은 숫자값(0,1)므로 한글로 변경하고 각 색상 넣기 
            if (board.meetBoardStatus == 0) {
              $boardCopy.find('span.board-status').html("모집중");
              $boardCopy.find('span.board-status').css('background-color', '#92B23B').css('color', 'white');

            } else {
              $boardCopy.find("span.board-status").html("모집완료");
              $boardCopy.find('span.board-status').css('background-color', '#a9a9a9').css('color', 'white');
            }
            $boardParent.append($boardCopy);
          });
          $board.hide();

          //---------페이징처리 START---------
          let $pagegroup = $('div.pagegroup')
          let $pagegroupHtml = '';
          if (pageBeanObj.startPage > 1) {
            $pagegroupHtml += '<span class="prev">PREV</span>';
          }
          for (let i = pageBeanObj.startPage; i <= pageBeanObj.endPage; i++) {
            $pagegroupHtml += '&nbsp;&nbsp;';
            if (pageBeanObj.currentPage == i) { //현재페이지인 경우 <span>태그 안만듦
              // $pagegroupHtml += i;
              $pagegroupHtml += '<span class="disabled">' + i + '</span>';//css에서 not함수를 사용하여 구분
            } else {
              $pagegroupHtml += '<span>' + i + '</span>';
            }
          }
          if (pageBeanObj.endPage < pageBeanObj.totalPage) {
            $pagegroupHtml += '&nbsp;&nbsp;';
            $pagegroupHtml += '<span class="next">NEXT</span>';
          }

          $pagegroup.html($pagegroupHtml);
          //---------페이징처리 END---------
        } else {
          // alert(jsonObj.msg);
        }
      },
      error: function (jqXHR) {
        // alert("에러:" + jqXHR.responseText);
      }
    });
    return false;
  }

  //---페이지 로드되자 마자 게시글1페이지 검색 START---
  showList('http://localhost:1129/backmeet/meet/board/list');
  //---페이지 로드되자 마자 게시글1페이지 검색 END---

  //----모집상태별 필터 클릭 START----
  $("input[type='checkbox']").click(function () {
        
    //3개의 필터중 하나만 클릭 가능
    const checkboxes 
    = document.getElementsByName("search__filter-status");
    checkboxes.forEach((cb) => {
      cb.checked = false;
    })
    this.checked = true;

    //필터별 모집상태 값 가져오기
    var status = $("input[name='search__filter-status']:checked").val();
    let url = '';
    let data = '';

    if (status == "-1") {//전체를 클릭했을 때
      url = 'http://localhost:1129/backmeet/meet/board/list';
      data = "";
    } else {
      url = 'http://localhost:1129/backmeet/meet/board/filter/' + status;
      data = "meetBoardStatus" + status;
    }

    showList(url, data);

  });
  //----모집상태별 필터 클릭 END----

  //---페이지 그룹의 페이지를 클릭 START---
  $('main>section>div.pagegroup').on('click', 'span:not(.disabled)', function () {//아래 if조건 대신 not 선택자 사용 
    let pageNo = 1;
    if ($(this).hasClass('prev')) {
      pageNo = parseInt($(this).next().html()) - 1;
    } else if ($(this).hasClass('next')) {
      pageNo = parseInt($(this).prev().html()) + 1;
    } else {
      pageNo = parseInt($(this).html());
    }

    let word = $('input[name=word]').val().trim();
    let url = '';
    let data = '';
    var status = $("input[name='search__filter-status']:checked").val();//필터 모집상태값 가져오기

    if ( status == '-1' && word == '') {//필터가 전체로 선택되어 있으며 검색어가 없는 경우
      url = 'http://localhost:1129/backmeet/meet/board/list/' + pageNo;
      data = 'currentPage=' + pageNo;
    } else if (word.length > 0) {//검색어가 있는 경우
      url = 'http://localhost:1129/backmeet/meet/board/search/' + word + '/' + pageNo;
      data = 'word=' + word + '&currentPage=' + pageNo;
    } else {//모집상태 필터가 모집중,모집완료로 선택된 경우
      url = 'http://localhost:1129/backmeet/meet/board/filter/' + status + '/' + pageNo;
      data = 'meetBoardStatus=' + status + '&currentPage=' + pageNo;
    }

    showList(url, data);
  });
  //---페이지 그룹의 페이지를 클릭 END---

  //---검색 클릭 START---
  $('button[name=board-search]').click(function () {
    let word = $('input[name=word]').val().trim();
    let url = 'http://localhost:1129/backmeet/meet/board/search/' + word;
    let data = 'word=' + word;

    // 검색 클릭 시 필터가 전체로 체크되어야함(추가예정)
    showList(url, data);
    return false;
  });
  //---검색 클릭 END--- 

  //----글쓰기 버튼 클릭 START----
  $('button[name=write-button]').click(function () {
    $(location).attr('href', '../html/meetboardwrite.html');
  })
  //----글쓰기 버튼 클릭 END----

});

