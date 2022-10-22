$(function () {
    let queryString = location.search.split("=")[1];
    // let queryString = 1;
    let url = "http://localhost:1129/backmeet/meet/board/" + queryString;

    let loginedNickname = localStorage.getItem("loginedNickname");
    console.log(loginedNickname);

    $.ajax({
        url: url,
        method: "GET",
        success: function (jsonObj) {
            if (jsonObj.status == 1) {
                $("h1.board_title").html(jsonObj.t.meetBoardTitle);
                $("span.user-nickname").html(jsonObj.t.userNickname);

                //summernote태그 변환
                $("span.board__content-content").html(jsonObj.t.meetBoardContent.replace(/&amp;/g, "&").replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&quot;/g, '"').replace(/&#40;/g, '(').replace(/&#41;/g, ')').replace(/&#35;/g, '#'));

                $("span.sub-title__meetboard_dt").html(jsonObj.t.meetBoardDt);
                $("span.board__content-dt").html(jsonObj.t.meetBoardMeetDt);

                let meetCnt = jsonObj.t.meetBaordCurCnt + '/' + jsonObj.t.meetBoardMaxCnt + '명';
                $("span.board-cnt").html(meetCnt);
                $("span.board__content-location").html(jsonObj.t.meetBoardLocation);
                $("span.board__content-ctg").html(jsonObj.t.meetCategory.meetCtgTitle);

                //모집상태에 따른 버튼
                if (jsonObj.t.meetBoardStatus == 1) {//모집완료인 경우
                    $("span.board-status").html("모집완료");
                    $("button[name='btn-in']").hide();//참여하기, 나가기 버튼 숨기기
                    $("button[name='btn-out']").hide();
                    $('span.board-status').css('background-color', '#a9a9a9').css('color', 'white');
                } else {//모집중인 경우
                    $("span.board-status").html("모집중");
                    $('span.board-status').css('background-color', '#92B23B').css('color', 'white');
                }

                //작성자여부에 따른 버튼
                if (loginedNickname != jsonObj.t.userNickname) {//게시글 작성자가 아닌 경우
                    $("button[name='btn-close']").hide();//모집종료버튼
                    $("button[name='btn-modify']").hide();
                    $("button[name='btn-delete']").hide();
                } else {//작성자인 경우
                    $("button[name='btn-in']").hide();
                    $("button[name='btn-out']").hide();
                }
            }
        },
    });

    //----참여하기 버튼 클릭 START----
    $("button[name='btn-in']").click(function () {
        if (loginedNickname == null) {//로그인여부 확인
            alert("로그인이 필요합니다.");
            return false;
        } else if (!confirm('모임에 참여하시겠습니까?')) {
            return false;
        } else {
            let meetBoardNo = queryString;
            $.ajax({
                method: "POST",
                url: 'http://localhost:1129/backmeet/meet/board/add/' + meetBoardNo,
                data: { loginedNickname },
                success: function () {
                    alert("모임에 참여하셨습니다");
                    location.href = '';
                },
                error: function (jqXHR) {
                    alert(jqXHR.responseText);
                }
            });
            return false;
        }
    });
    //----참여하기 버튼 클릭 END----

    //----나가기 버튼 클릭 START----
    $("button[name='btn-out']").click(function () {
        if (loginedNickname == null) {//로그인여부 확인
            alert("로그인이 필요합니다.");
            return false;
        }else if (!confirm('모임에서 나가시겠습니까?')) {
            return false;
        } else {
            let meetBoardNo = queryString;
            url = 'http://localhost:1129/backmeet/meet/board/leave/' + meetBoardNo;
            $.ajax({
                url: url,
                method: "DELETE",
                data: { loginedNickname },
                success: function () {
                    alert("나가기가 완료되었습니다");
                    location.href = '';
                },
                error: function (jqXHR) {
                    alert(jqXHR.responseText);
                }
            });
            return false;
        }
    });
    //----나가기 버튼 클릭 END----

    //----모집종료 버튼 클릭 START----
    $("button[name='btn-close']").click(function () {
        let meetBoardNo = queryString;
        url = 'http://localhost:1129/backmeet/meet/board/update/' + meetBoardNo;
        let data = {
            "meetBoardNo": meetBoardNo,
            "userNickname": loginedNickname,
            "meetBoardStatus": 2,
        }
        var jsonData = JSON.stringify(data);
        $.ajax({
            url: url,
            method: "PUT",
            data: jsonData,
            headers: {
                "content-Type": "application/json",
            },
            success: function () {
                alert("해당 모임이 모집종료되었습니다");
                location.href = '';
            },
            error: function (jqXHR) {
                alert(jqXHR.responseText);
            }
        });
        return false;
    });
    //----모집종료 버튼 클릭 END----

    //----수정버튼 클릭 START----
    $("button[name='btn-modify']").click(function () {
        let meetBoardNo = queryString;
        url = '../html/meetboardupdate.html?meet_board_no=' + meetBoardNo;
        if (!confirm('해당 모집글을 수정하시겠습니까?')) {
            return false;
        } else {
            window.location = url;
        }
        return false;
    });
    //----수정버튼 클릭 END----

    //---삭제버튼 클릭 START---
    $("button[name='btn-delete']").click(function () {
        if (!confirm('해당 모집글을 삭제하시겠습니까?')) {
            return false;
        } else {
            let meetBoardNo = queryString;
            url = 'http://localhost:1129/backmeet/meet/board/' + meetBoardNo;
            $.ajax({
                url: url,
                method: "DELETE",
                data: { loginedNickname },
                success: function () {
                    alert("삭제되었습니다.");
                    location.href = '../html/meetboardlist.html';
                },
                error: function (jqXHR) {
                    alert(jqXHR.responseText);
                }
            });
            return false;
        }
    });
    //---삭제버튼 클릭 END---

});