package com.ilhanaltunbas.divvydrive.uix.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableFloatingActionButton(
    onFabClicked: (Boolean) -> Unit,
    onOption1Clicked: () -> Unit,
    onOption2Clicked: () -> Unit,
    onOption3Clicked: () -> Unit,
    modifier: Modifier = Modifier,
    fabSize: Dp = 56.dp,
    expandedFabSize: Dp = 48.dp,
    spaceBetweenFabs: Dp = 16.dp,
    fabIconColor: Color = Color.White,
    mainFabGradientColors: List<Color> = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
    expandedFabGradientColors: List<Color> = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.surfaceVariant) // Seçenek FAB'ları için farklı bir gradient
) {
    var isExpanded by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "FabRotation"
    )
    val customEasing = CubicBezierEasing(0.18f, 0.89f, 0.32f, 1.0f)

    val option1OffsetY by animateDpAsState(
        targetValue = if (isExpanded) -(fabSize + spaceBetweenFabs) else 0.dp,
        animationSpec = tween(durationMillis = 250, easing = customEasing),
        label = "Option1OffsetY"
    )
    val option2OffsetY by animateDpAsState(
        targetValue = if (isExpanded) -(fabSize + spaceBetweenFabs + expandedFabSize + spaceBetweenFabs) else 0.dp,
        animationSpec = tween(durationMillis = 300, easing = customEasing),
        label = "Option2OffsetY"
    )
    val option3OffsetY by animateDpAsState(
        targetValue = if (isExpanded) -(fabSize + spaceBetweenFabs + expandedFabSize + spaceBetweenFabs + expandedFabSize + spaceBetweenFabs) else 0.dp,
        animationSpec = tween(durationMillis = 350, easing = customEasing),
        label = "Option3OffsetY" // Etiketi düzeltin
    )

    val mainFabGradient = Brush.verticalGradient(colors = mainFabGradientColors)
    val expandedFabGradient = Brush.verticalGradient(colors = expandedFabGradientColors) // Seçenekler için gradient

    Box(modifier = modifier) {
        Box(contentAlignment = Alignment.BottomEnd) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(150, delayMillis = 50)) + scaleIn(animationSpec = tween(150, delayMillis = 50)),
                exit = fadeOut(animationSpec = tween(150, delayMillis = 50)) + scaleOut(animationSpec = tween(150, delayMillis = 50))
            ) {
                Box(
                    modifier = Modifier
                        .size(fabSize)
                        .offset(y = option1OffsetY)
                        .clip(CircleShape)
                        .background(expandedFabGradient) // Seçenek FAB'ı için gradient
                        .clickable {
                            onOption1Clicked()
                            isExpanded = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CloudUpload, "Seçenek 1", tint = fabIconColor)
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(150, delayMillis = 100)) + scaleIn(animationSpec = tween(150, delayMillis = 100)),
                exit = fadeOut(animationSpec = tween(150, delayMillis = 100)) + scaleOut(animationSpec = tween(150, delayMillis = 100))
            ) {
                Box(
                    modifier = Modifier
                        .size(fabSize)
                        .offset(y = option2OffsetY)
                        .clip(CircleShape)
                        .background(expandedFabGradient) // Seçenek FAB'ı için gradient
                        .clickable {
                            onOption2Clicked()
                            isExpanded = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.UploadFile, "Seçenek 2", tint = fabIconColor)
                }
            }

            //Secenek 3
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(150, delayMillis = 150)) + scaleIn(animationSpec = tween(150, delayMillis = 150)),
                exit = fadeOut(animationSpec = tween(150, delayMillis = 150)) + scaleOut(animationSpec = tween(150, delayMillis = 150))
            ) {
                Box(
                    modifier = Modifier
                        .size(fabSize)
                        .offset(y = option3OffsetY)
                        .clip(CircleShape)
                        .background(expandedFabGradient) // Seçenek FAB'ı için gradient
                        .clickable {
                            onOption3Clicked()
                            isExpanded = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Folder, "Seçenek 3", tint = fabIconColor)
                }
            }

            // Ana FAB
            Box(
                modifier = Modifier
                    .size(fabSize)
                    .clip(CircleShape)
                    .background(mainFabGradient)
                    .clickable {
                        isExpanded = !isExpanded
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = if (isExpanded) "Menüyü Kapat" else "Menüyü Aç",
                    modifier = Modifier.rotate(rotationAngle),
                    tint = fabIconColor
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ExpandableFabAllGradientPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            ExpandableFloatingActionButton(
                onFabClicked = {},
                onOption1Clicked = { println("Option 1 Clicked") },
                onOption2Clicked = { println("Option 2 Clicked") },
                onOption3Clicked = { println("Option 3 Clicked") },
                modifier = Modifier.padding(16.dp),
                mainFabGradientColors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF)), // Ana FAB için özel gradient
                expandedFabGradientColors = listOf(Color(0xFFF09819), Color(0xFFEDDE5D)) // Seçenekler için özel gradient
            )
        }
    }
}