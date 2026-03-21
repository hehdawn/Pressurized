package net.dawn.pressurized;

import net.dawn.pressurized.Network.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Networking {
    public static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL1 = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PressurizedMain.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static final SimpleChannel CHANNEL2 = NetworkRegistry.newSimpleChannel(
            ResourceLocation.of(PressurizedMain.MODID, ResourceLocation.NAMESPACE_SEPARATOR),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static final SimpleChannel CHANNEL3 = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PressurizedMain.MODID, "test1"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static final SimpleChannel CHANNEL4 = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PressurizedMain.MODID, "test2"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    public static final SimpleChannel CHANNEL5 = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PressurizedMain.MODID, "test3"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static final SimpleChannel CHANNEL6 = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PressurizedMain.MODID, "test4"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static final SimpleChannel CHANNEL7 = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PressurizedMain.MODID, "test5"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL1.registerMessage(id++, BaroDamagePlayerPacket.class, BaroDamagePlayerPacket::encode, BaroDamagePlayerPacket::decode, BaroDamagePlayerPacket::handle);
        CHANNEL2.registerMessage(id++, CrushDamagePlayerPacket.class, CrushDamagePlayerPacket::encode, CrushDamagePlayerPacket::decode, CrushDamagePlayerPacket::handle);
        CHANNEL3.registerMessage(id++, UpdateCBArray.class, UpdateCBArray::encode, UpdateCBArray::decode, UpdateCBArray::handle);
        CHANNEL4.registerMessage(id++, UpdateCBTexture.class, UpdateCBTexture::encode, UpdateCBTexture::decode, UpdateCBTexture::handle);
        CHANNEL5.registerMessage(id++, SendPlayerDepth.class, SendPlayerDepth::encode, SendPlayerDepth::decode, SendPlayerDepth::handle);
        CHANNEL6.registerMessage(id++, UpdateAP.class, UpdateAP::encode, UpdateAP::decode, UpdateAP::handle);
        CHANNEL7.registerMessage(id++, BaroDamageBoatPacket.class, BaroDamageBoatPacket::encode, BaroDamageBoatPacket::decode, BaroDamageBoatPacket::handle);
    }
}