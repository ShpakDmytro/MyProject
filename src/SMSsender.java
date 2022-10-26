import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class SMSsender {
    public void sendSms(String number, String body) {

        String bodyRequest = "api_key=002c2de9&api_secret=w8m0GvlXyXLI6T8b&" +
                "from=ShadInc&to=" + number + "&text=" + body;

        try {
            String result = sendPOST("https://rest.nexmo.com/sms/json", bodyRequest);
            System.out.println(result);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    private static String sendPOST(String url, String bodyRequest) throws IOException {

        String result = "";
        HttpPost post = new HttpPost(url);
        post.addHeader("Host","rest.nexmo.com");
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");

        try {
            post.setEntity(new StringEntity(bodyRequest));
        } catch (UnsupportedEncodingException e) {
            System.out.println(e);
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {
            result = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            System.out.println(e);
        }

        return result;
    }
}
