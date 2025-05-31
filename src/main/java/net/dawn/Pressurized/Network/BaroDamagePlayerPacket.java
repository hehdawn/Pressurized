package net.dawn.Pressurized.Network;

import net.dawn.Pressurized.PressurizedDamageSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BaroDamagePlayerPacket {
    private final float damage;

    public BaroDamagePlayerPacket(float damage) {
        this.damage = damage;
    }

    public static void encode(BaroDamagePlayerPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.damage);
    }

    public static BaroDamagePlayerPacket decode(FriendlyByteBuf buf) {
        return new BaroDamagePlayerPacket(buf.readFloat());
    }

    public static void handle(BaroDamagePlayerPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            assert player != null;
            if (player.hurtTime <= 0) {
                player.hurt(PressurizedDamageSource.Barotrauma, msg.damage);
                player.indicateDamage(-2,0);
             //   player.level().playSeededSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.Barotrauma.get(), SoundSource.NEUTRAL, 1f, 1f, 0);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
