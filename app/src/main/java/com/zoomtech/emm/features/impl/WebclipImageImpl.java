package com.zoomtech.emm.features.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.util.Log;

import com.zoomtech.emm.R;
import com.zoomtech.emm.features.event.NotifyEvent;
import com.zoomtech.emm.features.http.RequestService;
import com.zoomtech.emm.features.policy.device.ShortcutUtils;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;

import org.greenrobot.eventbus.EventBus;

import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by lenovo on 2017/9/5.
 */
//todo baii impl for response
public class WebclipImageImpl extends BaseImpl<RequestService> {
    Context mContext;

    public WebclipImageImpl (Context context) {
        mContext = context;
    }

    /**
     *
     * @param webClipImgPath   下载图片的路径
     * @param webClipName   显示图片的名字
     * @param webClipUrl   设置指定跳转的url
     * @param picName    存储到本地图片的名字
     */
    public void downloadPicFromNet (final String webClipImgPath, final String webClipName, final String webClipUrl,final String picName) {

        if (TextUtils.isEmpty(webClipImgPath)|| TextUtils.isEmpty(webClipName) || TextUtils.isEmpty(webClipUrl) || TextUtils.isEmpty(picName)){
            Log.w(TAG,"webClipImgPath 或者 webClipName 或者 webClipUrl 或者 picName 下载图片路径为空返回");
            return;
        }
       // String urls="http://thetang.f3322.net:8088/fileupload/config_img/"+url;
        /// String urlss="http://www.thetang.f3322.net:8088/fileupload/config_img/adc73cf3-71f1-4e87-9ad3-f357d4bb5a6e.png";
        //String urlss="http://www.eoeandroid.com/data/attachment/forum/201107/18/142935bbi8d3zpf3d0dd7z.jpg";
        Log.w(TAG,"图---"+webClipImgPath);
        String str = "http://www.thetang.f3322.net:8088/"+ webClipImgPath;

        mService.getStreamInfo(webClipImgPath).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                Log.w(TAG,"图片下载成功---" + webClipImgPath);
                //解决主线程操作IO流
               TheTang.getSingleInstance().getThreadPoolObject().submit( new Runnable() {
                   @Override
                   public void run() {

                        if ( response.body() == null) {
                            Log.w(TAG,"body返回空---");
                            Bitmap bitmap = BitmapFactory.decodeResource( mContext.getResources(), R.mipmap.ic_launcher );
                            ShortcutUtils.addShortcut(TheTang.getSingleInstance().getContext(),ShortcutUtils.getShortCutIntent(webClipUrl),webClipName,false,bitmap);
                            String s = ShortcutUtils.saveBitmap(TheTang.getSingleInstance().getContext(), bitmap,picName);
                            Log.w(TAG,picName + "存图片的路径----"+s);
                            EventBus.getDefault().post(new NotifyEvent());
                            //回收bitmap
                            TheTang.gcBitmap( bitmap );
                            return;
                        }

                        InputStream byteStream = response.body().byteStream();

                        Bitmap bitmap = BitmapFactory.decodeStream(byteStream);

                        if (bitmap != null) {
                            {
                                bitmap= createBitmapThumbnail(bitmap);
                                //  bitmap.setHeight(80);
                                //  bitmap.setWidth(80);
                                //  webClipImgPath.split("\\\\").length - 1
                                // String picName = webClipImgPath.split("\\\\")[webClipImgPath.split("\\\\").length - 1];
                                Log.w(TAG,picName + "存图片的路径----" + webClipName);
                                ShortcutUtils.addShortcut(TheTang.getSingleInstance().getContext(),ShortcutUtils.getShortCutIntent(webClipUrl),webClipName,false,bitmap);
                                String s = ShortcutUtils.saveBitmap(TheTang.getSingleInstance().getContext(), bitmap,picName);
                                Log.w(TAG,picName + "存图片的路径----" + s);
                                EventBus.getDefault().post(new NotifyEvent());

                            }
                        }else {
                            Log.w(TAG,"返回来的----bitmap为空,给默认的图片");
                            bitmap = BitmapFactory.decodeResource( mContext.getResources(), R.mipmap.ic_launcher );
                            ShortcutUtils.addShortcut(TheTang.getSingleInstance().getContext(),ShortcutUtils.getShortCutIntent(webClipUrl),webClipName,false,bitmap);
                            String s = ShortcutUtils.saveBitmap(TheTang.getSingleInstance().getContext(), bitmap,picName);
                            Log.w(TAG,picName + "存图片的路径----" + s);
                            EventBus.getDefault().post(new NotifyEvent());

                        }
                        //回收bitmap
                        TheTang.gcBitmap( bitmap );
                    }
                } );
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                PreferencesManager.getSingleInstance().setConfiguration("downloadPic","true");
                Log.w(TAG,"图片下载失败---" + webClipImgPath);
                t.printStackTrace();

                //图片下载失败，使用默认图片
                Bitmap bitmap = BitmapFactory.decodeResource( mContext.getResources(), R.mipmap.ic_launcher );
                ShortcutUtils.addShortcut( TheTang.getSingleInstance().getContext(), ShortcutUtils.getShortCutIntent( webClipUrl ), webClipName, false, bitmap );
                //回收bitmap
                TheTang.gcBitmap( bitmap );
                EventBus.getDefault().post( new NotifyEvent() );
            }
        });


    }

    public Bitmap createBitmapThumbnail(Bitmap bitMap) {
        int width = bitMap.getWidth();
        int height = bitMap.getHeight();
        // 设置想要的大小
        int newWidth = 128;
        int newHeight = 128;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newBitMap = Bitmap.createBitmap(bitMap, 0, 0, width, height,
                matrix, true);
        return newBitMap;
    }



}




/**
 * call.enqueue(new Callback<ResponseBody>() {
@Override
public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

InputStream byteStream = response.body().byteStream();

bitmap = BitmapFactory.decodeStream(byteStream);

//   ShortcutUtils.writeResponseBodyToDisk("download.jpg",response.body());
if (bitmap != null){
//  bitmap.setHeight(80);
//  bitmap.setWidth(80);
//  webClipImgPath.split("\\\\").length - 1
String picName = webClipImgPath.split("\\\\")[webClipImgPath.split("\\\\").length - 1];

ShortcutUtils.addShortcut(TheTang.getSingleInstance().getContext(),ShortcutUtils.getShortCutIntent(webClipUrl),webClipName,false,bitmap);
String s = ShortcutUtils.saveBitmap(TheTang.getSingleInstance().getContext(), bitmap,picName);
Log.w(TAG,picName+"存图片的路径----"+s);
EventBus.getDefault().post(new NotifyEvent());

}else {
//快捷图片
// Parcelable icon = Intent.ShortcutIconResource.fromContext(TheTang.getSingleInstance().getContext(), R.mipmap.phone_android);
// shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,icon);//EXTRA_SHORTCUT_ICON   //EXTRA_SHORTCUT_ICON_RESOURCE

}

}

@Override
public void onFailure(Call<ResponseBody> call, Throwable t) {
bitmap=null;
t.printStackTrace();
Log.w(TAG,"存图片的路径----下载失败" );
}
});
 */

