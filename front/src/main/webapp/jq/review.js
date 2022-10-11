$(function(){
    $titleObj = $('div.lsn_title');
    $lineNoObj = $('input.lsn_line_no');
    $reviewObj = $('textarea.review_context');
    $starScoreObj = $('div.starform');

    //mypage에서 보내준 쿼리스트링 split해서 변수에 담기 
    let params = location.search.substring(1).split('&');
    let review_exist = params[0].split("=")[1];
    let lsn_line_no = params[1].split("=")[1]; 
    //1-1) 후기가 존재하지 않는 경우 (review_exist=0) -> 작성을 위해 레슨제목, 번호만 호출하기 
    if(review_exist == 0) { 
        $.ajax({
            url : "http://localhost:1124/back/review/new/",
            method : 'get',
            data : {lsn_line_no : lsn_line_no},
            success : function(jsonObj){
                let lsn_title = jsonObj.t.lsnTitle;
                let lsn_no = jsonObj.t.lsnNo;
                $titleObj.html(lsn_title);
                $lineNoObj.val(lsn_line_no);
                
                $("input.modify").hide();  
                $("img.lsn__img").attr("src","../lesson_images/" + lsn_no + ".PNG");
                review_exist = 0;
            },
            error : function(jqXHR){
                alert('오류 : ' + jqXHR.status);
            }
        });
    //1-2) 후기가 존재하는 경우 (review_exist!=0)-> 수정을 위해 기존작성내용 모두 불러오기 
    }else{ 
        $.ajax({
            url : "http://localhost:1124/back/review/previous/",
            method : 'get',
            data : {lsn_line_no : lsn_line_no},
            success : function(jsonObj){
                let lsn_title = jsonObj.t.lsn.lsnTitle;
                let lsn_no = jsonObj.t.lsn.lsnNo;
                let review = jsonObj.t.lsnReview.review;
                $titleObj.html(lsn_title);
                $lineNoObj.val(lsn_line_no);
                $reviewObj.val(review);
                
                $("input.submit").hide();  
                $("img.lsn__img").attr("src","../lesson_images/" + lsn_no + ".PNG");
            },
            error : function(jqXHR){
                alert('오류 : ' + jqXHR.status);
            }
        });
        
    };

    //2) 제출버튼 클릭시 작성용 구문 호출 
    if(review_exist == 0){
        $("input.submit").on('click', function(){
            let lsn_line_no = $("input.lsn_line_no").val();
            let review = $("textarea#review").val();
            //checked된 별점의 값
            let my_star_score = $("input[name='my_star_score']:checked").val();
            let obj = {
                lsnLine : {
                    lsnLineNo : lsn_line_no
                },
                review : review,
                myStarScore : my_star_score
            }
            $.ajax({
                url : "http://localhost:1124/back/review/write",
                method : 'post',
                data : JSON.stringify(obj),
                contentType: "application/json; charset=UTF-8",
                success : function(){
                    alert("글 작성 성공");
                    //작성 성공하면 mypage로 돌아가기 
                    location.href="../html/mypage.html";
                },
                error : function(jqXHR){
                    alert('오류 : ' + jqXHR.responseText);
                }
            })
            return false;
        });
    } else{
    //3) 수정 버튼 클릭시 수정용 구문 호출 
        $("input.modify").on('click', function(){
            let lsn_line_no = $("input.lsn_line_no").val();
            let review = $("textarea#review").val();
            //checked된 별점의 값
            let my_star_score = $("input[name='my_star_score']:checked").val();
            let obj = {
                lsnLine : {
                    lsnLineNo : lsn_line_no
                },
                review : review,
                myStarScore : my_star_score
            }
            $.ajax({
                url : "http://localhost:1124/back/review/modify",
                method : 'put',
                data : JSON.stringify(obj),
                contentType: "application/json; charset=UTF-8",
                success : function(){
                    alert("글 수정 성공");
                    location.href="../html/mypage.html";
                },
                error : function(jqXHR){
                    alert('오류 : ' + jqXHR.status);
                }
            })
            return false;
        });
    }
    //4) 이전 클릭시 이전 페이지로 이동
    let $backObj = $('input.back');
    $backObj.click(function(){
        $(location).attr('href', '/front/html/mypage.html');
    });
    
});