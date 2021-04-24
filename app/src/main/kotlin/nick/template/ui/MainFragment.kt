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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = MainFragmentBinding.bind(view)
        val viewModel: MainViewModel by viewModels { vmFactory.create(this) }

        val adapter = ItemAdapter()
        binding.recyclerView.adapter = adapter
        binding.add.setOnClickListener {
            val item = Item(name = "Legolas", description = "They're taking the hobbits to Isengard!")
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
