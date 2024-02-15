package com.example.shoppingcentre3d.Adapters

import android.util.Log
import  android.view.*
import  androidx.recyclerview.widget.RecyclerView
import com.example.shoppingcentre3d.ModelClasses.*
import com.google.android.material.textfield.TextInputEditText



/**
 * Banka kartı için olan  ve banka kartlarını tutan RecycleView için kullanacağımız bir adapter.
 * */

class BankCardsAdapter :
    RecyclerView.Adapter<BankCardViewHolder> {

    var userInformation : User

    constructor(user : User) {
        this.userInformation = user
    }


    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): BankCardViewHolder{

        val view = LayoutInflater.from(viewGroup.context)
            .inflate(
                com.example.shoppingcentre3d.R.layout.activity_user_settings_bank_card,
                viewGroup,
                false
            )
        return BankCardViewHolder(view)
    }
    override fun onBindViewHolder(holder: BankCardViewHolder , position: Int) {

        var card = this.userInformation.Cards[position]
        holder.name.setText(card.name)
        holder.surname.setText(card.surname)
        holder.cardNumber.setText(card.cardNumber)
        holder.cvv.setText(card.cvv)
        holder.validityDateMonth.setText(card.validityDateMonth)
        holder.validityDateYear.setText(card.validityDateYear)

    }
    override fun getItemCount() = this.userInformation.Cards.size

}


class BankCardViewHolder : RecyclerView.ViewHolder {

    var name: TextInputEditText
    var surname: TextInputEditText
    var cardNumber: TextInputEditText
    var cvv: TextInputEditText
    var validityDateMonth: TextInputEditText
    var validityDateYear: TextInputEditText

    constructor(itemView: View) : super(itemView) {
        this.name = itemView.findViewById(com.example.shoppingcentre3d.R.id.bankCardName)
        this.surname = itemView.findViewById(com.example.shoppingcentre3d.R.id.bankcardSurname)
        this.cardNumber = itemView.findViewById(com.example.shoppingcentre3d.R.id.cardNumber)
        this.cvv = itemView.findViewById(com.example.shoppingcentre3d.R.id.securityCode)
        this.validityDateMonth = itemView.findViewById(com.example.shoppingcentre3d.R.id.dateMonth)
        this.validityDateYear = itemView.findViewById(com.example.shoppingcentre3d.R.id.dateYear)

    }

}