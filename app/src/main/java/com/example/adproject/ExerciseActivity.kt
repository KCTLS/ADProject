package com.example.adproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class ExerciseActivity : AppCompatActivity() {

    lateinit var apiService: ApiService
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // 定义筛选选项
    private val gradeOptions = arrayOf(
        "grade1", "grade2", "grade3", "grade4", "grade5", "grade6",
        "grade7", "grade8", "grade9", "grade10", "grade11", "grade12"
    )

    private val subjectOptions = arrayOf(
        "language science", "natural science", "social science"
    )

    private val categoryOptions = arrayOf(
        "Adaptations", "Adaptations and natural selection", "Age of Exploration", "Analyzing literature",
        "Anatomy and physiology", "Animals", "Asia: society and environment", "Astronomy", "Atoms and molecules",
        "Author's purpose", "Author's purpose and tone", "Basic economic principles", "Banking and finance",
        "Biochemistry", "Capitalization", "Categories", "Cells", "Chemical reactions", "Cities", "Classification",
        "Classification and scientific names", "Climate change", "Colonial America", "Comprehension strategies",
        "Conservation", "Conservation and natural resources", "Context clues", "Creative techniques", "Cultural celebrations",
        "Designing experiments", "Descriptive details", "Developing and supporting arguments", "Domain-specific vocabulary",
        "Early 19th century American history", "Early China", "Early Modern Europe", "Early Americas",
        "Earth events", "Earth's features", "Economics", "Editing and revising", "Electricity", "Ecological interactions",
        "Engineering practices", "English colonies in North America", "Fossils", "Force and motion", "Formatting",
        "Genes to traits", "Geography", "Greece", "Government", "Heat and thermal energy", "Heredity", "Historical figures",
        "Independent reading comprehension", "Informational texts: level 1", "Islamic empires", "Kinetic and potential energy",
        "Literary devices", "Magnets", "Maps", "Materials", "Mixtures", "Natural resources and human impacts",
        "Oceania: geography", "Oceans and continents", "Opinion writing", "Particle motion and energy", "Persuasive strategies",
        "Phrases and clauses", "Photosynthesis", "Physical Geography", "Physical and chemical change", "Plant reproduction",
        "Plants", "Poetry elements", "Pronouns", "Pronouns and antecedents", "Read-alone texts", "Reading-comprehension",
        "Reference skills", "Research skills", "Rhyming", "Rocks and minerals", "Scientific names", "Science-and-engineering-practices",
        "Sentences, fragments, and run-ons", "Shades of meaning", "Short and long vowels", "Social studies skills", "Solutions",
        "States", "State capitals", "States of matter", "Supply and demand", "Text structure", "The Americas: geography",
        "The Antebellum period", "The American Revolution", "The Civil War", "The Civil War and Reconstruction",
        "The Constitution", "The Early Republic", "The Jacksonian period", "The Silk Road", "Thermal energy",
        "Topographic maps", "Traits", "Traits and heredity", "Units and measurement", "Velocity, acceleration, and forces",
        "Verb tense", "Visual elements", "Water cycle", "Weather and climate", "Word usage and nuance", "World religions"
    )

    private val topicOptions = arrayOf(
        "capitalization", "chemistry", "civics", "culture", "economics", "earth-science",
        "figurative-language", "global-studies", "grammar", "literacy-in-science", "phonological-awareness",
        "physics", "pronouns", "punctuation", "reading-comprehension", "reference-skills", "science-and-engineering-practices",
        "topicOptions", "units-and-measurement", "us-history", "verbs", "vocabulary", "word-study",
        "world-history", "writing-strategies"
    )


    // 用于存储用户选择的筛选项
    private val selectedGrades = mutableSetOf<String>()
    private val selectedSubjects = mutableSetOf<String>()
    private val selectedCategories = mutableSetOf<String>()
    private val selectedTopics = mutableSetOf<String>()

    // 用于存储所有问题数据的列表
    private lateinit var adapter: QuestionAdapter
    private lateinit var questionList: ListView

    // ✅ 添加一个变量来跟踪当前页码
    private var currentPage = 1
    // ✅ 添加标志位防止重复加载
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_exercise)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 创建 Retrofit 实例 ✅ 修正 baseUrl
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/student/") // ✅ 添加了 /student/
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build())
            .build()


        apiService = retrofit.create(ApiService::class.java)

        // 初始化视图
        initViews()

        // 加载初始数据
        loadInitialData()
    }

    private fun initViews() {
        // 初始化筛选按钮
        val gradeButton = findViewById<Button>(R.id.gradeButton)
        val subjectButton = findViewById<Button>(R.id.subjectButton)
        val categoryButton = findViewById<Button>(R.id.categoryButton)
        val topicButton = findViewById<Button>(R.id.topicButton)

        // 初始化 ListView
        questionList = findViewById(R.id.questionList)

        // 使用自定义适配器
        adapter = QuestionAdapter(this, mutableListOf())
        questionList.adapter = adapter

        // 为每个筛选按钮设置点击事件
        gradeButton.setOnClickListener {
            showMultiChoiceDialog("Select Grade", gradeOptions, selectedGrades, gradeOptions)
        }

        subjectButton.setOnClickListener {
            showMultiChoiceDialog("Select Subject", subjectOptions, selectedSubjects, subjectOptions)
        }

        categoryButton.setOnClickListener {
            showMultiChoiceDialog("Select Category", categoryOptions, selectedCategories, categoryOptions)
        }

        topicButton.setOnClickListener {
            showMultiChoiceDialog("Select Topic", topicOptions, selectedTopics, topicOptions)
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

        // 设置列表项点击事件
        questionList.setOnItemClickListener { _, _, position, _ ->
            // 拿到当前题目的 ID
            val questionId = adapter.getItem(position)?.id ?: return@setOnItemClickListener

            // 隐藏搜索和列表，显示 fragment 容器
            findViewById<View>(R.id.searchCard).visibility = View.GONE
            findViewById<View>(R.id.filterCard).visibility = View.GONE
            questionList.visibility = View.GONE
            findViewById<View>(R.id.fragmentContainer).visibility = View.VISIBLE

            // 只传 ID，Fragment 里自己去加载 Base64 并解码
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    QuestionFragment.newInstance(questionId)
                )
                .addToBackStack(null)
                .commit()
        }


        // 设置滚动监听，实现分页加载
        questionList.setOnScrollListener(object : android.widget.AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: android.widget.AbsListView, scrollState: Int) {
                // 滚动状态改变时的处理
            }

            override fun onScroll(view: android.widget.AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                // 当滚动到底部时加载下一页
                if (!isLoading && firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount != 0) {
                    loadNextPage()
                }
            }
        })
    }

    private fun loadInitialData() {
        // 加载初始数据
        applyFilters()
    }

    private fun showMultiChoiceDialog(
        title: String,
        allOptions: Array<String>,
        selectedSet: MutableSet<String>,
        displayOptions: Array<String>
    ) {
        val checkedItems = BooleanArray(allOptions.size) { index ->
            allOptions[index] in selectedSet
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMultiChoiceItems(displayOptions, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedSet.add(allOptions[which])
                } else {
                    selectedSet.remove(allOptions[which])
                }
            }
            .setPositiveButton("Apply") { dialog, _ ->
                // 应用筛选
                applyFilters()
                dialog.dismiss()
            }
            .setNegativeButton("Clear") { dialog, _ ->
                // 清除当前筛选器的选择
                selectedSet.clear()
                applyFilters()
                dialog.dismiss()
            }
            .setNeutralButton("Cancel") { dialog, _ ->
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

    /**
     * 应用所有筛选条件，并通过网络请求获取数据
     * 只传非空的筛选参数，page 是必需的
     */
    private fun applyFilters() {
        // 重置页码为1，因为应用新筛选条件后应从第一页开始
        currentPage = 1

        // 使用协程在后台线程执行网络请求
        coroutineScope.launch {
            try {
                // 构建筛选参数，只添加非空的筛选项
                val gradeParam = if (selectedGrades.isNotEmpty()) selectedGrades.joinToString(",") else ""
                val subjectParam = if (selectedSubjects.isNotEmpty()) selectedSubjects.joinToString(",") else ""
                val categoryParam = if (selectedCategories.isNotEmpty()) selectedCategories.joinToString(",") else ""
                val topicParam = if (selectedTopics.isNotEmpty()) selectedTopics.joinToString(",") else ""

                // 调用 API 获取题目数据
                // 只有当筛选参数不为空时才传入，page 是必需的
                val response = apiService.viewQuestion(
                    keyword = "",
                    questionName = "",
                    grade = gradeParam,
                    subject = subjectParam,
                    topic = topicParam,
                    category = categoryParam,
                    page = currentPage,
                    questionIndex = -1  // 这个参数可以省略，但为了兼容性保留
                )

                // 处理响应
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { resultDTO ->
                            if (resultDTO.errorMessage == null) {
                                // ✅ 正确的获取方式 - 修复了类型匹配问题
                                val questions: List<QsInform> = resultDTO.data?.items ?: emptyList()
                                if (questions.isEmpty()) {
                                    Toast.makeText(this@ExerciseActivity, "没有符合条件的题目", Toast.LENGTH_SHORT).show()
                                }
                                adapter.updateData(questions.toMutableList())

                                // 可以在这里添加日志，例如：
                                // Log.d("ExerciseActivity", "成功加载 ${questions.size} 道题目")
                            } else {
                                // 后端返回了业务错误
                                Toast.makeText(this@ExerciseActivity, "错误: ${resultDTO.errorMessage}", Toast.LENGTH_SHORT).show()
                            }
                        } ?: run {
                            // response.body() 为 null
                            Toast.makeText(this@ExerciseActivity, "请求失败：服务器返回空数据", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // HTTP 状态码错误 (如 404, 500)
                        Toast.makeText(this@ExerciseActivity, "网络错误: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: retrofit2.HttpException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExerciseActivity, "HTTP 错误: ${e.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExerciseActivity, "网络连接失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // 捕获其他所有异常
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExerciseActivity, "发生未知错误: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 当用户滚动到底部时，加载下一页
    private fun loadNextPage() {
        if (isLoading) return // 防止重复加载

        isLoading = true

        currentPage++ // 递增页码

        // 使用协程在后台线程执行网络请求
        coroutineScope.launch {
            try {
                // 构建筛选参数，只添加非空的筛选项
                val gradeParam = if (selectedGrades.isNotEmpty()) selectedGrades.joinToString(",") else ""
                val subjectParam = if (selectedSubjects.isNotEmpty()) selectedSubjects.joinToString(",") else ""
                val categoryParam = if (selectedCategories.isNotEmpty()) selectedCategories.joinToString(",") else ""
                val topicParam = if (selectedTopics.isNotEmpty()) selectedTopics.joinToString(",") else ""

                // 调用 API 获取题目数据
                val response = apiService.viewQuestion(
                    keyword = "",
                    questionName = "",
                    grade = gradeParam,
                    subject = subjectParam,
                    topic = topicParam,
                    category = categoryParam,
                    page = currentPage,
                    questionIndex = -1  // 这个参数可以省略，但为了兼容性保留
                )

                // 处理响应
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { resultDTO ->
                            if (resultDTO.errorMessage == null) {
                                // ✅ 正确的获取方式 - 修复了类型匹配问题
                                val questions: List<QsInform> = resultDTO.data?.items ?: emptyList()
                                if (questions.isNotEmpty()) {
                                    adapter.addItems(questions.toMutableList())
                                    // 可以在这里添加日志，例如：
                                    // Log.d("ExerciseActivity", "成功加载下一页 ${questions.size} 道题目")
                                } else {
                                    // 没有更多数据了
                                    Toast.makeText(this@ExerciseActivity, "没有更多题目了", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // 后端返回了业务错误
                                Toast.makeText(this@ExerciseActivity, "错误: ${resultDTO.errorMessage}", Toast.LENGTH_SHORT).show()
                            }
                        } ?: run {
                            // response.body() 为 null
                            Toast.makeText(this@ExerciseActivity, "请求失败：服务器返回空数据", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // HTTP 状态码错误 (如 404, 500)
                        Toast.makeText(this@ExerciseActivity, "网络错误: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                    isLoading = false // 重置加载状态
                }
            } catch (e: retrofit2.HttpException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExerciseActivity, "HTTP 错误: ${e.message()}", Toast.LENGTH_SHORT).show()
                }
                isLoading = false // 重置加载状态
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExerciseActivity, "网络连接失败", Toast.LENGTH_SHORT).show()
                }
                isLoading = false // 重置加载状态
            } catch (e: Exception) {
                // 捕获其他所有异常
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExerciseActivity, "发生未知错误: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                isLoading = false // 重置加载状态
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    fun showMainUI() {
        findViewById<View>(R.id.searchCard).visibility = View.VISIBLE
        findViewById<View>(R.id.filterCard).visibility = View.VISIBLE
        questionList.visibility = View.VISIBLE
        findViewById<View>(R.id.fragmentContainer).visibility = View.GONE
    }

}