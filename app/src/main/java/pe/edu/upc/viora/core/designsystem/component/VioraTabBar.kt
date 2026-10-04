package pe.edu.upc.viora.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.lerp as lerpColor
import kotlin.math.abs
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green100
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest700
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.PillShape
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.designsystem.theme.ShadowTint
import pe.edu.upc.viora.core.designsystem.theme.Spacing
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta600
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme

/** One destination of [VioraTabBar]. [label] is only used for accessibility. */
data class TabBarItem(@DrawableRes val icon: Int, val label: String)

/** One row of the "what will you record?" menu. [iconTint] is drawn on top of [iconBackground]. */
data class TabBarAction(
    @DrawableRes val icon: Int,
    val title: String,
    val subtitle: String,
    val iconBackground: Color,
    val iconTint: Color,
)

/** Figma "Estado" of Editorial/Tapbar flotante. */
enum class TabBarMode {
    /** Full pill with the four destinations. */
    Rest,

    /** Collapsed to a circle with the active destination (Figma state; the app does not use it). */
    Compact,

    /** "+" tapped: the circle sits at the start, the button stretches and the menu grows above it. */
    Actions,
}

object VioraTabBarDefaults {
    val BarHeight = 72.dp
    private val BottomMargin = Spacing.sm

    /** Bottom padding every tab screen must reserve so the floating bar never hides content. */
    val ContentBottomPadding = BarHeight + BottomMargin + Spacing.md

    internal val MaxWidth = 354.dp
    internal val BubbleSize = 60.dp
    internal val BarInnerPadding = 8.dp
    internal val CircleInnerPadding = 6.dp
    internal val Gap = Spacing.sm
    internal val MenuHeight = 240.dp
    internal val MenuInset = 8.dp
    internal val OptionHeight = 56.dp

    /** The menu grows above the bar, so the component is this tall and the extra part is empty. */
    internal val ContainerHeight = BarHeight + Gap + MenuHeight
}

/**
 * Floating navigation pill with an ivory bubble that springs to the selected destination,
 * plus the separate yellow "+" action (Figma: Editorial/Tapbar flotante). [mode] drives the
 * three Figma states; the caller decides when to switch (scroll, taps, back).
 * Designed for 4 destinations; max width 354 dp, centred on wider screens.
 */
@Composable
fun VioraTabBar(
    items: List<TabBarItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    actionLabel: String,
    actions: List<TabBarAction>,
    mode: TabBarMode,
    onActionClick: () -> Unit,
    onActionSelected: (Int) -> Unit,
    onCollapsedBarClick: () -> Unit,
    modifier: Modifier = Modifier,
    closeLabel: String = actionLabel,
    barRowModifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md),
        contentAlignment = Alignment.BottomCenter,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = VioraTabBarDefaults.MaxWidth)
                .fillMaxWidth()
                .height(VioraTabBarDefaults.ContainerHeight),
        ) {
            val barHeight = VioraTabBarDefaults.BarHeight
            val gap = VioraTabBarDefaults.Gap
            val restWidth = maxWidth - barHeight - gap

            // Invisible marker with the size of the bar row, so callers can locate it (e.g. the tour).
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(barHeight)
                    .then(barRowModifier),
            )

            val layoutSpec = spring<Dp>(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
            val barWidth by animateDpAsState(if (mode == TabBarMode.Rest) restWidth else barHeight, layoutSpec, label = "tabBarWidth")
            val barX by animateDpAsState(
                targetValue = if (mode == TabBarMode.Compact) maxWidth - barHeight * 2 - gap else 0.dp,
                animationSpec = layoutSpec,
                label = "tabBarX",
            )
            val actionX by animateDpAsState(
                targetValue = if (mode == TabBarMode.Actions) barHeight + gap else maxWidth - barHeight,
                animationSpec = layoutSpec,
                label = "tabActionX",
            )
            val actionWidth by animateDpAsState(
                targetValue = if (mode == TabBarMode.Actions) maxWidth - barHeight - gap else barHeight,
                animationSpec = layoutSpec,
                label = "tabActionWidth",
            )
            val collapse by animateFloatAsState(
                targetValue = if (mode == TabBarMode.Rest) 0f else 1f,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                label = "tabCollapse",
            )
            val menu by animateFloatAsState(
                targetValue = if (mode == TabBarMode.Actions) 1f else 0f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                label = "tabMenu",
            )

            NavigationPill(
                items = items,
                selectedIndex = selectedIndex,
                onItemClick = onItemClick,
                restWidth = restWidth,
                width = barWidth,
                collapse = collapse,
                enabled = mode == TabBarMode.Rest,
                onCollapsedClick = onCollapsedBarClick,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = barX),
            )

            ActionButton(
                label = actionLabel,
                closeLabel = closeLabel,
                open = mode == TabBarMode.Actions,
                menuProgress = menu,
                width = actionWidth,
                onClick = onActionClick,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = actionX),
            )

            if (menu > 0f) {
                ActionsMenu(
                    actions = actions,
                    progress = menu,
                    fullWidth = maxWidth - barHeight - gap,
                    startX = maxWidth - barHeight,
                    endX = barHeight + gap,
                    onSelected = onActionSelected,
                    modifier = Modifier.align(Alignment.BottomStart),
                )
            }
        }
    }
}

@Composable
private fun NavigationPill(
    items: List<TabBarItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    restWidth: Dp,
    width: Dp,
    collapse: Float,
    enabled: Boolean,
    onCollapsedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val barHeight = VioraTabBarDefaults.BarHeight
    val inner = VioraTabBarDefaults.BarInnerPadding
    val circleInner = VioraTabBarDefaults.CircleInnerPadding
    val cellWidth: Dp = (restWidth - inner * 2) / items.size
    val restBubbleSize = minOf(cellWidth, VioraTabBarDefaults.BubbleSize)
    val restBubbleX by animateDpAsState(
        targetValue = inner + cellWidth * selectedIndex + (cellWidth - restBubbleSize) / 2,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "tabBubbleOffset",
    )
    val bubbleSize = lerp(restBubbleSize, VioraTabBarDefaults.BubbleSize, collapse)
    val bubbleX = lerp(restBubbleX, circleInner, collapse)
    // Slides the icon row so the active destination ends up inside the circle's bubble.
    val activeCenter = inner + cellWidth * selectedIndex + cellWidth / 2
    val rowShift = (circleInner + VioraTabBarDefaults.BubbleSize / 2 - activeCenter) * collapse

    Box(
        modifier = modifier
            .size(width = width, height = barHeight)
            .shadow(
                elevation = 16.dp,
                shape = PillShape,
                ambientColor = ShadowTint,
                spotColor = ShadowTint,
            )
            .clip(PillShape)
            .background(Green900)
            .clickable(enabled = !enabled, role = Role.Button, onClick = onCollapsedClick),
    ) {
        // Content keeps its full-pill layout; the bar only reveals part of it while it shrinks.
        Box(
            modifier = Modifier
                .wrapContentSize(Alignment.TopStart, unbounded = true)
                .size(width = restWidth, height = barHeight),
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = bubbleX.roundToPx(), y = 0) }
                    .align(Alignment.CenterStart)
                    .size(bubbleSize)
                    .clip(PillShape)
                    .background(Neutral50),
            )

            Row(
                modifier = Modifier
                    .offset { IntOffset(x = (inner + rowShift).roundToPx(), y = 0) }
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    val selected = index == selectedIndex
                    // How much of the bubble sits under this cell: the icon darkens exactly as the
                    // bubble arrives, instead of on its own timer.
                    val cellCenter = inner + cellWidth * index + cellWidth / 2
                    val bubbleCenter = restBubbleX + restBubbleSize / 2
                    val coverage = (1f - abs((cellCenter - bubbleCenter) / cellWidth)).coerceIn(0f, 1f)
                    val tint = lerpColor(Neutral50, Neutral900, coverage)
                    Box(
                        modifier = Modifier
                            .size(width = cellWidth, height = barHeight)
                            .alpha(if (selected) 1f else 1f - collapse)
                            .selectable(
                                selected = selected,
                                enabled = enabled,
                                role = Role.Tab,
                                onClick = { onItemClick(index) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = item.label,
                            tint = tint,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    closeLabel: String,
    open: Boolean,
    menuProgress: Float,
    width: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val barHeight = VioraTabBarDefaults.BarHeight
    Box(
        modifier = modifier
            .size(width = width, height = barHeight)
            .shadow(
                elevation = 12.dp,
                shape = PillShape,
                ambientColor = ShadowTint,
                spotColor = ShadowTint,
            )
            .clip(PillShape)
            .background(Harvest300)
            .clickable(role = Role.Button, onClickLabel = if (open) closeLabel else label, onClick = onClick),
    ) {
        Text(
            text = label,
            style = OptionTitleStyle,
            color = Neutral900,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = Spacing.lg)
                .alpha(menuProgress),
        )
        // The "+" stays centred in the end square of the button and turns into a "×".
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(barHeight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = if (open) closeLabel else label,
                tint = Neutral900,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(45f * menuProgress),
            )
        }
    }
}

@Composable
private fun ActionsMenu(
    actions: List<TabBarAction>,
    progress: Float,
    fullWidth: Dp,
    startX: Dp,
    endX: Dp,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val barHeight = VioraTabBarDefaults.BarHeight
    val menuHeight = VioraTabBarDefaults.MenuHeight
    // Grows from the 72 dp circle of the button: its bottom rises to 12 dp above the bar.
    val width = lerp(barHeight, fullWidth, progress)
    val height = lerp(barHeight, menuHeight, progress)
    val x = lerp(startX, endX, progress)
    val y = -lerp(0.dp, VioraTabBarDefaults.Gap + barHeight, progress)
    val shape = RoundedCornerShape(lerp(36.dp, 28.dp, progress))

    Box(
        modifier = modifier
            .offset(x = x, y = y)
            .size(width = width, height = height)
            .alpha((progress * 2f).coerceAtMost(1f))
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = ShadowTint,
                spotColor = ShadowTint,
            )
            .clip(shape)
            .background(Neutral0),
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize(Alignment.TopStart, unbounded = true)
                .width(fullWidth)
                .padding(VioraTabBarDefaults.MenuInset)
                .alpha(progress),
        ) {
            actions.forEachIndexed { index, action ->
                ActionOption(action = action, onClick = { onSelected(index) })
            }
        }
    }
}

@Composable
private fun ActionOption(action: TabBarAction, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(VioraTabBarDefaults.OptionHeight)
            .clip(RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(PillShape)
                .background(action.iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(action.icon),
                contentDescription = null,
                tint = action.iconTint,
                modifier = Modifier.size(20.dp),
            )
        }
        Column {
            Text(text = action.title, style = OptionTitleStyle, color = Neutral900, maxLines = 1)
            Text(text = action.subtitle, style = OptionSubtitleStyle, color = Neutral600, maxLines = 1)
        }
    }
}

private val OptionTitleStyle = TextStyle(
    fontFamily = RobotoFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 20.sp,
)

private val OptionSubtitleStyle = TextStyle(
    fontFamily = RobotoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

/** Icon colours of the four record options, straight from the Figma menu. */
object VioraTabBarActionColors {
    val SamplingBackground = Harvest100
    val SamplingTint = Harvest700
    val ThinningBackground = Terracotta100
    val ThinningTint = Terracotta600
    val HarvestBackground = Green100
    val HarvestTint = Green800
    val NoteBackground = Neutral100
    val NoteTint = Neutral700
}

private val PreviewItems = listOf(
    TabBarItem(R.drawable.ic_home, "Inicio"),
    TabBarItem(R.drawable.ic_map, "Lotes"),
    TabBarItem(R.drawable.ic_calendar_month, "Plan"),
    TabBarItem(R.drawable.ic_menu_book, "Bitácora"),
)

private val PreviewActions = listOf(
    TabBarAction(
        R.drawable.ic_nutrition, "Muestreo de cuajado", "A pie de árbol · funciona sin señal",
        VioraTabBarActionColors.SamplingBackground, VioraTabBarActionColors.SamplingTint,
    ),
    TabBarAction(
        R.drawable.ic_content_cut, "Aclareo realizado", "Confirma lo ejecutado en el lote",
        VioraTabBarActionColors.ThinningBackground, VioraTabBarActionColors.ThinningTint,
    ),
    TabBarAction(
        R.drawable.ic_inventory, "Cosecha", "Kilos por lote",
        VioraTabBarActionColors.HarvestBackground, VioraTabBarActionColors.HarvestTint,
    ),
    TabBarAction(
        R.drawable.ic_edit_note, "Nota de bitácora", "Observación libre o foto",
        VioraTabBarActionColors.NoteBackground, VioraTabBarActionColors.NoteTint,
    ),
)

@Composable
private fun TabBarPreview(mode: TabBarMode) {
    VioraTheme {
        VioraTabBar(
            items = PreviewItems,
            selectedIndex = 1,
            onItemClick = {},
            actionLabel = "¿Qué vas a registrar?",
            actions = PreviewActions,
            mode = mode,
            onActionClick = {},
            onActionSelected = {},
            onCollapsedBarClick = {},
            modifier = Modifier.padding(vertical = Spacing.md),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9F6F1, widthDp = 412)
@Composable
private fun VioraTabBarRestPreview() = TabBarPreview(TabBarMode.Rest)

@Preview(showBackground = true, backgroundColor = 0xFFF9F6F1, widthDp = 412)
@Composable
private fun VioraTabBarCompactPreview() = TabBarPreview(TabBarMode.Compact)

@Preview(showBackground = true, backgroundColor = 0xFFF9F6F1, widthDp = 412)
@Composable
private fun VioraTabBarActionsPreview() = TabBarPreview(TabBarMode.Actions)
