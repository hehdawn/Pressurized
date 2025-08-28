package net.dawn.Pressurized.Client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.dawn.Pressurized.PressurizedMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class PressurizedHudOverlay {
    static Minecraft MC = Minecraft.getInstance();
    //    public static final ResourceLocation PressurizedOverlay = ResourceLocation.fromNamespaceAndPath(
    //            PressurizedMain.MODID,
    //            "textures/pressurized.png");

    public static final ResourceLocation PressurizedOverlay = ResourceLocation.fromNamespaceAndPath(
            PressurizedMain.MODID,
            "textures/pressurized_transparent_teal.png");

    public static final IGuiOverlay PressurizedHUD = (((gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F,1.0F);
        RenderSystem._setShaderTexture(0, PressurizedOverlay);
    }));

    private float Clamp(float n, float min, float max) {
        if (n > max) {
            return max;
        } else if (n < min) {
            return min;
        } else {
            return n;
        }
    }

    private void RenderPressurized(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int scw, int sch)
    {
        if (MC.player == null)
            return;

        //float Transparency = (float) Math.clamp(java.lang.Math.abs(PressurizedMain.getBodyPressure())/2, 0, 1); for some reason Math.Clamp don't work
        float X = (float) (PressurizedMain.getBodyPressure()-PressurizedMain.getDepth())/10;
        float Transparency  = Clamp(X*X, 0, 1);

        if (!ClientConfigs.PressureOverlay.get() | Minecraft.getInstance().player.isCreative() | !MC.player.isUnderWater()) {
            Transparency = 0;
        }

        RenderSystem.setShaderTexture(0, PressurizedOverlay);
        renderFullscreen(guiGraphics.pose(), scw, sch, 100, 58, 0, 0, 100, 58, Transparency);
    }

    @OnlyIn(Dist.CLIENT)
    public void initOverlays(final RegisterGuiOverlaysEvent event)
    {
        event.registerAbove(VanillaGuiOverlay.VIGNETTE.id(), PressurizedMain.MODID.concat(".pressurized_overlay"), this::RenderPressurized);
    }

    private static void renderFullscreen(PoseStack poseStack, int scw, int sch, int texw, int texh, int uoffset, int voffset, int spritew, int spriteh, float alpha)
    {
        Matrix4f mat = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableBlend();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferbuilder.vertex(mat, 0f, 0f, 0f).color(1f, 1f, 1f, alpha).uv((float)uoffset / texw, (float)voffset / texh).endVertex();
        bufferbuilder.vertex(mat, 0f, (float)sch, 0f).color(1f, 1f, 1f, alpha).uv((float)uoffset / texw, (float)(voffset + spriteh) / texh).endVertex();
        bufferbuilder.vertex(mat, (float)scw, (float)sch, 0f).color(1f, 1f, 1f, alpha).uv((float)(uoffset + spritew) / texw, (float)(voffset + spriteh) / texh).endVertex();
        bufferbuilder.vertex(mat, (float)scw, 0f, 0f).color(1f, 1f, 1f, alpha).uv((float)(uoffset + spritew) / texw, (float)voffset / texh).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.disableBlend();
    }
}