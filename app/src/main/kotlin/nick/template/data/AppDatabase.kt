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

interface DatabaseLifecycleDelegate {
    fun createTable(): String
    fun migrate(migration: Migration): String?
}

data class Migration(
    val oldVersion: Int,
    val newVersion: Int
)

class DatabaseHolder @Inject constructor(
    private val helper: SQLiteOpenHelper,
    @IoContext private val ioContext: CoroutineContext
) {
    suspend fun awaitDatabase(): SQLiteDatabase = withContext(ioContext) {
        helper.writableDatabase
    }
}

@Singleton
class SqliteAppDatabase @Inject constructor(
    @ApplicationContext private val context: Context,
    // Lazy to avoid cyclic reference -- DAOs owns an instance of the database.
    private val delegates: Lazy<Set<@JvmSuppressWildcards DatabaseLifecycleDelegate>>
) : SQLiteOpenHelper(context, "app_database.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        delegates.get().forEach { delegate ->
            val sql = delegate.createTable()
            db.execSQL(sql)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        delegates.get().forEach { delegate ->
            val sql = delegate.migrate(Migration(oldVersion, newVersion))
                ?: return@forEach
            db.execSQL(sql)
        }
    }
}
