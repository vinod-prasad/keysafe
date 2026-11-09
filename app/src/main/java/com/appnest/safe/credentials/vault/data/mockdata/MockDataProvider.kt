package com.appnest.safe.credentials.vault.data.mockdata

import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.model.SecurityQuestion
import java.util.UUID

data class MockAppInfo(
    val name: String,
    val packageName: String
)

object MockDataProvider {

    fun getInitialCredentials(): List<Credential> {
        return listOf(
            Credential(
                id = "mock-1",
                siteOrApp = "7stardigitalnetwork.com",
                category = "Network",
                username = "admin@7stardigital.com",
                password = "7StarSecurePass#2026",
                pin = "7788",
                note = "Primary ISP admin account",
                isFavorite = false
            ),
            Credential(
                id = "mock-2",
                siteOrApp = "accounts.google.com",
                category = "Site",
                siteLogo = "com.google.android.googlequicksearchbox",
                username = "appnestcare@gmail.com",
                password = "GoogleSecurePassword123!",
                pin = "1234",
                note = "Main Play Console developer account",
                securityQuestions = listOf(
                    SecurityQuestion(UUID.randomUUID().toString(), "What is your pet's name?", "Fluffy")
                ),
                isFavorite = true
            ),
            Credential(
                id = "mock-3",
                siteOrApp = "accounts.google.com",
                category = "Site",
                siteLogo = "com.google.android.googlequicksearchbox",
                username = "NaveshaniPrasad",
                password = "GoogleWorkPass456!",
                pin = "4321",
                note = "Personal Google Cloud account",
                isFavorite = true
            ),
            Credential(
                id = "mock-4",
                siteOrApp = "adcolony.com",
                category = "Site",
                username = "monetization@adcolony.com",
                password = "AdColonyPass#99",
                note = "AdMob mediation partner",
                isFavorite = false
            ),
            Credential(
                id = "mock-5",
                siteOrApp = "adobe.com",
                category = "Laptop",
                username = "designer@appnest.com",
                password = "AdobeCreativeCloud2026",
                pin = "9900",
                note = "Creative Cloud Subscription",
                isFavorite = false
            ),
            Credential(
                id = "mock-6",
                siteOrApp = "adstargets.com",
                category = "Site",
                username = "publisher@adstargets.com",
                password = "AdsTargetsPass123",
                isFavorite = false
            ),
            Credential(
                id = "mock-7",
                siteOrApp = "airdroid.com",
                category = "Mobile",
                username = "device_remote@airdroid.com",
                password = "AirDroidKey#2026",
                isFavorite = false
            ),
            Credential(
                id = "mock-8",
                siteOrApp = "airpush.com",
                category = "Mobile",
                username = "push_ads@airpush.com",
                password = "AirPushPass789",
                isFavorite = false
            ),
            Credential(
                id = "mock-9",
                siteOrApp = "AJIO Online Shopping App",
                category = "Mobile",
                siteLogo = "com.ril.ajio",
                username = "shopper@ajio.com",
                password = "AjioShoppingPass@1",
                note = "Shopping account",
                isFavorite = true
            )
        )
    }

    fun getMockInstalledApps(): List<MockAppInfo> {
        return listOf(
            MockAppInfo("AJIO Online Shopping App", "com.ril.ajio"),
            MockAppInfo("Amazon", "com.amazon.mShop.android.shopping"),
            MockAppInfo("Facebook", "com.facebook.katana"),
            MockAppInfo("Google", "com.google.android.googlequicksearchbox"),
            MockAppInfo("Instagram", "com.instagram.android"),
            MockAppInfo("LinkedIn", "com.linkedin.android"),
            MockAppInfo("Netflix", "com.netflix.mediaclient"),
            MockAppInfo("PayPal", "com.paypal.android.p2pmobile"),
            MockAppInfo("Spotify", "com.spotify.music"),
            MockAppInfo("Telegram", "org.telegram.messenger"),
            MockAppInfo("WhatsApp", "com.whatsapp")
        )
    }
}
