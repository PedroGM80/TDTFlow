package com.pedrogm.tdtflow.service.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class TdtCarSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen = TdtCarScreen(carContext)
}
