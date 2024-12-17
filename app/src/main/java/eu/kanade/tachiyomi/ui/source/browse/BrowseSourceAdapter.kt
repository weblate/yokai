package eu.kanade.tachiyomi.ui.source.browse

import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible

class BrowseSourceAdapter : FlexibleAdapter<IFlexible<*>>(null, null) {
    override fun clear() {
        allBoundViewHolders.forEach { holder ->
            val item = getItem(holder.flexibleAdapterPosition) as? BrowseSourceItem ?: return@forEach
            item.recycle()
        }

        super.clear()
    }
}
