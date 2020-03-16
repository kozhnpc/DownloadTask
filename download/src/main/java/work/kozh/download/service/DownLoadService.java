package work.kozh.download.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import work.kozh.download.R;
import work.kozh.download.listener.DownLoadListener;
import work.kozh.download.task.DownLoadTask;

/**
 * 下载用Service
 */
public class DownLoadService extends Service {

    private DownLoadTask downLoadTask;
    private String downLoadUrl;
    private Class mClass;

    public DownLoadService() {

    }

    private DownLoadBinder downLoadBinder = new DownLoadBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return downLoadBinder;
    }

    /**
     * Binder类
     */
    public class DownLoadBinder extends Binder {
        /**
         * 开始下载
         *
         * @param url
         * @param clazz 点击通知后，需要跳转的页面
         */
        public void startDownLoad(String url, Class clazz) {
            if (downLoadTask == null) {
                downLoadUrl = url;
                downLoadTask = new DownLoadTask(downLoadListener);
                downLoadTask.execute(downLoadUrl);
                DownLoadService.this.mClass = clazz;
                startForeground(1, getNotification("文件下载", 0, clazz));
                Toast.makeText(DownLoadService.this, "开始下载文件...", Toast.LENGTH_LONG).show();
            }
        }

        /**
         * 暂停下载
         */
        public void pauseDownLoad() {
            if (downLoadTask != null) {
                downLoadTask.pauseDownLoad();
            }
        }

        /**
         * 取消下载
         */
        public void cancelDownLoad() {
            if (downLoadTask != null) {
                downLoadTask.cancelDownLoad();
            }
            //文件删除
            if (downLoadUrl != null) {
                String fileName = downLoadUrl.substring(downLoadUrl.lastIndexOf("/"));
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                File file = new File(directory, fileName);
                if (file.exists()) {
                    file.delete();
                }
                getNotificationManager().cancel(1);
                stopForeground(true);
                Toast.makeText(DownLoadService.this, "下载任务取消！", Toast.LENGTH_LONG).show();
            }
        }

    }


    private DownLoadListener downLoadListener = new DownLoadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("正在下载", progress, mClass));
        }

        @Override
        public void onSuccess() {
            downLoadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载完成", -1, mClass));
            Toast.makeText(DownLoadService.this, "下载完成！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downLoadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载失败", -1, mClass));
            Toast.makeText(DownLoadService.this, "下载失败！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downLoadTask = null;
            Toast.makeText(DownLoadService.this, "下载暂停！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downLoadTask = null;
            stopForeground(true);
            Toast.makeText(DownLoadService.this, "下载取消！", Toast.LENGTH_SHORT).show();
        }
    };


    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    /**
     * 显示通知
     *
     * @param title
     * @param progress
     * @return
     */
    private Notification getNotification(String title, int progress, Class clazz) {
        Intent intent = new Intent(this, clazz);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建渠道
            String id = "download_channel";
            String name = "文件下载用服务";
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
            getNotificationManager().createNotificationChannel(mChannel);
            //设置图片,通知标题,发送时间,提示方式等属性
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id);
            builder.setContentTitle(title)  //标题
                    .setWhen(System.currentTimeMillis())    //系统显示时间
                    .setSmallIcon(R.drawable.download)     //收到信息后状态栏显示的小图标
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.download))//大图标
                    .setAutoCancel(true);       //设置点击后取消Notification
            builder.setContentIntent(pendingIntent);    //绑定PendingIntent对象
            if (progress >= 0) {
                builder.setContentText(progress + "%");
                builder.setProgress(100, progress, false);
            }
            return builder.build();
        } else {
            //设置图片,通知标题,发送时间,提示方式等属性
            Notification.Builder builder = new Notification.Builder(this);
            builder.setContentTitle(title)  //标题
                    .setWhen(System.currentTimeMillis())    //系统显示时间
                    .setSmallIcon(R.drawable.download)     //收到信息后状态栏显示的小图标
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.download))//大图标
                    .setAutoCancel(true);       //设置点击后取消Notification
            builder.setContentIntent(pendingIntent);    //绑定PendingIntent对象
            if (progress >= 0) {
                builder.setContentText(progress + "%");
                builder.setProgress(100, progress, false);
            }
            return builder.build();
        }
    }


}
