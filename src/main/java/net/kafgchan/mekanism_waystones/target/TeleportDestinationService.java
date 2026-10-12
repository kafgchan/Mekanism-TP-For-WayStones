package net.kafgchan.mekanism_waystones.target;

import mekanism.api.Coord4D;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyManagerWrapper;
import mekanism.common.lib.frequency.FrequencyType;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.kafgchan.mekanism_waystones.MekanismTPforWaystones;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * プレイヤーが利用可能な Waystones および Mekanism の転送先を収集するサービス
 */
public final class TeleportDestinationService {

    private TeleportDestinationService() {
    }

    /**
     * プレイヤーが利用可能なすべての転送先 (Waystones + Mekanism) を収集する
     */
    public static List<TeleportTarget> getAvailableTargets(ServerPlayer player) {
        List<TeleportTarget> targets = new ArrayList<>();

        // 1. Waystones のアクティベート済み地点を取得
        collectWaystoneTargets(player, targets);

        // 2. Mekanism のアクセス可能なテレポーター地点を取得
        collectMekanismTargets(player, targets);

        return targets;
    }

    private static void collectWaystoneTargets(ServerPlayer player, List<TeleportTarget> targets) {
        try {
            Collection<IWaystone> activated = WaystonesAPI.getActivatedWaystones(player);
            for (IWaystone ws : activated) {
                if (ws != null && ws.isValid()) {
                    targets.add(new TeleportTarget(
                            TargetType.WAYSTONE,
                            ws.getWaystoneUid().toString(),
                            ws.getName(),
                            ws.getDimension(),
                            ws.getPos(),
                            0x9B59B6 // Waystones パープル
                    ));
                }
            }
        } catch (Throwable e) {
            MekanismTPforWaystones.LOGGER.warn("Failed to collect Waystone destinations for player '{}'", player.getName().getString(), e);
        }
    }

    private static void collectMekanismTargets(ServerPlayer player, List<TeleportTarget> targets) {
        try {
            FrequencyManagerWrapper<TeleporterFrequency> wrapper = FrequencyType.TELEPORTER.getManagerWrapper();
            Set<String> processedFrequencies = new HashSet<>();

            // 1. 公開周波数
            FrequencyManager<TeleporterFrequency> publicMgr = wrapper.getPublicManager();
            if (publicMgr != null) {
                addFrequencies(publicMgr.getFrequencies(), targets, processedFrequencies);
            }

            // 2. プライベート周波数 (所有者一致)
            FrequencyManager<TeleporterFrequency> privateMgr = wrapper.getPrivateManager(player.getUUID());
            if (privateMgr != null) {
                addFrequencies(privateMgr.getFrequencies(), targets, processedFrequencies);
            }
        } catch (Throwable e) {
            MekanismTPforWaystones.LOGGER.warn("Failed to collect Mekanism frequencies for player '{}'", player.getName().getString(), e);
        }
    }

    private static void addFrequencies(Collection<TeleporterFrequency> frequencies, List<TeleportTarget> targets, Set<String> processed) {
        if (frequencies == null) return;
        for (TeleporterFrequency freq : frequencies) {
            if (freq == null || !freq.isValid()) continue;
            String freqKey = freq.getSecurity() + ":" + freq.getName() + ":" + freq.getOwner();
            if (!processed.add(freqKey)) continue;

            Set<Coord4D> coords = freq.getActiveCoords();
            if (coords != null) {
                int index = 1;
                for (Coord4D coord : coords) {
                    String displayName = coords.size() > 1
                            ? freq.getName() + " #" + index
                            : freq.getName();
                    int rgb = 0x00E5FF;
                    if (freq.getColor() != null && freq.getColor().getColor() != null) {
                        rgb = freq.getColor().getColor().getValue();
                    }

                    targets.add(new TeleportTarget(
                            TargetType.MEKANISM,
                            freq.getName() + "@" + coord.getPos().asLong(),
                            displayName,
                            coord.dimension,
                            coord.getPos(),
                            rgb
                    ));
                    index++;
                }
            }
        }
    }

    /**
     * クライアントから送信されたターゲット情報を、サーバー側で利用可能な正規の転送先と照合して再解決する
     */
    public static Optional<TeleportTarget> resolveTarget(ServerPlayer player, TargetType type, String id) {
        if (player == null || type == null || id == null || id.isEmpty()) {
            return Optional.empty();
        }

        List<TeleportTarget> available = getAvailableTargets(player);
        for (TeleportTarget target : available) {
            if (target.type() == type && target.id().equals(id)) {
                return Optional.of(target);
            }
        }

        return Optional.empty();
    }
}
