package SeungYeop_Han.Cosi.agents;

import SeungYeop_Han.Cosi.domains.Coin;
import SeungYeop_Han.Cosi.repositories.CoinRepository;
import lombok.Getter;
import lombok.Setter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TradePriceStreamer extends WebSocketClient {

    ///// 속성 /////
    // 각 종목별 현재가를 [Key: 종목코드, Value: 현재가] 쌍으로 저장하는 변수
    private static final Map<String, Double> tradePrices = new HashMap<>();

    StringBuilder request;

    ///// 생성자 /////
    public TradePriceStreamer(URI serverUri) {
        super(serverUri);
    }

    ///// 메소드 재정의: WebSocketClient /////
    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("[TradePriceStreamer] 새로운 통신이 연결되었습니다.");
        System.out.println(request);
        send(request.toString());
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("[TradePriceStreamer] 통신 연결이 해제되었습니다. code: " + code + "사유: " + reason);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("[TradePriceStreamer] 수신한 메시지: " + message);
    }

    @Override
    public void onMessage(ByteBuffer message) {
        String response = new String(message.array(), message.position(), message.limit());
        JSONObject jsonObject = new JSONObject(response);

        tradePrices.put((String) jsonObject.get("code"), jsonObject.getDouble("trade_price"));
    }

    @Override
    public void onError(Exception e) {
        System.err.println("[TradePriceStreamer] 오류가 발생했습니다. 사유: " + e);;
    }

    ///// 사용자 정의 메소드 /////
    /**
     * 주어진 marketCode와 일치하는 코인 종목의 현재가를 반환합니다.
     * @param marketCode String: 조회하고자 하는 종목의 코드
     * @return Double: 현재가, 만약 주어진 종목코드에 해당하는 종목이 존재하지 않으면 null을 반환합니다.
     */
    public static Double getTradePrice(String marketCode) {
        return tradePrices.get(marketCode);
    }

    /**
     * coinReository에 저장되어있는 모든 코인의 마켓 코드를 이용하여 웹소켓 요청 메시지를 초기화합니다.
     * @param coinRepository CoinRepository: Coin 엔티티에 대한 Repository 객체
     */
    public void initRequest(CoinRepository coinRepository) {
        List<Coin> marketCodes = coinRepository.findAll();
        for (int i = 0; i < marketCodes.size(); i++) {
            tradePrices.put((String) marketCodes.get(i).getCode(), 0.0);
        }

        //request 메시지 생성, 형식은 아래와 같습니다.(개행은 가독성을 위한 것일 뿐 실제로는 한 줄 입니다.)
        //  "[
        //      {"ticket": "MyTicket"},
        //      {
        //          "type": "ticker",
        //          "codes" :["KRW-CRO","KRW-ENJ",...,"KRW-STRK","KRW-ARK"],
        //          "isOnlyRealtime": "true"
        //      }
        //  ]"
        request = new StringBuilder("[{\"ticket\": \"MyTicket\"}, {\"type\": \"ticker\", \"codes\": [");
        tradePrices.forEach((k, v) -> {
            request.append("\"" + k + "\",");
        });
        request.deleteCharAt(request.lastIndexOf(","));
        request.append("], \"isOnlyRealtime\": \"true\"}]");
    }
}
