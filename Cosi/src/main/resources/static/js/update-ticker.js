/******************************************************************
 * summaryPrinter.js
 * 마지막 수정 일: 2022-05-24
 * SeungYeop Han, 32174842@dankook.ac.kr
 * 
 * 가상화폐 모의투자 서비스 메인 거래소 페이지의
 * 요약정보(ticker)를 갱신하는 자바스크립트 파일
 * ****************************************************************
 * 
 * 본 .js 스크립트 코드는 다음과 같은 구조로 작성되었습니다.
 * 
 *    //////////// DEFINE CONSTANTS //////////// 
 *                    ......
 *    //////////// DEFINE VARIABLES //////////// 
 *                    ......
 *    //////////// DEFINE FUNCTIONS //////////// 
 *                    ......
 *    //////////// DEFINE EVENT HANDLER //////////// 
 *                    ......
 * 
 *    //////////// MAIN: ENTRY POINT //////////// 
 *                 
 ******************************************************************/


////////////////////////////////// DEFINE CONSTANTS //////////////////////////////////





////////////////////////////////// DEFINE VARIABLES //////////////////////////////////
/* tickerApiUrl
 * 쿼리 파라미터
 *   market={KRW-BTC, BTC-ETH, ..., 등의 마켓코드 형식의 문자열}
 */
var tickerApiUrl;
var tradeApiUrl;






////////////////////////////////// DEFINE FUNCTIONS //////////////////////////////////
/* request(url, callback) -> void
 *   ::=  주어진 url로 GET 메시지를 보내어 데이터를 비 동기적으로 응답받습니다.
 *        만약 요청에 대해 성공적으로 데이터를 응답받는 경우, 명시한 callback 함수의 파라미터로 응답이 주어집니다.
 */
function requestTicker(url, callback){
  var httpRequestTicker = new XMLHttpRequest();

  httpRequestTicker.onreadystatechange = function () {
      if (httpRequestTicker.readyState === 4) { // request is done
          if (httpRequestTicker.status === 200) { // successfully
              callback(httpRequestTicker.response); // we're calling our method
          }
      }
  };

  httpRequestTicker.open('GET', url);
  httpRequestTicker.send();
}

function requestTrade(url, callback){
  var httpRequestTrade = new XMLHttpRequest();

  httpRequestTrade.onreadystatechange = function () {
      if (httpRequestTrade.readyState === 4) { // request is done
          if (httpRequestTrade.status === 200) { // successfully
              callback(httpRequestTrade.response); // we're calling our method
          }
      }
  };

  httpRequestTrade.open('GET', url);
  httpRequestTrade.send();
}

// 이채은 - 마켓 코드를 조회해서 마켓 코드와 코인 한국 이름 비교하기

function findKoreanName(element){

    const nameRequest = new XMLHttpRequest();
    const nameUrl = 'https://api.upbit.com/v1/market/all?isDetails=true';

    nameRequest.open("GET", nameUrl, false);
    nameRequest.send();

    var obj = JSON.parse(nameRequest.responseText);

    var codeArray = new Array(); // 코드 번호만 저장되는 배열
    var nameArray = new Array(); // 코드 이름만 저장되는 배열

    var i;
    for( i = 0; i < obj.length; i++){
    // 한국에서 유통되는 코인만 가져오기
        if( obj[i].market.slice(0,3) == "KRW" ){
           codeArray.push(obj[i].market);
           nameArray.push(obj[i].korean_name);
        }
    }

    if( codeArray.includes(element)) {
        var index  = codeArray.indexOf(element)
        var korean_name = nameArray[index];

        // 코인의 길이가 길 경우 처리
        if( korean_name.length > 9 ){
            document.getElementById("marketName").style.fontSize = "25px";
        }
        else{
            document.getElementById("marketName").style.fontSize = "30px";
        }

        return korean_name;
    }

}




/* tickerUpdate(response) -> void
 *   ::=  request(url, callback) 함수에 주어지는 콜백함수입니다.
 *        응답받은 데이터를 기반으로 종목요약정보를 표시하는 DOM객체를 갱신합니다.
 */
function tickerUpdate (result){

  var json = JSON.parse(result)[0];

  var color = "black"
  if(json.change == "FALL")
    color = "blue";
  else if(json.change == "RISE")
    color = "red";

//   document.getElementById("marketName").innerText = json.market;
   document.getElementById("marketName").innerText = findKoreanName(json.market);
   document.getElementById("tradePrice").innerText = json.trade_price + " KRW";
   document.getElementById("highPrice").innerText = json.high_price;
   document.getElementById("lowPrice").innerText = json.low_price;

   // 이채은 - 표기 추가

   if( json.signed_change_price >= 0 ){
       document.getElementById("changePrice").innerText = "▲ " + json.signed_change_price;
       document.getElementById("changePrice").style.color = color;
   }
   else{
       document.getElementById("changePrice").innerText = "▼ " + json.signed_change_price;
       document.getElementById("changePrice").style.color = color;
   }


  document.getElementById("changeRate").innerText = (parseFloat(json.signed_change_rate) * 100).toPrecision(2) + "%";
  document.getElementById("changeRate").style.color = color;

  document.getElementById("accTradeVolume_24H").innerText = (json.acc_trade_volume_24h).toPrecision(7);
  document.getElementById("accTradeRate_24H").innerText = Math.round(json.acc_trade_price_24h);
}

function tradeUpdate(result){
    var json = JSON.parse(result)[0];

    //console.log(tradeApiUrl)

    var table = document.getElementById('tradeTable');

    var tr = document.createElement("tr");
    var td1 = document.createElement("td");
    td1.appendChild(document.createTextNode(json.trade_date_utc + " " + json.trade_time_utc));

    var td2 = document.createElement("td");
    td2.appendChild(document.createTextNode(json.trade_price));

    var td3 = document.createElement("td");
    td3.appendChild(document.createTextNode(json.trade_volume));

    tr.appendChild(td1);
    tr.appendChild(td2);
    tr.appendChild(td3);

    table.prepend(tr);
}

/* updateData() -> void
 *   ::=  주기적으로 호출되어 종목 현황을 갱신합니다.
 *
 */ function updateTicker(){   
  requestTicker(tickerApiUrl, tickerUpdate);
}

function updateTrade(){
    requestTrade(tradeApiUrl, tradeUpdate);
}




////////////////////////////////// DEFINE EVENT HANDLERS //////////////////////////////////

$('select').change(function(){
  let market = $('#marketSelect option:selected').val();
  console.log("++")
  console.log(market)

  var table = document.getElementById('tradeTable');
  while(table.hasChildNodes()){
      table.removeChild(table.firstChild);
  }

  tickerApiUrl = "https://api.upbit.com/v1/ticker?markets=" + market;
  tradeApiUrl = "https://api.upbit.com/v1/trades/ticks?market=" + market + "&count=1";
  console.log(tickerApiUrl)
  requestTicker(tickerApiUrl, tickerUpdate);
  requestTrade(tradeApiUrl, tradeUpdate);

  setTimeout(function(){
      tradePriceOfSelectedCoin = Number(document.getElementById("tradePrice").innerText.slice(0, -4));
      //주문 가격 입력 폼 초기화
      $("#bpbuyMoney").val(tradePriceOfSelectedCoin)
      $("#bobuyWant").val(tradePriceOfSelectedCoin)
      $("#bobuyMoney").val(tradePriceOfSelectedCoin)
      $("#spsellMoney").val(tradePriceOfSelectedCoin)
      $("#sosellWant").val(tradePriceOfSelectedCoin)
      $("#sosellMoney").val(tradePriceOfSelectedCoin)
  }, 300)
});


////////////////////////////////// MAIN: ENTRY POINT //////////////////////////////////
setInterval(updateTicker, 1000);
setInterval(updateTrade, 1000);