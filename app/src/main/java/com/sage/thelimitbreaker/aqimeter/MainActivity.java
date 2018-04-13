package com.sage.thelimitbreaker.aqimeter;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sage.thelimitbreaker.aqimeter.Utils.AQIFetchUtil;
import com.sage.thelimitbreaker.aqimeter.Utils.Constants;
import com.sage.thelimitbreaker.aqimeter.Utils.MySharedPref;
import com.sage.thelimitbreaker.aqimeter.dialogs.SettingsDialog;

public class MainActivity extends AppCompatActivity {

    private static final String TAG=MainActivity.class.getSimpleName();

    private FloatingActionButton fabLoading;
    private TextView aqiValue;
    private TextView aqiStatus;
    private TextView aqiHeader;
    private ImageView ivOptions;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFields();
        AQIFetchUtil.scheduleJob(getApplicationContext());

    }



    private void initFields(){
        fabLoading=findViewById(R.id.fabLoading);
        aqiValue=findViewById(R.id.aqiValue);
        aqiHeader=findViewById(R.id.aqiHeader);
        aqiStatus=findViewById(R.id.aqiStatus);
        ivOptions=findViewById(R.id.ivOptions);
        preferences=MySharedPref.getMySharedPrefInstance(this).getSharedPrefInstance();
        int aqiVal=preferences.getInt(Constants.AQI_VALUE,-1);
        String desc=preferences.getString(Constants.AQI_STATUS,null);
        if(aqiVal==-1){
            aqiHeader.setVisibility(View.GONE);
            aqiValue.setVisibility(View.GONE);
            aqiStatus.setText("No last time sync");
        }else{
            aqiValue.setVisibility(View.VISIBLE);
            aqiHeader.setVisibility(View.VISIBLE);
            aqiValue.setText(String.valueOf(aqiVal));
            aqiStatus.setText(desc);

        }
        fabLoading.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                if(AQIFetchUtil.isConnectedToNetwork(MainActivity.this)) {
                    Intent intent = new Intent(MainActivity.this, AQIFetchingActivity.class);
                    Pair pair = new Pair<>(fabLoading, "fabTransition");
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                            MainActivity.this,
                            pair
                    );
                    startActivityForResult(intent, Constants.REQ_CODE, options.toBundle());
                }else{
                    Toast.makeText(MainActivity.this,"No Connected to Internet",Toast.LENGTH_LONG).show();
                }
            }
        });
        ivOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsDialog dialog = new SettingsDialog();
                dialog.show(getFragmentManager(),"Settings Dialog");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Constants.REQ_CODE){
            if(resultCode==RESULT_OK){
                int aqi = data.getIntExtra("aqi",-1);
                String desc=data.getStringExtra("desc");
                if(aqi==-1){
                    aqiHeader.setVisibility(View.GONE);
                    aqiValue.setVisibility(View.GONE);
                    aqiStatus.setText("Some problem in fetching ;(");
                }else{
                    aqiHeader.setVisibility(View.VISIBLE);
                    aqiValue.setVisibility(View.VISIBLE);
                    aqiValue.setText(String.valueOf(aqi));
                    aqiStatus.setText(desc);
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putInt(Constants.AQI_VALUE,aqi);
                    editor.putString(Constants.AQI_STATUS,desc);
                    editor.apply();
                }
            }
        }
    }

}