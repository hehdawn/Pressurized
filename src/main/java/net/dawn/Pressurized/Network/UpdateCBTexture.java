package net.dawn.Pressurized.Network;

import net.dawn.Pressurized.PressurizedMain;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class UpdateCBTexture {
    private final BlockPos Key;
    private final int Value;

    public UpdateCBTexture(BlockPos Key, int Value) {
        this.Key = Key;
        this.Value = Value;
    }

    public static void encode(UpdateCBTexture msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.Key);
        buf.writeInt(msg.Value);
    }

    public static UpdateCBTexture decode(FriendlyByteBuf buf) {
        return new UpdateCBTexture(buf.readBlockPos(), buf.readInt());
    }

    public static void handle(UpdateCBTexture msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            //synchronized (PressurizedMain.CrushedBlocks) {
                for (Map.Entry<Integer, Map.Entry<BlockPos, Integer>> entry : PressurizedMain.CrushedBlocks.entrySet()) {
                    Map.Entry<BlockPos, Integer> CrushedBlock = entry.getValue();
                    if (CrushedBlock.getKey().equals(msg.Key)) {
                        //NOTICE: REPLACED entry.setValue WITH WHATEVER IS BELOW ME, this is because the method i just described in this comment doesnt update the one in the original Array, its basically a separate copy.
                        PressurizedMain.CrushedBlocks.replace(entry.getKey(), Map.entry(msg.Key, msg.Value));
                    }
                }
           // }
        });
        ctx.get().setPacketHandled(true);
    }
}
