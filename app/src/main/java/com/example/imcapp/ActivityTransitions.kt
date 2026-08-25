package com.example.imcapp

import android.app.Activity
import android.os.Build

/**
 * Transiciones entre pantallas. Desde Android 14 se declaran una sola vez con
 * [Activity.overrideActivityTransition]; en versiones anteriores hay que llamar a
 * `overridePendingTransition` justo después de `startActivity()` / `finish()`.
 */

private val USES_MODERN_API = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

fun Activity.registerSlideTransitions() {
    if (USES_MODERN_API) {
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_OPEN,
            R.anim.slide_in_right,
            R.anim.slide_out_left,
        )
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_CLOSE,
            R.anim.slide_in_left,
            R.anim.slide_out_right,
        )
    }
}

fun Activity.registerFadeTransitions() {
    if (USES_MODERN_API) {
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_OPEN,
            R.anim.fade_in,
            R.anim.fade_out,
        )
    }
}

@Suppress("DEPRECATION")
fun Activity.applyLegacySlideOpen() {
    if (!USES_MODERN_API) overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

@Suppress("DEPRECATION")
fun Activity.applyLegacySlideClose() {
    if (!USES_MODERN_API) overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
}

@Suppress("DEPRECATION")
fun Activity.applyLegacyFade() {
    if (!USES_MODERN_API) overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
}
