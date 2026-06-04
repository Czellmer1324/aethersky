package org.czellmer1324.aetherskyPlugin.player.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder

fun serializePlayerInvent(inventory: Inventory) : String {
    val itemMap = HashMap<Int, String>()

    for (i in 0..<inventory.size) {
        val item = inventory.getItem(i)
        println(item == null)
        println(item?.type ?: "null")
        if (item != null && !item.type.isAir) {
            itemMap[i] = Base64Coder.encodeLines(item.serializeAsBytes())
        }
    }

    return Gson().toJson(itemMap)
}

fun deserializePlayerInvent(invent : String) : Array<ItemStack?> {
    val mapType = object : TypeToken<HashMap<Int, String>>() {}.type
    val inventMap : HashMap<Int, String> = Gson().fromJson(invent, mapType)

    val inventory : Array<ItemStack?> = arrayOfNulls(size = 41)

   inventMap.forEach { (slot, itemString) ->
       val item = ItemStack.deserializeBytes(Base64Coder.decodeLines(itemString))
       inventory[slot] = item
   }

    return inventory
}