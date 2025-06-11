package utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class HttpRequestUtil {
    public static Logger logger = Logger.getLogger(InitMysqlUtil.class);

    public static HashMap<String, String> jsonObjectToMap(String param) {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        // 解析json格式字符串为JSONObject
        JSONObject jsonObject = JSONObject.parseObject(param);
        // JSONObject转换为map
        Set<String> keys = jsonObject.keySet();
        for (String key : keys) {
            paramMap.put(key, jsonObject.getString(key));
        }
        return paramMap;
    }

    public static Map<String, Object> sendRequest(String url, String requestType, String method, String headers, String body){
        Map<String, Object> resMap = new HashMap<>();
        try {
            if ("get".equalsIgnoreCase(requestType)){
                resMap = getRequest(url, headers, body);
            }else if ("post".equalsIgnoreCase(requestType)){
                resMap = postRequest(url, method, headers, body);
            }else {
                resMap.put("statusCode", 999);
                resMap.put("res", "{'msg': '接口请求类型不对!'}");
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return resMap;
    }

    public static Map<String, Object> getRequest(String url, String headers, String body)  {
        Map<String, Object> resMap = new HashMap<>();
        // 添加请求体
        HashMap<String, String> paramMap =jsonObjectToMap(body);
        Set<String> keys = paramMap.keySet();
        // 拼接请求地址连接
        StringBuilder urlBuilder = new StringBuilder(url+"?");
        for (String key : keys) {
            if (urlBuilder.toString().endsWith("?")) {
                urlBuilder.append(key).append("=").append(paramMap.get(key));
            } else {
                urlBuilder.append("&").append(key).append("=").append(paramMap.get(key));
            }
        }
        // 对请求参数进行urlencoded编码，排除特殊符号导致的异常情况
        try {
            URL urlNew = new URL(urlBuilder.toString());
            URI urlTrans = new URI(urlNew.getProtocol(), urlNew.getHost(), urlNew.getPath(), urlNew.getQuery(), null);
            url = urlTrans.toString();
        } catch (URISyntaxException | MalformedURLException e) {
            logger.error(e.toString());
        }
        logger.info("接口请求类型: GET，接口请求地址: " + url);
        HttpGet httpGet = new HttpGet(url);
        // 通过形参设置请求头
        JSONObject headsJsonObject = JSONObject.parseObject(headers);
        Set<String> headerKeys = headsJsonObject.keySet();
        // 添加请求头
        for (String headerKey : headerKeys) {
            httpGet.addHeader(headerKey.trim(),headsJsonObject.getString(headerKey).trim());
        }
        // 创建可供关闭的发包客户端
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(httpResponse.getEntity());
            resMap.put("statusCode", statusCode);
            resMap.put("res", result);
        } catch (IOException e) {
            logger.error(e.toString());
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error(e.toString());
            }
        }
        return resMap;
    }

    public static Map<String, Object> postRequest(String url, String method, String headers, String body) {
        Map<String, Object> resMap = new HashMap<>();
        if (method.equalsIgnoreCase("json")) {  //参数类型为JSON格式
            JSONObject headsJsonObject = JSONObject.parseObject(headers);
            JSONObject bodyJsonObject = JSONObject.parseObject(body);
            resMap = postRequestByJson(url, headsJsonObject, bodyJsonObject);
        } else if (method.equalsIgnoreCase("from")) {   //参数类型为Form格式
            HashMap<String, String> paramMap =jsonObjectToMap(body);
            resMap = postRequestByFrom(url, paramMap);
        } else {
            // 如果数据类型不支持，该用例直接失败
            Assert.fail("参数类型不支持，用例执行失败");
        }
        return resMap;
    }

    // post发送Json数据类型请求
    public static Map<String, Object> postRequestByJson(String url, JSONObject headers, JSONObject body){
        Map<String, Object> resMap = new HashMap<>();
        HttpPost httpPost = new HttpPost(url);
        // 通过形参设置请求头
        Set<String> headerKeys = headers.keySet();
        for (String headerKey : headerKeys) {
            httpPost.addHeader(headerKey.trim(),headers.getString(headerKey).trim());
        }

        // 发送 json 类型数据
        httpPost.setEntity(new StringEntity(body.toString(),"UTF-8"));

        // 创建可供关闭的发包客户端
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(httpResponse.getEntity());
            resMap.put("statusCode", statusCode);
            resMap.put("res", result);
        } catch (IOException e) {
            logger.error(e.toString());
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error(e.toString());
            }
        }
        return resMap;
    }

    // post发送x-www-form-urlencoded数据类型请求
    public static Map<String, Object> postRequestByFrom(String url, Map<String,String> params){
        Map<String, Object> resMap = new HashMap<>();
        HttpPost httpPost = new HttpPost(url);
        ArrayList<BasicNameValuePair> basicNameValuePairs = new ArrayList<BasicNameValuePair>();

        // 遍历map，放到basicNameValuePairs中
        Set<String> keys = params.keySet();
        for (String key : keys) {
            basicNameValuePairs.add(new BasicNameValuePair(key,params.get(key)));
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            // 将Content-Type设置为application/x-www-form-urlencoded类型
            httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairs,"UTF-8"));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(httpResponse.getEntity());
            resMap.put("statusCode", statusCode);
            resMap.put("res", result);
        } catch (IOException e) {
            logger.error(e.toString());
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error(e.toString());
            }
        }
        return resMap;
    }
}
