package com.example.adproject

/**
 * QuestionFragment 的宿主接口：
 * - ExerciseActivity：支持上一/下一题与分页加载
 * - RecommendedActivity：不分页，返回 null 即可
 */
interface QuestionHost {
    /** 上一题的 id；没有就返回 null */
    fun getPrevQuestionId(currentId: Int): Int? = null

    /**
     * 下一题的 id；如果需要分页，宿主自行拉新页完成后再回调。
     * 不支持时直接 onReady(null)
     */
    fun getNextQuestionIdOrLoad(currentId: Int, onReady: (Int?) -> Unit) {
        onReady(null)
    }

    /** Fragment 点“RETURN”后，恢复宿主的主界面（列表等） */
    fun showMainUI() {}

    /** 作答结果回传：可用于宿主更新列表/统计（可不实现） */
    fun onAnswered(questionId: Int, selectedIndex: Int, isCorrect: Boolean) {}
}
