package app.mediacloud.com.avdemo;

import android.app.Application;
import android.util.Log;

import com.vlee78.android.media.MediaSdk;

/**
 * Created by youni on 2016/11/3.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            MediaSdk.init(this, false);
        }catch (Throwable e){
            e.printStackTrace();
            Log.d("MediaApplication", "onCreate: " + e.toString());
        }

        AppModel.getInstance().Init(this);
    }
}
