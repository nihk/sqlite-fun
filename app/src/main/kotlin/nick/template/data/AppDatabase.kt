package nick.template.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface DatabaseLifecycleDelegate {
    fun createTable(): String
    fun migrate(migration: Migration): String?
}

data class Migration(
    val oldVersion: Int,
    val newVersion: Int
)

@Singleton
class SqliteAppDatabase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val delegates: Set<@JvmSuppressWildcards DatabaseLifecycleDelegate>
) : SQLiteOpenHelper(context, "app_database.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        delegates.forEach { delegate ->
            val sql = delegate.createTable()
            db.execSQL(sql)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        delegates.forEach { delegate ->
            val sql = delegate.migrate(Migration(oldVersion, newVersion))
                ?: return@forEach
            db.execSQL(sql)
        }
    }
}
