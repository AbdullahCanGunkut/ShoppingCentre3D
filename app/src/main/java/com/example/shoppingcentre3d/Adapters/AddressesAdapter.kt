package com.example.shoppingcentre3d.Adapters

import  android.view.*
import  androidx.recyclerview.widget.RecyclerView
import com.example.shoppingcentre3d.ModelClasses.*
import com.google.android.material.textfield.TextInputEditText


/**
 * Adres bilgilerini tutan RecycleView için kullanacağımız bir adapter.
 *
 * */
class AddressesAdapter :
    RecyclerView.Adapter<AddressViewHolder> {

    var userInformation : User

    constructor(user : User) {
        this.userInformation = user
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): AddressViewHolder {

        val view = LayoutInflater.from(viewGroup.context)
            .inflate(
                com.example.shoppingcentre3d.R.layout.activity_user_settings_address_card,
                viewGroup,
                false
            )
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {

        var address = this.userInformation.Addresses[position]
        holder.address.setText(address.address)
        holder.titleName.setText(address.title)
        holder.name.setText(address.name)
        holder.surname.setText(address.surname)
        holder.telephone.setText(address.telephone)
        }

    override fun getItemCount() = this.userInformation.Addresses.size

}


class AddressViewHolder : RecyclerView.ViewHolder {

    val titleName : TextInputEditText
    val name : TextInputEditText
    val surname : TextInputEditText
    val address : TextInputEditText
    val telephone : TextInputEditText
    constructor(itemView: View) : super(itemView) {
        this.titleName = itemView.findViewById(com.example.shoppingcentre3d.R.id.addressTitle)
        this.name = itemView.findViewById(com.example.shoppingcentre3d.R.id.name)
        this.surname = itemView.findViewById(com.example.shoppingcentre3d.R.id.surname)
        this.address =itemView.findViewById(com.example.shoppingcentre3d.R.id.addressText)
        this.telephone = itemView.findViewById(com.example.shoppingcentre3d.R.id.telephone)
    }

}