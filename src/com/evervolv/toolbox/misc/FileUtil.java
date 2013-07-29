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

    public static String[] getMounts(final String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/mounts"), 256);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains(path)) {
                    br.close();
                    return line.split(" ");
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "/proc/mounts does not exist");
        } catch (IOException e) {
            Log.d(TAG, "Error reading /proc/mounts");
        }
        return null;
    }

    public static boolean getMount(final String mount) {
        final CMDProcessor cmd = new CMDProcessor();
        final String mounts[] = getMounts("/system");
        String command;
        if (mounts != null && mounts.length >= 3) {
            final String device = mounts[0];
            final String path = mounts[1];
            final String point = mounts[2];
            command = "mount -o " + mount + ",remount -t " + point + " "
                    + device + " " + path;
            if (cmd.su.runWaitFor(command).success()) {
                return true;
            }
        }
        command = "busybox mount -o remount," + mount + " /system";
        return (cmd.su.runWaitFor(command).success());
    }

}
