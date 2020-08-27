package com.haerokim.project_footprint.Utility

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.haerokim.project_footprint.R

/**  로딩 다이얼로그 객체  **/

class LoadingDialog
    constructor(context: Context) : Dialog(context) {

    init{
        setCanceledOnTouchOutside(false)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_loading)
    }
}