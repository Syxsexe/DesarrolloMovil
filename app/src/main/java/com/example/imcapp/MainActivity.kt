package com.example.imcapp

import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

/**
 * Pantalla 2: calculadora de IMC.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var weightLayout: TextInputLayout
    private lateinit var heightLayout: TextInputLayout
    private lateinit var weightInput: TextInputEditText
    private lateinit var heightInput: TextInputEditText
    private lateinit var resultCard: MaterialCardView
    private lateinit var imcValue: TextView
    private lateinit var categoryChip: TextView
    private lateinit var categoryMessage: TextView
    private lateinit var idealWeight: TextView
    private lateinit var lorentzWeight: TextView
    private lateinit var bodyFatNote: TextView
    private lateinit var genderBadge: TextView
    private lateinit var genderGroup: MaterialButtonToggleGroup
    private lateinit var genderError: TextView
    private lateinit var markerTrack: FrameLayout
    private lateinit var scaleMarker: ImageView

    private lateinit var history: ImcHistoryStore
    private var valueAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        registerSlideTransitions()

        history = ImcHistoryStore(this)

        bindViews()
        applyWindowInsets()
        restoreLastGender()

        findViewById<MaterialButton>(R.id.calculateButton).setOnClickListener { calculate() }
        findViewById<MaterialButton>(R.id.clearButton).setOnClickListener { clear() }
        findViewById<MaterialButton>(R.id.developerButton).setOnClickListener { openDeveloperScreen() }
        findViewById<MaterialButton>(R.id.historyButton).setOnClickListener { openHistoryScreen() }

        // Al escribir de nuevo se limpia el error anterior.
        weightInput.setOnFocusChangeListener { _, _ -> weightLayout.error = null }
        heightInput.setOnFocusChangeListener { _, _ -> heightLayout.error = null }
        genderGroup.addOnButtonCheckedListener { _, _, _ -> genderError.visibility = View.GONE }
    }

    /** El sexo elegido la última vez queda preseleccionado. */
    private fun restoreLastGender() {
        val checked = when (history.lastGender) {
            Gender.HOMBRE -> R.id.maleButton
            Gender.MUJER -> R.id.femaleButton
            null -> return
        }
        genderGroup.check(checked)
    }

    /** Sexo seleccionado, o null si el usuario todavía no eligió. */
    private fun selectedGender(): Gender? = when (genderGroup.checkedButtonId) {
        R.id.maleButton -> Gender.HOMBRE
        R.id.femaleButton -> Gender.MUJER
        else -> null
    }

    private fun bindViews() {
        weightLayout = findViewById(R.id.weightLayout)
        heightLayout = findViewById(R.id.heightLayout)
        weightInput = findViewById(R.id.weightInput)
        heightInput = findViewById(R.id.heightInput)
        resultCard = findViewById(R.id.resultCard)
        imcValue = findViewById(R.id.imcValue)
        categoryChip = findViewById(R.id.categoryChip)
        categoryMessage = findViewById(R.id.categoryMessage)
        idealWeight = findViewById(R.id.idealWeight)
        lorentzWeight = findViewById(R.id.lorentzWeight)
        bodyFatNote = findViewById(R.id.bodyFatNote)
        genderBadge = findViewById(R.id.genderBadge)
        genderGroup = findViewById(R.id.genderGroup)
        genderError = findViewById(R.id.genderError)
        markerTrack = findViewById(R.id.markerTrack)
        scaleMarker = findViewById(R.id.scaleMarker)
    }

    /** El encabezado absorbe el inset superior y el scroll el inferior. */
    private fun applyWindowInsets() {
        val header = findViewById<LinearLayout>(R.id.header)
        val scroll = findViewById<NestedScrollView>(R.id.scroll)
        val headerTop = header.paddingTop
        val scrollBottom = scroll.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            header.updatePaddingTop(headerTop + bars.top)
            scroll.setPadding(bars.left, 0, bars.right, scrollBottom + bars.bottom)
            insets
        }
    }

    private fun View.updatePaddingTop(top: Int) =
        setPadding(paddingLeft, top, paddingRight, paddingBottom)

    private fun calculate() {
        hideKeyboard()
        val gender = selectedGender()
        genderError.visibility = if (gender == null) View.VISIBLE else View.GONE
        val weight = readField(weightLayout, weightInput, ImcCalculator.MIN_WEIGHT_KG, ImcCalculator.MAX_WEIGHT_KG, R.string.error_weight_range)
        val height = readField(heightLayout, heightInput, ImcCalculator.MIN_HEIGHT_CM, ImcCalculator.MAX_HEIGHT_CM, R.string.error_height_range)
        if (gender == null || weight == null || height == null) return

        val result = ImcCalculator.calculate(weight, height, gender)
        showResult(result)
        saveToHistory(result, weight, height)
    }

    /** Cada cálculo válido queda registrado en el historial. */
    private fun saveToHistory(result: ImcResult, weightKg: Double, heightCm: Double) {
        history.lastGender = result.gender
        history.add(ImcRecord.from(result, weightKg, heightCm, System.currentTimeMillis()))
        Snackbar.make(findViewById(R.id.main), R.string.calc_saved, Snackbar.LENGTH_SHORT)
            .setAction(R.string.calc_history_button) { openHistoryScreen() }
            .show()
    }

    /** Valida un campo y devuelve su valor, o null marcando el error correspondiente. */
    private fun readField(
        layout: TextInputLayout,
        input: TextInputEditText,
        min: Double,
        max: Double,
        rangeErrorRes: Int,
    ): Double? {
        val raw = input.text?.toString().orEmpty()
        if (raw.isBlank()) {
            layout.error = getString(R.string.error_required)
            return null
        }
        val value = ImcCalculator.parseNumber(raw)
        if (value == null) {
            layout.error = getString(R.string.error_invalid_number)
            return null
        }
        if (value < min || value > max) {
            layout.error = getString(rangeErrorRes)
            return null
        }
        layout.error = null
        return value
    }

    private fun showResult(result: ImcResult) {
        val color = ContextCompat.getColor(this, result.category.colorRes)
        val genderColor = ContextCompat.getColor(this, result.gender.colorRes)

        genderBadge.text = getString(result.gender.labelRes)
        genderBadge.backgroundTintList = ColorStateList.valueOf(genderColor)
        genderBadge.setCompoundDrawablesRelativeWithIntrinsicBounds(
            ContextCompat.getDrawable(this, result.gender.iconRes), null, null, null,
        )
        lorentzWeight.text = getString(
            R.string.calc_lorentz_weight,
            getString(result.gender.labelRes).lowercase(SPANISH),
            format(result.lorentzWeight),
        )
        bodyFatNote.text = getString(result.gender.bodyFatRes)

        categoryChip.text = getString(result.category.labelRes)
        categoryChip.backgroundTintList = ColorStateList.valueOf(color)
        categoryMessage.text = getString(result.category.messageRes)
        idealWeight.text = getString(
            R.string.calc_ideal_weight,
            format(result.idealWeightMin),
            format(result.idealWeightMax),
        )
        imcValue.setTextColor(color)
        scaleMarker.imageTintList = ColorStateList.valueOf(color)

        val firstTime = resultCard.visibility != View.VISIBLE
        if (firstTime) {
            resultCard.visibility = View.VISIBLE
            resultCard.alpha = 0f
            resultCard.translationY = 60f
            resultCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(450)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        animateImcValue(result.imc)
        animateMarker(result.imc)
    }

    /** El número del IMC sube desde 0 hasta su valor final. */
    private fun animateImcValue(target: Double) {
        valueAnimator?.cancel()
        valueAnimator = ValueAnimator.ofFloat(0f, target.toFloat()).apply {
            duration = 900
            interpolator = DecelerateInterpolator()
            addUpdateListener { imcValue.text = format((it.animatedValue as Float).toDouble()) }
            start()
        }
    }

    /** El triángulo se desliza hasta la posición del IMC en la escala de colores. */
    private fun animateMarker(imc: Double) {
        markerTrack.doOnLayout { track ->
            val usableWidth = track.width - scaleMarker.width
            if (usableWidth <= 0) return@doOnLayout
            val targetX = ImcCalculator.scaleFraction(imc) * usableWidth
            scaleMarker.animate()
                .translationX(targetX)
                .setDuration(650)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun clear() {
        hideKeyboard()
        valueAnimator?.cancel()
        weightInput.text = null
        heightInput.text = null
        weightLayout.error = null
        heightLayout.error = null
        genderError.visibility = View.GONE
        weightInput.requestFocus()

        if (resultCard.visibility == View.VISIBLE) {
            resultCard.animate()
                .alpha(0f)
                .translationY(40f)
                .setDuration(250)
                .withEndAction {
                    resultCard.visibility = View.GONE
                    scaleMarker.translationX = 0f
                }
                .start()
        }
    }

    private fun openDeveloperScreen() {
        startActivity(Intent(this, DeveloperActivity::class.java))
        applyLegacySlideOpen()
    }

    private fun openHistoryScreen() {
        startActivity(Intent(this, HistoryActivity::class.java))
        applyLegacySlideOpen()
    }

    private fun hideKeyboard() {
        val focused = currentFocus ?: return
        val imm = getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(focused.windowToken, 0)
        focused.clearFocus()
    }

    private fun format(value: Double) = String.format(Locale.US, "%.1f", value)

    override fun onDestroy() {
        valueAnimator?.cancel()
        super.onDestroy()
    }

    private companion object {
        val SPANISH: Locale = Locale.forLanguageTag("es")
    }
}
