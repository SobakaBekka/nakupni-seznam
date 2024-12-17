package com.example.shoplist

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import android.text.TextWatcher
import android.text.Editable
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var addButton: ExtendedFloatingActionButton
    private lateinit var themeToggleButton: ImageButton
    private lateinit var pagerAdapter: GroceryPagerAdapter

    private var currentListPosition: Int = 0

    private val productTypes = listOf(
        "Maso", "Chléb", "Mléčné výrobky", "Zelenina", "Ovoce", "Nápoje",
        "Svačiny", "Konzervovaná jídla", "Zmražené potraviny", "Koření a omáčky",
        "Cereálie", "Těstoviny", "Ryby", "Sladkosti", "Domácí potřeby", "Jiné"
    )

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.view_pager)
        addButton = findViewById(R.id.fab_add)
        themeToggleButton = findViewById(R.id.btn_theme_toggle)

        pagerAdapter = GroceryPagerAdapter(
            lists = mutableListOf(),
            onAddListClick = { showAddListDialog() },
            onDeleteList = { position ->
                AlertDialog.Builder(this)
                    .setTitle("Smazat seznam")
                    .setMessage("Opravdu chcete tento seznam smazat?")
                    .setPositiveButton("Smazat") { _, _ ->
                        viewModel.deleteList(position)
                    }
                    .setNegativeButton("Zrušit", null)
                    .show()
            },
            onEditList = { position -> showEditListDialog(position) },
            onEditItemClick = { list, pos -> showEditItemDialog(list, pos) },
            onDeleteItemClick = { list, pos -> viewModel.deleteItem(list, pos) }
        )

        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentListPosition = position
                addButton.visibility = if (position < (viewModel.groceryLists.value?.size ?: 0)) View.VISIBLE else View.GONE
            }
        })

        addButton.setOnClickListener {
            if (currentListPosition < (viewModel.groceryLists.value?.size ?: 0)) {
                showAddItemDialog(viewModel.groceryLists.value?.get(currentListPosition))
            }
        }

        themeToggleButton.setOnClickListener {
            viewModel.toggleTheme()
            themeToggleButton.setImageResource(
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
                    R.drawable.ic_moon else R.drawable.ic_sun
            )
        }

        viewModel.groceryLists.observe(this, Observer { lists ->
            pagerAdapter.updateLists(lists)
            addButton.visibility = if (currentListPosition < lists.size) View.VISIBLE else View.GONE
        })

        viewModel.loadData()
    }

    private fun showAddItemDialog(list: MainViewModel.GroceryList?) {
        if (list == null) return
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_item, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_item_name)
        val etQuantity = dialogView.findViewById<EditText>(R.id.et_item_quantity)
        val spinnerType = dialogView.findViewById<AutoCompleteTextView>(R.id.spinner_product_type)

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, productTypes)
        spinnerType.setAdapter(adapter)
        spinnerType.setText(productTypes[0], false)

        AlertDialog.Builder(this)
            .setTitle("Přidat položku")
            .setView(dialogView)
            .setPositiveButton("Přidat") { _, _ ->
                val name = etName.text.toString().trim()
                val quantity = etQuantity.text.toString()
                val type = spinnerType.text.toString()

                if (name.isNotEmpty()) {
                    if (quantity.toIntOrNull() != null) {
                        val newItem = MainViewModel.GroceryItem(name, quantity.toInt(), type, false)
                        viewModel.addItem(currentListPosition, newItem)
                    } else {
                        Toast.makeText(this, "Množství musí být číslo", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Prosím zadejte název položky", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun showEditItemDialog(list: MainViewModel.GroceryList, position: Int) {
        if (position < 0 || position >= list.items.size) {
            Toast.makeText(this, "Neplatná pozice položky", Toast.LENGTH_SHORT).show()
            return
        }

        val item = list.items[position]
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_item, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_item_name)
        val etQuantity = dialogView.findViewById<EditText>(R.id.et_item_quantity)
        val spinnerType = dialogView.findViewById<AutoCompleteTextView>(R.id.spinner_product_type)

        etName.setText(item.name)
        etQuantity.setText(item.quantity)

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, productTypes)
        spinnerType.setAdapter(adapter)
        spinnerType.setText(item.type, false)

        AlertDialog.Builder(this)
            .setTitle("Upravit položku")
            .setView(dialogView)
            .setPositiveButton("Uložit") { _, _ ->
                val name = etName.text.toString().trim()
                val quantity = etQuantity.text.toString()
                val type = spinnerType.text.toString()

                if (name.isNotEmpty()) {
                    if (quantity.toIntOrNull() != null) {
                        val updatedItem = MainViewModel.GroceryItem(name, quantity.toInt(), type, item.purchased)
                        viewModel.editItem(currentListPosition, position, updatedItem)
                    } else {
                        Toast.makeText(this, "Množství musí být číslo", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Název položky nemůže být prázdný", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun showAddListDialog() {
        val input = EditText(this)
        input.hint = "Název seznamu"
        input.inputType = InputType.TYPE_CLASS_TEXT

        AlertDialog.Builder(this)
            .setTitle("Vytvořit nový seznam")
            .setView(input)
            .setPositiveButton("Vytvořit") { _, _ ->
                val listName = input.text.toString().trim()
                if (listName.isNotEmpty()) {
                    viewModel.addList(listName)
                    viewPager.post {
                        viewPager.setCurrentItem((viewModel.groceryLists.value?.size ?: 1) - 1, true)
                    }
                } else {
                    Toast.makeText(this, "Název seznamu nemůže být prázdný", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun showEditListDialog(position: Int) {
        val list = viewModel.groceryLists.value?.get(position) ?: return
        val input = EditText(this)
        input.setText(list.name)
        input.inputType = InputType.TYPE_CLASS_TEXT

        AlertDialog.Builder(this)
            .setTitle("Upravit název seznamu")
            .setView(input)
            .setPositiveButton("Uložit") { _, _ ->
                val listName = input.text.toString().trim()
                if (listName.isNotEmpty()) {
                    viewModel.editList(position, listName)
                } else {
                    Toast.makeText(this, "Název seznamu nemůže být prázdný", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }
}
