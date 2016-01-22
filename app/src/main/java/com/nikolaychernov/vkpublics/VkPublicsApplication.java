package com.nikolaychernov.vkpublics;

import android.app.Application;

import com.vk.sdk.VKSdk;

/**
 * Created by Nikolay on 23.01.2016.
 */
public class VkPublicsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(getApplicationContext());
    }
}
