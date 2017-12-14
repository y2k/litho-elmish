package y2k.litho.elmish.experimental.common

import android.support.v7.util.DiffUtil
import com.facebook.litho.Component
import com.facebook.litho.widget.ComponentRenderInfo
import com.facebook.litho.widget.RecyclerBinder
import com.facebook.litho.widget.RecyclerBinderUpdateCallback

internal fun <T> RecyclerBinder.applyDiff(
    old: List<T>, newItems: List<T>,
    func: (T) -> Component<*>,
    compareIds: (T, T) -> Boolean) {
    val renderer = RecyclerBinderUpdateCallback.ComponentRenderer<T> { x, _ ->
        ComponentRenderInfo.create().component(func(x)).build()
    }
    val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
        override fun getOldListSize(): Int = old.size
        override fun getNewListSize(): Int = newItems.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            compareIds(old[oldItemPosition], newItems[newItemPosition])

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition] == newItems[newItemPosition]
    })
    val callback = RecyclerBinderUpdateCallback.acquire(
        old.size, newItems, renderer, this)
    diffResult.dispatchUpdatesTo(callback)
    callback.applyChangeset()
    RecyclerBinderUpdateCallback.release(callback)
}