package com.bignerdranch.android.photogallery.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.*
import com.bignerdranch.android.photogallery.POLL_WORK
import com.bignerdranch.android.photogallery.PollWorker
import com.bignerdranch.android.photogallery.R
import com.bignerdranch.android.photogallery.databinding.FragmentPhotoGalleryBinding
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PhotoGalleryFragment : Fragment(R.layout.fragment_photo_gallery) {

    private var _binding: FragmentPhotoGalleryBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "FragmentPhotoGalleryBinding is null in ${lifecycle.currentState}"
        }

    private val viewModel: PhotoGalleryViewModel by viewModels()
    private var searchView: SearchView? = null
    private var pollingMenuItem: MenuItem? = null
    private val adapter = PhotoListAdapter { photoPageUri ->
        findNavController().navigate(
            PhotoGalleryFragmentDirections.showPhoto(
                photoPageUri
            )
        )

        /* Custom Tab /

        CustomTabsIntent.Builder()
            .setToolbarColor(
                ContextCompat.getColor(
                    requireContext(), R.color.black
                )
            )
            .setShowTitle(true)
            .build()
            .launchUrl(requireContext(), photoPageUri) */
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPhotoGalleryBinding.bind(view)
        binding.run {
            setHasOptionsMenu(true)

            /* OneTimeWorkRequest /

               val constraints = Constraints.Builder()
                   .setRequiredNetworkType(NetworkType.UNMETERED)
                   .build()

               val workRequest = OneTimeWorkRequest
                   .Builder(PollWorker::class.java)
                   .setConstraints(constraints)
                   .build()
               WorkManager.getInstance(requireContext()).enqueue(workRequest)*/

            photoGrid.layoutManager = GridLayoutManager(context, 3)
            photoGrid.adapter = adapter

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.uiState.collect { state ->
                        adapter.submitList(state.images)
                        searchView?.setQuery(state.query, false)
                        updatePollingState(state.isPolling)
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)
        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        searchView = searchItem.actionView as? SearchView
        pollingMenuItem = menu.findItem(R.id.menu_item_toggle_polling)

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("AAA", "QueryTextSubmit: $query")
                viewModel.setQuery(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("AAA", "QueryTextChange: $newText")
                return false
            }
        }
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                viewModel.setQuery("")
                true
            }
            R.id.menu_item_toggle_polling -> {
                viewModel.toggleIsPolling()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updatePollingState(isPolling: Boolean) {
        val toggleItemTitle = if (isPolling) R.string.stop_polling else R.string.start_polling
        pollingMenuItem?.setTitle(toggleItemTitle)

        if (isPolling) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            val periodRequest = PeriodicWorkRequestBuilder<PollWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                POLL_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                periodRequest
            )
        } else {
            WorkManager.getInstance(requireContext()).cancelUniqueWork(POLL_WORK)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        searchView = null
        pollingMenuItem = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}