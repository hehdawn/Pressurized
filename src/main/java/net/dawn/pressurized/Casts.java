package net.dawn.pressurized;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class Casts {
    public static BlockPos BlockCastNorth(BlockPos Position, Level level, ArrayList<BlockPos> Filter, boolean FilterExclude) {
        BlockPos CurrentPos = Position;
        BlockPos Result = null;
        int Attempts = 0;

        while (Result == null) {
            Attempts++;

            if (Attempts > 50) {
                Result = CurrentPos;
                break;
            }
            if (!Filter.contains(CurrentPos)) {
                CurrentPos = CurrentPos.north();
            } else {
                Result = CurrentPos;
            }
        }

        return Result;

    }

    public static BlockPos BlockCastEast(BlockPos Position, Level level, ArrayList<BlockPos> Filter, boolean FilterExclude) {
        BlockPos CurrentPos = Position;
        BlockPos Result = null;
        int Attempts = 0;

        while (Result == null) {
            Attempts++;

            if (Attempts > 50) {
                Result = CurrentPos;
                break;
            }
            if (!Filter.contains(CurrentPos)) {
                CurrentPos = CurrentPos.east();
            } else {
                Result = CurrentPos;
            }
        }

        return Result;
    }

    public static BlockPos BlockCastSouth(BlockPos Position, Level level, ArrayList<BlockPos> Filter, boolean FilterExclude) {
        BlockPos CurrentPos = Position;
        BlockPos Result = null;
        int Attempts = 0;

        while (Result == null) {
            Attempts++;

            if (Attempts > 50) {
                Result = CurrentPos;
                break;
            }
            if (!Filter.contains(CurrentPos)) {
                CurrentPos = CurrentPos.south();
            } else {
                Result = CurrentPos;
            }
        }

        return Result;
    }

    public static BlockPos BlockCastWest(BlockPos Position, Level level, ArrayList<BlockPos> Filter, boolean FilterExclude) {
        BlockPos CurrentPos = Position;
        BlockPos Result = null;
        int Attempts = 0;

        while (Result == null) {
            Attempts++;

            if (Attempts > 50) {
                Result = CurrentPos;
                break;
            }

            if (!Filter.contains(CurrentPos)) {
                CurrentPos = CurrentPos.west();
            } else {
                Result = CurrentPos;
            }
        }

        return Result;
    }

    public static BlockPos Raycast(Vec3 Position, Vec3 Direction, Level level, ArrayList<BlockPos> Filter, boolean FilterExclude) {
        BlockPos Result = null;
        Vec3 RayStart = Position;
        Vec3 RayDirection = Position.add(Direction);
        Vec3 Offset = new Vec3(0,0,0);

        int Attempts = 0;
        while (!Filter.contains(Result)) {
            if (Result != null && Position.distanceTo(Result.getCenter()) > 100) {
                //System.out.println("Raycast missed.");
                return Result;
            }

            if (Attempts >= 5) {
                return Result;
            }

            double X = 0;
            if (Direction.x() >= 1) {
                X = 1;
            } else if (Direction.x() <= -1) {
                X = -1;
            }

            double Y = 0;
            if (Direction.y() >= 1) {
                Y = 1;
            } else if (Direction.y() <= -1) {
                Y = -1;
            }

            double Z = 0;
            if (Direction.z() >= 1) {
                Z = 1;
            } else if (Direction.z() <= -1) {
                Z = -1;
            }

            if (Result != null) {
                Offset = new Vec3(
                        X,
                        Y,
                        Z
                );
            }

            if (Filter.contains(BlockPos.containing(RayStart.add(Offset)))) {
                return BlockPos.containing(RayStart.add(Offset));
            }

            BlockHitResult context = level.clip(new ClipContext(
                    RayStart.add(Offset),
                    RayDirection,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    null
            ));

            Result = context.getBlockPos();
            RayStart = Result.getCenter();
            RayDirection = Result.getCenter().add(Direction);
            Attempts++;

            if (FilterExclude) {
                return Result;
            }
        }

        return Result;
    }
}
