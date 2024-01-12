package eu.kanade.tachiyomi.ui.base.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.util.system.getResourceColor
import eu.kanade.tachiyomi.util.view.backgroundColor

abstract class BaseLegacyController<VB : ViewBinding>(bundle: Bundle? = null) :
    BaseController(bundle) {

    lateinit var binding: VB
    val isBindingInitialized get() = this::binding.isInitialized

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        showLegacyAppBar()
        binding = createBinding(inflater)
        binding.root.backgroundColor = binding.root.context.getResourceColor(R.attr.background)
        return binding.root
    }

    abstract fun createBinding(inflater: LayoutInflater): VB
}
