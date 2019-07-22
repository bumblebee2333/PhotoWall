package com.example.photowall.ImageUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.photowall.DiskLruCache.DiskLruCache;
import com.example.photowall.R;
import com.example.photowall.Widget.SquareImageView;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
//磁盘缓存的源代码 需要添加到自己的项目中
//https://github.com/JakeWharton/DiskLruCache/tree/master/src/main/java/com/jakewharton/disklrucache

@RequiresApi(api = Build.VERSION_CODES.N)
public class ImageLoader{
    private static final long DISK_CACHE_SIZE = 1024*1024*50;//50MB
    private static final int DISK_CACHE_INDEX = 0;
    private static final int MESSAGE_POST_RESULT = 1;
    private static final int TAG_KEY_URL = R.id.imageLoader_url;
    //返回java虚拟机可用的处理器数
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT+1;//核心线程数
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT*2+1;//核心线程数的2倍+1
    private static final long KEEP_ALIVE = 10L;//线程闲置超时时常为10s

    private boolean mIsDiskLruCacheCreated = false;

    private Context mContext;
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;

    private static final ThreadFactory mThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r,"ImageLoader#"+mCount.getAndIncrement());
        }
    };

    public static final Executor THREAD_POOL_EXEXUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),mThreadFactory);

    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            LoaderResult result = (LoaderResult) msg.obj;
            ImageView imageView = result.imageView;
            String url = (String) imageView.getTag(TAG_KEY_URL);
            if(url.equals(result.url)){
                imageView.setImageBitmap(result.bitmap);
            }else {
                Log.e("send","set image bitmap,but url has changed,ingored!");
            }
        }
    };

    public ImageLoader(Context context){
        mContext = context;
        int maxMemory = (int)(Runtime.getRuntime().maxMemory()/1024);
        int cacheSize = maxMemory/8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes()*bitmap.getHeight()/1024;
            }
        };
        //创建磁盘缓存
        File diskCacheDir = getDiskCacheDir(context,"bitmap");
        if(!diskCacheDir.exists()){
            diskCacheDir.mkdirs();
        }
        if(getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE){
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir,1,1,DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ImageLoader build(Context context){
        return new ImageLoader(context);
    }

    //将bitmap加入到内存缓存中
    private void addBitmapToMemoryCache(String key,Bitmap bitmap){
        if(getBitmapFromMemCache(key) == null){
            mMemoryCache.put(key,bitmap);
        }
    }
    //根据key来判断bitmap是否在内存缓存中
    private Bitmap getBitmapFromMemCache(String key){
        return mMemoryCache.get(key);
    }

    private Bitmap loadBitmapFromMemCache(String url){
        final String key = hashKeyFromUrl(url);
        Bitmap bitmap = getBitmapFromMemCache(key);
        return bitmap;
    }

    private Bitmap loadBitmapFromLocal(String url){
        if(Looper.myLooper() == Looper.getMainLooper()){
            throw new RuntimeException("load bitmap from local dir.");
        }

        Bitmap bitmap = null;
        String key = hashKeyFromUrl(url);
        try {
            bitmap = ImageResizer.decodeSampledBitmapFromStream((Activity) mContext,url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(bitmap != null){
            addBitmapToMemoryCache(key,bitmap);
        }
        return bitmap;
    }

    //两级缓存图片的加载方法
    public Bitmap loadBitmap(String url){
        Bitmap bitmap = loadBitmapFromMemCache(url);
        if (bitmap != null){
            Log.e("从内存缓存读取",url);
            return bitmap;
        }
        bitmap = loadBitmapFromLocal(url);
        Log.e("从本地Camera文件读取",url);
        return bitmap;
    }

    //异步加载图片接口的设计
    public void bindBitmap(final String url, final SquareImageView imageView){
        imageView.setTag(TAG_KEY_URL,url);
        Bitmap bitmap = loadBitmapFromMemCache(url);
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
            return;
        }
        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(url);
                if(bitmap != null){
                    LoaderResult result = new LoaderResult(imageView,url,bitmap);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT,result).sendToTarget();
                }
            }
        };
        THREAD_POOL_EXEXUTOR.execute(loadBitmapTask);
    }

    //判断外部存储是否存在
    public File getDiskCacheDir(Context context,String uniqueName){
        //返回主 外部存储介质的当前状态
        boolean externalStorageAvailable = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if(externalStorageAvailable){
            ///storage/emulated/0/Android/data/package_name/cache(外部存储目录)
            cachePath = context.getExternalCacheDir().getPath();//获取缓存路径
        }else {
            ///data/data/package_name/cache(文件系统目录)
            cachePath = context.getCacheDir().getPath();//返回sd卡不存在时或存满 文件系统的缓存路径
        }
        return new File(cachePath+File.separator+uniqueName);
    }

    private long getUsableSpace(File path){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
            return path.getUsableSpace();
        }
        //检索有关文件系统上的控件的整体信息
        final StatFs stats = new StatFs(path.getPath());
        long l = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            l = stats.getBlockSizeLong() * stats.getAvailableBlocksLong();
        }
        return l;
    }

    //将图片的url转化为key
    private String hashKeyFromUrl(String url){
        String cachKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cachKey = byteToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cachKey = String.valueOf(url.hashCode());
        }
        return cachKey;
    }

    private String byteToHexString(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<bytes.length;i++){
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if(hex.length() == 1){
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private static class LoaderResult{
        public SquareImageView imageView;
        public String url;
        public Bitmap bitmap;

        public LoaderResult(SquareImageView imageView,String url,Bitmap bitmap){
            this.imageView = imageView;
            this.url = url;
            this.bitmap = bitmap;
        }
    }
}
