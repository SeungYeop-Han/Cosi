// 이채은 - 현재가 조회 리스트

var socket; // 소켓

// 웹소켓 연결
function connectWS(marketCodes) {
	if(socket != undefined){
		socket.close();
	}

	socket = new WebSocket("wss://api.upbit.com/websocket/v1");
	socket.binaryType = 'arraybuffer';

	socket.onopen 	= function(e){

		filterRequest('[{"ticket":"UNIQUE_TICKET"},{"type":"ticker","codes":[' + marketCodes +']},{"type":"trade","codes":["KRW-BTC"]}]');
	}

	socket.onclose 	= function(e){
		socket = undefined;
	}

	socket.onmessage= function(e){

		var enc = new TextDecoder("utf-8");
		var arr = new Uint8Array(e.data);
		var str_d = enc.decode(arr);

		var coinData = JSON.parse(str_d);

		if(coinData.type == "ticker") { // 현재가 데이터

			let marketCode = coinData.code;
			let coinRow = document.getElementById(marketCode);

			let marketCodeCol = coinRow.firstElementChild;
			marketCodeCol.innerHTML = marketCode;
			let tradePriceCol = marketCodeCol.nextElementSibling;
			tradePriceCol.innerHTML = coinData.trade_price;
			let changeRateCol = tradePriceCol.nextElementSibling;
			changeRateCol.innerHTML = (coinData.signed_change_rate * 100).toPrecision(2) + "%";
			let changePriceCol = changeRateCol.nextElementSibling;
			changePriceCol.innerHTML = coinData.signed_change_price;
			let accTradePriceCol = changePriceCol.nextElementSibling;
			accTradePriceCol.innerHTML = Math.round(coinData.acc_trade_price_24h);
		}

		if(coinData.type == "orderbook") { // 호가 데이터
		// TODO
		}
		if(coinData.type == "trade") { // 체결 데이터

//			var table = document.getElementById('tradeTable');
//			var tr = document.createElement("tr");
//			var td1 = document.createElement("td");
//			td1.appendChild(document.createTextNode(coinData.trade_date + " " + coinData.trade_time));
//
//			var td2 = document.createElement("td");
//			td2.appendChild(document.createTextNode(coinData.trade_price));
//
//			var td3 = document.createElement("td");
//			td3.appendChild(document.createTextNode(coinData.trade_volume));
//
//			tr.appendChild(td1);
//	      	tr.appendChild(td2);
//	      	tr.appendChild(td3);
//
//			table.prepend(tr);

		}
	}
}
// 웹소켓 연결 해제
function closeWS() {
	if(socket != undefined){
		socket.close();
		socket = undefined;
	}
}

// 웹소켓 요청
function filterRequest(filter) {
	if(socket == undefined){
		alert('no connect exists');
		return;
	}
	socket.send(filter);
}

//코인 리스트 테이블에 click 버블 리스너 등록
let coinListTable = document.getElementById("coinListTable");
coinListTable.addEventListener("click", function(e){
	let val = e.target.innerText
	console.log(val)
	$('#marketSelect').val(val).prop("selected",true);
	$('#marketSelect').trigger("change")
}, false);

//코인 리스트 테이블 렌더링
var idCoinListRendered = false;
function printCoinList(){
	getMarkets('https://api.upbit.com/v1/market/all?isDetail=false')
		.then(function (markets){
			return new Promise(function(resolve, reject){
				//종목 테이블 생성
				let output = document.getElementById("output");
				for(let i = 0; i < markets.length; i++){
					let newCoinRow = document.createElement("tr");
					newCoinRow.id = markets[i];

					let marketCodeCol = document.createElement("td");
					marketCodeCol.innerHTML = markets[i];
					let tradePriceCol = document.createElement("td");
					tradePriceCol.innerHTML = '-';
					let changeRateCol = document.createElement("td");
					changeRateCol.innerHTML = '-';
					let changePriceCol = document.createElement("td");
					changePriceCol.innerHTML = '-';
					let accTradePriceCol = document.createElement("td");
					accTradePriceCol.innerHTML = '-';

					newCoinRow.appendChild(marketCodeCol);
					newCoinRow.appendChild(tradePriceCol);
					newCoinRow.appendChild(changeRateCol);
					newCoinRow.appendChild(changePriceCol);
					newCoinRow.appendChild(accTradePriceCol);

					output.appendChild(newCoinRow);
				}
				resolve(markets);
			});
		})
		.then(connectWS);
}