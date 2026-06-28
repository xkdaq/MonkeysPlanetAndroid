package top.monkeysxu.planet.feature_exam

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.feature_exam.model.Bank
import top.monkeysxu.planet.feature_exam.model.Category
import top.monkeysxu.planet.feature_exam.model.Question

class ExamViewModel(
    private val repository: ExamRepository
) : ViewModel() {

    private val _banks = MutableLiveData<Resource<List<Bank>>>()
    val banks: LiveData<Resource<List<Bank>>> = _banks

    private val _categories = MutableLiveData<Resource<List<Category>>>()
    val categories: LiveData<Resource<List<Category>>> = _categories

    private val _practiceStart = MutableLiveData<Resource<List<Question>>>()
    val practiceStart: LiveData<Resource<List<Question>>> = _practiceStart

    fun loadBanks() {
        viewModelScope.launch {
            _banks.value = Resource.Loading()
            _banks.value = repository.getBanks()
        }
    }

    fun loadCategories(bankId: Int) {
        viewModelScope.launch {
            _categories.value = Resource.Loading()
            _categories.value = repository.getCategoryTree(bankId)
        }
    }

    fun startPractice(bankId: Int, categoryId: Int?, type: Int) {
        viewModelScope.launch {
            _practiceStart.value = Resource.Loading()
            _practiceStart.value = repository.startPractice(bankId, categoryId, type)
        }
    }
}
