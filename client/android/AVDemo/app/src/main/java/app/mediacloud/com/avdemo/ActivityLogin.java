package app.mediacloud.com.avdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by youni on 2016/10/25.
 */

public class ActivityLogin extends Activity {

    private Button btnLogin;
    private Button btnRegister;
    private EditText _etPortal;
    GestureDetector _detector;
    private ProgressDialog progressDialog;

    private ActivityLogin me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0){
            finish();
            return;
        }

        me = this;

        setContentView(R.layout.activity_login);

        _etPortal = (EditText) findViewById(R.id.et_login_portal);
        _etPortal.setVisibility(View.INVISIBLE);

        _detector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (_etPortal.getVisibility() == View.VISIBLE){
                    _etPortal.setVisibility(View.INVISIBLE);
                }else{
                    _etPortal.setVisibility(View.VISIBLE);
                }

                return true;
            }
        });

        getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                _detector.onTouchEvent(event);

                return false;
            }
        });

        btnLogin = (Button) findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog pd = new ProgressDialog(me);
                pd.show();

                if(!Utils.getInstance().hasConnection()){
                    ShowToast("please check the network connection", pd);

                    return;
                }

                EditText uid = (EditText) findViewById(R.id.et_login_name);
                EditText pwd = (EditText) findViewById(R.id.et_login_pwd);

                String portal = "";

                if (_etPortal.getVisibility() == View.VISIBLE){
                    portal = _etPortal.getText().toString();

                }else{
                    portal = Utils.KDefault_Portal;
                }

                if (TextUtils.isEmpty(uid.getText()) || TextUtils.isEmpty(pwd.getText()) || TextUtils.isEmpty(portal)){
                    ShowToast("name or pwd or portal is empty",pd);
                    return;
                }

                final String uidStr = uid.getText().toString();
                final String pwdStr = pwd.getText().toString();
                final String fportal = portal;

                AppModel.getInstance().Login(uid.getText().toString(), pwd.getText().toString(), portal, new ICallback() {
                    @Override
                    public void OnSuccess() {
                        ShowToast("login successfully",pd);

                        AppModel.getInstance().saveUser(uidStr,pwdStr,fportal);

                        AppModel.getInstance().saveToken(AppModel.getInstance().get_LoginToken(),uidStr);
                        
                        me.startActivity(new Intent(me, ActivityMain.class));

                        finish();
                    }

                    @Override
                    public void OnFailed(ErrorCode error) {
                        ShowToast("login failed ",pd);
                    }
                });
            }
        });

        btnRegister = (Button) findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog pd = new ProgressDialog(me);
                pd.show();

                if(!Utils.getInstance().hasConnection()){
                    ShowToast("please check the network connection", pd);
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        EditText uid = (EditText) findViewById(R.id.et_login_name);
                        EditText pwd = (EditText) findViewById(R.id.et_login_pwd);

                        String portal = "";

                        if (_etPortal.getVisibility() == View.VISIBLE){
                            portal = _etPortal.getText().toString();

                        }else{
                            portal = Utils.KDefault_Portal;
                        }

                        if (TextUtils.isEmpty(uid.getText()) || TextUtils.isEmpty(pwd.getText()) || TextUtils.isEmpty(portal)){
                            ShowToast("name or pwd or portal is empty",pd);
                            return;
                        }

                        ErrorCode err = AppModel.getInstance().Register(uid.getText().toString(),pwd.getText().toString(),portal);

                        if (err == ErrorCode.KErrorNone){
                            ShowToast("register successfully",pd);
                        }else if (err == ErrorCode.KErrorUserExisted){
                            ShowToast("User is already existed",pd);
                        }else{
                            ShowToast("Register failed",pd);
                        }
                    }
                }).start();
            }
        });

    }

    private void ShowToast(final String msg, final ProgressDialog pd){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(pd != null){
                    pd.cancel();
                }

                Toast.makeText(me,msg,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
