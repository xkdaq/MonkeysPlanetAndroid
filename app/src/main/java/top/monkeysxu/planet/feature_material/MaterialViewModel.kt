package top.monkeysxu.planet.feature_material

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.feature_material.model.MaterialCategory
import top.monkeysxu.planet.feature_material.model.MaterialItem
import top.monkeysxu.planet.feature_material.model.MaterialSubject

class MaterialViewModel(
    private val repository: MaterialRepository
) : ViewModel() {

    private val _subjects = MutableLiveData<Resource<List<MaterialSubject>>>()
    val subjects: LiveData<Resource<List<MaterialSubject>>> = _subjects

    private val _categories = MutableLiveData<Resource<List<MaterialCategory>>>()
    val categories: LiveData<Resource<List<MaterialCategory>>> = _categories

    private val _materials = MutableLiveData<Resource<List<MaterialItem>>>()
    val materials: LiveData<Resource<List<MaterialItem>>> = _materials

    private var currentSubjectId: Int? = null
    private var currentCategoryId: Int? = null
    private var currentPage = 1

    fun loadSubjects() {
        viewModelScope.launch {
            _subjects.value = Resource.Loading()
            _subjects.value = repository.getSubjects()
        }
    }

    fun loadCategories(subjectId: Int? = null) {
        currentSubjectId = subjectId
        viewModelScope.launch {
            _categories.value = Resource.Loading()
            _categories.value = repository.getCategories(subjectId)
        }
    }

    fun loadMaterials(subjectId: Int? = currentSubjectId, categoryId: Int? = currentCategoryId, refresh: Boolean = true) {
        if (refresh) {
            currentPage = 1
            currentSubjectId = subjectId
            currentCategoryId = categoryId
        }
        viewModelScope.launch {
            _materials.value = Resource.Loading()
            _materials.value = repository.getMaterialList(currentSubjectId, currentCategoryId, currentPage)
        }
    }

    fun loadMore() {
        currentPage++
        viewModelScope.launch {
            val result = repository.getMaterialList(currentSubjectId, currentCategoryId, currentPage)
            if (result is Resource.Success) {
                val current = (_materials.value as? Resource.Success)?.data ?: emptyList()
                _materials.value = Resource.Success(current + result.data)
            } else {
                _materials.value = result
            }
        }
    }
}
