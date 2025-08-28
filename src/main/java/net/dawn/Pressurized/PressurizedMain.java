package net.dawn.Pressurized;

//code may not be perfect, as i am not too familiar with minecraft modding... but that does not bother me at the moment.

//NOTICE1: do NOT use == operator on BlockPos with another BlockPos, USE .equals INSTEAD.

//NOTICE2: Pressure damage and Crush damage are not the same source.
//Pressure: occurs when ascending/descending too fast.
//Crush: occurs when the Entity is at a great depth.

//NOTICE2: Ship blocks have 2 different positions.

//Position 1: Shipyard position.
//this position is static, which is important as it can be used as the key to the ship block.
//do be aware that Shipyard position is far away from its World position, so it should only be used as a key to the block,

//Position 2: World position.
//this position is dynamic, which is important to be aware of since it constantly changes and cannot act as a key to a ship block.

import com.mojang.logging.LogUtils;
import net.dawn.Pressurized.Client.ClientConfigs;
import net.dawn.Pressurized.Client.PressurizedHudOverlay;
import net.dawn.Pressurized.Network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Math;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.dawn.Pressurized.BlocksResistanceData.*;
import static net.dawn.Pressurized.PressurizedMain.ClientModEvents.BodyPressure;
import static net.dawn.Pressurized.PressurizedMain.ClientModEvents.Depth;

@Mod(PressurizedMain.MODID)
public class PressurizedMain {
    private PressurizedHudOverlay HudOverlay;
    public static final String MODID = "pressurized";
    private static final Logger LOGGER = LogUtils.getLogger();
    static HashMap<String, Integer> BlocksPressureResistance = new HashMap<>();
    public static final HashMap<Integer, Map.Entry<BlockPos, Integer>> CrushedBlocks = new HashMap<>();
    public static final HashMap<Entity, Integer> EntitiesDepth = new HashMap<>();

    static final Collection<BlockPos> SkipThread = new CopyOnWriteArrayList<>();
    static final Map<BlockPos, HashMap<Thread, Boolean>> DestroyThreads = new HashMap<>();
    static final ArrayList<Integer> RemovalCollection = new ArrayList<>();

    static int ServerTicks = 0;

    public PressurizedMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfigs.SPEC, "pressurized-Common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC, "pressurized-Client.toml");

        modEventBus.addListener(this::RegisterGui);
        modEventBus.addListener(this::clientSetup);

        Networking.register();
        ModSounds.Register(modEventBus);

        BlocksPressureResistance.put("bedrock", 100000);

        ForgeRegistries.BLOCKS.getEntries().forEach(entry -> {
            String Name = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(entry.getValue())).getPath();
            boolean Add = true;
            int Resistance = 10;

            for (Map.Entry<String, Integer> stringIntegerHashMap : BlocksPressureResistance.entrySet()) {

                for (String BlockName : StoneBlocks) {
                    if (BlockName.equals(Name)) {
                        Resistance = 40;
                        break;
                    }
                }
                for (String BlockName : WoodBlocks) {
                    if (BlockName.equals(Name)) {
                        Resistance = 10;
                        break;
                    }
                }
                for (String BlockName : MetalBlocks) {
                    if (BlockName.equals(Name)) {
                        Resistance = 80;
                        break;
                    }
                }

                if (stringIntegerHashMap.getKey().equals(Name)) {
                    Add = false;
                    break;
                }
            }
            if (Add) {
                BlocksPressureResistance.put(Name, Resistance);
            }
        });
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        HudOverlay = new PressurizedHudOverlay();
    }
    public void RegisterGui(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("idk", PressurizedHudOverlay.PressurizedHUD);
        HudOverlay = new PressurizedHudOverlay();
        HudOverlay.initOverlays(event);
        
        //event.registerAbove(VanillaGuiOverlay.VIGNETTE.id(), PressurizedMain.MODID.concat(".pressurized_overlay"), PressurizedHudOverlay::RenderPressurized);
    }

    public static double getBodyPressure() {
        return BodyPressure;
    };

    public static double getDepth() {
        return Depth;
    };
    public static void setDepth(int Value) {
        Depth = Value;
    };

    public static void BlockScan(BlockPos blockPos, ServerLevel level) { // i geneuinely hate this code and needs a lot of cleaning

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

                //should i delete these disabled lines aswell???
                //  if (depth*CommonConfigs.CrushDepthMultiplier.get() > -Resistance) {
                //        break;
                //   }

            } // adds crush resistance of the blocks below the crushed block (blockPos variable)

            if (BlockDepth * CommonConfigs.CrushDepthMultiplier.get() >= Resistance) {

                if (CrushedBlocks.isEmpty() & CrushedBlocks.size() < CommonConfigs.MaxBlocksDestructionCapacity.get()) {
                    Map.Entry<BlockPos, Integer> entry = Map.entry(blockPos, 0);
                    CrushedBlocks.put(CrushedBlocks.size()+1, entry);
                    Networking.CHANNEL3.send(PacketDistributor.ALL.noArg(), new UpdateCBArray(CrushedBlocks.size(), blockPos));
                }

                boolean found = false;
                for (Map.Entry<Integer, Map.Entry<BlockPos, Integer>> BlockMap : CrushedBlocks.entrySet()) {
                    if (BlockMap.getValue().getKey().equals(blockPos)) {
                        found = true;
                        break;
                    }
                }

                if (!found & CrushedBlocks.size() < CommonConfigs.MaxBlocksDestructionCapacity.get()) {
                    Map.Entry<BlockPos, Integer> entry = Map.entry(blockPos, 0);

                    CrushedBlocks.put(CrushedBlocks.size()+1, entry);
                    Networking.CHANNEL3.send(PacketDistributor.ALL.noArg(), new UpdateCBArray(CrushedBlocks.size(), blockPos));
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
                    for (Map.Entry<Integer, Map.Entry<BlockPos, Integer>> BlockMap : CrushedBlocks.entrySet()) {
                        Map.Entry<BlockPos, Integer> entry = BlockMap.getValue();
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
                                    if (entry.getValue() > 7) {
                                        level.getBlockState(key);
                                        synchronized (RemovalCollection) {
                                            RemovalCollection.add(BlockMap.getKey());
                                        }
                                        level.destroyBlock(key, true);
                                        level.addDestroyBlockEffect(key, level.getBlockState(key));
                                    } else {
                                        Networking.CHANNEL4.send(PacketDistributor.ALL.noArg(), new UpdateCBTexture(entry.getKey(), entry.getValue()+1));
                                        BlockMap.setValue(Map.entry(entry.getKey(), entry.getValue()+1));

                                    }
                                    synchronized (SkipThread) {
                                        SkipThread.remove(key);
                                    }
                                    synchronized (DestroyThreads) {
                                        DestroyThreads.get(key).replace(Thread.currentThread(), true);
                                    }
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
                ServerTicks = 0;
            }
        } else {
            for (Map.Entry<Integer, Map.Entry<BlockPos, Integer>> BlockMap : CrushedBlocks.entrySet()) {
                Map.Entry<BlockPos, Integer> entry = BlockMap.getValue();
                if (entry.getKey().equals(blockPos)) {
                    RemovalCollection.add(BlockMap.getKey());
                }
            }
        }
        for (Integer Num : RemovalCollection) {
            CrushedBlocks.remove(Num);
        }
        RemovalCollection.clear();
    }

    public static HashMap<Integer, BlockPos> BlockposDepth(BlockPos BlockPos, ServerLevel SLevel) {
        BlockPos currentPos = BlockPos.above();
        BlockPos highestWaterBlock = BlockPos;

        BlockPos NewBlockpos = null;
        int Distance;
        HashMap<Integer, net.minecraft.core.BlockPos> DistancePos = new HashMap<>();

        while (currentPos.getY() < SLevel.getHeight()) {
            if (SLevel.getBlockState(currentPos).liquid()) {
                highestWaterBlock = currentPos;
            } else {
                break;
            }
            currentPos = currentPos.above();
        }

        Distance = (int) highestWaterBlock.getCenter().distanceTo(BlockPos.getCenter());

        BlockHitResult context = SLevel.clip(new ClipContext(
                highestWaterBlock.getCenter(),
                BlockPos.getCenter(),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                null
        ));

        if (context.getType() == HitResult.Type.BLOCK) {
            Distance = (int) highestWaterBlock.getCenter().distanceTo(context.getBlockPos().getCenter());
            NewBlockpos = context.getBlockPos();

            if (ModList.get().isLoaded("valkyrienskies")) {
                BlockPos meow = VSCompat.valkShipToWorld(SLevel, context.getBlockPos());

                if (meow != null) {
                    Distance = (int) highestWaterBlock.getCenter().distanceTo(meow.getCenter());
                }
            }
        }

        DistancePos.put(Distance, NewBlockpos);

        return DistancePos;
    }
    public static int EntityDepth(BlockPos blockPos, ServerLevel level) {
        if (ModList.get().isLoaded("valkyrienskies")) {
            BlockPos meow = VSCompat.valkShipToWorld(level, blockPos);

            if (meow != null) {
                blockPos = meow;
            }
        }

        BlockPos currentPos = blockPos.above();
        BlockPos highestWaterBlock = blockPos;

        while (currentPos.getY() < level.getHeight()) {
            if (level.getBlockState(currentPos).getFluidState().is(Fluids.WATER) || level.getBlockState(currentPos).getBlock() == Blocks.WATER) {
                highestWaterBlock = currentPos;
            } else {
                break;
            }
            currentPos = currentPos.above();
        }
        return highestWaterBlock.getY();
    }

    @SubscribeEvent
    public void PRLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        EntitiesDepth.put(event.getEntity(), 0);
    }


    @SubscribeEvent
    public void PRLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        EntitiesDepth.remove(event.getEntity());
    }

    @SubscribeEvent
    public void OnSTick(TickEvent.ServerTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END)) {return;}
        if (ServerTicks < CommonConfigs.BlockScanRate.get()) {ServerTicks++;return;}

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {

            //NOTICE: the reason why were getting the Depth on the server instead of client is because valk skies
            //VSGameUtilsKt.getShipObjectManagingPos needs ServerLevel
            for (Map.Entry<net.minecraft.world.entity.Entity, Integer> Entry : EntitiesDepth.entrySet()) {
                if (player.getId() == Entry.getKey().getId()) {
                    int Depth = EntityDepth(player.getOnPos(), player.serverLevel());
                    Entry.setValue((int) (Entry.getKey().getY() - Depth));
                    Networking.CHANNEL5.send(PacketDistributor.PLAYER.with(() -> player), new SendPlayerDepth(Entry.getValue()));
                }
            }

            int radius = CommonConfigs.BlockScanRadius.get();
            BlockPos center = player.getOnPos();
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos blockPos = center.offset(x, y, z);
                        if (blockPos.distSqr(center) <= radius * radius) {
                            if (!player.serverLevel().getBlockState(blockPos).liquid()) {
                                BlockScan(blockPos, player.serverLevel()); // this is the cool part
                            }
                        }
                    }
                }
            }
        }
        ServerTicks = 0;
    }

    @SubscribeEvent
    public void BlockDestroyed(BlockEvent.BreakEvent event) {
        for (Map.Entry<Integer, Map.Entry<BlockPos, Integer>> BlockMap : CrushedBlocks.entrySet()) {
            Map.Entry<BlockPos, Integer> entry = BlockMap.getValue();
            if (entry.getKey().equals(event.getPos())) {
                RemovalCollection.add(BlockMap.getKey());
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        static net.minecraft.world.entity.player.Player Player = Minecraft.getInstance().player;
        static double BodyPressure = 0;
        static double PressureBuildup = 0;
        static double CrushDepth = -100;
        static double Depth = 0;
        static int CamShake = 1;

        static Boolean OverPressured = false;
        static Boolean PressureImmunity = false;
        static Boolean CrushImmunity = false;

        static Boolean ValidHelmet = false;
        static Boolean ValidChestPlate = false;
        static Boolean ValidLeggings = false;
        static Boolean ValidBoots = false;

        static Boolean HullDamageThread = false;

        //NOTICE: It seems like all other event listeners dont work without subscribing to this one below.
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        //i should probably keep it client-sided for break texture rendering
        //because last time i tried handling it on the server using level.destroyBlockProgress, the client rendered it a bit inconsistently...
        @SubscribeEvent
        public static void OnRLSEvent(RenderLevelStageEvent event) {
            for (Map.Entry<Integer, Map.Entry<BlockPos, Integer>> BlockMap : PressurizedMain.CrushedBlocks.entrySet()) {
                event.getLevelRenderer().destroyBlockProgress(BlockMap.getKey(), BlockMap.getValue().getKey(), BlockMap.getValue().getValue());
                //NOTICE: First parameter of destroyBlockProgress is for the id of the entity, this may result in issues.
            }
        }

        //am not sure if LoggedIn is actually needed.
        static Boolean LoggedIn = false;

        @SubscribeEvent
        public static void PRLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            LoggedIn = true;
        }


        @SubscribeEvent
        public static void PRLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
            LoggedIn = false;
        }

        @SubscribeEvent
        public static void OnPTick(TickEvent.ClientTickEvent event) {
            Player = Minecraft.getInstance().player;
            if (Player == null) {return;}
            //NOTE: this entire if statement is for Crushed Blocks damage sfx, buh
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
                    if (!Player.isUnderWater() || CrushedBlocks.isEmpty()) {
                        return;
                    }

                    int X = 0;
                    int Y = 0;
                    int Z = 0;

                    for (Map.Entry<Integer, Map.Entry<BlockPos, Integer>> BlockMap : CrushedBlocks.entrySet()) {
                        BlockPos key = BlockMap.getValue().getKey();

                        X += key.getX();
                        Y += key.getY();
                        Z += key.getZ();
                    }

                    X /= CrushedBlocks.size();
                    Y /= CrushedBlocks.size();
                    Z /= CrushedBlocks.size();

                    BlockPos blockPos = new BlockPos(X, Y, Z);
                    BlockState Blockstate = Minecraft.getInstance().player.level().getBlockState(blockPos);

                    double deltaX = Player.getOnPos().getX() - blockPos.getX();
                    double deltaY = Player.getOnPos().getY() - blockPos.getY();
                    double deltaZ = Player.getOnPos().getZ() - blockPos.getZ();

                    double Distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ));
                    String Name = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(Blockstate.getBlock())).getPath();

                    for (String BlockName : MetalBlocks) {
                        if (BlockName.equals(Name)) {
                            //    event.player.playSound(ModSounds.HULLDAMAGE.get(), (float) (1f / Distance), 1f);
                        }
                    }

                    for (String BlockName : StoneBlocks) {
                        if (BlockName.equals(Name)) {
                            //    event.player.playSound(ModSounds.ENVIRONMENTDAMAGE.get(), (float) (1f / Distance), 1f);
                        }
                    }

                    // event.player.level().playSound(null, X, Y, Z,
                    //         ModSounds.HULLDAMAGE.get(), SoundSource.BLOCKS,(float) (1f/Distance), 1f);
                }).start();
            }

            new Thread(() -> {
                if (PressureImmunity) {
                    BodyPressure = Depth;
                } else {
                    if (BodyPressure > Depth) {
                        BodyPressure -= .075;
                    } else if (BodyPressure < Depth) {
                        BodyPressure += .075;
                    } else if (BodyPressure - Depth >= .025) {
                        BodyPressure = Depth;
                    }
                }

                if (CrushImmunity || Depth > CrushDepth) {
                    if (PressureBuildup > 0) {
                        PressureBuildup -= .1;
                        if (PressureBuildup < 0) {
                            PressureBuildup = 0;
                        }
                    }
                }
            }).start();

            if (Player.isUnderWater() & !Player.isCreative()) {
                String Helmet = Player.getItemBySlot(EquipmentSlot.HEAD).getItem().toString();
                String ChestPlate = Player.getItemBySlot(EquipmentSlot.CHEST).getItem().toString();
                String Leggings = Player.getItemBySlot(EquipmentSlot.LEGS).getHoverName().getString();
                String Boots = Player.getItemBySlot(EquipmentSlot.FEET).getHoverName().getString();

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

                if (!CrushImmunity & Depth <= CrushDepth || !PressureImmunity & (Depth - BodyPressure <= -5 & CommonConfigs.PressureDamage.get() || Depth - BodyPressure >= 5 & CommonConfigs.ResurfaceDamage.get())) {
                    OverPressured = true;
                    if (Player.hurtTime <= 0) {
                        if (Depth - BodyPressure < 0 & Player.getDeltaMovement().y < -.15) {// damage when descending too fast
                            Minecraft.getInstance().getSoundManager().stop(ModSounds.BAROTRAUMA.getId(), SoundSource.AMBIENT);
                            Player.level().playLocalSound(Player.getX(), Player.getY(), Player.getZ(), ModSounds.BAROTRAUMA.get(), SoundSource.AMBIENT, (float) (1f * (Math.abs(Depth - BodyPressure))), 1f, false);
                            if (Player.isAlive()) {
                                Networking.CHANNEL1.sendToServer(new BaroDamagePlayerPacket(2));
                            }
                        } else if (Depth - BodyPressure > 0  & Player.getDeltaMovement().y > .15) {// damage when ascending too fast
                            Minecraft.getInstance().getSoundManager().stop(ModSounds.BAROTRAUMA.getId(), SoundSource.AMBIENT);
                            Player.level().playLocalSound(Player.getX(), Player.getY(), Player.getZ(), ModSounds.BAROTRAUMA.get(), SoundSource.AMBIENT, (float) (1f * (Math.abs(Depth - BodyPressure))), 1f, false);
                            if (Player.isAlive()) {
                                Networking.CHANNEL1.sendToServer(new BaroDamagePlayerPacket(2));
                            }
                        }
                    }

                    if (!CrushImmunity & Depth <= CrushDepth) {
                        PressureBuildup += 0.025;
                    }
                } else {
                    OverPressured = false;
                }
                if (PressureBuildup >= 5 & Player.isAlive()) {
                    Networking.CHANNEL2.sendToServer(new CrushDamagePlayerPacket(100));
                }
            } else {
                OverPressured = false;
            }
        }

        @SubscribeEvent
        public static void OnCTick(TickEvent.RenderTickEvent event) {
            if (event.phase == TickEvent.Phase.START & Player != null) {
                if (!LoggedIn || Player.isCreative()) {return;}

                if (OverPressured & !Minecraft.getInstance().isPaused() & Minecraft.getInstance().cameraEntity != null) {
                    if (CamShake >= 1) {
                        CamShake = (int) (-1 * (Math.abs(Depth - BodyPressure)));
                    } else {
                        CamShake = (int) (1 * (Math.abs(Depth - BodyPressure)));
                    }
                    Minecraft.getInstance().cameraEntity.setYRot(Player.getYHeadRot() + (float) CamShake / 20);
                }
            }
        }

        @SubscribeEvent
        public static void OnLeave(PlayerEvent.PlayerLoggedOutEvent event) {
            if (event.getEntity() == Minecraft.getInstance().player) {
                OverPressured = false;
                ValidHelmet = false;
                ValidChestPlate = false;
                ValidLeggings = false;
                ValidBoots = false;
            }
        }

        @SubscribeEvent
        public static void PRespawned(PlayerEvent.PlayerRespawnEvent event) {
            assert Minecraft.getInstance().player != null;
            if (Objects.equals(event.getEntity().getName().toString(), Minecraft.getInstance().player.getName().toString())) {
                PressureBuildup = 0;
                BodyPressure = 0;
                OverPressured = false;
            }
        }
    }
}