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

sealed class PremiumStatus {
    object Loading : PremiumStatus()
    object Free : PremiumStatus()
    data class Trial(val daysLeft: Int) : PremiumStatus()
    object Premium : PremiumStatus()
}

class BillingManager(context: Context) : PurchasesUpdatedListener {

    companion object {
        const val SKU_PREMIUM_MONTHLY = "zeitwert_premium_monthly"   // 2,99 €/Monat
        const val SKU_PREMIUM_LIFETIME = "zeitwert_premium_lifetime" // 19,99 € einmalig
    }

    private val _premiumStatus = MutableStateFlow<PremiumStatus>(PremiumStatus.Loading)
    val premiumStatus: StateFlow<PremiumStatus> = _premiumStatus.asStateFlow()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build())
        .build()

    fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {
                // Retry-Logik hier
            }
        })
    }

    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { _, purchases ->
            val hasActiveSub = purchases.any {
                it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        it.products.contains(SKU_PREMIUM_MONTHLY)
            }

            if (hasActiveSub) {
                _premiumStatus.value = PremiumStatus.Premium
                return@queryPurchasesAsync
            }

            // Einmalig-Kauf prüfen
            val inappParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            billingClient.queryPurchasesAsync(inappParams) { _, inapps ->
                val hasLifetime = inapps.any {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                            it.products.contains(SKU_PREMIUM_LIFETIME)
                }
                _premiumStatus.value = if (hasLifetime) PremiumStatus.Premium
                else PremiumStatus.Free
            }
        }
    }

    fun launchMonthlySubscription(activity: Activity) =
        launchBillingFlow(activity, SKU_PREMIUM_MONTHLY, BillingClient.ProductType.SUBS)

    fun launchLifetimePurchase(activity: Activity) =
        launchBillingFlow(activity, SKU_PREMIUM_LIFETIME, BillingClient.ProductType.INAPP)

    private fun launchBillingFlow(activity: Activity, productId: String, type: String) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(type)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { _, productDetailsList ->
            val productDetails = productDetailsList.firstOrNull() ?: return@queryProductDetailsAsync

            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken

            val productDetailsParamsList = buildList {
                val builder = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                if (offerToken != null) builder.setOfferToken(offerToken)
                add(builder.build())
            }

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            billingClient.launchBillingFlow(activity, flowParams)
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    acknowledgePurchase(purchase)
                    _premiumStatus.value = PremiumStatus.Premium
                }
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { }
        }
    }

    fun disconnect() = billingClient.endConnection()
}
