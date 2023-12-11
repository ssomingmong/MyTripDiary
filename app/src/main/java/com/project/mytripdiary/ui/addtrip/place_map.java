package com.project.mytripdiary.ui.addtrip;

import static com.project.mytripdiary.Setting.API_KEY;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.mytripdiary.R;
import com.project.mytripdiary.database.DbCreate_PlaceListData;
import com.project.mytripdiary.ui.addtrip.placelist.PlaceListAdapter;
import com.project.mytripdiary.ui.addtrip.placelist.PlaceListData;
import com.project.mytripdiary.ui.home.HomeFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


public class place_map extends Fragment implements OnMapReadyCallback {


    private String nowTripPlace;
    private String startDate;
    private String endDate;
    private GoogleMap googleMap;
    private MapView mapView =null;
    private RecyclerView placeListRecyclerView;
    private TextView searchText;
    private TextView listTitle;
    private TextView dDayTitle;
    private TextView dDateTitle;
    private ImageButton dayleft;
    private ImageButton dayRight;

    private RelativeLayout searchBtn;
    private ImageButton toolbarBackBtn;
    private AppCompatButton toolbarBtn;
    private PlacesClient placesClient;
    private  FusedLocationProviderClient fusedLocationProviderClient;
    private List<PlaceListData> placeListDataList;
    private List<PlaceListData> nowList;
    private PlaceListAdapter placeListAdapter;
    private int dDay=0;
    private List<String> days;
    private DbCreate_PlaceListData dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_place_map, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            // Bundle에서 여행지 정보, 출발 날짜, 도착 날짜를 가져옴
            nowTripPlace = bundle.getString("nowTripPlace");
            startDate = bundle.getString("startDate");
            endDate = bundle.getString("endDate");
        }
        //아이디 등록
       id_adder(view);
        //데이터베이스 켜기
        dbHelper = new DbCreate_PlaceListData(getContext());
        dbHelper.getWritableDatabase();
        //맵뷰 초기화
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        // Google Places API 초기화
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(),API_KEY);
        }
        //여행 리스트 초기화및 아뎁터 연결
        placeListDataList = new ArrayList<>();
        nowList = new ArrayList<>();
        days = getDatesBetween(startDate,endDate);
        placeListAdapter =new PlaceListAdapter(placeListDataList,getContext());
        placeListRecyclerView.setAdapter(placeListAdapter);

        placesClient = Places.createClient(getContext());
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
         //return after the user has made a selection.
        //클릭 리스너 연결-----------------
        toolbar_events();
        D_day_events();
        searchBtn.setOnClickListener(this::startSearch);
        //아뎁터 클릭 이벤트구현
        placeListAdapter.setOnItemClickListener((position, data) -> {
            //클릭하면 place정보를 가져와 지도에 띄움
            showLocationOnMap(data,(position+1)+"");
        });
        return view;

    }

    //초기 구글맵 설정
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.clear();

            // 맵이 준비되었을 때 호출되는 콜백에서 기본 위치를 서울로 지정합니다.
            LatLng seoul = new LatLng(37.5665, 126.9780);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 10));  // 줌 레벨을 조절합니다.

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
    // Autocomplete를 통해 Place를 가져오고 이를 mapView에 마커와 라인으로 표시하는 메서드
    private void handlePlaceSelection(Intent data) {
        Place place = Autocomplete.getPlaceFromIntent(data);
        Log.i("maps", "Place: " + place.getName() + ": " + place.getAddress()+":"+place.getLatLng());
        // 여행 리스트에 아이템 추가
        PlaceListData NewplaceListData = new PlaceListData(nowTripPlace,startDate,endDate,dDay,place.getName(),place.getAddress(),place.getLatLng().latitude,place.getLatLng().longitude);
        placeListDataList.add(NewplaceListData);


        //nowList에 존재하는 데이터 변경
        nowList = placeListDataList.stream()
                .filter(placeListData -> placeListData.getDday() == dDay)
                .collect(Collectors.toList());
        placeListAdapter.setLists(nowList);
        Log.i("maps","nowListSize"+nowList.size());
        // 검색한 위치를 지도에 표시
        showLocationOnMap(place.getLatLng(),nowList.size()+"");
        showLineOnMap(nowList);

    }
    private void id_adder(View view){
        //id찾기-------------------------
        placeListRecyclerView = view.findViewById(R.id.addtrip_map_placeList);
        listTitle = view.findViewById(R.id.addtrip_map_listTitle);
        searchBtn = view.findViewById(R.id.addtrip_map_searchWrap);
        toolbarBackBtn =view.findViewById(R.id.addtrip_toolbar_backbtn);
        toolbarBtn =view.findViewById(R.id.addtrip_toolbarbtn);
        mapView =view.findViewById(R.id.addtrip_map_map);
        searchText = view.findViewById(R.id.addtrip_map_searchText);
        placeListRecyclerView = view.findViewById(R.id.addtrip_map_placeList);
        dayleft = view.findViewById(R.id.map_left);
        dayRight = view.findViewById(R.id.map_right);
        dDayTitle = view.findViewById(R.id.map_dday);
        dDateTitle = view.findViewById(R.id.map_ddate);
    }
    private void D_day_events(){
        //디데이 버튼 전환 이벤트 구현
        dDayTitle.setText("Day "+String.valueOf(dDay+1));
        dDateTitle.setText(days.get(dDay));
        dayleft.setOnClickListener(view1 -> {
            if(dDay>0){
                dDay--;
                dDayTitle.setText("Day "+String.valueOf(dDay+1));
                dDateTitle.setText(days.get(dDay));
                nowList = placeListDataList.stream()
                        .filter(placeListData -> placeListData.getDday() == dDay)
                        .collect(Collectors.toList());
                placeListAdapter.setLists(nowList);
                setPoints(nowList);
            }
        });
        dayRight.setOnClickListener(view1 -> {
            if(days.size()-1>dDay){
                dDay++;
                dDayTitle.setText("Day "+String.valueOf(dDay+1));
                dDateTitle.setText(days.get(dDay));
                nowList = placeListDataList.stream()
                        .filter(placeListData -> placeListData.getDday() == dDay)
                        .collect(Collectors.toList());
                placeListAdapter.setLists(nowList);
                setPoints(nowList);
            }
        });
    }
    private void toolbar_events(){
        //백 버튼 세팅(toolbarBackbtn에 뒤로가기 이벤트를 처리합니다.)
        // 백 버튼 클릭 이벤트 처리
        toolbarBackBtn.setOnClickListener(view1 ->
                getParentFragmentManager().popBackStack());
        //틀바 버튼 클릭 이벤트
        toolbarBtn.setOnClickListener(view1 ->{
            //저장 완료 기능 구현
            save_data();
            //데이터베이스에 저장했으니 이제 홈 프래그먼트로 돌아감
            // 모든 백 스택 제거
            getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_activity_main,new HomeFragment()).commitAllowingStateLoss();

        });
        toolbarBtn.setText("저장");

    }
    private void save_data(){
        //내부 데이터베이스에 저장
        for (PlaceListData data:placeListDataList) {
            dbHelper.addPlaceListData(data);
        }
        Log.d("maps", "save_data: "+placeListDataList.size());
    }



    // Autocomplete 인텐트를 실행하는 메서드
    public void startSearch(View view) {
        // Start the autocomplete intent.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG);

        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(getContext());

        startAutocomplete.launch(intent);
    }

    private final ActivityResultLauncher<Intent> startAutocomplete = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        // Autocomplete 결과를 처리하는 메서드 호출
                        handlePlaceSelection(data);
                    }
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                    Status status = Autocomplete.getStatusFromIntent(result.getData());
                    Log.i("maps", status.getStatusMessage());
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Log.i("maps", "User canceled autocomplete");
                }
            });
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

    public void hideBottomNavigation(Boolean bool) {
        BottomNavigationView bottomNavigation = getActivity().findViewById(R.id.nav_view);
        if (bool == true)
            bottomNavigation.setVisibility(View.GONE);
        else
            bottomNavigation.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This callback will only be called when MyFragment is at least Started.
        Log.d("frag", "onCreate: addtrip_map");
        hideBottomNavigation(true);
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                getParentFragmentManager().popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    }
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();

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
        dbHelper.close();
        Log.d("frag", "onDestroy: addtrip_map");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideBottomNavigation(false);
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}

