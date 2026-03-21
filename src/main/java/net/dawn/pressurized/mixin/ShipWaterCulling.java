package net.dawn.pressurized.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.dawn.pressurized.VSCompat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static net.dawn.pressurized.VSCompat.AirPockets;

@Mixin(LevelRenderer.class)
public abstract class ShipWaterCulling {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    public void globalStencilClear(PoseStack p_109600_, float p_109601_, long p_109602_, boolean p_109603_, Camera p_109604_, GameRenderer p_109605_, LightTexture p_109606_, Matrix4f p_254120_, CallbackInfo ci) {
        RenderTarget rt = Minecraft.getInstance().getMainRenderTarget();
        if (!rt.isStencilEnabled()) rt.enableStencil();

        RenderSystem.stencilMask(0xFF);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
    }

    @Shadow
    protected abstract void renderChunkLayer(RenderType p_172994_, PoseStack p_172995_, double p_172996_, double p_172997_, double p_172998_, Matrix4f p_254039_);

    @Unique
    private boolean bool = false;


    @Inject(method = "renderChunkLayer", at = @At("HEAD"), cancellable = true)
    public void identifyWater(RenderType type, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projection, CallbackInfo ci) {
        if (type == RenderType.translucent() && !bool && ModList.get().isLoaded("valkyrienskies")) {
            bool = true;
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.depthMask(true);
            this.renderChunkLayer(type, poseStack, camX, camY, camZ, projection);

            Minecraft.getInstance().getMainRenderTarget().enableStencil();
            GL11.glEnable(GL11.GL_STENCIL_TEST);

            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.depthMask(false);

            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

            List<AABB> AirpocketsRender = new ArrayList<>(AirPockets);
            for (AABB box : AirpocketsRender) {
                VSCompat.drawMaskBox(poseStack, box, camX, camY, camZ);
                //this.drawMaskBox(poseStack, box, camX, camY, camZ);
            }

            RenderSystem.depthMask(true);
            RenderSystem.colorMask(true, true, true, true);

            RenderSystem.disableDepthTest();

            GL11.glStencilFunc(GL11.GL_NOTEQUAL, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

            this.renderChunkLayer(type, poseStack, camX, camY, camZ, projection);

            this.bool = false;

            ci.cancel();
        }
    }

    @Inject(method = "renderChunkLayer", at = @At("TAIL"))
    public void cleanupWater(RenderType p_172994_, PoseStack p_172995_, double p_172996_, double p_172997_, double p_172998_, Matrix4f p_254039_, CallbackInfo ci) {
        if (p_172994_ == RenderType.translucent()) {
            GL11.glDisable(GL11.GL_STENCIL_TEST);
        }
    }
}