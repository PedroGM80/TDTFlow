package com.pedrogm.tdtflow

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TdtFlowApp : Application() {
    companion object {
        lateinit var appContext: TdtFlowApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
    }
}
