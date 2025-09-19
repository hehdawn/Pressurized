package net.dawn.Pressurized;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.ArrayList;

public class VSCompat {
    //@OnlyIn(Dist.DEDICATED_SERVER)
    public static BlockPos valkShipToWorld(ServerLevel level, BlockPos shipPos) {
        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, shipPos);

        // Ship is null if the block isn't on a ship
        if (ship == null) {
            return shipPos;
        }

        // Convert the blockpos into something VS can use
        Vector3d vecShipPos = VectorConversionsMCKt.toJOML(shipPos.getCenter());

        // Move that blockpos ship -> world
        Vector3d vecWorldPos = ship.getTransform().getShipToWorld().transformPosition(vecShipPos, new Vector3d());

        // Back to a Minecraft format
        Vec3 mcVecWorldPos = VectorConversionsMCKt.toMinecraft(vecWorldPos);

        // Back to a BlockPos (optional, but probably easier to integrate into your existing code)
        return BlockPos.containing(mcVecWorldPos);
    }
    //^by brickyboy124
    //thx!!!!

    //@OnlyIn(Dist.DEDICATED_SERVER)
    public static Vec3 TEST(ServerLevel level, BlockPos shipPos) {
        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, shipPos);

        // Ship is null if the block isn't on a ship
        if (ship == null) {
            return shipPos.getCenter();
        }

        // Convert the blockpos into something VS can use
        Vector3d vecShipPos = VectorConversionsMCKt.toJOML(shipPos.getCenter());

        // Move that blockpos ship -> world
        Vector3d vecWorldPos = ship.getTransform().getShipToWorld().transformPosition(vecShipPos, new Vector3d());

        // Back to a Minecraft format
        return VectorConversionsMCKt.toMinecraft(vecWorldPos);
    }

    //@OnlyIn(Dist.DEDICATED_SERVER)
    public static boolean IDK(ArrayList<Vec3> jomlVectors, ServerLevel serverLevel) {
        try {
            for (int i = PressurizedMain.AirPockets.size() - 1; i >= 0; i--) {
                AABB ShipyardAirpocket = PressurizedMain.AirPockets.get(i);

                BlockPos MinBlock = new BlockPos(
                        (int) ShipyardAirpocket.minX,
                        (int) ShipyardAirpocket.minY,
                        (int) ShipyardAirpocket.minZ
                );
                BlockPos MaxBlock = new BlockPos(
                        (int) ShipyardAirpocket.maxX,
                        (int) ShipyardAirpocket.maxY,
                        (int) ShipyardAirpocket.maxZ
                );

                Vec3 Min = TEST(serverLevel, MinBlock);
                Vec3 Max = TEST(serverLevel, MaxBlock);

                AABB WorldAABB = new AABB(Min,Max);

                for (Vec3 jomlVector : jomlVectors) {
                    if (WorldAABB.contains(jomlVector)) {
                        return true;
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("womp");
        }
        return false;
    }

    public static boolean TESTIDK(ArrayList<Vec3> jomlVectors) {
        try {
            for (int i = PressurizedMain.AirPockets.size() - 1; i >= 0; i--) {
                AABB ShipyardAirpocket = PressurizedMain.AirPockets.get(i);

                for (Vec3 jomlVector : jomlVectors) {
                    if (ShipyardAirpocket.contains(jomlVector)) {
                        return true;
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("womp");
        }
        return false;
    }
}