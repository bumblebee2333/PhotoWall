package com.example.photowall.ImageUtil;

//图片文件夹bean类
public class ImgFolderBean {
    //当前文件夹路径
    private String dir;
    //第一张图片的路径用来做文件夹的封面图
    private String firstImgPath;
    //文件夹名
    private String name;
    //文件夹中图片的数量
    private int count;

    public ImgFolderBean(){

    }

    public ImgFolderBean(String dir,String firstImgPath,String name,int count){
        this.dir = dir;
        this.firstImgPath = firstImgPath;
        this.name = name;
        this.count = count;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndex = dir.lastIndexOf("/");
        this.name = dir.substring(lastIndex + 1);
    }

    public String getFirstImgPath() {
        return firstImgPath;
    }

    public void setFirstImgPath(String firstImgPath) {
        this.firstImgPath = firstImgPath;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String toString(){
        return "ImgFolderBean{" +
                "dir='" + dir + '\'' +
                ", fistImgPath='" + firstImgPath + '\'' +
                ", name='" + name + '\'' +
                ", count=" + count +
                '}';
    }
}
