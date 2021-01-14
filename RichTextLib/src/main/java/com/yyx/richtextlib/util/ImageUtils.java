package com.yyx.richtextlib.util;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.yyx.richtextlib.RichEditText;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;



/**
 * Author: AaronYang  \  aymiracle37@gmail.com
 * Date: 2020/12/29
 * Function:
 */
public class ImageUtils {

    /**
     * 图片压缩处理，size参数为压缩比，比如size为2，则压缩为1/4
     **/
    public static Bitmap compressBitmap(String path, byte[] data, Context context, Uri uri, int size, boolean width) {
        BitmapFactory.Options options = null;
        if (size > 0) {
            BitmapFactory.Options info = new BitmapFactory.Options();
            /**如果设置true的时候，decode时候Bitmap返回的为数据将空*/
            info.inJustDecodeBounds = false;
            decodeBitmap(path, data, context, uri, info);
            int dim = info.outWidth;
            if (!width) dim = Math.max(dim, info.outHeight);
            options = new BitmapFactory.Options();
            /**把图片宽高读取放在Options里*/
            options.inSampleSize = size;
        }
        Bitmap bm = null;
        try {
            bm = decodeBitmap(path, data, context, uri, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bm;
    }


    /**
     * 把byte数据解析成图片
     */
    private static Bitmap decodeBitmap(String path, byte[] data, Context context, Uri uri, BitmapFactory.Options options) {
        Bitmap result = null;
        if (path != null) {
            result = BitmapFactory.decodeFile(path, options);
        } else if (data != null) {
            result = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        } else if (uri != null) {
            ContentResolver cr = context.getContentResolver();
            InputStream inputStream = null;
            try {
                inputStream = cr.openInputStream(uri);
                result = BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    /**
     * 把bitmap转换成String
     *
     * @param filePath
     * @return
     */
    public static String bitmapToString(String filePath) {

        Bitmap bm = getSmallBitmap(filePath, 480, 800);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);

    }

    /**
     * 计算图片的缩放值
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) { //超出预设的宽高大小就尺寸压缩

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * 计算图片的缩放值
     *
     * @param options
     * @param reqWidth
     * @return
     */
    public static int calculateInSampleSizeForWidth(BitmapFactory.Options options,
                                                    int reqWidth) {
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth) { //超出预设的宽高大小就尺寸压缩
            inSampleSize = Math.round((float) options.outWidth / (float) reqWidth);
        }
        return inSampleSize;
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath, int newWidth, int newHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options); //这三句代码是保证不OOM  实际上这里不会返回一个真的bitmap，只是返回图片的宽高
//        Timber.i("============options.outWidth:" + options.outWidth);
        Log.i("=======", "============options.outHeight:" + options.outHeight);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, newWidth, newHeight);
        Log.i("=======", "============inSampleSize:" + options.inSampleSize);
        Log.i("=======", "============inSampleSize:" + options.inSampleSize);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);//inJustDecodeBounds = false了，这样才能真正的返回一个真正的Bitmap对象
        Log.i("=======", "============old bitmap  width:" + bitmap.getWidth());//这里oldbitmap和newbitmap宽高是一样的，因为压缩是质量压缩，图片文件大小实际没变，不会减小内存，
        Log.i("=======", "============old bitmap  height:" + bitmap.getHeight()); //所以bitmap的大小没有变化,所以应该比较的是option.out的宽高和真正decodefile之后的bitmap对比
        Bitmap newBitmap = compressImage(bitmap, 200);//图片大小最大200
        if (bitmap != null) {
            bitmap.recycle();
        }
        Log.i("=======", "============newBitmap width:" + newBitmap.getWidth());
        Log.i("=======", "============newBitmap heigth:" + newBitmap.getHeight());
        return newBitmap;
    }

    /**
     * 根据路径获得满屏图片
     */
    public static Bitmap getBitmapMatchParent(String filePath, int viewWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options); //这三句代码是保证不OOM  实际上这里不会返回一个真的bitmap，只是返回图片的宽高

        // Calculate inSampleSize
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (width > viewWidth) { //超出预设的宽高大小就尺寸压缩
            inSampleSize = Math.round((float) options.outWidth / (float) viewWidth);
        }
        options.inSampleSize = inSampleSize;


        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);//inJustDecodeBounds = false了，这样才能真正的返回一个真正的Bitmap对象
        Bitmap newBitmap = compressImage(bitmap, 200);//图片大小最大200
        if (bitmap != null) {
            bitmap.recycle();
        }
        return newBitmap;
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示,保持最大宽度
     */
    public static Bitmap getSmallBitmapKeepHeight(String filePath, int newHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmapSource = BitmapFactory.decodeFile(filePath, options);
        //获取原图大小计算出宽高比


        // Calculate inSampleSize
//        options.inSampleSize = calculateInSampleSize(options, newWidth, newHeight);
        Log.i("=======", "============inSampleSize:" + options.inSampleSize);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        Log.i("=======", "============old bitmap  width:" + bitmap.getWidth());
        Log.i("=======", "============old bitmap  height:" + bitmap.getHeight());
        Bitmap newBitmap = compressImage(bitmap, 200);//图片大小最大200
        if (bitmap != null) {
            bitmap.recycle();
        }
        Log.i("=======", "============newBitmap width:" + newBitmap.getWidth());
        Log.i("=======", "============newBitmap heigth:" + newBitmap.getHeight());
        return newBitmap;
    }


    /**
     * 根据view的宽度，动态缩放bitmap尺寸
     *
     * @param width
     *            view的宽度
     */
//    public Bitmap getScaledBitmap(String filePath, int width) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(filePath, options);
//        int sampleSize = options.outWidth > width ? options.outWidth / width
//                + 1 : 1;
//        options.inJustDecodeBounds = false;
//        options.inSampleSize = sampleSize;
//        return BitmapFactory.decodeFile(filePath, options);
//    }

    /**
     * 对图片进行按比例设置
     * @param bitmap 要处理的图片
     * @return 返回处理好的图片
     */
//    public static Bitmap getScaleBitmap(Bitmap bitmap, float widthScale, float heightScale){
//        Matrix matrix = new Matrix();
//        matrix.postScale(widthScale, heightScale);
//        if(bitmap == null){
//            return null;
//        }
//        Bitmap resizeBmp  =
//                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        return resizeBmp;
//    }

    /**
     * 根据路径删除图片
     *
     * @param path
     */
    public static void deleteTempFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 添加到图库
     */
    public static void galleryAddPic(Context context, String path) {
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    //使用Bitmap加Matrix来缩放
    public static Bitmap resizeImage(Bitmap bitmapOrg, int newWidth, int newHeight) {
//        Bitmap bitmapOrg = BitmapFactory.decodeFile(imagePath);
        // 获取这个图片的宽和高
        int width = bitmapOrg.getWidth();
        int height = bitmapOrg.getHeight();
        //如果宽度为0 保持原图
        if (newWidth == 0) {
            newWidth = width;
            newHeight = height;
        }
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = newWidth / width;
        float scaleHeight = newHeight / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, newWidth,
                newHeight, matrix, true);
        //Log.e("###newWidth=", resizedBitmap.getWidth()+"");
        //Log.e("###newHeight=", resizedBitmap.getHeight()+"");
        resizedBitmap = compressImage(resizedBitmap, 100);//质量压缩
        return resizedBitmap;
    }

    //使用BitmapFactory.Options的inSampleSize参数来缩放
    public static Bitmap resizeImage2(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//不加载bitmap到内存中
        BitmapFactory.decodeFile(path, options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 1;

        if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
            int sampleSize = (outWidth / width + outHeight / height) / 2;
            Log.d("###", "sampleSize = " + sampleSize);
            options.inSampleSize = sampleSize;
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 通过像素压缩图片，将修改图片宽高，适合获得缩略图，Used to get thumbnail
     *
     * @param srcPath
     * @return
     */
    public static Bitmap compressBitmapByPath(String srcPath, float pixelW, float pixelH) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = pixelH;//这里设置高度为800f
        float ww = pixelW;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        //        return compress(bitmap, maxSize); // 这里再进行质量压缩的意义不大，反而耗资源，删除
        return bitmap;
    }

    /**
     * 通过大小压缩，将修改图片宽高，适合获得缩略图，Used to get thumbnail
     *
     * @param image
     * @param pixelW
     * @param pixelH
     * @return
     */
    public static Bitmap compressBitmapByBmp(Bitmap image, float pixelW, float pixelH) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, os);
        if (os.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            os.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, os);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = pixelH;// 设置高度为240f时，可以明显看到图片缩小了
        float ww = pixelW;// 设置宽度为120f，可以明显看到图片缩小了
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        is = new ByteArrayInputStream(os.toByteArray());
        bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        int desWidth = (int) (w / be);
        int desHeight = (int) (h / be);
        bitmap = Bitmap.createScaledBitmap(bitmap, desWidth, desHeight, true);
        //压缩好比例大小后再进行质量压缩
//      return compress(bitmap, maxSize); // 这里再进行质量压缩的意义不大，反而耗资源，删除
        return bitmap;
    }

    /**
     * 质量压缩
     *
     * @param image
     * @param maxSize
     */
    public static Bitmap compressImage(Bitmap image, int maxSize) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // scale
        int options = 80;
        // Store the bitmap into output stream(no compress)
        image.compress(Bitmap.CompressFormat.JPEG, options, os);
        // Compress by loop
        while (os.toByteArray().length / 1024 > maxSize) {//循环压缩，直到图片小于最大尺寸
            // Clean up os
            os.reset();
            // interval 10
            options -= 10;
            image.compress(Bitmap.CompressFormat.JPEG, options, os);
        }

        Bitmap bitmap = null;
        byte[] b = os.toByteArray();
        if (b.length != 0) {
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return bitmap;
    }


    /**
     * 对图片进行缩放
     *
     * @param bgimage
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
//        //使用方式
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_img);
//        int paddingLeft = getPaddingLeft();
//        int paddingRight = getPaddingRight();
//        int bmWidth = bitmap.getWidth();//图片高度
//        int bmHeight = bitmap.getHeight();//图片宽度
//        int zoomWidth = getWidth() - (paddingLeft + paddingRight);
//        int zoomHeight = (int) (((float)zoomWidth / (float)bmWidth) * bmHeight);
//        Bitmap newBitmap = zoomImage(bitmap, zoomWidth,zoomHeight);
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        //如果宽度为0 保持原图
        if (newWidth == 0) {
            newWidth = width;
            newHeight = height;
        }
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        bitmap = compressImage(bitmap, 100);//质量压缩
        return bitmap;
    }

    /**
     * desc: 缩放图片，保持宽高比
     */
    public static Bitmap zoomImageKeepWH(Drawable drawable, double width, double height) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

//        drawable.getIntrinsicWidth()
        //质量压缩
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        //设置最大bitmap大小  判断是否需要质量压缩(小图不压缩)
        bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()), null, null);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;


        //尺寸压缩并保持宽高比

        return bitmap;
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @return
     */
    public static Bitmap getSmallBitmapKeepWidth(String filePath, int newWidth, int newHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, newWidth, newHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        Bitmap newBitmap = compressImage(bitmap, 200);
        if (bitmap != null) {
            bitmap.recycle();
        }
        return newBitmap;
    }


    /**
     * desc: drawable设置不超过限定宽高，并且宽高比不变
     */
    public static void scaleForRatio(Drawable drawable) {
        if (drawable != null) {
            Log.i("=======", "=========drawable:width" + drawable.getIntrinsicWidth());
            Log.i("=======", "=========drawable:height" + drawable.getIntrinsicHeight());
            float drawableW = drawable.getIntrinsicWidth();
            float drawableH = drawable.getIntrinsicHeight();
            float ratio = drawableW / drawableH;
            Log.i("=======", "=========ratio" + ratio);
            if (drawableW >= drawableH) {//横向
                drawableW = (int) RichEditText.imgSpanMaxWidth;
                drawableH = (int) (RichEditText.imgSpanMaxWidth / ratio);
            } else {//纵向
                drawableH = (int) RichEditText.imgSpanMaxHeight;
                drawableW = (int) (RichEditText.imgSpanMaxHeight * ratio);
            }
            Log.i("=======", "========drawableH" + drawableH);
            Log.i("=======", "========drawableW" + drawableW);
            drawable.setBounds(0, 0, (int) drawableW, (int) drawableH);
        }
    }

    /**
     * desc: drawable设置撑满edittextview书写区域,高度不限
     */
    public static void setDrawableWidthMatchParent(View view, Drawable drawable, float dpi) {
        if (view != null && drawable != null) {
            Log.i("=======", "=========dpi" + dpi);
            Log.i("=======", "=========view:width" + view.getWidth());
            Log.i("=======", "=========view:height" + view.getHeight());

            int contentWidth = view.getWidth() ; //内容宽度 减去边距
            int contentHeight = view.getWidth(); //内容宽度 减去边距

            float drawableW = drawable.getIntrinsicWidth() * dpi;
            float drawableH = (drawable.getIntrinsicHeight()) * dpi;

            Log.i("=======", "=========old drawableW" + drawableW);
            Log.i("=======", "=========old drawableH" + drawableH);
            if (drawableW > contentWidth) {
                drawableW = contentWidth;
            }
            Log.i("=======", "========new drawableH" + drawableH);
            Log.i("=======", "========new drawableW" + drawableW);
            drawable.setBounds(0, 0, (int) drawableW, (int) drawableH);
        }
    }

}