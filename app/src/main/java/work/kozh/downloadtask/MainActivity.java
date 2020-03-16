package work.kozh.downloadtask;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import work.kozh.download.service.DownLoadService;

public class MainActivity extends AppCompatActivity {


    private static final int WRITE_PERMISSION_CODE = 1000;
    //文件下载链接
    private String url = "http://kozhnpc.work/ONES.apk";

    private Context mContext;

    private DownLoadService.DownLoadBinder mDownLoadBinder;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownLoadBinder = (DownLoadService.DownLoadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkUserPermission();

        Intent intent = new Intent(this, DownLoadService.class);
//        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

    }

    /**
     * 检测权限
     */
    private void checkUserPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext, "拒绝权限将无法开启下载服务", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * 暂停下载
     *
     * @param view
     */
    public void pause(View view) {
        mDownLoadBinder.pauseDownLoad();
    }

    /**
     * 取消下载
     *
     * @param view
     */
    public void cancel(View view) {
        mDownLoadBinder.cancelDownLoad();

    }

    /**
     * 开始下载
     *
     * @param view
     */
    public void download(View view) {
        mDownLoadBinder.startDownLoad(url, MainActivity.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //虽然取消绑定，但是服务的方法仍然会在后台执行
        unbindService(mServiceConnection);
    }
}
