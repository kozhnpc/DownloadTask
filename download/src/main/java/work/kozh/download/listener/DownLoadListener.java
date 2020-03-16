package work.kozh.download.listener;

/**
 * 下载过程回调
 */
public interface DownLoadListener {

    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();

}
