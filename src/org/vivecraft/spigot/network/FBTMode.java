package org.vivecraft.spigot.network;

public enum FBTMode {
    /**
     * only controllers are available
     */
    ARMS_ONLY,
    /**
     * controller, waist and feet trackers are available
     */
    ARMS_LEGS,
    /**
     * controller, waist, feet, elbow and knee trackers are available
     */
    WITH_JOINTS
}
