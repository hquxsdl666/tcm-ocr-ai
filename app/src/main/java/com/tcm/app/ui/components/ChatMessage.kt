package com.tcm.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun ChatMessageItem(
    content: String,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = "AI",
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Surface(
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isUser) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User",
                modifier = Modifier
                    .size(32.dp)
                    .padding(start = 8.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.SmartToy,
            contentDescription = "AI",
            modifier = Modifier
                .size(32.dp)
                .padding(end = 8.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    TypingDot(delayMillis = index * 200)
                }
            }
        }
    }
}

@Composable
private fun TypingDot(delayMillis: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, delayMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typing dot"
    )
    
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .graphicsLayer { scaleX = scale; scaleY = scale },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {}
    }
}
