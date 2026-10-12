package net.kafgchan.mekanism_waystones.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class TeleporterConfig {

    public static final TeleporterConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    public static final int DEFAULT_MAX_ENERGY = 800_000;
    public static final int DEFAULT_MAX_TRANSFER = 800_000;
    public static final int DEFAULT_CROSS_DIMENSION_COST = 500;
    public static final double DEFAULT_DISTANCE_PER_FE = 1000.0;
    public static final int DEFAULT_BASE_COST = 0;
    public static final int DEFAULT_MAX_COST = 800_000;

    static {
        Pair<TeleporterConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(TeleporterConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ModConfigSpec.IntValue maxEnergy;
    public final ModConfigSpec.IntValue maxTransfer;
    public final ModConfigSpec.IntValue crossDimensionCost;
    public final ModConfigSpec.DoubleValue distancePerFe;
    public final ModConfigSpec.IntValue baseCost;
    public final ModConfigSpec.IntValue maxCost;

    private TeleporterConfig(ModConfigSpec.Builder builder) {
        builder.comment("Energy Settings for Portable Teleporter Plus").push("energy");

        maxEnergy = builder
                .comment("Maximum energy storage capacity in FE (Forge Energy). Default: 800,000")
                .defineInRange("maxEnergy", DEFAULT_MAX_ENERGY, 1_000, 100_000_000);

        maxTransfer = builder
                .comment("Maximum energy transfer rate in FE/t. Default: 800,000")
                .defineInRange("maxTransfer", DEFAULT_MAX_TRANSFER, 100, 100_000_000);

        builder.pop();

        builder.comment("Teleport Cost Calculation Settings").push("cost");

        crossDimensionCost = builder
                .comment("Energy cost for interdimensional teleportation (e.g., Overworld to Nether). Default: 500 FE")
                .defineInRange("crossDimensionCost", DEFAULT_CROSS_DIMENSION_COST, 0, 10_000_000);

        distancePerFe = builder
                .comment("Travel distance per 1 FE within the same dimension (in blocks). Formula: ceil(distance / distancePerFe) + baseCost. Default: 1000.0 (1 FE per 1000 blocks)")
                .defineInRange("distancePerFe", DEFAULT_DISTANCE_PER_FE, 0.1, 100_000.0);

        baseCost = builder
                .comment("Base fixed energy cost for same dimension teleportation. Default: 0 FE")
                .defineInRange("baseCost", DEFAULT_BASE_COST, 0, 1_000_000);

        maxCost = builder
                .comment("Maximum cost cap for single teleportation in FE. Default: 800,000 FE")
                .defineInRange("maxCost", DEFAULT_MAX_COST, 0, 100_000_000);

        builder.pop();
    }

    public static int getMaxEnergy() {
        try {
            if (SPEC.isLoaded()) {
                return INSTANCE.maxEnergy.get();
            }
        } catch (Exception ignored) {
        }
        return DEFAULT_MAX_ENERGY;
    }

    public static int getMaxTransfer() {
        try {
            if (SPEC.isLoaded()) {
                return INSTANCE.maxTransfer.get();
            }
        } catch (Exception ignored) {
        }
        return DEFAULT_MAX_TRANSFER;
    }

    public static int getCrossDimensionCost() {
        try {
            if (SPEC.isLoaded()) {
                return INSTANCE.crossDimensionCost.get();
            }
        } catch (Exception ignored) {
        }
        return DEFAULT_CROSS_DIMENSION_COST;
    }

    public static double getDistancePerFe() {
        try {
            if (SPEC.isLoaded()) {
                return INSTANCE.distancePerFe.get();
            }
        } catch (Exception ignored) {
        }
        return DEFAULT_DISTANCE_PER_FE;
    }

    public static int getBaseCost() {
        try {
            if (SPEC.isLoaded()) {
                return INSTANCE.baseCost.get();
            }
        } catch (Exception ignored) {
        }
        return DEFAULT_BASE_COST;
    }

    public static int getMaxCost() {
        try {
            if (SPEC.isLoaded()) {
                return INSTANCE.maxCost.get();
            }
        } catch (Exception ignored) {
        }
        return DEFAULT_MAX_COST;
    }
}
