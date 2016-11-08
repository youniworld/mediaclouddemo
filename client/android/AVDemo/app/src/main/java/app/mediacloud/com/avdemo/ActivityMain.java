package app.mediacloud.com.avdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;

public class ActivityMain extends FragmentActivity {
    private Fragment _confCall = new FragmentConfCall();
    private Fragment _p2pCall = new FragmentP2P();
    private Fragment _settings = new FragmentSettings();

    private Fragment _currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RadioGroup group = (RadioGroup) findViewById(R.id.rg_main_tab);

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                Fragment fragment = null;
                if(R.id.rb_p2p_call == id){
                    fragment = _p2pCall;

                }else if (R.id.rb_conf_call == id){
                    fragment = _confCall;

                }else if (R.id.rb_setting == id){
                    fragment = _settings;
                }

                if (fragment != null){
                    switchToFrament(fragment);
                }
            }
        });

        group.check(R.id.rb_p2p_call);

    }

    void switchToFrament(Fragment fragment){
        if (_currentFragment == fragment){
            return;
        }

        _currentFragment = fragment;
        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_main_container,_currentFragment);
        ft.commit();
    }
}
