package yokai.domain.category.interactor

import yokai.domain.category.CategoryRepository

class GetCategories(
    private val categoryRepository: CategoryRepository,
) {
    suspend fun await() = categoryRepository.getAll()
    fun subscribe() = categoryRepository.getAllAsFlow()
}
