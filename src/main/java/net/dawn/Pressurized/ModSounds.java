package net.dawn.Pressurized;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PressurizedMain.MODID);
    public static final RegistryObject<SoundEvent> BAROTRAUMA = registerSoundEvent("hurt.barotrauma");
    public static final RegistryObject<SoundEvent> CRUSH = registerSoundEvent("hurt.crush");
    public static final RegistryObject<SoundEvent> HULLDAMAGE = registerSoundEvent("hull.damage");
    public static final RegistryObject<SoundEvent> ENVIRONMENTDAMAGE = registerSoundEvent("environment.damage");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(PressurizedMain.MODID, name)));
    }

    public static void Register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
