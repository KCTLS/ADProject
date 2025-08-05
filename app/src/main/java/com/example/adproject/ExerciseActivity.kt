package com.example.adproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ExerciseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_exercise)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 筛选按钮
        val gradeButton = findViewById<Button>(R.id.gradeButton)
        val subjectButton = findViewById<Button>(R.id.subjectButton)
        val categoryButton = findViewById<Button>(R.id.categoryButton)
        val topicButton = findViewById<Button>(R.id.topicButton)

        // 卡片列表
        val questionList = findViewById<ListView>(R.id.questionList)

        // 模拟数据
        val questions = listOf(
            Pair("Which of these states is farthest north?", R.drawable.us_map),
            Pair("Identify the question that Tom and Justin's experiment can ...", R.drawable.catapult),
            Pair("Identify the question that Kathleen and Bryant's ...", R.drawable.skiing),
            Pair("What is the probability that a goat produced by this cross will ...", R.drawable.punnett_square),
            Pair("Compare the average kinetic energies of the particles in each ...", R.drawable.particle_samples)
        )

        // 使用自定义适配器
        val adapter = QuestionAdapter(this, questions)
        questionList.adapter = adapter

        // 筛选框点击事件
        val filterOptions = arrayOf("Option 1", "Option 2", "Option 3", "Option 4")
        val selectedItems = BooleanArray(filterOptions.size)

        gradeButton.setOnClickListener {
            showMultiChoiceDialog("Grade", filterOptions, selectedItems)
        }
        subjectButton.setOnClickListener {
            showMultiChoiceDialog("Subject", filterOptions, selectedItems)
        }
        categoryButton.setOnClickListener {
            showMultiChoiceDialog("Category", filterOptions, selectedItems)
        }
        topicButton.setOnClickListener {
            showMultiChoiceDialog("Topic", filterOptions, selectedItems)
        }

        // 导航按钮
        val exerciseButton = findViewById<Button>(R.id.exerciseButton)
        val dashboardButton = findViewById<Button>(R.id.dashboardButton)
        val classButton = findViewById<Button>(R.id.classButton)
        val homeButton = findViewById<Button>(R.id.homeButton)

        // 默认选中 Exercise
        setSelectedButton(exerciseButton)

        // 导航点击事件
        exerciseButton.setOnClickListener {
            setSelectedButton(exerciseButton)
            // 保持当前页面
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
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    private fun showMultiChoiceDialog(title: String, options: Array<String>, selectedItems: BooleanArray) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMultiChoiceItems(options, selectedItems) { _, which, isChecked ->
                selectedItems[which] = isChecked
            }
            .setPositiveButton("OK") { dialog, _ ->
                val selected = options.filterIndexed { index, _ -> selectedItems[index] }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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