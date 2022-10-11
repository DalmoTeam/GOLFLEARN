    $(function () {
    //로딩 되자마자 글 목록 불러오기(1 페이지)
    // let url = "http://localhost:1126/backresale/resale/board/list/1";
    let queryString = location.search.split("=")[1];
    console.log(queryString);
    let url = "http://localhost:1128/noticeboard/notice/" + queryString;
    let src = "../notice_image/";
    let boardNo = 0;
    let likeNo = 0;
    let userNickname = "";
    let cmtNickname = "";
    let cmtParentNo = 0;
    let likedNickname = "";
    let loginedNickname = localStorage.getItem("loginedNickname");
    $.ajax({
        url: url,
        method: "GET",
        // async: false,
        success: function (jsonObj) {
        // console.log("----" + jsonObj.t.noticeBoardLikeCnt);
        if (jsonObj.status == 1) {
            userNickname = jsonObj.t.noticeBoard.userNickname;
            // console.log("user=" + userNickname);
            boardNo = jsonObj.t.noticeBoard.noticeBoardNo;

            let fileNameArr = jsonObj.t.imageFileNames;
            // let dto = jsonObj.t.resaleBoard;
            console.log("파일명 : " + fileNameArr);
            console.log("----");
            // console.log(dto);
            console.log("저장된 파일 개수는 : " + fileNameArr.length);

            let insertHtml = "";
            let $parent = $("div.board__content__thumbnail");
            for (let i = 0; i < fileNameArr.length; i++) {
            insertHtml += "<img src=''";
            // insertHtml += src + detailObj.resaleBoardNo + "/" + fileNameArr[i];
            // insertHtml += " alt='' width='30%;' height=' 30%;'/>";
            // insertHtml += "&nbsp;&nbsp";
            //-----------------
            }
            $parent.append(insertHtml);


            $("div.board__content__title").html(
            "제목 : " + jsonObj.t.noticeBoard.noticeBoardTitle
            );
            $("div.board__content__date").html(jsonObj.t.noticeBoard.noticeBoardDt);
            $("div.board__content__view_cnt").html(jsonObj.t.noticeBoard.noticeBoardViewCnt);
            $("div.board__content__like_cnt").html(jsonObj.t.noticeBoard.noticeBoardLikeCnt);
            // $("div.board__content__thumbnail>img").attr(
            // "src",
            // src  + boardNo +  "/" + boardNo + "_image_" + "s_1" + ".PNG"
            // // src + boardNo + "_image_" + "pyeonan.png"
            // );
            $("div.board__content").html(jsonObj.t.noticeBoard.noticeBoardContent);

            let commentList = jsonObj.t.noticeBoard.noticeCommentList;
            let $commentParent = $("div.comment-list");

            let $comment = $("div.comment-content").first();
            $("div.comment-content").not($comment).remove();

            $(commentList).each(function (index, comment) {
            // console.log(comment);
            let $commentCopy = $comment.clone();
            $commentCopy
                .find("div.comment-list__cmtNo")
                .html(comment.noticeCmtNo);
            $commentCopy
                .find("div.comment-list__nickname")
                .html(comment.userNickname);
            $commentCopy
                .find("div.comment-list__content")
                .html(comment.noticeCmtContent);
            $commentCopy.find("div.comment-list__date").html(comment.noticeCmtDt);

            let cmt =
                "<div class='comment-list__content__modify'><input type='text'><button name='cmtModify'>수정</button><button name='cmtDelete'>댓글삭제</button></div>";

            // $commentCopy.append("< class='comment-list__content__modify'>");
            // $commentCopy.append("<input type='text'>");
            // $commentCopy.append("<button name='cmtModify'>수정</button>");
            // $commentCopy.append("<button name='cmtDelete'>댓글삭제</button>");
            cmtNickname = comment.userNickname;
            // cmtParentNo = comment.cmtParentNo;

            // if(cmtParentNo == 0 ){
            //     $commentCopy.find("div.recomment-write").show();
            // }else{
            //     $commentCopy.find("div.recomment-write").hide();
            // }
            $commentCopy.append(cmt);
            $commentParent.append($commentCopy);

            // 댓글 작성자에게만 댓글 수정및 삭제 show()
            // console.log(cmtNickname);
            if (cmtNickname == loginedNickname) {
                commentNo = comment.resaleCmtNo;
                commentParentNo = comment.resaleCmtParentNo;
                // console.log("댓글 번호 : " + commentNo);
                cmtNickname = loginedNickname;
                $commentCopy.find("div.comment-list__content__modify").show();
            } else {
                $commentCopy.find("div.comment-list__content__modify").hide();
            }
            console.log(cmtNickname+"//");
            console.log(loginedNickname+"//");
            });
            
            $comment.remove();

            // 좋아요 누른 사람들 목록
            let likeObj = jsonObj.t.noticeBoard.noticeLikeList;
            // console.log(likeObj);

            $.each(likeObj, function (i, like) {
                console.log("------" + like.userNickname);
                likedNickname = like.userNickname;
                if (likedNickname == localStorage.getItem("loginedNickname")) {
                    // 세션 아이디와 좋아요 한 닉네임이 같으면
                    likeNo = like.noticeLikeNo;
                    likedNickname = loginedNickname;
                } // each 의 if문
            // userNickname = detailObj.userNickname;
            });
            // boardNo = boardNo;

            // $("div.comment-list").html(jsonObj.t.noticeCommentList);
        
        //-----------------------------
            let $imgs = $("div.board__content__thumbnail>img");
            for (let i = 0; i < fileNameArr.length; i++) {
            $.ajax({
                url: "http://localhost:1128/noticeboard/notice/downloadimage/detail",
                data: { fileName: fileNameArr[i], boardNo: boardNo },
                method: "get",
                // credentials:true,
                cache: false,
                xhrFields: {
                responseType: "blob", //이미지 다운로드 문법
                // withCredentials: true,
                },
                success: function (responseData) {
                // 받아온 이미지들 객체를 넣어줌
                let url = URL.createObjectURL(responseData);
                //body > main > article > div > div.board-container > div.board__content__images > img:nth-child(1)
                //"div.board__content__images
                console.log("----------");
                console.log($imgs); //[i]);
                $($imgs[i]).attr("src", url);
                },
            });
            } //end for
        } // if문
        }, // success
        error: function (jsonObj) {
        alert(jsonObj.msg);
        }, // error
    }); // ajax
    // return false;

     //--------  게시글 삭제 START -------- 
    // 게시글 삭제(완성)
    userNickname = loginedNickname;
    if (userNickname == loginedNickname) {
        $("button.bt__board-delete").show();
    } else {
        $("button.bt__board-delete").hide();
    }
    //--------  게시글 삭제 END -------- 

    if (userNickname == loginedNickname) {
        $("button.bt__board-delete").show();
    } else {
        $("button.bt__board-delete").hide();
    }
    //삭제 버튼 클릭 시
    $("button.bt__board-delete").on("click", function () {
        console.log("삭제할 글번호 : " + boardNo);
        console.log("닉네임은 : " + userNickname);

        let obj = { noticeBoardNo: boardNo, userNickname: userNickname };
        $.ajax({
        url: "http://localhost:1128/noticeboard/notice/" + boardNo,
        method: "delete",
        contentType: "application/json",
        data: JSON.stringify(obj),
        success: function () {
            alert("게시글 삭제 성공");
            location.href="../html/noticeboardlist.html";
        },
        error: function (jqXHR) {
            alert("error : " + jqXHR.status + " : " + "게시글 삭제 실패");
        },
        });
    });

    // 게시글 수정
    if (userNickname == loginedNickname) {
        $("button.bt__board-modify").show();
    } else {
        $("button.bt__board-modify").hide();
    }
    $("button.bt__board-modify").on("click", function () {
        // alert("수정");
        // console.log("게시글번호는" + boardNo);
        let nickName = userNickname;
        // console.log("작성자 닉네임은" + nickName);
        // alert("작성자 닉네임은" + nickName);

        // alert("게시글번호는" + resaleBoardNo);
        location.href = "../html/noticeboardwrite.html?noticeBoardNo=" + boardNo;
    });

    //좋아요
    $("div.board-like").on("click", function () {
        // let url = "http://localhost:1128/noticeboard/notice/like/add";
        // likedNickname = loginedNickname;
        console.log(boardNo);
        console.log("좋아요한 닉넴" + likedNickname);
        console.log(localStorage.getItem("loginedNickname"));
        console.log(likeNo);

        if (likedNickname == loginedNickname) {
        // localStorage.getItem("loginedNickname")
        // 세션 아이디와 좋아요 한 닉네임이 같으면
        // 좋아요 삭제
            console.log("나맞아")
            let obj = {
                noticeLikeNo: likeNo,
                userNickname: likedNickname,
                noticeBoardDto: { noticeBoardNo: boardNo }
            };

            $.ajax({
                url: "http://localhost:1128/noticeboard/notice/like/"+likeNo,
                method: "delete",
                contentType: "application/json",
                data: JSON.stringify(obj), // userNickname 받아와야함
                success: function (jsonObj) {
                    if (jsonObj.status == 1) {
                        alert(jsonObj.msg);
                        location.reload();
                    }
                },
                error: function (jqXHR) {
                    alert("error : " + jqXHR.status + " : " + "좋아요 삭제 실패");
                    location.reload();
            },
        });
        } else {
            let obj = { noticeBoardDto :{noticeBoardNo: boardNo},
                        userNickname: loginedNickname
                    };
            //좋아요 클릭
            $.ajax({
                url: "http://localhost:1128/noticeboard/notice/like/add",
                method: "post",
                contentType: "application/json",
                data: JSON.stringify(obj),
                success: function (jsonObj) {
                    if ((jsonObj.status = 1)) {
                        alert("좋아요 추가 성공");
                        location.reload();

                    }
                },
                error: function (jqXHR) {
                    alert(jqXHR.status + ":" + "좋아요 추가 실패");
                    location.reload();
                }, //error
            }); // ajax
        }
        return false;
    });

    //댓글 작성
    $("div.comment-write>button").on("click", function () {
        let url = "http://localhost:1128/noticeboard/notice/comment/write";

        let cmt = $("div.comment-write>input").val();

        let data = {
        userNickname: loginedNickname,
        noticeCmtContent: cmt,
        noticeBoardDto: { noticeBoardNo: boardNo },
        noticeCmtParentNo: 0,
        };

        $.ajax({
        url: url,
        method: "post",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(data), // userNickname 받아와야함
        success: function (jsonObj) {
            console.log(data);
            location.reload();
        },
        error: function (jqXHR) {
            alert("error : " + jqXHR.status + " : " + "댓글작성 실패");
        },
        });
        return false;
    });

    //댓글 수정
    // $("button.cmtModify").on("click",
    $("div.comment-list").on(
        "click",
        "div.comment-content>div.comment-list__content__modify> button[name='cmtModify']",
        function () {
        if (cmtNickname == loginedNickname) {
            let cmtNo = $(this)
            .parent()
            .parent()
            .find("div.comment-list__cmtNo")
            .html();

            let url = "http://localhost:1128/noticeboard/notice/comment/" + cmtNo;
            console.log(url);
            let cmtContent = $(this).parent().find("input").val();
            let data = {
            noticeCmtNo: cmtNo,
            noticeCmtContent: cmtContent,
            userNickname: "id1",
            noticeBoardDto: { noticeBoardNo: boardNo },
            };

            $.ajax({
            url: url,
            method: "PUT",
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(data),
            success: function (jsonObj) {
                location.reload();
            },
            error: function (jqXHR) {
                alert("error : " + jqXHR.status + " : " + "댓글 수정  실패");
            },
            });
        } else {
            alert("댓글 작성자가 아닙니다.");
        }
        return false;
        }
    );

    // 댓글삭제
    // list 안에 content여러개 생성됨, 생성이후에 each 로 클릭한 댓글번호를 가져와야 함.
    $("div.comment-list").on(
        "click",
        "div.comment-content>div.comment-list__content__modify> button[name='cmtDelete']",
        function () {
        if (cmtNickname == loginedNickname) {
            let cmtNo = $(this)
            .parent()
            .parent()
            .find("div.comment-list__cmtNo")
            .html();
            let url = "http://localhost:1128/noticeboard/notice/comment/" + cmtNo;

            $.ajax({
            url: url,
            method: "DELETE",
            success: function (jsonObj) {
                location.reload();
            },
            error: function (jqXHR) {
                alert("error : " + jqXHR.status + " : " + "댓글 삭제  실패");
            },
            });
        } else {
            alert("댓글작성자가 아닙니다.");
        }
        }
    );

    $.ajax({
      url: "http://localhost:1124/back/user/loginstatus",
      method: "get",
      success: function (jsonObj) {
        let $tabObj = $("div#content>div#content-right");
        let $tabObjHtml = "";
        let loginedUserType = localStorage.getItem("loginedUserType");

        // document.write('<script src="../jq/loginStatus.js"></script>');

        console.log(jsonObj);
        if (loginedUserType == 1) {
          // $('header div#logined').show();
          $tabObjHtml +=
            '<div id="logined"><div id="logout" onclick="logout()">로그아웃</div>';
          $tabObjHtml +=
            '<div id="addlsn"><a id="mypage" href="../html/addlesson.html">레슨등록</a></div>';
          $tabObjHtml +=
            '<div id="mypage" onclick="mypage()">마이페이지</div></div>';
        } else if (loginedUserType == 0) {
          $tabObjHtml +=
            '<div id="logined"><div id="logout" onclick="logout()">로그아웃</div>';
          // $tabObjHtml += '<div id="addlsn"><a id="mypage" href="../html/addlesson.html">레슨등록</a></div>';
        } else {
          // $('header div#normal').show();
          $tabObjHtml +=
            ' <div id="normal"><a href="../html/login.html">로그인</a>';
          $tabObjHtml += '<a href="../html/signuptype.html">회원가입</a></div>';
        }
        $tabObj.html($tabObjHtml);

        // return false;
      },
      error: function (jqXHR) {
        alert(jqXHR.status);
      },
    }); 
});
