package topgrade.parent.com.parentseeks.Parent.Activity

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import topgrade.parent.com.parentseeks.R

class BiometricCredentialsDialog : DialogFragment() {
    
    private var onCredentialsEnteredListener: ((String, String, String) -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_biometric_credentials, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val emailEditText = view.findViewById<EditText>(R.id.email_edit_text)
        val passwordEditText = view.findViewById<EditText>(R.id.password_edit_text)
        val campusIdEditText = view.findViewById<EditText>(R.id.campus_id_edit_text)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)
        
        confirmButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val campusId = campusIdEditText.text.toString().trim()
            
            if (email.isEmpty()) {
                emailEditText.error = "Email is required"
                return@setOnClickListener
            }
            
            if (password.isEmpty()) {
                passwordEditText.error = "Password is required"
                return@setOnClickListener
            }
            
            if (campusId.isEmpty()) {
                campusIdEditText.error = "Campus ID is required"
                return@setOnClickListener
            }
            
            onCredentialsEnteredListener?.invoke(email, password, campusId)
            dismiss()
        }
        
        cancelButton.setOnClickListener {
            dismiss()
        }
    }
    
    fun setOnCredentialsEnteredListener(listener: (String, String, String) -> Unit) {
        onCredentialsEnteredListener = listener
    }
} 