package pe.edu.upc.viora.core.designsystem.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800

/** Shared, presentation-only glossary for the terms shown to producers. */
enum class AgronomicTerm(@StringRes val title: Int, @StringRes val definition: Int, @StringRes val example: Int) {
    THINNING(R.string.phase_thinning, R.string.term_thinning_definition, R.string.term_thinning_example),
    ALTERNATION(R.string.term_alternation_title, R.string.term_alternation_definition, R.string.term_alternation_example),
    DORMANCY(R.string.phase_dormancy, R.string.term_dormancy_definition, R.string.term_dormancy_example),
    BLOOM(R.string.phase_bloom, R.string.term_bloom_definition, R.string.term_bloom_example),
    FRUIT_SET(R.string.phase_fruit_set, R.string.term_fruit_set_definition, R.string.term_fruit_set_example),
    HARVEST(R.string.phase_harvest, R.string.term_harvest_definition, R.string.term_harvest_example),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgronomicTermHelp(term: AgronomicTerm, modifier: Modifier = Modifier, tint: Color = Green800) {
    var open by rememberSaveable { mutableStateOf(false) }
    // Compose expands the pointer target to the configured minimum (48 dp) without changing layout size.
    Box(
        modifier = modifier.size(30.dp).clip(RoundedCornerShape(50))
            .clickable(role = Role.Button, onClick = { open = true }),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_info),
            contentDescription = stringResource(R.string.term_help_description, stringResource(term.title)),
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
    if (open) {
        VioraBottomSheet(onDismissRequest = { open = false }) {
            Column(
                Modifier.navigationBarsPadding().fillMaxWidth().padding(horizontal = 24.dp).padding(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.term_help_eyebrow), style = MaterialTheme.typography.labelSmall, color = Green800)
                Text(stringResource(term.title), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(term.definition), style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(term.example),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Green200).padding(16.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AgronomicTermHelpPreview() { AgronomicTermHelp(AgronomicTerm.THINNING) }
