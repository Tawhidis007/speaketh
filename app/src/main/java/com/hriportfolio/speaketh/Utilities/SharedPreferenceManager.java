package com.hriportfolio.speaketh.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {

    protected Context mContext;
    protected SharedPreferences.Editor mEditor;
    protected SharedPreferences mSettings;

    public SharedPreferenceManager(Context ctx,String prefFileName){
        mContext = ctx;
        mSettings = mContext.getSharedPreferences(prefFileName,Context.MODE_PRIVATE);
        mEditor = mSettings.edit();
    }

    public void setValue(String key,String value){
        mEditor.putString(key,value);
        mEditor.commit();
    }
    public void setValue(String key,boolean value){
        mEditor.putBoolean(key,value);
        mEditor.commit();
    }

    public String getValue(String key,String defaultValue){
        return mSettings.getString(key,defaultValue);
    }
    public boolean getValue(String key,boolean defaultValue){
        return mSettings.getBoolean(key,defaultValue);
    }

    public boolean clear(){
        try{
            mEditor.clear().commit();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
