package com.xiaomi.emm.features.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.excute.MDMOrderMessageManager;
import com.xiaomi.emm.features.http.FeedBackService;
import com.xiaomi.emm.features.resend.MessageResendManager;
import com.xiaomi.emm.model.StrategyFeedBackData;
import com.xiaomi.emm.utils.DataParseUtil;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/6/16.
 */

public class FeedBackImpl extends BaseImpl<FeedBackService> {
    private static final String TAG = "FeedBackImpl";
    Context mContext;
    int repeatCount;

    public FeedBackImpl(Context context) {
        super();
        repeatCount = 0;
        this.mContext = context;
    }

    public void feedBackResult(String sendId, final String code, final String result) {
        if (TextUtils.isEmpty( result ) || TextUtils.isEmpty( code )) {
            return;
        }
        LogUtil.writeToFile(TAG,"feedback result code = " + code);
        String json = DataParseUtil.feedbackToJson(sendId,code,result) ;

        feedback(sendId, json);
    }

    public void feedBackResult(String sendId, String code, String fileId, String result) {
        if (TextUtils.isEmpty( result ) || TextUtils.isEmpty( fileId ) ||  TextUtils.isEmpty( code ) ) {
            return;
        }
        LogUtil.writeToFile(TAG,"feedback result appName = " + fileId + "code = " + code);
        String json = DataParseUtil.feedbackToJson(code,fileId,result);

        feedback(sendId, json);
    }

    public void feedStrategyBackResult(String sendId,final String code, final String id, final String type, final String alias, final String result) {

        if (TextUtils.isEmpty(result) || TextUtils.isEmpty(alias) || TextUtils.isEmpty(code)) {
            Log.w(TAG,"  feedStrategyBackResult---id为="+id+",不上传给服务器"+"code = " + code+ "result = " + result+"alias="+alias+"type="+type);
            return;
        }

        StrategyFeedBackData strategyFeedBackData = new StrategyFeedBackData();
        strategyFeedBackData.setAlias(alias);
        strategyFeedBackData.setId( id);
        strategyFeedBackData.setFeedback_code(code);
        strategyFeedBackData.setResult(result);
        strategyFeedBackData.setType( type );
        strategyFeedBackData.setSendId( sendId );

        final String json = new Gson().toJson(strategyFeedBackData);
        LogUtil.writeToFile(TAG,"feedback result appName = " +id + "code = " + code+ "result = " + result);
       // RequestBody body = RequestBody.create( okhttp3.MediaType.parse( "application/json;charset=UTF-8" ), json);
        //Log.w( TAG, body.toString() );
        String message = "";
        for (int i = 0; i < Common.message_info.length; i++) {
            if (code.equals(String.valueOf(Common.message_info[i][0]))) {
                message = mContext.getResources().getString((int)Common.message_info[i][1]);
                break;
            }
        }

        feedback(sendId, json);
        /*final String finalMessage = message;
        mService.feedBack(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (TheTang.getSingleInstance().whetherSendSuccess( response )) {

                    Log.w(TAG,code+" 回调给给服务器====成功"+ finalMessage);
                }else {
                    Log.w(TAG,code+" 回调给给服务器====失败,添加到临时表-1"+finalMessage);
                  //  feedStrategyBackResult( code,  id, alias,  result);
                    DatabaseOperate.getSingleInstance().add_backResult_sql(Common.backResult_Strategy,json);


                }


               // Log.w(TAG,"  回调给给服务器===="+response.toString());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.w(TAG,code+" 回调给给服务器====失败,添加该条数据到临时表-2"+finalMessage);
               // feedStrategyBackResult( code,  id, alias,  result);
                DatabaseOperate.getSingleInstance().add_backResult_sql(Common.backResult_Strategy,json);

            }
        });*/
    }

     public synchronized void backStrategyResult(String sendId, final String id,final String  json){
         if (TextUtils.isEmpty(id) && TextUtils.isEmpty(json)) {
             Log.w(TAG,"  feedStrategyBackResult---id为="+id+",不上传给服务器"+"json = " );
             return;
         }
         //RequestBody body = RequestBody.create( okhttp3.MediaType.parse( "application/json;charset=UTF-8" ), json);

         feedback( sendId ,json);
         /*mService.feedBack(body).enqueue(new Callback<ResponseBody>() {
             @Override
             public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                 if (TheTang.getSingleInstance().whetherSendSuccess( response )) {

                     Log.w(TAG," 返回给给服务器====成功,删除数据库该条数据"+ "  ,id = "+id+"  ,json="+json);
                   //  DatabaseOperate.getSingleInstance().delete_backResult_sql(id,json);
                     DatabaseOperate.getSingleInstance().delete_id_backResult_sql(id);

                     List<BackResultInfo> infos = DatabaseOperate.getSingleInstance().queryAll_backResult_sql();
                     Log.w(TAG,"  infos===="+infos.toString());
                 }

                 // Log.w(TAG,"  回调给给服务器===="+response.toString());
             }

             @Override
             public void onFailure(Call<ResponseBody> call, Throwable t) {
                 Log.w(TAG,id+" 返回回调给给服务器====失败,添加到临时表-2"+json);

             }
         });*/


     }

    /**
     * 上传临时表Code
     * @param id
     * @param json
     */
    public void BackCodeResult(String sendId, final String id,final String  json) {

        //RequestBody body = RequestBody.create( okhttp3.MediaType.parse(
         //       "application/json;charset=UTF-8" ), json);


        feedback( sendId, json );
        /*mService.feedBack(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                if (TheTang.getSingleInstance().whetherSendSuccess( response )) {


                //    DatabaseOperate.getSingleInstance().delete_backResult_sql(id,json);
                    DatabaseOperate.getSingleInstance().delete_id_backResult_sql(id);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {


            }
        });*/


    }


    /**
     * 反馈
     * @param data
     */
    public void feedback(final String sendId, final String data) {

        RequestBody body = TheTang.getSingleInstance().jsonToRequestBody(data);

        mService.feedBack(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {

                TheTang.getSingleInstance().getThreadPoolObject().submit( new Runnable() {
                    @Override
                    public void run() {
                        if (!TheTang.getSingleInstance().whetherSendSuccess( response )) {

                            sendFaile(sendId, data);
                            LogUtil.writeToFile(TAG,"反馈结果失败!");
                        } else {
                            MDMOrderMessageManager.getSingleInstance().feedbackSuccess( sendId );
                            LogUtil.writeToFile(TAG,"发送成功!");
                        }
                    }
                } );

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                TheTang.getSingleInstance().getThreadPoolObject().submit( new Runnable() {
                    @Override
                    public void run() {
                        sendFaile(sendId, data);
                        LogUtil.writeToFile(TAG, "反馈结果失败添加到数据库  2==" + data);
                    }
                });
            }
        });
    }

    /**
     * 发送失败逻辑
     * @param sendId
     * @param data
     */
    private void sendFaile(String sendId, String data) {
        if (repeatCount < 3) {
            MDMOrderMessageManager.getSingleInstance().feedbackFail( sendId );
        } else {
            feedback(sendId, data);
            repeatCount++;
        }
    }

    /**
     * 重发反馈
     * @param body
     */
    public void reSendFeedback(final MessageResendManager.ResendListener listener, RequestBody body) {
        mService.feedBack(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (TheTang.getSingleInstance().whetherSendSuccess( response )) {
                    listener.resendSuccess(  );
                } else {
                    listener.resendError();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.resendFail();
            }
        });
    }
}
