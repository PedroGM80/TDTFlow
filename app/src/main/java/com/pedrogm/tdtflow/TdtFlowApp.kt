package com.pedrogm.tdtflow

import android.app.Application

class TdtFlowApp : Application() {
    companion object {
        lateinit var appContext: TdtFlowApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}
