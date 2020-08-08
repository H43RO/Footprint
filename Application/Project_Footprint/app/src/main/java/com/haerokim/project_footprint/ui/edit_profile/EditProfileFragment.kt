package com.haerokim.project_footprint.ui.edit_profile

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.Activity.LoginActivity
import com.haerokim.project_footprint.Activity.WithdrawActivity
import com.haerokim.project_footprint.Data.UpdateProfile
import com.haerokim.project_footprint.Data.User
import com.haerokim.project_footprint.Data.Website
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.R
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import io.paperdb.Paper
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class EditProfileFragment : Fragment() {
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //프로필 사진 변경을 위한 사진이 선택 및 편집되면 Uri 형태로 결과가 반환됨
        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode === RESULT_OK) {
                val resultUri = result.uri
                image_edit_user_profile.setImageURI(resultUri)

                val bitmap =
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, resultUri)

                val uri = bitmapToFile(bitmap!!) // Uri
                val pref = activity?.getSharedPreferences("profile_image", Activity.MODE_PRIVATE)
                val editor: SharedPreferences.Editor? = pref?.edit()
                editor?.putString("profile_image", uri.toString())
                editor?.commit()

            } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            }
        }
    }

    private fun bitmapToFile(bitmap:Bitmap): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(context)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file,"profile_image.jpg")
        try{
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
            Log.e("Error Saving Image", e.message)
        }
        return Uri.parse(file.absolutePath)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Paper.init(context)
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image_edit_user_profile.setBackground(ShapeDrawable(OvalShape()))
        image_edit_user_profile.setClipToOutline(true)

        val pref: SharedPreferences? = context?.getSharedPreferences("profile_image", Activity.MODE_PRIVATE)
        val profileImageUri = Uri.parse(pref?.getString("profile_image", ""))

        if(profileImageUri.toString() != ""){
            image_edit_user_profile.setImageURI(profileImageUri)
        }else{
            image_edit_user_profile.setImageResource(R.drawable.basic_profile)
        }

        val user: User = Paper.book().read("user_profile")

        //Date to String
        val userBirthDate = user.birthDate
        val userBirthDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val userBirthDateString: String = userBirthDateFormat.format(userBirthDate)

        edit_profile_nickname.setText(user.nickname)
        edit_profile_email.setText(user.email)
        edit_profile_birth_date.setText(userBirthDateString)
        edit_profile_age.setText(user.age.toString())

        // 프로필 사진 변경
        button_change_profile_image.setOnClickListener {
           val popup: PopupMenu = PopupMenu(context, it)
            popup.inflate(R.menu.profile_image_menu)
            // 기본 이미지로 변경할 건지, 갤러리 및 촬영 사진으로 변경할 것인
            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                when(it.itemId){
                    R.id.basic->{
                        pref?.edit()?.remove("profile_image")?.commit()
                        image_edit_user_profile.setImageResource(R.drawable.basic_profile)
                    }
                    R.id.galery->{
                        CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setActivityTitle("프로필 사진 변경")
                            .setCropShape(CropImageView.CropShape.OVAL)
                            .setCropMenuCropButtonTitle("완료")
                            .setAspectRatio(1, 1)
                            .setRequestedSize(400,400)
                            .start(requireContext(), this)
                    }
                }
                true
            })
            popup.show()
        }

        // 회원 프로필 정보 수정 취소
        revert_edit_profile.setOnClickListener {
            edit_profile_nickname.setText(user.nickname)
            edit_profile_email.setText(user.email)
            edit_profile_birth_date.setText(userBirthDateString)
            edit_profile_age.setText(user.age.toString())
        }

        // 회원 프로필 정보 수정
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()

        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        var updateUserInfoService: RetrofitService =
            retrofit.create(RetrofitService::class.java)

        submit_edit_profile.setOnClickListener {
            // String to Date
            val updateBirthDateString: String = edit_profile_birth_date.text.toString()
            val updateBirthDate: Date = userBirthDateFormat.parse(updateBirthDateString)

            val updateProfile = UpdateProfile(
                edit_profile_nickname.text.toString(),
                updateBirthDate,
                Integer.parseInt(edit_profile_age.text.toString()),
                user.gender
            )

            // Retrofit API Request
            updateUserInfoService.updateUserInfo(
                user.id,
                updateProfile
            )
                .enqueue(object : Callback<UpdateProfile> {
                    override fun onFailure(call: Call<UpdateProfile>, t: Throwable) {
                        Log.e("Update Profile", t.message)
                    }

                    override fun onResponse(
                        call: Call<UpdateProfile>,
                        response: Response<UpdateProfile>
                    ) {
                        // Paper Overwrite (ID, Email, Token은 바뀌지 않음)
                        val updateUserProfile: UpdateProfile? = response.body()
                        val newUserProfile: User = User(
                            user.id,
                            user.email,
                            updateUserProfile!!.birthDate,
                            updateUserProfile.nickname,
                            updateUserProfile.age,
                            updateUserProfile.gender,
                            user.token
                        )

                        Paper.book().write("user_profile", newUserProfile)
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), "회원 정보 변경 완료!", Snackbar.LENGTH_LONG).show()
                    }
                })
        }

        // 로그아웃 버튼
        button_logout.setOnClickListener {
            val builder: AlertDialog.Builder =
                AlertDialog.Builder(context)
            builder.setTitle("로그아웃")
            builder.setMessage("로그아웃하시겠습니까?")
            builder.setPositiveButton("예",
                DialogInterface.OnClickListener { dialog, which ->
                    // 로그인 정보, 회원 정보 모두 삭제
                    Paper.book().delete("user_profile")
                    val autoLogin = context?.getSharedPreferences("auto_login", Activity.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = autoLogin!!.edit()
                    editor.remove("auto_login_enable")
                    editor.commit()
                    startActivity(Intent(context, LoginActivity::class.java))
                })
            builder.setNegativeButton("아니오",
                DialogInterface.OnClickListener { dialog, which ->
                })
            val alertDialog = builder.create()
            alertDialog.show()
            val view: ViewGroup.MarginLayoutParams =
                alertDialog.getButton(Dialog.BUTTON_POSITIVE).layoutParams as ViewGroup.MarginLayoutParams
            view.leftMargin = 16
            alertDialog.getButton(Dialog.BUTTON_NEGATIVE)
                .setBackgroundColor(Color.parseColor("#e8e8e8"))
            alertDialog.getButton(Dialog.BUTTON_NEGATIVE)
                .setTextColor(Color.parseColor("#000000"))

        }

        // 비밀번호 변경 버튼
        button_modify_password.setOnClickListener {

        }

        // 회원 탙퇴 버튼
        button_withdraw.setOnClickListener {
            startActivity(Intent(requireContext(), WithdrawActivity::class.java))
        }
    }
}