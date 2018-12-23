package com.sean.lxp.bleeditor.util;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class LogUtil {
    private static File file;
    private static final String LOG_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath()+"/LogFile";
    private static String LOG_FILE="log.txt";
    public static void V(String tag, String content) {
        try {
            Runtime.getRuntime().exec("logcat -f "+getFilePath());
            Log.v(tag,content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFilePath()  {
        if(file==null||!file.exists()){
           File folder=new File(LOG_FOLDER);
            if(!folder.exists()){
             folder.mkdir();

            }
            file=new File(folder,LOG_FILE) ;
            if(!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return file.getAbsolutePath();

        }else{
            return file.getAbsolutePath();
        }
    }
}
