package com.appnest.safe.credentials.vault.data.mockdata

import com.appnest.safe.credentials.vault.domain.model.CardCredential
import com.appnest.safe.credentials.vault.domain.model.ComputerCredential
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.model.SecurityQuestion
import com.appnest.safe.credentials.vault.domain.model.SiteCredential
import java.util.UUID

data class MockAppInfo(
    val name: String,
    val packageName: String
)

object MockDataProvider {

    fun getInitialCredentials(): List<Credential> {
        return listOf(
            SiteCredential(
                id = "mock-1",
                siteOrApp = "7stardigitalnetwork.com",
                category = "Site",
                username = "admin@7stardigital.com",
                password = "7StarSecurePass#2026",
                pin = "7788",
                note = "Primary ISP admin account",
                isFavorite = false
            ),
            SiteCredential(
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
            CardCredential(
                id = "mock-3",
                siteOrApp = "Primary HDFC Visa Card",
                category = "Card",
                cardIssuer = "HDFC Bank",
                cardNameOnCard = "John Doe",
                cardNumber = "4111 2222 3333 4444",
                cardValidityStart = "05/22",
                cardValidityEnd = "05/29",
                cardCvv = "998",
                pin = "4321",
                note = "Primary international credit card",
                isFavorite = true
            ),
            ComputerCredential(
                id = "mock-4",
                siteOrApp = "Work MacBook Pro",
                category = "Computer",
                osType = "Mac",
                username = "developer_admin",
                password = "MacBookSecurePass2026!",
                pin = "9900",
                note = "Company workstation login",
                isFavorite = false
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
