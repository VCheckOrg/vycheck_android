package com.vcheck.sdk.core.presentation.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.ListItemProviderBinding
import com.vcheck.sdk.core.domain.Provider

class ProvidersListAdapter(private val providersList: List<Provider>,
                           private val onProviderItemClick: OnProviderItemClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var providerListItemBinding: ListItemProviderBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        providerListItemBinding = ListItemProviderBinding.inflate(layoutInflater, parent, false)
        return ProviderViewHolder(providerListItemBinding, onProviderItemClick)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val provider = providersList[position]
        (holder as ProviderViewHolder).bind(provider)
    }

    override fun getItemCount(): Int = providersList.size

    class ProviderViewHolder(
        private val binding: ListItemProviderBinding,
        private val onProviderItemClick: OnProviderItemClick,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(provider: Provider) {

            if (provider.protocol != "vcheck") {
                binding.verifMethodTitleVcheck.text = provider.name.replaceFirstChar { it.uppercaseChar() }
                binding.verifMethodSubtitleVcheck.text = ""
                // other fields are not customizable yet
            }

            VCheckSDK.designConfig!!.backgroundTertiaryColorHex?.let {
                binding.cardVcheckBackground.setCardBackgroundColor(Color.parseColor(it))
            }
            VCheckSDK.designConfig!!.primaryTextColorHex?.let {
                binding.verifMethodTitleVcheck.setTextColor(Color.parseColor(it))
            }
            VCheckSDK.designConfig!!.secondaryTextColorHex?.let {
                binding.verifMethodSubtitleVcheck.setTextColor(Color.parseColor(it))
            }
            VCheckSDK.designConfig!!.sectionBorderColorHex?.let {
                binding.methodCardVcheck.setCardBackgroundColor(Color.parseColor(it))
            }

            binding.cardVcheckBackground.setOnClickListener {
                onProviderItemClick.onClick(provider)
            }
        }
    }

    interface OnProviderItemClick {
        fun onClick(provider: Provider)
    }
}