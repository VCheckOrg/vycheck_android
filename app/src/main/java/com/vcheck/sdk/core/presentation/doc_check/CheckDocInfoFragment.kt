package com.vcheck.sdk.core.presentation.doc_check

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.VCheckSDK.TAG
import com.vcheck.sdk.core.di.VCheckSDKConstantsProvider
import com.vcheck.sdk.core.databinding.FragmentCheckDocInfoBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.*
import com.vcheck.sdk.core.presentation.VCheckMainActivity
import com.vcheck.sdk.core.presentation.adapters.CheckDocInfoAdapter
import com.vcheck.sdk.core.presentation.adapters.DocInfoEditCallback
import com.vcheck.sdk.core.util.extensions.checkStageErrorForResult
import com.vcheck.sdk.core.util.extensions.closeSDKFlow
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment
import com.vcheck.sdk.core.util.utils.isValidDocRelatedDate
import okhttp3.Interceptor
import okhttp3.OkHttpClient


class CheckDocInfoFragment : ThemeWrapperFragment(), DocInfoEditCallback {

    private lateinit var binding: FragmentCheckDocInfoBinding
    private lateinit var viewModel: CheckDocInfoViewModel
    private lateinit var dataList: MutableList<DocFieldWitOptPreFilledData>

    private val args: CheckDocInfoFragmentArgs by navArgs()

    private var uploadedDocID: Int? = null

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.designConfig!!.primary?.let {
            binding.checkInfoConfirmButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundPrimaryColorHex?.let {
            binding.checkDocInfoBackground.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundSecondaryColorHex?.let {
            binding.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundTertiaryColorHex?.let {
            binding.photoCard1Background.setCardBackgroundColor(Color.parseColor(it))
            binding.photoCard2Background.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primaryTextColorHex?.let {
            binding.checkFilledDataTitle.setTextColor(Color.parseColor(it))
            //binding.checkInfoConfirmButton.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.secondaryTextColorHex?.let {
            binding.checkFilledDataDescription.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.sectionBorderColorHex?.let {
            binding.photoCard1.setCardBackgroundColor(Color.parseColor(it))
            binding.photoCard2.setCardBackgroundColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = CheckDocInfoViewModel(VCheckDIContainer.mainRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_check_doc_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCheckDocInfoBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        val currentLocaleCode = VCheckSDK.getSDKLangCode()

        uploadedDocID = if (args.checkDocInfoDataTO != null) {
            args.checkDocInfoDataTO!!.docId
        } else {
            args.uplaodedDocId
        }

        binding.apply {
            photoCard2.isVisible = false
        }

        dataList = mutableListOf()
        val adapter = CheckDocInfoAdapter(ArrayList(),
            this@CheckDocInfoFragment, currentLocaleCode)
        binding.docInfoList.adapter = adapter

        viewModel.documentInfoResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null) {
                populateDocImages(it.data.data)
                populateDocFields(it.data.data, currentLocaleCode)
            }
        }

        viewModel.confirmedDocResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.getCurrentStage()
            }
        }

        viewModel.stageResponse.observe(viewLifecycleOwner) {
            if (it.data?.data?.config != null) {
                viewModel.repository.setLivenessMilestonesList((it.data.data.config.gestures))
                findNavController().navigate(R.id.action_checkDocInfoFragment_to_livenessInstructionsFragment)
            } else if (VCheckSDK.getVerificationType() == VerificationSchemeType.DOCUMENT_UPLOAD_ONLY) {
                (activity as VCheckMainActivity).closeSDKFlow(true)
            }
        }

        viewModel.stageSpecificError.observe(viewLifecycleOwner) {
            (requireActivity() as AppCompatActivity)
                .checkStageErrorForResult(it?.errorData?.errorCode, executePartnerCallback = true)
        }

        viewModel.clientError.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.errorData?.errorCode != BaseClientErrors.PRIMARY_DOCUMENT_EXISTS_OR_USER_INTERACTION_COMPLETED) {
                    Toast.makeText((activity as VCheckMainActivity), it.errorText, Toast.LENGTH_LONG).show()
                } else {
                    viewModel.getCurrentStage()
                }
            }
        }

        binding.checkInfoConfirmButton.setOnClickListener {
            if (checkIfAnyFieldIsNotValid()) {
                Toast.makeText((activity as VCheckMainActivity),
                    R.string.check_doc_fields_validation_error, Toast.LENGTH_LONG).show()
            } else {
                viewModel.updateAndConfirmDocument(uploadedDocID!!, composeConfirmedDocFieldsData())
            }
        }

        viewModel.getDocumentInfo(uploadedDocID!!)
    }

    private fun populateDocImages(data: PreProcessedDocData) {
        val apiPicasso = getPicassoForAPI()
        val baseURL = if (VCheckSDK.getEnvironment() == VCheckEnvironment.DEV)
            VCheckSDKConstantsProvider.DEV_VERIFICATIONS_SERVICE_URL
        else VCheckSDKConstantsProvider.PARTNER_VERIFICATIONS_SERVICE_URL

        if (data.images.isNotEmpty()) {
            binding.photoCard1.isVisible = true

            apiPicasso.load(baseURL + data.images[0]).fit().centerInside()
                .error(R.drawable.ic_baseline_error_24)
                .into(binding.passportImage1)

            if (data.images.size > 1) {
                binding.photoCard2.isVisible = true

                apiPicasso.load(baseURL + data.images[1]).fit().centerInside()
                    .error(R.drawable.ic_baseline_error_24)
                    .into(binding.passportImage2)
            }
        }
    }

    private fun populateDocFields(preProcessedDocData: PreProcessedDocData, currentLocaleCode: String) {
        if (preProcessedDocData.type.docFields.isNotEmpty()) {
            dataList = preProcessedDocData.type.docFields.map { docField ->
                convertDocFieldToOptParsedData(docField, preProcessedDocData.parsedData)
            } as ArrayList<DocFieldWitOptPreFilledData>
            val updatedAdapter = CheckDocInfoAdapter(ArrayList(dataList),
                this@CheckDocInfoFragment, currentLocaleCode)
            binding.docInfoList.adapter = updatedAdapter
        } else {
            Log.i(TAG, "No available auto-parsed fields")
        }
    }

    private fun checkIfAnyFieldIsNotValid(): Boolean {
        var hasValidationErrors: Boolean = false
        dataList.forEach {
            if (it.autoParsedValue.length < 2) {
                hasValidationErrors = true
            }
            if (!isFieldValid(it)) {
                hasValidationErrors = true
            }
        }
        return hasValidationErrors
    }

    override fun onFieldInfoEdited(fieldName: String, value: String) {
         dataList.find { it.name == fieldName }?.autoParsedValue = value
    }

    private fun composeConfirmedDocFieldsData() : DocUserDataRequestBody {
        val data = ParsedDocFieldsData()
        dataList.forEach { docField ->
            if (docField.name == "date_of_birth") {
                data.dateOfBirth = docField.autoParsedValue
            }
            if (docField.name == "name") {
                data.name = docField.autoParsedValue
            }
            if (docField.name == "surname") {
                data.surname = docField.autoParsedValue
            }
            if (docField.name == "number") {
                data.number = docField.autoParsedValue
            }
            if (docField.name == "expiration_date") {
                data.expirationDate = docField.autoParsedValue
            }
        }
        return DocUserDataRequestBody(data, args.checkDocInfoDataTO?.isForced ?: false)
    }

    private fun convertDocFieldToOptParsedData(docField: DocField, parsedDocFieldsData: ParsedDocFieldsData?) : DocFieldWitOptPreFilledData {
        var optParsedData = ""
        if (parsedDocFieldsData == null) {
           return DocFieldWitOptPreFilledData(
                docField.name, docField.title, docField.type, docField.regex, "")
        } else {
            if (docField.name == "date_of_birth" && parsedDocFieldsData.dateOfBirth != null) {
                optParsedData = parsedDocFieldsData.dateOfBirth!!
            }
            if (docField.name == "name" && parsedDocFieldsData.name != null) {
                optParsedData = parsedDocFieldsData.name!!
            }
            if (docField.name == "surname" && parsedDocFieldsData.surname != null) {
                optParsedData = parsedDocFieldsData.surname!!
            }
            if (docField.name == "number" && parsedDocFieldsData.number != null) {
                optParsedData = parsedDocFieldsData.number!!
            }
            if (docField.name == "expiration_date" && parsedDocFieldsData.number != null) {
                optParsedData = parsedDocFieldsData.expirationDate!!
            }
            return DocFieldWitOptPreFilledData(
                docField.name, docField.title, docField.type, docField.regex, optParsedData)
        }
    }

    private fun getPicassoForAPI() : Picasso {
        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", VCheckSDK.getVerificationToken())
                    .build()
                chain.proceed(newRequest)
            })
            .build()
        return Picasso.Builder(requireActivity())
            .downloader(OkHttp3Downloader(client))
            .build()
    }

    private fun isFieldValid(fieldInfo: DocFieldWitOptPreFilledData): Boolean {
        if (fieldInfo.regex != null
            && !fieldInfo.autoParsedValue.matches(Regex(fieldInfo.regex))) {
            return false
        } else {
            if ((fieldInfo.name == "date_of_birth" || fieldInfo.name == "expiration_date")
                && !isValidDocRelatedDate(fieldInfo.autoParsedValue)) {
                return false
            } else {
                if (fieldInfo.autoParsedValue.length < 3) {
                    return false
                }
            }
        }
        return true
    }
}