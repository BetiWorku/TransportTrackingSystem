package com.example.transporttrackingsystem.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailHelper {

    private const val SENDER_EMAIL = "bwwmas@gmail.com"
    private const val APP_PASSWORD = "SFNDSASPSHLYPEHZ"

    // ─── Send OTP email ────────────────────────────────────────────────────
    fun sendOTPEmail(context: Context, userEmail: String, userName: String, otpCode: String) {
        Thread {
            try {
                Log.d("EMAIL", "=== SENDING OTP EMAIL ===")
                Log.d("EMAIL", "To: $userEmail, Code: $otpCode")

                val props = Properties()
                props["mail.smtp.host"] = "smtp.gmail.com"
                props["mail.smtp.port"] = "465"
                props["mail.smtp.auth"] = "true"
                props["mail.smtp.socketFactory.port"] = "465"
                props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
                props["mail.smtp.socketFactory.fallback"] = "false"
                props["mail.smtp.connectiontimeout"] = "30000"
                props["mail.smtp.timeout"] = "30000"

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD)
                    }
                })

                val message = MimeMessage(session)
                message.setFrom(InternetAddress(SENDER_EMAIL, "Addis Bus Tracker"))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail))
                message.subject = "Your Addis Bus Tracker Verification Code"
                message.setContent(
                    """
                    <div style="font-family:Arial,sans-serif;max-width:500px;margin:auto;padding:30px;border:1px solid #ddd;border-radius:10px;">
                        <h2 style="color:#3F51B5;text-align:center;">Addis Bus Tracker</h2>
                        <p>Hello <b>$userName</b>,</p>
                        <p>Your verification code is:</p>
                        <div style="text-align:center;margin:20px 0;">
                            <span style="font-size:36px;font-weight:bold;letter-spacing:8px;color:#3F51B5;background:#f0f2f9;padding:10px 30px;border-radius:8px;border:2px dashed #3F51B5;">$otpCode</span>
                        </div>
                        <p style="color:#777;font-size:13px;">This code is valid for 15 minutes. Do not share it.</p>
                        <hr style="border:none;border-top:1px solid #eee;margin:20px 0;">
                        <p style="color:#999;font-size:12px;text-align:center;">Addis Bus Tracker Team</p>
                    </div>
                    """.trimIndent(),
                    "text/html; charset=utf-8"
                )

                Transport.send(message)
                Log.d("EMAIL", "=== OTP EMAIL SENT SUCCESSFULLY ===")

                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, "✅ Code sent to $userEmail!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("EMAIL", "=== OTP EMAIL FAILED: ${e.message} ===", e)
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, "❌ Email failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    // ─── Send Welcome email ────────────────────────────────────────────────
    fun sendWelcomeEmail(context: Context, userEmail: String, userName: String) {
        Thread {
            try {
                val props = Properties()
                props["mail.smtp.host"] = "smtp.gmail.com"
                props["mail.smtp.port"] = "465"
                props["mail.smtp.auth"] = "true"
                props["mail.smtp.socketFactory.port"] = "465"
                props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
                props["mail.smtp.socketFactory.fallback"] = "false"

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD)
                    }
                })

                val message = MimeMessage(session)
                message.setFrom(InternetAddress(SENDER_EMAIL, "Addis Bus Tracker"))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail))
                message.subject = "Welcome to Addis Bus Tracker"
                message.setContent(
                    """
                    <div style="font-family:Arial,sans-serif;max-width:500px;margin:auto;padding:30px;">
                        <h2 style="color:#3F51B5;">Welcome, $userName!</h2>
                        <p>Your account is ready. Start tracking your bus today!</p>
                        <p style="color:#999;font-size:12px;">- Addis Bus Tracker Team</p>
                    </div>
                    """.trimIndent(),
                    "text/html; charset=utf-8"
                )

                Transport.send(message)
                Log.d("EMAIL", "✅ Welcome email sent to $userEmail")
            } catch (e: Exception) {
                Log.e("EMAIL", "❌ Welcome email failed: ${e.message}", e)
            }
        }.start()
    }
}
