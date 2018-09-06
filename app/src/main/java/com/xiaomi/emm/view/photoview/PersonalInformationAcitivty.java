package com.xiaomi.emm.view.photoview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xiaomi.emm.R;
import com.xiaomi.emm.base.BaseApplication;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.event.AvatarUpdateEvent;
import com.xiaomi.emm.features.impl.PhotoUploadImpl;
import com.xiaomi.emm.features.luban.OnCompressListener;
import com.xiaomi.emm.features.luban.PhotoUploadListener;
import com.xiaomi.emm.features.luban.PictureCompressionManager;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;
import com.xiaomi.emm.view.activity.BaseActivity;
import com.xiaomi.emm.view.photoview.bens.MediaPhoto;
import com.xiaomi.emm.view.photoview.constants.Constant;
import com.xiaomi.emm.view.photoview.constants.ProviderUtil;
import com.xiaomi.emm.view.photoview.widget.PhotoBottomDialog;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2017/11/8.
 */

public class PersonalInformationAcitivty extends BaseActivity {
    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_CODE_CAMERA = 100;
    private static final String TAG = "PersonalInformationAcitivty";
    private ArrayList<MediaPhoto> mPhotoList;
    //public List<String> mUrlList = new ArrayList<>();
    // public List<String> mUrlList_back = new ArrayList<>();
    String mUrlList;
    String mUrlList_back;
    private Activity mActivity;
    private int mMaxImageCount = 1;
    private int mSurplusCount = 1;
    public File mFile;
    RelativeLayout rl_back;

    Context mContext;
    CircleImageView iv_personpoto;
    private List<String> list;
    private int flage = 0;
    private Bitmap bitmap;

    boolean isTakePhotos = false;
    // private PhotoEditView mPhotoEditView;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_personalinformation;
    }

    @Override
    protected void initData() {
        //String IN_PATH = "/MDM/Files/images/";

        setAvater();
    }


    @Override
    protected void initView() {

        mContext = this;
        // mUrlList = new ArrayList<>();
        mActivity = this;
        mPhotoList = new ArrayList<>();

        Toolbar toolbar = mViewHolder.get(R.id.toolbar);
        TextView tv_userName = mViewHolder.get(R.id.tv_userName);
        TextView tv_address = mViewHolder.get(R.id.address);
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        tv_userName.setText(getResources().getString(R.string.person_account, preferencesManager.getData(Common.userName)));
        tv_address.setText(getResources().getString(R.string.login_address, preferencesManager.getData("baseUrl")));


        toolbar.setPadding(
                toolbar.getPaddingLeft(),
                toolbar.getPaddingTop() + TheTang.getSingleInstance().getStatusBarHeight(this),
                toolbar.getPaddingRight(),
                toolbar.getPaddingBottom());

        iv_personpoto = mViewHolder.get(R.id.iv_personpoto);
        rl_back = mViewHolder.get(R.id.rl_back);
        iv_personpoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flage = 1;
                //能够添加
                PhotoBottomDialog builder = new PhotoBottomDialog(mContext, mActivity, getTakePhotoIntent(), mSurplusCount);
                builder.getAlertDialog().show();

            }
        });

        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flage = 2;
                //能够添加
                PhotoBottomDialog builder = new PhotoBottomDialog(mContext, mActivity, getTakePhotoIntent(), mSurplusCount);
                builder.getAlertDialog().show();

            }
        });

    }

    /**
     * 申请相机权限回调
     */
 /*   @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //mPhotoEditView.onRequestPermissionResult(requestCode,permissions,grantResults);

        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                Log.e(TAG, "onResult: permissions" + permissions.length + ",grant[0] =  " + grantResults[0] + "[1] = " + grantResults[1] );
                if (permissions.length == 1) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                      //  mPhotoEditAdapter.takePhoto();
                        takePhoto();
                    } else {
                        toast("权限被拒绝,无法使用相机拍照");
                    }
                } else if (permissions.length == 2) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        //mPhotoEditAdapter.takePhoto();
                       takePhoto();
                    } else {
                        toast("权限被拒绝,无法使用相机拍照");
                    }
                }

                break;
        }

    }*/
    public void takePhoto() {
        Intent intent = getTakePhotoIntent();
        mActivity.startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //  Log.e(TAG, "onActivityResult: requestCode = " + requestCode + ",resultCode = " + resultCode);
        //  mPhotoEditView.onActivityResult(requestCode,resultCode,data);
        Log.e(TAG, "onActivityResult: requestCode = " + requestCode);
        Log.e(TAG, "onActivityResult: requestCode =101 ");
        if (requestCode == REQUEST_CAMERA) {
            /*if (mFile.exists()){

                Intent intent = new Intent(this, ImageCropActivity.class);
                //ArrayList<MediaPhoto> list = (ArrayList<MediaPhoto>) data.getSerializableExtra(Constant.PHOTO_LIST);
                intent.putExtra("personPic", mFile.getAbsolutePath());
                startActivityForResult(intent, 1002);  //单选需要裁剪，进入裁剪界面
            }else {


                 setTakePhotoData();
            }*/
            isTakePhotos = true;
            setTakePhotoData();
            return;
        }
        switch (resultCode) {
            case Constant.ADD_PHOTO:
               /* Intent intent = new Intent(this, ImageCropActivity.class);
                ArrayList<MediaPhoto> list = (ArrayList<MediaPhoto>) data.getSerializableExtra(Constant.PHOTO_LIST);
                intent.putExtra("personPic",list.get(0).getUrl());
                startActivityForResult(intent, 1002);  //单选需要裁剪，进入裁剪界面*/
                isTakePhotos = false;
                setAddPhotoData(data);

                break;
            case Constant.PREVIEW_PHOTO:
                isTakePhotos = false;
                setPreviewPhotoData(data);
                break;
            case 1002:
                //setAddPhotoData(data);
                break;
        }
    }

    @NonNull
    private Intent getTakePhotoIntent() {//启动手机中的  camera app , 帮组去实现 拍照 , 那么需要发意图
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        //写到 sd卡上 . 需要申请权限.Environment.getExternalStorageDirectory()
        mFile = new File(BaseApplication.baseImagesPath, SystemClock.elapsedRealtime() + ".jpg");
        Log.e(TAG, "getTakePhotoIntent: file = " + mFile);
        LogUtil.writeToFile(TAG, "getTakePhotoIntent: file = " + mFile);
        Uri uri;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            uri = Uri.fromFile(mFile);
        } else {
            /**
             * 7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
             * 并且这样可以解决MIUI系统上拍照返回size为0的情况
             */
            Log.e(TAG, "provider: " + ProviderUtil.getFileProviderName(this));
            LogUtil.writeToFile(TAG, " ProviderUtil.getFileProviderName(mContext) = " + ProviderUtil.getFileProviderName(mContext));
            uri = FileProvider.getUriForFile(mContext, ProviderUtil.getFileProviderName(mContext), mFile);
            List<ResolveInfo> resInfoList = mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                mContext.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        return intent;
    }

    public void setAddPhotoData(Intent data) {
        if (data != null) {
            //添加从相册选择的图片
            ArrayList<MediaPhoto> list = (ArrayList<MediaPhoto>) data.getSerializableExtra(Constant.PHOTO_LIST);

            if (flage == 1) {

                mUrlList = list.get(0).getUrl();
                mPhotoList.add(list.get(0));
                setList(mUrlList);
            } else if (flage == 2) {
                mUrlList_back = list.get(0).getUrl();
                mPhotoList.add(list.get(0));
                setList(mUrlList_back);
            }


            // mSurplusCount = mMaxImageCount - mUrlList.size();
            Log.e(TAG, "onActivityResult: maxCount = " + mMaxImageCount);
        }
    }

    public void setPreviewPhotoData(Intent data) {
        if (data != null) {
            //修改经过预览后的图片
            mPhotoList = (ArrayList<MediaPhoto>) data.getSerializableExtra(Constant.PHOTO_CHECK_LIST);
            for (int i = 0; i < mPhotoList.size(); i++) {
                if (mPhotoList.get(i).isCheck()) {
                    mUrlList = mPhotoList.get(i).getUrl();
                }
            }
            setList(mUrlList);
            //mSurplusCount = mMaxImageCount - mUrlList.size();
            Log.e(TAG, "onActivityResult: maxCount = " + mMaxImageCount);
        }
    }

    public void setTakePhotoData() {
        //发送一个广播,刷新相册
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(mFile));
        mActivity.sendBroadcast(intent);
        Log.e(TAG, "setTakePhotoData: 11111 ");
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (mFile.exists() && mFile.getAbsolutePath().length() > 0) {
                mUrlList = mFile.getAbsolutePath();
                setList(mUrlList);
                mPhotoList.add(new MediaPhoto(mFile.getAbsolutePath(), true));
                Log.e(TAG, "setTakePhotoData:,mFile.length() = " + mFile.getAbsolutePath().length());
            }

        } else {
            uri = Uri.fromFile(mFile);
            if (mFile.exists() && mFile.getAbsolutePath().length() > 0) {
                //添加拍照获取的图片
                mUrlList = mFile.getAbsolutePath();
                setList(mUrlList);
                mPhotoList.add(new MediaPhoto(mFile.getAbsolutePath(), true));
            } else {
                Log.e(TAG, "setTakePhotoData: file为空了");
            }
        }
        //  mSurplusCount = mMaxImageCount - mUrlList.size();
        return;
    }

    public void toast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public void setList(final String url) {
        if (!TextUtils.isEmpty(url) && url != null) {
            List<String> photos = new ArrayList<>();

            Log.e(TAG, "setData: url = " + url);

            photos.add(url);
            loadDialog(PersonalInformationAcitivty.this,R.string.tv_compress);
            PictureCompressionManager.compressWithRx(photos, mContext, new OnCompressListener() {
                @Override
                public void onStart() {
                    //dissDialog();
                }

                @Override
                public void onSuccess(final File file) {
                    //String IN_PATH = "/MDM/Files/images/";
                    dissDialog();
                    loadDialog(PersonalInformationAcitivty.this, R.string.tv_uptate);
                    final String savePath = BaseApplication.baseImagesPath/*getApplicationContext().getFilesDir().getAbsolutePath() + IN_PATH*/;
                    if (flage == 1) {
                        // Glide.with(mContext).load(file).into(iv_personpoto);
                        final File filePic_person = new File(savePath + File.separator + "personPic.png");
                        if (!filePic_person.exists()) {
                            try {
                                filePic_person.getParentFile().mkdirs();
                                filePic_person.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.w(TAG, file.toString() + "file.getCanonicalFile()=" + file.getPath() + " ---filePic=" + filePic_person.getPath());

                        //add by duanxin on 2018.1.11 for photo upload
                        if (file == null) {
                            deletePhotps(url);
                            Toast.makeText(mContext, getResources().getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        long fileSize = 0;
                        try {
                            fileSize = TheTang.getSingleInstance().getFileSizes(file);
                        } catch (Exception e) {
                            e.printStackTrace();
                            deletePhotps(url);
                        }

                        if (fileSize > 1024 * 1024) {
                            dissDialog();
                            deletePhotps(url);
                            Toast.makeText(mContext, getResources().getString(R.string.file_size_limit), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new PhotoUploadImpl(mContext).photoUpload(file, new PhotoUploadListener() {
                            @Override
                            public void onSuccess() {
                                dissDialog();
                                Toast.makeText(mContext, getResources().getString(R.string.photo_upload_success), Toast.LENGTH_SHORT).show();

                                //    final File filePic_person = new File(savePath + "personPic");
                                Log.w(TAG, file.toString() + "file.getCanonicalFile()=" + file.getPath() + " ---filePic=" + filePic_person.getPath());
                                // TheTang.copyFile(file.getPath(),filePic_person.getPath());
                                TheTang.getSingleInstance().getThreadPoolObject().submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        //TheTang.copyFile(file.getPath(),filePic_person.getPath());
                                        TheTang.copyFile(file.getPath(), filePic_person.getPath());

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                //bitmap回收，防止OOM
                                               /* if(bitmap != null && !bitmap.isRecycled()){
                                                    bitmap.recycle();
                                                    bitmap = null;
                                                }*/

                                                Log.w(TAG, file.toString() + "file.getCanonicalFile()=" + file.getPath() + " ---filePic=" + filePic_person.getPath());
                                                // Glide.with(mContext).load(filePic_person).into(iv_personpoto);
                                               /* bitmap = BitmapFactory.decodeFile( filePic_person.getPath() );
                                                if (bitmap != null) {
                                                    iv_personpoto.setImageBitmap( bitmap );
                                                }*/
                                                Glide.with(PersonalInformationAcitivty.this).load(filePic_person.getPath()).override(100, 100)
                                                        .skipMemoryCache(true) // 不使用内存缓存
                                                        .diskCacheStrategy(DiskCacheStrategy.NONE)  // 不使用磁盘缓存
                                                        .into(iv_personpoto);
                                                deletePhotps(url);
                                            }
                                        });
                                        Log.w(TAG, file.toString() + "file.getCanonicalFile()=" + file.getPath() + " ---filePic=" + filePic_person.getPath());
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorType, String message) {
                                dissDialog();
                                if ("1".equals(errorType)) {
                                    deletePhotps(url);
                                    String content = null;

                                    if (TextUtils.isEmpty(message)) {

                                        content = getResources().getString(R.string.upload_timeout);

                                    } else {

                                        switch (Integer.valueOf(message)) {
                                            case -1035:
                                                content = getResources().getString(R.string.illegal_picture);
                                                break;
                                            case -1036:
                                                content = getResources().getString(R.string.no_face);
                                                break;
                                            case -1037:
                                                content = getResources().getString(R.string.multiple_faces);
                                                break;
                                        }

                                    }

                                    Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();

                                } else {
                                    deletePhotps(url);
                                    Toast.makeText(mContext, getResources().getString(R.string.upload_timeout), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, "1");




                       /* ThreadUtils.submitRunnable(new Runnable() {
                            @Override
                            public void run() {
                                TheTang.copyFile(file.getPath(),filePic_person.getPath());

                            }
                        });*/


                     /*  new PhotoUploadImpl(mContext).photoUpload(file, new PhotoUploadListener() {
                           @Override
                           public void onSuccess() {
                               Glide.with(mContext).load(file).into(iv_personpoto);
                               File filePic_person = new File(savePath + "personPic");
                               Log.w(TAG,file.toString()+"file.getCanonicalFile()="+file.getPath()+" ---filePic="+filePic_person.getPath());
                               TheTang.copyFile(file.getPath(),filePic_person.getPath());
                           }

                           @Override
                           public void onError(String errorType) {
                               if ("1".equals(errorType)){
                                   Toast.makeText(mContext,"照片不符合",Toast.LENGTH_SHORT).show();
                               }else {

                                   Toast.makeText(mContext,"上传失败",Toast.LENGTH_SHORT).show();
                               }
                           }
                       },"1");*/

                    } else if (flage == 2) {
                      /*  Drawable drawable =new BitmapDrawable(BitmapFactory.decodeFile(file.getPath()));
                        rl_back.setBackground(drawable);*/
                      /*  Glide.with(mContext)
                                .load(file)
                                .asBitmap()
                                .into(new SimpleTarget<Bitmap>(rl_back.getWidth(),rl_back.getHeight()) {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                        Drawable drawable = new BitmapDrawable(resource);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                            rl_back.setBackground(drawable);
                                        }
                                    }
                                });*/
                        final File filePic = new File(savePath + "personBackResource");
                        Log.w(TAG, file.toString() + "file.getCanonicalFile()=" + file.getPath() + " ---filePic=" + filePic.getPath());

                       /* ThreadUtils.submitRunnable(new Runnable() {
                            @Override
                            public void run() {
                                TheTang.copyFile(file.getPath(),filePic.getPath());

                            }
                        });*/
                       /* new PhotoUploadImpl(mContext).photoUpload(file, new PhotoUploadListener() {
                            @Override
                            public void onSuccess() {


                            }

                            @Override
                            public void onError(String errorType) {

                            }
                        },"2");*/

                    }
                }

                @Override
                public void onError() {
                    dissDialog();
                    deletePhotps(url);
                    Toast.makeText(mContext,getResources().getString(R.string.image_not_exist),Toast.LENGTH_SHORT).show();
                }
            });


        } else {
            Log.e(TAG, "url为空");
        }

    }

    private void deletePhotps(String url) {

        dissDialog();

        //如果是拍照下来的，需删除
        if (isTakePhotos) {
            File file_url = new File(url);
            if (file_url.exists()) {
                file_url.delete();
            }
        }

        deleteFilesByDirectory(new File(PersonalInformationAcitivty.this.getExternalCacheDir(),"luban_disk_cache"));
    }

    /** * 删除方法 删除文件夹下的文件和文件及其子文件里面的内容  * * @param directory */
    private static void deleteFilesByDirectory(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                deleteFilesByDirectory(childFiles[i]);
            }
            file.delete();
        }
    }

    /**
     * 设置用户头像
     */
    private void setAvater() {
        String savePath = BaseApplication.baseImagesPath;
        File file = new File(BaseApplication.baseImagesPath + File.separator + "personPic.png");


        if (file.exists() && file.length() > 0) {
            // Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
           /* if (bitmap != null) {

             //   iv_personpoto.setImageBitmap(bitmap);
            }else {
                iv_personpoto.setImageResource(R.drawable.avatar);
            }*/
            //  iv_personpoto.setImageBitmap( BitmapFactory.decodeFile(file.getPath()));
            Glide.with(this).load(file).override(100, 100)
                    .skipMemoryCache(true) // 不使用内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE)  // 不使用磁盘缓存
                    .into(iv_personpoto);
        } else {
            //iv_personpoto.setImageResource(R.drawable.avatar);
            Log.w(TAG, "---------");
            Glide.with(this).load(R.drawable.avatar).override(100, 100)
                    .skipMemoryCache(true) // 不使用内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE)  // 不使用磁盘缓存
                    .into(iv_personpoto);
        }

        File files = new File(savePath + File.separator + "personBackResource");
        if (files.exists()) {
            Drawable drawable = new BitmapDrawable(BitmapFactory.decodeFile(files.getPath()));
            rl_back.setBackground(drawable);

        } else {

            rl_back.setBackgroundResource(R.mipmap.work_fragment);
            rl_back.setBackgroundResource(R.color._33333);

        }
    }

    /**
     * EventBus回调
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateAvater(AvatarUpdateEvent event) {
        setAvater();
    }


    @Override
    protected void onDestroy() {
        // 必须在UI线程中调用
        Glide.get(this).clearMemory();
        super.onDestroy();
    }
}