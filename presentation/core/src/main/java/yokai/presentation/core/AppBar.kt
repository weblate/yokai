package yokai.presentation.core

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isFinite
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.util.fastFirst
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Composable replacement for [eu.kanade.tachiyomi.ui.base.ExpandedAppBarLayout]
 *
 * Copied from [androidx.compose.material3.LargeTopAppBar], modified to mimic J2K's
 * [eu.kanade.tachiyomi.ui.base.ExpandedAppBarLayout] behaviors
 */
@Composable
fun ExpandedAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.largeTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TwoRowsTopAppBar(
        title = title,
        titleTextStyle = MaterialTheme.typography.headlineMedium,
        smallTitleTextStyle = MaterialTheme.typography.titleLarge,
        titleBottomPadding = LargeTitleBottomPadding,
        smallTitle = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        collapsedHeight = CollapsedContainerHeight,
        expandedHeight = ExpandedContainerHeight,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun TwoRowsTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    titleTextStyle: TextStyle,
    titleBottomPadding: Dp,
    smallTitle: @Composable () -> Unit,
    smallTitleTextStyle: TextStyle,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    collapsedHeight: Dp,
    expandedHeight: Dp,
    windowInsets: WindowInsets,
    colors: TopAppBarColors,
    scrollBehavior: TopAppBarScrollBehavior?
) {
    require(collapsedHeight.isSpecified && collapsedHeight.isFinite) {
        "The collapsedHeight is expected to be specified and finite"
    }
    require(expandedHeight.isSpecified && expandedHeight.isFinite) {
        "The expandedHeight is expected to be specified and finite"
    }
    require(expandedHeight >= collapsedHeight) {
        "The expandedHeight is expected to be greater or equal to the collapsedHeight"
    }
    val expandedHeightPx: Float
    val collapsedHeightPx: Float
    val titleBottomPaddingPx: Int
    LocalDensity.current.run {
        expandedHeightPx = expandedHeight.toPx()
        collapsedHeightPx = collapsedHeight.toPx()
        titleBottomPaddingPx = titleBottomPadding.roundToPx()
    }

    // Sets the app bar's height offset limit to hide just the bottom title area and keep top title
    // visible when collapsed.
    SideEffect {
        if (scrollBehavior?.state?.heightOffsetLimit != -expandedHeightPx) {
            scrollBehavior?.state?.heightOffsetLimit = -expandedHeightPx
        }
    }

    // Obtain the container Color from the TopAppBarColors using the `collapsedFraction`, as the
    // bottom part of this TwoRowsTopAppBar changes color at the same rate the app bar expands or
    // collapse.
    // This will potentially animate or interpolate a transition between the container color and the
    // container's scrolled color according to the app bar's scroll state.
    val colorTransitionFraction = scrollBehavior?.state?.collapsedFraction ?: 0f
    val topColorTransitionFraction = scrollBehavior?.state?.topCollapsedFraction(collapsedHeightPx) ?: 0f
    val bottomColorTransitionFraction = scrollBehavior?.state?.bottomCollapsedFraction(collapsedHeightPx, expandedHeightPx) ?: 0f

    val appBarContainerColor =
        lerp(
            colors.containerColor,
            colors.scrolledContainerColor,
            FastOutLinearInEasing.transform(colorTransitionFraction)
        )

    // Wrap the given actions in a Row.
    val actionsRow =
        @Composable {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    val topTitleAlpha = TopTitleAlphaEasing.transform(topColorTransitionFraction)
    val bottomTitleAlpha = 1f - bottomColorTransitionFraction
    // Hide the top row title semantics when its alpha value goes below 0.5 threshold.
    // Hide the bottom row title semantics when the top title semantics are active.
    val hideTopRowSemantics = topColorTransitionFraction < 0.5f
    val hideBottomRowSemantics = bottomColorTransitionFraction < 0.5f

    // Set up support for resizing the top app bar when vertically dragging the bar itself.
    val appBarDragModifier =
        if (scrollBehavior != null && !scrollBehavior.isPinned) {
            Modifier.draggable(
                orientation = Orientation.Vertical,
                state =
                    rememberDraggableState { delta -> scrollBehavior.state.heightOffset += delta },
                onDragStopped = { velocity ->
                    settleAppBar(
                        scrollBehavior.state,
                        velocity,
                        scrollBehavior.flingAnimationSpec,
                        scrollBehavior.snapAnimationSpec
                    )
                }
            )
        } else {
            Modifier
        }

    Surface(modifier = modifier.then(appBarDragModifier), color = appBarContainerColor) {
        Column {
            AppBarLayout(
                modifier =
                    Modifier
                        .windowInsetsPadding(windowInsets)
                        // clip after padding so we don't show the title over the inset area
                        .clipToBounds()
                        .heightIn(max = collapsedHeight),
                scrolledOffset = {
                    scrollBehavior?.state?.topHeightOffset(
                        topHeightPx = collapsedHeightPx,
                        totalHeightPx = expandedHeightPx,
                    ) ?: 0f
                },
                navigationIconContentColor = colors.navigationIconContentColor,
                titleContentColor = colors.titleContentColor,
                actionIconContentColor = colors.actionIconContentColor,
                title = smallTitle,
                titleTextStyle = smallTitleTextStyle,
                titleAlpha = topTitleAlpha,
                titleVerticalArrangement = Arrangement.Bottom,
                titleHorizontalArrangement = Arrangement.Start,
                titleBottomPadding = 0,
                hideTitleSemantics = hideTopRowSemantics,
                navigationIcon = navigationIcon,
                actions = actionsRow,
            )
            AppBarLayout(
                modifier =
                    Modifier
                        // only apply the horizontal sides of the window insets padding, since the
                        // top
                        // padding will always be applied by the layout above
                        .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Horizontal))
                        .clipToBounds()
                        .heightIn(max = expandedHeight - collapsedHeight),
                scrolledOffset = {
                    scrollBehavior?.state?.bottomHeightOffset(
                        topHeightPx = collapsedHeightPx,
                        totalHeightPx = expandedHeightPx,
                    ) ?: 0f
                },
                navigationIconContentColor = colors.navigationIconContentColor,
                titleContentColor = colors.titleContentColor,
                actionIconContentColor = colors.actionIconContentColor,
                title = title,
                titleTextStyle = titleTextStyle,
                titleAlpha = bottomTitleAlpha,
                titleVerticalArrangement = Arrangement.Bottom,
                titleHorizontalArrangement = Arrangement.Start,
                titleBottomPadding = titleBottomPaddingPx,
                hideTitleSemantics = hideBottomRowSemantics,
                navigationIcon = {},
                actions = {}
            )
        }
    }
}

@Composable
private fun AppBarLayout(
    modifier: Modifier,
    scrolledOffset: ScrolledOffset,
    navigationIconContentColor: Color,
    titleContentColor: Color,
    actionIconContentColor: Color,
    title: @Composable () -> Unit,
    titleTextStyle: TextStyle,
    titleAlpha: Float,
    titleVerticalArrangement: Arrangement.Vertical,
    titleHorizontalArrangement: Arrangement.Horizontal,
    titleBottomPadding: Int,
    hideTitleSemantics: Boolean,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable () -> Unit,
) {
    Layout(
        {
            Box(Modifier
                .layoutId("navigationIcon")
                .padding(start = TopAppBarHorizontalPadding)) {
                CompositionLocalProvider(
                    LocalContentColor provides navigationIconContentColor,
                    content = navigationIcon
                )
            }
            Box(
                Modifier
                    .layoutId("title")
                    .padding(horizontal = TopAppBarHorizontalPadding)
                    .then(if (hideTitleSemantics) Modifier.clearAndSetSemantics {} else Modifier)
                    .graphicsLayer(alpha = titleAlpha)
            ) {
                ProvideContentColorTextStyle(
                    contentColor = titleContentColor,
                    textStyle = titleTextStyle,
                    content = title
                )
            }
            Box(Modifier
                .layoutId("actionIcons")
                .padding(end = TopAppBarHorizontalPadding)) {
                CompositionLocalProvider(
                    LocalContentColor provides actionIconContentColor,
                    content = actions
                )
            }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val navigationIconPlaceable =
            measurables
                .fastFirst { it.layoutId == "navigationIcon" }
                .measure(constraints.copy(minWidth = 0))
        val actionIconsPlaceable =
            measurables
                .fastFirst { it.layoutId == "actionIcons" }
                .measure(constraints.copy(minWidth = 0))

        val maxTitleWidth =
            if (constraints.maxWidth == Constraints.Infinity) {
                constraints.maxWidth
            } else {
                (constraints.maxWidth - navigationIconPlaceable.width - actionIconsPlaceable.width)
                    .coerceAtLeast(0)
            }
        val titlePlaceable =
            measurables
                .fastFirst { it.layoutId == "title" }
                .measure(constraints.copy(minWidth = 0, maxWidth = maxTitleWidth))

        // Locate the title's baseline.
        val titleBaseline =
            if (titlePlaceable[LastBaseline] != AlignmentLine.Unspecified) {
                titlePlaceable[LastBaseline]
            } else {
                0
            }

        // Subtract the scrolledOffset from the maxHeight. The scrolledOffset is expected to be
        // equal or smaller than zero.
        val scrolledOffsetValue = scrolledOffset.offset()
        val heightOffset = if (scrolledOffsetValue.isNaN()) 0 else scrolledOffsetValue.roundToInt()

        val layoutHeight =
            if (constraints.maxHeight == Constraints.Infinity) {
                constraints.maxHeight
            } else {
                constraints.maxHeight + heightOffset
            }

        layout(constraints.maxWidth, layoutHeight) {
            // Navigation icon
            navigationIconPlaceable.placeRelative(
                x = 0,
                y =
                    when (titleVerticalArrangement) {
                        Arrangement.Bottom -> {
                            val padding = (constraints.maxHeight - navigationIconPlaceable.height) / 2
                            val paddingFromBottom = padding - (navigationIconPlaceable.height - titleBaseline)
                            val heightWithPadding = paddingFromBottom + navigationIconPlaceable.height
                            val adjustedBottomPadding =
                                if (heightWithPadding > constraints.maxHeight) {
                                    paddingFromBottom -
                                        (heightWithPadding - constraints.maxHeight)
                                } else {
                                    paddingFromBottom
                                }

                            layoutHeight - navigationIconPlaceable.height - max(0, adjustedBottomPadding)
                        }
                        else -> (layoutHeight - navigationIconPlaceable.height) / 2
                    }
            )

            // Title
            titlePlaceable.placeRelative(
                x =
                    when (titleHorizontalArrangement) {
                        Arrangement.Center -> {
                            var baseX = (constraints.maxWidth - titlePlaceable.width) / 2
                            if (baseX < navigationIconPlaceable.width) {
                                // May happen if the navigation is wider than the actions and the
                                // title is long. In this case, prioritize showing more of the title
                                // by
                                // offsetting it to the right.
                                baseX += (navigationIconPlaceable.width - baseX)
                            } else if (
                                baseX + titlePlaceable.width >
                                constraints.maxWidth - actionIconsPlaceable.width
                            ) {
                                // May happen if the actions are wider than the navigation and the
                                // title
                                // is long. In this case, offset to the left.
                                baseX +=
                                    ((constraints.maxWidth - actionIconsPlaceable.width) -
                                        (baseX + titlePlaceable.width))
                            }
                            baseX
                        }
                        Arrangement.End ->
                            constraints.maxWidth - titlePlaceable.width - actionIconsPlaceable.width
                        // Arrangement.Start.
                        // An TopAppBarTitleInset will make sure the title is offset in case the
                        // navigation icon is missing.
                        else -> max(TopAppBarTitleInset.roundToPx(), navigationIconPlaceable.width)
                    },
                y =
                    when (titleVerticalArrangement) {
                        Arrangement.Center -> (layoutHeight - titlePlaceable.height) / 2
                        // Apply bottom padding from the title's baseline only when the Arrangement
                        // is
                        // "Bottom".
                        Arrangement.Bottom -> {
                            val padding = if (titleBottomPadding == 0) {
                                (constraints.maxHeight - titlePlaceable.height) / 2
                            } else {
                                titleBottomPadding
                            }
                            // Calculate the actual padding from the bottom of the title, taking
                            // into account its baseline.
                            val paddingFromBottom =
                                padding - (titlePlaceable.height - titleBaseline)
                            // Adjust the bottom padding to a smaller number if there is no room
                            // to
                            // fit the title.
                            val heightWithPadding = paddingFromBottom + titlePlaceable.height
                            val adjustedBottomPadding =
                                if (heightWithPadding > constraints.maxHeight) {
                                    paddingFromBottom -
                                        (heightWithPadding - constraints.maxHeight)
                                } else {
                                    paddingFromBottom
                                }

                            layoutHeight - titlePlaceable.height - max(0, adjustedBottomPadding)
                        }
                        // Arrangement.Top
                        else -> 0
                    }
            )

            // Action icons
            actionIconsPlaceable.placeRelative(
                x = constraints.maxWidth - actionIconsPlaceable.width,
                y = (layoutHeight - actionIconsPlaceable.height) / 2
            )
        }
    }
}

@Composable
internal fun ProvideContentColorTextStyle(
    contentColor: Color,
    textStyle: TextStyle,
    content: @Composable () -> Unit
) {
    val mergedStyle = LocalTextStyle.current.merge(textStyle)
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalTextStyle provides mergedStyle,
        content = content
    )
}

private fun interface ScrolledOffset {
    fun offset(): Float
}

private suspend fun settleAppBar(
    state: TopAppBarState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?
): Velocity {
    // Check if the app bar is completely collapsed/expanded. If so, no need to settle the app bar,
    // and just return Zero Velocity.
    // Note that we don't check for 0f due to float precision with the collapsedFraction
    // calculation.
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }
    var remainingVelocity = velocity
    // In case there is an initial velocity that was left after a previous user fling, animate to
    // continue the motion to expand or collapse the app bar.
    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        AnimationState(
            initialValue = 0f,
            initialVelocity = velocity,
        )
            .animateDecay(flingAnimationSpec) {
                val delta = value - lastValue
                val initialHeightOffset = state.heightOffset
                state.heightOffset = initialHeightOffset + delta
                val consumed = abs(initialHeightOffset - state.heightOffset)
                lastValue = value
                remainingVelocity = this.velocity
                // avoid rounding errors and stop if anything is unconsumed
                if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
            }
    }
    // Snap if animation specs were provided.
    if (snapAnimationSpec != null) {
        // FIXME: Only snap the top app bar
        if (state.heightOffset < 0 && state.heightOffset > state.heightOffsetLimit) {
            AnimationState(initialValue = state.heightOffset).animateTo(
                if (state.collapsedFraction < 0.5f) {
                    0f
                } else {
                    state.heightOffsetLimit
                },
                animationSpec = snapAnimationSpec
            ) {
                state.heightOffset = value
            }
        }
    }

    return Velocity(0f, remainingVelocity)
}

private fun TopAppBarState.topHeightOffset(topHeightPx: Float, totalHeightPx: Float): Float {
    val offset = heightOffset + (totalHeightPx - topHeightPx)
    return offset.coerceIn(-topHeightPx, 0f)
}

private fun TopAppBarState.bottomHeightOffset(topHeightPx: Float, totalHeightPx: Float): Float {
    return heightOffset.coerceIn(topHeightPx - totalHeightPx, 0f)
}

private fun TopAppBarState.topCollapsedFraction(topHeightPx: Float): Float {
    return heightOffset / -topHeightPx
}

private fun TopAppBarState.bottomCollapsedFraction(topHeightPx: Float, totalHeightPx: Float): Float {
    return heightOffset / (topHeightPx - totalHeightPx)
}

@Composable
fun enterAlwaysCollapsedScrollBehavior(
    state: TopAppBarState = rememberTopAppBarState(),
    canScroll: () -> Boolean = { true },
    flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay()
): TopAppBarScrollBehavior {
    val topHeightPx: Float
    val totalHeightPx: Float
    LocalDensity.current.run {
        topHeightPx = CollapsedContainerHeight.toPx()
        totalHeightPx = ExpandedContainerHeight.toPx()
    }

    return EnterAlwaysCollapsedScrollBehavior(
        state = state,
        flingAnimationSpec = flingAnimationSpec,
        canScroll = canScroll,
        topHeightPx = topHeightPx,
        totalHeightPx = totalHeightPx,
    )
}

// TODO: Current it behaves exactly like EnterAlways
private class EnterAlwaysCollapsedScrollBehavior(
    override val state: TopAppBarState,
    override val flingAnimationSpec: DecayAnimationSpec<Float>?,
    val canScroll: () -> Boolean = { true },
    val topHeightPx: Float,
    val totalHeightPx: Float,
) : TopAppBarScrollBehavior {
    override val snapAnimationSpec: AnimationSpec<Float>? = null  // J2K's app bar doesn't do snap
    override val isPinned: Boolean = false
    override var nestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Don't intercept if scrolling down.
                if (!canScroll()) return Offset.Zero

                val prevHeightOffset = state.heightOffset
                state.heightOffset += available.y
                return if (prevHeightOffset != state.heightOffset) {
                    // We're in the middle of top app bar collapse or expand.
                    // Consume only the scroll on the Y axis.
                    available.copy(x = 0f)
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!canScroll()) return Offset.Zero
                state.contentOffset += consumed.y

                if (available.y < 0f || consumed.y < 0f) {
                    // When scrolling up, just update the state's height offset.
                    val oldHeightOffset = state.heightOffset
                    state.heightOffset += consumed.y
                    return Offset(0f, state.heightOffset - oldHeightOffset)
                }

                if (consumed.y == 0f && available.y > 0) {
                    // Reset the total content offset to zero when scrolling all the way down. This
                    // will eliminate some float precision inaccuracies.
                    state.contentOffset = 0f
                }

                if (available.y > 0f) {
                    // Adjust the height offset in case the consumed delta Y is less than what was
                    // recorded as available delta Y in the pre-scroll.
                    val oldHeightOffset = state.heightOffset
                    state.heightOffset += available.y
                    return Offset(0f, state.heightOffset - oldHeightOffset)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val superConsumed = super.onPostFling(consumed, available)
                return superConsumed +
                    settleAppBar(state, available.y, flingAnimationSpec, snapAnimationSpec)
            }
        }
}

val CollapsedContainerHeight = 64.0.dp
val ExpandedContainerHeight = 152.0.dp
internal val TopTitleAlphaEasing = CubicBezierEasing(.8f, 0f, .8f, .15f)
private val MediumTitleBottomPadding = 24.dp
private val LargeTitleBottomPadding = 28.dp
private val TopAppBarHorizontalPadding = 4.dp
private val TopAppBarTitleInset = 16.dp - TopAppBarHorizontalPadding
