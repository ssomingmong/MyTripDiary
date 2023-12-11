package com.project.mytripdiary.ui.home;

import static com.project.mytripdiary.Setting.API_KEY;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.project.mytripdiary.R;
import com.project.mytripdiary.database.DbCreate_PlaceListData;
import com.project.mytripdiary.ui.addtrip.placelist.PlaceListAdapter;
import com.project.mytripdiary.ui.addtrip.placelist.PlaceListData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private SQLiteOpenHelper dbOpen ;
    private String nowTrip;
    private String StartDate;
    private String EndDate;
    private int nowDay=0;
    private List<PlaceListData> allList;
    private List<PlaceListData> nowDayList;
    private TextView blankText;
    private RelativeLayout homeWrap;
    private TextView title;
    private TextView dayrangeView;
    private TextView dDayView, dDateView;
    private ImageButton before,foward;
    private MapView mapView = null;
    private RecyclerView placeListVew;
    private PlaceListAdapter placeListAdapter;
    private List<String> daysList,lastPlaces;
    private DbCreate_PlaceListData dbHelper;
    private GoogleMap googleMap;
    private PlacesClient placesClient;
    private FusedLocationProviderClient  fusedLocationProviderClient;
    private  SQLiteDatabase db;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        //아이디 추가
        idLink(view);
        //데이터베이스 연결
        dbHelper = new DbCreate_PlaceListData(getContext());
        db = dbHelper.getReadableDatabase();
        //맵뷰 초기화
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        // Google Places API 초기화
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(),API_KEY);
        }
        placesClient = Places.createClient(getContext());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());


        //리스트 초기화
        allList =  new ArrayList<>();
        nowDayList = new ArrayList<>();
        daysList = new ArrayList<>();
        lastPlaces = new ArrayList<>();
        //데이터베이스에서 최근 여행 위치와 날짜가져오기
        lastPlaces = dbHelper.getLatestPlaceListData(db);
        if(!lastPlaces.isEmpty()){
            Log.i("home","lastPlaces: "+lastPlaces.get(0)+lastPlaces.get(1)+lastPlaces.get(2));
            daysList = getDatesBetween(lastPlaces.get(1),lastPlaces.get(2));
            Log.i("home", "daysList Size: "+daysList.size());

            //여행 최근 여행으로 여행 리스트 채우기
            allList = dbHelper.getPlaceListDataByTripDate(db,lastPlaces.get(0),lastPlaces.get(1),lastPlaces.get(2));
            nowDayList = dayfilter(allList,0);
            blankText.setVisibility(View.GONE);
            homeWrap.setVisibility(View.VISIBLE);
            //제목 체우기
            title.setText(lastPlaces.get(0));
            dayrangeView.setText(lastPlaces.get(1)+"~"+lastPlaces.get(2));
            //디데이 클릭 이벤트 연결
            dDayEvents();
            //아뎁터 연결
            if(!nowDayList.isEmpty()){
                placeListAdapter = new PlaceListAdapter(nowDayList, getContext());
                placeListVew.setAdapter(placeListAdapter);
                placeListAdapter.setOnItemClickListener((position, data) -> {
                    //클릭하면 place정보를 가져와 지도에 띄움
                    showLocationOnMap(data,(position+1)+"");
                });

            }
            else{
                Log.d("home", "nowDayList가 존재하지 않아 어댑터 연결을 못했음 ");
            }
        }
        else {
            Log.d("home", "lastPlace: 마지막 여행 기록이 없음");
            blankText.setVisibility(View.VISIBLE);
            homeWrap.setVisibility(View.GONE);
        }
        Log.i("home","allListsize: "+allList.size());
        Log.i("home","nowListsize: "+nowDayList.size());



        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
    private void idLink(View v){
        blankText = v.findViewById(R.id.text_blank);
        homeWrap = v.findViewById(R.id.home_wrap);
        title = v.findViewById(R.id.home_tripTitle);
        dayrangeView = v.findViewById(R.id.home_dateRange);
        dDayView = v.findViewById(R.id.home_Dday);
        dDateView = v.findViewById(R.id.home_Ddate);
        before = v.findViewById(R.id.home_before);
        foward = v.findViewById(R.id.home_foward);
        mapView = v.findViewById(R.id.home_mapView);
        placeListVew = v.findViewById(R.id.home_listView);
    }
    public void setPoints(List<PlaceListData> list){
        Log.i("maps","nowListSize"+list.size());
        googleMap.clear();
        if(!list.isEmpty()){

            for(int i=0;i<list.size();i++){
                PlaceListData now = list.get(i);
                //now에서 latling을받아와 마커로 표시합니다.
                LatLng latLng = new LatLng(now.getX(), now.getY());
                showLocationOnMap(latLng, (i+1)+"");
            }
            showLineOnMap(list);
        }
    }
    private void dDayEvents(){
        dDayView.setText("Day "+String.valueOf(nowDay+1));
        dDateView.setText(daysList.get(nowDay));
        before.setOnClickListener(view1 -> {
            if(nowDay>0){
                nowDay--;
                dDayView.setText("Day "+String.valueOf(nowDay+1));
                dDateView.setText(daysList.get(nowDay));
                nowDayList = dayfilter(allList, nowDay);
                placeListAdapter.setLists(nowDayList);
                setPoints(nowDayList);
            }
        });
        foward.setOnClickListener(view1 -> {
            if(daysList.size()-1>nowDay){
                nowDay++;
                dDayView.setText("Day "+String.valueOf(nowDay+1));
                dDateView.setText(daysList.get(nowDay));
                nowDayList = dayfilter(allList,nowDay);
                placeListAdapter.setLists(nowDayList);
                setPoints(nowDayList);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        setPoints(nowDayList);
    }
    public List<PlaceListData> dayfilter(List<PlaceListData> list,int day){
        return list.stream()
                .filter(placeListData -> placeListData.getDday() == day)
                .collect(Collectors.toList());
    }
    // 메서드 내에서 검색한 위치를 받아와서 지도에 표시하는 코드 추가
    // 메서드 내에서 검색한 위치를 받아와서 지도에 표시하는 코드 추가
    private void showLocationOnMap(LatLng location,  String position) {


        // 선택한 위치에 새로운 마커 표시
        MarkerOptions markerOptions = new MarkerOptions().position(location);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(position))); // 숫자는 표시할 숫자에 맞게 수정
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }
    private void showLineOnMap(List<PlaceListData> list){
        if(list.size()>1){
            //리스트의 사이즈는 최소 2개가 존재해야 둘 사이의 선을 그릴 수 있음
            for(int i=1;i<list.size();i++){
                LatLng before = new LatLng(list.get(i-1).getX(),list.get(i-1).getY()) ;
                LatLng now = new LatLng(list.get(i).getX(),list.get(i).getY()) ;
                Polyline line = googleMap.addPolyline(new PolylineOptions()
                        .add(before,now)
                        .width(10)
                        .color(Color.GRAY)
                        .clickable(false));
            }
        }

    }
    private Bitmap getMarkerBitmapFromView(String number) {
        View customMarkerView = ((LayoutInflater)requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.markerlayout, null);

        TextView numberTextView = customMarkerView.findViewById(R.id.tv_marker);
        numberTextView.setText(number);

        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());

        customMarkerView.setDrawingCacheEnabled(true);
        Bitmap markerBitmap = Bitmap.createBitmap(customMarkerView.getDrawingCache());
        customMarkerView.setDrawingCacheEnabled(false);

        return markerBitmap;
    }
    public static List<String> getDatesBetween(String startDateStr, String endDateStr) {
        List<String> dateList = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            while (!calendar.getTime().after(endDate)) {
                dateList.add(sdf.format(calendar.getTime()));
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateList;
    }
    @Override
    public void onResume() {

        super.onResume();
        if (mapView != null)
            mapView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        db.close();
        Log.d("frag", "onDestroy: home");
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}