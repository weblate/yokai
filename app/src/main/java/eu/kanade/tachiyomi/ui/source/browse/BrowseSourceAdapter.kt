package eu.kanade.tachiyomi.ui.source.browse

import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible

class BrowseSourceAdapter : FlexibleAdapter<IFlexible<*>>(null, null) {
    private fun clearItems() {
        allBoundViewHolders.forEach { holder ->
            val item = getItem(holder.flexibleAdapterPosition) as? BrowseSourceItem ?: return@forEach
            item.recycle()
        }
    }

    override fun clear() {
        clearItems()
        super.clear()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        clearItems()
        super.onDetachedFromRecyclerView(recyclerView)
    }
}
