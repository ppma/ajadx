package com.ppma.apktool;

import android.content.Context;
import android.content.res.AssetManager;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Apktool {

    private static AssetManager assetManager;
    private static File homeDir;

    public static void init(Context context) throws IOException {
        assetManager = context.getAssets();
        homeDir = context.getFilesDir();
        for (String local : assetManager.getLocales()) {
            InputStream in = assetManager.open(local);
            FileOutputStream fo = new FileOutputStream(homeDir);
            IOUtils.copy(in, fo);
            in.close();
            fo.close();
        }
    }

    public static File getHomeDir() {
        return homeDir;
    }
}
