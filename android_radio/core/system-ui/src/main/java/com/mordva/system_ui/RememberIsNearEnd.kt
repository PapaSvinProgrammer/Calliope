import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember

/**
 * Хук для отслеживания пагинации.
 *
 * @param state Состояние LazyColumn
 * @param buffer Сколько элементов до конца списка должно остаться,
 *              чтобы вернуть true. (1 = второй с конца, 0 = самый последний)
 */
@Composable
fun rememberIsNearEnd(
    state: LazyListState,
    buffer: Int = 1,
): Boolean {
    return remember(state, buffer) {
        derivedStateOf {
            val totalItems = state.layoutInfo.totalItemsCount
            val lastVisibleIndex = state.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            totalItems > buffer && lastVisibleIndex >= totalItems - 1 - buffer
        }
    }.value
}
