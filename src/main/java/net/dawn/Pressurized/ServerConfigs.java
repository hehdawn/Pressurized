package net.dawn.Pressurized;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.*;

public class ServerConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ValidHelmets;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ValidChestPlates;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ValidLeggings;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ValidBoots;

    public static final ForgeConfigSpec.BooleanValue HelmetRequired;
    public static final ForgeConfigSpec.BooleanValue ChestPlateRequired;
    public static final ForgeConfigSpec.BooleanValue LeggingsRequired;
    public static final ForgeConfigSpec.BooleanValue BootsRequired;

    public static final ForgeConfigSpec.BooleanValue PressureDamage;
    public static final ForgeConfigSpec.BooleanValue ResurfaceDamage;

    public static final ForgeConfigSpec.ConfigValue<Double> CrushDepthMultiplier;
    public static final ForgeConfigSpec.ConfigValue<Integer> BlockScanRate;
    public static final ForgeConfigSpec.ConfigValue<Integer> BlockScanRadius;
    public static final ForgeConfigSpec.ConfigValue<Integer> MaxBlocksDestructionCapacity;

    static {
        BUILDER.push("Server");

        ValidHelmets = BUILDER.defineList("Valid Helmets",
                List.of(),
                obj -> obj instanceof String);

        ValidChestPlates = BUILDER.defineList("Valid ChestPlates",
                List.of(),
                obj -> obj instanceof String);

        ValidLeggings = BUILDER.defineList("Valid Leggings",
                List.of(),
                obj -> obj instanceof String);

        ValidBoots = BUILDER.defineList("Valid Boots",
                List.of(),
                obj -> obj instanceof String);

        HelmetRequired = BUILDER.define("Requires Helmet", true);
        ChestPlateRequired = BUILDER.define("Requires ChestPlate", true);
        LeggingsRequired = BUILDER.define("Requires Leggings", false);
        BootsRequired = BUILDER.define("Requires Boots", false);

        PressureDamage = BUILDER.define("Diving damage", true);
        ResurfaceDamage = BUILDER.define("Resurfacing damage", true);

        CrushDepthMultiplier = BUILDER.define("Crush Depth Multiplier", (double) 1);
        BlockScanRate = BUILDER.define("Block Scan After X Server Ticks", 5);
        BlockScanRadius = BUILDER.define("Block Scan Radius", 16);
        MaxBlocksDestructionCapacity = BUILDER.define("Max amount of blocks to be stored for breaking by water pressure", 100);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}