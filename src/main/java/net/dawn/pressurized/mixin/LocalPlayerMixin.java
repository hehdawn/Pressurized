package net.dawn.pressurized.mixin;

import net.dawn.pressurized.VSCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Shadow
    protected Minecraft minecraft;

    @Shadow
    protected abstract boolean hasEnoughImpulseToStartSprinting();

    @Shadow
    protected abstract boolean canStartSprinting();

    @Shadow
    public Input input;

    @Shadow
    protected abstract boolean hasEnoughFoodToStartSprinting();

    @Unique
    private LivingEntity pressurized$self() {
        return (LivingEntity) (Object) this;
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void InjectaiStep(CallbackInfo ci) {
        if (ModList.get().isLoaded("valkyrienskies")) {
            Vec3 jomlVector1 = pressurized$self().position();
            Vec3 jomlVector2 = pressurized$self().getEyePosition();

            ArrayList<Vec3> jomlVectors1 = new ArrayList<>();
            ArrayList<Vec3> jomlVectors2 = new ArrayList<>();

            jomlVectors1.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector1));
            jomlVectors2.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector2));

            boolean T1 = (pressurized$self().level().getFluidState(BlockPos.containing(pressurized$self().position())).isEmpty() || VSCompat.IsInAirpocket(jomlVectors1, pressurized$self().level()));
            boolean T2 = (pressurized$self().level().getFluidState(BlockPos.containing(pressurized$self().getEyePosition())).isEmpty() || VSCompat.IsInAirpocket(jomlVectors2, pressurized$self().level()));

            if (T1 && T2) {
                boolean flag4 = this.canStartSprinting();
                if (!pressurized$self().isSprinting() && this.hasEnoughImpulseToStartSprinting() && flag4 && !pressurized$self().isUsingItem() && !pressurized$self().hasEffect(MobEffects.BLINDNESS) && this.minecraft.options.keySprint.isDown()) {
                    pressurized$self().setSprinting(true);
                }
            }
        }
    }

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;setSprinting(Z)V",
                    ordinal = 2
            )
    )
    private void redirectsetSprinting1(LocalPlayer instance, boolean b) {
        if (ModList.get().isLoaded("valkyrienskies")) {
            Vec3 jomlVector1 = pressurized$self().position();
            Vec3 jomlVector2 = pressurized$self().getEyePosition();

            ArrayList<Vec3> jomlVectors1 = new ArrayList<>();
            ArrayList<Vec3> jomlVectors2 = new ArrayList<>();

            jomlVectors1.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector1));
            jomlVectors2.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector2));

            boolean T1 = (pressurized$self().level().getFluidState(BlockPos.containing(pressurized$self().position())).isEmpty() || VSCompat.IsInAirpocket(jomlVectors1, pressurized$self().level()));
            boolean T2 = (pressurized$self().level().getFluidState(BlockPos.containing(pressurized$self().getEyePosition())).isEmpty() || VSCompat.IsInAirpocket(jomlVectors2, pressurized$self().level()));

            if (T1 && T2) {
                if (instance.isSprinting()) {
                    boolean flag7 = !this.input.hasForwardImpulse() || !this.hasEnoughFoodToStartSprinting();
                    //boolean flag8 = flag7 || instance.horizontalCollision && !instance.minorHorizontalCollision;
                    instance.setSprinting(instance.onGround() && this.input.shiftKeyDown && !flag7);
                }
            } else {
                instance.setSprinting(false);
            }
        }
    }

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;setSprinting(Z)V",
                    ordinal = 3
            )
    )
    private void redirectsetSprinting2(LocalPlayer instance, boolean b) {
        if (ModList.get().isLoaded("valkyrienskies")) {
            Vec3 jomlVector1 = pressurized$self().position();
            Vec3 jomlVector2 = pressurized$self().getEyePosition();

            ArrayList<Vec3> jomlVectors1 = new ArrayList<>();
            ArrayList<Vec3> jomlVectors2 = new ArrayList<>();

            jomlVectors1.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector1));
            jomlVectors2.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector2));

            boolean T1 = (pressurized$self().level().getFluidState(BlockPos.containing(pressurized$self().position())).isEmpty() || VSCompat.IsInAirpocket(jomlVectors1, pressurized$self().level()));
            boolean T2 = (pressurized$self().level().getFluidState(BlockPos.containing(pressurized$self().getEyePosition())).isEmpty() || VSCompat.IsInAirpocket(jomlVectors2, pressurized$self().level()));

            if (T1 && T2) {
                if (instance.isSprinting()) {
                    boolean flag7 = !this.input.hasForwardImpulse() || !this.hasEnoughFoodToStartSprinting();
                    instance.setSprinting(instance.onGround() && !flag7);
                }
            } else {
                instance.setSprinting(false);
            }
        }
    }
}
