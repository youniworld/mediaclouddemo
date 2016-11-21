package app.mediacloud.com.avdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by youni on 2016/11/21.
 */

public class StartService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("service","StartService onStartCommand");
        super.onStartCommand(intent, flags, startId);

        //
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("service","StartService onDestroy");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.i("service","StartService onCreate");
        super.onCreate();
    }
}
