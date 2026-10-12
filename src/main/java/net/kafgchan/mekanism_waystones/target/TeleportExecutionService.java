package net.kafgchan.mekanism_waystones.target;

import com.mojang.datafixers.util.Either;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.WorldUtils;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneTeleportContext;
import net.blay09.mods.waystones.api.error.WaystoneTeleportError;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.kafgchan.mekanism_waystones.MekanismTPforWaystones;
import net.kafgchan.mekanism_waystones.item.ItemPortableTeleporterPlus;
import net.kafgchan.mekanism_waystones.item.TeleporterOwner;
import net.kafgchan.mekanism_waystones.registration.ModDataComponents;
import net.kafgchan.mekanism_waystones.util.TeleportCostCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * テレポート実行および検証・確定処理サービス
 * レビュー指摘対応 (P1, P2):
 * - 所有者UUIDの検証
 * - クライアント要求ターゲットのサーバー側再解決・改ざん防止 (P1)
 * - 目的地のロード状態・ブロック・着地点安全検証 (窒息・奈落防止) (P1)
 * - 例外ログの詳細記録 (P2)
 * - 転送成功時のみエネルギー確定消費
 */
public final class TeleportExecutionService {

    public enum Result {
        SUCCESS,
        NOT_OWNER,
        INSUFFICIENT_ENERGY,
        TARGET_NOT_FOUND,
        UNSAFE_DESTINATION,
        CANCELLED_OR_FAILED
    }

    private TeleportExecutionService() {
    }

    /**
     * テレポートを実行する (サーバー側処理)
     */
    public static Result executeTeleport(ServerPlayer player, ItemStack stack, TeleportTarget requestedTarget) {
        // 1. 所有権の検証
        TeleporterOwner owner = stack.get(ModDataComponents.OWNER.get());
        if (owner == null || !owner.isOwner(player.getUUID())) {
            MekanismTPforWaystones.LOGGER.warn("Ownership check failed: player '{}' is not the owner of the device", player.getName().getString());
            return Result.NOT_OWNER;
        }

        // 2. サーバー側での転送先再解決 (クライアントの座標改ざん・未開放地点要求を遮断)
        Optional<TeleportTarget> verifiedTargetOpt = TeleportDestinationService.resolveTarget(player, requestedTarget.type(), requestedTarget.id());
        if (verifiedTargetOpt.isEmpty()) {
            MekanismTPforWaystones.LOGGER.warn("Unverified target request from player '{}': type={}, id={}",
                    player.getName().getString(), requestedTarget.type(), requestedTarget.id());
            return Result.TARGET_NOT_FOUND;
        }
        TeleportTarget verifiedTarget = verifiedTargetOpt.get();

        // 3. 必要エネルギー (FE) の計算 (サーバー側で解決した正規座標を使用)
        int cost = TeleportCostCalculator.calculateCost(
                player.level().dimension(),
                player.position(),
                verifiedTarget.dimension(),
                Vec3.atCenterOf(verifiedTarget.pos())
        );

        // 4. エネルギー残量の検証
        int currentEnergy = ItemPortableTeleporterPlus.getEnergy(stack);
        if (currentEnergy < cost) {
            MekanismTPforWaystones.LOGGER.debug("Insufficient energy for player '{}': current={}, required={}",
                    player.getName().getString(), currentEnergy, cost);
            return Result.INSUFFICIENT_ENERGY;
        }

        // 5. 目標ディメンションの取得
        ServerLevel targetLevel = player.server.getLevel(verifiedTarget.dimension());
        if (targetLevel == null) {
            MekanismTPforWaystones.LOGGER.warn("Target level not found: {}", verifiedTarget.dimension());
            return Result.TARGET_NOT_FOUND;
        }

        Result result = Result.CANCELLED_OR_FAILED;

        // 6. ターゲットタイプ別のテレポート確定処理
        if (verifiedTarget.type() == TargetType.WAYSTONE) {
            result = teleportToWaystone(player, targetLevel, verifiedTarget);
        } else if (verifiedTarget.type() == TargetType.MEKANISM) {
            result = teleportToMekanism(player, targetLevel, verifiedTarget);
        }

        // 7. 成功時のみエネルギーを確定消費 (失敗・キャンセル時は未消費)
        if (result == Result.SUCCESS) {
            ItemPortableTeleporterPlus.setEnergy(stack, currentEnergy - cost);
            MekanismTPforWaystones.LOGGER.info("Teleport successful for player '{}' to '{}'. Consumed {} FE.",
                    player.getName().getString(), verifiedTarget.name(), cost);
        }

        return result;
    }

    private static Result teleportToWaystone(ServerPlayer player, ServerLevel targetLevel, TeleportTarget target) {
        try {
            UUID waystoneUid = UUID.fromString(target.id());
            Optional<Waystone> waystoneOpt = WaystonesAPI.getWaystone(targetLevel, waystoneUid);
            if (waystoneOpt.isEmpty()) {
                MekanismTPforWaystones.LOGGER.warn("Waystone entity not found for UUID '{}' in dimension '{}'", target.id(), target.dimension());
                return Result.TARGET_NOT_FOUND;
            }

            Waystone waystone = waystoneOpt.get();
            WaystoneTeleportContext context = WaystonesAPI.createUnboundTeleportContext(player, waystone);
            Either<List<Entity>, WaystoneTeleportError> teleportResult = WaystonesAPI.tryTeleport(context);

            if (teleportResult.left().isPresent()) {
                return Result.SUCCESS;
            } else {
                WaystoneTeleportError error = teleportResult.right().orElse(null);
                MekanismTPforWaystones.LOGGER.warn("Waystones teleport failed: {}", error);
                return Result.CANCELLED_OR_FAILED;
            }
        } catch (Exception e) {
            MekanismTPforWaystones.LOGGER.error("Exception during Waystones teleport to '{}'", target.name(), e);
            return Result.CANCELLED_OR_FAILED;
        }
    }

    private static Result teleportToMekanism(ServerPlayer player, ServerLevel targetLevel, TeleportTarget target) {
        try {
            BlockPos pos = target.pos();

            // チャンクロード検証
            if (!WorldUtils.isBlockLoaded(targetLevel, pos)) {
                targetLevel.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, true);
            }

            TileEntityTeleporter teleporter = WorldUtils.getTileEntity(TileEntityTeleporter.class, targetLevel, pos);
            if (teleporter == null) {
                MekanismTPforWaystones.LOGGER.warn("Mekanism teleporter not found at {} in {}", pos, target.dimension());
                return Result.TARGET_NOT_FOUND;
            }

            BlockPos targetPos = teleporter.getTeleporterTargetPos();
            if (targetPos == null || !isSafeLandingPosition(targetLevel, targetPos)) {
                MekanismTPforWaystones.LOGGER.warn("Landing position {} is unsafe or invalid in {}", targetPos, target.dimension());
                return Result.UNSAFE_DESTINATION;
            }

            double oldX = player.getX();
            double oldY = player.getY();
            double oldZ = player.getZ();
            ServerLevel oldLevel = player.serverLevel();

            // 安全なテレポート実行
            player.teleportTo(targetLevel, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, player.getYRot(), player.getXRot());

            // 効果音・パーティクル演出
            if (oldLevel != targetLevel || player.distanceToSqr(oldX, oldY, oldZ) >= 25.0) {
                oldLevel.playSound(null, oldX, oldY, oldZ, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
            }
            targetLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
            teleporter.sendTeleportParticles();

            return Result.SUCCESS;
        } catch (Exception e) {
            MekanismTPforWaystones.LOGGER.error("Exception during Mekanism teleport to '{}'", target.name(), e);
            return Result.CANCELLED_OR_FAILED;
        }
    }

    /**
     * 着地地点の安全性を検証 (窒息・奈落・溶岩防止)
     */
    private static boolean isSafeLandingPosition(ServerLevel level, BlockPos pos) {
        if (pos.getY() < level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight() - 1) {
            return false;
        }

        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());

        // 足元と頭が固体ブロックで埋まって窒息しないこと
        if (feet.isSuffocating(level, pos) || head.isSuffocating(level, pos.above())) {
            return false;
        }

        // 溶岩などの致命的危険ブロックでないこと
        if (feet.getFluidState().is(Fluids.LAVA) || feet.getFluidState().is(Fluids.FLOWING_LAVA)) {
            return false;
        }

        return true;
    }
}
