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

    private fun createWelcomeList() {
        val welcomeList = GroceryList("Vítejte v ShopList!")
        welcomeList.items.addAll(listOf(
            GroceryItem("Kuřecí prsa", 500, "Maso", false),
            GroceryItem("Rajčata", 4, "Zelenina", false),
            GroceryItem("Jablka", 6, "Ovoce", false),
            GroceryItem("Mléko", 2, "Mléčné výrobky", false),
            GroceryItem("Chléb", 1, "Chléb", false),
            GroceryItem("Sýr", 200, "Mléčné výrobky", false),
            GroceryItem("Mrkev", 3, "Zelenina", false),
            GroceryItem("Banány", 5, "Ovoce", false),
            GroceryItem("Jogurt", 3, "Mléčné výrobky", false),
            GroceryItem("Hovězí maso", 300, "Maso", false)
        ))
        
        // Удаляем существующий приветственный список, если он есть
        groceryLists.value?.removeAll { it.name == "Vítejte v ShopList!" }
        
        // Добавляем новый приветственный список в начало
        groceryLists.value?.add(0, welcomeList)
        groceryLists.value = groceryLists.value
        saveData()
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
                            list.items.add(
                                GroceryItem(
                                    itemObject.getString("name"),
                                    itemObject.getInt("quantity"),
                                    itemObject.getString("type"),
                                    itemObject.getBoolean("purchased")
                                )
                            )
                        }
                        lists.add(list)
                    }
                    groceryLists.value = lists
                } else {
                    groceryLists.value = mutableListOf()
                }
                
                // Всегда создаем приветственный список при загрузке
                createWelcomeList()
            } catch (e: Exception) {
                groceryLists.value = mutableListOf()
                createWelcomeList()
            }
        }
    }

    data class GroceryList(
        var name: String,
        val items: MutableList<GroceryItem> = mutableListOf()
    )

    data class GroceryItem(
        val name: String,
        val quantity: Int,
        val type: String,
        var purchased: Boolean
    )
}
