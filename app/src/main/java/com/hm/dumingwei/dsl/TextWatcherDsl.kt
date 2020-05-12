package com.hm.dumingwei.dsl

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * Created by dumingwei on 2020/5/12.
 *
 * Desc:
 */
class TextWatcherDsl : TextWatcher {

    private var beforeTextChanged: ((s: CharSequence?, start: Int, count: Int, after: Int) -> Unit)? = null
    private var onTextChanged: ((s: CharSequence?, start: Int, before: Int, count: Int) -> Unit)? = null
    private var afterTextChanged: ((s: Editable?) -> Unit)? = null

    override fun afterTextChanged(s: Editable?) {
        afterTextChanged?.invoke(s)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        beforeTextChanged?.invoke(s, start, count, after)
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        onTextChanged?.invoke(s, start, before, count)
    }

    fun afterTextChanged(after: (s: Editable?) -> Unit) {
        afterTextChanged = after
    }

    fun beforeTextChanged(before: (s: CharSequence?, start: Int, count: Int, after: Int) -> Unit) {
        beforeTextChanged = before
    }

    fun onTextChanged(onChanged: (s: CharSequence?, start: Int, before: Int, count: Int) -> Unit) {
        onTextChanged = onChanged
    }

}

inline fun EditText.onTextChange(textWatcher: TextWatcherDsl.() -> Unit): TextWatcher {
    val watcher = TextWatcherDsl().apply(textWatcher)
    addTextChangedListener(watcher)
    return watcher
}

inline fun EditText.onTextChange(textWatcher: TextWatcherDsl): TextWatcher {
    addTextChangedListener(textWatcher)
    return textWatcher
}
