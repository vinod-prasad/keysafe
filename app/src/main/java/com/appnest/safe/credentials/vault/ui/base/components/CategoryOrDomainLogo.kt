package com.appnest.safe.credentials.vault.ui.base.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appnest.safe.credentials.vault.ui.viewmodel.cleanDomainDisplay
import com.appnest.safe.credentials.vault.ui.viewmodel.getCategoryIconRes

@Composable
fun CategoryOrDomainLogo(
    category: String,
    nameOrDomain: String,
    modifier: Modifier = Modifier,
    containerSize: Dp = 40.dp,
    iconSize: Dp = 24.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    val fallbackIconRes = getCategoryIconRes(category, nameOrDomain)
    val isSite = category.equals("Site", ignoreCase = true)
    val cleanDomain = nameOrDomain.cleanDomainDisplay().trim()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .size(containerSize)
            .background(containerColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isSite && cleanDomain.isNotBlank() && cleanDomain.contains(".")) {
            val faviconUrl = com.appnest.safe.credentials.vault.utils.ImageLoaderUtils.getFaviconUrl(cleanDomain)
            val imageRequest = remember(faviconUrl, fallbackIconRes) {
                ImageRequest.Builder(context)
                    .data(faviconUrl)
                    .error(fallbackIconRes)
                    .placeholder(fallbackIconRes)
                    .crossfade(true)
                    .build()
            }
            AsyncImage(
                model = imageRequest,
                contentDescription = nameOrDomain,
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                painter = painterResource(id = fallbackIconRes),
                contentDescription = nameOrDomain,
                tint = iconTint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
