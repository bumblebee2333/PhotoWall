package com.example.photowall;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.photowall.Adapter.ImageAdapter;
import com.example.photowall.ImagePathUtil.ImagePathUtil;
import com.example.photowall.ImageUtil.ImageFolder;
import com.example.photowall.ImageUtil.ImageLoader;
import com.example.photowall.ImageUtil.ImgFolderBean;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private GridView photoWall;
    private List<String> imagePaths = new ArrayList<>();
    private ImageAdapter mImageAdapter;
    private ImageLoader mImageLoader;
    private boolean mIsGridViewIdle = true;

    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }

        initView();
        mImageLoader = ImageLoader.build(MainActivity.this);
        imagePaths = ImageFolder.getImageListByDir();
        mImageAdapter = new ImageAdapter(MainActivity.this,imagePaths,mImageLoader,mIsGridViewIdle);
        photoWall.setAdapter(mImageAdapter);
        photoWall.setOnScrollListener(this);
    }

    private void initView(){
        photoWall = findViewById(R.id.GridView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
            mIsGridViewIdle = true;
            mImageAdapter.notifyDataSetChanged();
        }else {
            mIsGridViewIdle = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
