package com.ppma.filemanager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.ppma.filemanager.adapter.FileHolder;
import com.ppma.filemanager.adapter.FileAdapter;
import com.ppma.filemanager.adapter.TitleAdapter;
import com.ppma.filemanager.adapter.base.RecyclerViewAdapter;
import com.ppma.filemanager.bean.FileBean;
import com.ppma.filemanager.bean.TitlePath;
import com.ppma.filemanager.bean.FileType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.utils.exceptions.JadxException;
import jadx.core.utils.files.FileUtils;
import jadx.gui.treemodel.JSources;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity {
    private RecyclerView title_recycler_view;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<FileBean> beanList = new ArrayList<>();
    private File rootFile;
    private LinearLayout empty_rel;
    private int PERMISSION_CODE_WRITE_EXTERNAL_STORAGE = 100;
    private String rootPath;
    private TitleAdapter titleAdapter;
    private String tmpFile;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            getFile(tmpFile);
            Toast.makeText(MainActivity.this, "完成进度：" + msg.obj, Toast.LENGTH_LONG).show();
            //Toast.makeText(FileActivity.this, "运行时间：" + msg.obj + "s", Toast.LENGTH_LONG).show();
        }
    };


    private void showListDialog(final String fileName) {
        final String[] items = {"反编译工具", "代码阅读器"};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle("请选择打开方式");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                // ...To-do
                switch (which) {
                    case 0:
                        deCode(fileName);
                        break;
                    case 1:
                        ArrayList<String> info = new ArrayList<>();
                        File hereFile = new File(fileName);
                        info.add(hereFile.getName());
                        info.add(readToString(hereFile));
                        FileUtil.openDecodeIntent(MainActivity.this, info);
                        break;
                }
            }
        });
        listDialog.show();
    }


    private ProgressDialog getProgressDialog(Context context, String title) {
        /* @setProgress 设置初始进度
         * @setProgressStyle 设置样式（水平进度条）
         * @setMax 设置进度最大值
         */
        final int MAX_PROGRESS = 100;
        final ProgressDialog progressDialog =
                new ProgressDialog(context);
        progressDialog.setProgress(0);
        progressDialog.setTitle(title);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(MAX_PROGRESS);
        progressDialog.show();
        return progressDialog;
    }


    public void deCode(String fileName) {

        JadxArgs args = new JadxArgs();
        args.setOutDir(new File(fileName.substring(0, fileName.lastIndexOf(".")) + "_src"));
        args.setThreadsCount(1);
        //args.setSkipResources(true);
        final JadxDecompiler decompiler = new JadxDecompiler(args);
        try {
            decompiler.loadFile(new File(fileName));
        } catch (JadxException e) {
            Toast.makeText(MainActivity.this, "错误信息：" + e.toString(), Toast.LENGTH_SHORT).show();
            return;
        }
        new JSources(decompiler);
        final ProgressDialog progressDialog = getProgressDialog(MainActivity.this, "反编译进度");
        Runnable save = new Runnable() {

            @Override

            public void run() {

                try {

                    ThreadPoolExecutor ex = (ThreadPoolExecutor) decompiler.getSaveExecutor();

                    ex.shutdown();

                    while (ex.isTerminating()) {

                        long total = ex.getTaskCount();

                        long done = ex.getCompletedTaskCount();

                        progressDialog.setProgress((int) (done * 100.0 / (double) total));

                        Thread.sleep(300);

                    }

                    progressDialog.cancel();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "反编译完成", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (InterruptedException e) {

                    Toast.makeText(MainActivity.this, "错误信息：" + e.toString(), Toast.LENGTH_SHORT).show();

                }

            }

        };
        new Thread(save).start();


    }

    public void startDecode(String fileName) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "开始反编译", Toast.LENGTH_SHORT).show();
            }
        });

        File file = new File(fileName);
        String path = fileName + "_decode";
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JadxArgs args = new JadxArgs();
        args.setOutDir(new File(path));
        JadxDecompiler jadx = new JadxDecompiler(args);
        try {
            jadx.loadFile(file);
        } catch (JadxException e) {
            e.printStackTrace();
            return;
        }
//        jadx.save();
        List<JavaClass> javaClassList = jadx.getClasses();
        long length = javaClassList.size();
        int i = 1;
        for (JavaClass cls : javaClassList) {
            ClassNode classNode = cls.getClassNode();
            if (classNode.contains(AFlag.DONT_GENERATE)) {
                continue;
            }
            try {
                //cls.decompile();
                String className = classNode.getClassInfo().getFullPath() + ".java";
                String javaCode = cls.getCode();
                if (javaCode == null)

                    if (className.startsWith("android") && className.startsWith("cn"))
                        continue;
                File classFile = new File(path, className);
                File outFile = FileUtils.prepareFile(classFile);
                classFile.deleteOnExit();
                try (PrintWriter out = new PrintWriter(outFile, "UTF-8")) {
                    out.println(javaCode);
                } catch (Exception e) {
                    continue;
                }


            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            if (i % 100 == 0 || i == length) {
                Message msg = new Message();
                msg.obj = i + "/" + length;
                mHandler.sendMessage(msg);
            }

            i++;
//            System.out.println(cls.getCode());
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置Title
        title_recycler_view = (RecyclerView) findViewById(R.id.title_recycler_view);
        title_recycler_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        titleAdapter = new TitleAdapter(MainActivity.this, new ArrayList<TitlePath>());
        title_recycler_view.setAdapter(titleAdapter);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        fileAdapter = new FileAdapter(this, beanList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(fileAdapter);

        empty_rel = (LinearLayout) findViewById(R.id.empty_rel);

        fileAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                if (viewHolder instanceof FileHolder) {
                    FileBean file = beanList.get(position);
                    FileType fileType = file.getFileType();
                    if (fileType == FileType.directory) {
                        tmpFile = file.getPath();
                        getFile(tmpFile);
                        refreshTitleState(file.getName(), file.getPath());
                    } else if (fileType == FileType.apk) {

                        FileUtil.openAppIntent(MainActivity.this, file.getPath());

                    } else if (fileType == FileType.decode || fileType == FileType.txt) {
                        ArrayList info = new ArrayList();
                        info.add(file.getName());
                        info.add(readToString(new File(file.getPath())));
                        FileUtil.openDecodeIntent(MainActivity.this, info);
                    } else {
                        Toast.makeText(MainActivity.this, "提示信息：" + "不支持的文件类型", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        fileAdapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder viewHolder, int position) {


                if (viewHolder instanceof FileHolder) {
                    final FileBean fileBean = (FileBean) fileAdapter.getItem(position);
                    FileType fileType = fileBean.getFileType();
                    if (fileType == FileType.directory)
                        return false;
                    else if (fileType != FileType.apk)
                        showListDialog(fileBean.getPath());
                    else if (fileType == FileType.apk) {
                        deCode(fileBean.getPath());
                    }
                }
                return true;
            }
        });

        titleAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                TitlePath titlePath = (TitlePath) titleAdapter.getItem(position);
                getFile(titlePath.getPath());

                int count = titleAdapter.getItemCount();
                int removeCount = count - position - 1;
                for (int i = 0; i < removeCount; i++) {
                    titleAdapter.removeLast();
                }
            }
        });

        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        refreshTitleState("内部存储设备", rootPath);

        // 先判断是否有权限。
        if (AndPermission.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // 有权限，直接do anything.
            getFile(rootPath);
        } else {
            //申请权限。
            AndPermission.with(this)
                    .requestCode(PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
                    .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .send();
        }
    }

    public String readToString(File file) {
        String encoding = "UTF-8";
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    public void getFile(String path) {
        rootFile = new File(path + File.separator);
        new MyTask(rootFile).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }

    class MyTask extends AsyncTask {
        File file;

        MyTask(File file) {
            this.file = file;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            List<FileBean> fileBeenList = new ArrayList<>();
            if (file.isDirectory()) {
                File[] filesArray = file.listFiles();
                if (filesArray != null) {
                    List<File> fileList = new ArrayList<>();
                    Collections.addAll(fileList, filesArray);  //把数组转化成list
                    Collections.sort(fileList, FileUtil.comparator);  //按照名字排序

                    for (File f : fileList) {
                        if (f.isHidden()) continue;

                        FileBean fileBean = new FileBean();
                        fileBean.setName(f.getName());
                        fileBean.setPath(f.getAbsolutePath());
                        fileBean.setFileType(FileUtil.getFileType(f));
                        fileBean.setChildCount(FileUtil.getFileChildCount(f));
                        fileBean.setSize(f.length());
                        fileBean.setHolderType(0);

                        fileBeenList.add(fileBean);

                        FileBean lineBean = new FileBean();
                        lineBean.setHolderType(1);
                        fileBeenList.add(lineBean);

                    }
                }
            }

            beanList = fileBeenList;
            return fileBeenList;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (beanList.size() > 0) {
                empty_rel.setVisibility(View.GONE);
            } else {
                empty_rel.setVisibility(View.VISIBLE);
            }
            fileAdapter.refresh(beanList);
        }
    }

    void refreshTitleState(String title, String path) {
        TitlePath filePath = new TitlePath();
        filePath.setNameState(title + " > ");
        filePath.setPath(path);
        titleAdapter.addItem(filePath);
        title_recycler_view.smoothScrollToPosition(titleAdapter.getItemCount());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {

            List<TitlePath> titlePathList = (List<TitlePath>) titleAdapter.getAdapterData();
            if (titlePathList.size() == 1) {
                finish();
            } else {
                titleAdapter.removeItem(titlePathList.size() - 1);
                getFile(titlePathList.get(titlePathList.size() - 1).getPath());
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 只需要调用这一句，其它的交给AndPermission吧，最后一个参数是PermissionListener。
        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, listener);
    }

    private PermissionListener listener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, List<String> grantedPermissions) {
            // 权限申请成功回调。
            if (requestCode == PERMISSION_CODE_WRITE_EXTERNAL_STORAGE) {
                getFile(rootPath);
            }
        }

        @Override
        public void onFailed(int requestCode, List<String> deniedPermissions) {
            // 权限申请失败回调。
            AndPermission.defaultSettingDialog(MainActivity.this, PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
                    .setTitle("权限申请失败")
                    .setMessage("我们需要的一些权限被您拒绝或者系统发生错误申请失败，请您到设置页面手动授权，否则功能无法正常使用！")
                    .setPositiveButton("好，去设置")
                    .show();
        }
    };
}
