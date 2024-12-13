package com.example.shoplist

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroceryPagerAdapter(
    private var lists: MutableList<MainViewModel.GroceryList>,
    private val onAddListClick: () -> Unit,
    private val onDeleteList: (Int) -> Unit,
    private val onEditList: (Int) -> Unit,
    private val onEditItemClick: (MainViewModel.GroceryList, Int) -> Unit,
    private val onDeleteItemClick: (MainViewModel.GroceryList, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_LIST = 0
    private val VIEW_TYPE_CREATE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.fragment_grocery_list, parent, false)
        return if (viewType == VIEW_TYPE_LIST) {
            ListViewHolder(view)
        } else {
            CreateListViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ListViewHolder && position < lists.size) {
            val list = lists[position]
            holder.titleText.text = list.name
            holder.deleteButton.setOnClickListener { onDeleteList(position) }
            holder.editButton.setOnClickListener { onEditList(position) }

            holder.groceryAdapter = GroceryAdapter(list.items, { pos ->
                onEditItemClick(list, pos)
            }, { pos ->
                onDeleteItemClick(list, pos)
            })
            holder.recyclerView.layoutManager = GridLayoutManager(holder.recyclerView.context, 2)
            holder.recyclerView.adapter = holder.groceryAdapter

            holder.itemView.findViewById<View>(R.id.list_header).visibility = View.VISIBLE
            holder.itemView.findViewById<View>(R.id.recycler_view).visibility = View.VISIBLE
            holder.itemView.findViewById<View>(R.id.search_layout).visibility = View.VISIBLE
            holder.itemView.findViewById<View>(R.id.create_list_container).visibility = View.GONE

            holder.searchInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    holder.groceryAdapter?.filter(s?.toString() ?: "")
                }
            })
        } else if (holder is CreateListViewHolder) {
            holder.itemView.findViewById<View>(R.id.list_header).visibility = View.GONE
            holder.itemView.findViewById<View>(R.id.recycler_view).visibility = View.GONE
            holder.itemView.findViewById<View>(R.id.search_layout).visibility = View.GONE

            val createListContainer = holder.itemView.findViewById<View>(R.id.create_list_container)
            createListContainer.visibility = View.VISIBLE
            holder.createText.text = "Vytvořit seznam"
            createListContainer.setOnClickListener {
                onAddListClick()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < lists.size) VIEW_TYPE_LIST else VIEW_TYPE_CREATE
    }

    override fun getItemCount() = lists.size + 1

    fun updateLists(newLists: MutableList<MainViewModel.GroceryList>) {
        lists = newLists
        notifyDataSetChanged()
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view)
        val titleText: TextView = itemView.findViewById(R.id.tv_list_title)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete_list)
        val editButton: ImageButton = itemView.findViewById(R.id.btn_edit_list)
        val searchInput: EditText = itemView.findViewById(R.id.et_search)
        var groceryAdapter: GroceryAdapter? = null
    }

    class CreateListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val createText: TextView = itemView.findViewById(R.id.tv_create_list)
    }
}
