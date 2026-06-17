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
    private val _selectedGenre = MutableStateFlow<String?>(null)
    private val _searchText = MutableStateFlow("")
    private val _searchResult = MutableStateFlow<UiState<List<Game>>>(UiState.Success(emptyList()))


    val featured:    StateFlow<UiState<List<Game>>> = _featured.asStateFlow()
    val hotDeals:    StateFlow<UiState<List<Game>>> = _hotDeals.asStateFlow()
    val newReleases: StateFlow<UiState<List<Game>>> = _newReleases.asStateFlow()
    val categories:  StateFlow<List<CategoryDto>>   = _categories.asStateFlow()
    val selectedGenre = _selectedGenre.asStateFlow()
    val searchText = _searchText.asStateFlow()
    val searchResult = _searchResult.asStateFlow()
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
        _selectedGenre.value = null
        loadFeatured()
        loadHotDeals()
        loadNewReleases()
        if (_categories.value.isEmpty()) {
            loadCategories()
        }
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
//        catch (e: Exception) {
//            if (cached.isEmpty()) _featured.value = UiState.Error("Không có kết nối mạng")
//        }
        catch (e: Exception) {

            e.printStackTrace()

            if (cached.isEmpty()) {

                _featured.value =
                    UiState.Error(
                        e.message ?: "Unknown error"
                    )
            }
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
//        catch (e: Exception) {
//            if (cached.isEmpty()) _hotDeals.value = UiState.Error("Không có kết nối mạng")
//        }
        catch (e: Exception) {

            e.printStackTrace()

            if (cached.isEmpty()) {

                _hotDeals.value =
                    UiState.Error(
                        e.message ?: "Unknown error"
                    )
            }
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
//        catch (e: Exception) {
//            if (cached.isEmpty()) _newReleases.value = UiState.Error("Không có kết nối mạng")
//        }
        catch (e: Exception) {

            e.printStackTrace()

            if (cached.isEmpty()) {

                _newReleases.value =
                    UiState.Error(
                        e.message ?: "Unknown error"
                    )
            }
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
    fun loadGamesByGenre(
        genre: String
    ) = viewModelScope.launch {

        _selectedGenre.value = genre

        _featured.value = UiState.Loading
        _hotDeals.value = UiState.Loading
        _newReleases.value = UiState.Loading

        try {

            val response = api.getGames(
                genre = genre,
                page = 0,
                size = 100
            )

            if (
                response.isSuccessful &&
                response.body()?.data?.items != null
            ) {

                val games =
                    response.body()!!
                        .data!!
                        .items
                        .map { it.toModel() }

                _featured.value =
                    UiState.Success(
                        games.filter { game ->
                            game.isFeatured
                        }
                    )

                _hotDeals.value =
                    UiState.Success(
                        games.filter { game ->
                            game.isHot
                        }
                    )

                _newReleases.value =
                    UiState.Success(
                        games.filter { game ->
                            game.isNew
                        }
                    )

            } else {

                _featured.value =
                    UiState.Success(emptyList())

                _hotDeals.value =
                    UiState.Success(emptyList())

                _newReleases.value =
                    UiState.Success(emptyList())
            }

        } catch (e: Exception) {

            val message =
                e.message ?: "Lỗi tải dữ liệu"

            _featured.value =
                UiState.Error(message)

            _hotDeals.value =
                UiState.Error(message)

            _newReleases.value =
                UiState.Error(message)
        }
    }
    fun onGenreClick(genre: String) {

        if (_selectedGenre.value == genre) {
            refresh()
        } else {
            loadGamesByGenre(genre)
        }
    }
    private fun searchGames(keyword: String) = viewModelScope.launch {

        _searchResult.value = UiState.Loading

        try {
            val response = api.search(q = keyword)

            if (response.isSuccessful && response.body()?.data != null) {

                val games = response.body()!!
                    .data!!
                    .items
                    .map { it.toModel() }

                _searchResult.value = UiState.Success(games)

            } else {
                _searchResult.value = UiState.Success(emptyList())
            }

        } catch (e: Exception) {
            _searchResult.value = UiState.Error(e.message ?: "Search error")
        }
    }
    fun onSearchChange(text: String) {
        _searchText.value = text

        if (text.isBlank()) {
            _searchResult.value = UiState.Success(emptyList())
            return
        }

        searchGames(text)
    }
}

data class PriceRange(val label: String, val min: Double?, val max: Double?)
