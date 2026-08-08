package com.mordva.system_ui.sheet

/**
 * Глобальный реестр всех BottomSheet-ов приложения.
 * Чтобы добавить новый шит — достаточно добавить новый объект/класс сюда
 * и одну ветку `when` в MainActivity.
 */
sealed interface AppSheet {
    data object Location : AppSheet
}
