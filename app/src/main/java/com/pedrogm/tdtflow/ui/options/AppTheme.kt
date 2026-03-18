package com.pedrogm.tdtflow.ui.options

import androidx.annotation.StringRes
import com.pedrogm.tdtflow.R

enum class AppTheme(@get:StringRes val labelRes: Int) {
    SYSTEM(R.string.options_theme_system),
    LIGHT(R.string.options_theme_light),
    DARK(R.string.options_theme_dark)
}
