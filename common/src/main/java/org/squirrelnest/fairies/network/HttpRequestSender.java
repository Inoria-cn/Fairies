package org.squirrelnest.fairies.network;

import org.apache.commons.collections4.MapUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by Inoria on 2019/3/17.
 */
@Service
public class HttpRequestSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestSender.class);

    @Value("${fairies.dht.request.timeout}")
    private Integer timeoutMs;

    @Resource
    private HttpClientBuilder httpClientBuilder;

    private RequestConfig defaultConfig() {
        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setConnectTimeout(timeoutMs)
                .setSocketTimeout(2000);
        return builder.build();
    }

    private URI addParams(URI uri, Map<String, String> params) throws URISyntaxException {
        if(MapUtils.isEmpty(params)) {
            return uri;
        }
        URIBuilder builder = new URIBuilder(uri);
        builder.setCharset(Charset.forName("UTF-8"));
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.setParameter(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    /**
     * 发送http get请求
     * @param address 目标地址和端口
     * @param params 请求参数（url参数）
     * @return 序列化json结果
     */
    public String httpGet(String address, Map<String, String> params) {
        String result = null;
        HttpGet httpGet = new HttpGet(address);
        httpGet.setConfig(defaultConfig());
        httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            httpGet.setURI(addParams(httpGet.getURI(), params));
        } catch (Exception e) {
            LOGGER.error("Add params raised an error.", e);
            return null;
        }
        CloseableHttpClient client = httpClientBuilder.build();
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            result = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            LOGGER.error("Decode response raised an error.", e);
        }
        return result;
    }
}
