package com.example.sendbirdplatformsdk

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.openapitools.client.model.CreateAUserRequest
import org.openapitools.client.model.ListUsersResponse
import org.sendbird.client.ApiClient
import org.sendbird.client.ApiException
import org.sendbird.client.api.UserApi
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private lateinit var tv: TextView
    private lateinit var btnLoadUsers: Button
    private lateinit var btnCreateUser: Button
    private lateinit var etUserId: EditText
    private lateinit var etNickname: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv = findViewById(R.id.sampleText)
        btnLoadUsers = findViewById(R.id.btnLoadUsers)
        btnCreateUser = findViewById(R.id.btnCreateUser)
        etUserId = findViewById(R.id.etUserId)
        etNickname = findViewById(R.id.etNickname)

        // 1) 유저 조회 버튼
        btnLoadUsers.setOnClickListener {
            tv.text = "Loading users..."
            setButtonsEnabled(false)

            executor.execute {
                try {
                    val client: ApiClient = SendbirdClient.create()
                    val userApi = UserApi(client)

                    val result: ListUsersResponse = userApi.listUsers()
                        .limit(20)
                        .activeMode("activated")
                        .showBot(true)
                        .execute()

                    val text = buildUserListText(result)

                    runOnUiThread {
                        tv.text = text
                        setButtonsEnabled(true)
                    }
                } catch (e: ApiException) {
                    runOnUiThread {
                        tv.text = buildApiErrorText(e)
                        setButtonsEnabled(true)
                    }
                } catch (t: Throwable) {
                    runOnUiThread {
                        tv.text = "Unexpected error: ${t.message}"
                        setButtonsEnabled(true)
                    }
                }
            }
        }

        // 2) 유저 생성 버튼
        btnCreateUser.setOnClickListener {
            val userId = etUserId.text.toString().trim()
            val nickname = etNickname.text.toString().trim()

            if (userId.isEmpty()) {
                tv.text = "User ID is required."
                return@setOnClickListener
            }

            tv.text = "Creating user..."
            setButtonsEnabled(false)

            executor.execute {
                try {

                    val client: ApiClient = SendbirdClient.create()
                    val userApi = UserApi(client)

                    val req = CreateAUserRequest().apply {
                        setUserId(userId)
//                        setMetadata(buildHardcodedMetadata())
                        if (nickname.isNotEmpty()) setNickname(nickname)
                    }

                    val createdUser = userApi.createAUser()
                        .createAUserRequest(req)
                        .execute()

                    runOnUiThread {
                        tv.text = """
                            ✅ User created successfully!!!

                            userId: ${createdUser.userId}
                            nickname: ${createdUser.nickname}
                            isActive: ${createdUser.isActive}
                            metadata: ${createdUser.metadata}
                        """.trimIndent()
                        setButtonsEnabled(true)
                    }

                } catch (e: ApiException) {
                    runOnUiThread {
                        tv.text = buildApiErrorText(e)
                        setButtonsEnabled(true)
                    }
                } catch (t: Throwable) {
                    runOnUiThread {
                        tv.text = "Unexpected error: ${t.message}"
                        setButtonsEnabled(true)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        btnLoadUsers.isEnabled = enabled
        btnCreateUser.isEnabled = enabled
    }

    private fun buildApiErrorText(e: ApiException): String {
        return """
            ❌ API Error
            Status code: ${e.code}
            Reason: ${e.responseBody}
        """.trimIndent()
    }

    private fun buildUserListText(result: ListUsersResponse): String {
        val users = result.users ?: emptyList()
        if (users.isEmpty()) return "No users found."

        return buildString {
            appendLine("✅ Users (${users.size})")
            appendLine()
            users.forEachIndexed { index, user ->
                appendLine("${index + 1}. userId = ${user.userId}")
                appendLine("   nickname = ${user.nickname}")
                appendLine("   isActive = ${user.isActive}")
                appendLine("   isOnline = ${user.isOnline}")
                appendLine()
            }
        }
    }

    private fun buildHardcodedMetadata(): Map<String, String> {
        return mapOf(
            "companyCode" to "sales",

            // 배열 → JSON 문자열
            "commodities" to """
            [
              "AGGREGATE",
              "AGRICULTURE",
              "AP",
              "BROWN",
              "BULK TRASH",
              "CONCRETE",
              "CRUDE",
              "DAIRY",
              "FUEL",
              "LNG",
              "MILK",
              "PIPE",
              "SAND",
              "SERVICE WORK",
              "SMART CHAIN",
              "SUPPLEMENTAL",
              "WATER",
              "YARD MGT"
            ]
        """.trimIndent(),

            "type" to "Driver",

            "terminals" to """
            [
              "LUBBOCK",
              "VICTORIA YARD",
              "FORT WORTH",
              "SOUTH SIDE"
            ]
        """.trimIndent()
        )
    }
}
