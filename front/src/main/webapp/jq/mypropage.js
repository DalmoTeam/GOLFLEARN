$(function(){
    //localStorage에서 로그인된 아이디 가져오기 
    let loginedId = localStorage.getItem("loginedId")
    //*테스트용 아이디
    //let loginedId = "ohpro@gmail.com"

    //1) 페이지 요청되었을때 프로의 레슨 리스트 로드하기 
    $.ajax({
        url : "http://localhost:1124/back/mypage/pro",
        method : 'get',
        data : {userId : loginedId},
        success : function(jsonObj){
            if (jsonObj.status == 1){
                let lsnObj = jsonObj.lt;
                let $lsn = $("div.content").first();
                $lsn.show();
                //clone의 틀이되는 Html코드 지우기 
                $("div.content").not($lsn).remove();
                let $lsnParent = $lsn.parent();
                $(lsnObj).each(function(index, lsn){
                    let $lsnCopy = $lsn.clone();

                    $lsnCopy.find("div.lsn__no").html(lsn.lsnNo);
                    $lsnCopy.find("img.lsn__image").attr("src", "../lesson_images/" + lsn.lsnNo + ".PNG");
                    $lsnCopy.find("div.lsn__title").html(lsn.lsnTitle);
                    let lsn_status = lsn.lsnStatus;
                    
                    //레슨 상태에 따라 현재 수강상태 확인 
                    //0:레슨재개 / 1:레슨닫기 / 2:승인대기레슨 / 3:승인취소레슨
                    $resumeObj = $lsnCopy.find("button.lsn__resume");
                    $closeObj = $lsnCopy.find("button.lsn__close");
                    $waitObj = $lsnCopy.find("button.lsn__wait");
                    $rejectObj = $lsnCopy.find("button.lsn__reject");
                    if(lsn_status == 0){ 
                        $closeObj.hide();
                        $waitObj.hide();
                        $rejectObj.hide();
                    } else if (lsn_status == 1){
                        $resumeObj.hide();
                        $waitObj.hide();
                        $rejectObj.hide();
                    } else if (lsn_status == 2){
                        $resumeObj.hide();
                        $closeObj.hide();
                        $rejectObj.hide();
                    } else if (lsn_status == 3){
                        $resumeObj.hide();
                        $closeObj.hide();
                        $waitObj.hide();
                    } 
                    
                    $lsnParent.append($lsnCopy);
                });
                $("div.content").first().hide();
            }
        },
        error : function(jqXHR){
            alert('오류 : ' + jqXHR.status);
        }
    });

    //2)레슨이미지 클릭시 레슨상세페이지 연결 
    $('div.td').on('click', 'div.lsn>img.lsn__image', function(){
        //레슨번호 찾아오기 
        let $lsnNoObj = $(this).siblings("div.lsn__no");
        let lsn_no = $lsnNoObj.html(); 
        console.log(lsn_no);
        location.href = "/front/html/viewlesson.html?lsn_no=" + lsn_no;
    });


    //3)수강생관리 클릭시 수강생관리 페이지 연결  
    $('div.td').on('click', 'button.stdt__manage', function(){
        let $lsnNoObj = $(this).siblings("div.lsn__no"); 
        let lsn_no = $lsnNoObj.html();
        location.href = "/front/html/studentmanage.html?lsn_no=" + lsn_no;
    });    

})