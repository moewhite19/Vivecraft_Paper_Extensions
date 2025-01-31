package org.vivecraft.spigot.network.packet.s2c;

import org.vivecraft.spigot.network.packet.PayloadIdentifier;

/**
 * indicates that the server supports roomscale crawling
 */
public record CrawlPayloadS2C() implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.CRAWL;
    }
}
