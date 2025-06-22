package wtf.yoraudev.ruby.screen

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import wtf.yoraudev.ruby.auth.McTokenAuth
import wtf.yoraudev.ruby.auth.SessionManager
import java.util.concurrent.CompletableFuture

class TokenLoginAuthScreen : Screen(Text.literal("TokenLogin")) {
    
    private lateinit var textField: TextFieldWidget
    private lateinit var loginButton: ButtonWidget
    
    private var statusMessage: Text? = null
    private var statusColor: Int = 0xFFFFFF
    private var isAuthenticating = false
    
    override fun init() {
        super.init()
        
        val centerX = width / 2
        val centerY = height / 2
        
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Back")) { button ->
                close()
            }
                .dimensions(5, 5, 50, 20)
                .build()
        )
        
        textField = TextFieldWidget(
            textRenderer,
            centerX - 200,
            centerY - 20,
            400,
            20,
            Text.literal("Enter McToken")
        )
        textField.setMaxLength(1000)
        textField.setFocusUnlocked(true)
        addDrawableChild(textField)
        
        loginButton = ButtonWidget.builder(Text.literal("Login")) { button ->
            if (!isAuthenticating && textField.text.isNotBlank()) {
                authenticateToken(textField.text.trim())
            }
        }
            .dimensions(centerX - 50, centerY + 10, 100, 20)
            .build()
        
        addDrawableChild(loginButton)
    }
    
    private fun authenticateToken(token: String) {
        isAuthenticating = true
        loginButton.active = false
        statusMessage = Text.literal("Authenticating...")
        statusColor = 0xFFFFFF
        
        CompletableFuture.supplyAsync {
            McTokenAuth.authenticate(token)
        }.thenAccept { result ->
            client?.execute {
                when (result) {
                    is McTokenAuth.AuthResult.Success -> {
                        if (SessionManager.setSession(token)) {
                            statusMessage = Text.literal("Success! Session updated.")
                            statusColor = 0x00FF00
                            CompletableFuture.runAsync {
                                Thread.sleep(1000)
                                client?.execute {
                                    close()
                                }
                            }
                        } else {
                            statusMessage = Text.literal("Failed to set session!")
                            statusColor = 0xFF0000
                        }
                    }
                    is McTokenAuth.AuthResult.Failure -> {
                        statusMessage = Text.literal("Failed: ${result.message}")
                        statusColor = 0xFF0000 // Red
                    }
                }
                isAuthenticating = false
                loginButton.active = true
            }
        }
    }
    
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        
        context.drawCenteredTextWithShadow(
            textRenderer,
            title,
            width / 2,
            20,
            16777215
        )
        
        statusMessage?.let { message ->
            context.drawCenteredTextWithShadow(
                textRenderer,
                message,
                width / 2,
                height / 2 + 40,
                statusColor
            )
        }
    }
    
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (textField.keyPressed(keyCode, scanCode, modifiers) || textField.isActive) {
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
    
    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        if (textField.charTyped(chr, modifiers)) {
            return true
        }
        return super.charTyped(chr, modifiers)
    }
    
    override fun shouldPause(): Boolean {
        return false
    }
}