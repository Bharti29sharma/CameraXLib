package com.example.myapplication;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.HealthData;

import retrofit2.Response;

public class UploadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploading);

        ProgressBar progressBar = findViewById(R.id.simpleProgressBar);
        RelativeLayout uploadingMessageLayout = findViewById(R.id.uploadingMessageLayout);
        LinearLayout healthDataLayout = findViewById(R.id.healthDataLayout);

        uploadingMessageLayout.setVisibility(View.VISIBLE);
        healthDataLayout.setVisibility(View.INVISIBLE);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.remedic_mini_wo_bg);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#0000'>REMEDIC </font>"));

        new CountDownTimer(6000, 200) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((6000 - millisUntilFinished) / 200);

                progressBar.setProgress(progress);


            }

            @Override
            public void onFinish() {
                //the progressBar will be invisible after 60 000 miliseconds ( 1 minute)
                //  progressBar.setVisibility(View.INVISIBLE);
                uploadingMessageLayout.setVisibility(View.INVISIBLE);
                healthDataLayout.setVisibility(View.VISIBLE);
              Response<HealthData> response = SingletonClass.getInstance().restResponse;
                TextView bpmValue = healthDataLayout.findViewById(R.id.bpm).findViewById(R.id.data_value);
                TextView so2Value = healthDataLayout.findViewById(R.id.spo2).findViewById(R.id.data_value);
                TextView siValue = healthDataLayout.findViewById(R.id.si).findViewById(R.id.data_value);
              //  TextView bpmValue = healthDataLayout.findViewById(R.id.bpm).findViewById(R.id.data_value);

                TextView bpmLabel = healthDataLayout.findViewById(R.id.bpm).findViewById(R.id.data_txt);
                TextView so2Label = healthDataLayout.findViewById(R.id.spo2).findViewById(R.id.data_txt);
                TextView siLabel = healthDataLayout.findViewById(R.id.si).findViewById(R.id.data_txt);

                ImageView bpmImage = healthDataLayout.findViewById(R.id.bpm).findViewById(R.id.data_image);
                ImageView so2Image = healthDataLayout.findViewById(R.id.spo2).findViewById(R.id.data_image);
                ImageView siImage= healthDataLayout.findViewById(R.id.si).findViewById(R.id.data_image);
              try {
                if(response != null && response.body() !=null ){

                    bpmValue.setText( response.body().bpm);
                    so2Value.setText( response.body().spo2);
                    siValue.setText( response.body().si);

                    bpmLabel.setText("Heart Rate");
                    so2Label.setText("Oxygen Saturation");
                    siLabel.setText("Stress Index");

                    bpmImage.setImageResource(R.drawable.pulse);
                    so2Image.setImageResource(R.drawable.spo2);
                    siImage.setImageResource(R.drawable.stress_index);

                }
              }
              catch (Exception e){
                  Log.e("server data error" , e.getMessage().toString());
              }

            }

        }.start();


    }
}