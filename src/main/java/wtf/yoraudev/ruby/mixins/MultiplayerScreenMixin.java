package wtf.yoraudev.ruby.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.yoraudev.ruby.screen.TokenLoginAuthScreen;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    
    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }
    
    @Inject(method = "init", at = @At("RETURN"))
    private void addSchubiAuthButton(CallbackInfo ci) {
        int buttonWidth = 100;
        int buttonHeight = 20;
        int padding = 5;
        
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("TokenLogin"), button -> {
                if (this.client != null) {
                    this.client.setScreen(new TokenLoginAuthScreen());
                }
            })
            .dimensions(this.width - buttonWidth - padding, padding, buttonWidth, buttonHeight)
            .build()
        );
    }
    
    @Inject(method = "render", at = @At("TAIL"))
    private void renderUsername(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = this.client;
        if (mc != null) {
            Session session = mc.getSession();
            if (session != null && session.getAccountType() == Session.AccountType.MSA) {
                String username = session.getUsername();
                int textWidth = this.textRenderer.getWidth(username);
                int x = this.width - 110 - textWidth - 10;
                int y = 10;
                
                context.drawTextWithShadow(
                    this.textRenderer,
                    username,
                    x,
                    y,
                    0x00FF00
                );
            }
        }
    }
}