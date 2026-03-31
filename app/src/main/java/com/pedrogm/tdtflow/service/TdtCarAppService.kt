package com.pedrogm.tdtflow.service

import android.content.pm.ApplicationInfo
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.pedrogm.tdtflow.service.car.TdtCarSession
import dagger.hilt.android.AndroidEntryPoint

/**
 * Punto de entrada para Automotive OS (coche con Android Automotive).
 * Proporciona la interfaz nativa de usuario con [TdtCarScreen].
 *
 * Para Android Auto (teléfono conectado), el acceso se realiza mediante
 * [TdtMediaService] (MediaLibraryService), no por esta clase.
 */
@AndroidEntryPoint
class TdtCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator =
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        } else {
            HostValidator.Builder(applicationContext)
                .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
                .build()
        }

    override fun onCreateSession(): Session = TdtCarSession()
}
