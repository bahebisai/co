package com.xiaomi.emm.features.luban;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Responsible for starting compress and managing active and cached resources.
 */
class Engine {
  private ExifInterface srcExif;
  private String srcImg;
  private File tagImg;
  private int srcWidth;
  private int srcHeight;
  Bitmap tagBitmap = null;

  Engine(String srcImg, File tagImg) throws IOException {
    if (Checker.isJPG(srcImg)) {
      this.srcExif = new ExifInterface(srcImg);
    }
    this.tagImg = tagImg;
    this.srcImg = srcImg;

    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    options.inSampleSize = 1;

    BitmapFactory.decodeFile(srcImg, options);
    this.srcWidth = options.outWidth;
    this.srcHeight = options.outHeight;
  }

  private int computeSize(int count) {
    srcWidth = srcWidth % 2 == 1 ? srcWidth + 1 : srcWidth;
    srcHeight = srcHeight % 2 == 1 ? srcHeight + 1 : srcHeight;

    int longSide = Math.max(srcWidth, srcHeight);
    int shortSide = Math.min(srcWidth, srcHeight);

    return 2 * count;
    /*float scale = ((float) shortSide / longSide);
    if (scale <= 1 && scale > 0.5625) {
      if (longSide < 1664) {
        return 1;
      } else if (longSide >= 1664 && longSide < 4990) {
        return 2;
      } else if (longSide > 4990 && longSide < 10240) {
        return 4;
      } else {
        return longSide / 1280 == 0 ? 1 : longSide / 1280;
      }
    } else if (scale <= 0.5625 && scale > 0.5) {
      return longSide / 1280 == 0 ? 1 : longSide / 1280;
    } else {
      return (int) Math.ceil(longSide / (1280.0 / scale));
    }*/
  }

  private Bitmap rotatingImage(Bitmap bitmap) {
    if (srcExif == null) {
        return bitmap;
    }

    Matrix matrix = new Matrix();
    int angle = 0;
    int orientation = srcExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    switch (orientation) {
      case ExifInterface.ORIENTATION_ROTATE_90:
        angle = 90;
        break;
      case ExifInterface.ORIENTATION_ROTATE_180:
        angle = 180;
        break;
      case ExifInterface.ORIENTATION_ROTATE_270:
        angle = 270;
        break;
    }

    matrix.postRotate(angle);

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
  }

  /**
   * 比例压缩与质量压缩结合
   * @return
   * @throws IOException
     */
  File compress(int count) throws IOException {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = computeSize(count);

    //bitmap回收，防止OOM
    if(tagBitmap != null && !tagBitmap.isRecycled()){
      tagBitmap.recycle();
      tagBitmap = null;
    }

    tagBitmap = BitmapFactory.decodeFile(srcImg, options);//按比例压缩，解码图片
    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    tagBitmap = rotatingImage(tagBitmap);
    tagBitmap.compress(Bitmap.CompressFormat.PNG, 60, stream);//压缩的为存储大小，为质量压缩，有损压缩
    tagBitmap.recycle();

    FileOutputStream fos = new FileOutputStream(tagImg);
    fos.write(stream.toByteArray());
    fos.flush();
    fos.close();
    stream.close();

    return tagImg;
  }
}