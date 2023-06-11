/******************************************************************
 * plot-candle.js
 * 마지막 수정 일: 2022-05-24
 * SeungYeop Han, 32174842@dankook.ac.kr
 * 
 * 가상화폐 모의투자 서비스 메인 거래소 페이지의
 * 캔들 차트를 출력 및 갱신하는 자바스크립트 파일         
 ******************************************************************/


////////////////////////////////// Constants //////////////////////////////////
const UP_COLOR = '#da0000';
const DOWN_COLOR = '#0012da';





////////////////////////////////// Variables //////////////////////////////////
var candleChartDom = document.getElementById('candleChart');
var candleChart = echarts.init(candleChartDom);
var candleChartOption;
var candleDataContainer = {}





////////////////////////////////// Functions //////////////////////////////////
/**
 * calculateMA(dayCount, data) -> maArray        ::=    주어진 data에 대한 dayCount-이동평균 배열을 반환한다.
 */
 function calculateMA(dayCount, data) {
  var maArray = [];

  for (var i = 0, len = data.values.length; i < len; i++) {
    if(i < dayCount) {
        maArray.push('-');
        continue;
    }

    var sum = 0;
    for(var j = 0; j < dayCount; j++) {
        //put close(trade) prices
        sum += data.values[i - j][1];
    }
    maArray.push(+(sum / dayCount).toFixed(3));
}

return maArray;
}

/**
 * splitCandleData(data) -> { datetimedata: [...], values: [...], volumes: [...] },
 *        ::= data[]를 datetimeData[], values[], volumes[]로 분할하고, 객체로 감싸서 반환한다.
 */
function splitCandleData(data) {

  let datetimeData = [];
  let values = [];
  let volumes = [];

  let j = 0;
  for (let i = data.length-1; i >= 0; i--, j++) {
    //datetime
    datetimeData.push(data[i][0]);
    //ochl & volume 
    values.push(data[i].slice(1));
    //           idx    volume     opening       close     up  down
    volumes.push([j, data[i][5], data[i][1] > data[i][2] ? 1 : -1]);
  }

  return {
    datetimeData: datetimeData,   //[datetime, ...]
    values: values,               //[[o, c, h, l, v], ..., []]
    volumes: volumes,             //[[idx, vol, 1|-1], ..., []]
  };
}

/** 
 * drawCandleGraph(data) -> void    ::=   주어진 data를 이용하여 차트를 그린다.
 */
function drawCandleGraph(data){

  let candleChartData = splitCandleData(data);

  candleChart.setOption(candleChartOption = {
    animation: false,
    legend: {
      up: 0,
      left: 30,
      data: ['Candle', 'MA5', 'MA10', 'MA20', 'MA30', 'Volume']
    },
    tooltip: {
      trigger: 'axis',
      showContent: false,
      axisPointer: {
        type: 'cross'
      },
       borderWidth: 1,
       borderColor: '#ccc',
       padding: 10,
       textStyle: {
         color: '#000'
       },
       textStyle: {
         fontSize: 6,
       },
       position: function (pos, params, el, elRect, size) {
         const obj = {
           // top: 25,
           // left: 10
         };
         obj[['left', 'right'][+(pos[0] < size.viewSize[0] / 2)]] = 30;
         return obj;
       },
    },
    axisPointer: {
      link: [
        {
          xAxisIndex: 'all'
        }
      ],
      label: {
        backgroundColor: '#777'
      }
    },
    toolbox: {
      show: false,
      feature: {
        dataZoom: {
          yAxisIndex: false
        },
        brush: {
          type: ['lineX', 'clear']
        }
      }
    },
    brush: {
      xAxisIndex: 'all',
      brushLink: 'all',
      outOfBrush: {
        colorAlpha: 0.1
      }
    },
    visualMap: {
      show: false,
      seriesIndex: 5,
      dimension: 2,
      pieces: [
        {
          value: 1,
          color: DOWN_COLOR
        },
        {
          value: -1,
          color: UP_COLOR
        }
      ]
    },
    grid: [
      {
        left: 5,
        top: 30,
        right: 100,
        bottom: "25%",
      },
      {
        left: 5,
        top: "75%",
        right: 100,
        bottom: 0,
        height: "15%",
      }
    ],
    xAxis: [
      //candle chart's
      {
        type: 'category',
        data: candleChartData.datetimeData,
        boundaryGap: true,
        position: 'top',
        axisLine: { show: false, onZero: false },
        axisTick: { show: false },
        axisLabel: { show: false },
        splitLine: { show: false },
        min: 'dataMin',
        max: 'dataMax',
        axisPointer: {
          z: 100
        }
      },
      //volume chart's
      {
        type: 'category',
        gridIndex: 1,
        data: candleChartData.datetimeData,
        boundaryGap: true,
        axisLine: { onZero: false },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { show: true },
        min: 'dataMin',
        max: 'dataMax'
      }
    ],
    yAxis: [
      //캔들
      {
        scale: true,
        position: 'right',
        axisLine: { show: true, onZero: false },
        axisLabel: { show: true },
      },
      //볼륨차트
      {
        scale: true,
        gridIndex: 1,
        splitNumber: 2,
        position: 'right',
        axisLine: { show: true, onZero: false },
        axisLabel: { show: false },
        axisTick: { show: false },
        splitLine: { show: false }
      }
    ],
    dataZoom: [
      {
        type: 'inside',
        xAxisIndex: [0, 1],
        start: 99.999999,
        end: 100,
        minValueSpan: 20,
        maxValueSpan: 250,    //category인 경우 최대 카테고리 수, time axis인 경우 3600 * 24 * 1000 * 5가 최대 5일 이라는 의미
      },
      //볼륨차트
      {
        type: 'inside',
        show: false,
        xAxisIndex: [0, 1],
      }
    ],
    series: [
      {
        name: 'Candle',
        type: 'candlestick',
        data: candleChartData.values,
        itemStyle: {
          color: UP_COLOR,
          color0: DOWN_COLOR,
          borderColor: undefined,
          borderColor0: undefined,
        },
      },
      {
        name: 'MA5',
        type: 'line',
        showSymbol: false,  //false: no ticks
        data: calculateMA(5, candleChartData),
        smooth: true,
        lineStyle: {
          opacity: 0.5
        }
      },
      {
        name: 'MA10',
        type: 'line',
        showSymbol: false,  //false: no ticks
        data: calculateMA(10, candleChartData),
        smooth: true,
        lineStyle: {
          opacity: 0.5
        }
      },
      {
        name: 'MA20',
        type: 'line',
        showSymbol: false,  //false: no ticks
        data: calculateMA(20, candleChartData),
        smooth: true,
        lineStyle: {
          opacity: 0.5
        }
      },
      {
        name: 'MA30',
        type: 'line',
        showSymbol: false,  //false: no ticks
        data: calculateMA(30, candleChartData),
        smooth: true,
        lineStyle: {
          opacity: 0.5
        }
      },
      {
        name: 'Volume',
        type: 'bar',
        xAxisIndex: 1,
        yAxisIndex: 1,
        data: candleChartData.volumes
      }
    ]
  });

  candleChart.dispatchAction(
    {
      type: 'brush',
      command: 'clear',
      areas: [],
    },
    // {
    //   type: 'datazoom',
    //   //command: 'clear',
    //   areas: [],
    // },
  );

  return new Promise(function(resolve, reject){
    resolve(candleChartOption);
  })
}

/**
 * storeCandleData(data: [[]])  ->  Promise     ::=     
 */
function storeCandleData(data){
  let market = $('#marketSelect option:selected').val();
  let unit = $('#candleUnitSelect option:selected').val();

  candleDataContainer[market + "_" + unit] = data;

  return new Promise(function(resolve, reject) {
    resolve(data);
  });
}

/**
 * appendUpToDateData(data: [[]])  ->  Promise     ::=     
 */
async function appendUpToDateData(newData){
  let market = $('#marketSelect option:selected').val();
  let unit = $('#candleUnitSelect option:selected').val();
  let url = 'https://api.upbit.com/v1/candles/' + unit + '?market=' + market + '&count=200&to=';

  return new Promise(async function(resolve, reject){

    let pos = 0;
    let to = candleDataContainer[market + '_' + unit][0][0]

    for(; newData[pos][0] != to; pos++)
      console.log(pos);

    //데이터를 더 가져와야 함
    if(newData[pos][0] == undefined){
      url = url + to.toISOString();
      let nextData = await getCandleData(url);

      candleDataContainer[market + "_" + unit] = [
        ...newData,
        ...appendUpToDateData(nextData)
      ];
      resolve(candleDataContainer[market + "_" + unit]);
    }
    else{
      candleDataContainer[market + "_" + unit].splice(0, 1);
      candleDataContainer[market + "_" + unit] = [
        ...newData.slice(0, pos+1), 
        ...candleDataContainer[market + "_" + unit]
      ];
      //여기서 resolve되면, 위 if 문의 //here가 호출된다.
      resolve(candleDataContainer[market + "_" + unit])
    }
  });
}

/**
 * getCandleData(url) -> Promise    ::=   주어진 업비트 url로 부터 json 형식의 캔들 데이터를 수신하는 비동기적 콜백 함수
 *                                  수신한 rawData를 [[t, o, c, h, l, v], ..., [...]] 형식으로 변환해서 resolve로 넘긴다.
 */
function getCandleData(url){
  return new Promise(function(resolve, reject) {
    $.get(url, function(response){
      if(response){
        //필요한 필드들 만을 추출하여 적절한 형태로 변환
        let data = [];

        for(let i = 0; i < 200; i++){
          if(response[i] == undefined)
            break;
          else
            data.push([
              response[i]['candle_date_time_kst'],
              response[i]['opening_price'],
              response[i]['trade_price'],
              response[i]['high_price'],
              response[i]['low_price'],
              response[i]['candle_acc_trade_volume'],
            ]);
        }
        resolve(data);
      }
      else
        reject(new Error("Request from " + url + " is FAILED!!!"));
    });
  });
}



////////////////////////////////// Event Handlers //////////////////////////////////
var tradePriceOfSelectedCoin = 0;
//코인 유형 또는 기간 변경 시   ->    선택된 코인 유형과 캔들 기간의 차트를 출력한다.
$('select').change(function(){
  let market = $('#marketSelect option:selected').val();
  let unit = $('#candleUnitSelect option:selected').val();
  let url = 'https://api.upbit.com/v1/candles/' + unit + '?market=' + market + '&count=200&to='

  candleChart.showLoading();
  if((market + "_" + unit) in candleDataContainer){
    console.log("Already loaded!");
    drawCandleGraph(candleDataContainer[market + "_" + unit]);
  }
  else{
    getCandleData(url)
    .then(storeCandleData)
    .then(drawCandleGraph);
  }
  candleChart.hideLoading();
});

//차트를 가장 앞으로 스크롤/줌 시    ->    만약 최신 데이터가 있으면 반영하여 그래프를 그린다.
//차트를 가장 뒤로 스크롤/줌 시   -> 만약 표시할 과거 데이터가 남아 있으면 반영하여 그래프를 그린다.
candleChart.on('datazoom', function(params){

  let market = $('#marketSelect option:selected').val();
  let unit = $('#candleUnitSelect option:selected').val();
  let url = 'https://api.upbit.com/v1/candles/' + unit + '?market=' + market + '&count=200&to='

  let start = params['batch'][0]['start']
  let end = params['batch'][0]['end']
  let preLen = candleDataContainer[market + '_' + unit].length;
  let numOfCandle = preLen * ((end-start) / 100);   //기존의 비율을 유지하기 위한 필드, 캔들이 1개인 경우는 없으므로, 항상 end != start이다  
  
  //아래의 두 경우에 동시에 해당할 수 있다.(start == 0 && end == 100)
  //차트 가장 뒤로 스크롤/줌 시
  if(start == 0){
    console.log("Previous");

    let from = new Date(candleDataContainer[market + "_" + unit].at(-1)[0])
    url = url + from.toISOString();

    getCandleData(url)
      .then(function(newData){
        return new Promise(function(resolve, reject){
          candleDataContainer[market + "_" + unit].push(...newData);
          resolve(candleDataContainer[market + "_" + unit]);
        });
      })
      .then(drawCandleGraph)
      .then(function(candleChartOption){
        let postLen = candleDataContainer[market + '_' + unit].length;
        let newStart = (postLen - preLen) / postLen * 100;
        let newEnd = 100 * numOfCandle / postLen + newStart;

        candleChart.setOption(candleChartOption = {
          dataZoom: [
            {
              type: 'inside',
              xAxisIndex: [0, 1],
              start: newStart,
              end: newEnd,
              minValueSpan: 20,
              maxValueSpan: 100,    //category인 경우 최대 카테고리 수, time axis인 경우 3600 * 24 * 1000 * 5가 최대 5일 이라는 의미
            },
            //볼륨차트
            {
              type: 'inside',
              show: false,
              xAxisIndex: [0, 1],
            }
          ],
        })
      });
  }

  //차트 가장 앞으로 스크롤/줌 시
  if(end == 100){
    console.log("UpToDate")

    //url에서 to 파라미터를 생략해야만 최신의 캔들을 가져올 수 있다.
    getCandleData(url)
      .then(appendUpToDateData)
      .then(drawCandleGraph)
      .then(function(candleChartOption){
        let postLen = candleDataContainer[market + '_' + unit].length;
        let newEnd = 100;
        let newStart = 100 - (numOfCandle / postLen * 100);

        candleChart.setOption(candleChartOption = {
          dataZoom: [
            {
              type: 'inside',
              xAxisIndex: [0, 1],
              start: newStart,
              end: newEnd,
              minValueSpan: 20,
              maxValueSpan: 100,    //category인 경우 최대 카테고리 수, time axis인 경우 3600 * 24 * 1000 * 5가 최대 5일 이라는 의미
            },
            //볼륨차트
            {
              type: 'inside',
              show: false,
              xAxisIndex: [0, 1],
            }
          ],
        })
      });
  }
})

//차트에 마우스를 올릴 시   ->    마우스의 x좌표에 해당하는 캔들의 정보를 표시
candleChart.on('highlight', function(params){

  let candleChartOption = candleChart.getOption();
  let highlighted = params['batch'];

  if(highlighted != undefined){
    let dataIndex = highlighted[0]['dataIndex']
    let series = candleChartOption['series']
    let candle = series[0]['data'][dataIndex];   //candle: [o, c, h, l, v]
    let ma = [
      series[1]['data'][dataIndex],   //MA5
      series[2]['data'][dataIndex],   //MA10
      series[3]['data'][dataIndex],   //MA20
      series[4]['data'][dataIndex],   //MA30
    ];
    let volume = series[5]['data'][dataIndex].slice(1);   //[vol, +|-]

    dom = document.getElementById('ochlv')
    .innerText = 'O: ' + candle[0] +  ' C: ' + candle[1] + ' H: ' + candle[2] + ' L: ' + candle[3] + ' V: ' + candle[4];
    dom = document.getElementById('mas')
    .innerText = 'MA5:' + ma[0] + ' MA10: ' + ma[1] + ' MA20: ' + ma[2] + ' MA30: ' + ma[3];
  }
  
})

//브라우저 크기 변경 시   ->    차트의 크기도 창의 크기에 맞춤(반응형)
$(function () {
  $(window).on('resize', resize);
  $(".menu-toggle").on('click', resize);
  function resize() {
      setTimeout(function() {
          candleChart.resize();
      }, 200);
  }
});


//toFront버튼 클릭 시   ->    캔들 차트를 갱신 후 가장 앞으로 이동
document.getElementById("toFront").onclick = function(){
  console.log("UpToDate")

  let market = $('#marketSelect option:selected').val();
  let unit = $('#candleUnitSelect option:selected').val();
  let url = 'https://api.upbit.com/v1/candles/' + unit + '?market=' + market + '&count=200&to='

  let start = candleChartOption.dataZoom[0].start;
  let end = candleChartOption.dataZoom[0].end;
  let span = end - start;

  let newEnd = 100;
  let newStart = newEnd - span;

  //url에서 to 파라미터를 생략해야만 최신의 캔들을 가져올 수 있다.
  getCandleData(url)
    .then(appendUpToDateData)
    .then(drawCandleGraph)
    .then(function(candleChartOption){
      candleChart.setOption(candleChartOption = {
        dataZoom: [
          {
            type: 'inside',
            xAxisIndex: [0, 1],
            start: newStart,
            end: newEnd,
            minValueSpan: 20,
            maxValueSpan: 100,    //category인 경우 최대 카테고리 수, time axis인 경우 3600 * 24 * 1000 * 5가 최대 5일 이라는 의미
          },
          //볼륨차트
          {
            type: 'inside',
            show: false,
            xAxisIndex: [0, 1],
          }
        ],
      })
    });
}



////////////////////////////////// Main //////////////////////////////////