package org.example.component;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.example.config.WeChatPayApi;
import org.example.config.WechatPayConfig;
import org.example.utils.CommonUtil;
import org.example.vo.PayInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WechatPayStrategy implements PayStrategy{

    @Autowired
    private WechatPayConfig wechatPayConfig;

    @Autowired
    private CloseableHttpClient wechatPayClient;

    @Override
    public String unifiedOrder(PayInfoVO payInfoVO) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        //支付订单过期时间
        String timeExpire= sdf.format(new Date(System.currentTimeMillis()+payInfoVO.getOrderPayTimeOutMills()));

        Map payObject = new HashMap();
        payObject.put("mchid",wechatPayConfig.getMchId());
        payObject.put("out_trade_no",payInfoVO.getOutTradeNo());
        payObject.put("appid",wechatPayConfig.getWxPayAppid());
        payObject.put("description",payInfoVO.getDescription());
        payObject.put("notify_url",wechatPayConfig.getCallBackUrl());
        payObject.put("time_expire", timeExpire);
        Map amountObj = new HashMap();
        // 数据库存储时double类型 比如 100.99
        int amount = payInfoVO.getPayFee().multiply(BigDecimal.valueOf(100)).intValue();
        amountObj.put("total",amount);
        amountObj.put("currency","CNY");

        payObject.put("amount",amountObj);
        payObject.put("attach","{\"accountNo\":"+payInfoVO.getAccountNo()+"}");

        String body = payObject.toString();

        log.info("请求参数：{}",body);

        StringEntity entity = new StringEntity(body,"utf-8");
        entity.setContentType("application/json");

        HttpPost httpPost = new HttpPost(WeChatPayApi.NATIVE_ORDER);
        httpPost.setHeader("Accept","application/json");
        httpPost.setEntity(entity);

        String result = "";
        try(CloseableHttpResponse httpResponse = wechatPayClient.execute(httpPost);) {
            // 响应码
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            // 响应体
            String responseStr = EntityUtils.toString(httpResponse.getEntity());
            log.info("下单响应码：{}，响应体：{}",responseCode,responseStr);
            if (responseCode == HttpStatus.SC_OK) {
                JSONObject jsonObject = JSONObject.parseObject(responseStr);
                if (jsonObject.containsKey("code_url"))
                result = jsonObject.getString("code_url");
            } else {
                log.error("下单响应失败：{}，响应体：{}",responseCode,responseStr);
            }
        } catch (Exception e) {
            log.error("微信支付响应异常：{}",e);
        }
        return result;
    }

    @Override
    public String refund(PayInfoVO payInfoVO) {
        String outTradeNo = CommonUtil.getStringNumRandom(32);
        String url = String.format(WeChatPayApi.NATIVE_QUERY,outTradeNo,wechatPayConfig.getWxPayAppid());

        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("Accept","application/json");
        String result = "";
        try(CloseableHttpResponse httpResponse = wechatPayClient.execute(httpget);) {
            // 响应码
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            // 响应体
            String responseStr = EntityUtils.toString(httpResponse.getEntity());
            log.info("查询响应码：{}，响应体：{}",responseCode,responseStr);
            if (responseCode == HttpStatus.SC_OK) {
                JSONObject jsonObject = JSONObject.parseObject(responseStr);
                if (jsonObject.containsKey("trade_state"))
                    result = jsonObject.getString("trade_state");
            } else {
                log.error("查询响应失败：{}，响应体：{}",responseCode,responseStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询支付状态
     * @param payInfoVO
     * @return
     */
    @Override
    public String queryPayStatus(PayInfoVO payInfoVO) {
        String url = String.format(WeChatPayApi.NATIVE_QUERY,payInfoVO.getOutTradeNo(),wechatPayConfig.getWxPayAppid());

        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("Accept","application/json");
        String result = "";
        try(CloseableHttpResponse httpResponse = wechatPayClient.execute(httpget);) {
            // 响应码
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            // 响应体
            String responseStr = EntityUtils.toString(httpResponse.getEntity());
            log.info("查询响应码：{}，响应体：{}",responseCode,responseStr);
            if (responseCode == HttpStatus.SC_OK) {
                JSONObject jsonObject = JSONObject.parseObject(responseStr);
                if (jsonObject.containsKey("trade_state"))
                    result = jsonObject.getString("trade_state");
            } else {
                log.error("查询响应失败：{}，响应体：{}",responseCode,responseStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String closeOrder(PayInfoVO payInfoVO) {
        Map payObject = new HashMap();
        payObject.put("mchid",wechatPayConfig.getMchId());

        String body = payObject.toString();

        log.info("请求参数：{}",body);

        StringEntity entity = new StringEntity(body,"utf-8");
        entity.setContentType("application/json");

        String url = String.format(WeChatPayApi.NATIVE_CLOSE,payInfoVO.getOutTradeNo());
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept","application/json");
        httpPost.setEntity(entity);
        String result = "";
        try(CloseableHttpResponse httpResponse = wechatPayClient.execute(httpPost)) {
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            log.info("关闭订单响应码：{}",responseCode);
            if (responseCode == HttpStatus.SC_NO_CONTENT) {
                result = "CLOSE_SUCCESS";
            } else {
                log.error("关闭订单响应失败：{}",responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
