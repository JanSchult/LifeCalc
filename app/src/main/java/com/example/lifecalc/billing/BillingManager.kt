package com.example.lifecalc.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow



class BillingManager(context: Context) : PurchasesUpdatedListener {

    companion object {
        const val SKU_PREMIUM_MONTHLY = "lifecost_premium_monthly"   // 4,99 €/Monat
        const val SKU_PREMIUM_YEARLY = "lifecost_premium_yearly"   // 49,99 €/Jahr
    }

    private val _premiumStatus = MutableStateFlow<PremiumStatus>(PremiumStatus.Loading)
    val premiumStatus: StateFlow<PremiumStatus> = _premiumStatus.asStateFlow()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    // ── Verbindung ────────────────────────────────────────────────────────

    fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                } else {
                    // Billing nicht verfügbar (z.B. kein Play Store)
                    _premiumStatus.value = PremiumStatus.Free
                }
            }

            override fun onBillingServiceDisconnected() {
                // Einfacher Retry — bei echter App mit Backoff
                connect()
            }
        })
    }

    fun disconnect() = billingClient.endConnection()

    // ── Bestehende Käufe prüfen ───────────────────────────────────────────

    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { _, purchases ->
            val hasPremium = purchases.any {
                it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        (it.products.contains(SKU_PREMIUM_MONTHLY) ||
                                it.products.contains(SKU_PREMIUM_YEARLY))
            }
            _premiumStatus.value = if (hasPremium) PremiumStatus.Premium
            else PremiumStatus.Free
        }
    }


    // ── Kaufdialog öffnen ─────────────────────────────────────────────────

    fun launchMonthlySubscription(activity: Activity) =
        launchBillingFlow(activity, SKU_PREMIUM_MONTHLY, BillingClient.ProductType.SUBS)

    fun launchYearlySubscription(activity: Activity) =
        launchBillingFlow(activity, SKU_PREMIUM_YEARLY, BillingClient.ProductType.INAPP)

    private fun launchBillingFlow(activity: Activity, productId: String, type: String) {
        if (!billingClient.isReady) {
            connect() // Reconnect falls Verbindung weg
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(type)
                .build()
        )

        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()
        ) { billingResult, productDetailsList ->

            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
            val productDetails = productDetailsList.firstOrNull() ?: return@queryProductDetailsAsync

            val offerToken = productDetails.subscriptionOfferDetails
                ?.firstOrNull()?.offerToken

            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .apply { if (offerToken != null) setOfferToken(offerToken) }
                .build()

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()

            activity.runOnUiThread {
                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    // ── Kauf bestätigen ───────────────────────────────────────────────────

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        acknowledgePurchase(purchase)
                        _premiumStatus.value = PremiumStatus.Premium
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                // Nutzer hat abgebrochen — kein Fehler, nichts tun
            }
            else -> {
                // Echter Fehler — Status nicht ändern
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            ) { /* Acknowledge-Callback — bei Fehler retry nötig */ }
        }
    }
}