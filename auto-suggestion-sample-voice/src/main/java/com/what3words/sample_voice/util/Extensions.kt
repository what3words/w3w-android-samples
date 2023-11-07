package autosuggestsample.util

import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatEditText

fun AppCompatEditText.addOnTextChangedListener(onTextChanged: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged(s.toString())
        }
    })
}
