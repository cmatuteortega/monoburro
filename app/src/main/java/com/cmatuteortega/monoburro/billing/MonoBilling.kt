package com.cmatuteortega.monoburro.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** What the UI needs to know about the Mono subscription. */
data class BillingState(
    /** Monthly price as Google Play formats it for the user's country (e.g. "5,00 €"). Null until loaded. */
    val price: String? = null,
    /** Whether a Mono subscription is active. Null until Play has answered. */
    val subscribed: Boolean? = null,
    /** Play is connected and the product was found, so a purchase can start. */
    val canPurchase: Boolean = false,
    /** A purchase flow is open or being confirmed. */
    val busy: Boolean = false,
    /** Something the user should be told (pending payment, Play unavailable, error). */
    val message: String? = null,
)

/**
 * Google Play Billing for the single Mono subscription.
 *
 * The product is a monthly auto-renewing subscription with id [PRODUCT_ID],
 * priced at €5/month in the Play Console (Play converts it for other
 * countries; [BillingState.price] is the localized string to show).
 *
 * There's no backend, so entitlement is what Play reports on this device:
 * purchases are acknowledged here and re-queried on every [refresh].
 */
class MonoBilling(context: Context) : PurchasesUpdatedListener {
    private val _state = MutableStateFlow(BillingState())
    val state: StateFlow<BillingState> = _state.asStateFlow()

    private var product: ProductDetails? = null

    private val client = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    /** Connects if needed, then reloads the product and the user's purchases. */
    fun refresh() {
        if (client.isReady) {
            loadProduct()
            loadPurchases()
            return
        }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingResponseCode.OK) {
                    loadProduct()
                    loadPurchases()
                } else {
                    _state.update { it.copy(canPurchase = false, message = playUnavailable(result)) }
                }
            }

            override fun onBillingServiceDisconnected() {
                _state.update { it.copy(canPurchase = false) }
            }
        })
    }

    /** Opens Google Play's purchase sheet for Mono. */
    fun purchase(activity: Activity) {
        val details = product
        val offer = details?.subscriptionOfferDetails?.firstOrNull()
        if (details == null || offer == null) {
            _state.update { it.copy(message = "Mono isn't available from Google Play right now. Try again later.") }
            refresh()
            return
        }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offer.offerToken)
                        .build(),
                ),
            )
            .build()
        val result = client.launchBillingFlow(activity, params)
        if (result.responseCode == BillingResponseCode.OK) {
            _state.update { it.copy(busy = true, message = null) }
        } else {
            _state.update { it.copy(busy = false, message = "Couldn't open Google Play (${result.debugMessage}).") }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }

    fun close() = client.endConnection()

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingResponseCode.OK -> handle(purchases.orEmpty(), fromQuery = false)
            BillingResponseCode.USER_CANCELED -> _state.update { it.copy(busy = false) }
            BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _state.update { it.copy(busy = false) }
                loadPurchases()
            }
            else -> _state.update { it.copy(busy = false, message = "The purchase didn't go through (${result.debugMessage}).") }
        }
    }

    private fun loadProduct() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(ProductType.SUBS)
                        .build(),
                ),
            )
            .build()
        client.queryProductDetailsAsync(params) { result, details ->
            val found = details.productDetailsList.firstOrNull { it.productId == PRODUCT_ID }
            product = found
            val price = found?.subscriptionOfferDetails?.firstOrNull()
                ?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice
            _state.update {
                it.copy(
                    price = price ?: it.price,
                    canPurchase = result.responseCode == BillingResponseCode.OK && found != null,
                )
            }
        }
    }

    private fun loadPurchases() {
        val params = QueryPurchasesParams.newBuilder().setProductType(ProductType.SUBS).build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingResponseCode.OK) handle(purchases, fromQuery = true)
        }
    }

    private fun handle(purchases: List<Purchase>, fromQuery: Boolean) {
        val mono = purchases.filter { PRODUCT_ID in it.products }
        val active = mono.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        active.filterNot { it.isAcknowledged }.forEach(::acknowledge)
        val pending = mono.any { it.purchaseState == Purchase.PurchaseState.PENDING }
        _state.update {
            it.copy(
                // A fresh purchase callback only lists what changed, so only a full query can say "not subscribed".
                subscribed = if (active.isNotEmpty()) true else if (fromQuery) false else it.subscribed,
                busy = false,
                message = if (pending && active.isEmpty()) "Payment pending. Mono unlocks as soon as Google Play confirms it." else it.message,
            )
        }
    }

    /** Play refunds subscriptions that aren't acknowledged within 3 days. */
    private fun acknowledge(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        client.acknowledgePurchase(params) { }
    }

    private fun playUnavailable(result: BillingResult) = when (result.responseCode) {
        BillingResponseCode.BILLING_UNAVAILABLE -> "Google Play billing isn't available on this device."
        BillingResponseCode.SERVICE_UNAVAILABLE -> "Can't reach Google Play. Check your connection."
        else -> "Google Play isn't responding (${result.debugMessage})."
    }

    companion object {
        /** Subscription product id in the Play Console: monthly, €5. */
        const val PRODUCT_ID = "mono_monthly"
        /** Shown until Play returns the localized price. */
        const val FALLBACK_PRICE = "€5"
        /** Where users manage or cancel the subscription. */
        const val MANAGE_URL = "https://play.google.com/store/account/subscriptions?sku=$PRODUCT_ID&package=com.cmatuteortega.monoburro"
    }
}
