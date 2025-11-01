package topgrade.parent.com.parentseeks.Parent.Activity

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.fragment.app.DialogFragment
import topgrade.parent.com.parentseeks.R

class BiometricSetupPopupDialog : DialogFragment() {
    
    private var onEnableClickListener: (() -> Unit)? = null
    private var onNotNowClickListener: (() -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_biometric_setup_popup, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val enableButton = view.findViewById<Button>(R.id.enable_button)
        val notNowButton = view.findViewById<Button>(R.id.not_now_button)
        
        enableButton.setOnClickListener {
            onEnableClickListener?.invoke()
            dismiss()
        }
        
        notNowButton.setOnClickListener {
            onNotNowClickListener?.invoke()
            dismiss()
        }
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
    
    fun setOnEnableClickListener(listener: () -> Unit) {
        onEnableClickListener = listener
    }
    
    fun setOnNotNowClickListener(listener: () -> Unit) {
        onNotNowClickListener = listener
    }
    
    companion object {
        fun show(
            context: Context,
            onEnable: () -> Unit,
            onNotNow: () -> Unit
        ) {
            val dialog = BiometricSetupPopupDialog()
            dialog.setOnEnableClickListener(onEnable)
            dialog.setOnNotNowClickListener(onNotNow)
            
            if (context is androidx.fragment.app.FragmentActivity) {
                dialog.show(context.supportFragmentManager, "BiometricSetupPopup")
            }
        }
    }
} 