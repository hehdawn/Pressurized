package net.dawn.Pressurized;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class VSCompat {
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
}

//by brickyboy124
//thx!!!!