package net.dawn.Pressurized.Network;

import net.dawn.Pressurized.PressurizedMain;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

//TODO: add a way to remove an index from Client CrushedBlocks.
//NOTICE: i may not have to do the TO-DO above because during client printing it seems the table cleans itself up by itself.

//TODO: update CrushedBlocks array to players who joined recently.

public class UpdateCBArray {
    private final int Key;
    private final BlockPos Value;

    public UpdateCBArray(int Key, BlockPos Blockpos) {
        this.Key = Key;
        this.Value = Blockpos;
    }

    public static void encode(UpdateCBArray msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.Key);
        buf.writeBlockPos(msg.Value);
    }

    public static UpdateCBArray decode(FriendlyByteBuf buf) {
        return new UpdateCBArray(buf.readInt(), buf.readBlockPos());
    }

    public static void handle(UpdateCBArray msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            synchronized (PressurizedMain.CrushedBlocks) {
                PressurizedMain.CrushedBlocks.put(msg.Key, Map.entry(msg.Value, 0));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
