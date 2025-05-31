package net.dawn.Pressurized.Network;

import net.dawn.Pressurized.ModSounds;
import net.dawn.Pressurized.PressurizedDamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockBendPlayerPacket {
    private final BlockPos Position;
    private final RegistryObject<SoundEvent> Sound;

    public BlockBendPlayerPacket(BlockPos Position, ResourceLocation Sound) {
        this.Position = Position;
        this.Sound = ModSounds.BAROTRAUMA;
    }

    public static void encode(BlockBendPlayerPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.Position);
        //buf.write(msg.Position);
    }

    public static BlockBendPlayerPacket decode(FriendlyByteBuf buf) {
        return new BlockBendPlayerPacket(buf.readBlockPos(), buf.readResourceLocation());
    }

    public static void handle(BlockBendPlayerPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            assert player != null;
            if (player.hurtTime <= 0) {
          //      player.hurt(PressurizedDamageSource.Barotrauma, msg.damage);
                player.indicateDamage(-2,0);
             //   player.level().playSeededSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.Barotrauma.get(), SoundSource.NEUTRAL, 1f, 1f, 0);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
