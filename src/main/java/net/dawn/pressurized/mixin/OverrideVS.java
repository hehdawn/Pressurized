package net.dawn.pressurized.mixin;

import net.dawn.pressurized.ServerConfigs;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Dynamic;

@Pseudo
@Mixin(value = Entity.class, priority = 1500)
public abstract class OverrideVS {

    @Dynamic("Method added by Valkyrien Skies")
    @Inject(
            method = "vs$isInSealedArea()Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0,
            expect = 0
    )
    private void killSealedLogic(CallbackInfoReturnable<Boolean> cir) {
        if (ServerConfigs.Airpockets.get()) {
            cir.setReturnValue(false);
        }
    }

    @Dynamic("Method added by Valkyrien Skies")
    @Inject(
            method = "vs$setInSealedArea()Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0,
            expect = 0
    )
    private void forceUnsealed(boolean inSealedArea, CallbackInfo ci) {
        if (inSealedArea && ServerConfigs.Airpockets.get()) {
            ci.cancel();
        }
    }
}