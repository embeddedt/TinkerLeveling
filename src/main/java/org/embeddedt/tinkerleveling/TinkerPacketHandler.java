package org.embeddedt.tinkerleveling;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class TinkerPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TinkerLeveling.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, LevelUpMessage.class, LevelUpMessage::encode, LevelUpMessage::decode, LevelUpMessage::handle);
    }

    public static void sendLevelUp(int level, Player player) {
        LevelUpMessage msg = new LevelUpMessage();
        msg.level = level;
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), msg);
    }

    static class LevelUpMessage {
        int level;
        void encode(FriendlyByteBuf buf) {
            buf.writeInt(level);
        }

        static LevelUpMessage decode(FriendlyByteBuf buf) {
            int level = buf.readInt();
            LevelUpMessage msg = new LevelUpMessage();
            msg.level = level;
            return msg;
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() ->
                    // Make sure it's only executed on the physical client
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHelper.sendLevelUpMessage(level))
            );
            ctx.get().setPacketHandled(true);
        }
    }
}
