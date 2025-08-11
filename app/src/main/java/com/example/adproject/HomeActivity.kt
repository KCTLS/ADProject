package com.example.adproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {

    // 本地会话存储名字（和键），你也可以统一到单独的 UserSession 工具类中
    private val PREF_NAME = "user_session"
    private val KEY_USER_ID = "user_id"
    private val KEY_USER_NAME = "user_name"   // 如果后端返回用户名，可以一起保存

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

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

        // 其他按钮
        val profileButton = findViewById<Button>(R.id.profileButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        // 默认选中 Home
        setSelectedButton(homeButton)

        // （可选）把用户名展示到卡片上：布局里如果给用户名 TextView 起了 id（比如 userNameText），这里就能设置
        setUserNameIfPossible()

        // 底部导航点击
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
            // 留在当前页
        }

        // 账户/设置
        profileButton.setOnClickListener {
            // TODO: 跳到账号管理页
            Toast.makeText(this, "Go to Account Management", Toast.LENGTH_SHORT).show()
        }
        settingsButton.setOnClickListener {
            // TODO: 跳到设置页
            Toast.makeText(this, "Go to Settings", Toast.LENGTH_SHORT).show()
        }

        // 退出登录
        logoutButton.setOnClickListener {
            logoutAndGoLogin()
        }

        // 如果没登录（user_id 缺失或为 -1），直接去登录页
        ensureLoggedInOrGoLogin()
    }

    private fun setSelectedButton(selectedButton: Button) {
        findViewById<Button>(R.id.exerciseButton).isSelected = false
        findViewById<Button>(R.id.dashboardButton).isSelected = false
        findViewById<Button>(R.id.classButton).isSelected = false
        findViewById<Button>(R.id.homeButton).isSelected = false
        selectedButton.isSelected = true
    }

    private fun ensureLoggedInOrGoLogin() {
        val userId = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getInt(KEY_USER_ID, -1)
        if (userId == -1) {
            // 未登录，跳转登录页
            goLoginClearBackStack()
        }
    }

    private fun logoutAndGoLogin() {
        // 清空会话
        getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().clear().apply()
        // 跳到登录页并清空返回栈
        goLoginClearBackStack()
    }

    private fun goLoginClearBackStack() {
        // 把 LoginActivity 换成你的实际登录页类名
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        // finish() 不一定必要，因为我们加了 CLEAR_TASK
    }

    /**
     * 可选：展示用户名（如果你在登录成功时把用户名存入了 SharedPreferences）
     * 你的布局里当前 “Jason” 的 TextView 没有 id，如果你愿意给它加一个 id（如 @+id/userNameText），
     * 这里就可以自动替换成真实用户名。
     */
    private fun setUserNameIfPossible() {
        val id = resources.getIdentifier("userNameText", "id", packageName)
        if (id != 0) {
            val tv = findViewById<TextView>(id)
            val userName = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(KEY_USER_NAME, null)
            if (!userName.isNullOrBlank()) {
                tv.text = userName
            }
        }
    }
}
