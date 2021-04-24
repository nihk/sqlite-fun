package nick.template.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import nick.template.data.Item
import nick.template.data.ItemsDao
import javax.inject.Inject

class MainViewModel(private val itemsDao: ItemsDao) : ViewModel() {

    fun items(): Flow<List<Item>> {
        return itemsDao.items()
    }

    fun add(item: Item) {
        viewModelScope.launch {
            itemsDao.insert(item)
        }
    }

    fun nuke() {
        viewModelScope.launch {
            itemsDao.nuke()
        }
    }

    class Factory @Inject constructor(private val itemsDao: ItemsDao) {
        fun create(owner: SavedStateRegistryOwner): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, null) {
                override fun <T : ViewModel?> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(itemsDao) as T
                }
            }
        }
    }
}
