package com.xiaomi.emm.features.luban;

import android.content.Context;
import android.support.annotation.NonNull;

import com.xiaomi.emm.view.activity.MainActivity;

import java.io.File;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/11/8.
 */

public class PictureCompressionManager {

    /**
     * 压缩图片入口
     * @param photos
     * @param context
     */
    public static void compressWithRx(final List<String> photos, final Context context, final OnCompressListener onCompressListener) {
        Flowable.just(photos)
                .observeOn( Schedulers.io())
                .map(new Function<List<String>, List<File>>() {
                    @Override public List<File> apply(@NonNull List<String> list) throws Exception {
                        return Luban.with(context).load(list).get();
                    }
                })
                .observeOn( AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<File>>() {
                    @Override public void accept(@NonNull List<File> list) throws Exception {

                        if (list.size() == 0) {
                            onCompressListener.onError();
                        } else {
                            for (File file : list) {
                                onCompressListener.onSuccess(file);
                            }
                        }
                    }
                });
    }
}
