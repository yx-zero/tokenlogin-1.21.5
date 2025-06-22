package wtf.yoraudev.ruby.auth

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import net.minecraft.client.MinecraftClient
import net.minecraft.client.session.Session
import java.util.*

object SessionManager {
    private val gson = Gson()
    
    fun setSession(accessToken: String): Boolean {
        return try {
            val client = MinecraftClient.getInstance()
            
            // Log current session info
            val currentSession = client.session
            println("[TokenLogin] Current session: ${currentSession.username} (${currentSession.accountType})")
            
            val profileData = fetchProfile(accessToken) ?: return false
            
            val uuidString = profileData.get("id").asString
            val username = profileData.get("name").asString
            
            val formattedUuid = if (uuidString.contains("-")) {
                uuidString
            } else {
                "${uuidString.substring(0, 8)}-${uuidString.substring(8, 12)}-${uuidString.substring(12, 16)}-${uuidString.substring(16, 20)}-${uuidString.substring(20)}"
            }
            
            val uuid = UUID.fromString(formattedUuid)
            
            val session = Session(
                username,
                uuid,
                accessToken,
                Optional.empty(),
                Optional.empty(),
                Session.AccountType.MSA
            )
            
            (client as wtf.yoraudev.ruby.mixins.MinecraftClientAccessor).setSession(session)
            
            // Verify session was updated
            val newSession = client.session
            println("[TokenLogin] New session set: ${newSession.username} (${newSession.accountType})")
            println("[TokenLogin] Session update successful: ${newSession.accessToken == accessToken}")
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun fetchProfile(accessToken: String): JsonObject? {
        return try {
            val client = java.net.http.HttpClient.newBuilder().build()
            val request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("https://api.minecraftservices.com/minecraft/profile"))
                .header("Authorization", "Bearer $accessToken")
                .GET()
                .build()
            
            val response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
            
            if (response.statusCode() == 200) {
                gson.fromJson(response.body(), JsonObject::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}