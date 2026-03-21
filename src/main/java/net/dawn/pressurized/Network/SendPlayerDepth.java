package net.dawn.pressurized.Network;

import net.dawn.pressurized.Client.PressurizedClient;
import net.dawn.pressurized.PressurizedMain;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

//TODO: add a way to remove an index from Client CrushedBlocks.
//NOTICE: i may not have to do the TO-DO above because during client printing it seems the table cleans itself up by itself.

//TODO: update CrushedBlocks array to players who joined recently.

public class SendPlayerDepth {
    private final int Depth;

    public SendPlayerDepth(int depth) {
        this.Depth = depth;
    }

    public static void encode(SendPlayerDepth msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.Depth);
    }

    public static SendPlayerDepth decode(FriendlyByteBuf buf) {
        return new SendPlayerDepth(buf.readInt());
    }

    public static void handle(SendPlayerDepth msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            synchronized (PressurizedMain.CrushedBlocks) {
                PressurizedClient.Depth = msg.Depth;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
