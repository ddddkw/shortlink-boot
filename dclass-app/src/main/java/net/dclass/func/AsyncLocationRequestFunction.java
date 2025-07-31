package net.dclass.func;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.dclass.model.ShortLinkWideDO;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 异步将ip映射为实际的地域
 **/
@Slf4j
public class AsyncLocationRequestFunction extends RichAsyncFunction<ShortLinkWideDO,String> {

    private static final String IP_PARSE_URL = "https://restapi.amap.com/v3/ip?ip=%s&output=json&key=b33e135f674b0c27c67273db13c78177";

    private CloseableHttpAsyncClient httpAsyncClient;

    @Override
    public void open(Configuration parameters) throws Exception {

        this.httpAsyncClient = createAsyncHttpClient();
    }

    @Override
    public void close() throws Exception {
        if(httpAsyncClient!=null){
            httpAsyncClient.close();
        }
    }

    @Override
    public void asyncInvoke(ShortLinkWideDO shortLinkWideDO, ResultFuture<String> resultFuture) throws Exception {


        String ip = shortLinkWideDO.getIp();
        String url = String.format(IP_PARSE_URL,ip);
        HttpGet httpGet = new HttpGet(url);
        log.info("补全后的url：{}",url);
        Future<HttpResponse> future = httpAsyncClient.execute(httpGet, null);


        CompletableFuture<ShortLinkWideDO> completableFuture = CompletableFuture.supplyAsync(new Supplier<ShortLinkWideDO>() {

            @Override
            public ShortLinkWideDO get() {

                try {
                    HttpResponse response = future.get();
                    int statusCode = response.getStatusLine().getStatusCode();
                    log.info("返回结果：{}",response.getEntity());
                    if (statusCode == HttpStatus.SC_OK) {
                        HttpEntity entity = response.getEntity();
                        String result = EntityUtils.toString(entity, "UTF-8");
                        JSONObject locationObj = JSON.parseObject(result);
                        log.info("返回结果：{}",locationObj);
                        String city = locationObj.getString("city");
                        String province = locationObj.getString("province");

                        shortLinkWideDO.setProvince(province);
                        shortLinkWideDO.setCity(city);
                        return shortLinkWideDO;
                    }

                } catch (InterruptedException | ExecutionException | IOException e) {
                    log.error("ip解析错误,value={},msg={}", shortLinkWideDO, e.getMessage());
                }
                return null;
            }
        });


        completableFuture.thenAccept(new Consumer<ShortLinkWideDO>() {
            @Override
            public void accept(ShortLinkWideDO shortLinkWideDO) {
                resultFuture.complete(Collections.singleton(JSON.toJSONString(shortLinkWideDO)));
            }
        });

//        completableFuture.thenAccept( (dbResult) -> {
//            resultFuture.complete(Collections.singleton(JSON.toJSONString(shortLinkWideDO)));
//        });


    }

    private CloseableHttpAsyncClient createAsyncHttpClient() {
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    //返回数据的超时时间
                    .setSocketTimeout(20000)
                    //连接上服务器的超时时间
                    .setConnectTimeout(10000)
                    //从连接池中获取连接的超时时间
                    .setConnectionRequestTimeout(1000)
                    .build();

            ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();

            PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor);
            //设置连接池最大是500个连接
            connManager.setMaxTotal(500);
            //MaxPerRoute是对maxtotal的细分，每个主机的并发最大是300，route是指域名
            connManager.setDefaultMaxPerRoute(300);

            CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom().setConnectionManager(connManager)
                    .setDefaultRequestConfig(requestConfig)
                    .build();
            httpClient.start();
            return httpClient;

        } catch (IOReactorException e) {
            log.error("初始化 CloseableHttpAsyncClient异常:{}",e.getMessage());
            return null;
        }

    }
}
