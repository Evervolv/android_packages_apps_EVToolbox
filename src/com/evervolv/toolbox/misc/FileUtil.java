package com.evervolv.toolbox.misc;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

    private static final String TAG = "EVToolbox";

    public static boolean fileExists(String filename) {
        return new File(filename).exists();
    }

    public static boolean fileIsWritable(String fname) {
        return new File(fname).canWrite();
    }

    public static String fileReadOneLine(String fname) {
        BufferedReader br;
        String line = null;

        try {
            br = new BufferedReader(new FileReader(fname), 512);
            try {
                line = br.readLine();
            } finally {
                br.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "IO Exception when reading file:" + fname, e);
        }
        return line;
    }

    public static boolean fileWriteOneLine(String fname, String value) {
        try {
            FileWriter fw = new FileWriter(fname);
            try {
                fw.write(value);
            } finally {
                fw.close();
            }
        } catch (IOException e) {
            String Error = "Error writing to " + fname + ". Exception: ";
            Log.e(TAG, Error, e);
            return false;
        }
        return true;
    }

    public static boolean fileDelete(String fname) {
        File f = new File(fname);
        return f.exists() && f.delete();
    }
}
