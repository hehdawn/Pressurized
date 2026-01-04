package net.dawn.pressurized.mixin;

import net.dawn.pressurized.VSCompat;
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

    @Inject(method = "isInWater", at = @At("HEAD"), cancellable = true)
    private void InjectIsInWater(CallbackInfoReturnable<Boolean> cir) {
        if (ModList.get().isLoaded("valkyrienskies")) {
            Vec3 jomlVector1 = pressurized$self().position();

            ArrayList<Vec3> jomlVectors = new ArrayList<>();

            jomlVectors.add(VSCompat.WorldToValkShip(pressurized$self().level(), jomlVector1));

            if (VSCompat.IsInAirpocket(jomlVectors, pressurized$self().level())) {
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

            jomlVectors.add(VSCompat.WorldToValkShip(instance, jomlVector1));

            if (VSCompat.IsInAirpocket(jomlVectors, pressurized$self().level())) {
                return Fluids.EMPTY.defaultFluidState();
            }
        }
        return pressurized$self().level().getFluidState(BlockPos.containing(pressurized$self().getEyePosition()));
    }
}