package app.mediacloud.com.avdemo;

import android.app.Application;
import android.util.Log;

import com.mmc.android.sdk.MMCSDK;
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
            MMCSDK.InitRealTimeVideo("if.appsvr.hpsp.hifun.mobi:5001", "1a5eb7ca0ed54503b3ca854d0ed8a725");
        }catch (Throwable e){
            e.printStackTrace();
            Log.d("MediaApplication", "onCreate: " + e.toString());
        }

        AppModel.getInstance().Init(this);
    }
}
