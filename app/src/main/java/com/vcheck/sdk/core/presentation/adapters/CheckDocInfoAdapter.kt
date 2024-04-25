package com.vcheck.sdk.core.presentation.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.RowDocInfoFieldBinding
import com.vcheck.sdk.core.domain.DocFieldWitOptPreFilledData
import com.vcheck.sdk.core.presentation.doc_check.CheckDocInfoFragment
import com.vcheck.sdk.core.util.utils.isValidDocRelatedDate
import java.util.*


class CheckDocInfoAdapter(
    private val documentInfoList: ArrayList<DocFieldWitOptPreFilledData>,
    private val docInfoEditCallback: DocInfoEditCallback,
    private val currentLocaleCode: String
) :
    RecyclerView.Adapter<CheckDocInfoAdapter.ViewHolder>() {

    private lateinit var binding: RowDocInfoFieldBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = RowDocInfoFieldBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, docInfoEditCallback, currentLocaleCode)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val documentInfo = documentInfoList[position]
        holder.bind(documentInfo)
    }

    override fun getItemCount(): Int = documentInfoList.size

    class ViewHolder(
        private val binding: RowDocInfoFieldBinding,
        private val docInfoEditCallback: DocInfoEditCallback,
        private val localeCode: String)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(fieldInfo: DocFieldWitOptPreFilledData) {

            val title = when (localeCode) {
                "uk" -> fieldInfo.title.ua ?: fieldInfo.title.ua
                "ru" -> fieldInfo.title.ru ?: fieldInfo.title.ru
                "pl" -> fieldInfo.title.pl ?: fieldInfo.title.pl
                else -> fieldInfo.title.en
            }
            binding.docFieldTitle.text = title
            binding.infoField.setText(fieldInfo.autoParsedValue)

            VCheckSDK.designConfig!!.backgroundSecondaryColorHex?.let {
                binding.docInfoRowBackground.background = ColorDrawable(Color.parseColor(it))
                binding.infoField.background = ColorDrawable(Color.parseColor(it))
            }
            VCheckSDK.designConfig!!.primaryTextColorHex?.let {
                binding.docFieldTitle.setTextColor(Color.parseColor(it))
                binding.infoField.setTextColor(Color.parseColor(it))
                binding.infoField.setHintTextColor(Color.parseColor(it))
            }
            VCheckSDK.designConfig!!.sectionBorderColorHex?.let {
                binding.infoFieldBorder.setCardBackgroundColor(Color.parseColor(it))
            }

            if ((fieldInfo.name == "date_of_birth" || fieldInfo.name == "expiration_date")) {

                val hint = when (localeCode) {
                    "uk" -> "РРРР-ММ-ДД"
                    "ru" -> "ГГГГ-ММ-ДД"
                    "pl" -> "RRRR-MM-DD"
                    else -> "YYYY-MM-DD"
                }

                binding.infoField.hint = hint
                binding.infoField.inputType = InputType.TYPE_CLASS_NUMBER

            } else {
                binding.infoField.hint = ""
            }

            binding.infoField.addTextChangedListener(object : TextWatcher {

                private var processedText = ""
                private val yyyymmdd = (docInfoEditCallback as CheckDocInfoFragment).getString(
                    R.string.yyyymmdd)
                private val cal = Calendar.getInstance()

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(
                    text: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int) {

                    if (fieldInfo.name == "date_of_birth"
                        || fieldInfo.name == "expiration_date") {
                        processDateInput(text)
                    }

                    if (text.isNotEmpty()) {
                        if (fieldInfo.regex != null
                            && !text.matches(Regex(fieldInfo.regex))) {
                            binding.infoField.error =
                                (docInfoEditCallback as CheckDocInfoFragment).getString(
                                    R.string.number_of_document_validation_error)
                        } else {
                            if (fieldInfo.name == "date_of_birth" && !isValidDocRelatedDate(processedText)) {
                                binding.infoField.error = (docInfoEditCallback as CheckDocInfoFragment).getString(
                                        R.string.date_of_birth_validation_error)
                            }
                            if (fieldInfo.name == "expiration_date" && !isValidDocRelatedDate(processedText)) {
                                binding.infoField.error = (docInfoEditCallback as CheckDocInfoFragment).getString(
                                        R.string.enter_valid_exp_date)
                            }
                            if (fieldInfo.name != "date_of_birth" && fieldInfo.name != "expiration_date") {
                                if (text.length < 3) {
                                    when (fieldInfo.name) {
                                        "Surname (cyrillic)" -> {
                                            binding.infoField.error =
                                                (docInfoEditCallback as CheckDocInfoFragment).getString(
                                                    R.string.surname_cyrillic_validation_error)
                                        }
                                        "Surname (latin)" -> {
                                            binding.infoField.error =
                                                (docInfoEditCallback as CheckDocInfoFragment).getString(
                                                    R.string.surname_latin_validation_error)
                                        }
                                        "Name (cyrillic)" -> {
                                            binding.infoField.error =
                                                (docInfoEditCallback as CheckDocInfoFragment).getString(
                                                    R.string.name_cyrillic_validation_error)
                                        }
                                        "Name (latin)" -> {
                                            binding.infoField.error =
                                                (docInfoEditCallback as CheckDocInfoFragment).getString(
                                                    R.string.name_latin_validation_error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (fieldInfo.name == "date_of_birth" || fieldInfo.name == "expiration_date") {
                        docInfoEditCallback.onFieldInfoEdited(fieldInfo.name, processedText)
                    } else {
                        docInfoEditCallback.onFieldInfoEdited(fieldInfo.name, text.toString())
                    }

                }

                private fun processDateInput(text: CharSequence) {
                    try {
                        if (text.toString() != processedText) {
                            var cleanString =
                                text.toString().replace("[^\\d.]|\\.".toRegex(), "")
                            val cleanCurrent = processedText.replace("[^\\d.]|\\.".toRegex(), "")
                            var selection = cleanString.length
                            if (cleanString.length == 4) {
                                selection++
                            }
                            if (cleanString.length == 5) {
                                selection++
                            }
                            if (cleanString.length == 6) {
                                selection += 2
                            }
                            if (cleanString.length == 7) {
                                selection += 2
                            }
                            if (cleanString.length >= 8) {
                                selection += 2
                            }
                            if (cleanString == cleanCurrent) {
                                selection--
                            }
                            if (cleanString.length < 8) {
                                cleanString += yyyymmdd.substring(cleanString.length)
                            } else {
                                var day = cleanString.substring(6, 8).toInt()
                                var mon = cleanString.substring(4, 6).toInt()
                                var year = cleanString.substring(0, 4).toInt()
                                mon = if (mon < 1) 1 else if (mon > 12) 12 else mon
                                cal[Calendar.MONTH] = mon - 1
                                year =
                                    if (year < 1900) 1900 else if (year > 2100) 2100 else year
                                cal[Calendar.YEAR] = year

                                day =
                                    if (day > cal.getActualMaximum(Calendar.DATE)) cal.getActualMaximum(
                                        Calendar.DATE
                                    ) else day
                                cleanString = String.format("%02d%02d%02d", year, mon, day)
                            }
                            cleanString = String.format(
                                "%s-%s-%s",
                                cleanString.substring(0, 4),
                                cleanString.substring(4, 6),
                                cleanString.substring(6, 8)
                            )
                            selection = if (selection < 0) 0 else selection
                            processedText = cleanString
                            binding.infoField.setText(processedText)
                            binding.infoField.setSelection(if (selection >= 10) 10 else selection)
                        }

                    } catch (e: Exception) {
                        Log.d(VCheckSDK.TAG, e.message ?: "caught onTextChanged error")
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                    //Stub
                }
            })
        }
    }
}