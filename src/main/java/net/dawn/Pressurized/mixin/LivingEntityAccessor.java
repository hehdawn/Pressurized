package net.dawn.Pressurized.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Invoker("jumpFromGround") //protected bypass guh
    void invokeJumpFromGround();

    @Accessor("noJumpDelay")
    void setNoJumpDelay(int value);
}