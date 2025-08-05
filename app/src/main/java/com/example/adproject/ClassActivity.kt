package com.example.adproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ClassActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_class)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 找到主要功能按钮 (修改：从 CardView 改为 Button)
        val announcementButton = findViewById<Button>(R.id.announcementButton)
        val quizButton = findViewById<Button>(R.id.quizButton)
        val leaveButton = findViewById<Button>(R.id.leaveButton)

        // 找到底部导航按钮
        val exerciseButton = findViewById<Button>(R.id.exerciseButton)
        val dashboardButton = findViewById<Button>(R.id.dashboardButton)
        val classButton = findViewById<Button>(R.id.classButton)
        val homeButton = findViewById<Button>(R.id.homeButton)

        // 默认选中 Class
        setSelectedButton(classButton)

        // 设置主要功能点击事件 (修改：使用新的 Button ID)
        announcementButton.setOnClickListener {
            // TODO: 跳转到公告页面
        }

        quizButton.setOnClickListener {
            // TODO: 跳转到测验页面
        }

        leaveButton.setOnClickListener {
            // TODO: 退出班级逻辑
        }

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
            // 保持当前页面
        }

        homeButton.setOnClickListener {
            setSelectedButton(homeButton)
            startActivity(Intent(this, HomeActivity::class.java))
        }

        // 设置Homework ListView
        val homeworkListView = findViewById<ListView>(R.id.homeworkListView)
        val homeworkData = listOf(
            HomeworkItem("Math", "Due Today, 18:00", false),           // 已过期
            HomeworkItem("Physics", "Due Tomorrow, 09:30", false),     // 2025-08-01
            HomeworkItem("English", "Due July 28, 23:59", false),      // 已过期
            HomeworkItem("Chemistry", "Due Aug 2, 14:00", false),      // 未来
            HomeworkItem("Biology", "Due Aug 3, 10:00", false)
        )
        val adapter = HomeworkAdapter(homeworkData)
        homeworkListView.adapter = adapter
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

    // Homework数据类
    data class HomeworkItem(val subject: String, val due: String, var isSelected: Boolean)

    // ListView适配器
    inner class HomeworkAdapter(private val items: List<HomeworkItem>) : BaseAdapter() {
        override fun getCount(): Int = items.size

        override fun getItem(position: Int): Any = items[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@ClassActivity).inflate(R.layout.row_homework, parent, false)
            val item = items[position]

            val starIcon = view.findViewById<ImageView>(R.id.starIcon)
            val subjectText = view.findViewById<TextView>(R.id.subjectText)
            val dueText = view.findViewById<TextView>(R.id.dueText)

            subjectText.text = item.subject
            dueText.text = item.due
            starIcon.setImageResource(if (item.isSelected) R.drawable.star_yellow else R.drawable.star_black)

            // 处理星星图标点击事件，切换选中状态
            starIcon.setOnClickListener {
                item.isSelected = !item.isSelected
                notifyDataSetChanged() // 刷新列表
            }

            return view
        }
    }
}