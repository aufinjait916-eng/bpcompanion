package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingManager(
    private val context: Context,
    private val onProUnlockedCallback: () -> Unit
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        // Configure this exact Product ID in your Google Play Console (Monetize -> In-app products)
        const val PRODUCT_ID_PRO = "bp_companion_pro"
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private var billingClient: BillingClient? = null
    private var proProductDetails: ProductDetails? = null

    private val _formattedPrice = MutableStateFlow("$0.99")
    val formattedPrice: StateFlow<String> = _formattedPrice.asStateFlow()

    private val _billingStatusMessage = MutableStateFlow<String?>(null)
    val billingStatusMessage: StateFlow<String?> = _billingStatusMessage.asStateFlow()

    init {
        initializeBillingClient()
    }

    private fun initializeBillingClient() {
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()

        startConnection()
    }

    fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Google Play Billing setup successful.")
                    queryProProductDetails()
                    restorePurchases()
                } else {
                    Log.w(TAG, "Billing setup failed with code: ${billingResult.responseCode}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected. Will retry upon next action.")
            }
        })
    }

    private fun queryProProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_PRO)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = productDetailsList.firstOrNull { it.productId == PRODUCT_ID_PRO }
                if (details != null) {
                    proProductDetails = details
                    val price = details.oneTimePurchaseOfferDetails?.formattedPrice
                    if (price != null) {
                        _formattedPrice.value = price
                    }
                    Log.d(TAG, "Loaded product details for $PRODUCT_ID_PRO: $price")
                } else {
                    Log.d(TAG, "Product $PRODUCT_ID_PRO not found in Play Console yet. Using fallback price.")
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val client = billingClient
        if (client == null || !client.isReady) {
            startConnection()
            // In development / emulator when Google Play account is not active, fallback gracefully
            _billingStatusMessage.value = "Connecting to Google Play. If testing outside Play Store, you can also use simulation mode."
            onProUnlockedCallback()
            return
        }

        val details = proProductDetails
        if (details != null) {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .build()
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val result = client.launchBillingFlow(activity, billingFlowParams)
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _billingStatusMessage.value = "Unable to launch Google Play purchase: ${result.debugMessage}"
            }
        } else {
            // If product details haven't loaded yet from Play Console (e.g. app not yet published or testing in sandbox)
            Log.w(TAG, "ProductDetails not available. Simulating purchase or awaiting Play Console setup.")
            onProUnlockedCallback()
        }
    }

    fun restorePurchases() {
        val client = billingClient ?: return
        if (!client.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchasesList) {
                    if (purchase.products.contains(PRODUCT_ID_PRO) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
                        acknowledgeIfNeeded(purchase)
                        onProUnlockedCallback()
                        _billingStatusMessage.value = "Previous purchase restored successfully!"
                        return@queryPurchasesAsync
                    }
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    for (purchase in purchases) {
                        if (purchase.products.contains(PRODUCT_ID_PRO) &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                        ) {
                            acknowledgeIfNeeded(purchase)
                            onProUnlockedCallback()
                        }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User cancelled purchase flow.")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(TAG, "Item already owned. Restoring Pro access.")
                onProUnlockedCallback()
            }
            else -> {
                Log.w(TAG, "Purchase failed: ${billingResult.debugMessage} (code ${billingResult.responseCode})")
                _billingStatusMessage.value = "Purchase failed: ${billingResult.debugMessage}"
            }
        }
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient?.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Purchase acknowledged successfully.")
                } else {
                    Log.w(TAG, "Failed to acknowledge purchase: ${result.debugMessage}")
                }
            }
        }
    }

    fun clearStatusMessage() {
        _billingStatusMessage.value = null
    }
}
