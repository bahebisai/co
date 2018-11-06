package com.xiaomi.emm.features.impl;
import android.util.Log;
import android.widget.Toast;

import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.TheTang;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 基础实现类
 * 1、拦截请求 为请求添加token
 * 2、自动刷新token
 * 3、OkHttpClient配置
 * 4、retrofit配置
 * 5、token service
 * Created by Administrator on 2017/5/26.
 */

public class BaseImpl<Service>{
    public static  final String TAG = "BaseImpl";
    protected static Retrofit retrofit;
    protected Service mService;
    protected static TheTang theTang;
    public BaseImpl(/*Context context*/) {

        theTang = TheTang.getSingleInstance();
        initRetrofit();
        mService = retrofit.create(getServiceClass());
    }

    private Class<Service> getServiceClass() {
        return (Class<Service>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    private void initRetrofit() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                //LogUtil.writeToFile("LoggingInterceptor", message);
                LogUtil.writeToFile("LoggingInterceptor", message);
                Log.w("LoggingInterceptor", message);

            }
        });
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);//只抓取请求/响应行，防止内存溢出

        //拦截请求，为每个请求添加token
        Interceptor mTokenIntercepter = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request originaRequest = chain.request();
                Log.d(TAG,"originaRequest:" + originaRequest.toString());
                if (PreferencesManager.getSingleInstance().getData("token") == null/*|| alreadyHasAuthorizationHeader(originaRequest)*/) {
                    return chain.proceed(originaRequest);
                }

                String token = PreferencesManager.getSingleInstance().getData("token");
                Request request = originaRequest.newBuilder()
                        .header("token",token)
                        .build();
                return chain.proceed(request);
            }
        };

        //自动刷新token,错误代码为401时调用
       /* Authenticator mAuthenticator = ne Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                TokenService tokenService = retrofit.create(TokenService.class);
                String accessToken = "";
                if (mACacheUtil.getToken() != null) {
                    Call<Token> call = tokenService.refreshToken("refresh_token",mACacheUtil.getToken().getRefresh_token());
                    retrofit2.Response tokenReponse = call.execute();
                    Token token = (Token) tokenReponse.body();
                    if (token != null) {
                        mACacheUtil.saveToken(token);
                        accessToken = token.getAccess_token();
                    }
                }
                return response.request().newBuilder().addHeader("Authorization",accessToken).build();
            }
        };*/

        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s)
                    throws java.security.cert.CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s)
                    throws java.security.cert.CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }
        } };

        SSLContext sc =null;

        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //配置client
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .retryOnConnectionFailure(true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .addNetworkInterceptor(mTokenIntercepter)
                //.authenticator(mAuthenticator)
                .hostnameVerifier( new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                })
                .sslSocketFactory( sc.getSocketFactory() )
                .protocols( Collections.singletonList( Protocol.HTTP_1_1) )
                .build();


        //配置retrofit
        String  baseUrl = PreferencesManager.getSingleInstance().getData( "baseUrl" );

        if (baseUrl != null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        } else {

            Toast.makeText( TheTang.getSingleInstance().getContext(),"URL为空",Toast.LENGTH_LONG );

        }



       /* if (!TextUtils.isEmpty(Common.URL)){

            builder.baseUrl(Common.URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }else {
            builder.baseUrl(Common.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }*/

    }

    /*private boolean alreadyHasAuthorizationHeader(Request originRequest) {
        String token = originRequest.header("");
        return
    }*/

    /*interface TokenService {
        /**
         * 刷新 token
         */
       // @POST("app/login")
       // @FormUrlEncoded
    //    Call<Token> refreshToken(@Field("grant_type") String grant_type, @Field("refresh_token") String refresh_token);
    //}


}
