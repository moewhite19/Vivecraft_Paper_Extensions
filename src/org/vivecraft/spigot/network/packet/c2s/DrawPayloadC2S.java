package org.vivecraft.spigot.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.spigot.network.packet.PayloadIdentifier;

/**
 * holds the clients current bow draw percent
 *
 * @param draw how far the player has pulled the bow
 */
public record DrawPayloadC2S(float draw) implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.DRAW;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeFloat(this.draw);
    }

    public static DrawPayloadC2S read(FriendlyByteBuf buffer) {
        return new DrawPayloadC2S(buffer.readFloat());
    }
}
