package com.example.photowall.Bean;

public class ImageBean {
    private String imagPath;
    private boolean isChoosed;

    public ImageBean(String imagPath,boolean isChoosed){
        this.imagPath = imagPath;
        this.isChoosed = isChoosed;
    }

    public String getImagPath(){
        return imagPath;
    }

    public void setImagPath(String imagPath){
        this.imagPath = imagPath;
    }

    public boolean isChoosed(){
        return isChoosed;
    }

    public void setChoosed(boolean isChoosed){
        this.isChoosed = isChoosed;
    }
}
