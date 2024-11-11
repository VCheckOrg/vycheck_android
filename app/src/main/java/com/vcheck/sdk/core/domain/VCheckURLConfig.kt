package com.vcheck.sdk.core.domain

import com.vcheck.sdk.core.di.VCheckSDKConstantsProvider

data class VCheckURLConfig(val verificationApiBaseUrl: String, val verificationServiceUrl: String) {
    companion object {
        fun getDefaultConfig(environment: VCheckEnvironment) =
                when (environment) {
                    VCheckEnvironment.DEV ->
                            VCheckURLConfig(
                                    verificationApiBaseUrl =
                                            VCheckSDKConstantsProvider
                                                    .DEV_VERIFICATIONS_API_BASE_URL,
                                    verificationServiceUrl =
                                            VCheckSDKConstantsProvider.DEV_VERIFICATIONS_SERVICE_URL
                            )
                    VCheckEnvironment.PARTNER ->
                            VCheckURLConfig(
                                    verificationApiBaseUrl =
                                            VCheckSDKConstantsProvider
                                                    .PARTNER_VERIFICATIONS_API_BASE_URL,
                                    verificationServiceUrl =
                                            VCheckSDKConstantsProvider
                                                    .PARTNER_VERIFICATIONS_SERVICE_URL
                            )
                }
    }
}
