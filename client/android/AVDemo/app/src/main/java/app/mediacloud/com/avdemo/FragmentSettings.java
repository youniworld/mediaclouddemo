package app.mediacloud.com.avdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by youni on 2016/11/7.
 */

public class FragmentSettings extends Fragment {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button logout = (Button) getActivity().findViewById(R.id.btn_logout);

        logout.setText("Logout(" + AppModel.getInstance().getUid() + ")");

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppModel.getInstance().Logout();

                getActivity().startActivity(new Intent(getActivity(),ActivityLogin.class));
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings,container,false);
    }
}
