package org.vivecraft.spigot.network;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import javax.annotation.Nullable;
import java.lang.Math;

//todo  还未使用
public class VRData {
    // headset center
    public VRDevicePose hmd;
    // main controller aim
    public VRDevicePose c0;
    // offhand controller aim
    public VRDevicePose c1;

    // main controller hand
    public VRDevicePose h0;
    // offhand controller hand
    public VRDevicePose h1;

    // fbt trackers
    public VRDevicePose waist;
    public VRDevicePose foot_left;
    public VRDevicePose foot_right;
    public VRDevicePose knee_left;
    public VRDevicePose knee_right;
    public VRDevicePose elbow_left;
    public VRDevicePose elbow_right;

    public FBTMode fbtMode = FBTMode.ARMS_ONLY;

    // room origin, all VRDevicePose are relative to that
    public Vec3 origin;
    // room rotation rotated around the origin
    public float rotation_radians;
    // pose positions get scaled by that
    public float worldScale;

    public VRData() {
    }



    /**
     * @return estimated pivot point that the players head rotates around, in world space
     */
    public Vec3 getHeadPivot() {
        Vec3 eye = this.hmd.getPosition();
        // scale pivot point with world scale, to prevent unwanted player movement
        Vector3f headPivotOffset = this.hmd.getMatrix()
                .transformPosition(new Vector3f(0.0F,-0.1F * this.worldScale,0.1F * this.worldScale));
        return eye.add(headPivotOffset.x,headPivotOffset.y,headPivotOffset.z);
    }

    /**
     * calculates the head pivot estimation with the provided room origin and scale
     *
     * @param newOrigin     new room origin to use instead of the one linked to this VRData
     * @param newWorldScale new world scale to use instead of the one linked to this VRData
     * @return estimated pivot point that the players head rotates around, in world space
     */
    public Vec3 getNewHeadPivot(Vec3 newOrigin,float newWorldScale) {
        Vec3 newEye = this.hmd.getPosition().subtract(this.origin).add(newOrigin);
        // scale pivot point with world scale, to prevent unwanted player movement
        Vector3f headPivotOffset = this.hmd.getMatrix()
                .transformPosition(new Vector3f(0.0F,-0.1F * newWorldScale,0.1F * newWorldScale));
        return newEye.add(headPivotOffset.x,headPivotOffset.y,headPivotOffset.z);
    }

    /**
     * @return estimated point that is behind the players back, in world space
     */
    public Vec3 getHeadRear() {
        Vec3 eye = this.hmd.getPosition();
        Vector3f headBackOffset = this.hmd.getMatrix()
                .transformPosition(new Vector3f(0.0F,-0.2F * this.worldScale,0.2F * this.worldScale));
        return eye.add(headBackOffset.x,headBackOffset.y,headBackOffset.z);
    }

    @Override
    public String toString() {
        return """
                VRData:
                    origin: %s
                    rotation: %.2f
                    scale: %.2f
                    hmd: %s
                    c1: %s
                    c2: %s
                """
                .formatted(
                        this.origin,
                        this.rotation_radians,
                        this.worldScale,
                        this.hmd,
                        this.c0,
                        this.c1
                );
    }

    public class VRDevicePose {
        // link to the parent, holds the rotation, scale and origin
        private final VRData data;
        // in room position
        private final Vector3fc pos;
        // in room direction
        private final Vector3fc dir;
        // in room orientation
        private final Matrix4fc matrix;

        public VRDevicePose(VRData data,Matrix4fc matrix,Vector3fc pos,Vector3fc dir) {
            this.data = data;
            this.matrix = new Matrix4f(matrix);
            this.pos = pos;
            this.dir = dir;
        }

        /**
         * @return position of this device in world space
         */
        public Vec3 getPosition() {
            Vector3f localPos = this.pos.mul(VRData.this.worldScale,new Vector3f())
                    .rotateY(this.data.rotation_radians);
            return this.data.origin.add(localPos.x,localPos.y,localPos.z);
        }

        /**
         * returns the world position as a float Vector, is safe to use for VRData marked as {@code room}
         *
         * @return position of this device in world space
         */
        public Vector3f getPositionF() {
            Vector3f localPos = this.pos.mul(VRData.this.worldScale,new Vector3f())
                    .rotateY(this.data.rotation_radians);
            return localPos.add((float) this.data.origin.x,(float) this.data.origin.y,(float) this.data.origin.z);
        }

        /**
         * calculates the difference between the current worldScale and the given {@code newWorldScale}
         * the result of this call is newPos - oldPos
         *
         * @param newWorldScale new world scale
         * @return returns the position offset from changed world scale
         */
        public Vector3f getScalePositionOffset(float newWorldScale) {
            Vector3f oldPos = this.pos.mul(VRData.this.worldScale,new Vector3f())
                    .rotateY(this.data.rotation_radians);
            Vector3f newPos = this.pos.mul(newWorldScale,new Vector3f())
                    .rotateY(this.data.rotation_radians);
            return newPos.sub(oldPos);
        }

        /**
         * @return direction of this device in world space
         */
        public Vector3f getDirection() {
            return this.dir.rotateY(this.data.rotation_radians,new Vector3f());
        }

        /**
         * transforms the device local vector {@code axis} to world space
         *
         * @param axis local vector to transform
         * @return {@code axis} transformed into world space
         */
        public Vector3f getCustomVector(Vector3fc axis) {
            return this.matrix.transformDirection(axis,new Vector3f())
                    .rotateY(this.data.rotation_radians);
        }

        /**
         * @return yaw of the device in world space, in degrees
         */
        public float getYaw() {
            return Mth.RAD_TO_DEG * this.getYawRad();
        }

        /**
         * @return yaw of the device in world space, in radians
         */
        public float getYawRad() {
            Vector3f dir = this.getDirection();
            return (float) Math.atan2(-dir.x,dir.z);
        }

        /**
         * @return pitch of the device in world space, in degrees
         */
        public float getPitch() {
            return Mth.RAD_TO_DEG * this.getPitchRad();
        }

        /**
         * @return pitch of the device in world space, in radians
         */
        public float getPitchRad() {
            Vector3f dir = this.getDirection();
            return (float) Math.asin(dir.y / dir.length());
        }

        /**
         * @return roll of the device in world space, in degrees
         */
        public float getRoll() {
            return Mth.RAD_TO_DEG * this.getRollRad();
        }

        /**
         * @return pitch of the device in world space, in radians
         */
        public float getRollRad() {
            return (float) -Math.atan2(this.matrix.m01(),this.matrix.m11());
        }

        /**
         * @return pose matrix of the device in world space
         */
        public Matrix4f getMatrix() {
            return new Matrix4f().rotationY(VRData.this.rotation_radians).mul(this.matrix);
        }

        @Override
        public String toString() {
            return "Device: pos:" + this.getPosition() + ", dir: " + this.getDirection();
        }
    }
}
