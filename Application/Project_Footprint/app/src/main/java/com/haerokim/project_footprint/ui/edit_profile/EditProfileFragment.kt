package com.haerokim.project_footprint.ui.edit_profile

import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.Data.UpdateProfile
import com.haerokim.project_footprint.Data.User
import com.haerokim.project_footprint.Data.Website
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.R
import io.paperdb.Paper
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_DATE
import java.util.*


class EditProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image_edit_user_profile.setBackground(ShapeDrawable(OvalShape()))
        image_edit_user_profile.setClipToOutline(true)

        val user: User = Paper.book().read("user_profile")

        //Date to String
        val userBirthDate = user.birthDate
        val userBirthDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val userBirthDateString: String = userBirthDateFormat.format(userBirthDate)

        edit_profile_nickname.setText(user.nickname)
        edit_profile_email.setText(user.email)
        edit_profile_birth_date.setText(userBirthDateString)
        edit_profile_age.setText(user.age.toString())

        revert_edit_profile.setOnClickListener {
            edit_profile_nickname.setText(user.nickname)
            edit_profile_email.setText(user.email)
            edit_profile_birth_date.setText(userBirthDateString)
            edit_profile_age.setText(user.age.toString())
        }

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()

        var retrofit = Retrofit.Builder()
            .baseUrl(Website.baseUrl) //사이트 Base URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        var updateUserInfoService: RetrofitService =
            retrofit.create(RetrofitService::class.java)

        submit_edit_profile.setOnClickListener {

            //String to Date
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
    }
}