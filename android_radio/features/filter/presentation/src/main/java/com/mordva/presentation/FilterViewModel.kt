package com.mordva.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mordva.datastore.api.model.CityData
import com.mordva.datastore.api.repository.CityPreferencesRepository
import com.mordva.domain.location.domain.model.City
import com.mordva.domain.location.domain.usecase.LoadCityUseCase
import com.mordva.domain.location.domain.usecase.SearchCityUseCase
import com.mordva.presentation.state.FilterAction
import com.mordva.presentation.state.FilterEvent
import com.mordva.presentation.state.FilterType
import com.mordva.presentation.state.FilterUiState
import com.mordva.presentation.state.LocationCityState
import com.mordva.presentation.state.getItems
import com.mordva.presentation.utils.toUiState
import kotlinx.coroutines.Dispatchers
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
    cityPreferencesRepository: CityPreferencesRepository,
    private val loadCityUseCase: LoadCityUseCase,
    private val searchCityUseCase: SearchCityUseCase,
) : ViewModel() {
    private var loadMoreJob: Job? = null

    private val searchCitiesState = MutableStateFlow<LocationCityState>(LocationCityState.Init)
    private val citiesState = MutableStateFlow<LocationCityState>(LocationCityState.Loading)
    private val searchTextState = MutableStateFlow("")
    private val currentFilterType = MutableStateFlow<FilterType>(FilterType.Location)
    private val selectedCitiesState = MutableStateFlow<List<City>>(emptyList())
    private val selectedCategoriesState = MutableStateFlow<Set<String>>(emptySet())

    private val _uiEvent = Channel<FilterEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val uiState = combine(
        citiesState,
        cityPreferencesRepository.get(),
        searchTextState,
        searchCitiesState,
        currentFilterType,
        selectedCitiesState,
        selectedCategoriesState,
    ) { values ->
        val cities = values[0] as LocationCityState
        val currentCity = values[1] as CityData
        val searchText = values[2] as String
        val searchCities = values[3] as LocationCityState
        val filterType = values[4] as FilterType

        @Suppress("UNCHECKED_CAST")
        val selectedCities = values[5] as List<City>

        @Suppress("UNCHECKED_CAST")
        val selectedCategories = values[6] as Set<String>

        FilterUiState(
            searchText = searchText,
            cityListState = handleCitiesAndSearchCities(cities, searchCities),
            currentCity = currentCity.toUiState(),
            selectedCities = selectedCities,
            selectedCategories = selectedCategories,
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
            citiesState.update { LocationCityState.Success(cities) }
        }.onFailure {
            citiesState.update { LocationCityState.Error }
        }
    }

    private fun loadMoreCities() {
        if (loadMoreJob?.isActive == true) return

        Log.d(TAG, "loadCities")

        loadMoreJob = viewModelScope.launch {
            loadCityUseCase.execute().onSuccess {
                appendLoadedCities(it)
            }.onFailure {
                sendEvent(FilterEvent.SendLoadMoreError)
            }
        }
    }

    private fun appendLoadedCities(newCities: List<City>) {
        Log.d(TAG, "appendLoadedCities()")
        citiesState.update { current ->
            LocationCityState.Success(current.getItems() + newCities)
        }
    }

    private fun toggleCity(city: City) = viewModelScope.launch(Dispatchers.Default) {
        selectedCitiesState.update { selected ->
            if (selected.any { it.id == city.id }) {
                selected.filterNot { it.id == city.id }
            } else {
                selected + city
            }
        }
    }

    private fun toggleCategory(category: String) = viewModelScope.launch(Dispatchers.Default) {
        selectedCategoriesState.update { selected ->
            if (category in selected) {
                selected - category
            } else {
                selected + category
            }
        }
    }

    private fun resetFilters() {
        selectedCitiesState.value = emptyList()
        selectedCategoriesState.value = emptySet()
    }

    private fun applyFiltersStub() {
        Log.d(
            TAG,
            "applyFiltersStub(cities=${selectedCitiesState.value.map(City::id)}, " +
                    "categories=${selectedCategoriesState.value})"
        )
    }

    private fun searchCitiesByName(q: String) = viewModelScope.launch {
        Log.d(TAG, "searchCitiesByName()")

        searchCityUseCase.execute(q).onSuccess { cities ->
            searchCitiesState.value = LocationCityState.Success(cities)
        }.onFailure {
            searchCitiesState.value = LocationCityState.Error
        }
    }

    private fun handleCitiesAndSearchCities(
        default: LocationCityState,
        search: LocationCityState,
    ): LocationCityState {
        return if (searchCitiesState.value !is LocationCityState.Init) {
            search
        } else {
            default
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchName() = viewModelScope.launch {
        searchTextState
            .debounce(DEBOUNCE_SEARCH)
            .map(String::trim)
            .distinctUntilChanged()
            .collectLatest { query ->
                if (query.length < MIN_SEARCH_QUERY_LENGTH) {
                    searchCitiesState.value = LocationCityState.Init
                    return@collectLatest
                }

                searchCitiesState.value = LocationCityState.Loading
                searchCitiesByName(query)
            }
    }

    private fun updateSearchText(text: String) {
        searchTextState.value = text
    }

    private fun sendEvent(event: FilterEvent) = viewModelScope.launch {
        _uiEvent.send(event)
    }

    private companion object {
        const val TAG = "LocationViewModel"
        const val MIN_SEARCH_QUERY_LENGTH = 2
        val DEBOUNCE_SEARCH = 300.milliseconds
        val STARTED_TIME = 5_000.milliseconds
    }
}