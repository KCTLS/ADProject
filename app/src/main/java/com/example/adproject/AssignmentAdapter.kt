package com.example.adproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.adproject.model.ClassAssignmentItem
import java.time.LocalDateTime
import java.time.Duration
import java.util.Calendar

class AssignmentAdapter(
    private val onClick: (ClassAssignmentItem) -> Unit
) : RecyclerView.Adapter<AssignmentAdapter.VH>() {

    private val items = mutableListOf<ClassAssignmentItem>()

    fun submit(data: List<ClassAssignmentItem>) {
        items.clear(); items.addAll(data); notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assignment, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position], onClick)

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitle = v.findViewById<TextView>(R.id.tvTitle)
        private val tvMeta  = v.findViewById<TextView>(R.id.tvMeta)
        private val dot     = v.findViewById<View>(R.id.dotStatus)

        fun bind(item: ClassAssignmentItem, onClick: (ClassAssignmentItem) -> Unit) {
            tvTitle.text = item.assignmentName

            val dueMs = item.expireTime.toEpochMillis()
            val nowMs = System.currentTimeMillis()
            val progress = AssignmentProgressStore.get(itemView.context, item.assignmentId)
            val answered = progress.answers.size
            val total = itemView.tag as? Int ?: 0 // 可在提交数据后传入题目总数；或进入做题页返回时更新
            val completedLocal = progress.completed
            val completedRemote = item.whetherFinish == 1

            val status = when {
                dueMs != null && nowMs > dueMs -> "expired"
                completedRemote || completedLocal -> "done"
                answered > 0 -> "doing"
                else -> "new"
            }

            val (dotRes, clickable, alpha, statusText) = when (status) {
                "expired" -> Quad(R.drawable.bg_dot_gray, false, 0.5f, "Expired")
                "done"    -> Quad(R.drawable.bg_dot_green, true, 1f, "Completed")
                "doing"   -> Quad(R.drawable.bg_dot_orange, true, 1f, "In progress${if (total>0) " ($answered/$total)" else ""}")
                else      -> Quad(R.drawable.bg_dot_red,   true, 1f, "Not started")
            }

            val dueText = item.expireTime.toDateTimeText()
            tvMeta.text = if (dueText.isNotEmpty()) "Due $dueText · $statusText" else statusText

            dot.visibility = View.VISIBLE
            dot.setBackgroundResource(dotRes)
            itemView.isEnabled = clickable
            itemView.alpha = alpha
            itemView.setOnClickListener { if (clickable) onClick(item) }
        }
        private data class Quad<A,B,C,D>(val a:A,val b:B,val c:C,val d:D)
    }
}

/** 工具方法：把 [yyyy,MM,dd,HH,mm,ss] 转 LocalDateTime */
private fun List<Int>?.toLocalDateTime(): LocalDateTime? {
    if (this == null || this.isEmpty()) return null
    val y = this.getOrNull(0) ?: return null
    val m = this.getOrNull(1) ?: 1
    val d = this.getOrNull(2) ?: 1
    val h = this.getOrNull(3) ?: 0
    val min = this.getOrNull(4) ?: 0
    val s = this.getOrNull(5) ?: 0
    return LocalDateTime.of(y, m, d, h, min, s)
}

/** [yyyy,MM,dd,HH,mm,ss] -> 毫秒时间戳（本地时区） */
private fun List<Int>?.toEpochMillis(): Long? {
    if (this == null || this.isEmpty()) return null
    val y   = this.getOrNull(0) ?: return null
    val m   = this.getOrNull(1) ?: 1
    val d   = this.getOrNull(2) ?: 1
    val h   = this.getOrNull(3) ?: 0
    val min = this.getOrNull(4) ?: 0
    val s   = this.getOrNull(5) ?: 0

    val cal = Calendar.getInstance()
    cal.set(Calendar.MILLISECOND, 0)
    cal.set(y, m - 1, d, h, min, s) // 月份要减 1（Calendar 的月从 0 开始）
    return cal.timeInMillis
}

/** [yyyy,MM,dd,HH,mm,ss] -> "yyyy-MM-dd HH:mm" 文本 */
private fun List<Int>?.toDateTimeText(): String {
    if (this == null || this.isEmpty()) return ""
    val y   = this.getOrNull(0) ?: return ""
    val m   = this.getOrNull(1) ?: 1
    val d   = this.getOrNull(2) ?: 1
    val h   = this.getOrNull(3) ?: 0
    val min = this.getOrNull(4) ?: 0
    return String.format("%04d-%02d-%02d %02d:%02d", y, m, d, h, min)
}