package com.example.imcapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * Pantalla 3: detalles del desarrollador.
 */
class DeveloperActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_developer)
        registerSlideTransitions()

        applyWindowInsets()
        animateContent()

        findViewById<MaterialButton>(R.id.backButton).setOnClickListener { closeScreen() }
        findViewById<View>(R.id.emailRow).setOnClickListener { sendEmail() }
        findViewById<View>(R.id.githubRow).setOnClickListener { openGithub() }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = closeScreen()
            },
        )
    }

    private fun applyWindowInsets() {
        val header = findViewById<LinearLayout>(R.id.devHeader)
        val headerTop = header.paddingTop

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.devRoot)) { root, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            header.setPadding(
                header.paddingLeft,
                headerTop + bars.top,
                header.paddingRight,
                header.paddingBottom,
            )
            root.setPadding(bars.left, 0, bars.right, bars.bottom)
            insets
        }
    }

    /** Las tarjetas entran escalonadas de abajo hacia arriba. */
    private fun animateContent() {
        val avatar = findViewById<View>(R.id.avatar)
        val name = findViewById<View>(R.id.devName)
        val role = findViewById<View>(R.id.devRole)
        val cards = listOf<MaterialCardView>(
            findViewById(R.id.aboutCard),
            findViewById(R.id.contactCard),
            findViewById(R.id.techCard),
        )

        avatar.alpha = 0f
        avatar.scaleX = 0.4f
        avatar.scaleY = 0.4f
        avatar.animate()
            .alpha(1f).scaleX(1f).scaleY(1f)
            .setStartDelay(100)
            .setDuration(600)
            .setInterpolator(OvershootInterpolator(2f))
            .start()

        listOf(name, role).forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 24f
            view.animate()
                .alpha(1f).translationY(0f)
                .setStartDelay(300L + index * 100L)
                .setDuration(400)
                .start()
        }

        cards.forEachIndexed { index, card ->
            card.alpha = 0f
            card.translationY = 60f
            card.animate()
                .alpha(1f).translationY(0f)
                .setStartDelay(400L + index * 120L)
                .setDuration(450)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${getString(R.string.dev_email)}")
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        }
        launchOrWarn(intent)
    }

    private fun openGithub() {
        launchOrWarn(Intent(Intent.ACTION_VIEW, Uri.parse("https://${getString(R.string.dev_github)}")))
    }

    private fun launchOrWarn(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_no_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun closeScreen() {
        finish()
        applyLegacySlideClose()
    }
}
