package SeungYeop_Han.Cosi.agents;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpConnection {

    /**
     * 주어진 URL 주소로 GET 요청을 보낸 후, 만약 JSON 형식의 데이터를 수신했다면 해당 데이터를 반환합니다.
     * @param urlStr String: 요청을 보낼 url 주소 문자열
     * @return JSONArray: 수신한 JSON 데이터
     */
    public static JSONArray getJSONArray(String urlStr){
        try {
            URL url = new URL(urlStr);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("auth", "myAuth");
            httpURLConnection.setDoOutput(true);

            BufferedReader br
                    = new BufferedReader(
                            new InputStreamReader(
                                    httpURLConnection.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line = null;

            //한 줄 씩 모두 읽습니다.
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            JSONArray jsonArray = new JSONArray(sb.toString());
            return jsonArray;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
