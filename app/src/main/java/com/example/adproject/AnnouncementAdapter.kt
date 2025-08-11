// AnnouncementAdapter.kt
package com.example.adproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.adproject.model.AnnouncementItem
import com.example.adproject.model.displayTime

class AnnouncementAdapter(
    private val ctx: Context,
    private val data: MutableList<AnnouncementItem> = mutableListOf()
) : BaseAdapter() {

    fun setItems(list: List<AnnouncementItem>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = data.size
    override fun getItem(position: Int): AnnouncementItem = data[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val v = convertView ?: LayoutInflater.from(ctx)
            .inflate(R.layout.row_announcement, parent, false)

        val item = getItem(position)

        v.findViewById<TextView>(R.id.tvTitle).text = item.title
        v.findViewById<TextView>(R.id.tvSnippet).text = item.content
        v.findViewById<TextView>(R.id.tvTime).text = item.displayTime()

        // 显示班级名，如果为空则隐藏
        v.findViewById<TextView?>(R.id.tvAuthor)?.let { tvAuthor ->
            val clsName = item.className  // 这里用你的数据模型里的字段
            if (!clsName.isNullOrBlank()) {
                tvAuthor.text = clsName
                tvAuthor.visibility = View.VISIBLE
            } else {
                tvAuthor.visibility = View.GONE
            }
        }


        // 可选：未读/置顶点位如果你要用，按需显示
        // v.findViewById<View>(R.id.dotUnread)?.visibility = View.GONE
        // v.findViewById<ImageView>(R.id.ivPinned)?.visibility = View.GONE

        return v
    }
}
