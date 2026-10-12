package net.kafgchan.mekanism_waystones.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Portable Teleporter Plus のエネルギー消費計算ユーティリティ
 * 仕様:
 * - 同一ディメンション内: ceil(3次元直線距離[block] / 1000) FE (固定基本料金 0 FE)
 * - 異なるディメンション間: 一律 500 FE
 */
public final class TeleportCostCalculator {

    public static final int CROSS_DIMENSION_COST = 500;
    public static final double DISTANCE_PER_FE = 1000.0;

    private TeleportCostCalculator() {
    }

    /**
     * 転送に必要なエネルギー (FE) を計算する
     *
     * @param fromDim 出発地点のディメンション
     * @param fromPos 出発地点の3次元座標
     * @param toDim   目標地点のディメンション
     * @param toPos   目標地点の3次元座標
     * @return 消費 FE
     */
    public static int calculateCost(ResourceKey<Level> fromDim, Vec3 fromPos, ResourceKey<Level> toDim, Vec3 toPos) {
        if (!fromDim.equals(toDim)) {
            return CROSS_DIMENSION_COST;
        }

        double distance = fromPos.distanceTo(toPos);
        if (distance <= 0.0) {
            return 0;
        }

        return (int) Math.ceil(distance / DISTANCE_PER_FE);
    }

    /**
     * BlockPos を受け取るオーバーロード
     */
    public static int calculateCost(ResourceKey<Level> fromDim, BlockPos fromPos, ResourceKey<Level> toDim, BlockPos toPos) {
        return calculateCost(
                fromDim,
                new Vec3(fromPos.getX() + 0.5, fromPos.getY(), fromPos.getZ() + 0.5),
                toDim,
                new Vec3(toPos.getX() + 0.5, toPos.getY(), toPos.getZ() + 0.5)
        );
    }

    /**
     * Entity と TeleportTarget を受け取るオーバーロード
     */
    public static int calculateCost(net.minecraft.world.entity.Entity entity, net.kafgchan.mekanism_waystones.target.TeleportTarget target) {
        if (entity == null || target == null) {
            return 0;
        }
        return calculateCost(
                entity.level().dimension(),
                entity.position(),
                target.dimension(),
                new Vec3(target.pos().getX() + 0.5, target.pos().getY(), target.pos().getZ() + 0.5)
        );
    }
}
