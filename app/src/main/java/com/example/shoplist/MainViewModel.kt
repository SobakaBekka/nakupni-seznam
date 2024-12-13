package com.example.shoplist

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val groceryLists = MutableLiveData<MutableList<GroceryList>>().apply { value = mutableListOf() }

    fun addList(name: String) {
        val list = GroceryList(name)
        groceryLists.value?.add(list)
        groceryLists.value = groceryLists.value
        saveData()
    }

    fun editList(position: Int, name: String) {
        groceryLists.value?.get(position)?.name = name
        groceryLists.value = groceryLists.value
        saveData()
    }

    fun deleteList(position: Int) {
        groceryLists.value?.removeAt(position)
        groceryLists.value = groceryLists.value
        saveData()
    }

    fun addItem(listPosition: Int, item: GroceryItem) {
        groceryLists.value?.get(listPosition)?.items?.add(item)
        groceryLists.value = groceryLists.value
        saveData()
    }

    fun editItem(listPosition: Int, itemPosition: Int, item: GroceryItem) {
        groceryLists.value?.get(listPosition)?.items?.set(itemPosition, item)
        groceryLists.value = groceryLists.value
        saveData()
    }

    fun deleteItem(list: GroceryList, itemPosition: Int) {
        val listIndex = groceryLists.value?.indexOf(list) ?: return
        groceryLists.value?.get(listIndex)?.items?.removeAt(itemPosition)
        groceryLists.value = groceryLists.value
        saveData()
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            val newMode = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.MODE_NIGHT_NO
            } else {
                AppCompatDelegate.MODE_NIGHT_YES
            }
            AppCompatDelegate.setDefaultNightMode(newMode)
        }
    }

    fun saveData() {
        viewModelScope.launch {
            try {
                val jsonArray = JSONArray()
                groceryLists.value?.forEach { list ->
                    val listObject = JSONObject()
                    listObject.put("name", list.name)
                    val itemsArray = JSONArray()
                    list.items.forEach { item ->
                        val itemObject = JSONObject()
                        itemObject.put("name", item.name)
                        itemObject.put("quantity", item.quantity)
                        itemObject.put("purchased", item.purchased)
                        itemObject.put("type", item.type)
                        itemsArray.put(itemObject)
                    }
                    listObject.put("items", itemsArray)
                    jsonArray.put(listObject)
                }
                val file = File(getApplication<Application>().filesDir, "grocery_lists.json")
                file.writeText(jsonArray.toString())
            } catch (e: Exception) {
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                val file = File(getApplication<Application>().filesDir, "grocery_lists.json")
                if (file.exists()) {
                    val jsonString = file.readText()
                    val jsonArray = JSONArray(jsonString)
                    val lists = mutableListOf<GroceryList>()
                    for (i in 0 until jsonArray.length()) {
                        val listObject = jsonArray.getJSONObject(i)
                        val list = GroceryList(listObject.getString("name"))
                        val itemsArray = listObject.getJSONArray("items")
                        for (j in 0 until itemsArray.length()) {
                            val itemObject = itemsArray.getJSONObject(j)
                            val item = GroceryItem(
                                itemObject.getString("name"),
                                itemObject.getString("quantity"),
                                itemObject.getBoolean("purchased"),
                                itemObject.getString("type")
                            )
                            list.items.add(item)
                        }
                        lists.add(list)
                    }
                    groceryLists.value = lists
                }
            } catch (e: Exception) {
            }
        }
    }

    data class GroceryList(
        var name: String,
        val items: MutableList<GroceryItem> = mutableListOf()
    )

    data class GroceryItem(
        var name: String,
        var quantity: String = "",
        var purchased: Boolean = false,
        var type: String = "Jiné"
    )
}
