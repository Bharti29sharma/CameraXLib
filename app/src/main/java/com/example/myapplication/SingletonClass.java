package com.example.myapplication;

import com.example.myapplication.model.HealthData;

import java.util.HashMap;

import retrofit2.Response;

public class SingletonClass {


        private static SingletonClass mInstance= null;

        public int frameCount = 0;
        public boolean isRecordingStarted = false;
        public boolean isRecordingFinished = false;
        public HashMap leftcheekFramMap = new HashMap();
        public Response<HealthData> restResponse= null;

        protected SingletonClass(){}

        public static synchronized SingletonClass getInstance() {
            if(null == mInstance){
                mInstance = new SingletonClass();
            }
            return mInstance;
        }
    }

