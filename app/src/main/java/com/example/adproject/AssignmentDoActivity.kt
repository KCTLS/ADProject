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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.coroutineScope

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
    private var questions: List<AssignmentQuestion> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assignment_do)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, ins ->
            val b = ins.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, v.paddingBottom)
            ins
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

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        btnFinish.setOnClickListener {
            lifecycleScope.launch { submitAssignment() }
        }

        loadQuestions()
    }

    private fun setLoading(b: Boolean) {
        progress.visibility = if (b) View.VISIBLE else View.GONE
    }

    private fun loadQuestions() {
        if (assignmentId <= 0) {
            Toast.makeText(this, "缺少 assignmentId", Toast.LENGTH_SHORT).show()
            return
        }
        setLoading(true)
        lifecycleScope.launch {
            questions = withContext(Dispatchers.IO) {
                try {
                    val resp = api.selectAssignment(assignmentId)
                    val body: SelectAssignmentResponse? = resp.body()
                    if (resp.isSuccessful && body?.code == 1) {
                        body.data?.list.orEmpty()
                    } else {
                        emptyList()
                    }
                } catch (_: Exception) {
                    emptyList()
                }
            }
            setLoading(false)

            adapter.submit(questions, assignmentId)
            total = questions.size
            refreshProgress()
        }
    }

    private fun onSelected(qid: Int, choice: Int) {
        AssignmentProgressStore.setAnswer(this, assignmentId, qid, choice)
        refreshProgress()
    }

    private fun refreshProgress() {
        val answered = AssignmentProgressStore.get(this, assignmentId).answers.size
        tvProgress.text = "$answered/$total"
    }

    /** 点击提交按钮时调用 */
    private suspend fun submitAssignment() {
        val progressState = AssignmentProgressStore.get(this, assignmentId)
        if (progressState.answers.size != total) {
            Toast.makeText(this, "还有未作答的题目", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        // 并发获取每题正确答案：/student/doquestion?id=...
        // 你的 SelectQuestionDTO 里只有 answer，可能是 0-based；也可能是 1-based。
        // 这里做兼容：如果 answer ∈ [1..choices.size]，当作 1-based 转为 0-based；否则直接当 0-based。
        val results = coroutineScope {
            questions.map { q ->
                async(Dispatchers.IO) {
                    try {
                        val resp = api.getQuestionById(q.id)
                        val body = resp.body()
                        if (resp.isSuccessful && body?.code == 1 && body.data != null) {
                            val dto = body.data!!
                            val raw = dto.answer
                            val max = dto.choices.size
                            val correctIndex = if (raw in 1..max) raw - 1 else raw
                            val my = progressState.answers[q.id]
                            (my != null && my == correctIndex)
                        } else {
                            false
                        }
                    } catch (_: Exception) {
                        false
                    }
                }
            }.awaitAll()
        }


        val correctCount = results.count { it }
        val accuracy = if (total > 0) (correctCount.toDouble() / total) * 100.0 else 0.0
        val accuracyRounded = String.format("%.2f", accuracy).toDouble()

        // 提交完成信息（POST）
        val ok = withContext(Dispatchers.IO) {
            try {
                val resp = api.finishAssignment(assignmentId, 1, accuracyRounded)
                resp.isSuccessful && resp.body()?.code == 1
            } catch (_: Exception) {
                false
            }
        }

        setLoading(false)

        if (ok) {
            AssignmentProgressStore.setCompleted(this, assignmentId, true)
            Toast.makeText(this, "提交成功，准确率：$accuracyRounded%", Toast.LENGTH_LONG).show()
            setResult(RESULT_OK)
            finish()
        } else {
            // 尝试把后端的 msg 打出来更好排错
            Toast.makeText(this, "提交失败，请稍后重试", Toast.LENGTH_SHORT).show()
        }
    }
}
