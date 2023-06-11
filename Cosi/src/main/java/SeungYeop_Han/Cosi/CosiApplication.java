package SeungYeop_Han.Cosi;

import SeungYeop_Han.Cosi.DTOs.RegistrationRequest;
import SeungYeop_Han.Cosi.agents.HttpConnection;
import SeungYeop_Han.Cosi.agents.TradePriceStreamer;
import SeungYeop_Han.Cosi.domains.Coin;
import SeungYeop_Han.Cosi.domains.Member;
import SeungYeop_Han.Cosi.domains.OrderState;
import SeungYeop_Han.Cosi.repositories.CoinRepository;
import SeungYeop_Han.Cosi.repositories.MemberRepository;
import SeungYeop_Han.Cosi.repositories.OrderRepository;
import SeungYeop_Han.Cosi.repositories.WalletRepository;
import SeungYeop_Han.Cosi.services.BuyOrderConcluder;
import SeungYeop_Han.Cosi.services.MemberService;
import SeungYeop_Han.Cosi.services.RegistrationService;
import SeungYeop_Han.Cosi.services.SellOrderConcluder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class CosiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CosiApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(
			ApplicationContext applicationContext,
			MemberRepository memberRepository,
			CoinRepository coinRepository,
			OrderRepository orderRepository,
			WalletRepository walletRepository,
			RegistrationService registrationService,
			MemberService memberService,
			BuyOrderConcluder buyOrderConcluder,
			SellOrderConcluder sellOrderConcluder
	){
		return args -> {

			//업비트 REST API를 이용한 마켓 코드 조회
			JSONArray marketCodes = HttpConnection.getJSONArray("https://api.upbit.com/v1/market/all?isDetail=false");
			JSONArray KRWMarketCodes = new JSONArray();

			//원화(KRW) 마켓을 제외한 나머지 제거
			for (int i = 0; i < marketCodes.length(); i++) {
				JSONObject market = marketCodes.getJSONObject(i);

				if(((String) market.get("market")).matches(".*KRW.*")){
					KRWMarketCodes.put(market);
				}
			}

			//원화 마켓 코인 삽입
			List<Coin> coins = new ArrayList<>();
			int numOfKRWMarketCodes = KRWMarketCodes.length();
			for(int i = 0; i < numOfKRWMarketCodes; i++){
				if(coinRepository.findCoinByCode((String) KRWMarketCodes.getJSONObject(i).get("market")).isPresent()){
					continue;
				}
				Coin coin = new Coin(
						(String) KRWMarketCodes.getJSONObject(i).get("market"),
						(String) KRWMarketCodes.getJSONObject(i).get("korean_name")
				);
				coins.add(coin);
			}
			if( ! coins.isEmpty()){
				coinRepository.saveAll(coins);
			}

			//현재가를 실시간으로 업데이트 해주는 새로운 비동기 스레드 생성
			TradePriceStreamer streamer = new TradePriceStreamer(new URI("wss://api.upbit.com/websocket/v1"));
			streamer.initRequest(coinRepository);
			streamer.connect();

			//코인 별로 매수/매도 체결자 스레드를 생성 후, 미 체결 주문 내역을 코인 별로 읽어 오면서 삽입한다.
			try {
				//TODO: 페이징
				orderRepository.findAll().forEach(o -> {
					if (o.getOrderState().equals(OrderState.UNFILLED)) {
						if (o.getIsBuying()) {
							buyOrderConcluder.pushOrder(o);
						} else {
							sellOrderConcluder.pushOrder(o);
						}
					}
				});

			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			//테스트용 계정 생성
			try {
				RegistrationRequest req = new RegistrationRequest("tester", "street4727@naver.com", "q7w8e9a4s5d6!");
				registrationService.register(req);
				Member tester = memberRepository.findByEmail("street4727@naver.com").get();
				memberService.enableMember(tester.getEmail());
			} catch (Exception e) {
				System.out.println("테스트 계정 생성 중 예외 발생: " + e.getMessage());
			}


		};
	}

}
