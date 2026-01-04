package net.dawn.pressurized.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Invoker("jumpFromGround") //protected bypass guh
    void invokeJumpFromGround();
}