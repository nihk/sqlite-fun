package nick.template.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext
import nick.template.di.IoContext

interface Dao {
    fun createTable(): Sql
    fun migrate(migration: Migration): Sql?
}

data class Migration(
    val oldVersion: Int,
    val newVersion: Int
)

@JvmInline
value class Sql(val value: String)

class DatabaseHolder @Inject constructor(
    private val helper: SQLiteOpenHelper,
    @IoContext private val ioContext: CoroutineContext
) {
    suspend fun awaitDatabase(): SQLiteDatabase = withContext(ioContext) {
        helper.writableDatabase
    }
}

@Singleton
class AppSQLiteOpenHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    // Lazy to avoid cyclic reference -- DAOs own an instance of the database.
    private val daos: Lazy<Set<@JvmSuppressWildcards Dao>>
) : SQLiteOpenHelper(context, "app_database.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        daos.get().forEach { dao ->
            val sql = dao.createTable()
            db.execSQL(sql.value)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        var from = oldVersion
        while (from < newVersion) {
            val to = from + 1
            daos.get().forEach { dao ->
                val sql = dao.migrate(Migration(from, to))
                    ?: return@forEach
                db.execSQL(sql.value)
            }
            ++from
        }
    }
}
