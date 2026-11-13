package com.appnest.safe.credentials.vault.utils

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy

object ImageLoaderUtils {
    fun createImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve(Constants.IMAGE_CACHE_DIR))
                    .maxSizePercent(Constants.IMAGE_CACHE_MAX_SIZE_PERCENT)
                    .build()
            }
            // Aggressively cache the images.
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) // Force caching even if server says no-cache
            .crossfade(true)
            .build()
    }

    fun getFaviconUrl(domain: String): String {
        return String.format(Constants.FAVICON_URL_TEMPLATE, domain)
    }
}