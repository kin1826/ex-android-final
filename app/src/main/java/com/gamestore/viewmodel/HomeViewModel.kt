package com.gamestore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamestore.data.local.CartDao
import com.gamestore.data.local.GameDao
import com.gamestore.data.local.toModel
import com.gamestore.data.remote.*
import com.gamestore.model.*
import com.gamestore.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: GameApi,
    private val gameDao: GameDao,
    private val cartDao: CartDao,
    private val tm: TokenManager,
) : ViewModel() {

    private val _featured    = MutableStateFlow<UiState<List<Game>>>(UiState.Loading)
    private val _hotDeals    = MutableStateFlow<UiState<List<Game>>>(UiState.Loading)
    private val _newReleases = MutableStateFlow<UiState<List<Game>>>(UiState.Loading)
    private val _categories  = MutableStateFlow<List<CategoryDto>>(emptyList())
    private val _filteredGames = MutableStateFlow<UiState<List<Game>>>(UiState.Success(emptyList()))

    val featured:    StateFlow<UiState<List<Game>>> = _featured.asStateFlow()
    val hotDeals:    StateFlow<UiState<List<Game>>> = _hotDeals.asStateFlow()
    val newReleases: StateFlow<UiState<List<Game>>> = _newReleases.asStateFlow()
    val categories:  StateFlow<List<CategoryDto>>   = _categories.asStateFlow()
    val filteredGames: StateFlow<UiState<List<Game>>> = _filteredGames.asStateFlow()

    private val _selectedPriceRange = MutableStateFlow<PriceRange?>(null)
    val selectedPriceRange = _selectedPriceRange.asStateFlow()

    private val _sortBy = MutableStateFlow("newest") // newest, rating, price_asc, price_desc
    val sortBy = _sortBy.asStateFlow()

    private val _onlyDiscounted = MutableStateFlow(false)
    val onlyDiscounted = _onlyDiscounted.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val cartCount: StateFlow<Int> = cartDao
        .getCount(tm.getUserId())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private var filterJob: Job? = null

    init { refresh() }

    fun refresh() {
        loadFeatured()
        loadHotDeals()
        loadNewReleases()
        loadCategories()
        applyFilters(debounce = false)
    }

    fun setPriceRange(range: PriceRange?) {
        _selectedPriceRange.value = range
        applyFilters()
    }

    fun setSortBy(sort: String) {
        _sortBy.value = sort
        applyFilters()
    }

    fun toggleOnlyDiscounted() {
        _onlyDiscounted.value = !_onlyDiscounted.value
        applyFilters()
    }

    fun setCategory(categoryName: String?) {
        _selectedCategory.value = if (_selectedCategory.value == categoryName) null else categoryName
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters(debounce = true)
    }

    fun clearAllFilters() {
        _selectedPriceRange.value = null
        _sortBy.value = "newest"
        _onlyDiscounted.value = false
        _selectedCategory.value = null
        _searchQuery.value = ""
        applyFilters(debounce = false)
    }

    fun isFiltering(): Boolean {
        return _selectedPriceRange.value != null || 
               _sortBy.value != "newest" || 
               _onlyDiscounted.value || 
               _selectedCategory.value != null || 
               _searchQuery.value.isNotBlank()
    }

    private fun applyFilters(debounce: Boolean = false) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            if (!isFiltering()) {
                _filteredGames.value = UiState.Success(emptyList())
                return@launch
            }

            if (debounce) delay(500)
            
            _filteredGames.value = UiState.Loading
            try {
                val range = _selectedPriceRange.value
                val resp = api.getGames(
                    minPrice = range?.min,
                    maxPrice = range?.max,
                    sortBy = _sortBy.value,
                    genre = _selectedCategory.value,
                    search = _searchQuery.value.takeIf { it.isNotBlank() },
                    pageSize = 50
                )
                if (resp.isSuccessful) {
                    var items = resp.body()?.data?.items?.map { it.toModel() } ?: emptyList()
                    
                    // Nếu backend chưa hỗ trợ lọc discount, ta lọc ở client
                    if (_onlyDiscounted.value) {
                        items = items.filter { it.hasDiscount }
                    }

                    _filteredGames.value = UiState.Success(items)
                } else {
                    _filteredGames.value = UiState.Error("Không thể tải danh sách game")
                }
            } catch (e: Exception) {
                _filteredGames.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    private fun loadFeatured() = viewModelScope.launch {
        _featured.value = UiState.Loading
        try {
            val resp = api.getFeatured()
            if (resp.isSuccessful && resp.body()?.data?.items != null) {
                val dtos = resp.body()!!.data!!.items
                gameDao.insertAll(dtos.map { it.toEntity() })
                _featured.value = UiState.Success(dtos.map { it.toModel() })
            }
        } catch (_: Exception) {
            val cached = gameDao.getFeatured().first()
            if (cached.isNotEmpty()) _featured.value = UiState.Success(cached.map { it.toModel() })
        }
    }

    private fun loadHotDeals() = viewModelScope.launch {
        _hotDeals.value = UiState.Loading
        try {
            val resp = api.getHotDeals()
            if (resp.isSuccessful && resp.body()?.data?.items != null) {
                val dtos = resp.body()!!.data!!.items
                gameDao.insertAll(dtos.map { it.toEntity() })
                _hotDeals.value = UiState.Success(dtos.map { it.toModel() })
            }
        } catch (_: Exception) {
            val cached = gameDao.getHotDeals().first()
            if (cached.isNotEmpty()) _hotDeals.value = UiState.Success(cached.map { it.toModel() })
        }
    }

    private fun loadNewReleases() = viewModelScope.launch {
        _newReleases.value = UiState.Loading
        try {
            val resp = api.getNewReleases()
            if (resp.isSuccessful && resp.body()?.data?.items != null) {
                val dtos = resp.body()!!.data!!.items
                gameDao.insertAll(dtos.map { it.toEntity() })
                _newReleases.value = UiState.Success(dtos.map { it.toModel() })
            }
        } catch (_: Exception) {
            val cached = gameDao.getNewReleases().first()
            if (cached.isNotEmpty()) _newReleases.value = UiState.Success(cached.map { it.toModel() })
        }
    }

    private fun loadCategories() = viewModelScope.launch {
        try {
            val resp = api.getCategories()
            if (resp.isSuccessful) {
                _categories.value = resp.body()?.data ?: emptyList()
            }
        } catch (_: Exception) {}
    }
}

data class PriceRange(val label: String, val min: Double?, val max: Double?)
