package pe.edu.upc.viora.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.PillShape
import pe.edu.upc.viora.core.designsystem.theme.ShadowTint
import pe.edu.upc.viora.core.designsystem.theme.Spacing
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme

/** One destination of [VioraTabBar]. [label] is only used for accessibility. */
data class TabBarItem(@DrawableRes val icon: Int, val label: String)

object VioraTabBarDefaults {
    val BarHeight = 72.dp
    private val BottomMargin = Spacing.sm

    /** Bottom padding every tab screen must reserve so the floating bar never hides content. */
    val ContentBottomPadding = BarHeight + BottomMargin + Spacing.md

    internal val MaxBarWidth = 270.dp
    internal val BubbleSize = 60.dp
    internal val BarInnerPadding = 8.dp
    internal val Gap = Spacing.sm
}

/**
 * Floating navigation pill with an ivory bubble that springs to the selected destination,
 * plus the separate yellow "+" action (Figma: Editorial/Tapbar flotante, state "Reposo").
 * Designed for 4 destinations; max width 354 dp, centred on wider screens.
 */
@Composable
fun VioraTabBar(
    items: List<TabBarItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(VioraTabBarDefaults.Gap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavigationPill(
            items = items,
            selectedIndex = selectedIndex,
            onItemClick = onItemClick,
            modifier = Modifier.weight(1f, fill = false),
        )
        ActionButton(label = actionLabel, onClick = onActionClick)
    }
}

@Composable
private fun NavigationPill(
    items: List<TabBarItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .widthIn(max = VioraTabBarDefaults.MaxBarWidth)
            .height(VioraTabBarDefaults.BarHeight)
            .shadow(
                elevation = 16.dp,
                shape = PillShape,
                ambientColor = ShadowTint,
                spotColor = ShadowTint,
            )
            .clip(PillShape)
            .background(Green900),
    ) {
        val cellWidth: Dp = (maxWidth - VioraTabBarDefaults.BarInnerPadding * 2) / items.size
        val bubbleSize = minOf(cellWidth, VioraTabBarDefaults.BubbleSize)
        val bubbleOffset by animateDpAsState(
            targetValue = VioraTabBarDefaults.BarInnerPadding +
                cellWidth * selectedIndex + (cellWidth - bubbleSize) / 2,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
            label = "tabBubbleOffset",
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(x = bubbleOffset.roundToPx(), y = 0) }
                .align(Alignment.CenterStart)
                .size(bubbleSize)
                .clip(PillShape)
                .background(Neutral50),
        )

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = VioraTabBarDefaults.BarInnerPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                val tint by animateColorAsState(
                    targetValue = if (selected) Neutral900 else Neutral50,
                    label = "tabIconTint",
                )
                Box(
                    modifier = Modifier
                        .size(width = cellWidth, height = VioraTabBarDefaults.BarHeight)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onItemClick(index) }),
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

@Composable
private fun ActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(VioraTabBarDefaults.BarHeight)
            .shadow(
                elevation = 12.dp,
                shape = PillShape,
                ambientColor = ShadowTint,
                spotColor = ShadowTint,
            )
            .clip(PillShape)
            .background(Harvest300)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = label,
            tint = Neutral900,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9F6F1, widthDp = 412)
@Composable
private fun VioraTabBarPreview() {
    VioraTheme {
        VioraTabBar(
            items = listOf(
                TabBarItem(R.drawable.ic_home, "Inicio"),
                TabBarItem(R.drawable.ic_map, "Lotes"),
                TabBarItem(R.drawable.ic_calendar_month, "Plan"),
                TabBarItem(R.drawable.ic_menu_book, "Bitácora"),
            ),
            selectedIndex = 1,
            onItemClick = {},
            actionLabel = "¿Qué vas a registrar?",
            onActionClick = {},
            modifier = Modifier.padding(vertical = Spacing.md),
        )
    }
}
