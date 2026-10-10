package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialWheelPicker(
    items: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemsCount: Int = 3,
    itemHeight: Dp = 44.dp,
    label: String = "",
    unit: String = "",
    highlightColor: Color = MaterialTheme.colorScheme.primary
) {
    val coroutineScope = rememberCoroutineScope()
    val initialIndex = remember(items, selectedItem) {
        val idx = items.indexOf(selectedItem)
        if (idx >= 0) idx else 0
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Sync state changes from scroll to caller
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (index in items.indices) {
                    onItemSelected(items[index])
                }
            }
    }

    // Scroll to new selected item if changed externally
    LaunchedEffect(selectedItem) {
        val targetIdx = items.indexOf(selectedItem)
        if (targetIdx >= 0 && targetIdx != listState.firstVisibleItemIndex) {
            listState.animateScrollToItem(targetIdx)
        }
    }

    val totalHeight = itemHeight * visibleItemsCount

    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label & Target Header
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = highlightColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Nudge Up Arrow Button
        IconButton(
            onClick = {
                val currentIdx = items.indexOf(selectedItem)
                if (currentIdx > 0) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(currentIdx - 1)
                        onItemSelected(items[currentIdx - 1])
                    }
                }
            },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase value",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // The Wheel Drum
        Box(
            modifier = Modifier
                .width(82.dp)
                .height(totalHeight),
            contentAlignment = Alignment.Center
        ) {
            // Center Selection Highlight Band
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight),
                shape = RoundedCornerShape(10.dp),
                color = highlightColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, highlightColor.copy(alpha = 0.4f))
            ) {}

            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(vertical = itemHeight * ((visibleItemsCount - 1) / 2)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeight)
            ) {
                items(count = items.size) { index ->
                    val value = items[index]
                    val isSelected by remember {
                        derivedStateOf { listState.firstVisibleItemIndex == index }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value.toString(),
                            style = if (isSelected) {
                                MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = highlightColor
                                )
                            } else {
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                )
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Nudge Down Arrow Button
        IconButton(
            onClick = {
                val currentIdx = items.indexOf(selectedItem)
                if (currentIdx < items.size - 1) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(currentIdx + 1)
                        onItemSelected(items[currentIdx + 1])
                    }
                }
            },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease value",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
