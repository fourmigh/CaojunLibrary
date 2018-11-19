package org.caojun.areapicker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


class DataAdapter(private val context: Context, private var mDatas: Array<String?>?) : BaseAdapter() {

    override fun getCount(): Int {
        return if (mDatas == null) 0 else mDatas!!.size
    }

    override fun getItem(position: Int): Any? {
        return mDatas!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        var convertView = convertView
//        convertView = View.inflate(context, R.layout.data_textview, null)
//        val textView = convertView.findViewById<View>(R.id.data_text) as TextView
//        textView.text = mDatas!![position]
//        return textView

        val holder: ViewHolder
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.data_textview, null)
            holder = ViewHolder()
            holder.textView = view.findViewById(R.id.data_text)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        holder.textView?.text = mDatas!![position]

        return view!!
    }

    fun setList(datas: Array<String?>?) {
        if (datas != null && datas.isNotEmpty()) {
            mDatas = datas
        }
        notifyDataSetChanged()
    }

    private inner class ViewHolder {
        internal var textView: TextView? = null
    }
}
