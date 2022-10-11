    $(function () {
        let noticeBoardNo = 0;
        let loginedNickname = localStorage.getItem("loginedNickname");
    $(document).ready(function () {
        // 페이지 로딩이 끝나면
        let queryString = location.search.split("=")[1];
        // console.log(queryString);
        noticeBoardNo = queryString;
        let url = "http://localhost:1124/board/write";

        $("#summernote").summernote({
        // summernote 실행
        placeholder: "게시글을 입력해 주세요(최대 1000자까지 쓸 수 있습니다)",
        height: 500, // 에디터 높이
        minHeight: null, // 최소 높이
        maxHeight: null, // 최대 높이
        focus: true, // 에디터 로딩후 포커스를 맞출지 여부
        lang: "ko-KR", // 한글 설정
        disableResizeEditor: true, // 크기 조절 기능 삭제
        fontNames: [
            "Arial",
            "Arial Black",
            "Comic Sans MS",
            "Courier New",
            "Helvetica neue",
            "Helvetica",
            "Impact",
            "Lucida Grande",
            "Tahoma",
            "Times New Roman",
            "Verdana",
            "Tahoma",
            "Courier New",
            "맑은 고딕",
            "굴림",
            "돋움",
        ],
        fontNamesIgnoreCheck: [
            "Arial",
            "Arial Black",
            "Comic Sans MS",
            "Courier New",
            "Helvetica neue",
            "Helvetica",
            "Impact",
            "Lucida Grande",
            "Tahoma",
            "Times New Roman",
            "Verdana",
            "Tahoma",
            "Courier New",
            "맑은 고딕",
            "굴림",
            "돋움",
        ],
        toolbar: [
            ["fontname", ["fontname"]],
            ["fontsize", ["fontsize"]],
            ["style", ["bold", "italic", "underline", "clear"]],
            ["color", ["color"]],
            ["table", ["table"]],
            ["para", ["paragraph"]],
            ["insert", ["link", "picture"]],
            ["view", []],
        ],
        fontSizes: [
            "8",
            "9",
            "10",
            "11",
            "12",
            "14",
            "16",
            "18",
            "20",
            "22",
            "24",
            "28",
            "30",
            "36",
            "50",
            "72",
        ],
    }); //summernote에
        if (
        typeof queryString == "undefined" ||
        queryString == null ||
        queryString == ""
        ) {
        $("button.submit-board__button").show();
        $("button.modify-board__button").hide();
        } else {
        $("button.submit-board__button").hide();
        $("button.modify-board__button").show();
        $.ajax({
            url: "http://localhost:1128/noticeboard/notice/" + noticeBoardNo,
            method: "get",
            success: function (jsonObj) {
            let detailObj = jsonObj.t.noticeBoard;
            let $formObj =$("form.write");
            console.log($formObj[0]);
            let content = detailObj.noticeBoardContent;
            let title = detailObj.noticeBoardTitle;
            // console.log("title" + title);
            $("input.write-title").val(title);
            $("#summernote").summernote("code", content); // 기존 내용 불러오기
            }, //success
        }); //ajax
        return false;
        } // else문
    }); // document.ready

    // 드래그 드롭
    // $(".sortable").sortable();
    //이미지 등록
    $("#imgFiles").change(function (e) {
        //div 내용 비워주기
        $("#preview").empty();

        let files = e.target.files;
        let arr = Array.prototype.slice.call(files);

        preview(arr);

        function preview(arr) {
            arr.forEach(function (f) {
            //div에 이미지 추가
            let img = '<li class="ui-state-default">';
            //str += '<span>'+fileName+'</span><br>';

            //이미지 파일 미리보기
            if (f.type.match("image.*")) {
                //파일을 읽기 위한 FileReader객체 생성
                let reader = new FileReader();
                reader.onload = function (e) {
                //파일 읽어들이기를 성공했을때 호출되는 이벤트 핸들러
                img += '<img src="' + e.target.result + '" title="' + f.name + '" width=300 height=300>';
                img += '<span class="delBtn" onclick="delImg(this)"> X </span>';
                img += "</li>";
                $(img).appendTo("#preview");
                };
                reader.readAsDataURL(f);
            }
            });
        }
        return false;
    });

    let $btSubmitBoard = $("div.submit-board>button.submit-board__button");

    // 버튼 클릭
    $btSubmitBoard.click( function () {
    let text = $("#summernote").summernote("code");
    
    let $formObj = $("form.write");
    // console.log($formObj[0]);
    let formData = new FormData($formObj[0]);
    // console.log("content: " + text);
    let title = $("input.write-title").val();
    // console.log("title: " + title);

    let obj = {};
    obj = formData.set("userNickname", loginedNickname);
    formData.append("key", JSON.stringify(obj));
    // console.log(obj);
    formData.forEach(function (value, key) {
        // console.log(key + ":" + value);
        // formData.append(key, value);
    });

    let obj2 = formData.get("imageFile");
    console.log(obj2);
    alert(obj2);
    if (obj2.size <= 0) {
        alert("이미지 첨부는 필수입니다");
    } else if (text == "" || text == " " || text == "  ") {
        alert("내용은 필수입력값 입니다.");
    } else if (title == "" || title == " " || title == "  ") {
        alert("제목은 필수입력값 입니다.");
    } else {
        $.ajax({
        url: "http://localhost:1128/noticeboard/notice/writeboard",
        method: "post",
        // enctype: 'multipart/form-data',
        // dataType: "json",
        contentType: false, //파일업로드용 설정
        processData: false, //파일업로드용 설정
        data: formData, //파일업로드용 설정
        cache: false, //이미지 다운로드용 설정
        success: function (data) {
            alert("등록완료");
            location.href = "../html/noticeboardlist.html";
        }, // 알럿이랑 href와 같이 동작 안됨
        error: function (jqXHR) {
            //응답실패
            alert("에러 : " + jqXHR.status);
        },
        });
        return false;
    }
    }); // 글 저장 버튼 클릭

    // -------- 게시글 수정 버튼 클릭 START --------
    let $btModifyBoard = $("div.submit-board>button.modify-board__button");
    // console.log(noticeBoardNo);
    // // 버튼 클릭
    // 버튼 클릭
    $btModifyBoard.click(function () {
        // console.log(loginedNickname);
    let text = $("#summernote").summernote("code");
    let $formObj = $("form.write");
    let formData = new FormData($formObj[0]);
    let title = $("input.write-title").val();
    // console.log("content: " + text);
    // console.log(typeof text); // string
    // console.log("title: " + title);
    // console.log(typeof title); // string

    let obj = {
        noticeBoardContent: text,
        noticeBoardTitle: title,
        userNickname: loginedNickname,
    };
    if (text == "" || text == " " || text == "  ") {
        alert("내용은 필수입력값 입니다.");
    } else if (title == "" || title == " " || title == "  ") {
        alert("제목은 필수입력값 입니다.");
    } else {
        $.ajax({
        url: "http://localhost:1128/noticeboard/notice/" + noticeBoardNo,
        method: "put",
        data: JSON.stringify(obj), //파일업로드용 설정
        contentType: "application/json; charset=UTF-8",
        success: function () {
            alert("글 수정 성공");
            location.href = "../html/noticeboardlist.html";
        },
        error: function (jqXHR) {
            //응답실패
            alert("에러:" + jqXHR.status);
        },
        });
        return false;
    }
    }); // 글 저장 버튼 클릭
    // // -------- 게시글 수정 버튼 클릭 END --------
});