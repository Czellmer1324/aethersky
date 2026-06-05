package org.czellmer1324.aetherskyPlugin.player.util

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.io.encoding.Base64

fun serializePlayerInvent(inventory: Inventory) : String {
    val itemMap = HashMap<Int, ByteArray>()

    // Encode each item stack to a byte array and store its position and byte[] in a hashmap
    for (i in 0..<inventory.size) {
        val item = inventory.getItem(i)
        if (item != null && !item.type.isAir) {
            itemMap[i] = item.serializeAsBytes()
        }
    }

    // Convert the hashmap into a byte stream
    val byteStream = ByteArrayOutputStream()
    ObjectOutputStream(byteStream).use { oos ->
        oos.writeObject(itemMap)
    }

    // Encode and return the byte array as a string
    return Base64.encode(byteStream.toByteArray())
}

fun deserializePlayerInvent(invent : String) : Array<ItemStack?> {
    // If the player had nothing in their inventory
    if (invent.isEmpty()) {
        return arrayOfNulls(size = 41)
    }

    // Convert the string to a byte array
    val inventByteArray = Base64.decode(invent)

    // Suppressing cast as I know what the types are
    @Suppress("UNCHECKED_CAST")
    val inventMap : HashMap<Int, ByteArray> = ObjectInputStream(ByteArrayInputStream(inventByteArray)).use { ois ->
        ois.readObject() as HashMap<Int, ByteArray>
    }

    val inventory : Array<ItemStack?> = arrayOfNulls(size = 41)

    // Fill the array with the items stacks
   inventMap.forEach { (slot, itemBytes) ->
       val item = ItemStack.deserializeBytes(itemBytes)
       inventory[slot] = item
   }

    return inventory
}