package controllers;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class SMSSender {
    public void sendSms(String number, String body) {

        String bodyRequest = "api_key=002c2de9&api_secret=w8m0GvlXyXLI6T8b&" +
                "from=ShadInc&to=" + number + "&text=" + body;

        try {
            CloseableHttpResponse result = sendPOST("https://rest.nexmo.com/sms/json", bodyRequest);
            System.out.println(result.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private CloseableHttpResponse sendPOST(String url, String bodyRequest) throws IOException {

        HttpPost post = new HttpPost(url);
        post.addHeader("Host", "rest.nexmo.com");
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");

        try {
            post.setEntity(new StringEntity(bodyRequest));
        } catch (UnsupportedEncodingException e) {
            System.out.println(e);
        }

        CloseableHttpResponse response = null;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new IOException("Wrong work with request");
            }
        } catch (IOException e) {
            System.out.println(e);
        }

        return response;
    }
}
