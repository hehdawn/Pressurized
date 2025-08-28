package net.dawn.Pressurized.Client;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.*;

public class ClientConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> PressureOverlay;

    static {
        BUILDER.push("Client");

        PressureOverlay = BUILDER.define("UI Pressure Overlay for when the pressure is unequal", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}