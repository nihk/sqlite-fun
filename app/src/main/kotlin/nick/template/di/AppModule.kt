package nick.template.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import nick.template.data.DatabaseLifecycleDelegate
import nick.template.data.ItemsDao
import nick.template.data.SqliteItemsDao

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    companion object {
        @Provides
        @IoContext
        fun ioContext(): CoroutineContext = Dispatchers.IO
    }

    @Binds
    abstract fun itemsDao(sqliteItemsDao: SqliteItemsDao): ItemsDao

    @Binds
    @IntoSet
    abstract fun itemsDatabaseLifecycleDelegate(sqliteItemsDao: SqliteItemsDao): DatabaseLifecycleDelegate
}
