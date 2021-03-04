package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class HealthData {

    public class faceResponse {

        @SerializedName("bpm")
        public String bpm;
        @SerializedName("spo2")
        public String spo2;
        @SerializedName("si")
        public String si;

    }
}
