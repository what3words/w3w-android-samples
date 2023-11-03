package com.what3words.compose.sample.ui.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.what3words.compose.sample.R

class RadioGroupState<T>(val items: List<T>, selectedItemIndex: Int, val label: (T) -> String) {
    var selected: T? by mutableStateOf(if (items.isNotEmpty()) items[selectedItemIndex] else null)
}

@Composable
fun <T> RadioGroup(
    state: RadioGroupState<T>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val ripple = rememberRipple()
        val interactionSource = remember {
            MutableInteractionSource()
        }
        state.items.forEach { item: T ->
            RadioGroupItem(
                selected = item == state.selected,
                label = state.label(item),
                modifier = Modifier.selectable(
                    selected = item == state.selected,
                    role = Role.RadioButton,
                    interactionSource = interactionSource,
                    indication = ripple,
                    enabled = true,
                    onClick = { state.selected = item }
                ),
                onClick = { state.selected = item }
            )
        }
    }
}

@Composable
private fun RadioGroupItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val interactionSource = remember {
            MutableInteractionSource()
        }
        RadioButton(
            modifier = Modifier.size(size = dimensionResource(id = R.dimen.normal_200)),
            selected = selected, onClick = onClick, interactionSource = interactionSource
        )
        Text(
            text = label,
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}