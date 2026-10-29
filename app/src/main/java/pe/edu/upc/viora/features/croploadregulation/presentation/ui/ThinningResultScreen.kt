package pe.edu.upc.viora.features.croploadregulation.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.CircleIconButton
import pe.edu.upc.viora.core.designsystem.component.PrimaryPillButton
import pe.edu.upc.viora.core.designsystem.component.VioraLoadingState
import pe.edu.upc.viora.core.designsystem.component.VioraVoice
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.designsystem.theme.newsreader
import pe.edu.upc.viora.core.designsystem.theme.roboto
import pe.edu.upc.viora.core.designsystem.theme.robotoMedium
import pe.edu.upc.viora.core.presentation.formatDecimal
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CaliberProjection
import pe.edu.upc.viora.features.croploadregulation.domain.entity.LoadBalance
import pe.edu.upc.viora.features.croploadregulation.domain.entity.YieldEstimate
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.LoadState
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.YieldEstimateBasis
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.YieldEstimateStatus
import pe.edu.upc.viora.features.croploadregulation.presentation.state.CaliberCard
import pe.edu.upc.viora.features.croploadregulation.presentation.state.ResultVoice
import pe.edu.upc.viora.features.croploadregulation.presentation.state.ThinningResultUiState
import pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel.ThinningResultViewModel
import pe.edu.upc.viora.core.presentation.labelRes

/**
 * P63 · Aclareo registrado (US27/US28): the caliber expected at harvest in one of its four Figma variants, the load
 * the thinning left, the campaign yield and the way to the next campaign (Alternancia).
 */
@Composable
fun ThinningResultScreen(
    onClose: () -> Unit,
    onOpenLogbook: () -> Unit,
    onOpenAlternation: (plotName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ThinningResultViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showHowItWorks by rememberSaveable { mutableStateOf(false) }

    Box(modifier.fillMaxSize().background(Neutral100)) {
        when (val s = state) {
            ThinningResultUiState.Loading -> VioraLoadingState(modifier = Modifier.fillMaxSize())
            is ThinningResultUiState.Error -> Column(
                Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).padding(24.dp),
            ) {
                CircleIconButton(icon = R.drawable.ic_close, contentDescription = stringResource(R.string.action_close), onClick = onClose)
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(s.error.messageRes()),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Neutral700,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                PrimaryPillButton(text = stringResource(R.string.harvest_retry), onClick = viewModel::refresh)
                Spacer(Modifier.weight(1f))
            }
            is ThinningResultUiState.Content -> ResultBody(
                state = s,
                onClose = onClose,
                onOpenLogbook = onOpenLogbook,
                onOpenAlternation = { onOpenAlternation(s.plotName) },
                onHowItWorks = { showHowItWorks = true },
            )
        }
    }

    val content = state as? ThinningResultUiState.Content
    if (showHowItWorks && content != null) {
        CaliberInfoSheet(
            active = content.caliberCard,
            projection = content.execution.caliberProjection,
            load = content.execution.loadBalance,
            variety = content.variety?.let { stringResource(it.labelRes()) } ?: stringResource(R.string.thinning_this_variety),
            onDismiss = { showHowItWorks = false },
        )
    }
}

@Composable
private fun ResultBody(
    state: ThinningResultUiState.Content,
    onClose: () -> Unit,
    onOpenLogbook: () -> Unit,
    onOpenAlternation: () -> Unit,
    onHowItWorks: () -> Unit,
) {
    val variety = state.variety?.let { stringResource(it.labelRes()) } ?: stringResource(R.string.thinning_this_variety)
    val execution = state.execution
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 24.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            CircleIconButton(icon = R.drawable.ic_close, contentDescription = stringResource(R.string.action_close), onClick = onClose)
            SavedInLogbookChip(Modifier.padding(top = 8.dp))
        }
        val headline = newsreader(48.sp, 50.sp).copy(letterSpacing = (-1.2).sp)
        Column(Modifier.padding(top = 24.dp)) {
            Text(stringResource(R.string.thinning_result_headline_lead), style = headline, color = Neutral900)
            Text(stringResource(R.string.thinning_result_headline_emphasis), style = headline.copy(fontStyle = FontStyle.Italic), color = Neutral900)
        }
        VioraVoice(
            text = stringResource(
                when (state.voice) {
                    ResultVoice.GOOD_JOB -> R.string.thinning_result_voice_good
                    ResultVoice.LATE -> R.string.thinning_result_voice_late
                    ResultVoice.STILL_OVERLOADED -> R.string.thinning_result_voice_overloaded
                },
            ),
            modifier = Modifier.padding(top = 12.dp),
        )
        val projection = execution.caliberProjection
        val card = state.caliberCard
        if (card != null && projection != null) {
            CaliberHero(card, projection, variety, state, onHowItWorks, Modifier.padding(top = 16.dp))
            caliberFootnote(card, variety, execution.loadBalance)?.let {
                VioraVoice(text = it, modifier = Modifier.padding(top = 16.dp))
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 16.dp).height(252.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LoadTile(execution.loadBalance, overloaded = state.stillOverloaded, modifier = Modifier.weight(1f).fillMaxHeight())
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                YieldTile(execution.yieldEstimate, state.campaignYear, Modifier.weight(1f).fillMaxWidth())
                NextCampaignTile(state.campaignYear + 1, onOpenAlternation, Modifier.weight(1f).fillMaxWidth())
            }
        }
        PrimaryPillButton(
            text = stringResource(R.string.thinning_result_back_to_plan),
            onClick = onClose,
            modifier = Modifier.padding(top = 36.dp),
            trailingIcon = R.drawable.ic_arrow_forward,
        )
        Text(
            stringResource(R.string.thinning_result_view_logbook),
            style = robotoMedium(14.sp, 20.sp),
            color = Green800,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).clip(CircleShape)
                .clickable(role = Role.Button, onClick = onOpenLogbook).padding(vertical = 6.dp),
        )
    }
}

/** "Guardado en Bitácora" (Figma "En Bitácora"): a statement, not a button; the link at the bottom opens it. */
@Composable
private fun SavedInLogbookChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clip(CircleShape).background(Green200).padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(Neutral0), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_menu_book), contentDescription = null, tint = Green800, modifier = Modifier.size(24.dp))
        }
        Text(stringResource(R.string.thinning_result_saved), style = robotoMedium(13.sp, 18.sp), color = Green800)
    }
}

/** The look of each Figma variant of "Calibre · héroe". */
private data class HeroLook(val background: Color, val accent: Color, val unknown: Color?, val dot: Color)

private fun heroLook(card: CaliberCard): HeroLook = when (card) {
    CaliberCard.ESTIMATED -> HeroLook(Harvest100, Harvest800, null, Green800)
    CaliberCard.LEARNING -> HeroLook(Harvest100, Harvest800, Harvest800, Green800)
    CaliberCard.LATE -> HeroLook(Terracotta100, Terracotta700, Terracotta500, Terracotta700)
    CaliberCard.OUT_OF_RANGE -> HeroLook(Neutral0, Neutral700, Neutral500, Green800)
}

@Composable
private fun CaliberHero(
    card: CaliberCard,
    projection: CaliberProjection,
    variety: String,
    state: ThinningResultUiState.Content,
    onHowItWorks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val look = heroLook(card)
    val locale = LocalConfiguration.current.locales[0]
    Column(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(look.background)
            .padding(start = 22.dp, end = 22.dp, top = 21.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(Green800))
            Text(
                stringResource(R.string.thinning_result_caliber_eyebrow),
                style = robotoMedium(11.sp, 16.sp).copy(letterSpacing = 1.32.sp),
                color = look.accent,
                modifier = Modifier.padding(start = 8.dp).weight(1f),
            )
            Row(
                modifier = Modifier.clip(CircleShape).background(Neutral0).clickable(role = Role.Button, onClick = onHowItWorks)
                    .padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_info), contentDescription = null, tint = Neutral900, modifier = Modifier.size(14.dp))
                Text(stringResource(R.string.thinning_result_how_title), style = robotoMedium(11.sp, 14.sp), color = Green800)
            }
        }
        val (figure, caption) = when (card) {
            CaliberCard.ESTIMATED -> projection.mostLikelySizeGrade.orEmpty() to stringResource(R.string.thinning_result_caliber_caption)
            CaliberCard.LEARNING -> stringResource(
                R.string.thinning_yield_history_value,
                projection.calibrationObservations,
                projection.requiredObservations,
            ) to stringResource(R.string.thinning_result_learning_caption, variety)
            CaliberCard.LATE -> stringResource(R.string.thinning_result_not_estimated) to stringResource(R.string.thinning_result_late_caption)
            CaliberCard.OUT_OF_RANGE -> stringResource(R.string.thinning_result_not_estimated) to stringResource(R.string.thinning_result_range_caption)
        }
        val figureSize = if (card == CaliberCard.LATE || card == CaliberCard.OUT_OF_RANGE) 52.sp else 64.sp
        Text(
            figure,
            style = newsreader(figureSize, 68.sp).copy(letterSpacing = figureSize * -0.03f),
            color = Neutral900,
            textAlign = TextAlign.Center,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 36.sp, maxFontSize = figureSize),
            modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
        )
        Text(
            caption,
            style = newsreader(18.sp, 24.sp).copy(fontStyle = FontStyle.Italic),
            color = look.accent,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        val arrowLabel = when (card) {
            CaliberCard.ESTIMATED -> projection.confidenceLevel?.let {
                stringResource(R.string.thinning_result_range_label, (it * 100).roundToInt())
            }.orEmpty()
            CaliberCard.LEARNING -> stringResource(R.string.thinning_result_learning_label)
            CaliberCard.LATE, CaliberCard.OUT_OF_RANGE -> stringResource(R.string.thinning_result_no_data_label)
        }
        OliveRangeIllustration(
            arrowLabel = arrowLabel,
            labelColor = look.accent,
            unknownColor = look.unknown,
            modifier = Modifier.padding(top = 12.dp),
        )
        when (card) {
            CaliberCard.ESTIMATED -> {
                RangeEnds(projection)
                Spacer(Modifier.height(16.dp))
                FactChip(stringResource(R.string.thinning_result_harvests_fact, projection.calibrationObservations, variety), look.dot, Green800)
            }
            CaliberCard.LEARNING -> {
                HarvestSegments(projection.calibrationObservations, projection.requiredObservations, Modifier.padding(top = 12.dp))
                Spacer(Modifier.height(16.dp))
                FactChip(stringResource(R.string.thinning_result_learning_fact), look.dot, Green800)
            }
            CaliberCard.LATE -> {
                val day = DateTimeFormatter.ofPattern(stringResource(R.string.thinning_pattern_short), locale)
                    .format(state.execution.executedOn).replace(".", "")
                FactChip(stringResource(R.string.thinning_result_late_fact, day), look.dot, Terracotta700)
            }
            CaliberCard.OUT_OF_RANGE -> {
                val min = projection.calibratedLoadMin
                val max = projection.calibratedLoadMax
                if (min != null && max != null) {
                    FactChip(
                        stringResource(R.string.thinning_result_range_fact, variety, formatDecimal(min, 2), formatDecimal(max, 2)),
                        look.dot,
                        Green800,
                    )
                }
            }
        }
    }
}

/** "121/140 · fruto más chico" under the small olive and "91/100 · fruto más grande" under the big one. */
@Composable
private fun RangeEnds(projection: CaliberProjection) {
    BoxWithConstraints(Modifier.fillMaxWidth().height(44.dp)) {
        val scale = maxWidth / 310.dp
        RangeEnd(
            value = projection.smallFruitSizeGrade.orEmpty(),
            caption = stringResource(R.string.thinning_result_small_fruit),
            valueColor = Neutral700,
            captionColor = Harvest800,
            modifier = Modifier.width(110.dp).offset(x = 46.dp * scale - 55.dp),
        )
        RangeEnd(
            value = projection.bigFruitSizeGrade.orEmpty(),
            caption = stringResource(R.string.thinning_result_big_fruit),
            valueColor = Neutral900,
            captionColor = Green800,
            modifier = Modifier.width(110.dp).offset(x = 240.dp * scale - 55.dp),
        )
    }
}

@Composable
private fun RangeEnd(value: String, caption: String, valueColor: Color, captionColor: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = newsreader(26.sp, 30.sp).copy(letterSpacing = (-0.26).sp), color = valueColor, maxLines = 1)
        Text(caption, style = robotoMedium(11.sp, 14.sp), color = captionColor, maxLines = 1)
    }
}

/** "Cosechas reunidas · 3 de 8": one 30 × 8 dp segment per harvest the model needs. */
@Composable
private fun HarvestSegments(collected: Int, required: Int, modifier: Modifier = Modifier) {
    if (required <= 0) return
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(required) { index ->
            Box(
                Modifier.weight(1f, fill = false).width(30.dp).height(8.dp).clip(RoundedCornerShape(4.dp))
                    .background(if (index < collected) Green800 else Neutral0),
            )
        }
    }
}

/** A white pill with a 7 dp dot: the evidence behind the card ("Con 14 cosechas reales de Sevillana"). */
@Composable
private fun FactChip(text: String, dot: Color, textColor: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clip(CircleShape).background(Neutral0).padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(dot))
        // Phones narrower than Figma's 402 dp shrink the fact instead of wrapping it.
        Text(
            text,
            style = robotoMedium(12.sp, 14.sp),
            color = textColor,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = 12.sp),
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

/** The Viora line under the card when there is no figure: what is missing for one. */
@Composable
private fun caliberFootnote(card: CaliberCard, variety: String, load: LoadBalance?): String? =
    when (card) {
        CaliberCard.ESTIMATED -> null
        CaliberCard.LEARNING -> stringResource(R.string.thinning_result_learning_voice, variety)
        CaliberCard.LATE -> stringResource(R.string.thinning_result_late_voice)
        CaliberCard.OUT_OF_RANGE -> load?.let { stringResource(R.string.thinning_result_range_voice, formatDecimal(it.residualFruitsPerShoot, 2)) }
    }

/**
 * "Tras el aclareo": one column on an 18 dp margin. The label and the before/after bars share the top row, the residual
 * fruits per shoot sit low with their unit, and the load state closes the tile as a full-width chip.
 */
@Composable
private fun LoadTile(load: LoadBalance?, overloaded: Boolean, modifier: Modifier = Modifier) {
    val background = if (overloaded) Terracotta100 else Green900
    val content = if (overloaded) Terracotta700 else Neutral50
    Column(modifier.clip(RoundedCornerShape(28.dp)).background(background).padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Text(
                stringResource(R.string.thinning_result_after),
                style = robotoMedium(12.sp, 16.sp),
                color = content,
                modifier = Modifier.weight(1f).padding(top = 2.dp),
            )
            if (load != null && load.beforeFruitsPerShoot > 0) {
                val afterShare = (load.residualFruitsPerShoot / load.beforeFruitsPerShoot).toFloat().coerceIn(0.08f, 1f)
                Row(Modifier.height(48.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                    Box(Modifier.width(14.dp).fillMaxHeight().clip(RoundedCornerShape(7.dp)).background(if (overloaded) Terracotta500 else Neutral50))
                    Box(Modifier.width(14.dp).fillMaxHeight(afterShare).clip(RoundedCornerShape(7.dp)).background(if (overloaded) Terracotta500 else Harvest300))
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            load?.let { formatDecimal(it.residualFruitsPerShoot, 2) } ?: stringResource(R.string.thinning_no_value),
            style = newsreader(56.sp, 56.sp).copy(letterSpacing = (-1.12).sp),
            color = content,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 40.sp, maxFontSize = 56.sp),
        )
        Text(
            stringResource(R.string.thinning_result_fruits_per_shoot),
            style = robotoMedium(13.sp, 18.sp),
            color = if (overloaded) Terracotta700 else Green200,
            modifier = Modifier.padding(top = 4.dp),
        )
        load?.let {
            val stateLabel = stringResource(
                when (it.loadState) {
                    LoadState.BALANCED -> R.string.thinning_result_state_balanced
                    LoadState.MODERATE_OVERLOAD -> R.string.thinning_result_state_moderate
                    LoadState.SEVERE_OVERLOAD -> R.string.thinning_result_state_severe
                },
            )
            FactChip(
                text = stringResource(
                    R.string.thinning_result_state_fact,
                    stateLabel,
                    formatDecimal(it.beforeFruitsPerShoot, 2),
                    formatDecimal(it.residualFruitsPerShoot, 2),
                ),
                dot = if (overloaded) Terracotta500 else Green800,
                textColor = if (overloaded) Terracotta700 else Green800,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            )
        }
    }
}

/** "8,2 t/ha estimadas · campaña 2026", the ceiling without caliber, or what history is missing. */
@Composable
private fun YieldTile(estimate: YieldEstimate?, campaignYear: Int, modifier: Modifier = Modifier) {
    val dash = stringResource(R.string.thinning_no_value)
    val (value, caption) = when {
        estimate == null || estimate.status == YieldEstimateStatus.LOAD_UNKNOWN || estimate.status == YieldEstimateStatus.AREA_UNKNOWN ->
            dash to stringResource(R.string.thinning_yield_estimated)
        estimate.status == YieldEstimateStatus.INSUFFICIENT_HISTORY ->
            stringResource(R.string.thinning_yield_history_value, estimate.harvestCampaigns, estimate.requiredHarvestCampaigns) to
                stringResource(R.string.thinning_yield_history_caption)
        estimate.basis == YieldEstimateBasis.FRUIT_COUNT_ONLY ->
            stringResource(R.string.thinning_yield_ceiling_value, estimate.estimatedKgPerHa?.let(::tons) ?: dash) to
                stringResource(R.string.thinning_yield_ceiling_caption)
        else -> (estimate.estimatedKgPerHa?.let(::tons) ?: dash) to stringResource(R.string.thinning_yield_estimated)
    }
    Column(modifier.clip(RoundedCornerShape(28.dp)).background(Neutral0).padding(start = 18.dp, end = 12.dp, top = 14.dp)) {
        Text(
            value,
            style = newsreader(52.sp, 52.sp).copy(letterSpacing = (-0.52).sp),
            color = Neutral900,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 32.sp, maxFontSize = 52.sp),
        )
        Text(
            caption,
            style = robotoMedium(13.sp, 16.sp),
            color = Neutral700,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 13.sp),
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(stringResource(R.string.thinning_result_campaign, campaignYear), style = roboto(12.sp, 14.sp), color = Neutral700, modifier = Modifier.padding(top = 2.dp))
    }
}

/** "2027 · año más estable · ver mi alternancia", with the round arrow that opens Alternancia (P40). */
@Composable
private fun NextCampaignTile(year: Int, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(28.dp)).background(Green200).clickable(role = Role.Button, onClick = onOpen)) {
        Column(Modifier.padding(start = 18.dp, top = 14.dp)) {
            // The round arrow sits at the top end: the year shrinks before running under it.
            Text(
                year.toString(),
                style = newsreader(40.sp, 40.sp).copy(letterSpacing = (-0.4).sp),
                color = Neutral900,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 28.sp, maxFontSize = 40.sp),
                modifier = Modifier.padding(end = 60.dp),
            )
            Text(stringResource(R.string.thinning_result_next_year), style = robotoMedium(12.sp, 16.sp), color = Green900, modifier = Modifier.padding(top = 10.dp))
            Text(stringResource(R.string.thinning_result_view_alternation), style = roboto(12.sp, 14.sp), color = Green900, modifier = Modifier.padding(top = 2.dp))
        }
        Box(
            Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 18.dp).size(36.dp).clip(CircleShape).background(Neutral0),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_arrow_forward),
                contentDescription = null,
                tint = Terracotta700,
                modifier = Modifier.size(20.dp).rotate(-45f),
            )
        }
    }
}
