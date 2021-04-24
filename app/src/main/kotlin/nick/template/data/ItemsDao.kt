package nick.template.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import dagger.Lazy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import nick.template.di.IoContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

interface ItemsDao {
    fun items(): Flow<List<Item>>
    suspend fun insert(item: Item)
    suspend fun nuke()
}

class SqliteItemsDao @Inject constructor(
    // Lazy to avoid cyclic reference -- the app database owns an instance of this DAO.
    private val sqliteOpenHelper: Lazy<SQLiteOpenHelper>,
    @IoContext private val ioContext: CoroutineContext
) : ItemsDao,
    DatabaseLifecycleDelegate {

    private val database get() = sqliteOpenHelper.get().writableDatabase
    private val invalidations = MutableSharedFlow<Unit>()
    private val migrations = mapOf<Migration, String>()

    override fun createTable(): String {
        return """
            CREATE TABLE $Table (
                ${Columns.Id} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ${Columns.Name} TEXT NOT NULL,
                ${Columns.Description} TEXT NOT NULL
            )
        """.trimIndent()
    }

    override fun migrate(migration: Migration): String? {
        return migrations[migration]
    }

    override fun items(): Flow<List<Item>> {
        return invalidations
            .map { queryItems() }
            .onStart { emit(queryItems()) }
    }

    private suspend fun queryItems(): List<Item> {
        val sql = """
            SELECT *
            FROM $Table
        """.trimIndent()
        return withContext(ioContext) {
            database.rawQuery(sql, null).use { cursor -> cursor.toItems() }
        }
    }

    override suspend fun insert(item: Item) = invalidateTable {
        withContext(ioContext) {
            database.insert(Table, null, item.toContentValues())
        }
    }

    override suspend fun nuke() = invalidateTable {
        val sql = """
            DELETE
            FROM $Table
        """.trimIndent()
        withContext(ioContext) {
            database.execSQL(sql)
        }
    }

    private fun Cursor.toItems(): List<Item> {
        val items = mutableListOf<Item>()
        while (moveToNext()) {
            val id = getLong(getColumnIndexOrThrow(Columns.Id))
            val name = getString(getColumnIndexOrThrow(Columns.Name))
            val description = getString(getColumnIndexOrThrow(Columns.Description))
            items += Item(id, name, description)
        }
        return items
    }

    private fun Item.toContentValues(): ContentValues {
        return ContentValues().apply {
            /** No need to put [Columns.Id] because the table auto-increments that value. **/
            put(Columns.Name, name)
            put(Columns.Description, description)
        }
    }

    private suspend fun invalidateTable(block: suspend () -> Unit) {
        block()
        invalidations.emit(Unit)
    }

    companion object {
        const val Table = "items"

        object Columns {
            const val Id = "id"
            const val Name = "name"
            const val Description = "description"
        }
    }
}
