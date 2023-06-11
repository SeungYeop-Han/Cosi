var header = $("meta[name='_csrf_header']").attr('content');
var token = $("meta[name='_csrf']").attr('content');

function passwordTest(){
    var mypw = document.getElementById("password").value;
    var p1 = document.getElementById("newPassword1").value;
    var p2 = document.getElementById("newPassword2").value;

    var checkIndex = false;

    if(mypw == "" || p1 == "" || p2==""){
        document.getElementById("printPass").innerHTML = "모든 항목을 입력해주세요"
        document.getElementById("printPass").style.color = "red";
        checkIndex = false;
    }

    else if( p1 != p2 ){
        document.getElementById("printPass").innerHTML = "새로운 비밀번호가 일치하지 않습니다."
        document.getElementById("printPass").style.color = "red";
        checkIndex = false;
    }
    else{
        document.getElementById("printPass").innerHTML = "새로운 비밀번호가 일치합니다."
        document.getElementById("printPass").style.color = "blue";
        checkIndex = true;
    }

    if( checkIndex == true ){
        $.ajax({
            type : 'PUT',
            url : "/member/password?password=" + mypw + "&newPassword=" + p1,
            dataType: "text",
            charset: "utf-8",
            beforeSend: function(xhr){
                xhr.setRequestHeader(header, token)
            },
            success: function(msg){
                alert(msg);
                document.getElementById("newPassword1").value="";
                document.getElementById("newPassword2").value ="";
                document.getElementById("printPass").innerHTML = "";
                document.getElementById("password").value = "";
            },
            error: function(msg){
                alert(msg);
            }
        })
    }
}

function reset(){
    console.log("Reseting...");

    $.ajax({
        type: 'PUT',
        url: "/reset",
        dataType: "text",
        charset: "utf-8",
        beforeSend: function(xhr){
            xhr.setRequestHeader(header, token)
        },
        success: function (msg) {
            alert(msg);
        },
        error: function (msg) {
            alert(msg);
        }
    });
}

// function withdraw(){
//     console.log("Withdrawing...");
//
//     $.ajax({
//         type: 'DELETE',
//         url: "/member",
//         dataType: "html",
//         charset: "utf-8",
//         success: function (msg) {
//             alert(msg);
//         },
//         error: function (msg) {
//             alert(msg);
//         }
//     });
// }