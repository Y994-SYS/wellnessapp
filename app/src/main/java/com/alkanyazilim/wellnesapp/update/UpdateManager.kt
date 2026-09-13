package com.alkanyazilim.wellnesapp.update

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Play Store'un kendi "güncelleme mevcut" bildirimini beklemek yerine, uygulama
// her açıldığında aktif olarak sorar. Sadece Play Store'dan kurulmuş sürümlerde
// çalışır — sideload/debug kurulumlarda hep "güncelleme yok" döner.
class UpdateManager(private val activity: Activity) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    // true olduğunda: güncelleme indirildi, kullanıcının "yeniden başlat"a
    // basmasını bekliyoruz (FLEXIBLE update akışının son adımı).
    private val _updateReadyToInstall = MutableStateFlow(false)
    val updateReadyToInstall: StateFlow<Boolean> = _updateReadyToInstall

    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            _updateReadyToInstall.value = true
        }
    }

    init {
        appUpdateManager.registerListener(listener)
    }

    fun checkForUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val updateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val flexibleAllowed = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

            if (updateAvailable && flexibleAllowed) {
                @Suppress("DEPRECATION")
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    activity,
                    com.google.android.play.core.appupdate.AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                    1001
                )
            }
        }
    }

    // Uygulama arka plandan öne gelince, indirme tamamlanmış ama kullanıcı
    // henüz "yeniden başlat"a basmamış bir güncelleme var mı diye kontrol eder.
    fun checkForStalledUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                _updateReadyToInstall.value = true
            }
        }
    }

    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    fun unregister() {
        appUpdateManager.unregisterListener(listener)
    }
}