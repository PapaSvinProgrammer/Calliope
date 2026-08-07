package com.mordva.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mordva.datastore.api.model.FilterData
import com.mordva.datastore.api.repository.FilterPreferencesRepository
import com.mordva.domain.location.domain.model.City
import com.mordva.domain.location.domain.usecase.LoadCityUseCase
import com.mordva.domain.location.domain.usecase.SearchCityUseCase
import com.mordva.presentation.state.FilterAction
import com.mordva.presentation.state.FilterEvent
import com.mordva.presentation.state.FilterType
import com.mordva.presentation.state.FilterUiState
import com.mordva.presentation.state.LocationCityState
import com.mordva.presentation.state.getItems
import com.mordva.presentation.utils.toData
import com.mordva.presentation.utils.toUiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal class FilterViewModel(
    private val filterPreferencesRepository: FilterPreferencesRepository,
    private val loadCityUseCase: LoadCityUseCase,
    private val searchCityUseCase: SearchCityUseCase,
) : ViewModel() {
    private var loadMoreJob: Job? = null

    private val searchCitiesState = MutableStateFlow<LocationCityState>(LocationCityState.Init)
    private val citiesState = MutableStateFlow<LocationCityState>(LocationCityState.Loading)
    private val searchTextState = MutableStateFlow("")
    private val currentFilterType = MutableStateFlow<FilterType>(FilterType.Location)

    private val _uiEvent = Channel<FilterEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val uiState = combine(
        citiesState,
        filterPreferencesRepository.get(),
        searchTextState,
        searchCitiesState,
        currentFilterType,
    ) { cities, filters, searchText, searchCities, filterType ->
        FilterUiState(
            searchText = searchText,
            cityListState = handleCitiesAndSearchCities(cities, searchCities),
            selectedCities = filters.cities.mapNotNull { it.toUiState() },
            selectedCategories = filters.categories,
            filterType = filterType,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STARTED_TIME),
        initialValue = FilterUiState(),
    )

    init {
        getInitialCities()
        observeSearchName()
    }

    fun onActionHandle(action: FilterAction) = when (action) {
        FilterAction.OnListEnded -> loadMoreCities()
        FilterAction.OnApplyClick -> applyFiltersStub()
        FilterAction.OnResetClick -> resetFilters()
        is FilterAction.OnFilterTypeClick -> currentFilterType.value = action.type
        is FilterAction.OnLocationClick -> toggleCity(action.item)
        is FilterAction.OnCategoryClick -> toggleCategory(action.category)
        is FilterAction.OnSearchTextChanged -> updateSearchText(action.text)
    }

    private fun getInitialCities() = viewModelScope.launch {
        Log.d(TAG, "getInitialCities")

        loadCityUseCase.execute().onSuccess { cities ->
            citiesState.value = LocationCityState.Success(cities)
        }.onFailure {
            citiesState.value = LocationCityState.Error
        }
    }

    private fun loadMoreCities() {
        if (loadMoreJob?.isActive == true) return
        loadMoreJob = viewModelScope.launch {
            loadCityUseCase.execute().onSuccess(::appendLoadedCities).onFailure {
                sendEvent(FilterEvent.SendLoadMoreError)
            }
        }
    }

    private fun appendLoadedCities(newCities: List<City>) {
        citiesState.update { current ->
            LocationCityState.Success(current.getItems() + newCities)
        }
    }

    private fun toggleCity(city: City) = updateFilters { filters ->
        val cities = if (filters.cities.any { it.id == city.id }) {
            filters.cities.filterNot { it.id == city.id }
        } else {
            filters.cities + city.toData()
        }
        filters.copy(cities = cities)
    }

    private fun toggleCategory(category: String) = updateFilters { filters ->
        val categories = if (category in filters.categories) {
            filters.categories - category
        } else {
            filters.categories + category
        }
        filters.copy(categories = categories)
    }

    private fun resetFilters() = updateFilters { FilterData() }

    private fun updateFilters(transform: (FilterData) -> FilterData) = viewModelScope.launch {
        val current = uiState.value
        val filters = FilterData(
            cities = current.selectedCities.map(City::toData),
            categories = current.selectedCategories,
        )
        filterPreferencesRepository
            .update(transform(filters))
            .onFailure { error ->
                Log.e(TAG, "Failed to update filters", error)
            }
    }

    private fun applyFiltersStub() {
        Log.d(TAG, "Filters are already persisted in DataStore")
    }

    private fun searchCitiesByName(query: String) = viewModelScope.launch {
        searchCityUseCase.execute(query).onSuccess { cities ->
            searchCitiesState.value = LocationCityState.Success(cities)
        }.onFailure {
            searchCitiesState.value = LocationCityState.Error
        }
    }

    private fun handleCitiesAndSearchCities(
        default: LocationCityState,
        search: LocationCityState,
    ): LocationCityState = if (search is LocationCityState.Init) default else search

    @OptIn(FlowPreview::class)
    private fun observeSearchName() = viewModelScope.launch {
        searchTextState
            .debounce(DEBOUNCE_SEARCH)
            .map(String::trim)
            .distinctUntilChanged()
            .collectLatest { query ->
                if (query.length < MIN_SEARCH_QUERY_LENGTH) {
                    searchCitiesState.value = LocationCityState.Init
                } else {
                    searchCitiesState.value = LocationCityState.Loading
                    searchCitiesByName(query)
                }
            }
    }

    private fun updateSearchText(text: String) {
        searchTextState.value = text
    }

    private fun sendEvent(event: FilterEvent) = viewModelScope.launch {
        _uiEvent.send(event)
    }

    private companion object {
        const val TAG = "FilterViewModel"
        const val MIN_SEARCH_QUERY_LENGTH = 2
        val DEBOUNCE_SEARCH = 300.milliseconds
        val STARTED_TIME = 5_000.milliseconds
    }
}
