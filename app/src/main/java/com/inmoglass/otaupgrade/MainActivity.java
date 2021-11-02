package com.inmoglass.otaupgrade;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RecoverySystem;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.inmoglass.otaupgrade.bean.DownloadBean;
import com.inmoglass.otaupgrade.download.DownloadListner;
import com.inmoglass.otaupgrade.download.DownloadManager;
import com.inmoglass.otaupgrade.utils.ToastUtil;
import com.tencent.mmkv.MMKV;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private boolean isNetworkOK = false; //是否有网络
    private String downLoadPath; //创建的文件路径
    boolean isHasStoragePermission = false; //文件权限 //有无读写权限
    private int ElectricQuantity = 14;
    private static final String MODEL = "X";
    private static final String VERSION = "1.01";
    private static final String REQUEST_URL = "http://47.108.221.230:4041/im/version/update?model=" + MODEL + "&versionNumber=" + VERSION;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String downloadLink = ""; //下载的链接
    private TextView viewLog;//查看日志
    private String logContent = null;//日志的内容
    private ImageView imageBack;//图片返回按钮
    private Context context;
    private ScrollView scrollewLogContent;
    private TextView tvViewLogContext;
    private String Filepath;//文件的路径
    private TextView tvChangeDownlod; //下载的字
    private ProgressBar downloadProgressBar; //下载的进度条
    private ProgressBar pbUpdataNow;
    private TextView tvUpdataNow;
    private String curLog;//日志保存
    private boolean isFileExists = false; //是否有这个文件
    private DownloadManager downloadManager;
    private File RECOVERY_DIR = new File("cache/recovery");
    private File COMMAND_FILE = new File(RECOVERY_DIR, "command");
    private MMKV kv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        //一进来开始去读取
        kv = MMKV.defaultMMKV();
        curLog = kv.decodeString("logSave", "0");
        Log.d(TAG, "curLog: " + curLog);
        initView(); //显示布局
        permissionApplication();//权限申请
        listenBtnEvents(); //事件处理
        Log.d(TAG, "" + REQUEST_URL);
        isNetworkConnections();
//        createFile();
//        new dataRequest().execute(requestUrl);
    }

    /**
     * 判断网络
     */
    public void isNetworkConnections() {
        ConnectivityManager connManager = (ConnectivityManager) this
                .getSystemService(CONNECTIVITY_SERVICE);
        // 获取代表联网状态的NetWorkInfo对象
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        // 获取当前的网络连接是否可用
        if (null == networkInfo) {
//            Toast.makeText(this, "当前的网络连接不可用", Toast.LENGTH_SHORT).show();
            // 当网络不可用时，跳转到网络设置页面
//            startActivityForResult(new Intent(
//                    android.provider.Settings.ACTION_WIRELESS_SETTINGS), 1);

        } else {
            boolean available = networkInfo.isAvailable();
            if (available) {
                new dataRequest().execute(REQUEST_URL);//网络请求
//                Log.i("通知", "当前的网络连接可用");
//                Toast.makeText(this, "当前的网络连接可用", Toast.LENGTH_SHORT).show();
            } else {
//                Log.i("通知", "当前的网络连接不可用");
//                ToastUtil.showTextToas(getApplicationContext(),""+R.string.The_network_is_not_connected);
            }
        }

        NetworkInfo.State state = connManager.getNetworkInfo(
                ConnectivityManager.TYPE_MOBILE).getState();
        if (NetworkInfo.State.CONNECTED == state) {
//            Log.i("通知", "GPRS网络已连接");
//            Toast.makeText(this, "GPRS网络已连接", Toast.LENGTH_SHORT).show();
        }

        state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState();
        if (NetworkInfo.State.CONNECTED == state) {
//            Log.i("通知", "WIFI网络已连接");
//            Toast.makeText(this, "WIFI网络已连接", Toast.LENGTH_SHORT).show();
        }

        // // 跳转到无线网络设置界面
        // startActivity(new
        // Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        // // 跳转到无限wifi网络设置界面
        // startActivity(new
        // Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));

    }

    /**
     * 数据请求
     */
    class dataRequest extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(params[0])
                    .build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                finish();
                e.printStackTrace();
            }
            return null;

        }


        @Override
        protected void onPostExecute(String s) {
            DownloadBean downloadBean = JSON.parseObject(s, DownloadBean.class);
            isNetworkOK = true;
            logContent = downloadBean.getData().getNote();
            downloadLink = downloadBean.getData().getDownloadUrl();
            initDownloads();
            if (logContent != null) {
                kv.encode("logSave", logContent);
            }
//            Log.d(TAG, "数据转换" + logContent + downloadLink);
        }
    }

    public void initDownloads() {
        downloadManager = DownloadManager.getInstance();
        downloadManager.add(downloadLink, new DownloadListner() {
            @Override
            public void onFinished() {
                Filepath = DownloadManager.getInstance().getIsConnect();
//                Log.d("让我看看这个数据", "文件存储位置" + Filepath);
                tvChangeDownlod.setVisibility(View.VISIBLE);
                downloadProgressBar.setVisibility(View.VISIBLE);
                pbUpdataNow.setVisibility(View.GONE);
                tvUpdataNow.setVisibility(View.GONE);
                isFileExists = true;
//                Toast.makeText(MainActivity.this, "下载完成!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(float progress) {
                downloadProgressBar.setProgress((int) (progress * 100));
                tvChangeDownlod.setText(R.string.app_updating_xia + (String.format("%.2f", progress * 100) + "%"));
//                Log.d("数据传递", String.format("%.2f", progress * 100) + "%");

            }

            @Override
            public void onPause() {
//                Toast.makeText(MainActivity.this, "暂停了!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                downloadProgressBar.setProgress(0);
//                Toast.makeText(MainActivity.this, "下载已取消!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //按钮点击
    private void listenBtnEvents() {
        //点击安装
        pbUpdataNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                installUpdateFile(ElectricQuantity);
            }
        });
        //点击下载
        downloadProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFile();
                isNetworkConnections();//网络权限
                //判断是否有读写权限
                if (isHasStoragePermission == false) {
                    ToastUtil.showTextToats(getApplicationContext(), "" + R.string.app_no_read_write);
                } else {
                    downloadManager.download(downloadLink);
                }
//                if (isFileExists==false){
//                  //再去判断是否有读写权限
//                    if (isFileRight==false){
//                        ToastUtil.showTextToas(getApplicationContext(),""+R.string.app_no_read_write);
//                    }else {
//                        isTheFileDuplicate();
//                        Log.d(TAG,"是否执行"+downloadLink);
//                        downloadManager.download(downloadLink);
//                    }
//                    Log.d(TAG,"是否执行");
//                }else {
                //直接开始安装
//                }
            }
        });
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        viewLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isNetworkOK = false;
//                new dataRequest().execute(requestUrl);
                Log.d(TAG, "1" + curLog);
                if (!curLog.equals("0")) {
//                    Log.d(TAG,"是否执行");
                    scrollewLogContent.setVisibility(View.VISIBLE);
                    tvViewLogContext.setText(curLog);
                } else {
                    ToastUtil.showTextToats(getApplicationContext(), "" + R.string.app_no_data);

                }
            }
        });
    }

    private void initView() {
        tvChangeDownlod = findViewById(R.id.text_change_download_updates);//下载
        tvViewLogContext = findViewById(R.id.sc_tv_log);
        scrollewLogContent = findViewById(R.id.base_update_scrollew);
        viewLog = findViewById(R.id.base_update_tx_log);
        downloadProgressBar = findViewById(R.id.pb_progress1); //下载
        imageBack = findViewById(R.id.update_image_back);
        pbUpdataNow = findViewById(R.id.pb_progress2);//安装
        tvUpdataNow = findViewById(R.id.text_restart_update);//安装
    }

    private void permissionApplication() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            createFile();
        }
    }

    //开始创建文件
    private void createFile() {
        boolean result = true;
        File file = new File("/data/media/", "");
        Log.d("infoo", "falisroute" + file.getAbsolutePath());
        downLoadPath = file.getAbsolutePath();
//        System.out.println("--------"+DOWN_LOAD_PATH);
        if (!file.exists()) {
            file.mkdirs();
            result = file.mkdirs();
        } else {
            Log.d("infoo", "FileTrue!");
//            switch_configurefile();
            isTheFileDuplicate();
        }
        if (result) {
            isHasStoragePermission = true;
        } else {
            isHasStoragePermission = false;
            Log.d("infoo", "FileFalse!");
        }
    }

    public ArrayList<String> getFileName(String fileAbsolutePaht, String type) {
        ArrayList<String> result = new ArrayList<String>();
        File file = new File(fileAbsolutePaht);
        File[] files = file.listFiles();
        Log.e(TAG, "file" + files);
        if (files == null) {

        } else {
            for (int i = 0; i < files.length; ++i) {
                if (!files[i].isDirectory()) {
                    String fileName = files[i].getName();
                    if (fileName.trim().toLowerCase().endsWith(type)) {
                        result.add(fileName);
                    }
                }
            }
        }
        return result;
    }

    //这个isTheFileDuplicate方法执行的时候一定要有这个文件夹才可以，就是我上面创建的文件夹，那个权限是一定要可以的
    private void isTheFileDuplicate() {
        String switch_configurefilePath = "update.zip";
        //这个方法是获取内部存储的根路径
        //getFilesDir().getAbsolutePath() =/data/user/0/packname/files
        String path = "/data/media";
        boolean pdtemp = false;

        ArrayList<String> ss = getFileName(path, ".zip");
        for (String s : ss) {
            Log.d("TAG", "result:" + s);
            if (s.equals(switch_configurefilePath)) pdtemp = true;
        }

//        File file = new File(switch_configurefilePath);
        if (!pdtemp) {// 文件不存在
            Log.d("TAG", "filenotexistence");
        } else {
            isFileExists = true;
            tvChangeDownlod.setVisibility(View.GONE);
            downloadProgressBar.setVisibility(View.GONE);
            pbUpdataNow.setVisibility(View.VISIBLE);
            tvUpdataNow.setVisibility(View.VISIBLE);
            Log.d("TAG", "fileexistence");

        }
    }

    public void installUpdateFile(int state) {
//        RECOVERY_DIR.mkdirs();
//        COMMAND_FILE.delete();
        try {
            FileWriter command = new FileWriter(COMMAND_FILE);
//            Log.i(TAG, "--update_package=" + mStorage.getStorageFilePath());
            command.write("--update_package=/storage/sdcard0/media/update.zip");
            Log.i(TAG, "command is write");
            command.write("\n");
            command.close();
        } catch (IOException e) {
            Log.e(TAG, "command is failed");
            e.printStackTrace();
        }
        Log.d(TAG, "Start looking for files");
        File file = new File("/data/media/", "update.zip");
        try {
            //签名校验
            RecoverySystem.verifyPackage(file, new RecoverySystem.ProgressListener() {
                @Override
                public void onProgress(int progress) {
//                    LogHelper.getInstance().d("签名校验进度:" + progress);
                }
            }, null);
            Log.d(TAG, "Signature verification passed");
            //升级
            Log.d(TAG, "To upgrade steps");
            RecoverySystem.installPackage(this, file);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        mStorage.setUpgrade(true);
//        mStorage.setState(state);
//        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//        pm.reboot("recovery");
    }
}