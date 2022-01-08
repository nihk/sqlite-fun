package nick.template.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.content.contentValuesOf
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import nick.template.di.IoContext

interface ItemsDao : Dao {
    fun items(): Flow<List<Item>>
    suspend fun insert(item: Item)
    suspend fun update(item: Item)
    suspend fun delete(item: Item)
    suspend fun nuke()
}

class SqliteItemsDao @Inject constructor(
    private val holder: DatabaseHolder,
    @IoContext private val ioContext: CoroutineContext
) : ItemsDao {
    private val notifications = MutableSharedFlow<Unit>()
    private val migrations = mapOf(
        Migration(oldVersion = 1, newVersion = 2) to Sql(
            """
            ALTER TABLE $Table
            ADD COLUMN ${Column.Rating} INTEGER DEFAULT 0
        """.trimIndent()
        )
    )

    override fun createTable(): Sql {
        return Sql(
            """
            CREATE TABLE $Table (
                ${Column.Id} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ${Column.Name} TEXT NOT NULL,
                ${Column.Description} TEXT NOT NULL,
                ${Column.Rating} INTEGER NOT NULL
            )
        """.trimIndent()
        )
    }

    override fun migrate(migration: Migration): Sql? {
        return migrations[migration]
    }

    override fun items(): Flow<List<Item>> {
        return notifications
            .onStart { emit(Unit) }
            .map { queryItems() }
    }

    private suspend fun queryItems(): List<Item> {
        val sql = """
            SELECT *
            FROM $Table
        """.trimIndent()

        return withDb {
            rawQuery(sql, null).use { cursor -> cursor.toItems() }
        }
    }

    override suspend fun insert(item: Item) = notify {
        withDb {
            insert(Table, null, item.toContentValues())
        }
    }

    override suspend fun update(item: Item) = notify {
        withDb {
            update(
                Table,
                item.toContentValues(),
                whereId(),
                arrayOf(item.id.toString())
            )
        }
    }

    override suspend fun delete(item: Item) = notify {
        withDb {
            delete(
                Table,
                whereId(),
                arrayOf(item.id.toString())
            )
        }
    }

    override suspend fun nuke() = notify {
        val sql = """
            DELETE
            FROM $Table
        """.trimIndent()

        withDb {
            execSQL(sql)
        }
    }

    private fun whereId(): String {
        return """
            ${Column.Id} = ?
        """.trimIndent()
    }

    private suspend fun <T> withDb(block: SQLiteDatabase.() -> T): T {
        return withContext(ioContext) {
            holder.awaitDatabase().block()
        }
    }

    private fun Cursor.toItems(): List<Item> {
        val idColumnIndex = getColumnIndexOrThrow(Column.Id)
        val nameColumnIndex = getColumnIndexOrThrow(Column.Name)
        val descriptionColumnIndex = getColumnIndexOrThrow(Column.Description)
        val ratingColumnIndex = getColumnIndexOrThrow(Column.Rating)
        return map {
            Item(
                id = getLong(idColumnIndex),
                name = getString(nameColumnIndex),
                description = getString(descriptionColumnIndex),
                rating = getInt(ratingColumnIndex)
            )
        }
    }

    private fun Item.toContentValues(): ContentValues {
        return contentValuesOf(
            /** No need to put [Column.Id] because the table auto-increments that value. **/
            Column.Name to name,
            Column.Description to description,
            Column.Rating to rating,
        )
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
