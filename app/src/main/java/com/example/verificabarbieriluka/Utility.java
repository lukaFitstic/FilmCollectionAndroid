package com.example.verificabarbieriluka;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utility {
    static String getImagesPath(Context context){
        String imagePath = "";

        String dir = "/images";

        String completePath = context.getFilesDir() + dir;
        File file = new File(completePath);
        if(!file.exists()){
            boolean success = file.mkdir();
            if(success){
                imagePath = completePath;
            }
        }else{
            imagePath = completePath;
        }

        return imagePath;
    }

    public static void saveImage(String path, Bitmap bitmap){
        FileOutputStream outputStream = null;
        File outFile = new File(path);
        try{
            outputStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        }catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean deleteImage(String path) {

        File file = new File(path);

        if (file.exists()) {

            return file.delete();
        }

        return false;
    }
}
