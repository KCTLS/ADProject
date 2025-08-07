package com.example.adproject

import android.graphics.BitmapFactory
import android.os.*
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuestionFragment : Fragment() {

    companion object {
        private const val ARG_QUESTION_ID = "questionId"
        fun newInstance(questionId: Int): QuestionFragment {
            return QuestionFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_QUESTION_ID, questionId)
                }
            }
        }
    }

    private var questionId: Int = 0
    private lateinit var apiService: ApiService

    // UI
    private lateinit var questionText: TextView
    private lateinit var questionImage: ImageView
    private lateinit var optionsGroup: RadioGroup
    private lateinit var confirmBtn: Button

    // Data
    private var choices: List<String> = emptyList()
    private var correctAnswerIndex: Int = -1
    private var selectedOptionIndex: Int = -1
    private var isAnswered = false

    private lateinit var feedbackText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        questionId = arguments?.getInt(ARG_QUESTION_ID) ?: 0
        apiService = (activity as ExerciseActivity).apiService
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_question, container, false).also { view ->
        questionText = view.findViewById(R.id.questionText)
        questionImage = view.findViewById(R.id.questionImage)
        optionsGroup = view.findViewById(R.id.optionsGroup)
        confirmBtn = view.findViewById(R.id.confirmButton)
        confirmBtn.setOnClickListener { onConfirmClicked() }
        feedbackText = view.findViewById(R.id.feedbackText)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAndDisplayQuestion()
    }

    private fun loadAndDisplayQuestion() {
        lifecycleScope.launch {
            val resp = withContext(Dispatchers.IO) {
                apiService.getQuestionById(questionId)
            }

            if (resp.isSuccessful) {
                val dto = resp.body()?.data
                if (dto != null) {
                    questionText.text = dto.question
                    correctAnswerIndex = dto.answer
                    choices = dto.choices

                    if (!dto.image.isNullOrEmpty()) {
                        lifecycleScope.launch {
                            try {
                                val bmp = withContext(Dispatchers.IO) {
                                    val bytes = Base64.decode(dto.image, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                }

                                if (bmp != null && isAdded && view != null) {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        if (isAdded && view != null) {
                                            questionImage.setImageBitmap(bmp)
                                        }
                                    }, 50)
                                } else {
                                    Log.e("QuestionFragment", "Bitmap 解析失败或 Fragment 已销毁")
                                    if (isAdded && view != null) {
                                        questionImage.setImageResource(R.drawable.error_image)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("QuestionFragment", "图片解码异常", e)
                                if (isAdded && view != null) {
                                    questionImage.setImageResource(R.drawable.error_image)
                                }
                            }
                        }
                    } else {
                        questionImage.setImageResource(R.drawable.placeholder_image)
                    }

                    optionsGroup.removeAllViews()
                    choices.forEachIndexed { _, text ->
                        val rb = RadioButton(requireContext()).apply {
                            id = View.generateViewId()
                            this.text = text
                            setPadding(8, 8, 8, 8)
                        }
                        optionsGroup.addView(rb)
                    }

                    optionsGroup.setOnCheckedChangeListener { group, checkedId ->
                        selectedOptionIndex = group.indexOfChild(group.findViewById(checkedId))
                    }

                } else {
                    Toast.makeText(requireContext(), "题目数据为空", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "加载失败：${resp.code()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onConfirmClicked() {
        if (isAnswered) {
            parentFragmentManager.popBackStack()
            (activity as? ExerciseActivity)?.showMainUI()
            return
        }

        if (selectedOptionIndex == -1) {
            Toast.makeText(requireContext(), "请选择一个选项", Toast.LENGTH_SHORT).show()
            return
        }

        val correct = (selectedOptionIndex == correctAnswerIndex)
        val msg = if (correct)
            "✅ 答对了！"
        else
            "❌ 答错了，正确答案是：${choices[correctAnswerIndex]}"

        feedbackText.text = msg
        feedbackText.visibility = View.VISIBLE


        lifecycleScope.launch {
            try {
                val rsp = apiService.answerQuestion(
                    id = questionId,
                    correct = if (correct) 1 else 0,
                    param = selectedOptionIndex
                )
                if (rsp.isSuccessful && rsp.body()?.code == 1) {
                    Toast.makeText(requireContext(), "提交成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "提交失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "网络错误", Toast.LENGTH_SHORT).show()
            }
        }

        isAnswered = true
        confirmBtn.text = "返回"
    }
}
