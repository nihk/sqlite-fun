package nick.template.data

import android.database.Cursor

inline fun <T> Cursor.map(block: () -> T): List<T> {
    val list = mutableListOf<T>()
    while (moveToNext()) {
        list += block()
    }
    return list
}
