package com.example.roomdb_exercise.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.roomdb_exercise.R
import com.example.roomdb_exercise.adapters.NewsAdapter
import com.example.roomdb_exercise.databinding.FragmentHeadlineBinding
import com.example.roomdb_exercise.ui.MainActivity
import com.example.roomdb_exercise.ui.NewsViewModel
import com.example.roomdb_exercise.util.Constants
import com.example.roomdb_exercise.util.Resource


class HeadlineFragment : Fragment(R.layout.fragment_headline) {
    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var retryButton:Button
    lateinit var errorText:TextView
    lateinit var itemHeadlinesError:CardView
    lateinit var binding: FragmentHeadlineBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHeadlineBinding.bind(view)

        itemHeadlinesError = view.findViewById(R.id.itemHeadlinesError)

        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view:View = inflater.inflate(R.layout.item_error,null)

        retryButton = view.findViewById(R.id.retryButton)
        errorText = view.findViewById(R.id.errorText)

        newsViewModel = (activity as MainActivity).newsViewModel
        setUpHeadlinesRecycler()

        newsAdapter.setOnItemClickListener { 
            val bundle = Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(R.id.action_headlineFragment_to_articleFragment,bundle)
        }

        newsViewModel.headLines.observe(viewLifecycleOwner, Observer { response ->
            when(response){
                is Resource.Error -> {
                    hideProgressbar()
                    hideErrorMessage()
                    response.data?.let {newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults/Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = newsViewModel.headLinesPage == totalPages
                        if(isLastPage){
                            binding.recyclerHeadlines.setPadding(0,0,0,0)
                        }
                    }
                }
                is Resource.Loading ->{
                    hideProgressbar()
                    response.message?.let { message ->
                        Toast.makeText(activity,"Sorry error: $message",Toast.LENGTH_LONG).show()
                        showErrorMessage(message)
                    }
                }
                is Resource.Success ->{
                    showProgressbar()
                }
            }
        })
        retryButton.setOnClickListener {
            newsViewModel.getHeadlines("us")
        }
    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    fun hideProgressbar(){
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    fun showProgressbar(){
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    fun hideErrorMessage(){
        itemHeadlinesError.visibility = View.INVISIBLE
        isError = false
    }

    fun showErrorMessage(message:String){
        itemHeadlinesError.visibility = View.VISIBLE
        errorText.text = message
        isError = true
    }

    val scrollListener = object :RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate =
                isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                newsViewModel.getHeadlines("us")
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

        private fun setUpHeadlinesRecycler(){
            newsAdapter = NewsAdapter()
            binding.recyclerHeadlines.apply {
                adapter = newsAdapter
                layoutManager = LinearLayoutManager(activity)
                addOnScrollListener(this@HeadlineFragment.scrollListener)
            }
        }
    }