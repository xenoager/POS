package com.aidevu.pos.ui.common.spinner;

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class SpinnerTextViewBaseAdapter<DataType> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    abstract fun getCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder

    abstract fun getBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)

    private var mSelectedIndex: Int = -1

    private var mHintTextEnable: Boolean = false

    private var mItems = ArrayList<DataType>()

    fun notifyItemSelected(index: Int) {
        mSelectedIndex = index
        notifyDataSetChanged()
    }

    fun getSelectedIndex(): Int {
        return mSelectedIndex
    }

    fun setItems(items: List<DataType>) {
        mItems.clear()
        mItems.addAll(items)
        notifyDataSetChanged()
    }

    fun getItems(): List<DataType> {
        return mItems
    }

    fun getItemForBindView(position: Int): DataType {
        // kts 선택된 아이템을 제거 하기 위한 코드
//        return if (position >= mSelectedIndex && mSelectedIndex != -1) mItems[position + 1]
//        else mItems[position]
        return mItems[position]
    }

    fun getRealItem(position: Int) : DataType? {
        if (mItems.isEmpty()) return null
        return mItems[position]
    }

    fun getPosition(position: Int): Int {
        var realPosition = position + 1
        return if (position >= mSelectedIndex && mSelectedIndex != -1) realPosition
        else position
    }

    fun enableHintText(enable: Boolean) {
        mHintTextEnable = enable
    }

    fun isEnableHintText(): Boolean {
        return mHintTextEnable
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return getCreateViewHolder(parent!!, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getBindViewHolder(holder!!, position)
    }


    override fun getItemCount(): Int {
        //kts 선택된 아이템을 제거 하기 위한 코드
//        return if (mHintTextEnable) mItems.size else mItems.size - 1
        return mItems.size
    }

    interface OnItemSelectedListener {
        fun onItemSelected(any: Any, title: String, position: Int)
    }

}