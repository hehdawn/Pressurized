package net.dawn.pressurized.Network;

import net.dawn.pressurized.ModSounds;
import net.dawn.pressurized.PressurizedDamageSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

public class BaroDamageBoatPacket {
    private final float damage;

    public BaroDamageBoatPacket(float damage) {
        this.damage = damage;
    }

    public static void encode(BaroDamageBoatPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.damage);
    }

    public static BaroDamageBoatPacket decode(FriendlyByteBuf buf) {
        return new BaroDamageBoatPacket(buf.readFloat());
    }

    public static void handle(BaroDamageBoatPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            assert player != null;

            if (player.getVehicle() instanceof LivingEntity livingEntity) {
                if (player.getVehicle() != null && livingEntity.getHealth() > 0.0F) {
                    player.getVehicle().hurt(player.level().damageSources().generic(), msg.damage);
                    //   player.level().playSeededSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.Barotrauma.get(), SoundSource.NEUTRAL, 1f, 1f, 0);
                    if (livingEntity.getHealth() <= 0.0F) {
                        player.hurt(PressurizedDamageSource.Crushed, 100);
                        player.indicateDamage(-2,0);
                        player.level().playSeededSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.CRUSH.get(), SoundSource.NEUTRAL, 1f, 1f, 0);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}