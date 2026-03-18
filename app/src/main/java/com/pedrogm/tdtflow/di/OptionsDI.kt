package com.pedrogm.tdtflow.di

import com.pedrogm.tdtflow.TdtFlowApp
import com.pedrogm.tdtflow.data.OptionsPreferences
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel

object OptionsDI {
    val viewModel: OptionsMenuViewModel by lazy {
        OptionsMenuViewModel(OptionsPreferences(TdtFlowApp.appContext))
    }
}
