var isEmailTestPassed = false;
var isPasswordTestPassed = false;
var isNameTestPassed = false;

function emailTest() {
    isEmailTestPassed = true;
    let email = document.getElementById("userEmail").value;

    $.ajax({
        url: "/registration/isEmailDuplicated?email=" + email, // 클라이언트가 HTTP 요청을 보낼 서버의 URL 주소
        method: "GET",   // HTTP 요청 메소드(GET, POST 등)
        dataType: "text" // 서버에서 보내줄 데이터의 타입
    }).done(function(result) {
        document.getElementById("emailDupCheckMsg").innerText = result;
        document.getElementById("emailDupCheckMsg").style.color = "red";
        if(result == "사용 가능한 이메일입니다."){
            document.getElementById("emailDupCheckMsg").style.color = "blue";
            isEmailTestPassed = true;
        }
    });

    return isEmailTestPassed;
}

function passwordTest(){

    var p1 = document.getElementById("userPassword").value;
    var p2 = document.getElementById("CheckUserPassword").value;

    if( p1.length == 0 || p2.length == 0 ){
        document.getElementById("passwordMatchCheckMsg").innerHTML = "비밀번호를 입력해주세요."
        document.getElementById("passwordMatchCheckMsg").style.color = "red";
        isPasswordTestPassed = false;
    }
    else if(  p1 != p2 ){
        document.getElementById("passwordMatchCheckMsg").innerHTML = "비밀번호가 일치하지 않습니다."
        document.getElementById("passwordMatchCheckMsg").style.color = "red";
        isPasswordTestPassed = false;
    }
    else{
        document.getElementById("passwordMatchCheckMsg").innerHTML = "비밀번호가 일치합니다."
        document.getElementById("passwordMatchCheckMsg").style.color = "blue";
        isPasswordTestPassed = true;
    }

    return isPasswordTestPassed;
}

function nameTest(){
    let name = document.getElementById("name").value;


    if(name.length >= 1){
        document.getElementById("submitButton").disabled = true;
        isNameTestPassed = true;
    }
    else {
        document.getElementById("nameLengthCheckMsg").innerHTML = "이름을 입력 해주세요."
        document.getElementById("nameLengthCheckMsg").style.color = "red";
        isNameTestPassed = false;
    }

    return isNameTestPassed;
}

function isRegistrationFormValid(){
    emailTest();
    passwordTest();
    nameTest();

    if( isPasswordTestPassed && isEmailTestPassed && isNameTestPassed){
        return true;
    }
    else {
        return false;
    }
}
