package net.dawn.Pressurized;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.*;

public class PressurizedDamageSource extends DamageSource {
    public static final PressurizedDamageSource Barotrauma = new PressurizedDamageSource("Barotrauma"); // Just an instance I used
    public static final PressurizedDamageSource Crushed = new PressurizedDamageSource("Crushed"); // Just an instance I used

    public PressurizedDamageSource(String Name) {
        super(Holder.direct(new DamageType(Name, DamageScaling.NEVER, 0, DamageEffects.HURT, DeathMessageType.INTENTIONAL_GAME_DESIGN)));
    }
}