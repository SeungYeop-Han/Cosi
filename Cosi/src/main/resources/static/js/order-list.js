var header = $("meta[name='_csrf_header']").attr('content');
var token = $("meta[name='_csrf']").attr('content');

/**
 * getOrders(url) -> Promise()   ::=   resolve(orders)
 */
function getOrders(url){
    return new Promise(function(resolve, reject){
        $.get(url, function(orders){
            resolve(orders);
        })
    });
}

function printUnfilledOrderList(){
    //리스트 초기화
    let unfilledOrderTable = document.getElementById("unfilledOrders");
    while(unfilledOrderTable.hasChildNodes()){
        unfilledOrderTable.removeChild(unfilledOrderTable.firstChild);
    }

    getOrders('http://localhost:8080/member/unfilledOrders')
        .then(function (unfilledOrders){
            return new Promise(function(resolve, reject){
                //미체결 주문 테이블 생성
                let table = document.getElementById("unfilledOrders");
                for(let iRow = 0; iRow < unfilledOrders.length; iRow++) {
                    //새 테이블 레코드 생성
                    let newOrderRow = document.createElement("tr");

                    //주문번호
                    let orderIdCol = document.createElement("td");
                    orderIdCol.innerHTML = unfilledOrders[iRow][0];
                    newOrderRow.appendChild(orderIdCol);

                    //코인코드
                    let marketCodeCol = document.createElement("td");
                    marketCodeCol.innerHTML = unfilledOrders[iRow][1];
                    newOrderRow.appendChild(marketCodeCol);

                    //매수|매도
                    let buyOrSellCol = document.createElement("td");
                    buyOrSellCol.innerHTML = ((unfilledOrders[iRow][2] == true) ? "매수" : "매도");
                    newOrderRow.appendChild(buyOrSellCol);

                    //주문수량
                    let orderAmountCol = document.createElement("td");
                    orderAmountCol.innerHTML = unfilledOrders[iRow][3];
                    newOrderRow.appendChild(orderAmountCol);

                    //주문단가
                    let orderPriceCol = document.createElement("td");
                    orderPriceCol.innerHTML = unfilledOrders[iRow][4];
                    newOrderRow.appendChild(orderPriceCol);

                    //주문총액
                    let totalPriceCol = document.createElement("td");
                    totalPriceCol.innerHTML = unfilledOrders[iRow][5];
                    newOrderRow.appendChild(totalPriceCol);

                    //감시가격
                    let stopPriceCol = document.createElement("td");
                    stopPriceCol.innerHTML = unfilledOrders[iRow][6];
                    newOrderRow.appendChild(stopPriceCol);

                    //주문시간
                    let orderDatetimeCol = document.createElement("td");
                    orderDatetimeCol.innerHTML = unfilledOrders[iRow][7];
                    newOrderRow.appendChild(orderDatetimeCol);
                    
                    //취소 버튼
                    let cancelOrderBtn = document.createElement("button");
                    cancelOrderBtn.innerText = "주문취소";
                    cancelOrderBtn.addEventListener("click", function(e){
                        $.ajax({
                            type : 'DELETE',
                            url: "/order?orderId=" + orderIdCol.innerHTML,
                            dataType : "text",
                            charset: "utf-8",
                            beforeSend: function(xhr){
                                xhr.setRequestHeader(header, token)
                            },
                            success: function(msg){
                                alert(msg);
                            }
                        })
                    });
                    newOrderRow.appendChild(cancelOrderBtn);

                    unfilledOrderTable.appendChild(newOrderRow);
                }
                resolve(unfilledOrders);
            });
        });
}

function printFilledOrderList(){
    //리스트 초기화
    let filledOrderTable = document.getElementById("filledOrders");
    while(filledOrderTable.hasChildNodes()){
        filledOrderTable.removeChild(filledOrderTable.firstChild);
    }

    getOrders('http://localhost:8080/member/filledOrders')
        .then(function (filledOrders){
            return new Promise(function(resolve, reject){
                //미체결 주문 테이블 생성
                let table = document.getElementById("filledOrders");
                for(let iRow = 0; iRow < filledOrders.length; iRow++) {
                    //새 테이블 레코드 생성
                    let newOrderRow = document.createElement("tr");

                    //주문번호
                    let orderIdCol = document.createElement("td");
                    orderIdCol.innerHTML = filledOrders[iRow][0];
                    newOrderRow.appendChild(orderIdCol);

                    //코인코드
                    let marketCodeCol = document.createElement("td");
                    marketCodeCol.innerHTML = filledOrders[iRow][1];
                    newOrderRow.appendChild(marketCodeCol);

                    //매수|매도
                    let buyOrSellCol = document.createElement("td");
                    buyOrSellCol.innerHTML = ((filledOrders[iRow][2] == true) ? "매수" : "매도");
                    newOrderRow.appendChild(buyOrSellCol);

                    //주문유형
                    let orderTypeCol = document.createElement("td");
                    orderTypeCol.innerHTML = filledOrders[iRow][3];
                    newOrderRow.appendChild(orderTypeCol);

                    //주문단가
                    let unitPriceCol = document.createElement("td");
                    unitPriceCol.innerHTML = filledOrders[iRow][4];
                    newOrderRow.appendChild(unitPriceCol);

                    //체결단가
                    let filledUnitPriceCol = document.createElement("td");
                    filledUnitPriceCol.innerHTML = filledOrders[iRow][5];
                    newOrderRow.appendChild(filledUnitPriceCol);

                    //체결수량
                    let filledAmountCol = document.createElement("td");
                    filledAmountCol.innerHTML = filledOrders[iRow][6];
                    newOrderRow.appendChild(filledAmountCol);

                    //체결금액
                    let filledPriceCol = document.createElement("td");
                    filledPriceCol.innerHTML = filledOrders[iRow][7];
                    newOrderRow.appendChild(filledPriceCol);

                    //주문총액
                    let totalPriceCol = document.createElement("td");
                    totalPriceCol.innerHTML = filledOrders[iRow][8];
                    newOrderRow.appendChild(totalPriceCol);

                    //주문시간
                    let orderDatetimeCol = document.createElement("td");
                    orderDatetimeCol.innerHTML = filledOrders[iRow][9];
                    newOrderRow.appendChild(orderDatetimeCol);

                    //체결시간
                    let filledDatetimeCol = document.createElement("td");
                    filledDatetimeCol.innerHTML = filledOrders[iRow][10];
                    newOrderRow.appendChild(filledDatetimeCol);

                    //수수료
                    let buyingFeeCol = document.createElement("td");
                    buyingFeeCol.innerHTML = filledOrders[iRow][11];
                    newOrderRow.appendChild(buyingFeeCol);

                    filledOrderTable.appendChild(newOrderRow);
                }
                resolve(filledOrders);
            });
        });
}

function refreshOrderList(){
    //미체결 주문 리스트 출력
    printUnfilledOrderList();
    //체결 주문 리스트 출력
    printFilledOrderList();
}