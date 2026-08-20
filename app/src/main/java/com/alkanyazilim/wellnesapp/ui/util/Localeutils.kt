package com.alkanyazilim.wellnesapp.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/**
 * Locale.getDefault() yerine kullanılır. LocalConfiguration.current üzerinden
 * okuma yaptığı için Compose bunu recomposition tetikleyici olarak izleyebilir
 * ("Reading locale in a non-observable way" lint uyarısını çözer).
 * Kullanıcı çalışma sırasında sistem dilini değiştirirse ekran doğru güncellenir.
 */
@Composable
fun rememberCurrentLocale(): Locale {
    val configuration = LocalConfiguration.current
    return configuration.locales.get(0)
}