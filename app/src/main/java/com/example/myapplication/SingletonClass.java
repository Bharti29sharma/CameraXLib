package com.example.myapplication;

import java.util.HashMap;

public class SingletonClass {


        private static SingletonClass mInstance= null;

        public int frameCount = 0;
        public boolean isRecordingStarted = false;
        public boolean isRecordingFinished = false;
        public HashMap leftcheekFramMap = new HashMap();

        protected SingletonClass(){}

        public static synchronized SingletonClass getInstance() {
            if(null == mInstance){
                mInstance = new SingletonClass();
            }
            return mInstance;
        }
    }

