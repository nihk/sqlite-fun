package nick.template.data

import android.database.Cursor

inline fun <T> Cursor.map(block: () -> T): List<T> {
    return List(count) { index ->
        moveToPosition(index)
        block()
    }
}
