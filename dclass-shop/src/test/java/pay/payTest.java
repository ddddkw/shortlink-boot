package pay;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.example.ShopApplication;
import org.example.config.WeChatPayApi;
import org.example.config.WechatPayConfig;
import org.example.utils.CommonUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShopApplication.class)
@Slf4j
public class payTest {

    @Autowired
    private WechatPayConfig wechatPayConfig;

    @Autowired
    private CloseableHttpClient wechatPayClient;

    @Test
    public void testPayment() throws JSONException, IOException {
        String outTradeNo = CommonUtil.getStringNumRandom(32);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mchid",wechatPayConfig.getMchId());
        jsonObject.put("out_trade_no",outTradeNo);
        jsonObject.put("appid",wechatPayConfig.getWxPayAppid());
        jsonObject.put("description","测试");
        jsonObject.put("notify_url",wechatPayConfig.getCallBackUrl());

        JSONObject amountObj = new JSONObject();
        amountObj.put("total",100);
        amountObj.put("currency","CNY");

        jsonObject.put("amount",amountObj);
        jsonObject.put("attach","{\"accountNo\":"+888+"}");

        String body = jsonObject.toString();

        log.info("请求参数：{}",body);

        StringEntity entity = new StringEntity(body,"utf-8");
        entity.setContentType("application/json");

        HttpPost httpPost = new HttpPost(WeChatPayApi.NATIVE_ORDER);
        httpPost.setHeader("Accept","application/json");
        httpPost.setEntity(entity);


        try(CloseableHttpResponse httpResponse = wechatPayClient.execute(httpPost);) {
            // 响应码
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            // 响应体
            String response = EntityUtils.toString(httpResponse.getEntity());
            log.info(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNativeQuery() throws JSONException, IOException {
        String outTradeNo = CommonUtil.getStringNumRandom(32);
        String url = String.format(WeChatPayApi.NATIVE_QUERY,outTradeNo,wechatPayConfig.getWxPayAppid());

        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("Accept","application/json");
        try(CloseableHttpResponse httpResponse = wechatPayClient.execute(httpget);) {
            // 响应码
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            // 响应体
            String response = EntityUtils.toString(httpResponse.getEntity());
            log.info(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testNativeClose() throws JSONException, IOException {
        String outTradeNo = CommonUtil.getStringNumRandom(32);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mchid",wechatPayConfig.getMchId());

        String body = jsonObject.toString();

        log.info("请求参数：{}",body);

        StringEntity entity = new StringEntity(body,"utf-8");
        entity.setContentType("application/json");

        String url = String.format(WeChatPayApi.NATIVE_CLOSE,outTradeNo);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept","application/json");
        httpPost.setEntity(entity);


        try(CloseableHttpResponse httpResponse = wechatPayClient.execute(httpPost);) {
            // 响应码
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            // 响应体
            String response = EntityUtils.toString(httpResponse.getEntity());
            log.info(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNativeRefund() throws JSONException, IOException {
        String outTradeNo = CommonUtil.getStringNumRandom(32);
        String refundNo = CommonUtil.getStringNumRandom(32);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no",outTradeNo);
        jsonObject.put("out_refund_no",refundNo);
        jsonObject.put("reason","商品已售完");
        jsonObject.put("notify_url",wechatPayConfig.getCallBackUrl());

        JSONObject amountObj = new JSONObject();
        amountObj.put("total",100);
        amountObj.put("currency","CNY");
        jsonObject.put("refund",10);
        jsonObject.put("amount", amountObj);
        String body = jsonObject.toString();

        log.info("请求参数：{}",body);

        StringEntity entity = new StringEntity(body,"utf-8");
        entity.setContentType("application/json");

        String url = String.format(WeChatPayApi.NATIVE_REFUND,outTradeNo);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept","application/json");
        httpPost.setEntity(entity);


        try(CloseableHttpResponse httpResponse = wechatPayClient.execute(httpPost);) {
            // 响应码
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            // 响应体
            String response = EntityUtils.toString(httpResponse.getEntity());
            log.info(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNativeRefundQuery() throws JSONException, IOException {
        String refundNo = CommonUtil.getStringNumRandom(32);

        String url = String.format(WeChatPayApi.NATIVE_REFUND_QUERY,refundNo);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept","application/json");


        try(CloseableHttpResponse httpResponse = wechatPayClient.execute(httpGet);) {
            // 响应码
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            // 响应体
            String response = EntityUtils.toString(httpResponse.getEntity());
            log.info(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
