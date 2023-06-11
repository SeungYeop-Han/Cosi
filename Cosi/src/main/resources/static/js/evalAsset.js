var seed = Number(document.getElementById("seed").innerText)

function evalAsset(){
    /*요약정보*/
    let assetValueCol = document.getElementById("assetValue");
    let assetValue = 0;         //총 보유 자산 = 보유시드 + 총평가금액
    let totalBuyPriceCol = document.getElementById("totalBuyPrice");
    let totalBuyPrice = 0;      //총 매수 금액 = ∑(매수금액)
    let totalPLCol = document.getElementById("totalPL");
    let totalPL = 0;            //총 평가 손익 = ∑(평가손익)
    let totalMarketValueCol = document.getElementById("totalMarketValue");
    let totalMarketValue = 0;   //총 평가 금액 = ∑(평가금액)
    let totalPLRateCol = document.getElementById("totalPLRate");
    let totalPLRate = 0;        //총 평가 수익률 = (평가손익 / 매수금액) * 100%

    /*코인 별*/
    let amountCol;
    let amount = 0;             //보유수량 = 주어짐
    let avgBuyPriceCol
    let avgBuyPrice = 0;        //매수평균가 = 주어짐
    let buyPriceCol
    let buyPrice = 0;           //매수금액 = 주어짐
    let marketValueCol;
    let marketValue = 0;        //평가금액 = 현재가 * 보유수량
    let PLCol;
    let PL = 0;                 //평가손익 = 평가금액 - 매수금액
    let PLRateCol;
    let PLRate = 0;             //평가수익률 = (평가손익 / 매수금액) * 100%

    let marketCodeCol;
    let marketCode;         //마켓코드
    let tradePrice;         //현재가

    let coins = document.getElementById("coins");
    let currentRow = coins.firstElementChild;
    while( currentRow != null ){
        //마켓코드
        marketCodeCol = currentRow.firstElementChild;
        marketCode = marketCodeCol.innerText;

        //보유 수량
        amountCol = marketCodeCol.nextElementSibling;
        amount = Number(amountCol.innerText);

        //보유수량 검사
        if (amount == 0.0) {
            temp = currentRow;
            currentRow = currentRow.nextElementSibling
            coins.removeChild(temp)
            continue;
        }

        //매수 평균가
        avgBuyPriceCol = amountCol.nextElementSibling;
        avgBuyPrice = Number(avgBuyPriceCol.innerText);

        //매수 금액
        buyPriceCol = avgBuyPriceCol.nextElementSibling;
        buyPrice = Number(buyPriceCol.innerText);

        //현재가
        tradePrice = Number(document.getElementById(marketCode)
            .firstElementChild
            .nextElementSibling
            .innerText);

        //평가금액
        marketValueCol = buyPriceCol.nextElementSibling;
        marketValue = amount * tradePrice;
        marketValueCol.innerText = marketValue.toFixed(2) + "KRW";

        //평가손익
        PLCol = marketValueCol.nextElementSibling;
        PL = marketValue - buyPrice;
        PLCol.innerText = PL.toFixed(2) + "KRW";

        //평가수익률
        PLRateCol = PLCol.nextElementSibling;
        PLRate = (PL / buyPrice) * 100
        PLRateCol.innerText = PLRate.toFixed(2) + "%";

        //총 매수 금액 누적
        totalBuyPrice += buyPrice;

        //총 평가 금액 누적
        totalMarketValue += marketValue;

        //총 평가 손익 누적
        totalPL += PL;

        //다음 행으로
        currentRow = currentRow.nextElementSibling;
    }
    //총 매수 금액
    totalBuyPriceCol.innerText = totalBuyPrice.toFixed(2) + " KRW";

    //총 평가 금액
    totalMarketValueCol.innerText = totalMarketValue.toFixed(2) + " KRW";

    //총 평가 손익
    totalPLCol.innerText = totalPL.toFixed(2) + " KRW";

    //총 평가 수익률
    totalPLRate = (totalPL / totalBuyPrice) * 100;
    if(Number.isNaN(totalPLRate)){
        totalPLRateCol.innerText = "0.0 %"
    }
    else{
        totalPLRateCol.innerText = totalPLRate.toFixed(2) + " %";
    }

    //총 보유 자산
    assetValue = seed + totalMarketValue;
    assetValueCol.innerText = assetValue.toFixed(2) + " KRW";

    //보유 시드
    let seedCol = document.getElementById("seed");
    seedCol.innerText = seed + " KRW";
}