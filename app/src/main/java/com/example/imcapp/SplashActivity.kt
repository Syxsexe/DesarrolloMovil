package com.example.imcapp

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * Pantalla 1: animación de bienvenida. Tras [SPLASH_DURATION_MS] abre la calculadora.
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var ringAnimator: ObjectAnimator
    private lateinit var progressAnimator: ValueAnimator
    private var navigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        registerFadeTransitions()

        val root = findViewById<View>(R.id.splashRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val ring = findViewById<View>(R.id.logoRing)
        val circle = findViewById<View>(R.id.logoCircle)
        val icon = findViewById<ImageView>(R.id.logoIcon)
        val title = findViewById<TextView>(R.id.splashTitle)
        val subtitle = findViewById<TextView>(R.id.splashSubtitle)
        val progress = findViewById<LinearProgressIndicator>(R.id.splashProgress)

        startEntranceAnimation(ring, circle, icon, title, subtitle, progress)
    }

    private fun startEntranceAnimation(
        ring: View,
        circle: View,
        icon: ImageView,
        title: TextView,
        subtitle: TextView,
        progress: LinearProgressIndicator,
    ) {
        listOf(ring, circle, title, subtitle, progress).forEach { it.alpha = 0f }
        circle.scaleX = 0.3f
        circle.scaleY = 0.3f
        ring.scaleX = 0.5f
        ring.scaleY = 0.5f
        title.translationY = 40f
        subtitle.translationY = 40f

        // El círculo del logo entra con rebote.
        circle.animate()
            .alpha(1f).scaleX(1f).scaleY(1f)
            .setStartDelay(150)
            .setDuration(700)
            .setInterpolator(OvershootInterpolator(2f))
            .start()

        // El anillo aparece y gira de forma continua.
        ring.animate()
            .alpha(1f).scaleX(1f).scaleY(1f)
            .setStartDelay(300)
            .setDuration(600)
            .setInterpolator(DecelerateInterpolator())
            .start()

        ringAnimator = ObjectAnimator.ofFloat(ring, View.ROTATION, 0f, 360f).apply {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }

        // Latido suave del icono.
        icon.animate()
            .scaleX(1.12f).scaleY(1.12f)
            .setStartDelay(850)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                icon.animate().scaleX(1f).scaleY(1f).setDuration(500).start()
            }
            .start()

        // Textos que suben con desvanecido escalonado.
        title.animate().alpha(1f).translationY(0f).setStartDelay(600).setDuration(500).start()
        subtitle.animate().alpha(1f).translationY(0f).setStartDelay(800).setDuration(500).start()
        progress.animate().alpha(1f).setStartDelay(950).setDuration(400).start()

        // La barra de progreso marca el tiempo restante y al terminar navega.
        progressAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = SPLASH_DURATION_MS
            interpolator = LinearInterpolator()
            addUpdateListener { progress.progress = it.animatedValue as Int }
            doOnEnd { goToCalculator() }
            start()
        }
    }

    private fun goToCalculator() {
        if (navigated) return
        navigated = true
        startActivity(Intent(this, MainActivity::class.java))
        applyLegacyFade()
        finish()
    }

    override fun onDestroy() {
        if (::ringAnimator.isInitialized) ringAnimator.cancel()
        if (::progressAnimator.isInitialized) {
            // Se quitan los listeners antes de cancelar para no navegar al destruir la pantalla.
            progressAnimator.removeAllListeners()
            progressAnimator.cancel()
        }
        super.onDestroy()
    }

    private companion object {
        const val SPLASH_DURATION_MS = 2600L
    }
}
