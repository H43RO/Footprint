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
import com.haerokim.project_footprint.DataClass.UpdateProfile
import com.haerokim.project_footprint.DataClass.User
import com.haerokim.project_footprint.Network.Website
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

/**
 *  사용자 프로필 정보 수정 기능 제공 (프로필 이미지, 회원 정보 등)
 *  - 현재 프로필 이미지는 Local 에만 저장하는 구조 (추후 마이그레이션 예정)
 **/

class EditProfileFragment : Fragment() {
    /**
     *  Android Image Cropper 라이브러리를 통해 이미지 선택 및 편집 완료 후 진입
     *  - imageUri 변수에 업로드 될 이미지의 Uri 값을 넣게 됨
     **/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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

    /**  Bitmap 이미지를 Local에 저장하고, URI를 반환함  **/
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

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()

        // API 호출을 위한 Retrofit 객체 생성
        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        var updateUserInfoService: RetrofitService =
            retrofit.create(RetrofitService::class.java)

        image_edit_user_profile.setBackground(ShapeDrawable(OvalShape()))
        image_edit_user_profile.setClipToOutline(true)

        // 저장된 프로필 이미지 로드
        val pref: SharedPreferences? = context?.getSharedPreferences("profile_image", Activity.MODE_PRIVATE)
        val profileImageUri = Uri.parse(pref?.getString("profile_image", ""))

        if(profileImageUri.toString() != ""){
            image_edit_user_profile.setImageURI(profileImageUri)
        }else{
            image_edit_user_profile.setImageResource(R.drawable.basic_profile)
        }

        // 저장된 프로필 정보 로드
        val user: User = Paper.book().read("user_profile")

        // 회원 생년월일 Date to String
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
            // 기본 이미지로 변경할 건지, 갤러리 사진으로 변경할 것인지 선택할 수 있는 Popup Menu
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

        // 회원 프로필 정보 수정 취소 (정보 원상 복구)
        revert_edit_profile.setOnClickListener {
            edit_profile_nickname.setText(user.nickname)
            edit_profile_email.setText(user.email)
            edit_profile_birth_date.setText(userBirthDateString)
            edit_profile_age.setText(user.age.toString())
        }


        submit_edit_profile.setOnClickListener {
            // 회원이 입력한 생년월일 String to Date (API 대응 시 필요)
            val updateBirthDateString: String = edit_profile_birth_date.text.toString()
            val updateBirthDate: Date = userBirthDateFormat.parse(updateBirthDateString)

            val updateProfile = UpdateProfile(
                edit_profile_nickname.text.toString(),
                updateBirthDate,
                Integer.parseInt(edit_profile_age.text.toString()),
                user.gender
            )

            // 회원 정보 수정 API 호출
            updateUserInfoService.updateUserInfo(user.id, updateProfile)
                .enqueue(object : Callback<UpdateProfile> {
                    override fun onFailure(call: Call<UpdateProfile>, t: Throwable) {
                        Log.e("Update Profile", t.message)
                    }

                    override fun onResponse(call: Call<UpdateProfile>, response: Response<UpdateProfile>) {
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

                        // Paper 저장 데이터 변경 (ID, Email, Token은 바뀌지 않음)
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
                    // 저장되어있는 로그인 정보, 회원 정보 모두 삭제
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
            //TODO("구현 예정")
        }

        // 회원 탙퇴 버튼
        button_withdraw.setOnClickListener {
            startActivity(Intent(requireContext(), WithdrawActivity::class.java))
        }
    }
}