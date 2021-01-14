package com.yyx.richtextlib.span;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

/**
 * Author: AaronYang  \  aymiracle37@gmail.com
 * Date: 2020/12/29
 * Function: 添加区别 网络图片和本地图片
 */
public class CustomImageSpan extends android.text.style.ImageSpan {
    private String mFilePath;
    private String mUrl;

    public CustomImageSpan(Context context, Bitmap bitmap, String filePath) {
        super(context, bitmap);
        mFilePath = filePath;
    }

    public CustomImageSpan(Context context, Bitmap bitmap, String Path, boolean isLocal) {
        super(context, bitmap);
        if (isLocal) {
            mFilePath = Path;
        } else {
            mFilePath = mUrl;
        }
    }

    public CustomImageSpan(Drawable drawable) {
        super(drawable);
    }

    public String getFilePath() {
        return mFilePath;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public boolean isLocalImage() {
        //网图
        return !TextUtils.isEmpty(mFilePath);
    }
}