package nick.template.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nick.template.R
import nick.template.data.Item
import nick.template.databinding.MainFragmentBinding
import nick.template.ui.adapters.ItemAdapter
import javax.inject.Inject

class MainFragment @Inject constructor(
    private val vmFactory: MainViewModel.Factory
) : Fragment(R.layout.main_fragment) {
    private val viewModel: MainViewModel by viewModels { vmFactory.create(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = MainFragmentBinding.bind(view)

        val updates: (Item) -> Unit = { item ->
            viewModel.update(item)
        }
        val deletes: (Item) -> Unit = { item ->
            viewModel.delete(item)
        }
        val adapter = ItemAdapter(
            updates = updates,
            deletes = deletes
        )
        binding.recyclerView.adapter = adapter
        binding.add.setOnClickListener {
            val item = Item(name = "Legolas", description = "They're taking the hobbits to Isengard!", rating = (0..10).random())
            viewModel.add(item)
        }
        binding.nuke.setOnClickListener {
            viewModel.nuke()
        }

        viewModel.items()
            .onEach { adapter.submitList(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
