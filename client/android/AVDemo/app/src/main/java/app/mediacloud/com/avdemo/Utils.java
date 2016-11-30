package app.mediacloud.com.avdemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by youni on 2016/11/10.
 */

public class Utils {
    public static String KDefault_Portal = "yaobo.mediacloud.app";

    private static Utils _instance = new Utils();
    private Context _context;

    public void init(Context context){
        _context = context;
    }

    public static Utils getInstance() {
        return _instance;
    }

    public boolean hasConnection(){
        Context appContext = _context.getApplicationContext();

        ConnectivityManager manager = (ConnectivityManager)appContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo[] infos = manager.getAllNetworkInfo();

        if (infos == null || infos.length <= 0){
            return false;
        }

        for (NetworkInfo info:infos){
            if (info.isConnected()){
                return true;
            }
        }

        return false;
    }
}
