package net.dawn.Pressurized.Network;

import net.dawn.Pressurized.ModSounds;
import net.dawn.Pressurized.PressurizedDamageSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CrushDamagePlayerPacket {
    private final float damage;

    public CrushDamagePlayerPacket(float damage) {
        this.damage = damage;
    }

    public static void encode(CrushDamagePlayerPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.damage);
    }

    public static CrushDamagePlayerPacket decode(FriendlyByteBuf buf) {
        return new CrushDamagePlayerPacket(buf.readFloat());
    }

    public static void handle(CrushDamagePlayerPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            assert player != null;
            System.out.println(player.hurtTime);
            if (player.hurtTime <= 0) {
                player.hurt(PressurizedDamageSource.Crushed, msg.damage);
                player.indicateDamage(-2,0);
                player.level().playSeededSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.CRUSH.get(), SoundSource.NEUTRAL, 1f, 1f, 0);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
