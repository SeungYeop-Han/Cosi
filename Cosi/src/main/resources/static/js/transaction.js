var header = $("meta[name='_csrf_header']").attr('content');
var token = $("meta[name='_csrf']").attr('content');

//매수
function BuyButton(){

    console.log("매수 실행");
    let orderType = document.getElementsByName("buyOrderType");

    let coinCode = document.getElementById("marketSelect").value;

    for(var i=0; i < 3; i++){
        if(document.getElementsByName("buyOrderType")[i].checked == true){
            var order = document.getElementsByName("buyOrderType")[i].value;
            console.log(order);
        }
    }

    if( order == "pendingOrder"){

        let buyMoney = adjustPrice(document.getElementById("bpbuyMoney").value);
        let buyNumber = document.getElementById("bpbuyNumber").value;
        let buyTotal = buyMoney * buyNumber
        document.getElementById("bpbuyTotal").value = buyTotal;

        let dataObj = new Object();

        dataObj.isBuying = true; //true : 매수
        dataObj.orderType = "LIMIT";
        dataObj.coinCode = coinCode;
        dataObj.orderPrice = buyMoney;
        dataObj.orderAmount = buyNumber;
        dataObj.totalPrice = buyTotal;

        let order = JSON.stringify(dataObj);
        console.log(order);

         $.ajax({
                type : 'POST',
                url: "/order",
                data: order,
                dataType : "text",
                contentType: 'application/json',
                charset: "utf-8",
                beforeSend: function(xhr){
                    xhr.setRequestHeader(header, token)
                },
                success: function(msg) {
                    alert(msg);
                }
         })

    } // 시장가 일경우
    else if( order == "marketOrder"){

        let buyTotal = document.getElementById("bmbuyTotal").value;
        document.getElementById("bpbuyTotal").value = buyTotal;
        let dataObj = new Object();

        dataObj.orderType = "MARKET";
        dataObj.isBuying = true; //true : 매수
        dataObj.coinCode = coinCode;
        dataObj.totalPrice = buyTotal;

        let order = JSON.stringify(dataObj);
        console.log(order);

         $.ajax({
             type : 'POST',
             url: "/order",
             data: order,
             dataType : "text",
             contentType: 'application/json',
             charset: "utf-8",
             beforeSend: function(xhr){
                 xhr.setRequestHeader(header, token)
             },
             success: function(msg){
                    alert(msg);
             }
         })

    }else if( order == "bookOrder" ){

        let buyWant = adjustPrice(document.getElementById("bobuyWant").value);
        let buyMoney = adjustPrice(document.getElementById("bobuyMoney").value);
        let buyNumber = document.getElementById("bobuyNumber").value;
        let buyTotal = buyMoney * buyNumber;
        document.getElementById("bobuyTotal").value = buyTotal;

        let dataObj = new Object();

        dataObj.orderType = "STOP_LIMIT";
        dataObj.isBuying = true; //true : 매수
        dataObj.coinCode = coinCode;
        dataObj.stopPrice = buyWant;
        dataObj.orderPrice = buyMoney;
        dataObj.orderAmount = buyNumber;
        dataObj.totalPrice = buyTotal;

        let order = JSON.stringify(dataObj);
        console.log(order);

        $.ajax({
            type : 'POST',
            url: "/order",
            data: order,
            dataType : "text",
            contentType: 'application/json',
            charset: "utf-8",
            beforeSend: function(xhr){
                xhr.setRequestHeader(header, token)
            },
            success: function(msg){
                    alert(msg);
            }
        })

    }

}

//매도
function SellButton(){

    console.log("매도 실행");
    let orderType = document.getElementsByName("sellOrderType");

    let coinCode = document.getElementById("marketSelect").value;

    for(var i=0; i < 3; i++){
        if(document.getElementsByName("sellOrderType")[i].checked == true){
            var order = document.getElementsByName("sellOrderType")[i].value;
            console.log(order);
        }
    }

    if( order == "pendingOrder"){ //지정가

        let sellMoney = adjustPrice(document.getElementById("spsellMoney").value);
        let sellNumber = document.getElementById("spsellNumber").value;
        let sellTotal = sellMoney * sellNumber;
        document.getElementById("spsellTotal").value = sellTotal;

        let dataObj = new Object();

        dataObj.orderType = "LIMIT";
        dataObj.isBuying = false; //true : 매수
        dataObj.coinCode = coinCode;
        dataObj.orderPrice = sellMoney;
        dataObj.orderAmount = sellNumber;
        dataObj.totalPrice = sellTotal;

        let order = JSON.stringify(dataObj);
        console.log(order);

        $.ajax({
            type : 'POST',
            url: "/order",
            data: order,
            dataType : "text",
            contentType: 'application/json',
            charset: "utf-8",
            beforeSend: function(xhr){
                xhr.setRequestHeader(header, token)
            },
            success: function(msg){
                alert(msg);
            }
        })

    }
    else if( order == "marketOrder"){  //시장가

        let sellTotal = document.getElementById("smsellTotal").value;

        let dataObj = new Object();

        dataObj.orderType = "MARKET";
        dataObj.isBuying = false; //true : 매수
        dataObj.coinCode = coinCode;
        dataObj.orderAmount = sellTotal;

        let order = JSON.stringify(dataObj);
        console.log(order);

        $.ajax({
            type : 'POST',
            url: "/order",
            data: order,
            dataType : "text",
            contentType: 'application/json',
            charset: "utf-8",
            beforeSend: function(xhr){
                xhr.setRequestHeader(header, token)
            },
            success: function(msg){
                alert(msg);
            }
        })


    }else if( order == "bookOrder" ){ // 예약-지정가

        let sellWant = adjustPrice(document.getElementById("sosellWant").value);
        let sellMoney = adjustPrice(document.getElementById("sosellMoney").value);
        let sellNumber = document.getElementById("sosellNumber").value;
        let sellTotal = sellMoney * sellNumber;
        document.getElementById("sosellTotal").value = sellTotal;

        let dataObj = new Object();

        dataObj.orderType = "STOP_LIMIT";
        dataObj.isBuying = false; //true : 매수
        dataObj.coinCode = coinCode;
        dataObj.stopPrice = sellWant;
        dataObj.orderPrice = sellMoney;
        dataObj.orderAmount = sellNumber;
        dataObj.totalPrice = sellTotal;

        let order = JSON.stringify(dataObj);
        console.log(order);

        $.ajax({
            type : 'POST',
            url: "/order",
            data: order,
            dataType : "text",
            contentType: 'application/json',
            charset: "utf-8",
            beforeSend: function(xhr){
                xhr.setRequestHeader(header, token)
            },
            success: function(msg){
                alert(msg);
            }
        })
    }
}

function adjustPrice(price){

    price = Number(price);
    let tickUnit = 0;
    let isInteger = true;

    if(price >= 2000000){
        tickUnit = 1000
    }
    else if(price >= 1000000){
        tickUnit = 500
    }
    else if(price >= 500000){
        tickUnit = 100
    }
    else if(price >= 100000){
        tickUnit = 50
    }
    else if(price >= 10000){
        tickUnit = 10
    }
    else if(price >= 1000){
        tickUnit = 5
    }
    else if(price >= 100){
        tickUnit = 1
    }
    else if(price >= 10){
        isInteger = false;
        tickUnit = 1
    }
    else if(price >= 0.01){
        isInteger = false;
        tickUnit = 2
    }
    else{
        isInteger = false;
        tickUnit = 4
    }

    if(isInteger)
        price = price - (price % tickUnit);
    else
        price = price.toFixed(tickUnit)

    return price
}

function setStep(e){
    e.target.value = adjustPrice(e.target.value);

}