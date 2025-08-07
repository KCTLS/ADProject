package com.example.adproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class DashboardActivity : AppCompatActivity() {

    private lateinit var chartView: LineChart
    private lateinit var last7Days: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 底部导航
        val exerciseButton = findViewById<Button>(R.id.exerciseButton)
        val dashboardButton = findViewById<Button>(R.id.dashboardButton)
        val classButton = findViewById<Button>(R.id.classButton)
        val homeButton = findViewById<Button>(R.id.homeButton)

        setSelectedButton(dashboardButton)

        exerciseButton.setOnClickListener {
            setSelectedButton(exerciseButton)
            startActivity(Intent(this, ExerciseActivity::class.java))
        }
        dashboardButton.setOnClickListener {
            setSelectedButton(dashboardButton)
        }
        classButton.setOnClickListener {
            setSelectedButton(classButton)
            startActivity(Intent(this, ClassActivity::class.java))
        }
        homeButton.setOnClickListener {
            setSelectedButton(homeButton)
            startActivity(Intent(this, HomeActivity::class.java))
        }

        // 图表初始化
        chartView = findViewById(R.id.chartView)
        setupChart()

        // 处理 Last 7 Days ▼ 点击事件
        last7Days = findViewById(R.id.last7Days)
        last7Days.setOnClickListener {
            showDayOptions()
        }

        // 初始显示 7 天
        updateChartForDays(7)
    }

    private fun setupChart() {
        chartView.description.isEnabled = false
        chartView.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chartView.xAxis.granularity = 1f
        chartView.axisRight.isEnabled = false
        chartView.axisLeft.axisMinimum = 0f
        chartView.axisLeft.axisMaximum = 100f
    }

    private fun updateChartForDays(days: Int) {
        val entries = when (days) {
            3 -> listOf(
                Entry(0f, 60f),
                Entry(1f, 75f),
                Entry(2f, 85f)
            )
            5 -> listOf(
                Entry(0f, 65f),
                Entry(1f, 70f),
                Entry(2f, 80f),
                Entry(3f, 85f),
                Entry(4f, 90f)
            )
            7 -> listOf(
                Entry(0f, 60f),
                Entry(1f, 70f),
                Entry(2f, 80f),
                Entry(3f, 75f),
                Entry(4f, 90f),
                Entry(5f, 85f),
                Entry(6f, 95f)
            )
            else -> emptyList()
        }

        val dataSet = LineDataSet(entries, "Accuracy").apply {
            color = getColor(R.color.black)
            valueTextColor = getColor(R.color.black)
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            setDrawCircles(true)
        }

        chartView.data = LineData(dataSet)
        chartView.invalidate()
    }

    private fun showDayOptions() {
        val options = arrayOf("Last 3 Days", "Last 5 Days", "Last 7 Days")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select Duration")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    last7Days.text = "Last 3 Days ▼"
                    updateChartForDays(3)
                }
                1 -> {
                    last7Days.text = "Last 5 Days ▼"
                    updateChartForDays(5)
                }
                2 -> {
                    last7Days.text = "Last 7 Days ▼"
                    updateChartForDays(7)
                }
            }
        }
        builder.show()
    }

    private fun setSelectedButton(selectedButton: Button) {
        findViewById<Button>(R.id.exerciseButton).isSelected = false
        findViewById<Button>(R.id.dashboardButton).isSelected = false
        findViewById<Button>(R.id.classButton).isSelected = false
        findViewById<Button>(R.id.homeButton).isSelected = false

        selectedButton.isSelected = true
    }
}
