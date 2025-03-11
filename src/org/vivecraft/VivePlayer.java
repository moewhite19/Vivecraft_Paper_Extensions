package org.vivecraft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.vivecraft.listeners.VivecraftNetworkListener;
import org.vivecraft.spigot.network.BodyPart;
import org.vivecraft.spigot.network.FBTMode;
import org.vivecraft.spigot.network.Pose;
import org.vivecraft.spigot.network.VrPlayerState;
import org.vivecraft.spigot.network.packet.s2c.UberPacketPayloadS2C;
import org.vivecraft.utils.MetadataHelper;
import org.vivecraft.utils.Quaternion;
import org.vivecraft.utils.Vector3;

import net.minecraft.world.phys.Vec3;

public class VivePlayer {
    public byte[] draw;
    public byte[] uberData;
    public float worldScale = 1f;
    public float heightScale = 1f;
    boolean isTeleportMode;
    boolean isReverseHands;
    boolean isVR;
    public BodyPart activeBodyPart = BodyPart.MAIN_HAND;
    public boolean crawling;

    public Vec3 offset = new Vec3(0,0,0);
    public Player player;
    public String version;
    public FBTMode fbtMode = FBTMode.ARMS_ONLY;
    VrPlayerState state;

    public VivePlayer(Player player) {
        this.player = player;
    }

    public float getDraw() {
        try{
            if (draw != null){
                ByteArrayInputStream byin = new ByteArrayInputStream(draw);
                DataInputStream da = new DataInputStream(byin);

                float draw = da.readFloat();

                da.close(); //needed?
                return draw;
            } else {
            }
        }catch (IOException e){

        }

        return 0;
    }

    @SuppressWarnings("unused")
    public Vec3 getHMDDir() {
//        try{
//            if (hmdData != null){
//
//                ByteArrayInputStream byin = new ByteArrayInputStream(hmdData);
//                DataInputStream da = new DataInputStream(byin);
//
//                boolean isSeated = da.readBoolean();
//                float lx = da.readFloat();
//                float ly = da.readFloat();
//                float lz = da.readFloat();
//
//                float w = da.readFloat();
//                float x = da.readFloat();
//                float y = da.readFloat();
//                float z = da.readFloat();
//                Vector3 forward = new Vector3(0,0,-1);
//                Quaternion q = new Quaternion(w,x,y,z);
//                Vector3 out = q.multiply(forward);
//
//                //System.out.println("("+out.getX()+","+out.getY()+","+out.getZ()+")" + " : W:" + w + " X: "+x + " Y:" + y+ " Z:" + z);
//                da.close(); //needed?
//                return new Vec3(out.getX(),out.getY(),out.getZ());
//            } else {
//            }
//        }catch (IOException e){
//
//        }

        if (state != null){
            final Pose hmd = state.hmd();
            Vector3 forward = new Vector3(0,0,-1);
            final Quaternionfc orientation = hmd.orientation();
            Quaternion q = new Quaternion(orientation.w(),orientation.x(),orientation.y(),orientation.z());
            Vector3 out = q.multiply(forward);
            return new Vec3(out.getX(),out.getY(),out.getZ());
        }

        return ((CraftEntity) player).getHandle().getViewVector(1.0f);
    }

    @SuppressWarnings("unused")
    public Quaternion getHMDRot() {
        if (state != null){
            return new Quaternion(state.hmd().orientation());
        }

        return new Quaternion();
    }

    @SuppressWarnings("unused")
    public Vec3 getControllerDir(BodyPart controller) {
        if (state != null){
            final Pose pose = controller == BodyPart.MAIN_HAND ? state.mainHand() : state.offHand();
            Vector3 forward = new Vector3(0,0,-1);
            final Quaternionfc orientation = pose.orientation();
            Quaternion q = new Quaternion(orientation.w(),orientation.x(),orientation.y(),orientation.z());
            Vector3 out = q.multiply(forward);
            return new Vec3(out.getX(),out.getY(),out.getZ());
        }
        return ((CraftEntity) player).getHandle().getViewVector(1.0f);
    }

    @SuppressWarnings("unused")
    public Quaternion getControllerRot(int controller) {
        if (state != null){
            return new Quaternion(controller == 0 ? state.mainHand().orientation() : state.offHand().orientation());
        }
        return new Quaternion();
    }

    public Location getHMDPos() {
        if (state != null){
            final Vector3fc position = state.hmd().position();
            return player.getLocation().add(position.x(),position.y(),position.z()).add(offset.x,offset.y,offset.z);
        }
        return player.getLocation(); //why
    }

    public Location getControllerPos(BodyPart part) {
        if (state != null){
            if (this.isSeated()){
                Vec3 dir = this.getHMDDir();
                dir = dir.yRot((float) Math.toRadians(part == BodyPart.MAIN_HAND ? -35 : 35));
                dir = new Vec3(dir.x,0,dir.z);
                dir = dir.normalize();
                Location out = this.getHMDPos().add(dir.x * 0.3 * worldScale,-0.4 * worldScale,dir.z * 0.3 * worldScale);
                return out;
            }

            //获取手柄的坐标
            final Vector3fc position = part == BodyPart.MAIN_HAND ? state.mainHand().position() : state.offHand().position();
            return player.getLocation().add(position.x(),position.y(),position.z()).add(offset.x,offset.y,offset.z);
//                return player.getLocation().add(x,y,z).add(offset.x,offset.y,offset.z);
        }

        return player.getLocation(); //why

    }

    public boolean isVR() {
        return this.isVR;
    }

    public void setVR(boolean vr) {
        this.isVR = vr;
        if (!vr){
            this.draw = null;
            state = null;
            uberData = null;
        }
    }

    public boolean isSeated() {
        if (state != null){
            return state.seated();
        }
        return false;
    }

    public byte[] getUberPacket() {
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        try{
//            output.write((byte) VivecraftNetworkListener.PacketDiscriminators.UBERPACKET.ordinal());
//            output.write(java.nio.ByteBuffer.allocate(8).putLong(player.getUniqueId().getMostSignificantBits()).array());
//            output.write(java.nio.ByteBuffer.allocate(8).putLong(player.getUniqueId().getLeastSignificantBits()).array());
////            if (hmdData.length < 29) output.write(0);
////            output.write(hmdData);
////            output.write(controller0data);
////            output.write(controller1data);
//            output.write(java.nio.ByteBuffer.allocate(4).putFloat(worldScale).array());
//            output.write(java.nio.ByteBuffer.allocate(4).putFloat(heightScale).array());
//        }catch (IOException e){
//        }
//        return output.toByteArray();
        return uberData;
    }

    public byte[] getVRPacket() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try{
            output.write((byte) VivecraftNetworkListener.PacketDiscriminators.IS_VR_ACTIVE.ordinal());
            output.write(isVR ? 1 : 0);
            output.write(java.nio.ByteBuffer.allocate(8).putLong(player.getUniqueId().getMostSignificantBits()).array());
            output.write(java.nio.ByteBuffer.allocate(8).putLong(player.getUniqueId().getLeastSignificantBits()).array());
        }catch (IOException e){
        }
        return output.toByteArray();
    }

    public void updateState(VrPlayerState state) {
        this.state = state;
        fbtMode = state.fbtMode();
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        new UberPacketPayloadS2C(player.getUniqueId(),state,worldScale,heightScale).write(buffer);
//        uberData = new byte[buffer.readableBytes()];
//        buffer.readBytes(uberData);
//        uberData = buffer.readByteArray(); //不知道为什么这个没法读
        if (uberData == null || uberData.length != buffer.readableBytes()){
            uberData = new byte[buffer.readableBytes()]; //需要改变或初始化数组长度
        }
        uberData = Arrays.copyOfRange(buffer.array(),buffer.arrayOffset(),buffer.arrayOffset() + buffer.readableBytes());

        //可配置项
        if (VSE.me.useMeatData) MetadataHelper.updateMetdata(this);
    }

    public byte[] getUberData() {
        return uberData;
    }
}