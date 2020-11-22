package ru.haroncode.aquarius.core.base

import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
import ru.haroncode.aquarius.core.Differ
import ru.haroncode.aquarius.core.RenderAdapter
import ru.haroncode.aquarius.core.ViewTypeSelector
import ru.haroncode.aquarius.core.base.strategies.DifferStrategy
import ru.haroncode.aquarius.core.clicker.Clicker
import ru.haroncode.aquarius.core.helper.RenderItemTouchHelperCallback
import ru.haroncode.aquarius.core.observer.AdapterDataSourceObserver
import ru.haroncode.aquarius.core.observer.DataSourceObserver
import ru.haroncode.aquarius.core.renderer.BaseRenderer
import ru.haroncode.aquarius.core.util.moveSwap
import java.util.*
import kotlin.reflect.KClass

class BaseRenderAdapter<T : Any>(
    differStrategy: DifferStrategy<T>,
    itemIdSelector: (T) -> Long,
    touchHelperCallback: RenderItemTouchHelperCallback,
    viewTypeSelector: ViewTypeSelector<KClass<out T>>,
    clickers: SparseArrayCompat<Clicker<*, out RecyclerView.ViewHolder>>,
    renderers: SparseArrayCompat<BaseRenderer<out T, *, out RecyclerView.ViewHolder>>
) : RenderAdapter<T>(
    itemIdSelector = itemIdSelector,
    viewTypeSelector = object : ViewTypeSelector<T> {
        override fun createViewTypeFor(item: T): Int = viewTypeSelector.createViewTypeFor(item::class)

        override fun viewTypeFor(item: T): Int = viewTypeSelector.viewTypeFor(item::class)
    },
    clickers = clickers,
    renderers = renderers,
    touchHelperCallback = touchHelperCallback
) {

    override val differ: Differ<T> = BaseDiffer(differStrategy, AdapterDataSourceObserver(this))

    private class BaseDiffer<T>(
        private val differStrategy: DifferStrategy<T>,
        private val dataSourceObserver: DataSourceObserver
    ) : Differ<T> {

        private var actualItems = emptyList<T>()

        override val currentList: List<T>
            get() = actualItems

        override fun submitList(items: List<T>) {
            val calculateDiff = differStrategy.calculateDiff(actualItems, items)
            actualItems = items
            calculateDiff.dispatchUpdatesTo(dataSourceObserver)
        }

        override fun removeAtPosition(position: Int) {
            val calculateDiff = differStrategy.removeAtPosition(position)
            actualItems = actualItems.toMutableList().apply { removeAt(position) }
            calculateDiff.dispatchUpdatesTo(dataSourceObserver)
        }

        override fun swap(fromPosition: Int, toPosition: Int) {
            val calculateDiff = differStrategy.swap(fromPosition, toPosition)
            actualItems = actualItems.moveSwap(fromPosition, toPosition)
            calculateDiff.dispatchUpdatesTo(dataSourceObserver)
        }
    }
}