package net.dawn.pressurized.mixin;

import net.dawn.pressurized.VSCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
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
            Vec3 jomlVector1 = pressurized$self().position();
            Vec3 jomlVector2 = pressurized$self().getEyePosition();

            ArrayList<Vec3> jomlVectors1 = new ArrayList<>();
            ArrayList<Vec3> jomlVectors2 = new ArrayList<>();

            jomlVectors1.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector1));
            jomlVectors2.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector2));

            BlockPos BodyBlockpos = new BlockPos((int) jomlVector1.x(), (int) jomlVector1.y(), (int) jomlVector1.z());
            boolean BodyInFluid = !instance.level().getFluidState(BodyBlockpos).isEmpty();

            BlockPos HeadBlockpos = new BlockPos((int) jomlVector2.x(), (int) jomlVector2.y(), (int) jomlVector2.z());
            boolean HeadInFluid = !instance.level().getFluidState(HeadBlockpos).isEmpty();

            if ((!BodyInFluid || VSCompat.IsInAirpocket(jomlVectors1, pressurized$self().level())) && (!HeadInFluid || VSCompat.IsInAirpocket(jomlVectors2, pressurized$self().level()))) {
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
            Vec3 jomlVector1 = pressurized$self().position();
            Vec3 jomlVector2 = pressurized$self().getEyePosition();

            ArrayList<Vec3> jomlVectors1 = new ArrayList<>();
            ArrayList<Vec3> jomlVectors2 = new ArrayList<>();

            jomlVectors1.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector1));
            jomlVectors2.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector2));

            BlockPos BodyBlockpos = new BlockPos((int) jomlVector1.x(), (int) jomlVector1.y(), (int) jomlVector1.z());
            boolean BodyInFluid = !instance.level().getFluidState(BodyBlockpos).isEmpty();

            BlockPos HeadBlockpos = new BlockPos((int) jomlVector2.x(), (int) jomlVector2.y(), (int) jomlVector2.z());
            boolean HeadInFluid = !instance.level().getFluidState(HeadBlockpos).isEmpty();

            if ((BodyInFluid || VSCompat.IsInAirpocket(jomlVectors1, pressurized$self().level())) && (HeadInFluid || VSCompat.IsInAirpocket(jomlVectors2, pressurized$self().level()))) {
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
                    ordinal = 2
            )
    )
    private void redirectJumpInFluid2(LivingEntity instance, FluidType fluidType) {
        if (ModList.get().isLoaded("valkyrienskies")) {
            Vec3 jomlVector1 = pressurized$self().position();

            Vec3 jomlVector3 = pressurized$self().getEyePosition();

            ArrayList<Vec3> jomlVectors1 = new ArrayList<>();
            ArrayList<Vec3> jomlVectors2 = new ArrayList<>();

            jomlVectors1.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector1));

            BlockPos BodyBlockpos = new BlockPos((int) jomlVector1.x(), (int) jomlVector1.y(), (int) jomlVector1.z());
            boolean BodyInFluid = !instance.level().getFluidState(BodyBlockpos).isEmpty();

            BlockPos HeadBlockpos = new BlockPos((int) jomlVector3.x(), (int) jomlVector3.y(), (int) jomlVector3.z());
            boolean HeadInFluid = !instance.level().getFluidState(HeadBlockpos).isEmpty();

            jomlVectors2.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector3));

            if ((!BodyInFluid || VSCompat.IsInAirpocket(jomlVectors1, pressurized$self().level())) && (!HeadInFluid || VSCompat.IsInAirpocket(jomlVectors2, pressurized$self().level()))) {
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
            Vec3 jomlVector1 = pressurized$self().position();
            Vec3 jomlVector2 = pressurized$self().getEyePosition();

            ArrayList<Vec3> jomlVectors1 = new ArrayList<>();
            ArrayList<Vec3> jomlVectors2 = new ArrayList<>();

            jomlVectors1.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector1));
            jomlVectors2.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector2));

            boolean T1 = (instance.level().getFluidState(BlockPos.containing(pressurized$self().position())).isEmpty() || VSCompat.IsInAirpocket(jomlVectors1, pressurized$self().level()));
            boolean T2 = (instance.level().getFluidState(BlockPos.containing(pressurized$self().getEyePosition())).isEmpty() || VSCompat.IsInAirpocket(jomlVectors2, pressurized$self().level()));

            return !(T1 && T2);
        }
        return true;
    }
}