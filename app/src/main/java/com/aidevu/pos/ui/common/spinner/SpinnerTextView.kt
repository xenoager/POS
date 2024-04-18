package com.aidevu.pos.ui.common.spinner;

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aidevu.pos.R
import com.aidevu.pos.utils.Constants


class SpinnerTextView<DataType> : AppCompatTextView {

    private lateinit var mPopupWindow: PopupWindow

    private lateinit var mListView: RecyclerView

    private var mArrowDrawable: Drawable? = null

    private var mDropDownListBackgroundDrawable: Drawable? = null

    private var mCollapseDrawable: Int = -1

    private var mExpandDrawable: Int = -1

    private var mDropDownItemHeight: Int = 0

    private var mDropDownListMaxHeight: Int = 0

    private var mNothingSelected: Boolean = false

    private var mHideArrow: Boolean = false

    private var mHintTextEnable: Boolean = false

    private var mItemSelectedPosition: Int = -1

    private var mMaxShowListCount = 6

    private lateinit var mAdapter: SpinnerTextViewBaseAdapter<DataType>

    private val mOnDismissListener = PopupWindow.OnDismissListener {
        if (!mPopupWindow.isFocusable) {
            if (!mHideArrow) {
                animateArrow(false)
                if (mCollapseDrawable != -1) {
                    setBackgroundResource(mCollapseDrawable)
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                mPopupWindow.isFocusable = true
            }, 100)
        }
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrRes: AttributeSet?) : super(context, attrRes) {
        init(context, attrRes)
    }

    constructor(context: Context, attrRes: AttributeSet?, defStyleAttr: Int) : super(context, attrRes, defStyleAttr) {
        init(context, attrRes)
    }

    fun setAdapter(adapter: SpinnerTextViewBaseAdapter<DataType>) {
        mAdapter = adapter
        mListView.adapter = adapter
    }

    fun selectItem(position: Int, title: String) {
        mAdapter.notifyItemSelected(position)
        text = title
        recalculatePopupWindowHeight()
    }

    fun setItemDecoration(itemDecoration: RecyclerView.ItemDecoration) {
        mListView.addItemDecoration(itemDecoration)
    }

    private fun recalculatePopupWindowHeight() {
        mPopupWindow.height = calculatePopupWindowHeight()
    }

    fun getSelectedItem(): DataType? {
        return mAdapter.getRealItem(mItemSelectedPosition)
    }

    private fun expand() {
       try {
           if (mAdapter.itemCount <= 0) {
               return
           }

           if (mExpandDrawable != -1) {
               setBackgroundResource(mExpandDrawable)
           }
           if (!mHideArrow) animateArrow(true)
           mNothingSelected = true
           // kts 하단 박스 마진 위치
           mPopupWindow.showAsDropDown(this, 0, 5)
       }catch (e : Exception) {}
    }

    private fun collapse() {
        if (mCollapseDrawable != -1) {
            setBackgroundResource(mCollapseDrawable)
        }
        if (!mHideArrow) animateArrow(false)
        mPopupWindow.dismiss()
    }

    private fun init(context: Context, attrRes: AttributeSet?) {
        mNothingSelected = true
        context?.obtainStyledAttributes(attrRes, R.styleable.SpinnerTextView)?.run {
            mDropDownItemHeight = getLayoutDimension(R.styleable.SpinnerTextView_dropdown_item_height, WindowManager.LayoutParams.WRAP_CONTENT)
            mDropDownListMaxHeight = getDimensionPixelSize(R.styleable.SpinnerTextView_dropdown_max_height, WindowManager.LayoutParams.WRAP_CONTENT)
            mCollapseDrawable = getResourceId(R.styleable.SpinnerTextView_collapse_item_display_background, -1)
            mExpandDrawable = getResourceId(R.styleable.SpinnerTextView_expand_item_display_background, -1)
            if (hasValue(R.styleable.SpinnerTextView_drop_down_list_background)) {
                mDropDownListBackgroundDrawable = getDrawable(R.styleable.SpinnerTextView_drop_down_list_background)
            }

            if (hasValue(R.styleable.SpinnerTextView_arrow_icon)) {
                mArrowDrawable = getDrawable(R.styleable.SpinnerTextView_arrow_icon)
                setCompoundDrawablesWithIntrinsicBounds(null, null, mArrowDrawable, null)
            }

            mHintTextEnable = getBoolean(R.styleable.SpinnerTextView_hint_text_enable, false)
            this.recycle()
        }

        // create recycler view (list view)
        mListView = createListView(context)

        // create popup window
        mPopupWindow = createPopupWindow(context)

    }

    private fun createListView(context: Context) = RecyclerView(context).apply {
        this.overScrollMode = OVER_SCROLL_NEVER
        this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private fun createPopupWindow(context: Context?) = PopupWindow(context).apply {
        this.contentView = mListView
        this.isOutsideTouchable = true
        this.isFocusable = true
        this.setOnDismissListener(mOnDismissListener)
        if (mDropDownListBackgroundDrawable != null)
            this.setBackgroundDrawable(mDropDownListBackgroundDrawable)
    }

    private fun calculatePopupWindowHeight(): Int {
        var itemHeight = mDropDownItemHeight
        // kts 여기서 높이 고정시킨다.
        var listViewHeight = 0;
        var listViewHeightAdd = 0;

        if(Constants.POS) {
            listViewHeight = (5.4 * itemHeight).toInt()
            listViewHeightAdd = 8
        }else{
            listViewHeight = (8 * itemHeight).toInt()
            listViewHeightAdd = 30
        }

        try { // 아직 어댑터가 안만들어진 상태
            if (mAdapter != null) {
                listViewHeight = if (mAdapter.itemCount >= mMaxShowListCount) {
                    mMaxShowListCount * (itemHeight + listViewHeightAdd)
                } else {
                    mAdapter.itemCount * (itemHeight + listViewHeightAdd)
                }
                if (mAdapter.itemCount == 1) {
                    if(Constants.POS) {
                        listViewHeight += 10
                    }else{
                        listViewHeight += 10
                    }
                }
            }
        }catch (e: Exception){ }

        if (mDropDownItemHeight != WindowManager.LayoutParams.WRAP_CONTENT) {
            return listViewHeight
        }

        return WindowManager.LayoutParams.WRAP_CONTENT
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun animateArrow(shouldRotateUp: Boolean) {
        val start = if (shouldRotateUp) 0 else 10000
        val end = if (shouldRotateUp) 10000 else 0
        if (mArrowDrawable != null)
            ObjectAnimator.ofInt(null, "level", start, end).start()  // ObjectAnimator.ofInt(mArrowDrawable, "level", start, end).start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mPopupWindow.width = MeasureSpec.getSize(widthMeasureSpec)
        mPopupWindow.height = calculatePopupWindowHeight()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mPopupWindow.isFocusable) {
            if (event?.action == MotionEvent.ACTION_DOWN) {
                if (isEnabled && isClickable) {
                    if (!mPopupWindow.isShowing) {
                        mPopupWindow.isFocusable = false
                        expand()
                    } else {
                        collapse()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun notifySelectedItem(title: String, position: Int) {
        mItemSelectedPosition = position
        text = title
        collapse()
        mAdapter.notifyItemSelected(position)
        if (mHintTextEnable) {
            mHintTextEnable = false
            mAdapter.enableHintText(false)
            recalculatePopupWindowHeight()
        }
    }

}