package com.example.transporttrackingsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class RegisterBusFragment : Fragment() {

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_register_bus, container, false)
        db = FirebaseFirestore.getInstance()

        val etBusId = view.findViewById<TextInputEditText>(R.id.etBusId)
        val etBusNumber = view.findViewById<TextInputEditText>(R.id.etBusNumber)
        val etBusType = view.findViewById<TextInputEditText>(R.id.etBusType)
        val etRouteId = view.findViewById<TextInputEditText>(R.id.etRouteId)
        val etTerminal = view.findViewById<TextInputEditText>(R.id.etTerminal)
        val etCapacity = view.findViewById<TextInputEditText>(R.id.etCapacity)
        val etDriverName = view.findViewById<TextInputEditText>(R.id.etDriverName)
        val etDriverPhone = view.findViewById<TextInputEditText>(R.id.etDriverPhone)
        val btnRegister = view.findViewById<Button>(R.id.btnRegisterBus)

        btnRegister.setOnClickListener {
            val id = etBusId.text.toString().trim()
            val number = etBusNumber.text.toString().trim()
            val type = etBusType.text.toString().trim()
            val route = etRouteId.text.toString().trim()
            val terminal = etTerminal.text.toString().trim()
            val cap = etCapacity.text.toString().toIntOrNull() ?: 0
            val dName = etDriverName.text.toString().trim()
            val dPhone = etDriverPhone.text.toString().trim()

            if (id.isNotEmpty() && number.isNotEmpty()) {
                val bus = Bus(
                    busId = id,
                    busNumber = number,
                    busType = type,
                    routeId = route,
                    terminal = terminal,
                    capacity = cap,
                    driverName = if (dName.isEmpty()) "Unknown" else dName,
                    driverPhone = if (dPhone.isEmpty()) "N/A" else dPhone,
                    status = "Active",
                    createdAt = Timestamp.now()
                )
                db.collection("buses").document(id).set(bus).addOnSuccessListener {
                    Toast.makeText(context, "Bus Registered with Driver!", Toast.LENGTH_SHORT).show()
                    etBusId.text?.clear()
                    etBusNumber.text?.clear()
                    etBusType.text?.clear()
                    etRouteId.text?.clear()
                    etTerminal.text?.clear()
                    etCapacity.text?.clear()
                    etDriverName.text?.clear()
                    etDriverPhone.text?.clear()
                }
            }
        }

        return view
    }
}
