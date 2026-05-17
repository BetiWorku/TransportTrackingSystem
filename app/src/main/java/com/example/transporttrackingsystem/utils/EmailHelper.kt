package com.example.transporttrackingsystem.utils

import android.util.Log
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailHelper {

    fun sendWelcomeEmail(userEmail: String, userName: String) {
        // 🚨 IMPORTANT: You MUST use a real Gmail App Password (16 digits)
        // 1. Enable 2FA on your Google Account.
        // 2. Search for "App Passwords" in Google Account settings.
        // 3. Select "Other" and name it "AndroidBusApp".
        val senderEmail = "bwwmas@gmail.com" 
        val appPassword = "gmym hgyb gkmm lbel"    

        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = "587"
        props["mail.smtp.ssl.protocols"] = "TLSv1.2"

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(senderEmail, appPassword)
            }
        })

        // Use a background thread for networking
        Thread {
            try {
                Log.d("EMAIL", "Starting email send to $userEmail...")
                val message = MimeMessage(session)
                message.setFrom(InternetAddress(senderEmail, "Addis Bus Tracker"))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail))
                message.setSubject("Welcome to Addis Bus Tracker 🚍")
                
                val htmlContent = """
                    <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.1);">
                        <div style="background: linear-gradient(135deg, #3F51B5, #2196F3); padding: 40px 20px; text-align: center; color: white;">
                            <h1 style="margin: 0; font-size: 32px; letter-spacing: 1px;">Addis Bus Tracker</h1>
                            <p style="margin: 10px 0 0; opacity: 0.9; font-size: 16px;">Your Smart Transit Companion</p>
                        </div>
                        <div style="padding: 40px; line-height: 1.8; color: #444; background: #ffffff;">
                            <h2 style="color: #3F51B5; margin-top: 0;">Welcome, $userName!</h2>
                            <p>We're thrilled to have you join <b>Addis Bus Tracker</b>. Your account is now active and ready for your next commute.</p>
                            
                            <div style="background: #f8f9fa; padding: 25px; border-left: 5px solid #3F51B5; border-radius: 8px; margin: 30px 0;">
                                <h3 style="margin-top: 0; font-size: 18px; color: #333;">Next Steps:</h3>
                                <ul style="margin: 0; padding-left: 20px;">
                                    <li><b>Stay Updated:</b> Use the live map to see bus locations.</li>
                                    <li><b>Plan Ahead:</b> Get accurate ETAs for your destination.</li>
                                    <li><b>Safe Travel:</b> Join thousands of smart commuters in Addis.</li>
                                </ul>
                            </div>
                            
                            <p>If you didn't receive your verification link from Firebase, please check your Spam folder.</p>
                            
                            <p style="margin-top: 40px; border-top: 1px solid #eee; padding-top: 20px;">Best regards,<br><b style="color: #3F51B5;">The Addis Transport Team</b></p>
                        </div>
                        <div style="background: #f1f1f1; padding: 20px; text-align: center; font-size: 13px; color: #777;">
                            © 2026 Addis Bus Tracker System. <br>
                            This is an automated message, please do not reply directly.
                        </div>
                    </div>
                """.trimIndent()

                message.setContent(htmlContent, "text/html; charset=utf-8")

                Transport.send(message)
                Log.d("EMAIL", "✅ SUCCESS: HTML Welcome email sent to $userEmail")
            } catch (e: Exception) {
                Log.e("EMAIL", "❌ ERROR: Failed to send email: ${e.message}")
                e.printStackTrace()
            }
        }.start()
    }
}
