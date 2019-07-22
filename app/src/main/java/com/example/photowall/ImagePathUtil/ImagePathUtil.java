package com.example.photowall.ImagePathUtil;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.photowall.Bean.ImageBean;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ImagePathUtil {
    private List<ImageBean> imageBeans = new ArrayList<>();
    private HashMap<String,List<ImageBean>> mGroupMap = new HashMap<>();
    private final static int SCAN_OK = 1;

    public static List<String> getImagePathFromSd(){
        //图片列表
        List<String> imagePathList = new ArrayList<>();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String time = sdf.format(date);
        String DCIMPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        String directory = DCIMPath + "/Camera";
        //File file = new File(directory,"/IMG_"+time+"_"+count+".jpg");
        //得到该路径文件夹下所有的文件
        File fileAll = new File(directory);
        if(!fileAll.exists()){
            fileAll.mkdirs();
        }
        File[] files = fileAll.listFiles();
        //将所有的文件存入ArrayList中，并过滤所有图片格式文件
        for(int i=0;i<files.length;i++){
            File file = files[i];
            if(checkIsImageFile(file.getPath())){
                imagePathList.add(file.getPath());
            }
        }
        return imagePathList;
    }

    private static boolean checkIsImageFile(String fName){
        boolean isImageFile = false;
        //获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".")+1,
                fName.length()).toLowerCase();
        if(FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")
                || FileEnd.equals("jpeg") || FileEnd.equals("bmp")){
            isImageFile = true;
        }else {
            isImageFile = false;
        }
        return isImageFile;
    }

    public void getImages(final Context context){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                ContentResolver contentResolver = context.getContentResolver();
                Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Cursor cursor = contentResolver.query(imageUri, null,
                        MediaStore.Images.Media.MIME_TYPE +"=?or"
                                +MediaStore.Images.Media.MIME_TYPE+"=?",
                        new String[]{"image/jpg","image/png"},MediaStore.Images.Media.DATE_MODIFIED);
                if(cursor == null){
                    return;
                }
                while (cursor.moveToFirst()){
                    String path = cursor.getString(cursor.
                            getColumnIndex(MediaStore.Images.Media.DATA));
                    ImageBean bean = new ImageBean(path,false);
                    imageBeans.add(bean);
                    String parentName = new File(path).getParentFile().getName();
                    if(!mGroupMap.containsKey(parentName)){
                        List<ImageBean> childList = new ArrayList<>();
                        ImageBean imageBean = new ImageBean(path,false);
                        childList.add(imageBean);
                        mGroupMap.put(parentName,childList);
                    }else {
                        mGroupMap.get(parentName).add(new ImageBean(path,false));
                    }
                }
                mGroupMap.put("全部图片",imageBeans);
            }
//        }).start();
    }

