package me.lortseam.uglyscoreboardfix.mixin;

import me.lortseam.uglyscoreboardfix.UglyScoreboardFix;
import me.lortseam.uglyscoreboardfix.config.ModConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Unique
    private int xShift;

    @Final
    @Shadow
    private DebugHud debugHud;

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"), cancellable = true)
    private void uglyscoreboardfix$hide(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        if ((UglyScoreboardFix.getConfig().isHideSidebar()) || (UglyScoreboardFix.getConfig().isHideOnDebug() && debugHud.shouldShowDebugHud())) {
            ci.cancel();
            return;
        }
        context.getMatrices().push();
        float scale = UglyScoreboardFix.getConfig().getScale();
        context.getMatrices().scale(scale, scale, scale);
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("TAIL"))
    private void uglyscoreboardfix$pop(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        context.getMatrices().pop();
    }

    @Redirect(method = "method_55440", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;getScaledWindowWidth()I"))
    private int uglyscoreboardfix$scaleWidth(DrawContext context) {
        return (int) (context.getScaledWindowWidth() / UglyScoreboardFix.getConfig().getScale());
    }

    @Redirect(method = "method_55440", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;getScaledWindowHeight()I"))
    private int uglyscoreboardfix$scaleHeight(DrawContext context) {
        return (int) (context.getScaledWindowHeight() / UglyScoreboardFix.getConfig().getScale());
    }

    @ModifyVariable(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At(value = "STORE"))
    private NumberFormat uglyscoreboardfix$modifyNumberFormat(NumberFormat numberFormat, DrawContext context, ScoreboardObjective objective) {
        if (UglyScoreboardFix.getConfig().getHideScores() == ModConfig.HideScores.Yes || (UglyScoreboardFix.getConfig().getHideScores() == ModConfig.HideScores.Auto && ModConfig.scoresAreConsecutive(objective)))
            return BlankNumberFormat.INSTANCE;
        else
            return numberFormat;
    }

    @Redirect(method = "method_55440", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", ordinal = 0))
    private void uglyscoreboardfix$fillHeadingBackground(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        if (UglyScoreboardFix.getConfig().isHideTitle())
            return;
        if (UglyScoreboardFix.getConfig().getHorizontalPosition() == ModConfig.HorizontalPosition.Left) {
            xShift = x1 - 1;
            x1 = 1;
            x2 -= xShift;
        }
        y1 += UglyScoreboardFix.getConfig().getYOffset();
        y2 += UglyScoreboardFix.getConfig().getYOffset();
        color = UglyScoreboardFix.getConfig().getHeadingBackgroundColor().getRGB();
        context.fill(x1, y1, x2, y2, color);
    }

    @Redirect(method = "method_55440", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", ordinal = 1))
    private void uglyscoreboardfix$fillBackground(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        if (UglyScoreboardFix.getConfig().getHorizontalPosition() == ModConfig.HorizontalPosition.Left) {
            x1 -= xShift;
            x2 -= xShift;
        }
        y1 += UglyScoreboardFix.getConfig().getYOffset();
        y2 += UglyScoreboardFix.getConfig().getYOffset();
        color = UglyScoreboardFix.getConfig().getBackgroundColor().getRGB();
        context.fill(x1, y1, x2, y2, color);
    }

    @Redirect(method = "method_55440", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I", ordinal = 0))
    private int uglyscoreboardfix$drawHeadingText(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow) {
        if (UglyScoreboardFix.getConfig().isHideTitle())
            return 0;
        if (UglyScoreboardFix.getConfig().getHorizontalPosition() == ModConfig.HorizontalPosition.Left)
            x -= xShift;
        y += UglyScoreboardFix.getConfig().getYOffset();
        color = UglyScoreboardFix.getConfig().getHeadingForegroundColor().getRGB();
        shadow = UglyScoreboardFix.getConfig().isHeadingShadow();
        return context.drawText(textRenderer, text, x, y, color, shadow);
    }

    @Redirect(method = "method_55440", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I", ordinal = 1))
    private int uglyscoreboardfix$drawText(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow) {
        if (UglyScoreboardFix.getConfig().getHorizontalPosition() == ModConfig.HorizontalPosition.Left)
            x -= xShift;
        y += UglyScoreboardFix.getConfig().getYOffset();
        color = UglyScoreboardFix.getConfig().getForegroundColor().getRGB();
        shadow = UglyScoreboardFix.getConfig().isShadow();
        return context.drawText(textRenderer, text, x, y, color, shadow);
    }

    @Redirect(method = "method_55440", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I", ordinal = 2))
    private int uglyscoreboardfix$drawScoreText(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow) {
        if (UglyScoreboardFix.getConfig().getHorizontalPosition() == ModConfig.HorizontalPosition.Left)
            x -= xShift;
        y += UglyScoreboardFix.getConfig().getYOffset();
        color = UglyScoreboardFix.getConfig().getScoreForegroundColor().getRGB();
        shadow = UglyScoreboardFix.getConfig().isScoreShadow();
        return context.drawText(textRenderer, text, x, y, color, shadow);
    }

    @ModifyConstant(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", constant = @Constant(longValue = 15L))
    private long uglyscoreboardfix$modifyMaxLineCount(long maxSize) {
        return UglyScoreboardFix.getConfig().getMaxLineCount();
    }
}
