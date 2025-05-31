package net.dawn.Pressurized;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dawn.Pressurized.Network.CrushDamagePlayerPacket;
import com.mojang.logging.LogUtils;
import net.dawn.Pressurized.Network.BaroDamagePlayerPacket;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.dawn.Pressurized.BlocksResistanceData.*;

@Mod(PressurizedMain.MODID)
public class PressurizedMain {
    public static final String MODID = "pressurized";
    private static final Logger LOGGER = LogUtils.getLogger();

    static HashMap<String, Integer> BlocksPressureResistance = new HashMap<>();
    //static ArrayList<HashMap<BlockPos, Integer>> CrushedBlocks = new ArrayList<>();
    static HashMap<Integer, HashMap<BlockPos, Integer>> CrushedBlocks = new HashMap<>();

    static final Collection<BlockPos> SkipThread = new CopyOnWriteArrayList<>();
    static final Map<BlockPos, HashMap<Thread, Boolean>> DestroyThreads = new HashMap<>();
    static final ArrayList<Integer> RemovalCollection = new ArrayList<>();
    static final ArrayList<BlockPos> RemovedWater = new ArrayList<>();

    public PressurizedMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);

        Networking.register();
        ModSounds.Register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfigs.SPEC, "pressurized-Common.toml");
        BlocksPressureResistance.put("bedrock", 100000);

        ForgeRegistries.BLOCKS.getEntries().forEach(entry -> {
            String Name = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(entry.getValue())).getPath();
            final boolean[] Add = {true};
            final int[] Resistance = new int[]{10};

            for (Map.Entry<String, Integer> stringIntegerHashMap : BlocksPressureResistance.entrySet()) {

                for (String BlockName : StoneBlocks) {
                    if (BlockName.equals(Name)) {
                        Resistance[0] = 40;
                        break;
                    }
                }
                for (String BlockName : WoodBlocks) {
                    if (BlockName.equals(Name)) {
                        Resistance[0] = 10;
                        break;
                    }
                }
                for (String BlockName : MetalBlocks) {
                    if (BlockName.equals(Name)) {
                        Resistance[0] = 80;
                        break;
                    }
                }

                if (stringIntegerHashMap.getKey().equals(Name)) {
                    Add[0] = false;
                    break;
                }
            }
            if (Add[0]) {
                BlocksPressureResistance.put(Name, Resistance[0]);
            }
        });

    }

    double BodyPressure = 0;
    double PressureBuildup = 0;
    double CrushDepth = -100;
    double PlayerYPos = 0;

    double DivingDepth = 0;
    double depth = 0;

    int CamShake = 1;
    int ServerTicks = 0;

    Boolean OverPressured = false;
    Boolean PressureImmunity = false;
    Boolean CrushImmunity = false;

    Boolean ValidHelmet = false;
    Boolean ValidChestPlate = false;
    Boolean ValidLeggings = false;
    Boolean ValidBoots = false;

    Boolean HullDamageThread = false;

    public void BlockScan(BlockPos blockPos, Level level) { // i geneuinely hate this code and needs a lot of cleaning
        if (!level.getBlockState(blockPos.above()).liquid()) {
            return;
        }
        HashMap<Integer, BlockPos> DistancePos = BlockposDepth(blockPos, level);
        int BlockDepth = 0;

        for (Map.Entry<Integer, BlockPos> entry : DistancePos.entrySet()) {
            if (!(entry.getKey() == null)) {
                BlockDepth = entry.getKey();
            }
            if (!(entry.getValue() == null)) {
                blockPos = entry.getValue();
            }
        }

        String name = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(level.getBlockState(blockPos).getBlock())).getPath();
        BlockState blockState = level.getBlockState(blockPos);

        if (blockState.isSolid() & BlocksPressureResistance.containsKey(name)) {
            int Resistance = BlocksPressureResistance.get(name);

            BlockPos LowerBlockPos = blockPos.below();
            String LowerBlockName = Objects.requireNonNull(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(level.getBlockState(LowerBlockPos).getBlock())).getPath());
            while (!LowerBlockName.equals("void_air") & !LowerBlockName.equals("water") & !LowerBlockName.equals("air") & BlocksPressureResistance.containsKey(LowerBlockName)) {
                Resistance += (BlocksPressureResistance.get(LowerBlockName));
                LowerBlockPos = LowerBlockPos.below();
                LowerBlockName = Objects.requireNonNull(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(level.getBlockState(LowerBlockPos).getBlock())).getPath());
                //  if (depth*CommonConfigs.CrushDepthMultiplier.get() > -Resistance) {
                //        break;
                //   }
            }

            if (BlockDepth * CommonConfigs.CrushDepthMultiplier.get() >= Resistance) {

                if (CrushedBlocks.isEmpty() & CrushedBlocks.size() < CommonConfigs.MaxBlocksDestructionCapacity.get()) {
                    HashMap<BlockPos, Integer> map = new HashMap<>();
                    map.put(blockPos, 0);
                    CrushedBlocks.put(CrushedBlocks.size()+1, map);
                }

                boolean found = false;
                for (Map.Entry<Integer, HashMap<BlockPos, Integer>> BlockMap : CrushedBlocks.entrySet()) {
                    if (BlockMap.getValue().containsKey(blockPos)) {
                        found = true;
                        break;
                    }
                }

                if (!found & CrushedBlocks.size() < CommonConfigs.MaxBlocksDestructionCapacity.get()) {
                    HashMap<BlockPos, Integer> map = new HashMap<>();
                    map.put(blockPos, 0);

                    CrushedBlocks.put(CrushedBlocks.size()+1, map);
                }

                for (BlockPos B : DestroyThreads.keySet()) {
                    HashMap<Thread, Boolean> IDK = DestroyThreads.get(B);
                    for (Thread T : IDK.keySet()) {
                        Boolean InterruptThread = IDK.get(T);
                        if (InterruptThread) {
                            T.interrupt();
                        }
                    }
                }

                if (ServerTicks >= CommonConfigs.BlockScanRate.get()) {
                    for (Map.Entry<Integer, HashMap<BlockPos, Integer>> BlockMap : CrushedBlocks.entrySet()) {
                        for (Map.Entry<BlockPos, Integer> entry : BlockMap.getValue().entrySet()) {
                            BlockPos key = entry.getKey();
                            boolean SkipIteration = false;

                            try {
                                for (BlockPos bp : SkipThread) {
                                    if (bp.equals(key)) {
                                        SkipIteration = true;
                                        break;
                                    }
                                }
                            } catch (NullPointerException e) {
                                throw new RuntimeException();
                            }

                            if (SkipIteration) {
                                continue;
                            }

                            if (level.getBlockState(key.above()).isSolid()) {
                                RemovalCollection.add(BlockMap.getKey());
                                continue;
                            }
                            SkipThread.add(key);

                            Thread t = new Thread(() -> {
                                long Delay = new Random().nextLong(0, 5000);
                                try {
                                    Thread.sleep(Delay);
                                    if (!Thread.currentThread().isInterrupted()) {
                                        Minecraft.getInstance().execute(() -> {
                                            if (entry.getValue() > 7) {
                                                level.getBlockState(key);
                                                synchronized (RemovalCollection) {
                                                    RemovalCollection.add(BlockMap.getKey());
                                                }
                                                level.destroyBlock(key, true);
                                                level.addDestroyBlockEffect(key, level.getBlockState(key));
                                            } else {
                                                entry.setValue(entry.getValue() + 1);
                                                //    level.destroyBlockProgress(entry.getValue(), key, entry.getValue());
                                            }
                                            synchronized (SkipThread) {
                                                SkipThread.remove(key);
                                            }
                                            synchronized (DestroyThreads) {
                                                DestroyThreads.get(key).replace(Thread.currentThread(), true);
                                            }
                                        });
                                    }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            });
                            HashMap<Thread, Boolean> Data = new HashMap<>();
                            Data.put(t, false);
                            DestroyThreads.put(key, Data);
                            t.start();
                        }
                    }
                }
                ServerTicks = 0;
            }
        } else {
            for (Map.Entry<Integer, HashMap<BlockPos, Integer>> BlockMap : CrushedBlocks.entrySet()) {
                for (Map.Entry<BlockPos, Integer> entry : BlockMap.getValue().entrySet()) {
                    if (entry.getKey().equals(blockPos)) {
                        RemovalCollection.add(BlockMap.getKey());
                    }
                }
            }
        }
        for (Integer Num : RemovalCollection) {
            CrushedBlocks.remove(Num);
        }
        RemovalCollection.clear();
    }

    public static HashMap<Integer, BlockPos> BlockposDepth(BlockPos BlockPos, Level World) {
        BlockPos currentPos = BlockPos.above();
        BlockPos highestWaterBlock = BlockPos;

        BlockPos NewBlockpos = null;
        int Distance;
        HashMap<Integer, net.minecraft.core.BlockPos> DistancePos = new HashMap<>();

        while (currentPos.getY() < World.getHeight()) {
            if (World.getBlockState(currentPos).liquid()) {
                highestWaterBlock = currentPos;
            } else {
                break;
            }
            currentPos = currentPos.above();
        }

        Distance = highestWaterBlock.getY() - BlockPos.getY();

        BlockHitResult context = World.clip(new ClipContext(
                highestWaterBlock.getCenter(),
                BlockPos.getCenter(),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                null
        ));

        if (context.getType() == HitResult.Type.BLOCK) {
            Distance = (int) highestWaterBlock.getCenter().distanceTo(BlockPos.getCenter());
            NewBlockpos = context.getBlockPos();
        }

        DistancePos.put(Distance, NewBlockpos);

        return DistancePos;
    }

    public static double PlayerDepth(BlockPos BlockPos, Level World) {
        BlockPos currentPos = BlockPos.above();
        BlockPos highestWaterBlock = BlockPos;

        while (currentPos.getY() < World.getHeight()) {
            if (World.getBlockState(currentPos).getFluidState().is(Fluids.WATER) || World.getBlockState(currentPos).getBlock() == Blocks.WATER) {
                highestWaterBlock = currentPos;
            } else {
                break;
            }
            currentPos = currentPos.above();
        }
        return highestWaterBlock.getY();
    }

    boolean TEST = false;

    @SubscribeEvent
    public void OnCTick(TickEvent.RenderTickEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (event.phase == TickEvent.Phase.END & event.side == LogicalSide.CLIENT) {
            Camera c = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vec3 vec3 = c.getPosition();
            double d0 = vec3.x();
            double d1 = vec3.y();
            double d2 = vec3.z();

            if (!TEST) {
                new Thread(() -> {
                    //TEST = true;
                    Minecraft.getInstance().execute(() -> {
                        for (Map.Entry<Integer, HashMap<BlockPos, Integer>> BlockMap : CrushedBlocks.entrySet()) {
                            for (Map.Entry<BlockPos, Integer> entry : BlockMap.getValue().entrySet()) {
                                //if (true) {return;}

                                PoseStack poseStack = new PoseStack();
                                poseStack.mulPose(Axis.XP.rotationDegrees(c.getXRot()));
                                poseStack.mulPose(Axis.YP.rotationDegrees(c.getYRot() + 180.0F));
                                poseStack.pushPose();
                                poseStack.translate(entry.getKey().getX() - d0, entry.getKey().getY() - d1, entry.getKey().getZ() - d2);
                                PoseStack.Pose posestack$pose = poseStack.last();

                                VertexConsumer shit = mc.renderBuffers().bufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(entry.getValue()));
                                VertexConsumer vertexconsumer = new SheetedDecalTextureGenerator(
                                        shit,
                                        posestack$pose.pose(),
                                        poseStack.last().normal(), 1.0F);

                                assert mc.player != null;
                                BlockState blockState = mc.player.level().getBlockState(entry.getKey());
                                Minecraft.getInstance().getBlockRenderer().renderBreakingTexture(
                                        blockState,
                                        BlockPos.of(entry.getValue()),
                                        mc.player.level(),
                                        poseStack,
                                        vertexconsumer);
                                poseStack.popPose();
                            }
                        }
                    });
                }).start();
            }
        }

        if (event.phase == TickEvent.Phase.START & event.side == LogicalSide.CLIENT) {
            assert mc.player != null;
            if (OverPressured & !mc.isPaused() & mc.cameraEntity != null) {
                if (CamShake >= 1) {
                    CamShake = (int) (-1 * (Math.abs(depth - BodyPressure)));
                } else {
                    CamShake = (int) (1 * (Math.abs(depth - BodyPressure)));
                }

                mc.cameraEntity.setYRot(mc.player.getYHeadRot() + (float) CamShake / 20);
            }
        }
    }

    @SubscribeEvent
    public void OnLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() == Minecraft.getInstance().player) {
            OverPressured = false;
            ValidHelmet = false;
            ValidChestPlate = false;
            ValidLeggings = false;
            ValidBoots = false;
        }
    }

    @SubscribeEvent
    public void PRespawned(PlayerEvent.PlayerRespawnEvent event) {
        assert Minecraft.getInstance().player != null;
        if (Objects.equals(event.getEntity().getName().toString(), Minecraft.getInstance().player.getName().toString())) {
            PressureBuildup = 0;
            BodyPressure = 0;
            OverPressured = false;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void OnPTick(TickEvent.PlayerTickEvent event) {
        Minecraft mc = Minecraft.getInstance();

//& (event.player.getVehicle() != null & event.player.getVehicle().getName().getString().equals("Submarine"))
        if (event.player != Minecraft.getInstance().player) {
            return;
        }

        if (!HullDamageThread) {
            // if (!player.isUnderWater() || CrushedBlocks.isEmpty()) {return;}
            HullDamageThread = true;
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                HullDamageThread = false;
                if (!event.player.isUnderWater() || CrushedBlocks.isEmpty()) {
                    return;
                }

                int X = 0;
                int Y = 0;
                int Z = 0;

                for (Map.Entry<Integer, HashMap<BlockPos, Integer>> BlockMap : CrushedBlocks.entrySet()) {
                    for (Map.Entry<BlockPos, Integer> entry : BlockMap.getValue().entrySet()) {
                        BlockPos key = entry.getKey();

                        X += key.getX();
                        Y += key.getY();
                        Z += key.getZ();
                    }
                }

                X /= CrushedBlocks.size();
                Y /= CrushedBlocks.size();
                Z /= CrushedBlocks.size();

                BlockPos blockPos = new BlockPos(X, Y, Z);
                BlockState Blockstate = event.player.level().getBlockState(blockPos);

                double deltaX = event.player.getOnPos().getX() - blockPos.getX();
                double deltaY = event.player.getOnPos().getY() - blockPos.getY();
                double deltaZ = event.player.getOnPos().getZ() - blockPos.getZ();

                double Distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ));
                String Name = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(Blockstate.getBlock())).getPath();

                for (String BlockName : MetalBlocks) {
                    if (BlockName.equals(Name)) {
                        event.player.playSound(ModSounds.HULLDAMAGE.get(), (float) (1f / Distance), 1f);
                    }
                }

                for (String BlockName : StoneBlocks) {
                    if (BlockName.equals(Name)) {
                        event.player.playSound(ModSounds.ENVIRONMENTDAMAGE.get(), (float) (1f / Distance), 1f);
                    }
                }

                // event.player.level().playSound(null, X, Y, Z,
                //         ModSounds.HULLDAMAGE.get(), SoundSource.BLOCKS,(float) (1f/Distance), 1f);
            }).start();
        }

        new Thread(() -> {
            if (PressureImmunity) {
                BodyPressure = depth;
            } else {
                if (BodyPressure > depth) {
                    BodyPressure -= .075;
                } else if (BodyPressure < depth) {
                    BodyPressure += .075;
                } else if (BodyPressure - depth >= .025) {
                    BodyPressure = depth;
                }
            }

            if (CrushImmunity || depth > CrushDepth) {
                if (PressureBuildup > 0) {
                    PressureBuildup -= .1;
                    if (PressureBuildup < 0) {
                        PressureBuildup = 0;
                    }
                }
            }
        }).start();

        if (event.player.isUnderWater()) {
            if (event.player.isCreative()) {
                return;
            }

            DivingDepth = PlayerDepth(event.player.getOnPos(), event.player.level());
            depth = event.player.getY() - DivingDepth;

            //            if (event.player.getVehicle() != null) {
            //                System.out.println(event.player.getVehicle().getName().toString().equals("Submarine"));
            //                if (event.player.getVehicle().getName().toString().equals("Submarine")) {
            //                    return;
            //                }
            //            } else {
            //                assert Minecraft.getInstance().player != null;
            //                Minecraft.getInstance().player.sendSystemMessage(Component.literal(event.player.getVehicle()));
            //            }

            String Helmet = event.player.getItemBySlot(EquipmentSlot.HEAD).getItem().toString();
            String ChestPlate = event.player.getItemBySlot(EquipmentSlot.CHEST).getItem().toString();
            String Leggings = event.player.getItemBySlot(EquipmentSlot.LEGS).getHoverName().getString();
            String Boots = event.player.getItemBySlot(EquipmentSlot.FEET).getHoverName().getString();

            for (String Gear : CommonConfigs.ValidHelmets.get()) {
                ValidHelmet = (Helmet.equals(Gear) || !CommonConfigs.HelmetRequired.get());
            }

            for (String Gear : CommonConfigs.ValidChestPlates.get()) {
                ValidChestPlate = (ChestPlate.equals(Gear) || !CommonConfigs.ChestPlateRequired.get());
            }

            for (String Gear : CommonConfigs.ValidLeggings.get()) {
                ValidLeggings = (Leggings.equals(Gear) || !CommonConfigs.LeggingsRequired.get());
            }

            for (String Gear : CommonConfigs.ValidBoots.get()) {
                ValidBoots = (Boots.equals(Gear) || !CommonConfigs.BootsRequired.get());
            }

            if (ValidHelmet & ValidChestPlate & ValidLeggings & ValidBoots) {
                PressureImmunity = true;
                CrushDepth = -400 * CommonConfigs.CrushDepthMultiplier.get();
            } else {
                PressureImmunity = false;
                CrushDepth = -100 * CommonConfigs.CrushDepthMultiplier.get();
            }

            if (!CrushImmunity & depth <= CrushDepth || !PressureImmunity & (depth - BodyPressure <= -5 & CommonConfigs.PressureDamage.get() || depth - BodyPressure >= 5 & CommonConfigs.ResurfaceDamage.get())) {
                OverPressured = true;
                if (event.player.hurtTime <= 0) {
                    if (depth - BodyPressure < 0 & event.player.isDescending()) {
                        Minecraft.getInstance().getSoundManager().stop(ModSounds.BAROTRAUMA.getId(), SoundSource.AMBIENT);
                        event.player.level().playLocalSound(event.player.getX(), event.player.getY(), event.player.getZ(), ModSounds.BAROTRAUMA.get(), SoundSource.AMBIENT, (float) (1f * (Math.abs(depth - BodyPressure))), 1f, false);
                        if (event.player.isAlive()) {
                            Networking.CHANNEL1.sendToServer(new BaroDamagePlayerPacket(2));
                        }
                    } else if (depth - BodyPressure > 0) {
                        Minecraft.getInstance().getSoundManager().stop(ModSounds.BAROTRAUMA.getId(), SoundSource.AMBIENT);
                        event.player.level().playLocalSound(event.player.getX(), event.player.getY(), event.player.getZ(), ModSounds.BAROTRAUMA.get(), SoundSource.AMBIENT, (float) (1f * (Math.abs(depth - BodyPressure))), 1f, false);
                        if (event.player.isAlive()) {
                            Networking.CHANNEL1.sendToServer(new BaroDamagePlayerPacket(2));
                        }
                    }
                }
                if (!CrushImmunity & depth <= CrushDepth) {
                    PressureBuildup += 0.025;
                }
            } else {
                OverPressured = false;
            }
            if (PressureBuildup >= 5 & event.player.isAlive()) {
                Networking.CHANNEL2.sendToServer(new CrushDamagePlayerPacket(100));
            }
        } else {
            OverPressured = false;
            // BodyPressure = depth;
        }
        PlayerYPos = event.player.getY();
    }

    @SubscribeEvent
    public void OnSTick(TickEvent.ServerTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END)) {
            return;
        }
        if (ServerTicks < CommonConfigs.BlockScanRate.get()) {
            ServerTicks++;
            return;
        }

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {

            BlockState AirBlock = null;
            int radius = CommonConfigs.BlockScanRadius.get();

            BlockPos center = player.getOnPos();
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos blockPos = center.offset(x, y, z);
                        if (blockPos.distSqr(center) <= radius * radius) {
                            if (!player.serverLevel().getBlockState(blockPos).liquid()) {
                                BlockScan(blockPos, player.level());
                                if (player.serverLevel().getBlockState(blockPos).isAir()) {
                                    AirBlock = player.serverLevel().getBlockState(blockPos);
                                }
                            } else {
                                BlockState blockState = player.level().getBlockState(blockPos);

                                // if ((blockPos.getCenter().distanceTo(player.getOnPos().getCenter())) <= 2) {
                                //      blockState. what was i cooking again :wilted_rose:
                                //was trying to mainpulate the water block property here lmao
                                // }

                                if (blockPos.equals(player.getOnPos()) || blockPos.equals(player.getOnPos().above())) {
                                    RemovedWater.add(blockPos);

                                    //  player.serverLevel().removeBlock(blockPos, false);
                                    //  player.serverLevel().removeBlockEntity(blockPos);

                                    assert AirBlock != null;
                                    //    player.serverLevel().setBlockAndUpdate(blockPos, AirBlock);
                                    //} else {
                                    //  for (BlockPos blockPos1 : RemovedWater) {
                                    //   if (blockPos1.equals(blockPos.equals(player.getOnPos()))) {
                                    //       break;
                                    //   }
                                    //   if (blockPos1.equals(blockPos.equals(player.getOnPos().above()))) {
                                    //       break;
                                    //  }
                                    // if (blockPos.equals(blockPos1)) {
                                    // RemovedWater.remove(blockPos);
                                    // player.level().setBlock(blockPos, blockState, 1);
                                    //   System.out.println(blockPos);
                                    //  }
                                    //   }

                                }
                            }
                        }
                    }
                }
            }
        }
        ServerTicks = 0;
    }


    //@SubscribeEvent
    public void LeftClick(PlayerInteractEvent.RightClickEmpty event) {
        //  if (!event.getLevel().isClientSide) {
        Vec3 playerPos = event.getEntity().getEyePosition(1.0f); // Eye position is used to simulate the player's viewpoint
        Vec2 lookDirection = event.getEntity().getRotationVector();

        double rayRange = 100.0;

        // Calculate the end point of the ray (start position + direction * range)
        Vec3 endPos = playerPos.add(lookDirection.x * rayRange, lookDirection.y * rayRange, 0 * rayRange);

        Level world = event.getLevel();
        BlockHitResult context = world.clip(new ClipContext(
                event.getEntity().getEyePosition(),
                endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                null
        ));

        if (context.getType() == HitResult.Type.BLOCK) {
            // world.getBlockState(context.getBlockPos()).
            double Distance = (event.getEntity().getEyePosition().distanceTo(endPos));
            LOGGER.warn(String.valueOf(event.getEntity().level().getBlockState(context.getBlockPos()).getBlock().getName()));
            BlockScan(context.getBlockPos(), event.getEntity().level());
            //  world.destroyBlockProgress(10, context.getBlockPos());
            //event.getLevel().destroyBlock(context.getBlockPos(),true);
        }
        //   }
    }

    @SubscribeEvent
    public void BlockDestroyed(BlockEvent.BreakEvent event) {
        LOGGER.warn("TEST");
        BlockPos blockPos = event.getPos();
        for (Map.Entry<Integer, HashMap<BlockPos, Integer>> BlockMap : CrushedBlocks.entrySet()) {
            for (Map.Entry<BlockPos, Integer> entry : BlockMap.getValue().entrySet()) {
                if (entry.getKey().equals(blockPos)) {
                    RemovalCollection.add(BlockMap.getKey());
                }
            }
        }
        //BlockEvent.FluidPlaceBlockEvent
    }
}