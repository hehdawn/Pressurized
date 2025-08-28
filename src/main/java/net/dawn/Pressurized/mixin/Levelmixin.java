package net.dawn.Pressurized.mixin;

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

//@Mixin(net.minecraft.world.level.Level.class)
//public abstract class Levelmixin {
//    @Inject(method = "getFluidState", at =  @At("HEAD"), cancellable = true)
//    private void meow(BlockPos p_46671_, CallbackInfoReturnable<FluidState> cir) {
//        if (ModList.get().isLoaded("valkyrienskies")) {
//            //currently the mod uses VS ship AABB as an early test for if the Mixin works.
//            //the goal is to create an AABB by determining the hull space and if there are any water leaks.
//            final ArrayList<AABB> AirPockets = new ArrayList<>();
//
//            for (Ship ship : VSGameUtilsKt.getAllShips(Minecraft.getInstance().level)) {
//                Vec3 Min = new Vec3(ship.getWorldAABB().minX(), ship.getWorldAABB().minY(), ship.getWorldAABB().minZ());
//                Vec3 Max = new Vec3(ship.getWorldAABB().maxX(), ship.getWorldAABB().maxY(), ship.getWorldAABB().maxZ());
//
//                AABB TEST = new AABB(Min, Max);
//                AirPockets.add(AirPockets.size()+1, TEST);
//            }
//
//            Vec3 jomlVector = new Vec3(p_46671_.getX(), p_46671_.getY(), p_46671_.getZ());
//
//            for (AABB tired : AirPockets) {
//                if (Objects.requireNonNull(tired).contains(jomlVector)) {
//                    cir.setReturnValue(Fluids.EMPTY.defaultFluidState());
//                }
//            }
//            AirPockets.clear();
//        }
//    }
//}