/******************************************************************
 * on-documnet-ready.js
 * 마지막 수정 일: 2022-05-24
 * SeungYeop Han, 32174842@dankook.ac.kr
 * 
 * 마켓 리스트 생성 및 기능 동기화       
 ******************************************************************/

/**
 * getMarkets(url) -> Promise()   ::=   resolve(markets)
 *                                      markets = ['KRW_BTC', ...]
 */
 function getMarkets(url){
  return new Promise(function(resolve, reject){
    let markets = [];

    $.get(url, function(data){
      for(let i = 0; i < data.length; i++){
        if(data[i]['market'].slice(0, 3) == 'KRW')
          markets.push(data[i]['market']);
      }

      resolve(markets);
    })
  });
}

/**
 * marketSelectOptions(markets) -> Promise    ::=   append option tags to marketSelect
 *                                                  marketSelect = DOM Object of SELECT(id: marketSelect)
 *                                                  resolve(marketSelect)
 */
function makeSelectOptions(markets){
  let marketSelect = document.getElementById('marketSelect');
  let options = [];
  let option;

  for(let i = 0; i < markets.length; i++){
    option = document.createElement('option');
    option.setAttribute('label', markets[i]);
    option.setAttribute('value', markets[i]);

    options.push(option);
    marketSelect.appendChild(option);
  }

  return new Promise(function(resolve, reject){
    resolve(marketSelect);
  })
}