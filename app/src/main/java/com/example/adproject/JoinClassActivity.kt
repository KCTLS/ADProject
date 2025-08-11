package com.example.adproject

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.adproject.api.ApiClient
import com.example.adproject.model.JoinClassResponse
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

class JoinClassActivity : AppCompatActivity() {

    companion object {
        private const val ACCESS_BY_NAME = "byName"
        // 如果后端是全小写 “bylink”，请把下面改为 "bylink"
        private const val ACCESS_BY_LINK = "byLink"
    }

    // UI
    private lateinit var toolbar: MaterialToolbar
    private lateinit var toggle: MaterialButtonToggleGroup
    private lateinit var btnByToken: MaterialButton
    private lateinit var btnByName: MaterialButton
    private lateinit var inputLayout: TextInputLayout
    private lateinit var etQuery: TextInputEditText
    private lateinit var btnAction: MaterialButton
    private lateinit var progress: View
    private lateinit var emptyState: View
    private lateinit var emptyText: TextView

    // 状态
    private var modeByToken: Boolean = true

    // 统一的 API（带 Cookie）
    private val api by lazy { ApiClient.api }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_join_class)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        bindViews()
        setupToolbar()
        setupToggle()
        setupTextWatcher()

        // 默认 Token 模式
        toggle.check(R.id.btnByToken)
        updateMode(true)

        btnAction.setOnClickListener { onJoinClicked() }
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.topAppBar)
        toggle = findViewById(R.id.toggleAccessType)
        btnByToken = findViewById(R.id.btnByToken)
        btnByName = findViewById(R.id.btnByName)
        inputLayout = findViewById(R.id.inputLayout)
        etQuery = findViewById(R.id.etQuery)
        btnAction = findViewById(R.id.btnAction)
        progress = findViewById(R.id.progress)
        emptyState = findViewById(R.id.emptyState)
        emptyText = findViewById(R.id.emptyText)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupToggle() {
        toggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btnByToken -> updateMode(true)
                R.id.btnByName -> updateMode(false)
            }
        }
    }

    private fun setupTextWatcher() {
        etQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnAction.isEnabled = !s.isNullOrBlank()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        btnAction.isEnabled = false
    }

    private fun updateMode(byToken: Boolean) {
        modeByToken = byToken
        etQuery.setText("")
        btnAction.isEnabled = false

        if (byToken) {
            inputLayout.hint = "Enter token"
            inputLayout.startIconDrawable = getDrawable(android.R.drawable.ic_lock_idle_lock)
            btnAction.text = "Join"
            emptyState.visibility = View.GONE
        } else {
            inputLayout.hint = "Enter class name"
            inputLayout.startIconDrawable = getDrawable(android.R.drawable.ic_menu_search)
            btnAction.text = "Join"
            emptyState.visibility = View.VISIBLE
            emptyText.text = "Enter a class name, then tap Join"
        }
    }

    private fun onJoinClicked() {
        val key = etQuery.text?.toString()?.trim().orEmpty()
        if (key.isEmpty()) {
            Toast.makeText(this, "Input cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        val accessType = if (modeByToken) ACCESS_BY_LINK else ACCESS_BY_NAME
        join(accessType, key)
    }

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnAction.isEnabled = !loading && !etQuery.text.isNullOrBlank()
        btnByToken.isEnabled = !loading
        btnByName.isEnabled = !loading
        etQuery.isEnabled = !loading
    }

    private fun join(accessType: String, key: String) {
        setLoading(true)
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val resp = api.joinClass(accessType, key)
                    val body: JoinClassResponse? = resp.body()

                    if (!resp.isSuccessful || body == null) {
                        val raw = resp.errorBody()?.string()
                        Log.e("JoinClass", "HTTP ${resp.code()} ${resp.message()} body=$raw")
                        false to "HTTP ${resp.code()}: ${resp.message()}"
                    } else {
                        when (body.code) {
                            1 -> true to (body.msg ?: "Joined successfully")
                            0 -> true to (body.msg ?: "Already in this class") // 你后端示例
                            else -> false to (body.msg ?: "Join failed")
                        }
                    }
                } catch (e: Exception) {
                    false to (e.message ?: "Request failed")
                }
            }

            setLoading(false)
            Toast.makeText(this@JoinClassActivity, result.second, Toast.LENGTH_SHORT).show()
            if (result.first) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
