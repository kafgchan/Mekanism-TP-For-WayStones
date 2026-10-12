package net.kafgchan.mekanism_waystones.target;

import com.mojang.datafixers.util.Either;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.WorldUtils;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneTeleportContext;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.blay09.mods.waystones.api.error.WaystoneTeleportError;
import net.kafgchan.mekanism_waystones.item.ItemPortableTeleporterPlus;
import net.kafgchan.mekanism_waystones.item.TeleporterOwner;
import net.kafgchan.mekanism_waystones.registration.ModDataComponents;
import net.kafgchan.mekanism_waystones.util.TeleportCostCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * テレポート実行および検証・確定処理サービス
 * 仕様:
 * - 所有者UUIDの検証
 * - 消費FEの計算と残量チェック
 * - 転送成功時のみエネルギーを消費 (失敗時は未消費)
 */
public final class TeleportExecutionService {

    public enum Result {
        SUCCESS,
        NOT_OWNER,
        INSUFFICIENT_ENERGY,
        TARGET_NOT_FOUND,
        CANCELLED_OR_FAILED
    }

    private TeleportExecutionService() {
    }

    /**
     * テレポートを実行する (サーバー側処理)
     */
    public static Result executeTeleport(ServerPlayer player, ItemStack stack, TeleportTarget target) {
        // 1. 所有権の検証
        TeleporterOwner owner = stack.get(ModDataComponents.OWNER.get());
        if (owner == null || !owner.isOwner(player.getUUID())) {
            return Result.NOT_OWNER;
        }

        // 2. 必要エネルギー (FE) の計算
        int cost = TeleportCostCalculator.calculateCost(
                player.level().dimension(),
                player.position(),
                target.dimension(),
                Vec3.atCenterOf(target.pos())
        );

        // 3. エネルギー残量の検証
        int currentEnergy = ItemPortableTeleporterPlus.getEnergy(stack);
        if (currentEnergy < cost) {
            return Result.INSUFFICIENT_ENERGY;
        }

        // 4. 目標ディメンションの取得
        ServerLevel targetLevel = player.server.getLevel(target.dimension());
        if (targetLevel == null) {
            return Result.TARGET_NOT_FOUND;
        }

        boolean success = false;

        // 5. ターゲットタイプ別のテレポート確定処理
        if (target.type() == TargetType.WAYSTONE) {
            success = teleportToWaystone(player, targetLevel, target);
        } else if (target.type() == TargetType.MEKANISM) {
            success = teleportToMekanism(player, targetLevel, target);
        }

        // 6. 成功時のみエネルギーを確定消費 (失敗時は未消費)
        if (success) {
            ItemPortableTeleporterPlus.setEnergy(stack, currentEnergy - cost);
            return Result.SUCCESS;
        } else {
            return Result.CANCELLED_OR_FAILED;
        }
    }

    private static boolean teleportToWaystone(ServerPlayer player, ServerLevel targetLevel, TeleportTarget target) {
        try {
            UUID waystoneUid = UUID.fromString(target.id());
            Optional<Waystone> waystoneOpt = WaystonesAPI.getWaystone(targetLevel, waystoneUid);
            if (waystoneOpt.isEmpty()) {
                return false;
            }

            Waystone waystone = waystoneOpt.get();
            WaystoneTeleportContext context = WaystonesAPI.createUnboundTeleportContext(player, waystone);
            Either<List<Entity>, WaystoneTeleportError> teleportResult = WaystonesAPI.tryTeleport(context);

            return teleportResult.left().isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean teleportToMekanism(ServerPlayer player, ServerLevel targetLevel, TeleportTarget target) {
        try {
            BlockPos pos = target.pos();
            TileEntityTeleporter teleporter = WorldUtils.getTileEntity(TileEntityTeleporter.class, targetLevel, pos);
            if (teleporter == null) {
                return false;
            }

            BlockPos targetPos = teleporter.getTeleporterTargetPos();
            double oldX = player.getX();
            double oldY = player.getY();
            double oldZ = player.getZ();
            ServerLevel oldLevel = player.serverLevel();

            // 安全なテレポート
            player.teleportTo(targetLevel, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, player.getYRot(), player.getXRot());

            // 効果音・パーティクル演出
            if (oldLevel != targetLevel || player.distanceToSqr(oldX, oldY, oldZ) >= 25.0) {
                oldLevel.playSound(null, oldX, oldY, oldZ, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
            }
            targetLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
            teleporter.sendTeleportParticles();

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
