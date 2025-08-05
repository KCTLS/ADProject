package com.example.adproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)  // 假设布局文件名为 activity_dashboard.xml
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 导航按钮
        val exerciseButton = findViewById<Button>(R.id.exerciseButton)
        val dashboardButton = findViewById<Button>(R.id.dashboardButton)
        val classButton = findViewById<Button>(R.id.classButton)
        val homeButton = findViewById<Button>(R.id.homeButton)

        // 默认选中 Dashboard
        setSelectedButton(dashboardButton)

        // 导航点击事件
        exerciseButton.setOnClickListener {
            setSelectedButton(exerciseButton)
            startActivity(Intent(this, ExerciseActivity::class.java))
        }
        dashboardButton.setOnClickListener {
            setSelectedButton(dashboardButton)
            // 保持当前页面
        }
        classButton.setOnClickListener {
            setSelectedButton(classButton)
            startActivity(Intent(this, ClassActivity::class.java))
        }
        homeButton.setOnClickListener {
            setSelectedButton(homeButton)
            startActivity(Intent(this, HomeActivity::class.java))
        }

        // 这里可以添加 Dashboard 特有的逻辑，例如图表初始化、按钮点击事件等
        // 例如：
        // val wrongAnswerCard = findViewById<androidx.cardview.widget.CardView>(R.id.wrongAnswerCard)
        // wrongAnswerCard.setOnClickListener { /* 处理点击 */ }
        // 同理处理 recommendedPracticeCard 和 answerHistoryCard
    }

    private fun setSelectedButton(selectedButton: Button) {
        // 重置所有按钮为未选中状态
        findViewById<Button>(R.id.exerciseButton).isSelected = false
        findViewById<Button>(R.id.dashboardButton).isSelected = false
        findViewById<Button>(R.id.classButton).isSelected = false
        findViewById<Button>(R.id.homeButton).isSelected = false

        // 设置选中按钮
        selectedButton.isSelected = true
    }
}