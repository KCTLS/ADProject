package com.example.adproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adproject.api.ApiClient
import com.example.adproject.model.ClassAssignmentItem
import com.example.adproject.model.SelectClassDetailResponse
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClassAssignmentActivity : AppCompatActivity() {

    private val api by lazy { ApiClient.api }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rv: androidx.recyclerview.widget.RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View

    // ✅ 把点击回调接上
    private val adapter by lazy { AssignmentAdapter(::onAssignmentClick) }


    private var classId: Int = -1
    private var className: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_class_assignment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, v.paddingBottom)
            insets
        }

        classId = intent.getIntExtra("classId", -1)
        className = intent.getStringExtra("className") ?: ""

        toolbar = findViewById(R.id.topAppBar)
        rv = findViewById(R.id.rvAssignments)
        emptyView = findViewById(R.id.emptyView)
        progress = findViewById(R.id.progress)

        toolbar.title = if (className.isNotBlank()) "$className Assignments" else "Assignments"
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        loadAssignments()
    }

    private fun setLoading(b: Boolean) {
        progress.visibility = if (b) View.VISIBLE else View.GONE
    }

    private fun loadAssignments() {
        if (classId <= 0) {
            Toast.makeText(this, "缺少 classId", Toast.LENGTH_SHORT).show()
            return
        }
        setLoading(true)
        lifecycleScope.launch {
            val (name, list) = withContext(Dispatchers.IO) {
                try {
                    val resp = api.selectClass(classId)
                    val body: SelectClassDetailResponse? = resp.body()
                    if (resp.isSuccessful && body?.code == 1) {
                        val data = body.data
                        Pair(data?.className.orEmpty(), data?.list.orEmpty())
                    } else Pair("", emptyList())
                } catch (e: Exception) {
                    Pair("", emptyList())
                }
            }
            setLoading(false)
            if (name.isNotBlank()) toolbar.title = "$name Assignments"
            adapter.submit(list)
            emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun onAssignmentClick(item: ClassAssignmentItem) {
        // 过期的我前面已禁点，这里直接跳
        startActivity(
            Intent(this, AssignmentDoActivity::class.java)
                .putExtra("assignmentId", item.assignmentId)
                .putExtra("assignmentName", item.assignmentName)
        )
    }

}
