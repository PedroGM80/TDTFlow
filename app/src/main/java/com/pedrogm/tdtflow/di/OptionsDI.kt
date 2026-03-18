package com.pedrogm.tdtflow.di

import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel

object OptionsDI {
    val viewModel: OptionsMenuViewModel by lazy {
        OptionsMenuViewModel()
    }
}
