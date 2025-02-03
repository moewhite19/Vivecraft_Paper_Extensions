package org.vivecraft;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.GoalSelector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

public class Reflector {

    //last checked 1.21.4
    public static Field Entity_Data_Pose;
    public static Field SynchedEntityData_itemsById;
    public static Field aboveGroundTickCount;
    public static Field Entity_eyeHeight;
    public static Field availableGoals;

    static {
        try{
            //    public static Field SynchedEntityData_itemsById = getPrivateField("itemsById",SynchedEntityData.class);
            SynchedEntityData_itemsById = getFieldFormType(SynchedEntityData.class,SynchedEntityData.DataItem.class);
            SynchedEntityData_itemsById.setAccessible(true);
//            Entity_Data_Pose = getPrivateField("DATA_POSE",Entity.class);
            Entity_Data_Pose = getFieldFormType(Entity.class,makeGenericTypes(EntityDataAccessor.class,Pose.class));
            Entity_Data_Pose.setAccessible(true);
//            Entity_eyeHeight = getPrivateField("eyeHeight",Entity.class);
            Entity_eyeHeight = getFieldFormStructure(Entity.class,EntityDimensions.class,float.class)[1];
            Entity_eyeHeight.setAccessible(true);
//            availableGoals = getPrivateField("availableGoals",GoalSelector.class);
            availableGoals = getFieldFormType(GoalSelector.class,Set.class);
            availableGoals.setAccessible(true);
//            aboveGroundTickCount = getPrivateField("tickCount",ServerGamePacketListenerImpl.class);
            aboveGroundTickCount = getFieldFormStructure(ServerGamePacketListenerImpl.class,PlayerChunkSender.class,int.class,int.class)[1];
            aboveGroundTickCount.setAccessible(true);

        }catch (NoSuchFieldException e){
        }
    }

    public static int enderManFreezePriority = 1;
    public static int enderManLookTargetPriority = 1;

    public static Object getFieldValue(Field field,Object object) {
        try{
            return field.get(object);
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }
        return null;
    }

    public static void setFieldValue(Field field,Object object,Object value) {
        try{
            field.set(object,value);
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }
    }

    private static Field getPrivateField(String fieldName,Class clazz) {
        Field field = null;
        try{
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
        }catch (NoSuchFieldException e){
            e.printStackTrace();
        }
        return field;
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private static Method getPrivateMethod(String methodName,Class clazz,Class... param) {
        Method m = null;
        try{
            if (param == null){
                m = clazz.getDeclaredMethod(methodName);
            } else {
                m = clazz.getDeclaredMethod(methodName,param);
            }
            m.setAccessible(true);
        }catch (NoSuchMethodException e){
            e.printStackTrace();
        }
        return m;
    }

    public static Object invoke(Method m,Object object,Object... param) {
        try{
            if (param == null)
                return m.invoke(object);
            else
                return m.invoke(object,param);
        }catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
            e.printStackTrace();
        }
        return null;
    }

    //根据类型获取Field
    public static Field getFieldFormType(Class<?> clazz,Class<?> type) throws NoSuchFieldException {
        for (Field declaredField : clazz.getDeclaredFields()) {
            if (declaredField.getType().equals(type)) return declaredField;
        }

        //如果有父类 检查父类
        var superClass = clazz.getSuperclass();
        if (superClass != null) return getFieldFormType(superClass,type);
        throw new NoSuchFieldException(type.getName());
    }

    //根据类型获取Field(针对泛型)
    public static Field getFieldFormType(Class<?> clazz,String type) throws NoSuchFieldException {
        for (Field declaredField : clazz.getDeclaredFields()) {
            if (declaredField.getAnnotatedType().getType().getTypeName().equals(type)) return declaredField;
        }
        //如果有父类 检查父类
        var superClass = clazz.getSuperclass();
        if (superClass != null) return getFieldFormType(superClass,type);
        throw new NoSuchFieldException(type);
    }

    //从数组结构中查找Field
    public static Field[] getFieldFormStructure(Class<?> clazz,Class<?>... types) throws NoSuchFieldException {
        var fields = clazz.getDeclaredFields();
        Field[] result = new Field[types.length];
        int index = 0;
        for (Field f : fields) {
            if (f.getType() == types[index]){
                result[index] = f;
                index++;
                if (index >= types.length){
                    return result;
                }
            } else {
                index = 0;
            }
        }
        throw new NoSuchFieldException(Arrays.toString(types));
    }


    public static String makeGenericTypes(Class<?> clazz,String... types) {
        if (types.length <= 0) return clazz.getName();
        StringBuilder builder = new StringBuilder(clazz.getName()).append('<');
        if (types.length > 1){
            builder.append(String.join(", ",types));
        } else {
            builder.append(types[0]);
        }
        builder.append('>');
        return builder.toString();
    }


    public static String makeGenericTypes(Class<?> clazz,Class<?>... types) {
        if (types.length <= 0) return clazz.getName();
        StringBuilder builder = new StringBuilder(clazz.getName()).append('<');
        if (types.length > 1){
            String[] names = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                names[i] = types[i].getName();
            }
            builder.append(String.join(", ",names));
        } else {
            builder.append(types[0].getName());
        }
        builder.append('>');
        return builder.toString();
    }
}
