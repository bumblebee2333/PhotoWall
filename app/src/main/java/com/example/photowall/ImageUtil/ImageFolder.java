package com.example.photowall.ImageUtil;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageFolder {
    private static List<String> imagePaths = new ArrayList<>();

    public static List<ImgFolderBean> getImageFolders(Context context){
        List<ImgFolderBean> folders = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Images.Media.MIME_TYPE + "= ? or " + MediaStore.Images.Media.MIME_TYPE + "= ?",
                new String[]{"image/jpg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);
        //用于保存已经添加过的文件夹目录
        List<String> mDirs = new ArrayList<>();
        while (cursor.moveToNext()){
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            File parentFile = new File(path).getParentFile();
            if (parentFile == null)
                continue;
            String dir = parentFile.getAbsolutePath();
            //imagePaths = getImageListByDir(dir);
            if(mDirs.contains(dir))
                break;
            mDirs.add(dir);
            ImgFolderBean folderBean = new ImgFolderBean();
            folderBean.setDir(dir);
            folderBean.setFirstImgPath(path);
            if (parentFile.list() == null)
                continue;
            int count = parentFile.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if(name.endsWith(".jpeg") || name.endsWith(".jpg") || name.endsWith(".png")){
                        return true;
                    }
                    return false;
                }
            }).length;
            folderBean.setCount(count);
            folders.add(folderBean);
        }
        cursor.close();
        return folders;
    }

    public static List<String> getImageListByDir(){
        ArrayList<String> imgPaths = new ArrayList<>();
        String localPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/DCIM/Camera";
        File directory = new File(localPath);
//        if(directory == null || !directory.exists()){
//            return imgPaths;
//        }
        File[] files = directory.listFiles();
        for(File file : files){
            String path = file.getAbsolutePath();
            if(isPicFile(path)){
                imgPaths.add(path);
            }
        }
        Collections.reverse(imgPaths);
        return imgPaths;
    }

    public static boolean isPicFile(String path){
        path = path.toLowerCase();
        if(path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png")){
            return true;
        }else {
            return false;
        }
    }

    public static List<Uri> getBitmapUriFromLocalDir(Context context){
        List<Uri> imageUri = new ArrayList<>();
        String localPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/DCIM/Camera";
        File file = new File(localPath);
        File[] files = file.listFiles();
        for(File file1 : files){
            Uri uri = getMediaUriFromPath(context,file1.getAbsolutePath());
            if(uri == null)
                continue;
            imageUri.add(uri);
        }
        return imageUri;
    }

    public static Uri getMediaUriFromPath(Context context,String path){
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(mediaUri,
                null,MediaStore.Images.Media.DISPLAY_NAME+"=?",
                new String[]{path.substring(path.lastIndexOf("/")+1)},
                null);
        Uri uri = null;
        if(cursor.moveToFirst()){
            uri = ContentUris.withAppendedId(mediaUri,
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
        }
        cursor.close();
        return uri;
    }
}
