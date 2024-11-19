package com.suisho.pixelposed;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook;
public class Init implements IXposedHookLoadPackage{
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        //防止乱注入
        if(!lpparam.packageName.contains("jp.pxv.android")){
            XposedBridge.log("Wrong package:"+lpparam.packageName);
            return;
        }
        Class<?> activityClass=XposedHelpers.findClass("jp.pxv.android.activity.RoutingActivity",lpparam.classLoader);
        XposedHelpers.findAndHookMethod(activityClass, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Hooking");
                super.beforeHookedMethod(param);
                Activity activityContext=(Activity) param.thisObject;
                SharedPreferences sharedPreferences = activityContext.getSharedPreferences("jp.pxv.android_preferences", Context.MODE_PRIVATE);
                //如果没有对应值不做处理 一般意味着没打开过
                long lastLaunchTime=sharedPreferences.getLong("first_launch_time_millis",-24L);
                if(lastLaunchTime==-24L){
                    Toast.makeText(activityContext, "PixelPosed:未找到对应键值\n可能是首次启动", Toast.LENGTH_LONG).show();
                    XposedBridge.log("Cannot found key");
                    return;
                }
                long newTime=System.currentTimeMillis();
                //修改首次启动时间为当前时间-30秒 (稍微造下假)
                sharedPreferences.edit().putLong("first_launch_time_millis",newTime-30000L).commit();
                Toast.makeText(activityContext, "PixelPosed:修改完成", Toast.LENGTH_LONG).show();
                XposedBridge.log("Pixel hook success");
            }
        });
    }
}
