package app.appsdial.inappdemo

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import com.android.billingclient.api.*


class MainActivity : Activity(), PurchasesUpdatedListener {

    lateinit var btnBuyProduct: Button
    private lateinit var mBillingClient: BillingClient
    private val listOfSUKS = listOf("test_product_one", "test_product_two")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnBuyProduct = findViewById(R.id.buttonBuyProduct)

        initBillingClient()

    }

    private fun initBillingClient() {
        mBillingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        mBillingClient.startConnection(object : BillingClientStateListener {
            @RequiresApi(31)
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    logger("Setup Billing Successful")
                    loadAllSKUs()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                logger("Setup Billing Failed")

            }
        })

    }

    @RequiresApi(31)
    private fun loadAllSKUs() = if (mBillingClient.isReady) {
        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(listOfSUKS)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        mBillingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            // Process the result.
            if (skuDetailsList != null) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {
                    for (skuDetails in skuDetailsList) {
                                if (skuDetails.sku == "test_product_two")
                                    btnBuyProduct.setOnClickListener {
                                        val billingFlowParams = BillingFlowParams
                                            .newBuilder()
                                            .setSkuDetails(skuDetails)
                                            .build()
                                        mBillingClient.launchBillingFlow(this, billingFlowParams)
                                    }
                            }
                }
            }
            if (skuDetailsList != null) {
                logger(skuDetailsList[0].description)
            }

        }

    } else {
        logger("Billing Client is not ready")
    }


    private fun acknowledgePurchaseDetails(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        mBillingClient.acknowledgePurchase(params) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage
            logger(debugMessage)
            logger(responseCode)
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                acknowledgePurchaseDetails(purchase.purchaseToken)

            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            logger("User Cancelled")
            logger(billingResult.debugMessage)


        } else {
            logger(billingResult.debugMessage)
            // Handle any other error codes.
        }
    }

}