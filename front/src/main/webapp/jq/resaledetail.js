    $(function () {
    // 로딩되자마자 글 불러오기
    let queryString = location.search.split("=")[1].split("%")[0];
    // console.log("쿼리스트링값 " + queryString);

    let src = "../resale_images/";
    let resaleBoardNo = "";
    let userNickname = "";
    // let likeNickname = "";
    let likeNo = "";
    let likedNickname = "";
    let commentNickname = "";
    let commentNo = 0;
    let commentParentNo = 0;
    let cmtParentNo = 0;
    let loginedNickname = localStorage.getItem("loginedNickname");
    $.ajax({
        url: "http://localhost:1126/backresale/resale/board/" + queryString,
        method: "get",
        async: false,
        success: function (jsonObj) {
        if (jsonObj.status == 1) {
            // 여기까지 OK
            let detailObj = jsonObj.t.resaleBoard;
            console.log("좋아요 수 : " + jsonObj.t.resaleBoard.resaleBoardLikeCnt);
            console.log("게시판 번호 : " + jsonObj.t.resaleBoard.resaleBoardNo);

            resaleBoardNo = detailObj.resaleBoardNo;
            userNickname = detailObj.userNickname;

            let fileNameArr = jsonObj.t.imageFileNames;
            // let dto = jsonObj.t.resaleBoard;
            console.log("파일명 : " + fileNameArr);
            console.log("----");
            // console.log(dto);
            console.log("저장된 파일 개수는 : " + fileNameArr.length);

            let insertHtml = "";
            let $parent = $("div.board__content__images");
            for (let i = 0; i < fileNameArr.length; i++) {
            insertHtml += "<img src=''";
            // insertHtml += src + detailObj.resaleBoardNo + "/" + fileNameArr[i];
            insertHtml += " alt='' width='30%;' height=' 30%;'/>";
            insertHtml += "&nbsp;&nbsp";
            //-----------------
            }
            $parent.append(insertHtml);

            $("div.board__content__title").html(detailObj.resaleBoardTitle);
            $("div.board__content__nickname").html(detailObj.userNickname);
            $("div.board__content__date").html(detailObj.resaleBoardDt);
            $("div.board__content__view_cnt").html(detailObj.resaleBoardViewCnt);
            $("div.board__content__cmt_cnt").html(detailObj.resaleBoardCmtCnt);
            $("div.board__content__like_cnt").html(detailObj.resaleBoardLikeCnt);
            $("div.board__content").html(detailObj.resaleBoardContent);
            //-------------------------------------
            // 댓글 보여주기
            let commentObj = jsonObj.t.resaleBoard.resaleComment;
            // let
            console.log("--댓글보여주기 commentObj ----");
            console.log(commentObj);
            console.log("-------------------------------");
            let $commnetParent = $("div.comment-list");
            // 원본 하나 선택 후 나머지 댓글의 div 삭제
            let $comment = $("div.comment-content").first();
            $("div.comment-content").not($comment).remove();

            $(commentObj).each(function (i, comment) {
            let $commentCopy = $comment.clone();
            console.log(comment);
            $commentCopy
                .find("div.comment-list__nickname")
                .html(comment.userNickname);
            $commentCopy.find("div.comment-list__date").html(comment.resaleCmtDt);
            $commentCopy
                .find("div.comment-list__content")
                .html(comment.resaleCmtContent);
            $commentCopy.find("div.comment-list__no").html(comment.resaleCmtNo);
            $commentCopy
                .find("div.comment-list__parentno")
                .html(comment.resaleCmtParentNo);

            $commnetParent.append($commentCopy);

            commentNickname = comment.userNickname;
            cmtParentNo = comment.resaleCmtParentNo;

            if (cmtParentNo == 0) {
                $commentCopy.find("div.recomment-write").show();
            } else {
                $commentCopy.find("div.recomment-write").hide();
            }

            // console.log("댓글 작성한 사람들 : " + commentNickname);
            // console.log("부모글번호는 ? " + cmtParentNo);
            if (commentNickname == loginedNickname) {
                // 댓글 작성자 중 로그인한 닉네임과 일치하는 것이 있으면
                commentNo = comment.resaleCmtNo; // 일치한 댓글 번호
                commentParentNo = comment.resaleCmtParentNo; // 부모댓글번호를 가지고 옴
                commentNickname = loginedNickname;
                // console.log("댓글 번호 : " + commentNo);
                $commentCopy.find("div.comment-content-function").show();
            } else {
                $commentCopy.find("div.comment-content-function").hide();
            }
            }); // each
            $comment.remove();

            // 좋아요 누른 사람들 목록
            let likeObj = jsonObj.t.resaleBoard.resaleLike;
            // console.log(likeObj);

            $(likeObj).each(function (i, like) {
            likedNickname = like.userNickname;
            console.log("좋아요 한 사람들 : " + likedNickname);
            if (likedNickname == loginedNickname) {
                //localStorage.getItem("loginedNickname")
                // 세션 아이디와 좋아요 한 닉네임이 같으면
                likeNo = like.resaleLikeNo;
                likedNickname = loginedNickname;
                // console.log("좋아요 한 사람 & 로그인 된 사람" + likedNickname);
                // console.log("좋아요 번호 : " + likeNo);
            } // each 의 if문
            // userNickname = detailObj.userNickname;
            });

            //-----------------------------
            let $imgs = $("div.board__content__images>img");
            for (let i = 0; i < fileNameArr.length; i++) {
            $.ajax({
                url: "http://localhost:1126/backresale/resale/downloadimage/detail",
                data: { fileName: fileNameArr[i], resaleBoardNo: resaleBoardNo },
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
    // --------- 상세 조회 END ---------

    // 댓글 작성(완성)
    if (loginedNickname == null) {
        $("div.comment-write").hide();
    } else {
        $("div.comment-write").show();
    }

    // --------- 댓글 작성 START --------
    $("div.comment-write>button").on("click", function () {
        let cmtContent = $("div.comment-write>input").val();
        let cmtNickname = localStorage.getItem("loginedNickname");
        // alert(cmtNickname);

        let obj = {
        resaleCmtContent: cmtContent,
        userNickname: cmtNickname,
        resaleBoard: { resaleBoardNo: resaleBoardNo },
        };
        $.ajax({
        url: "http://localhost:1126/backresale/resale/comment/write",
        method: "post",
        contentType: "application/json",
        data: JSON.stringify(obj), // userNickname 받아와야함
        success: function (jsonObj) {
            if (jsonObj.status == 1) {
            // console.log(obj);
            alert(jsonObj.msg);
            location.reload();
            }
        },
        error: function (jsonObj) {
            alert(jsonObj.msg);
            location.reload();
        },
        });

        return false;
    });
    // --------- 댓글 작성 END ---------

    // // --------- 대댓글 작성 START --------
    // console.log("부모댓글번호 : "+ cmtParentNo);
    // $("div.recomment-write>button").on("click", function () {
    //     let recmtContent = $("div.recomment-write>input").val();
    //     let recmtNickname = loginedNickname;
    //     // alert(cmtNickname);
    //     let obj = {
    //         resaleCmtContent: recmtContent,
    //         userNickname: recmtNickname,
    //         resaleCmtParentNo : commentParentNo,
    //         resaleBoard: { resaleBoardNo: resaleBoardNo },
    //     };
    //     $.ajax({
    //         url: "http://localhost:1126/backresale/resale/comment/write",
    //         method: "post",
    //         contentType: "application/json",
    //         data: JSON.stringify(obj), // userNickname 받아와야함
    //         success: function (jsonObj) {
    //             if (jsonObj.status == 1) {
    //                 console.log(obj);
    //                 alert(jsonObj.msg);
    //                 location.reload();
    //             }
    //         },
    //         error: function (jsonObj) {
    //             alert(jsonObj.msg);
    //             location.reload();
    //         },
    //     });
    //     return false;
    // });
    // // --------- 대댓글 작성 END --------

    //-------- 댓글 수정 START ---------

    $("div.comment-list").on(
        "click",
        "div.comment-content>div.comment-content-function> button.bt__cmt-modify",
        function () {
        commentNo = $(this).parent().parent().find("div.comment-list__no").text();
        console.log(commentNo + "댓글");
        // commentNo = commentNo.split("-")[1].trim();
        // console.log("수정댓글번호:" + commentNo1);
        console.log("댓글작성자" + commentNickname);
        // if(loginedNickname == commentNickname){
        let url = "http://localhost:1126/backresale/resale/comment/" + commentNo;
        // console.log(url);
        let cmtContent = $(this).parent().find("input").val();
        // console.log(cmtContent);
        let obj = {
            resaleCmtNo: commentNo,
            resaleCmtContent: cmtContent,
            userNickname: loginedNickname,
            resaleBoard: { resaleBoardNo: resaleBoardNo },
        };
        $.ajax({
            url: url,
            method: "put",
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(obj),
            success: function (jsonObj) {
            alert(jsonObj.msg);
            location.reload();
            },
            error: function (jsonObj) {
            alert(jsonObj.msg);
            },
        });
        // }else{ // if문
        // alert("댓글 작성자가 아닙니다.");
        // }
        return false;
        }
    );
    //-------- 댓글 수정 END ---------

    //-------- 댓글 삭제 START ---------
    console.log(commentNickname);
    // if (commentNickname == loginedNickname) {
    //     $("button.bt__cmt-delete").show();
    // } else {
    //     $("button.bt__cmt-delete").hide();
    // }
    //버튼 클릭 시 댓글 삭제
    $("div.comment-list").on(
        "click",
        "div.comment-content>div.comment-content-function> button.bt__cmt-delete",
        function () {
        commentNo = $(this).parent().parent().find("div.comment-list__no").text();
        // slice(commentNo)
        // commentNo = commentNo.split("-")[1].trim();
        console.log("삭제 할 댓글번호 : " + commentNo);
        console.log("삭제 할 부모댓글번호 : " + commentParentNo);
        console.log("삭제 할 닉네임 : " + commentNickname);
        console.log("원글번호" + resaleBoardNo);
        if (loginedNickname == commentNickname) {
            let obj = {
            resaleCmtNo: commentNo,
            resaleCmtParentNo: commentParentNo,
            userNickname: commentNickname,
            resaleBoard: { resaleBoardNo: resaleBoardNo },
            };
            $.ajax({
            url: "http://localhost:1126/backresale/resale/comment/" + commentNo,
            method: "delete",
            contentType: "application/json",
            data: JSON.stringify(obj),
            success: function () {
                alert("댓글 삭제 성공");
                location.reload();
            },
            error: function (jqXHR) {
                alert("error : " + jqXHR.status + " : " + "댓글 삭제 실패");
                location.reload();
            },
            }); //ajax
        } else {
            alert("댓글 작성자가 아닙니다");
        }
        }
    );
    //-------- 댓글 삭제 END ---------

    //--------  게시글 삭제 START --------
    // 게시글 삭제(완성)
    if (userNickname == loginedNickname) {
        $("button.bt__board-delete").show();
    } else {
        $("button.bt__board-delete").hide();
    }
    //삭제 버튼 클릭 시
    $("button.bt__board-delete").on("click", function () {
        console.log("삭제할 글번호 : " + resaleBoardNo);
        console.log("닉네임은 : " + userNickname);

        let obj = { resaleBoardNo: resaleBoardNo, userNickname: userNickname };
        $.ajax({
        url: "http://localhost:1126/backresale/resale/board/" + resaleBoardNo,
        method: "delete",
        contentType: "application/json",
        data: JSON.stringify(obj),
        success: function () {
            alert("게시글 삭제 성공");
            location.href = "../html/resaleboardlist.html";
        },
        error: function (jqXHR) {
            alert("error : " + jqXHR.status + " : " + "게시글 삭제 실패");
        },
        });
    });
    //--------  게시글 삭제 END --------

    // -------- 게시글 수정 START --------
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
        location.href =
        "../html/resaleboardwrite.html?resaleBoardNo=" + resaleBoardNo;
    });
    // -------- 게시글 삭제 END --------

    // -------- 좋아요 추가, 삭제 START ---------
    $("div.board-like").on("click", function () {
        // console.log("보드 넘버는" + resaleBoardNo);
        // 좋아요 여부
        console.log("좋아요한 닉넴" + likedNickname);
        // console.log("좋아요 번호 " + likeNo);
        if (likedNickname == loginedNickname) {
        // localStorage.getItem("loginedNickname")
        // 세션 아이디와 좋아요 한 닉네임이 같으면
        // 좋아요 삭제
        let obj = {
            resaleLikeNo: likeNo,
            userNickname: likedNickname,
            resaleBoard: { resaleBoardNo: resaleBoardNo },
        };
        $.ajax({
            url: "http://localhost:1126/backresale/resale/like/" + likeNo,
            method: "delete",
            contentType: "application/json",
            data: JSON.stringify(obj),
            success: function (jsonObj) {
            if (jsonObj.status == 1) {
                alert(jsonObj.msg);
                location.reload();
            }
            },
            error: function (jqXHR) {
            alert(jqXHR.status + ":" + "좋아요 삭제 실패");
            location.reload();
            },
        });
        } else {
        // 세션 아이디와 좋아요한 닉네임이 같지 않으면
        console.log("rmfqjsgh" + resaleBoardNo);
        let obj = {
            resaleBoard: {
            resaleBoardNo: resaleBoardNo,
            },
            userNickname: loginedNickname,
        };
        //좋아요 클릭
        $.ajax({
            url: "http://localhost:1126/backresale/resale/like/add",
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
        } //else 끝
    }); //클릭 끝
    // -------- 좋아요 추가, 삭제 END ---------
    }); // 첫 function
