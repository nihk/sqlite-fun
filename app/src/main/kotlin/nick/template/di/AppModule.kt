package nick.template.di

import android.database.sqlite.SQLiteOpenHelper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlinx.coroutines.Dispatchers
import nick.template.data.DatabaseLifecycleDelegate
import nick.template.data.ItemsDao
import nick.template.data.SqliteAppDatabase
import nick.template.data.SqliteItemsDao
import kotlin.coroutines.CoroutineContext

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    companion object {
        @Provides
        @IoContext
        fun ioContext(): CoroutineContext = Dispatchers.IO
    }

    @Binds
    abstract fun sqliteOpenHelper(sqliteAppDatabase: SqliteAppDatabase): SQLiteOpenHelper

    @Binds
    abstract fun itemsDao(sqliteItemsDao: SqliteItemsDao): ItemsDao

    @Binds
    @IntoSet
    abstract fun itemsDatabaseLifecycleDelegate(sqliteItemsDao: SqliteItemsDao): DatabaseLifecycleDelegate
}
