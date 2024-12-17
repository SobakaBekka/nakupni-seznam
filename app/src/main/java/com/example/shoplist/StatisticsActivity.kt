package com.example.shoplist

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import androidx.appcompat.app.AppCompatDelegate

class StatisticsActivity : AppCompatActivity() {
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        pieChart = findViewById(R.id.pie_chart)
        findViewById<android.widget.ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        setupPieChart()
        loadData()
    }

    private fun setupPieChart() {
        val isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        val textColor = if (isDarkMode) Color.WHITE else Color.BLACK

        pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            setCenterText("Typy produktů")
            setCenterTextColor(textColor)
            setDrawEntryLabels(true)
            setEntryLabelColor(textColor)
            legend.isEnabled = true
            legend.textSize = 12f
            legend.textColor = textColor
        }
    }

    private fun loadData() {
        val listPosition = intent.getIntExtra("list_position", -1)
        if (listPosition == -1) {
            finish()
            return
        }

        try {
            val file = File(filesDir, "grocery_lists.json")
            if (!file.exists()) {
                finish()
                return
            }

            val jsonString = file.readText()
            val jsonArray = JSONArray(jsonString)
            if (listPosition >= jsonArray.length()) {
                finish()
                return
            }

            val listObject = jsonArray.getJSONObject(listPosition)
            val itemsArray = listObject.getJSONArray("items")
            
            val typeQuantities = mutableMapOf<String, Float>()
            
            for (i in 0 until itemsArray.length()) {
                val itemObject = itemsArray.getJSONObject(i)
                val type = itemObject.getString("type")
                val quantity = itemObject.getInt("quantity").toFloat()
                typeQuantities[type] = (typeQuantities[type] ?: 0f) + quantity
            }

            if (typeQuantities.isEmpty()) {
                pieChart.setCenterText("Žádná data")
                return
            }

            val entries = typeQuantities.map { (type, quantity) ->
                PieEntry(quantity, type)
            }

            val colors = listOf(
                Color.rgb(64, 89, 128),
                Color.rgb(149, 165, 124),
                Color.rgb(217, 184, 162),
                Color.rgb(191, 134, 134),
                Color.rgb(179, 48, 80),
                Color.rgb(193, 37, 82),
                Color.rgb(255, 102, 0),
                Color.rgb(245, 199, 0),
                Color.rgb(106, 150, 31),
                Color.rgb(179, 100, 53),
                Color.rgb(207, 248, 246),
                Color.rgb(148, 212, 212),
                Color.rgb(136, 180, 187),
                Color.rgb(118, 174, 175),
                Color.rgb(42, 109, 130),
                Color.rgb(217, 80, 138)
            )

            val isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
            val valueTextColor = if (isDarkMode) Color.WHITE else Color.BLACK

            val dataSet = PieDataSet(entries, "")
            dataSet.apply {
                setColors(colors)
                valueTextSize = 12f
                setValueTextColor(valueTextColor)
                valueFormatter = PercentFormatter(pieChart)
            }

            val data = PieData(dataSet)
            pieChart.data = data
            pieChart.invalidate()
        } catch (e: Exception) {
            finish()
        }
    }
}
