package com.example.myapplication.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.R;
import com.example.myapplication.SingletonClass;
import com.example.myapplication.model.HealthData;

import retrofit2.Response;

public class HealthDataFragment extends Fragment {
    NavController navController;

    public HealthDataFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_uploading, container, false);
        ProgressBar progressBar = rootView.findViewById(R.id.simpleProgressBar);
        RelativeLayout uploadingMessageLayout = rootView.findViewById(R.id.uploadingMessageLayout);
        LinearLayout healthDataLayout = rootView.findViewById(R.id.healthDataLayout);

        uploadingMessageLayout.setVisibility(View.VISIBLE);
        healthDataLayout.setVisibility(View.INVISIBLE);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(Html.fromHtml("<font color='#696969'> REMEDIC </font>"));

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
                ImageView siImage = healthDataLayout.findViewById(R.id.si).findViewById(R.id.data_image);
                try {
                    if (response != null && response.body() != null) {

                        bpmValue.setText(response.body().bpm);
                        so2Value.setText(response.body().spo2);
                        siValue.setText(response.body().si);

                        bpmLabel.setText("Heart Rate");
                        so2Label.setText("Oxygen Saturation");
                        siLabel.setText("Stress Index");

                        bpmImage.setImageResource(R.drawable.pulse);
                        so2Image.setImageResource(R.drawable.spo2);
                        siImage.setImageResource(R.drawable.stress_index);

                    }
                } catch (Exception e) {
                    Log.e("server data error", e.getMessage().toString());
                }

            }

        }.start();

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDestroy() {
        NavHostFragment.findNavController(getParentFragment()).navigate(R.id.nav_home);
        super.onDestroy();
    }
}




