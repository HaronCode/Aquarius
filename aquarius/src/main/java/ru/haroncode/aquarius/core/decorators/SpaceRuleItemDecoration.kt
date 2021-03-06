package ru.haroncode.aquarius.core.decorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.haroncode.aquarius.core.decorators.view.Padding

/**
 * Decoration which add space between {@link RecyclerView} child's. Separate for space between child's and
 * space between child and {@link RecyclerView} container
 */
class SpaceRuleItemDecoration private constructor(
    rulesWithParams: List<RuleWithParams<Param>>
) : RuleItemDecoration<SpaceRuleItemDecoration.Param>(rulesWithParams) {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
        param: Param
    ) {
        super.getItemOffsets(outRect, view, parent, state, param)
        val layoutManager = parent.layoutManager as? LinearLayoutManager ?: return
        val orientation = layoutManager.orientation
        if (orientation == RecyclerView.VERTICAL) {
            outRect.left = if (isStartSpan(view, parent)) param.container.start else param.padding.start
            outRect.top = if (isFirstRow(view, parent)) param.container.top else param.padding.top
            outRect.right = if (isEndSpan(view, parent)) param.container.end else param.padding.end
            outRect.bottom = if (isLastRow(view, parent)) param.container.bottom else param.padding.bottom
        } else {
            outRect.left = if (isFirstRow(view, parent)) param.container.start else param.padding.start
            outRect.right = if (isLastRow(view, parent)) param.container.end else param.padding.end
            outRect.top = if (isStartSpan(view, parent)) param.container.top else param.padding.top
            outRect.bottom = if (isEndSpan(view, parent)) param.container.bottom else param.padding.bottom
        }
    }

    private fun isStartSpan(view: View, parent: RecyclerView): Boolean {
        val position = getPosition(parent, view)
        val spanCount = getSpanCount(parent)
        return position % spanCount == 0
    }

    private fun isFirstRow(view: View, parent: RecyclerView): Boolean {
        val position = getPosition(parent, view)
        val spanCount = getSpanCount(parent)
        return (position + 1).toFloat() / spanCount.toFloat() <= 1.0f
    }

    private fun isLastRow(view: View, parent: RecyclerView): Boolean {
        val position = getPosition(parent, view)
        val itemCount = parent.adapter?.itemCount ?: 0
        val spanCount = getSpanCount(parent)
        val remainder = itemCount % spanCount
        val lastRowItemCount = if (remainder == 0) spanCount else remainder
        return position >= itemCount - lastRowItemCount
    }

    private fun isEndSpan(view: View, parent: RecyclerView): Boolean {
        val position = getPosition(parent, view)
        val spanCount = getSpanCount(parent)
        return position % spanCount == spanCount - 1
    }

    private fun getSpanCount(parent: RecyclerView): Int {
        return (parent.layoutManager as? GridLayoutManager)?.spanCount ?: 1
    }

    private fun getPosition(parent: RecyclerView, view: View): Int = parent.getChildAdapterPosition(view)

    data class Param(
        val padding: Padding,
        val container: Padding
    )

    class ParamBuilder<T : Any> {

        private var padding: Padding = Padding()
        private var container: Padding? = null
        private var rule: DecorationRule = AnyRule()

        fun paddingHorizontal(value: Int) {
            padding = padding.copy(start = value, end = value)
        }

        fun paddingVertical(value: Int) {
            padding = padding.copy(top = value, bottom = value)
        }

        fun padding(start: Int = 0, top: Int = 0, end: Int, bottom: Int = 0) {
            padding = padding.copy(start = start, top = top, end = end, bottom = bottom)
        }

        /**
         * Sets padding for the first column, you should only use this if you use GridLayoutManager.
         * Then these padding will only be used for the first and last element in the column.
         *
         * You can use the padding or paddingHorizontal / paddingVertical methods to set padding between elements
         */
        fun container(start: Int = 0, top: Int = 0, end: Int, bottom: Int = 0) {
            container = container
                ?.copy(start = start, top = top, end = end, bottom = bottom)
                ?: Padding(start = start, top = top, end = end, bottom = bottom)
        }

        fun with(ruleBuilder: DecorationRuleBuilder<T>.() -> Unit) {
            rule = DecorationRuleBuilder<T>()
                .apply(ruleBuilder)
                .create()
        }

        internal fun create(): RuleWithParams<Param> {
            val param = Param(padding = padding, container = container ?: padding)
            return RuleWithParams(rule, param)
        }
    }

    class Builder<T : Any> {

        private val ruleWithParams = mutableListOf<RuleWithParams<Param>>()

        fun addRule(
            block: ParamBuilder<T>.() -> Unit
        ): Builder<T> {
            val ruleWithParam = ParamBuilder<T>()
                .apply(block)
                .create()
            ruleWithParams.add(ruleWithParam)
            return this
        }

        fun create() = SpaceRuleItemDecoration(ruleWithParams)
    }
}
