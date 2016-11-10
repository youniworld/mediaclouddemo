package app.mediacloud.com.avdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

/**
 * Created by youni on 2016/10/25.
 */

public class ActivitySplash extends Activity {
    private Handler H = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0){
            finish();
            return;
        }

        setContentView(R.layout.activity_splash);

        H.postDelayed(new Runnable() {
            @Override
            public void run() {
                String pwd = AppModel.getInstance().getPwd();

                if(!TextUtils.isEmpty(pwd)){
                    String uid = AppModel.getInstance().getUid();
                    String portal = AppModel.getInstance().getPortal();
                    AppModel.getInstance().AutoLogin(uid, pwd, portal);

                    startActivity(new Intent(ActivitySplash.this, ActivityMain.class));
                }else{
                    startActivity(new Intent(ActivitySplash.this, ActivityLogin.class));
                }

                finish();
            }
        }, 1000);
    }
}
