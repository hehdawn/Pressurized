package net.dawn.pressurized.Network;

import net.dawn.pressurized.VSCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateAP {
    private final int Key;
    private final BlockPos Min;
    private final BlockPos Max;

    public UpdateAP(int Key, BlockPos Min, BlockPos Max) {
        this.Key = Key;
        this.Min = Min;
        this.Max = Max;
    }

    public static void encode(UpdateAP msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.Key);

        buf.writeBlockPos(msg.Min);
        buf.writeBlockPos(msg.Max);
    }

    public static UpdateAP decode(FriendlyByteBuf buf) {
        return new UpdateAP(buf.readInt(), buf.readBlockPos(), buf.readBlockPos());
    }

    public static void handle(UpdateAP msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.Key == 0) { //this is actually lame and sad... but idc, am tired of this bs.
                VSCompat.AirPockets.clear();
            }

            AABB Airpocket = new AABB(msg.Min, msg.Max);

            VSCompat.AirPockets.add(msg.Key,  Airpocket);
        });
        ctx.get().setPacketHandled(true);
    }
}
