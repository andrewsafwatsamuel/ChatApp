package com.example.chatappfinal.presentation

import android.util.SparseBooleanArray
import android.view.View
import androidx.core.util.contains
import androidx.core.util.forEach
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<T>(val view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: T, isSelected: Boolean)
}

abstract class BaseSelectableAdapter<T, VH : BaseViewHolder<T>>(
    private val onLongClick: (List<T>) -> Unit,
    internal val onClick: (T) -> Unit,
    private val selectedItems: SparseBooleanArray = SparseBooleanArray(),
    val items: MutableList<T> = mutableListOf()
) : RecyclerView.Adapter<VH>() {

    private fun isSelected(position: Int) = selectedItems.contains(position)

    private fun toggleSelection(position: Int) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position)
        } else {
            selectedItems.put(position, true)
        }
        notifyItemChanged(position)
    }

    private fun clearSelection(selection: SparseBooleanArray = selectedItems) {
        selectedItems.clear()
        selection.forEach { position, _ -> notifyItemChanged(position) }
    }

    fun getSelectedItemCount() = selectedItems.size()

    fun getSelectedItems() =
        arrayListOf<Int>().apply { selectedItems.forEach { item, _ -> add(item) } }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position], isSelected(position))


    fun setOnSelectionChanged(position: Int): Boolean {
        toggleSelection(position)
        onLongClick(getSelection())
        notifyDataSetChanged()
        return true
    }

    private fun getSelection() = mutableListOf<T>()
        .apply { getSelectedItems().forEach { add(items[it]) } }

    fun submitList(items: List<T>) {
        this.items.clear()
        this.items.addAll(items)
        clearSelection()
        notifyDataSetChanged()
    }
}

abstract class DefaultSelectableAdapter<T, VH : BaseViewHolder<T>>(
    onLongClick: (List<T>) -> Unit,
    onClick: (T) -> Unit
) : BaseSelectableAdapter<T, VH>(onLongClick, onClick) {

    override fun onBindViewHolder(holder: VH, position: Int) = items[position].let { item ->
            super.onBindViewHolder(holder, position)
            holder.view.setOnClickListener {
                if (getSelectedItemCount() > 0) setOnSelectionChanged(position) else onClick(item)
            }
            holder.view.setOnLongClickListener { setOnSelectionChanged(position) }
        }
}