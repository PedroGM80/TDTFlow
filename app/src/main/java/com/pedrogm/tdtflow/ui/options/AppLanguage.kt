package com.pedrogm.tdtflow.ui.options

import androidx.annotation.StringRes
import com.pedrogm.tdtflow.R

enum class AppLanguage(@get:StringRes val labelRes: Int) {
    SYSTEM(R.string.options_language_system),
    ES(R.string.options_language_es),
    EN(R.string.options_language_en),
    CA(R.string.options_language_ca)
}
