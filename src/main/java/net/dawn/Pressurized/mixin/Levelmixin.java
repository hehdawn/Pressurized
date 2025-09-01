package net.dawn.Pressurized.mixin;

import net.dawn.Pressurized.PressurizedMain;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.ArrayList;
import java.util.Objects;

@Mixin(net.minecraft.world.level.Level.class)
public abstract class Levelmixin {
    @Inject(method = "getFluidState", at =  @At("HEAD"), cancellable = true)
    private void meow(BlockPos p_46671_, CallbackInfoReturnable<FluidState> cir) {
        if (ModList.get().isLoaded("valkyrienskies")) {
            //currently the mod uses VS ship AABB as an early test for if the Mixin works.
            //the goal is to create an AABB by determining the hull space and if there are any water leaks.

            Vec3 jomlVector = new Vec3(p_46671_.getX(), p_46671_.getY(), p_46671_.getZ());

            for (int i = PressurizedMain.AirPockets.size() - 1; i >= 0; i--) {
                if (PressurizedMain.AirPockets.get(i).contains(jomlVector)) {
                    cir.setReturnValue(Fluids.EMPTY.defaultFluidState());
                }
            }
            //PressurizedMain.AirPockets.clear();
        }
    }
}