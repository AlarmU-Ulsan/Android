package com.alarmit.uou_alarm_it

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.alarmit.uou_alarm_it.databinding.DialogUpdateBinding

interface UpdateDialogInterface {
    fun onClickYes(url: String)
    fun onClickNo()
}

class UpdateDialog (
    updateDialogInterface: UpdateDialogInterface,
    url: String,
    lastVersion: String, thisVersion: String
): DialogFragment()
{
    private var _binding: DialogUpdateBinding? = null
    private val binding get() = _binding!!

    private var updateDialogInterface: UpdateDialogInterface? = null

    private var lastVersion: String? = null
    private var version: String? = null
    private var url: String? = null

    init {
        this.lastVersion = lastVersion
        this.version = version
        this.url = url
        this.updateDialogInterface = updateDialogInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogUpdateBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.dialogYesBtn.setOnClickListener {
            this.updateDialogInterface?.onClickYes(url!!)
            dismiss()
        }

        binding.dialogNoBtn.setOnClickListener {
            this.updateDialogInterface?.onClickNo()
            dismiss()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}