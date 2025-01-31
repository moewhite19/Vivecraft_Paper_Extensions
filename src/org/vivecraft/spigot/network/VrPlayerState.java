package org.vivecraft.spigot.network;

import net.minecraft.network.FriendlyByteBuf;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.VivePlayer;

import javax.annotation.Nullable;

/**
 * holds all data from a player
 *
 * @param seated              if the player is in seated mode
 * @param hmd                 device Pose of the headset
 * @param leftHanded          if true, {@code mainHand} is the left hand, else {@code offHand} is
 * @param mainHand            device Pose of the main hand
 * @param reverseHands1legacy same as {@code leftHanded}, just here for legacy compatibility
 * @param offHand             device Pose of the offhand
 * @param fbtMode             determines what additional trackers are in the player state
 * @param waist               waist tracker pos, can be {@code null}
 * @param rightFoot           right foot tracker pos, can be {@code null}
 * @param leftFoot            left foot tracker pos, can be {@code null}
 * @param rightKnee           right knee tracker pos, can be {@code null}
 * @param leftKnee            left knee tracker pos, can be {@code null}
 * @param rightElbow          right elbow tracker pos, can be {@code null}
 * @param leftElbow           left elbow tracker pos, can be {@code null}
 */
public record VrPlayerState(boolean seated,Pose hmd,boolean leftHanded,Pose mainHand,
                            boolean reverseHands1legacy,Pose offHand,
                            FBTMode fbtMode,@Nullable Pose waist,
                            @Nullable Pose rightFoot,@Nullable Pose leftFoot,
                            @Nullable Pose rightKnee,@Nullable Pose leftKnee,
                            @Nullable Pose rightElbow,@Nullable Pose leftElbow) {

    public static VrPlayerState create(VivePlayer vrPlayer) {
        FBTMode fbtMode = vrPlayer.fbtMode;
        boolean hasFbt = fbtMode != FBTMode.ARMS_ONLY;
        boolean hasExtendedFbt = fbtMode == FBTMode.WITH_JOINTS;

        return new VrPlayerState(
                false,
                createPose(),
                false,
                createPose(),
                false,
                createPose(),
                fbtMode,
                hasFbt ? createPose() : null,
                hasFbt ? createPose() : null,
                hasFbt ? createPose() : null,
                hasExtendedFbt ? createPose() : null,
                hasExtendedFbt ? createPose() : null,
                hasExtendedFbt ? createPose() : null,
                hasExtendedFbt ? createPose() : null
        );
    }

    /**
     * creates the device Pose object for the specified device, from the client vr data
     *
     * @return Pose object of the current device state
     */
    private static Pose createPose() {
        return Pose.create();
    }


    /**
     * @param buffer     buffer to read from
     * @param bytesAfter specifies how many bytes in the buffer are meant to be left unread
     * @return a VrPlayerState read from the given {@code buffer}
     */
    public static VrPlayerState deserialize(FriendlyByteBuf buffer,int bytesAfter) {
        boolean seated = buffer.readBoolean();
        Pose hmd = Pose.deserialize(buffer);
        boolean reverseHands = buffer.readBoolean();
        Pose mainController = Pose.deserialize(buffer);
        boolean reverseHandsLegacy = buffer.readBoolean();
        Pose offController = Pose.deserialize(buffer);

        // the rest here is only sent when the client has any fbt trackers
        FBTMode fbtMode = FBTMode.ARMS_ONLY;
        Pose waist = null;
        Pose rightFoot = null;
        Pose leftFoot = null;
        Pose rightKnee = null;
        Pose leftKnee = null;
        Pose rightElbow = null;
        Pose leftElbow = null;
        if (buffer.readableBytes() > bytesAfter){
            fbtMode = FBTMode.values()[buffer.readByte()];
        }
        if (fbtMode != FBTMode.ARMS_ONLY){
            waist = Pose.deserialize(buffer);
            rightFoot = Pose.deserialize(buffer);
            leftFoot = Pose.deserialize(buffer);
        }
        if (fbtMode == FBTMode.WITH_JOINTS){
            rightKnee = Pose.deserialize(buffer);
            leftKnee = Pose.deserialize(buffer);
            rightElbow = Pose.deserialize(buffer);
            leftElbow = Pose.deserialize(buffer);
        }
        return new VrPlayerState(seated,
                hmd,
                reverseHands,
                mainController,
                reverseHandsLegacy,
                offController,
                fbtMode,waist,
                rightFoot,leftFoot,
                rightKnee,leftKnee,
                rightElbow,leftElbow);
    }

    /**
     * gets the Pose for the given body part
     *
     * @param bodyPart BodyPart to get the pose for
     * @return Pose of the {@code bodyPart}, or {@code null} if the body part is not valid for the current FBT mode
     */
    @Nullable
    public Pose getBodyPartPose(BodyPart bodyPart) {
        return switch (bodyPart) {
            case MAIN_HAND -> this.mainHand;
            case OFF_HAND -> this.offHand;
            case LEFT_FOOT -> this.leftFoot;
            case RIGHT_FOOT -> this.rightFoot;
            case LEFT_ELBOW -> this.leftElbow;
            case RIGHT_ELBOW -> this.rightElbow;
            case LEFT_KNEE -> this.leftKnee;
            case RIGHT_KNEE -> this.rightKnee;
            case WAIST -> this.waist;
        };
    }

    /**
     * writes this VrPlayerState to the given {@code buffer}
     *
     * @param buffer buffer to write to
     */
    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.seated);
        this.hmd.serialize(buffer);
        buffer.writeBoolean(this.leftHanded);
        this.mainHand.serialize(buffer);
        buffer.writeBoolean(this.leftHanded);
        this.offHand.serialize(buffer);
        // only send those, if it is there and the server supports it
        if (this.fbtMode != FBTMode.ARMS_ONLY){
            buffer.writeByte(this.fbtMode.ordinal());
            this.waist.serialize(buffer);
            this.rightFoot.serialize(buffer);
            this.leftFoot.serialize(buffer);
            if (this.fbtMode == FBTMode.WITH_JOINTS){
                this.rightKnee.serialize(buffer);
                this.leftKnee.serialize(buffer);
                this.rightElbow.serialize(buffer);
                this.leftElbow.serialize(buffer);
            }
        }
    }
}
