package com.blub0x.bluidsdk_sample_app.Database

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.lang.reflect.Type


open class DBManager<T>(val activity: Activity, val m_dbKey: String) {
    private var sharedPref: SharedPreferences = activity.getPreferences(Context.MODE_PRIVATE)

    open fun read(type: Type): T? {
        val value = sharedPref.getString(m_dbKey, null)
        return Gson().fromJson(value, type)
    }

    open fun write(value: Any) {
        val prefEditor = sharedPref.edit()
        val jsonString = Gson().toJson(value)
        prefEditor.putString(m_dbKey, jsonString)
        prefEditor.apply()
    }

    open fun delete() {
        val prefEditor = sharedPref.edit()
        prefEditor.remove(m_dbKey)
        prefEditor.apply()
    }
}




