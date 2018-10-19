package com.xiaomi.emm.utils;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class HttpHelper {
    public static final String TAG = HttpHelper.class.getName();

    /**
     * 获取ResponseBody的内容
     *
     * @param response
     * @return
     */
    public static String getResponseBodyString(Response<ResponseBody> response) {
        ResponseBody body = (ResponseBody) response.body();
        Log.w(TAG, "response body is null!----" + response.code());
        if (body == null) {
            LogUtil.writeToFile(TAG, "response body is null!" + response.code());
            return null;
        }
        String content = null;
        try {
            content = body.string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 判断发送是否成功
     *
     * @return
     */
    public static boolean whetherSendSuccess(String content) {
        LogUtil.writeToFile(TAG, "content = " + content);
        if (TextUtils.isEmpty(content)) {
            return false;
        }

        JSONObject object = null;
        int resultCode = 0;
        try {
            object = new JSONObject(content);
            if (object == null) {
                LogUtil.writeToFile(TAG, "body object is null!");
                return false;
            }
            resultCode = Integer.valueOf(object.getString("result"));
            LogUtil.writeToFile(TAG, "resultCode = " + resultCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (resultCode == 200) {
            return true;
        }
        return false;
    }

    /**
     * 判断发送是否成功
     *
     * @return
     */
    public static boolean whetherSendSuccess(Response<ResponseBody> response) {
//        response.isSuccessful() //todo baii to replace this method
        return whetherSendSuccess(getResponseBodyString(response));
    }

    /**
     * 提取Host
     *
     * @param url
     * @return
     */
    public static String getHost(String url) {//todo baii util http
        Pattern p = Pattern.compile("(http://|https://)?([^/]*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(url);
        return m.find() ? m.group(2) : url;
    }

    /**
     * 获取网页图标url
     *
     * @param url
     * @return
     */
    public static String getUrlForWebClip(String url) {//todo baii util http
        Document doc = null;
        String web_url = null;
        if (url.contains("www")) {
            url = url.replaceFirst("www", "wap");
        } else {
            url = url.replaceFirst("//", "//wap.");
        }

        doc = getUrl(url);
        if (doc == null) {
            return null;
        }
        Elements element = doc.select("head").select("link");
        for (Element links : element) {
            String target = links.attr("rel").toString();

            if (target != null) {
                if ("shortcut icon".equals(target.toLowerCase())) {
                    web_url = links.attr("href").toString();
                    break;
                }
            }
        }
        return web_url;
    }

    private static Document getUrl(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();//timeout(5000).post();
        } catch (Exception e) {

        }
        return doc;
    }
}
