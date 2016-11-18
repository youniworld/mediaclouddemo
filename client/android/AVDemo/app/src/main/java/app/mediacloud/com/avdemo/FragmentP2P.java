package app.mediacloud.com.avdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by youni on 2016/11/7.
 */

public class FragmentP2P extends Fragment {
    private ListView _peopleList;
    private Handler _H = new Handler();
    private PeopleAdapter _adapter;
    private SearchView _searchView;
    private ExecutorService _executor = Executors.newSingleThreadExecutor();

    private OnConnectionListener _connectionListener = new OnConnectionListener() {
        @Override
        public void OnConnected() {
            refresh();
        }

        @Override
        public void OnDisconnected(ErrorCode error) {

        }
    };

    private AppModel.OnUserStateChangedListener _userStateChangeListener = new AppModel.OnUserStateChangedListener() {
        @Override
        public void OnUserStateChanged(String uid, People.State state) {
            final List<People> users = AppModel.getInstance().getAllUsers();

            if (users == null){
                return;
            }

            for (People people:users){
                if(people.get_uid().equals(uid)){
                    people.set_state(state);

                    _H.post(new Runnable() {
                        @Override
                        public void run() {
                            _adapter.refresh(new ArrayList<People>(users));
                        }
                    });

                    break;
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppModel.getInstance().addConnectionListener(_connectionListener);
        AppModel.getInstance().addStateChangeListener(_userStateChangeListener);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        _peopleList = (ListView) getActivity().findViewById(R.id.lv_people_list);

        final SwipeRefreshLayout srl = (SwipeRefreshLayout) getActivity().findViewById(R.id.srl_refesh_people);

        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                _executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        final List<People> users = AppModel.getInstance().getAllUsersFromServer();

                        _H.post(new Runnable() {
                            @Override
                            public void run() {
                                _adapter.refresh(users);

                                _adapter.notifyDataSetChanged();

                                srl.setRefreshing(false);
                            }
                        });
                    }
                });
            }
        });

        _adapter = new PeopleAdapter(getActivity());

        _peopleList.setAdapter(_adapter);
        //_peopleList.setTextFilterEnabled(true);

        _peopleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                People people = (People) adapterView.getAdapter().getItem(i);

                Intent intent = new Intent(getActivity(),ActivityDialCall.class);
                intent.putExtra("to",people.get_uid());

                getActivity().startActivity(intent);

            }
        });

        _searchView = (SearchView) getActivity().findViewById(R.id.sv_people_search);

        _searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    filter(newText);
                }else{
                    refresh();
                }

                return false;
            }
        });

    }

    void filter(String filterString){
        final List<People> users = AppModel.getInstance().getAllUsers();

        if (users.size() <= 0){
            return;
        }

        List<People> filterList = new ArrayList<People>();

        for(People pl : users){
            if (pl.get_uid().contains(filterString)){
                filterList.add(pl);
            }
        }

        _adapter.refresh(filterList);

        _adapter.notifyDataSetChanged();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_p2p,container,false);
    }


    void refresh(){
        _executor.execute(new Runnable() {
            @Override
            public void run() {
                List<People> peoples = AppModel.getInstance().getAllUsers();

                if (peoples == null || peoples.isEmpty()){
                    peoples = AppModel.getInstance().getAllUsersFromServer();
                }

                final List<People> users = peoples;

                _H.post(new Runnable() {
                    @Override
                    public void run() {
                        _adapter.refresh(users);

                        _adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        AppModel.getInstance().removeConnectionListener(_connectionListener);
        AppModel.getInstance().removeStateChangeListener(_userStateChangeListener);
    }
}

class PeopleAdapter extends BaseAdapter {

    private Context _context;
    private List<People> _people;
    public PeopleAdapter(Context context) {
        _context = context;
    }

    public void refresh(List<People> users){
        _people = users;

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (_people != null){
            return _people.size();
        }

        return 0;
    }

    @Override
    public People getItem(int i) {
        if (_people != null){
            return _people.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(_context);

        ViewHolder holder = null;

        if (convertView == null){
            convertView = inflater.inflate(R.layout.row_people,null);

            TextView nick = (TextView) convertView.findViewById(R.id.tv_people_nick);
            ImageView state = (ImageView) convertView.findViewById(R.id.iv_peope_state);

            holder = new ViewHolder();

            holder._nick = nick;
            holder._state = state;

            convertView.setTag(holder);
        }

        holder = (ViewHolder) convertView.getTag();

        People people = getItem(position);

        holder._nick.setText(people.get_nick());

        if (people.get_state() == People.State.EOnline){
            holder._state.setImageResource(R.drawable.presence_online);
        }else{
            holder._state.setImageResource(R.drawable.presence_offline);
        }

        return convertView;
    }

    static class ViewHolder{
        TextView _nick;
        ImageView _state;
    }

}
