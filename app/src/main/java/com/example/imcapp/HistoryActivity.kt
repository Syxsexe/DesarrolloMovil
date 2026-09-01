package com.example.imcapp

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

/**
 * Pantalla de historial: lista las mediciones guardadas y permite filtrarlas
 * por sexo, borrar una a una o vaciar el historial completo.
 */
class HistoryActivity : AppCompatActivity() {

    private lateinit var store: ImcHistoryStore
    private lateinit var adapter: HistoryAdapter

    private lateinit var list: RecyclerView
    private lateinit var emptyState: NestedScrollView
    private lateinit var emptyTitle: TextView
    private lateinit var emptyBody: TextView
    private lateinit var summary: TextView
    private lateinit var clearButton: MaterialButton

    private var filter = HistoryFilter.TODOS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        registerSlideTransitions()

        store = ImcHistoryStore(this)
        bindViews()
        applyWindowInsets()

        adapter = HistoryAdapter(onDelete = ::confirmDelete)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        keepContentClearOfChrome()

        findViewById<TextView>(R.id.limitNote).text = resources.getQuantityString(
            R.plurals.history_limit_note,
            ImcHistoryStore.MAX_RECORDS,
            ImcHistoryStore.MAX_RECORDS,
        )

        findViewById<MaterialButton>(R.id.backButton).setOnClickListener { closeScreen() }
        clearButton.setOnClickListener { confirmClearAll() }

        // El filtro se restaura antes de escuchar cambios, para no refrescar de más.
        filter = savedInstanceState?.getString(KEY_FILTER)
            ?.let { name -> HistoryFilter.entries.firstOrNull { it.name == name } }
            ?: HistoryFilter.TODOS
        val filterGroup = findViewById<ChipGroup>(R.id.filterGroup)
        filterGroup.check(chipIdOf(filter))
        filterGroup.setOnCheckedStateChangeListener { _, checked ->
            filter = when (checked.firstOrNull()) {
                R.id.filterMale -> HistoryFilter.HOMBRES
                R.id.filterFemale -> HistoryFilter.MUJERES
                else -> HistoryFilter.TODOS
            }
            refresh()
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = closeScreen()
            },
        )

        refresh()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_FILTER, filter.name)
    }

    private fun chipIdOf(filter: HistoryFilter) = when (filter) {
        HistoryFilter.TODOS -> R.id.filterAll
        HistoryFilter.HOMBRES -> R.id.filterMale
        HistoryFilter.MUJERES -> R.id.filterFemale
    }

    /**
     * En el CoordinatorLayout el pie flota sobre el contenido, así que tanto la lista
     * como el estado vacío reservan abajo el alto del pie. Ese alto depende del inset
     * inferior, por eso se recalcula en cada pasada de layout de la raíz.
     */
    private fun keepContentClearOfChrome() {
        val root = findViewById<View>(R.id.historyRoot)
        val footer = findViewById<LinearLayout>(R.id.historyFooter)

        root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            list.reserveBottom(footer.height)
            emptyState.reserveBottom(footer.height)
        }
    }

    private fun View.reserveBottom(bottom: Int) {
        if (paddingBottom != bottom) setPadding(paddingLeft, paddingTop, paddingRight, bottom)
    }

    private fun bindViews() {
        list = findViewById(R.id.historyList)
        emptyState = findViewById(R.id.emptyState)
        emptyTitle = findViewById(R.id.emptyTitle)
        emptyBody = findViewById(R.id.emptyBody)
        summary = findViewById(R.id.historySummary)
        clearButton = findViewById(R.id.clearButton)
    }

    private fun applyWindowInsets() {
        val header = findViewById<LinearLayout>(R.id.historyHeader)
        val footer = findViewById<LinearLayout>(R.id.historyFooter)
        val headerTop = header.paddingTop
        val footerBottom = footer.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.historyRoot)) { root, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            header.setPadding(
                header.paddingLeft,
                headerTop + bars.top,
                header.paddingRight,
                header.paddingBottom,
            )
            footer.setPadding(
                footer.paddingLeft,
                footer.paddingTop,
                footer.paddingRight,
                footerBottom + bars.bottom,
            )
            root.setPadding(bars.left, 0, bars.right, 0)
            insets
        }
    }

    /** Vuelve a leer el historial y actualiza lista, resumen y estado vacío. */
    private fun refresh() {
        val all = store.all()
        val visible = all.filter(filter::matches)
        adapter.submitList(visible)

        val stats = HistorySummary.of(visible)
        summary.text = if (stats.averageImc == null) {
            getString(R.string.history_summary_empty)
        } else {
            resources.getQuantityString(
                R.plurals.history_summary,
                stats.count,
                stats.count,
                format(stats.averageImc),
            )
        }

        val isEmpty = visible.isEmpty()
        list.visibility = if (isEmpty) View.GONE else View.VISIBLE
        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        clearButton.isEnabled = all.isNotEmpty()

        if (isEmpty) {
            val gender = filter.gender
            emptyTitle.text = getString(
                if (gender == null) R.string.history_empty_title else R.string.history_empty_filtered_title
            )
            emptyBody.text = if (gender == null) {
                getString(R.string.history_empty_body)
            } else {
                getString(R.string.history_empty_filtered, getString(gender.labelRes).lowercase(SPANISH))
            }
        }
    }

    /** Borra la medición y ofrece deshacer antes de que el usuario salga de la pantalla. */
    private fun confirmDelete(record: ImcRecord) {
        store.remove(record)
        refresh()
        Snackbar.make(findViewById(R.id.historyRoot), R.string.history_deleted, Snackbar.LENGTH_LONG)
            .setAnchorView(R.id.historyFooter)
            .setAction(R.string.history_undo) {
                store.add(record)
                refresh()
            }
            .show()
    }

    private fun confirmClearAll() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.history_clear_title)
            .setMessage(R.string.history_clear_message)
            .setNegativeButton(R.string.history_cancel, null)
            .setPositiveButton(R.string.history_clear_confirm) { _, _ ->
                store.clear()
                refresh()
            }
            .show()
    }

    private fun closeScreen() {
        finish()
        applyLegacySlideClose()
    }

    private fun format(value: Double) = String.format(Locale.US, "%.1f", value)

    private companion object {
        const val KEY_FILTER = "filter"
        val SPANISH: Locale = Locale.forLanguageTag("es")
    }
}
