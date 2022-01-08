package nick.template.data

import android.content.ContentValues
import android.database.Cursor
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import nick.template.di.IoContext

interface ItemsDao {
    fun items(): Flow<List<Item>>
    suspend fun insert(item: Item)
    suspend fun nuke()
}

class SqliteItemsDao @Inject constructor(
    private val holder: DatabaseHolder,
    @IoContext private val ioContext: CoroutineContext
) : ItemsDao,
    DatabaseLifecycleDelegate {
    private val notifications = MutableSharedFlow<Unit>()
    private val migrations = mapOf(
        Migration(oldVersion = 1, newVersion = 2) to """
            ALTER TABLE $Table
            ADD COLUMN ${Column.Rating} INTEGER DEFAULT 0
        """.trimIndent()
    )

    override fun createTable(): String {
        return """
            CREATE TABLE $Table (
                ${Column.Id} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ${Column.Name} TEXT NOT NULL,
                ${Column.Description} TEXT NOT NULL,
                ${Column.Rating} INTEGER NOT NULL
            )
        """.trimIndent()
    }

    override fun migrate(migration: Migration): String? {
        return migrations[migration]
    }

    override fun items(): Flow<List<Item>> {
        return notifications
            .map { queryItems() }
            .onStart { emit(queryItems()) }
    }

    private suspend fun queryItems(): List<Item> {
        val sql = """
            SELECT *
            FROM $Table
        """.trimIndent()
        return withContext(ioContext) {
            holder.awaitDatabase().rawQuery(sql, null).use { cursor -> cursor.toItems() }
        }
    }

    override suspend fun insert(item: Item) = notify {
        withContext(ioContext) {
            holder.awaitDatabase().insert(Table, null, item.toContentValues())
        }
    }

    override suspend fun nuke() = notify {
        val sql = """
            DELETE
            FROM $Table
        """.trimIndent()
        withContext(ioContext) {
            holder.awaitDatabase().execSQL(sql)
        }
    }

    private fun Cursor.toItems(): List<Item> {
        return map {
            val id = getLong(getColumnIndexOrThrow(Column.Id))
            val name = getString(getColumnIndexOrThrow(Column.Name))
            val description = getString(getColumnIndexOrThrow(Column.Description))
            val rating = getInt(getColumnIndexOrThrow(Column.Rating))
            Item(
                id = id,
                name = name,
                description = description,
                rating = rating
            )
        }
    }

    private fun Item.toContentValues(): ContentValues {
        return ContentValues().apply {
            /** No need to put [Column.Id] because the table auto-increments that value. **/
            put(Column.Name, name)
            put(Column.Description, description)
            put(Column.Rating, rating)
        }
    }

    private suspend fun notify(block: suspend () -> Unit) {
        block()
        notifications.emit(Unit)
    }

    companion object {
        const val Table = "items"

        object Column {
            const val Id = "id"
            const val Name = "name"
            const val Description = "description"
            const val Rating = "rating"
        }
    }
}
