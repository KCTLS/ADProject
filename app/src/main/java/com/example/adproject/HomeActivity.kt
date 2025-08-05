package com.example.adproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // 找到底部导航按钮
        val exerciseButton = findViewById<Button>(R.id.exerciseButton)
        val dashboardButton = findViewById<Button>(R.id.dashboardButton)
        val classButton = findViewById<Button>(R.id.classButton)
        val homeButton = findViewById<Button>(R.id.homeButton)

        // 默认选中 Home
        setSelectedButton(homeButton)


        // 设置导航点击事件
        exerciseButton.setOnClickListener {
            setSelectedButton(exerciseButton)
            startActivity(Intent(this, ExerciseActivity::class.java))
        }

        dashboardButton.setOnClickListener {
            setSelectedButton(dashboardButton)
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        classButton.setOnClickListener {
            setSelectedButton(classButton)
            startActivity(Intent(this, ClassActivity::class.java))
        }

        homeButton.setOnClickListener {
            setSelectedButton(homeButton)
            // 保持当前页面
        }
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