package net.kafgchan.mekanism_waystones;

import net.kafgchan.mekanism_waystones.util.TeleportCostCalculator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeleportCostCalculatorTest {

    private static final ResourceKey<Level> OVERWORLD = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("overworld"));
    private static final ResourceKey<Level> THE_NETHER = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("the_nether"));

    @Test
    @DisplayName("同一地点へのテレポートは 0 FE")
    void testZeroDistanceCost() {
        Vec3 pos = new Vec3(100.0, 64.0, 100.0);
        int cost = TeleportCostCalculator.calculateCost(OVERWORLD, pos, OVERWORLD, pos);
        assertEquals(0, cost);
    }

    @Test
    @DisplayName("同一ディメンション内 1ブロック〜1000ブロックの移動は 1 FE")
    void testWithin1000BlocksCost() {
        Vec3 from = new Vec3(0.0, 64.0, 0.0);
        Vec3 to1m = new Vec3(1.0, 64.0, 0.0);
        Vec3 to1000m = new Vec3(1000.0, 64.0, 0.0);

        assertEquals(1, TeleportCostCalculator.calculateCost(OVERWORLD, from, OVERWORLD, to1m));
        assertEquals(1, TeleportCostCalculator.calculateCost(OVERWORLD, from, OVERWORLD, to1000m));
    }

    @Test
    @DisplayName("同一ディメンション内 1001ブロック〜2000ブロックの移動は 2 FE (切り上げ計算)")
    void testCeilDistanceCost() {
        Vec3 from = new Vec3(0.0, 64.0, 0.0);
        Vec3 to1001m = new Vec3(1000.5, 64.0, 0.0);
        Vec3 to2000m = new Vec3(2000.0, 64.0, 0.0);
        Vec3 to2500m = new Vec3(2500.0, 64.0, 0.0);

        assertEquals(2, TeleportCostCalculator.calculateCost(OVERWORLD, from, OVERWORLD, to1001m));
        assertEquals(2, TeleportCostCalculator.calculateCost(OVERWORLD, from, OVERWORLD, to2000m));
        assertEquals(3, TeleportCostCalculator.calculateCost(OVERWORLD, from, OVERWORLD, to2500m));
    }

    @Test
    @DisplayName("異次元間 (オーバーワールド ⇔ ネザー) の移動は距離に関係なく一律 500 FE")
    void testCrossDimensionCost() {
        Vec3 from = new Vec3(0.0, 64.0, 0.0);
        Vec3 toSameCoord = new Vec3(0.0, 64.0, 0.0);
        Vec3 toFarCoord = new Vec3(100000.0, 64.0, 100000.0);

        assertEquals(500, TeleportCostCalculator.calculateCost(OVERWORLD, from, THE_NETHER, toSameCoord));
        assertEquals(500, TeleportCostCalculator.calculateCost(OVERWORLD, from, THE_NETHER, toFarCoord));
    }
}
