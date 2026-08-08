package com.mordva.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.mordva.filter.presentation.R
import com.mordva.system_ui.Resources

@Composable
internal fun FilterSearchBar(
    searchBarState: SearchBarState,
    textFieldState: TextFieldState,
    onVoiceInputClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchBar(
        state = searchBarState,
        inputField = {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                placeholder = { Text(text = stringResource(R.string.title_search_placeholder)) },
                onSearch = {},
                leadingIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onVoiceInputClick) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_mic),
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        modifier = modifier
            .padding(horizontal = Resources.Dimens.DP16)
            .fillMaxWidth(),
    )
}
