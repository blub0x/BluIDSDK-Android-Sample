package com.blub0x.bluidsdk_sample_app.Database

import android.app.Activity
import com.blub0x.bluidsdk_sample_app.UserDefaultsDB_Constants
import com.google.gson.reflect.TypeToken

class DBController {
    fun saveUserData(activity: Activity, userData: UserCreds) {
        DBManager<UserCreds>(activity, UserDefaultsDB_Constants.USER_CREDS_KEY).write(userData)
    }

    fun getUserData(activity: Activity): UserCreds? {
        val itemType = object : TypeToken<UserCreds>() {}.type
        return DBManager<UserCreds>(
            activity,
            UserDefaultsDB_Constants.USER_CREDS_KEY
        ).read(itemType)
    }

    fun saveEnvironment(activity: Activity, env: Environment) {
        DBManager<Environment>(activity, UserDefaultsDB_Constants.ENVIRONMENT).write(env)
    }

    fun getEnvironment(activity: Activity): Environment? {
        val itemType = object : TypeToken<Environment>() {}.type
        return DBManager<Environment>(activity, UserDefaultsDB_Constants.ENVIRONMENT).read(itemType)
    }
}