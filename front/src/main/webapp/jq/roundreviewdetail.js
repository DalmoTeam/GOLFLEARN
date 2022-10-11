$(function () {
    //localStorage에서 로그인된 아이디 가져오기 
    //- 로그인된 계정 잡는 방법 
    //semi -> back에서 session 이용 / final -> front에서 localstorage이용 / 추후 -> 토큰/쿠키 이용 인증 
    let user_nickname = localStorage.getItem("loginedNickname");
    // 테스트용 
    // let user_nickname = "데빌";

    //1) 상세내용 보여주기
    let currentPage = location.search;
    let board_no = location.search.substring(1).split("=")[1];
    let url = "http://localhost:1125/backroundreview/board/" + board_no;
    let data = "";
    let likedNickname = "";
    $.ajax({
        url : url,
        method : "get",
        data : data,
        success : function(jsonObj){
            if (jsonObj.status == 1){
                let roundReview = jsonObj.t.roundReviewBoard;
                //이미지 보여주기
                let fileNameArr = jsonObj.t.imageFileNames;
                let insertHtml = "";
                let $parent = $("div.images");
                for (let i = 0; i < fileNameArr.length; i++) {
                    insertHtml += "<img src=''";
                    insertHtml += " alt='' width='250px;' height=' 250px;'/>";
                    insertHtml += "&nbsp;&nbsp";
                }
                $parent.append(insertHtml);

                $("div.board__no").html("글번호&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp" + roundReview.roundReviewBoardNo)
                $("div.board__title").html("제목&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp" + roundReview.roundReviewBoardTitle);
                $("div.user__nickname").html("작성자&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp" + roundReview.userNickname);
                $("div.board__dt").html("날짜&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp" + roundReview.roundReviewBoardDt);
                $("div.board__view-cnt").html("조회수&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp" + roundReview.roundReviewBoardViewCnt);
                // $("img.board__images1").attr("src", "../roundreview_images/" + roundReview.roundReviewBoardNo + "/image_1.PNG");
                // $("img.board__images2").attr("src", "../roundreview_images/" + roundReview.roundReviewBoardNo + "/image_2.PNG");
                // $("img.board__images3").attr("src", "../roundreview_images/" + roundReview.roundReviewBoardNo + "/image_3.PNG");
                $("div.board__content").html(roundReview.roundReviewBoardContent);
                // $("div.board__map").html(roundReview.roundReviewBoardMap);
            
                //댓글 로드
                let cmtList = roundReview.roundReviewCommentList;

                let $comment = $("div.comment-list").first();
                $comment.show();
                    
                $("div.comment-list").not($comment).remove();
                let $commentParent = $comment.parent(); 
                $(cmtList).each(function(index, comment){
                    let $commentCopy = $comment.clone();
                    $commentCopy.find("div.comment-list__no").html(comment.roundReviewCmtNo);
                    $commentCopy.find("div.comment-list__nickname").html(comment.userNickname);
                    $commentCopy.find("div.comment-list__content").html(comment.roundReviewCmtContent);
                    $commentCopy.find("div.comment-list__date").html(comment.roundReviewCmtDt);

                    let commentName = comment.userNickname;

                    //유저의 닉네임과 댓글작성자가 다를 경우 수정버튼과 삭제버튼 숨기기 
                    if(user_nickname != commentName){
                        $("button.comment-list__modify").hide();
                        $("button.comment-list__remove").hide();
                    }
                    $commentParent.append($commentCopy);
                    $commentCopy.find("div.comment-list__no").html(comment.roundReviewCmtNo).hide();
                })
                $("div.comment-list").first().hide();
                
                // 좋아요 누른 사람들 목록
                let likeObj = jsonObj.t.roundReviewLikeList;

                $.each(likeObj, function (i, like) {
                    likedNickname = like.userNickname;
                    //접속된 아이디의 닉네임과 좋아요 한 닉네임이 같으면
                    if (likedNickname == user_nickname) {
                        likeNo = like.roundReviewLikeNo;
                    } 
                });
                //-----카카오맵-------------
                let latitude = roundReview.roundReviewBoardLatitude;
                let longitude = roundReview.roundReviewBoardLongitude;
                let mapContainer = document.getElementById('map'); //지도를 담을 영역의 DOM 레퍼런스
                let mapOption = { //지도를 생성할 때 필요한 기본 옵션
                    center: new kakao.maps.LatLng(latitude, longitude), //지도의 중심좌표.
                    level: 3 //지도의 레벨(확대, 축소 정도)
                };
                let map = new kakao.maps.Map(mapContainer, mapOption); //지도 생성 및 객체 리턴
                //마커 위치
                var markerPosition  = new kakao.maps.LatLng(latitude, longitude); 
                //마커 생성
                var marker = new kakao.maps.Marker({
                    position: markerPosition,
                    text : "장소"
                });
                marker.setMap(map);

                //마커에 장소 표시하기 
                let infowindow = new kakao.maps.InfoWindow({zIndex:1});
                kakao.maps.event.addListener(marker, 'click', function(mouseEvent){
                    let msg = "약속장소입니다"
                    //마커를 클릭하면 장소명이 인포윈도위에 노출 
                    infowindow.setContent('<div style="padding:5px; font-size:12px;">' + msg + '</div>');
                    infowindow.open(map, marker);
                });

                //주소얻기 
                // let geocoder = new kakao.maps.services.Geocoder();
                
                // searchAddrFromCoords(map.getCenter(), displayCenterInfo);
                // kakao.maps.event.addListener(map, 'click', function(mouseEvent){
                //     searchDetailAddrFromCoords(mouseEvent.latLng, function(result, status) {
                //         if (status === kakao.maps.services.Status.OK) {
                //             var detailAddr = !!result[0].road_address ? '<div>도로명주소 : ' + result[0].road_address.address_name + '</div>' : '';
                //             detailAddr += '<div>지번 주소 : ' + result[0].address.address_name + '</div>';
                            
                //             var content = '<div class="bAddr">' +
                //                             '<span class="title">법정동 주소정보</span>' + 
                //                             detailAddr + 
                //                         '</div>';
                
                //             // 마커를 클릭한 위치에 표시
                //             marker.setPosition(mouseEvent.latLng);
                //             marker.setMap(map);
                
                //             // 인포윈도우에 클릭한 위치에 대한 법정동 상세 주소정보를 표시
                //             infowindow.setContent(content);
                //             infowindow.open(map, marker);

                //         }
                //     });
                // });
                // kakao.maps.event.addListener(map, 'idle', function() {
                //     searchAddrFromCoords(map.getCenter(), displayCenterInfo);
                // });
                // function searchAddrFromCoords(coords, callback) {
                //     // 좌표로 행정동 주소 정보를 요청
                //     geocoder.coord2RegionCode(coords.getLng(), coords.getLat(), callback);         
                // }
                // function searchDetailAddrFromCoords(coords, callback) {
                //     // 좌표로 법정동 상세 주소 정보를 요청
                //     geocoder.coord2Address(coords.getLng(), coords.getLat(), callback);
                // }
                
                // // 지도 좌측상단에 지도 중심좌표에 대한 주소정보를 표출하는 함수
                // function displayCenterInfo(result, status) {
                //     if (status === kakao.maps.services.Status.OK) {
                //         var infoDiv = document.getElementById('centerAddr');
                
                //         for(var i = 0; i < result.length; i++) {
                //             // 행정동의 region_type 값은 'H' 이므로
                //             if (result[i].region_type === 'H') {
                //                 infoDiv.innerHTML = result[i].address_name;
                //                 break;
                //             }
                //         }
                //     }    
                // }
                let $imgs =  $('div.images>img');
                for (let i = 0; i < fileNameArr.length; i++) {
                    $.ajax({
                        url: "http://localhost:1125/backroundreview/downloadimage/detail",
                        data: {fileName : fileNameArr[i], roundReviewBoardNo : board_no},
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
                            $($imgs[i]).attr("src", url);
                        },
                    });
                }
            }
        },
        error : function(jqXHR){
            alert("에러:" + jqXHR.status);
        }
    });

    //1) 댓글 작성하기
    $("div.comment-box").on("click", "button.comment-box__send", function(){
        let roundReviewBoardNo = board_no;
        let roundReviewCmtContent = $(this).siblings("input[name=comment-box__write]").val();
        let roundReviewCmtParentNo = 0;

        let userNickname = localStorage.getItem("loginedNickname");
        // 테스트용
        // let userNickname = "데빌"; 
        $.ajax({
            url : "http://localhost:1125/backroundreview/comment/" + board_no,
            method : "post",
            timeout : 0,
            headers: {
                "Content-Type": "application/json"
            },
			data: JSON.stringify({
				roundReviewCmtContent: roundReviewCmtContent,
				roundReviewCmtParentNo: roundReviewCmtParentNo,
				userNickname: userNickname,
				roundReviewBoard: {
					roundReviewBoardNo: roundReviewBoardNo,
				},
			}),
			success: function () {
                //전달 성공하면 현재 페이지로 돌아가기 
				location.href = currentPage;
			},
			error: function (jqXHR) {
				alert("에러:" + jqXHR.status);
			}
        });
    return false;
    });
  //3. 댓글 수정하기
  // let $test = $("article");
  // let $test2 = $test.parents();
  // $test.on('click', "div.comment>div.comment-list>button.comment-list__modify", function(){
  //     let roundReviewCmtNo = $(this).siblings("div.comment-list__no").html();
  //     console.log(roundReviewCmtNo);
  //     $.ajax({
  //         url : "http://localhost:1125/backroundreview/comment/" + roundReviewCmtNo,
  //         method : 'put',
  //         timeout : 0,
  //         header : {
  //             "Content-Type": "application/json"
  //         },
  //         data : JSON.stringify({}),
  //         success : function(){
  //             alert("수정성공")
  //         },
  //         error : function (jqXHR) {
  //             alert(
  //                 "수정 에러: " +
  //                 jqXHR.status +
  //                 ", jqXHR.responseText:" +
  //                 jqXHR.responseTest
  //             );
  //         }

  //     })

  // })
  //댓글수정실패
  // $("div.comment").on("click",
  //     "div.comment-list">"button.comment-list__modify",
  //     function(){
  //         commentNo = $(this).parent().find("div.comment-list__no").text();
  //         console.log(commentNo+"댓글")
  //         // commentNo = commentNo.split("-")[1].trim();
  //         // console.log("수정댓글번호:" + commentNo1);
  //     // if(loginedNickname == commentNickname){
  //         let url = "http://localhost:1125/backroundreview/comment/" + commentNo;
  //         // console.log(url);
  //         let cmtContent = $(this).parent().find("input").val();
  //         // console.log(cmtContent);
  //         let obj = {
  //         "roundReviewCmtNo": commentNo,
  //         "roundReviewCmtContent": cmtContent,
  //         "userNickname":user_nickname,
  //         "roundReviewBoard" : {"roundReviewBoardNo" : board_no}};
  //         $.ajax({
  //             url: url,
  //             method: "put",
  //             contentType: "application/json; charset=utf-8",
  //             data: JSON.stringify(obj),
  //             success: function (jsonObj) {
  //                 alert(jsonObj.msg);
  //                 location.reload();
  //             },
  //             error: function (jsonObj) {
  //                 alert(jsonObj.msg);
  //             }
  //         });
  //     // }else{ // if문
  //        // alert("댓글 작성자가 아닙니다.");
  //     // }
  //     return false;
  // });

    //6. 좋아요 누르기/해제하기
    $("img.board__like").on("click", function () {
		let roundReviewBoardNo = board_no;
		// let likedNickname = "데빌";

        // 접속한 아이디와 좋아요한 닉네임이 같으면 -> 좋아요 삭제
		if (likedNickname == user_nickname) {
            //좋아요 하트 이미지 -> 흰색
            $("img.board__like").attr("src", "https://a.slack-edge.com/production-standard-emoji-assets/14.0/google-large/1f90d.png");
            $.ajax({
                //삭제할 글번호를 PathVariable로 보내주기 
                url: "http://localhost:1125/backroundreview/like/" + roundReviewBoardNo,
                method: "delete",
                data: { userNickname: likedNickname },
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
		alert("좋아요 삭제 성공");
		} else {

            // 세션 아이디와 좋아요한 닉네임이 같지 않으면 -> 좋아요 추가 
            let nickname = user_nickname;
            let data = { userNickname: nickname };
            //좋아요 하트 이미지 -> 주황색
            $("img.board__like").attr("src","https://a.slack-edge.com/production-standard-emoji-assets/14.0/google-medium/1f9e1.png");
            $.ajax({
                url: "http://localhost:1125/backroundreview/like/" + roundReviewBoardNo,
                method: "post",
                data: data,
                success: function (jsonObj) {
                    if ((jsonObj.status = 1)) {
                        alert("좋아요 추가 성공");
                        location.reload();
                    }
                },
                error: function (jqXHR) {
                    alert(jqXHR.status + ":" + "좋아요 추가 실패");
                    location.reload();
                } 
            }); 
            alert("좋아요 추가 성공");
		} 
    }); 

    //7. 이전 버튼 눌렀을 때 이전으로 넘어가기
    $("div.footer").on("click", "button.previous", function () {
        location.href = "./roundreviewboardlist.html";
    });
});