package ch.rmy.android.http_shortcuts.activities.sync.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import ch.rmy.android.http_shortcuts.R

@Composable
fun PasswordProtection(
    modifier: Modifier,
    label: String,
    password: String,
    onPasswordChanged: (String) -> Unit,
) {
    TextField(
        modifier = modifier,
        label = {
            Text(label)
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false,
        ),
        value = password,
        onValueChange = onPasswordChanged,
        visualTransformation = PasswordVisualTransformation(),
        maxLines = 2,
    )
}
