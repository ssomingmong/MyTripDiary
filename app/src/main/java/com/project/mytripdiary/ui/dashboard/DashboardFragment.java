package com.project.mytripdiary.ui.dashboard;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.project.mytripdiary.R;
import com.project.mytripdiary.database.DbCreate_Diary;
import com.project.mytripdiary.database.DbCreate_PlaceListData;
import com.project.mytripdiary.databinding.FragmentDashboardBinding;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<TripsList> list;

    private TripListAdapter adapter;

    DbCreate_PlaceListData dbHelper;
    SQLiteDatabase db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        //init
        list = new ArrayList<>();

        //id
        recyclerView = view.findViewById(R.id.diary_listViews);
        dbHelper= new DbCreate_PlaceListData(getContext());
        db = dbHelper.getReadableDatabase();
        list =  dbHelper.getRecentTripsList(db);
        Log.d("dash", "listSize : "+list.size());
        if(!list.isEmpty()){
                //아뎁터 연결
            adapter = new TripListAdapter(list,getContext());  // Initialize the adapter
                recyclerView.setAdapter(adapter);
                //아뎁터 이벤트 연결
                adapter.setOnItemClickListener(((position, data) -> {

                }));
                //
        }

        else {
            Log.d("dash", "데이터베이에 여행 목록이 없음");
        }



        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        db.close();
    }
}