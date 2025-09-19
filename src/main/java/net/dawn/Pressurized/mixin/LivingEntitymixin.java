package net.dawn.Pressurized.mixin;

import net.dawn.Pressurized.PressurizedMain;
import net.dawn.Pressurized.VSCompat;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;

//TODO: patch player swimming in airpockets

@Mixin(LivingEntity.class)
public abstract class LivingEntitymixin implements LivingEntityAccessor {

    @Shadow private int noJumpDelay;

    @Unique
    private LivingEntity pressurized$self() {
        return (LivingEntity) (Object) this;
    }

    @Unique
    public boolean pressurized$AirpocketOverride(ArrayList<Vec3> jomlVectors) {
        try {
            for (int i = PressurizedMain.AirPockets.size() - 1; i >= 0; i--) {
                for (Vec3 jomlVector : jomlVectors) {
                    if (PressurizedMain.AirPockets.get(i).contains(jomlVector)) {
                        return true;
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("womp");
        }
        return false;
    }

    @Redirect(
            method = "aiStep()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;jumpInFluid(Lnet/minecraftforge/fluids/FluidType;)V",
                    ordinal = 0
            )
    )
    private void redirectJumpInFluid0(LivingEntity instance, FluidType fluidType) {
        if (ModList.get().isLoaded("valkyrienskies")) {
            Vec3 jomlVector1 = new Vec3(pressurized$self().getX(), pressurized$self().getY(), pressurized$self().getZ());
            Vec3 jomlVector2 = pressurized$self().getEyePosition();

            ArrayList<Vec3> jomlVectors = new ArrayList<>();
            jomlVectors.add(jomlVector1);
            jomlVectors.add(jomlVector2);

            if (pressurized$AirpocketOverride(jomlVectors)) {
                if ((pressurized$self().onGround()) && this.noJumpDelay == 0) {
                    this.invokeJumpFromGround();
                }
            } else {
                pressurized$self().jumpInFluid(fluidType);
            }
        }
    }

    @Redirect(
            method = "aiStep()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;jumpInFluid(Lnet/minecraftforge/fluids/FluidType;)V",
                    ordinal = 1
            )
    )
    private void redirectJumpInFluid1(LivingEntity instance, FluidType fluidType) {
        if (ModList.get().isLoaded("valkyrienskies")) {
            Vec3 jomlVector1 = new Vec3(pressurized$self().getX(), pressurized$self().getY(), pressurized$self().getZ());
            Vec3 jomlVector2 = pressurized$self().getEyePosition();

            ArrayList<Vec3> jomlVectors = new ArrayList<>();
            jomlVectors.add(jomlVector1);
            jomlVectors.add(jomlVector2);

            if (pressurized$AirpocketOverride(jomlVectors)) {
                if ((pressurized$self().onGround()) && this.noJumpDelay == 0) {
                    this.invokeJumpFromGround();
                }
            }  else {
                pressurized$self().jumpInFluid(fluidType);
            }
        }
    }

    @Redirect(
            method = "aiStep()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;jumpInFluid(Lnet/minecraftforge/fluids/FluidType;)V",
                    ordinal = 2
            )
    )
    private void redirectJumpInFluid2(LivingEntity instance, FluidType fluidType) {
        if (ModList.get().isLoaded("valkyrienskies")) {
            Vec3 jomlVector1 = new Vec3(pressurized$self().getX(), pressurized$self().getY(), pressurized$self().getZ());
            Vec3 jomlVector2 = pressurized$self().getEyePosition();

            ArrayList<Vec3> jomlVectors = new ArrayList<>();
            jomlVectors.add(jomlVector1);
            jomlVectors.add(jomlVector2);

            if (pressurized$AirpocketOverride(jomlVectors)) {
                if ((pressurized$self().onGround()) && this.noJumpDelay == 0) {
                    this.invokeJumpFromGround();
                }
            } else {
                pressurized$self().jumpInFluid(fluidType);
            }
        }
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isAffectedByFluids()Z"))
    private boolean RedirectTravel1(LivingEntity instance) {
        if (ModList.get().isLoaded("valkyrienskies")) {
            Vec3 jomlVector1 = new Vec3(pressurized$self().getX(), pressurized$self().getY(), pressurized$self().getZ());
            Vec3 jomlVector2 = pressurized$self().getEyePosition();

            ArrayList<Vec3> jomlVectors = new ArrayList<>();
            jomlVectors.add(jomlVector1);
            jomlVectors.add(jomlVector2);

            return !pressurized$AirpocketOverride(jomlVectors);
        }
        return true;
    }
}