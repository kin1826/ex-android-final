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
import kotlinx.coroutines.*
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

    // 1. Dữ liệu các phần trang chủ
    private val _featured    = MutableStateFlow<UiState<List<Game>>>(UiState.Loading)
    private val _hotDeals    = MutableStateFlow<UiState<List<Game>>>(UiState.Loading)
    private val _newReleases = MutableStateFlow<UiState<List<Game>>>(UiState.Loading)
    private val _categories  = MutableStateFlow<List<CategoryDto>>(emptyList())
    private val _isRefreshing  = MutableStateFlow(false)

    // 2. Toàn bộ trạng thái lọc quy về 1 mối
    private val _filterState = MutableStateFlow(FilterState())
    val filterState = _filterState.asStateFlow()

    // Public States cho UI
    val featured:    StateFlow<UiState<List<Game>>> = _featured.asStateFlow()
    val hotDeals:    StateFlow<UiState<List<Game>>> = _hotDeals.asStateFlow()
    val newReleases: StateFlow<UiState<List<Game>>> = _newReleases.asStateFlow()
    val categories:  StateFlow<List<CategoryDto>>   = _categories.asStateFlow()
    val isRefreshing  = _isRefreshing.asStateFlow()

    // Đếm số lượng bộ lọc đang chọn (Dùng cho Badge)
    val activeFilterCount = _filterState.map { it.activeCount }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Quyết định hiển thị danh sách kết quả (Search Mode)
    // Hiển thị danh sách nếu có Search Query HOẶC có bất kỳ bộ lọc nào khác "mặc định"
    val isSearchingMode = _filterState.map { it.isSearching }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Luồng kết quả lọc Reactive - Luôn đồng bộ với filterState
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredGames: StateFlow<UiState<List<Game>>> = _filterState
        .debounce { if (it.search.isBlank()) 0 else 500 }
        .flatMapLatest { params ->
            if (params.isDefault) {
                flowOf(UiState.Success(emptyList()))
            } else {
                flow {
                    emit(UiState.Loading)
                    try {
                        val resp = api.getGames(
                            genre = params.genre,
                            search = params.search.takeIf { it.isNotBlank() },
                            platform = params.platform,
                            minPrice = params.priceRange?.min,
                            maxPrice = params.priceRange?.max,
                            sortBy = params.sortBy,
                            onlyDiscounted = params.onlyDiscounted.takeIf { it },
                            size = 50
                        )
                        if (resp.isSuccessful) {
                            val items = resp.body()?.data?.items?.map { it.toModel() } ?: emptyList()
                            emit(UiState.Success(items))
                        } else {
                            emit(UiState.Error("Không tìm thấy kết quả phù hợp"))
                        }
                    } catch (e: Exception) {
                        emit(UiState.Error("Lỗi kết nối: ${e.message}"))
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Success(emptyList()))

    val cartCount: StateFlow<Int> = cartDao
        .getCount(tm.getUserId())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val jobs = listOf(
                launch { loadFeatured() },
                launch { loadHotDeals() },
                launch { loadNewReleases() },
                launch { loadCategories() }
            )
            jobs.joinAll()
            _isRefreshing.value = false
        }
    }

    /**
     * Hàm được gọi khi nhấn nút "Áp dụng"
     */
    fun updateFilters(genre: String?, platform: String?, sort: String, price: PriceRange?, discount: Boolean) {
        val oldGenre = _filterState.value.genre
        _filterState.update { 
            it.copy(genre = genre, platform = platform, sortBy = sort, priceRange = price, onlyDiscounted = discount)
        }
        
        // Nếu thể loại thay đổi, tải lại các phần đề cử ở trang chủ
        if (oldGenre != genre) {
            viewModelScope.launch {
                loadFeatured()
                loadHotDeals()
                loadNewReleases()
            }
        }
    }

    fun setSearchQuery(query: String) {
        _filterState.update { it.copy(search = query) }
    }

    fun onGenreClick(genre: String) {
        val newGenre = if (_filterState.value.genre == genre) null else genre
        _filterState.update { it.copy(genre = newGenre) }
        viewModelScope.launch {
            loadFeatured()
            loadHotDeals()
            loadNewReleases()
        }
    }

    fun clearAllFilters() {
        _filterState.value = FilterState()
        refresh()
    }

    private suspend fun loadFeatured() {
        _featured.value = UiState.Loading
        try {
            val genre = _filterState.value.genre
            val resp = api.getFeatured(genre = genre)
            if (resp.isSuccessful) {
                val items = resp.body()?.data?.items ?: emptyList()
                if (genre == null) gameDao.insertAll(items.map { it.toEntity() })
                _featured.value = UiState.Success(items.map { it.toModel() })
            }
        } catch (e: Exception) {
            val cached = gameDao.getFeatured().first()
            if (cached.isNotEmpty() && _filterState.value.genre == null) {
                _featured.value = UiState.Success(cached.map { it.toModel() })
            } else {
                _featured.value = UiState.Error("Lỗi tải dữ liệu")
            }
        }
    }

    private suspend fun loadHotDeals() {
        _hotDeals.value = UiState.Loading
        try {
            val resp = api.getHotDeals(genre = _filterState.value.genre)
            if (resp.isSuccessful) {
                val items = resp.body()?.data?.items ?: emptyList()
                _hotDeals.value = UiState.Success(items.map { it.toModel() })
            }
        } catch (e: Exception) {
            _hotDeals.value = UiState.Error("Lỗi tải dữ liệu")
        }
    }

    private suspend fun loadNewReleases() {
        _newReleases.value = UiState.Loading
        try {
            val resp = api.getNewReleases(genre = _filterState.value.genre)
            if (resp.isSuccessful) {
                val items = resp.body()?.data?.items ?: emptyList()
                _newReleases.value = UiState.Success(items.map { it.toModel() })
            }
        } catch (e: Exception) {
            _newReleases.value = UiState.Error("Lỗi tải dữ liệu")
        }
    }

    private suspend fun loadCategories() {
        try {
            val resp = api.getCategories()
            if (resp.isSuccessful) {
                _categories.value = resp.body()?.data ?: emptyList()
            }
        } catch (_: Exception) {}
    }
}

/**
 * Cấu trúc dữ liệu đại diện cho toàn bộ trạng thái lọc
 */
data class FilterState(
    val genre: String? = null,
    val platform: String? = null,
    val search: String = "",
    val priceRange: PriceRange? = null,
    val sortBy: String = "newest",
    val onlyDiscounted: Boolean = false
) {
    val isDefault: Boolean get() = genre == null && platform == null && search.isBlank() && 
                                  priceRange == null && sortBy == "newest" && !onlyDiscounted

    val isSearching: Boolean get() = search.isNotBlank() || platform != null ||
                                    priceRange != null || sortBy != "newest" || onlyDiscounted || genre != null

    val activeCount: Int get() {
        var count = 0
        if (genre != null) count++
        if (platform != null) count++
        if (priceRange != null) count++
        if (sortBy != "newest") count++
        if (onlyDiscounted) count++
        return count
    }
}

data class PriceRange(val label: String, val min: Double?, val max: Double?)
