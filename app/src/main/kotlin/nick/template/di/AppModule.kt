package nick.template.di

import android.database.sqlite.SQLiteOpenHelper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import nick.template.data.Dao
import nick.template.data.ItemsDao
import nick.template.data.AppSQLiteOpenHelper
import nick.template.data.SqliteItemsDao

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

    companion object {
        @Provides
        @IoContext
        fun ioContext(): CoroutineContext = Dispatchers.IO
    }

    @Binds
    fun sqliteOpenHelper(helper: AppSQLiteOpenHelper): SQLiteOpenHelper

    @Binds
    fun itemsDao(sqliteItemsDao: SqliteItemsDao): ItemsDao

    @Binds
    @IntoSet
    fun itemsDatabaseLifecycleDelegate(sqliteItemsDao: SqliteItemsDao): Dao
}
