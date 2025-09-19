package net.dawn.Pressurized.mixin;

import net.dawn.Pressurized.PressurizedMain;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(net.minecraft.world.entity.Entity.class)
public abstract class Entitymixin {
    @Unique
    private Entity pressurized$self() {
        return (Entity) (Object) this;
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

    @Inject(method = "isInWater", at = @At("HEAD"), cancellable = true)
    private void goog(CallbackInfoReturnable<Boolean> cir) {
        if (ModList.get().isLoaded("valkyrienskies")) {
            Vec3 jomlVector1 = new Vec3(pressurized$self().getX(), pressurized$self().getY(), pressurized$self().getZ());
            Vec3 jomlVector2 = new Vec3(
                    pressurized$self().getX(),
                    pressurized$self().getEyeY() - (double) 0.11111111F,
                    pressurized$self().getZ()
            );

            ArrayList<Vec3> jomlVectors = new ArrayList<>();
            jomlVectors.add(jomlVector1);
            jomlVectors.add(jomlVector2);

            if (pressurized$AirpocketOverride(jomlVectors)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Redirect(
            method = "updateFluidOnEyes",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"
            )
    )
    private FluidState RedirectupdateFluidOnEyes(Level instance, BlockPos blockPos) {
        if (ModList.get().isLoaded("valkyrienskies")) {
            Vec3 jomlVector1 = new Vec3(
                    pressurized$self().getX(),
                    pressurized$self().getEyeY() - (double) 0.11111111F,
                    pressurized$self().getZ()
            );

            ArrayList<Vec3> jomlVectors = new ArrayList<>();
            jomlVectors.add(jomlVector1);

            if (pressurized$AirpocketOverride(jomlVectors)) {
                return Fluids.EMPTY.defaultFluidState();
            }
        }
        return pressurized$self().level().getFluidState(pressurized$self().blockPosition());
    }
}