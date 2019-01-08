package com.ppma.jadx;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;

import com.ppma.filemanager.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import thereisnospon.codeview.CodeView;
import thereisnospon.codeview.CodeViewTheme;

public class ShowCodeActivity extends AppCompatActivity {
    CodeView codeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_code);
        ArrayList<String> str = getIntent().getStringArrayListExtra("fileName");
        setTitle(str.get(0));
        codeView = findViewById(R.id.codeview);
        codeView.setTheme(CodeViewTheme.IDEA);
        WebSettings webSettings = codeView.getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        //webSettings.setLoadWithOverviewMode(true);
        codeView.showCode(str.get(1));
    }


}
