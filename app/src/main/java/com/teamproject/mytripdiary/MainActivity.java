package com.teamproject.mytripdiary;

import static com.teamproject.mytripdiary.R.id.btn_plus;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

        btn_home = findViewById(R.id.btn_home);
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlans();
            }
        });

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

    void showPlans() {
        layoutPlans.removeAllViews();

        Cursor cursorTableTrip = myDatabase.getAllDataFromTrip();
        if (cursorTableTrip.moveToFirst()) {
            findViewById(R.id.main_middle_messaage).setVisibility(View.INVISIBLE);
            do {
                // 첫 번째 테이블 데이터 처리
                @SuppressLint("Range") int idx = cursorTableTrip.getInt(cursorTableTrip.getColumnIndex("idx"));
                @SuppressLint("Range") String title = cursorTableTrip.getString(cursorTableTrip.getColumnIndex("title"));
                @SuppressLint("Range") String start_date= cursorTableTrip.getString(cursorTableTrip.getColumnIndex("start_date"));
                @SuppressLint("Range") String end_date = cursorTableTrip.getString(cursorTableTrip.getColumnIndex("end_date"));
                Log.d("MyTripDairy", "title = "+title+" start_date = "+start_date+" end_date = "+end_date);

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View viewPlan = inflater.inflate(R.layout.created_plan, null);
                Button btn = viewPlan.findViewById(R.id.btnPlan);
                btn.setText(title+"\n"+start_date+"~"+end_date);
                layoutPlans.addView(viewPlan);
            } while (cursorTableTrip.moveToNext());
        } else {
            findViewById(R.id.main_middle_messaage).setVisibility(View.VISIBLE);
        }
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
                myDatabase.insertDataToTrip(txtTitle.getText().toString(), btnSelectStartDate.getText().toString(), btnSelectEndDate.getText().toString());
                showPlans();
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
}