$(function(){
    //localStorage에서 로그인된 아이디 가져오기 
    //- 로그인된 계정 잡는 방법 
    //semi -> back에서 session 이용 / final -> front에서 localstorage이용 / 추후 -> 토큰/쿠키 이용 인증 
    let loginedId = localStorage.getItem("loginedId")
    //*테스트용 ID
    //let loginedId = "zzeonsh@gmail.com"

    //1) 페이지 요청되었을때 레슨내역 리스트 로드하기 
    $.ajax({
        url : "http://localhost:1124/back/mypage/student",
        method : 'get',
        data : {userId : loginedId},
        success : function(jsonObj){
            if (jsonObj.status ==1){
                //json데이터 찍어오기 
                let lsnLineObj = jsonObj.lt;
                let myStarScore = 0;
                let $lsnLine = $("div.content").first();
                $lsnLine.show();
                //clone의 틀이되는 Html코드 지우기 
                $("div.content").not($lsnLine).remove();

                let $lsnLineParent = $lsnLine.parent();
                $(lsnLineObj).each(function(index, lsnLine){
                    let $lsnLineCopy = $lsnLine.clone();
                    
                    //내용들 넣어주기 
                    $lsnLineCopy.find("img.lsn__image").attr("src", "../lesson_images/" + lsnLine.lsn.lsnNo + ".PNG");
                    $lsnLineCopy.find("div.no").html(lsnLine.lsn.lsnNo);
                    $lsnLineCopy.find("div.title").html(lsnLine.lsn.lsnTitle);
                    $lsnLineCopy.find("div.exp_dt").html(lsnLine.lsnExpDt);
                    $lsnLineCopy.find("div.current__cnt").html(lsnLine.lsnCnt);
                    $lsnLineCopy.find("div.total__cnt").html(lsnLine.lsn.lsnCntSum);
                    
                    let lsnReview = lsnLine.lsn.lsnReview;
                    // let myStarScore = lsnLine.lsn.lsnReview;
                    // console.log($lsnLineCopy);
                    // console.log($lsnLineCopy.prev().innerHtml);
                    let $writeReviewObj = $('input[value=레슨후기작성]');
                    let $modifyReviewObj = $('input[value=레슨후기수정]');
                    // if (lsnReview == null) { //작성한 후기가 없으면 수정 버튼숨기기
                    //     // $modifyReviewObj.hide();
                    // }else {
                    //     // $lsnLineCopy>$writeReviewObj.show();

                    //     // $writeReviewObj.first().hide();
                    //     // $modifyReviewObj.show();
                    // }
                    $lsnLineParent.append($lsnLineCopy);
                    });
                    $("div.content").first().hide();
            }
        },
        error : function(jqXHR){
            alert('오류 : ' + jqXHR.status);
        }
    });

    //2) 레슨이미지를 클릭했을 때 해당 상세로 이동하기 
    $('div.td').on('click', 'div.middle>img.lsn__image', function(){
        let $lsnNoObj = $(this).siblings("div.no"); 
        let lsn_no = $lsnNoObj.html(); 
        //lsn_no를 쿼리스트링으로 보내줘서 어떤 레슨을 클릭했는지 구분
        location.href = "/front/html/viewlesson.html?lsn_no=" + lsn_no;
    });
    //3) 레슨후기작성 버튼 눌렀을때 작성 페이지로 이동하기
    let $lsnListObj = $('div.table');
    $lsnListObj.on('click', 'input[value=레슨후기작성]', function(){
        let $lsnLineNoObj = $(this).parent().find('div.no');
        let lsn_line_no = $lsnLineNoObj.html();
        //lsn_line_no를 쿼리스트링으로 보내줘서 레슨내역 구분, review_exist값을 0으로 보내줘서 수정과 구분
        location.href = "../html/review.html?review_exist=0&lsn_line_no=" + lsn_line_no;
    });
    //4) 레슨후기수성 버튼 눌렀을때 수정 페이지로 이동하기
    let $lsnListModiObj = $('div.table');
    $lsnListModiObj.on('click', 'input[value=레슨후기수정]', function(){
        let $lsnLineNoObj = $(this).parent().find('div.no');
        let lsn_line_no = $lsnLineNoObj.html();
        //lsn_line_no를 쿼리스트링으로 보내줘서 레슨내역 구분, review_exist값을 1으로 보내줘서 작성과 구분
        location.href = "../html/review.html?review_exist=1&lsn_line_no=" + lsn_line_no;
    });
})