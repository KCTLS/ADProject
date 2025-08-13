package com.example.adproject

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adproject.api.ApiClient
import com.example.adproject.model.AssignmentQuestion
import com.example.adproject.model.SelectAssignmentResponse
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AssignmentDoActivity : AppCompatActivity() {
    private val api by lazy { ApiClient.api }
    private var assignmentId = -1
    private var titleName = ""

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rv: RecyclerView
    private lateinit var tvProgress: TextView
    private lateinit var btnFinish: Button
    private lateinit var progress: View

    private val adapter by lazy { DoAdapter(::onSelected) }
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(); setContentView(R.layout.activity_assignment_do)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, ins ->
            val b = ins.getInsets(WindowInsetsCompat.Type.systemBars()); v.setPadding(b.left,b.top,b.right,v.paddingBottom); ins
        }

        assignmentId = intent.getIntExtra("assignmentId", -1)
        titleName = intent.getStringExtra("assignmentName") ?: ""
        toolbar = findViewById(R.id.topAppBar)
        rv = findViewById(R.id.rv)
        tvProgress = findViewById(R.id.tvProgress)
        btnFinish = findViewById(R.id.btnFinish)
        progress = findViewById(R.id.progress)

        toolbar.title = if (titleName.isNotBlank()) titleName else "Assignment"
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        rv.layoutManager = LinearLayoutManager(this); rv.adapter = adapter

        btnFinish.setOnClickListener {
            val p = AssignmentProgressStore.get(this, assignmentId)
            if (p.answers.size == total && total > 0) {
                AssignmentProgressStore.setCompleted(this, assignmentId, true)
                Toast.makeText(this, "已标记完成", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK); finish()
            } else {
                Toast.makeText(this, "还有未作答的题目", Toast.LENGTH_SHORT).show()
            }
        }

        loadQuestions()
    }

    private fun setLoading(b: Boolean) { progress.visibility = if (b) View.VISIBLE else View.GONE }

    private fun loadQuestions() {
        if (assignmentId <= 0) {
            Toast.makeText(this, "缺少 assignmentId", Toast.LENGTH_SHORT).show()
            return
        }
        setLoading(true)
        lifecycleScope.launch {
            val qs: List<AssignmentQuestion> = withContext(Dispatchers.IO) {
                try {
                    val resp = api.selectAssignment(assignmentId)
                    val body: SelectAssignmentResponse? = resp.body()
                    if (resp.isSuccessful && body?.code == 1) {
                        body.data?.list.orEmpty()   // ✅ 一定是 List<AssignmentQuestion>
                    } else {
                        emptyList()
                    }
                } catch (_: Exception) {
                    emptyList()
                }
            }
            setLoading(false)

            // ✅ 传入的第一个参数必须是 List<AssignmentQuestion>
            adapter.submit(qs, assignmentId)
            total = qs.size
            refreshProgress()
        }
    }


    private fun onSelected(qid: Int, choice: Int) {
        AssignmentProgressStore.setAnswer(this, assignmentId, qid, choice)
        // 如果全部答完，可选：自动标记完成
        // val p = AssignmentProgressStore.get(this, assignmentId)
        // if (p.answers.size == total) AssignmentProgressStore.setCompleted(this, assignmentId, true)
        refreshProgress()
    }

    private fun refreshProgress() {
        val answered = AssignmentProgressStore.get(this, assignmentId).answers.size
        tvProgress.text = "$answered/$total"
    }
}
