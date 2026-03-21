package net.dawn.pressurized;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.dawn.pressurized.Network.UpdateAP;
import net.dawn.pressurized.Network.UpdateCBArray;
import net.dawn.pressurized.Network.UpdateCBTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.events.CollisionEvent;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.dawn.pressurized.PressurizedMain.CrushedBlocks;

public class VSCompat {
    public static final HashMap<AABB, Ship> AirpocketToShip = new HashMap<>();

    public static final ArrayList<AABB> AirPockets = new ArrayList<>();
    public static final HashMap<AABB, AABB> AirPocketsVisuals = new HashMap<>();
    public static final ArrayList<BlockPos> ExcludedBlocks = new ArrayList<>();
    static boolean UpdateAirpockets = false;
    static boolean Debounce = false;

    public static BlockPos valkShipToWorld(Level level, BlockPos shipPos) {
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

    public static Vec3 valkShipToWorld(Ship ship, Vec3 shipPos) {
        // Convert the blockpos into something VS can use
        Vector3d vecShipPos = VectorConversionsMCKt.toJOML(shipPos);

        // Move that blockpos ship -> world
        Vector3d vecWorldPos = ship.getTransform().getShipToWorld().transformPosition(vecShipPos, new Vector3d());

        // Back to a Minecraft format
        Vec3 mcVecWorldPos = VectorConversionsMCKt.toMinecraft(vecWorldPos);

        // Back to a BlockPos (optional, but probably easier to integrate into your existing code)
        return mcVecWorldPos;
    }

    public static AABB ShipyardAirpocketToWorld(Level level, AABB Airpocket) {
        Ship ship = AirpocketToShip.get(Airpocket);
        if (ship == null) {
            return Airpocket;
        }

        Vector3d AABBMin = VectorConversionsMCKt.toJOML(new Vec3(Airpocket.minX, Airpocket.minY, Airpocket.minZ));
        Vector3d AABBMax = VectorConversionsMCKt.toJOML(new Vec3(Airpocket.maxX, Airpocket.maxY, Airpocket.maxZ));

        Vector3d vecWorldPosMin = ship.getTransform().getShipToWorld().transformPosition(AABBMin, new Vector3d());
        Vector3d vecWorldPosMax = ship.getTransform().getShipToWorld().transformPosition(AABBMax, new Vector3d());

        Vec3 mcVecWorldPosMin = VectorConversionsMCKt.toMinecraft(vecWorldPosMin);
        Vec3 mcVecWorldPosMax = VectorConversionsMCKt.toMinecraft(vecWorldPosMax);

        return new AABB(mcVecWorldPosMin, mcVecWorldPosMax);
    }

    public static Vec3 WorldToValkShip(Level level, Vec3 Pos) {
        Ship SourceShip = null;

        for (Ship ship : VSGameUtilsKt.getAllShips(level)) {
            if (ship.getWorldAABB().containsPoint(Pos.x(), Pos.y(), Pos.z())) {
                SourceShip = ship;
            }
        }
        if (SourceShip == null) {
            return Pos;
        }

        Vector3d vecShipPos = VectorConversionsMCKt.toJOML(Pos);

        Vector3d vecWorldPos = SourceShip.getTransform().getWorldToShip().transformPosition(vecShipPos, new Vector3d());

        return VectorConversionsMCKt.toMinecraft(vecWorldPos);
    }

    public static Vec3 WorldToValkShip(Vec3 Pos, Ship Ship) {
        Vector3d vecShipPos = VectorConversionsMCKt.toJOML(Pos);

        Vector3d vecWorldPos = Ship.getTransform().getWorldToShip().transformPosition(vecShipPos, new Vector3d());

        return VectorConversionsMCKt.toMinecraft(vecWorldPos);
    }

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

    public static boolean CheckAbovePocket(AABB Airpocket, Level level) {
        for (double x = Airpocket.minX; x < Airpocket.maxX; x++) {
            for (double z = Airpocket.minZ; z < Airpocket.maxZ; z++) {
                BlockPos blockPos = new BlockPos((int) x, (int) Airpocket.maxY, (int) z).below();

                boolean meo = false;
                while (!meo) {
                    meo = true;
                    for (int b = AirPockets.size() - 1; b >= 0; b--) {
                        AABB AirpocketB = AirPockets.get(b);
                        if (AirpocketB != null && AirpocketB.contains(blockPos.getCenter())) {
                            blockPos = blockPos.above();
                            meo = false;
                        }
                    }
                }

                boolean IsAir = level.getBlockState(
                        VSCompat.valkShipToWorld(level, blockPos)
                ).isAir();

                if (level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty() && !IsAir) {
                    return false;
                }

            }
        }
        return true;
    }

    public static boolean IsInAirpocket(ArrayList<Vec3> jomlVectors, Level level) {
        try {
            AABB Airpocket = null;
            for (int i = AirPockets.size() - 1; i >= 0; i--) {
                for (Vec3 jomlVector : jomlVectors) {
                    if (AirPockets.get(i) != null && AirPockets.get(i).contains(jomlVector)) {
                        Airpocket = AirPockets.get(i);
                        break;
                    }
                }
            }

            if (Airpocket != null) {
                return CheckAbovePocket(Airpocket, level);
            }

        } catch (IndexOutOfBoundsException ignored) {}

        return false;
    }

    public static boolean IsInAirpocket(Vec3 jomlVector, Level level) {
        try {
            AABB Airpocket = null;
            for (int i = AirPockets.size() - 1; i >= 0; i--) {
                if (AirPockets.get(i) != null && AirPockets.get(i).contains(jomlVector)) {
                    Airpocket = AirPockets.get(i);
                    break;
                }
            }

            if (Airpocket != null) {
                return CheckAbovePocket(Airpocket, level);
            }

        } catch (IndexOutOfBoundsException ignored) {}

        return false;
    }

    public static ArrayList<ArrayList<BlockPos>> ProcessShip(Map.Entry<ArrayList<ArrayList<BlockPos>>, Integer> LayersKeys, ServerLevel SL, Ship ship) {
        ArrayList<ArrayList<BlockPos>> AllLayers = LayersKeys.getKey();
        ArrayList<BlockPos> Layer = AllLayers.get(LayersKeys.getValue());

        BlockPos Start = null;
        BlockPos End;

        ArrayList<BlockPos> Filter = new ArrayList<>();

            for (int x = ship.getShipAABB().minX(); x < ship.getShipAABB().maxX(); x++) {
                for (int y = ship.getShipAABB().minY(); y < ship.getShipAABB().maxY(); y++) {
                for (int z = ship.getShipAABB().minZ(); z < ship.getShipAABB().maxZ(); z++) {

                    BlockPos currentPos = new BlockPos(x, y, z);
                    if (!SL.getBlockState(currentPos).getCollisionShape(SL, currentPos).isEmpty()) {
                        Filter.add(currentPos);
                    }
                }
            }
        }

        int i = 0;
        while (i < Layer.size()) {
            BlockPos blockPos = Layer.get(i);

            BlockPos Ray1 = Casts.BlockCastEast(blockPos, SL, Filter, false);
            BlockPos Ray2 = Casts.BlockCastNorth(blockPos, SL, Filter, false);
            BlockPos Ray3 = Casts.BlockCastSouth(blockPos, SL, Filter, false);
            BlockPos Ray4 = Casts.BlockCastWest(blockPos, SL, Filter, false);

            boolean T1 = (!SL.getBlockState(Ray1).getCollisionShape(SL, Ray1).isEmpty() && SL.getFluidState(Ray1).isEmpty());
            boolean T2 = (!SL.getBlockState(Ray2).getCollisionShape(SL, Ray2).isEmpty() && SL.getFluidState(Ray2).isEmpty());
            boolean T3 = (!SL.getBlockState(Ray3).getCollisionShape(SL, Ray3).isEmpty() && SL.getFluidState(Ray3).isEmpty());
            boolean T4 = (!SL.getBlockState(Ray4).getCollisionShape(SL, Ray4).isEmpty() && SL.getFluidState(Ray4).isEmpty());

            boolean jingaling = T1 && T2 && T3 & T4;

            if (!jingaling) {
                ExcludedBlocks.add(blockPos);
                Layer.remove(blockPos);
                continue;
            }

            if (Start == null || (Start.getX()+Start.getZ()) > (blockPos.getX()+blockPos.getZ())) {
                Start = blockPos;
            }
            i++;
        }

        if (Start == null) {return AllLayers;}
        Layer.remove(Start);

        BlockPos Ray1 = Casts.BlockCastEast(Start, SL, Filter, false).west();

        End = Ray1;

        BlockPos Ray2 = Casts.BlockCastSouth(Ray1, SL, Filter, false).north();

        boolean ExtendAirpocket = false;
        for (double x = Start.getX(); x <= Ray2.getX(); x++) {
            for (double z = Start.getZ(); z <= Ray2.getZ(); z++) {
                BlockPos block = new BlockPos((int) x, Start.getY(), (int) z);
                if (SL.getBlockState(block).getCollisionShape(SL, block).equals(Shapes.block())) {
                    ExtendAirpocket = true;
                    break;
                }
            }
        }

        if (!ExtendAirpocket) {
            End = Ray2;
        }
        Layer.remove(End);

        boolean Exclude = false;
        for (double x = Start.getX(); x < End.getX(); x++) {
            for (double y = Start.getY(); y < End.getY(); y++) {
                for (double z = Start.getZ(); z < End.getZ(); z++) {
                    BlockPos block = new BlockPos((int) x, (int) y, (int) z);

                    BlockPos West = block.west();
                    BlockPos South = block.south();
                    BlockPos East = block.east();
                    BlockPos North = block.north();
                    BlockPos Below = block.above();

                    if (!Exclude) {
                        Exclude = ExcludedBlocks.contains(West);
                    }
                    if (!Exclude) {
                        Exclude = ExcludedBlocks.contains(South);
                    }
                    if (!Exclude) {
                        Exclude = ExcludedBlocks.contains(East);
                    }
                    if (!Exclude) {
                        Exclude = ExcludedBlocks.contains(North);
                    }
                    if (!Exclude) {
                        Exclude = ExcludedBlocks.contains(Below);
                    }

                    Layer.remove(block);
                }
            }
        }

        if (Exclude) {
            for (double x = Start.getX(); x < End.getX(); x++) {
                for (double y = Start.getY(); y < End.getY(); y++) {
                    for (double z = Start.getZ(); z < End.getZ(); z++) {
                        BlockPos block = new BlockPos((int) x, (int) y, (int) z);
                        ExcludedBlocks.add(block);
                    }
                }
            }
            return AllLayers;
        }

        Vec3 AirBlockStart = Start.getCenter();
        Vec3 AirBlockEnd = End.getCenter();

        Vec3 Min = new Vec3(
                AirBlockStart.x()-.5,
                AirBlockStart.y()-.5,
                AirBlockStart.z()-.5
        );

        Vec3 Max = new Vec3(
                AirBlockEnd.x()+.5,
                AirBlockEnd.y()+.5,
                AirBlockEnd.z()+.5
        );

        AABB AirPocket = new AABB(Min, Max);

        AirPockets.add(AirPockets.size(), AirPocket);
        AirpocketToShip.put(AirPocket, ship);

        Vec3 AirBlockStartVisuals = VSCompat.TEST(
                SL,
                Start
        );

        Vec3 AirBlockEndVisuals = VSCompat.TEST(
                SL,
                End
        );

        if (PressurizedMain.DevMode) {
            Vec3 MinVisuals = new Vec3(
                    AirBlockStartVisuals.x()-.5,
                    AirBlockStartVisuals.y()-.5,
                    AirBlockStartVisuals.z()-.5
            );

            Vec3 MaxVisuals = new Vec3(
                    AirBlockEndVisuals.x()+.5,
                    AirBlockEndVisuals.y()+.5,
                    AirBlockEndVisuals.z()+.5
            );

            AABB AirPocketVisuals = new AABB(MinVisuals, MaxVisuals);
            AirPocketsVisuals.put(AirPocket, AirPocketVisuals);
        }

        return AllLayers;
    }

    public static void RegisterShipAirpockets(ServerLevel serverLevel) {
        if (ModList.get().isLoaded("valkyrienskies") && ServerConfigs.Airpockets.get()) {
            //AirPocketsVisuals.clear();
            ExcludedBlocks.clear();
            //AirPockets.clear();
            Debounce = true;

            try {
                for (Ship ship : VSGameUtilsKt.getAllShips(serverLevel)) {
                    ArrayList<ArrayList<BlockPos>> Shipblocks = new ArrayList<>();

                    AABBic shipAABB = ship.getShipAABB();
                    if (shipAABB != null) {
                        for (int y = shipAABB.minY(); y < shipAABB.maxY(); y++) {
                            final ArrayList<BlockPos> meow = new ArrayList<>();

                            for (int x = shipAABB.minX(); x < shipAABB.maxX(); x++) {
                                for (int z = shipAABB.minZ(); z < shipAABB.maxZ(); z++) {

                                    BlockPos currentPos = new BlockPos(x, y, z);
                                    BlockState blockState = serverLevel.getBlockState(currentPos);

                                    if (blockState.getCollisionShape(serverLevel, currentPos).isEmpty()) {
                                        boolean Found = false;
                                        for (int i = AirPockets.size() - 1; i >= 0; i--) {
                                            AABB Airpocket = AirPockets.get(i);

                                            if (Airpocket != null && Airpocket.contains(currentPos.getCenter())) {
                                                Found = true;
                                            }
                                        }

                                        if (!Found) {
                                            meow.add(currentPos);
                                        }
                                    }
                                }
                            }
                            Shipblocks.add(meow);
                        }
                    }

                    int i = 0;
                    while (i < Shipblocks.size()) {
                        Map.Entry<ArrayList<ArrayList<BlockPos>>, Integer> entry = Map.entry(Shipblocks, i);
                        Shipblocks = ProcessShip(
                                entry,
                                serverLevel,
                                ship
                        ); //building airpockets
                        try {
                            if (Shipblocks.get(i).isEmpty()) {
                                i++;
                            }
                        } catch (IndexOutOfBoundsException ignore) {}
                    }
                    Shipblocks.clear();
                } //stop iterating through VS ships

                ArrayList<AABB> ExcludedAirpockets = new ArrayList<>();
                for (AABB Airpocket : AirPockets) {
                    for (double x = Airpocket.minX; x < Airpocket.maxX; x++) {
                        for (double y = Airpocket.minY; y < Airpocket.maxY; y++) {
                            for (double z = Airpocket.minZ; z < Airpocket.maxZ; z++) {
                                BlockPos block = new BlockPos((int) x, (int) y, (int) z);

                                BlockPos West = block.west();
                                BlockPos South = block.south();
                                BlockPos East = block.east();
                                BlockPos North = block.north();
                                BlockPos Below = block.above();

                                boolean Exclude = false;

                                if (!Exclude) {
                                    Exclude = ExcludedBlocks.contains(West);
                                }
                                if (!Exclude) {
                                    Exclude = ExcludedBlocks.contains(South);
                                }
                                if (!Exclude) {
                                    Exclude = ExcludedBlocks.contains(East);
                                }
                                if (!Exclude) {
                                    Exclude = ExcludedBlocks.contains(North);
                                }
                                if (!Exclude) {
                                    //Exclude = ExcludedBlocks.contains(Below);
                                }

                                if (Exclude) {
                                    ExcludedAirpockets.add(Airpocket);
                                }
                            }
                        }
                    }
                }

                for (AABB Airpocket : ExcludedAirpockets) {
                    AirPocketsVisuals.remove(Airpocket);
                    AirPockets.remove(Airpocket);
                }

                int i = 0;
                while (i < AirPockets.size()) {
                    AABB Airpocket = AirPockets.get(i);

                    BlockPos Min = new BlockPos((int) Airpocket.minX, (int) Airpocket.minY, (int) Airpocket.minZ);
                    BlockPos Max = new BlockPos((int) Airpocket.maxX, (int) Airpocket.maxY, (int) Airpocket.maxZ);

                    Networking.CHANNEL6.send(PacketDistributor.ALL.noArg(), new UpdateAP(i, Min, Max));
                    i++;
                }

            } catch (ConcurrentModificationException ignored) {}
        }
    }

    static final HashMap<Ship, Vector3dc> SpeedTracker = new HashMap<>();
    static final Vector3d idfk = new Vector3d();
    static final HashMap<Long, Long> ShipStress = new HashMap<>();

    public static void CollisionDamage(CollisionEvent event) {
        ServerLevel level = null;
        for (ServerLevel serverLevel : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
            if (VSGameUtilsKt.getDimensionId(serverLevel).equals(event.getDimensionId())) {
                level = serverLevel;
                break;
            }
        }

        final ServerLevel serverLevel = level;

        if (level != null && ShipStress.containsKey(event.getShipIdA())) {
            event.getContactPoints().forEach(collisionContactPoint -> {
                Ship ship = ValkyrienSkies.getShipById(serverLevel, event.getShipIdA());
                if (ship == null) {return;}

                Vec3 vec3 = VSCompat.WorldToValkShip(
                        VectorConversionsMCKt.toMinecraft(
                                collisionContactPoint.getPosition()
                        ),
                        ship
                );

                BlockPos blockPos = BlockPos.containing(vec3);
                long StressResistance = (long) (serverLevel.getBlockState(blockPos).getBlock().getExplosionResistance()*100000);

                if (ShipStress.get(event.getShipIdA()) > StressResistance && StressResistance >= 1) {

                    //                            BlockHitResult context = serverLevel.clip(new ClipContext(
                    //                                    VectorConversionsMCKt.toMinecraft(
                    //                                            collisionContactPoint.getPosition()
                    //                                    ),
                    //                                    vec3.add(Velocity),
                    //                                    ClipContext.Block.COLLIDER,
                    //                                    ClipContext.Fluid.NONE,
                    //                                    null
                    //                            ));
                    //
                    //if (context.getType() == HitResult.Type.MISS) {return;}

                    int Progress = java.lang.Math.toIntExact((ShipStress.get(event.getShipIdA()) / StressResistance));

                    boolean Found = false;
                    for (Map.Entry<Integer, Map.Entry<BlockPos, Integer>> BlockMap : CrushedBlocks.entrySet()) {
                        if (BlockMap.getValue().getKey().equals(blockPos)) {
                            Progress += BlockMap.getValue().getValue();
                            Map.Entry<BlockPos, Integer> entry = BlockMap.getValue();
                            BlockMap.setValue(Map.entry(entry.getKey(), Progress));
                            Found = true;
                            break;
                        }
                    }

                    if (!Found) {
                        Map.Entry<BlockPos, Integer> entry = Map.entry(blockPos, 0);

                        CrushedBlocks.put(CrushedBlocks.size()+1, entry);
                        Networking.CHANNEL3.send(
                                PacketDistributor.ALL.noArg(),
                                new UpdateCBArray(CrushedBlocks.size(), blockPos)
                        );
                    }
                    if (Progress > 7) {
                        serverLevel.destroyBlock(blockPos, true);
                        serverLevel.addDestroyBlockEffect(blockPos, serverLevel.getBlockState(blockPos));
                    } else {
                        Networking.CHANNEL4.send(
                                PacketDistributor.ALL.noArg(),
                                new UpdateCBTexture(
                                        blockPos,
                                        Progress
                                )
                        );
                    }
                }
            });
        }
    }

    public static void drawMaskBox(PoseStack poseStack, AABB box, double CamX, double CamY, double CamZ) {
        Ship ship = VSCompat.AirpocketToShip.get(box);
        if (ship == null) return; {}

        poseStack.pushPose();
        Vec3 Cam = new Vec3(CamX, CamY, CamZ);
        Cam = VSCompat.WorldToValkShip(Cam, ship);

        double centerX = box.getCenter().x;
        double centerY = box.getCenter().y;
        double centerZ = box.getCenter().z;

        double dx = centerX - Cam.x;
        double dy = centerY - Cam.y;
        double dz = centerZ - Cam.z;

        Vector3d YXZVec3 = ship.getTransform().getRotation().getEulerAnglesYXZ(new Vector3d());

        float pitchDegrees = (float) Math.toDegrees(YXZVec3.x());
        float yawDegrees = (float) Math.toDegrees(YXZVec3.y());
        float rollDegrees = (float) Math.toDegrees(YXZVec3.z());

        poseStack.mulPose(Axis.YP.rotationDegrees(yawDegrees));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitchDegrees));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rollDegrees));
        poseStack.translate(dx, dy, dz);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        float halfX = (float) box.getXsize() / 2f;
        float ySize = (float) box.getYsize()-0.5f;
        float halfZ = (float) box.getZsize() / 2f;

        bufferbuilder.vertex(matrix, -halfX, ySize, -halfZ).endVertex();
        bufferbuilder.vertex(matrix, -halfX, ySize, halfZ).endVertex();
        bufferbuilder.vertex(matrix, halfX, ySize, halfZ).endVertex();
        bufferbuilder.vertex(matrix, halfX, ySize, -halfZ).endVertex();

        ySize = ySize-0.2f;

        bufferbuilder.vertex(matrix, -halfX, ySize, -halfZ).endVertex();
        bufferbuilder.vertex(matrix, halfX, ySize, -halfZ).endVertex();
        bufferbuilder.vertex(matrix, halfX, ySize, halfZ).endVertex();
        bufferbuilder.vertex(matrix, -halfX, ySize, halfZ).endVertex();

        tesselator.end();
        poseStack.popPose();
    }

    private static ConcurrentHashMap<Integer, CollisionEvent> fish = new ConcurrentHashMap<>();

    public static void CommonSetup(FMLCommonSetupEvent event) {
        ValkyrienSkies.api().getCollisionPersistEvent().on(collisionPersistentEvent -> {
            if (ServerConfigs.CollisionDamage.get()) {
                fish.put(fish.size()+1, collisionPersistentEvent);
            }
        });
    }

    public static void OnSTick(TickEvent.ServerTickEvent event) {
        Iterator<Map.Entry<Integer, CollisionEvent>> it = fish.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Integer, CollisionEvent> entry = it.next();
            CollisionEvent collisionEvent = entry.getValue();

            VSCompat.CollisionDamage(collisionEvent);

            it.remove();
        }
        // for (CollisionEvent collisionEvent:fish) {

        // }

        for (ServerLevel level: event.getServer().getAllLevels()) {
            for (Ship ship: VSGameUtilsKt.getAllShips(level)) {
                LoadedServerShip ServerShip = VSGameUtilsKt.getLoadedShipManagingPos(
                        level,
                        ship.getTransform().getPositionInShip()
                );
                if (ServerShip == null) {continue;}

                Vector3dc CurrentVelocity = ship.getVelocity();

                if (SpeedTracker.get(ship) != null) {
                    Vector3dc PreviousVelocity = SpeedTracker.get(ship);
                    Vector3d deltaV = CurrentVelocity.sub(PreviousVelocity, idfk);

                    double deltaVAmount = deltaV.length();
                    double acceleration = deltaVAmount / 0.05;
                    double gForce = acceleration / 9.80665;

                    double Mass = ServerShip.getInertiaData().getMass();
                    long totalForce = (long) (Mass * (gForce * 9.80665));

                    SpeedTracker.replace(ship, CurrentVelocity);
                    ShipStress.put(ship.getId(), totalForce);

                } else {
                    SpeedTracker.put(ship, CurrentVelocity);
                }
            }
        }
    }

}