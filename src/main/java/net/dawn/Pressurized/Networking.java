package net.dawn.Pressurized;

import net.dawn.Pressurized.Network.CrushDamagePlayerPacket;
import net.dawn.Pressurized.Network.BaroDamagePlayerPacket;
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

    public static void register() {
        int id = 0;
        CHANNEL1.registerMessage(id++, BaroDamagePlayerPacket.class, BaroDamagePlayerPacket::encode, BaroDamagePlayerPacket::decode, BaroDamagePlayerPacket::handle);
        CHANNEL2.registerMessage(id++, CrushDamagePlayerPacket.class, CrushDamagePlayerPacket::encode, CrushDamagePlayerPacket::decode, CrushDamagePlayerPacket::handle);
    }
}