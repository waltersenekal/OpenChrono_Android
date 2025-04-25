package net.senekal.openchrono.utils;

public class FileInfo {


    private String fileName;


    private String md5;


    private String sha1;


    private String size;


    public FileInfo(String fileName, String md5, String sha1, String size) {
        this.fileName = fileName;
        this.md5 = md5;
        this.sha1 = sha1;
        this.size = size;
    }


    public String getFileName() {
        return fileName;
    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public String getMd5() {
        return md5;
    }


    public void setMd5(String md5) {
        this.md5 = md5;
    }


    public String getSha1() {
        return sha1;
    }


    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }


    public String getSize() {
        return size;
    }


    public void setSize(String size) {
        this.size = size;
    }
}

