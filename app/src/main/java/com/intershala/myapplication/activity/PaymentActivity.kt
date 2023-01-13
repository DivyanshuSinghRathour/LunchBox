package com.intershala.myapplication.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.intershala.myapplication.R


class PaymentActivity : AppCompatActivity() {

    private lateinit var totalPaymentTV: TextView
    private lateinit var txtCashOnDelivery: TextView
    private lateinit var confirmPaymentBtn: Button
    private lateinit var cashPaymentRB: RadioButton

    var totalItemPrice = 0.0F
    var totalTaxPrice = 0.0F
    var subTotalPrice = 0.0F

    var takeAwayTime = ""

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_alert)
            .setTitle("Alert!")
            .setMessage("Do you want to cancel the payment?")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
                super.onBackPressed()
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.dismiss()
            })
            .create().show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        txtCashOnDelivery=findViewById(R.id.txtCashOnDelivery)

        totalItemPrice = intent.getFloatExtra("totalItemPrice", 0.0F)
        totalTaxPrice = intent.getFloatExtra("totalTaxPrice", 0.0F)
        subTotalPrice = intent.getFloatExtra("subTotalPrice", 0.0F)

        takeAwayTime = intent?.getStringExtra("takeAwayTime").toString()

        totalPaymentTV = findViewById(R.id.total_payment_tv)
        totalPaymentTV.text = "Rs%.2f".format(subTotalPrice)

        cashPaymentRB = findViewById(R.id.cash_payment_radio_btn)

        setupPaymentButtons()
        findViewById<ImageView>(R.id.payment_go_back_iv).setOnClickListener { onBackPressed() }
    }


    private fun setupPaymentButtons() {
        confirmPaymentBtn = findViewById(R.id.confirm_payment_btn)
        confirmPaymentBtn.setOnClickListener { orderDone() }

    }


    fun chooseModeOfPayment(view: View) {
        var payMethod = ""
        payMethod = if(view is RadioButton) {
            ((view.parent as LinearLayout).getChildAt(1) as TextView).text.toString()
        } else {
            (((view as CardView).getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString()
        }

        cashPaymentRB.isChecked = false
        when(payMethod) {
            getString(R.string.cash_payment) -> {
                cashPaymentRB.isChecked = true
                confirmPaymentBtn.visibility = ViewGroup.VISIBLE
            }
        }
    }

    private fun orderDone() {
        var paymentMethod = ""
        when {
            cashPaymentRB.isChecked -> paymentMethod = "Pending: Cash Payment"
            //netBankingRB.isChecked -> paymentMethod = "Paid: Net Banking"
        }
        if(cashPaymentRB.isChecked==true){
            val intent = Intent(this, OrderDoneActivity::class.java)
            intent.putExtra("totalItemPrice", totalItemPrice)
            intent.putExtra("totalTaxPrice", totalTaxPrice)
            intent.putExtra("subTotalPrice", subTotalPrice)
            intent.putExtra("takeAwayTime", takeAwayTime)
            intent.putExtra("paymentMethod", paymentMethod)

            startActivity(intent)
        }else
        {
            Toast.makeText(this,"Please choose payment method",Toast.LENGTH_SHORT).show()
        }

    }

}