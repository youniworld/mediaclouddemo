package app.mediacloud.com.avdemo;

import android.app.Activity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by youni on 2016/11/9.
 */

public class ActivityCallBase extends Activity {
    protected ExecutorService _callExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onBackPressed() {
        _callExecutor.shutdownNow();
        moveTaskToBack(true);
    }
}
