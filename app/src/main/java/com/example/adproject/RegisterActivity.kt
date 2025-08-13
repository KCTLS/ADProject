package com.example.adproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.adproject.api.ApiClient
import com.example.adproject.databinding.ActivityRegisterBinding
import com.example.adproject.model.*
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var vb: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(vb.root)

        // 性别下拉
        val genders = listOf("male", "female", "other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, genders)
        vb.dropGender.setAdapter(adapter)

        // 已有账号？去登录
        vb.linkToLogin.setOnClickListener { finish() }

        vb.btnRegister.setOnClickListener {
            val name  = vb.inputName.editText?.text?.toString()?.trim().orEmpty()
            val email = vb.inputEmail.editText?.text?.toString()?.trim().orEmpty()
            val pwd   = vb.inputPassword.editText?.text?.toString()?.trim().orEmpty()

            if (name.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
                toast("请填写完整信息"); return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast("邮箱格式不正确"); return@setOnClickListener
            }

            val address   = vb.inputAddress.editText?.text?.toString()?.trim()!!.ifBlank { "N/A" }
            val phone     = vb.inputPhone.editText?.text?.toString()?.trim()!!.ifBlank { "0000000000" }
            val gender    = vb.dropGender.text?.toString()?.trim()!!.ifBlank { "male" }
            val group     = vb.inputGroup.editText?.text?.toString()?.trim()!!.ifBlank { "default" }
            val title     = vb.inputTitle.editText?.text?.toString()?.trim()!!.ifBlank { "student" }
            val signature = vb.inputSignature.editText?.text?.toString()?.trim()!!.ifBlank { "" }
            val tagsInput = vb.inputTags.editText?.text?.toString()?.trim().orEmpty()
            val tags = if (tagsInput.isBlank()) emptyList()
            else tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            val req = RegisterRequest(
                address = address,
                email = email,
                gender = gender,
                group = group,
                name = name,
                password = pwd,
                phone = phone,
                signature = signature,
                tags = tags,
                title = title
            )

            setLoading(true)
            lifecycleScope.launch {
                try {
                    // 1) 调注册
                    val regResp = ApiClient.api.register(req)
                    if (!regResp.isSuccessful) {
                        toast("注册失败(${regResp.code()}): ${regResp.errorBody()?.string()?.take(200) ?: "无响应"}")
                        return@launch
                    }
                    val body = regResp.body()

                    // 后端拦截：未登录或会话失效（不改后端时，引导去登录）
                    if (body?.code == 0 && (body.msg?.contains("未登录") == true || body.msg?.contains("会话已失效") == true)) {
                        showLoginRequiredDialog(body.msg ?: "未登录或会话已失效")
                        return@launch
                    }

                    // 非成功码
                    if (body?.code != 5) {
                        toast(body?.msg ?: "注册失败")
                        return@launch
                    }

                    // 2) 注册成功后自动登录
                    val loginResp = ApiClient.api.login(LoginRequest(email, pwd))
                    if (!loginResp.isSuccessful) {
                        toast("已注册，但自动登录失败(${loginResp.code()})")
                        return@launch
                    }
                    val loginData = loginResp.body()
                    val ok = loginData?.status.equals("ok", true)
                            && loginData?.currentAuthority.equals("student", true)
                    if (!ok) {
                        val tip = loginData?.message ?: loginData?.msg ?: "已注册，但登录失败"
                        toast(tip); return@launch
                    }

                    // 3) 保存会话并进首页
                    val uid = (loginData?.userId ?: -1).toInt()
                    val uname = loginData?.userName ?: name
                    UserSession.save(this@RegisterActivity, uid, uname, email, loginData?.token)
                    toast("注册并登录成功")
                    startActivity(Intent(this@RegisterActivity, ExerciseActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                } catch (e: Exception) {
                    toast("网络异常：${e.message}")
                } finally {
                    setLoading(false)
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        vb.btnRegister.isEnabled = !loading
        vb.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showLoginRequiredDialog(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("需要登录")
            .setMessage("$message\n\n请先使用已有账号登录，再进行注册操作。")
            .setPositiveButton("去登录") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
