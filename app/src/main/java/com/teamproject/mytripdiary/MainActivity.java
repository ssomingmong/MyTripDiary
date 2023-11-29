package com.teamproject.mytripdiary;

import static com.teamproject.mytripdiary.R.id.btn_plus;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private MyDatabase myDatabase;
    private ImageButton btn_home;
    private ImageButton btn_plus;
    private ImageButton btn_record;
    LinearLayout layoutPlans;
    Calendar calendar = Calendar.getInstance();
    String selectedStartDate;
    String selectedEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDatabase = new MyDatabase(this);
        myDatabase.open();

        btn_plus = findViewById(R.id.btn_plus);
        btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreatePlanPopup();
            }
        });

        layoutPlans = findViewById(R.id.layoutPlans);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void showCreatePlanPopup() {
        // LayoutInflater를 통해 XML 레이아웃을 객체로 변환
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_create_plan, null);

        // 팝업창 생성
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // 팝업창 외부 터치 시 닫기 여부
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // 팝업창 내의 버튼 클릭 이벤트 처리
        EditText txtTitle = popupView.findViewById(R.id.q_title);
        Button btnCreatePlan = popupView.findViewById(R.id.btn_create_plan);
        Button btnSelectStartDate = popupView.findViewById(R.id.select_start_date);
        Button btnSelectEndDate = popupView.findViewById(R.id.select_end_date);

        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = sdf.format(dt);

        selectedStartDate = strDate;
        selectedEndDate = strDate;

        btnSelectStartDate.setText(strDate);
        btnSelectEndDate.setText(strDate);
        btnCreatePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go_detailedplan = new Intent(MainActivity.this , DetailedPlan.class);
                go_detailedplan.putExtra("title", txtTitle.getText());
                go_detailedplan.putExtra("startdate", btnSelectStartDate.getText());
                go_detailedplan.putExtra("enddate", btnSelectEndDate.getText());
                startActivity(go_detailedplan);
                popupWindow.dismiss(); // 팝업창 닫기
            }
        });

        btnSelectStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog("start", btnSelectStartDate);
            }
        });

        btnSelectEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog("end", btnSelectEndDate);
            }
        });


        // 팝업창을 액티비티의 중앙에 위치하도록 설정
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    void showDatePickerDialog(final String dateType, final Button targetButton) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;

                        if (dateType.equals("start")) {
                            // 여행 시작 날짜 정보 저장
                            selectedStartDate = selectedDate;
                        } else if (dateType.equals("end")) {
                            // 여행 종료 날짜 정보 저장
                            selectedEndDate = selectedDate;
                        }

                        // 버튼에 선택한 날짜 표시
                        targetButton.setText(selectedDate);

                        Toast.makeText(MainActivity.this, "Selected Date: " + selectedDate, Toast.LENGTH_SHORT).show();
                    }
                },
                year, month, dayOfMonth);

        datePickerDialog.show();
    }

    void showMap(String locationName) {
//        String locationName = "대한민국 경기도 수원시 영통구 동탄지성로 488번길 22";

        // Google Maps 앱을 열기 위한 URI 생성
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(locationName));

        // Intent 생성 및 Google Maps 앱 열기
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Google Maps 앱이 설치되어 있으면 열고, 그렇지 않으면 Play Store로 이동하여 설치하도록 함
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Google Maps 앱이 설치되지 않은 경우 Play Store로 이동
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"));
            startActivity(intent);
        }
    }
}