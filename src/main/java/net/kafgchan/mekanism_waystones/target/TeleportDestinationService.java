package net.kafgchan.mekanism_waystones.target;

import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyManagerWrapper;
import mekanism.common.lib.frequency.FrequencyType;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneTypes;
import net.blay09.mods.waystones.api.WaystoneVisibility;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.blay09.mods.waystones.core.WaystoneManagerImpl;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * プレイヤーが利用可能な Waystones および Mekanism の転送先を収集するサービス (NeoForge 1.21.1)
 * - Waystones: 個人 (PERSONAL) / 共有 (SHARED: 共有転移ポイント・全体公開)
 * - Mekanism: 公開 (PUBLIC) / 非公開 (PRIVATE) / 許可 (TRUSTED)
 */
public final class TeleportDestinationService {

    private TeleportDestinationService() {
    }

    /**
     * プレイヤーが利用可能なすべての転送先 (Waystones + Mekanism) を収集する
     */
    public static List<TeleportTarget> getAvailableTargets(ServerPlayer player) {
        return getAvailableTargets(player, false);
    }

    /**
     * プレイヤーが利用可能な転送先を収集する (isDisposableがtrueの場合は公開/共有テレポーターのみ)
     */
    public static List<TeleportTarget> getAvailableTargets(ServerPlayer player, boolean isDisposable) {
        List<TeleportTarget> targets = new ArrayList<>();

        // 1. Waystones の地点を取得 (個人 & 共有転移ポイント)
        collectWaystoneTargets(player, targets, isDisposable);

        // 2. Mekanism のテレポーター地点を取得 (公開・非公開・許可)
        collectMekanismTargets(player, targets, isDisposable);

        return targets;
    }

    private static void collectWaystoneTargets(ServerPlayer player, List<TeleportTarget> targets, boolean isDisposable) {
        try {
            Set<String> addedUids = new HashSet<>();

            // 1. 共有転移ポイント (Sharestone) および Global Waystone (共有)
            // プレイヤーのアクティベート不要で利用可能 (使い捨て端末でも利用可能)
            MinecraftServer server = player.getServer();
            if (server != null) {
                Stream<Waystone> allWaystones = WaystoneManagerImpl.get(server).getWaystones();
                allWaystones.forEach(ws -> {
                    if (ws != null && ws.isValid()) {
                        boolean isSharestone = WaystoneTypes.isSharestone(ws.getWaystoneType())
                                || (ws.getWaystoneType() != null && ws.getWaystoneType().getPath().contains("sharestone"));
                        boolean isShared = (ws.getVisibility() == WaystoneVisibility.GLOBAL) || isSharestone;
                        if (isShared && addedUids.add(ws.getWaystoneUid().toString())) {
                            targets.add(new TeleportTarget(
                                    TargetType.WAYSTONE,
                                    ws.getWaystoneUid().toString(),
                                    ws.getName().getString(),
                                    ws.getDimension(),
                                    ws.getPos(),
                                    0xFFFFB300, // 共有ゴールド
                                    AccessScope.SHARED
                            ));
                        }
                    }
                });
            }

            // 2. プレイヤーのアクティベート済み地点 (個人)
            // 使い捨てテレポーターでも使用者の個人登録Waystoneおよび共有Waystoneへテレポート可能
            Collection<Waystone> activated = WaystonesAPI.getActivatedWaystones(player);
            for (Waystone ws : activated) {
                if (ws != null && ws.isValid()) {
                    if (addedUids.add(ws.getWaystoneUid().toString())) {
                        boolean isSharestone = WaystoneTypes.isSharestone(ws.getWaystoneType())
                                || (ws.getWaystoneType() != null && ws.getWaystoneType().getPath().contains("sharestone"));
                        boolean isShared = (ws.getVisibility() == WaystoneVisibility.GLOBAL) || isSharestone;
                        targets.add(new TeleportTarget(
                                TargetType.WAYSTONE,
                                ws.getWaystoneUid().toString(),
                                ws.getName().getString(),
                                ws.getDimension(),
                                ws.getPos(),
                                isShared ? 0xFFFFB300 : 0xFFAA55FF, // パープル / ゴールド
                                isShared ? AccessScope.SHARED : AccessScope.PERSONAL
                        ));
                    }
                }
            }
        } catch (Exception e) {
            net.kafgchan.mekanism_waystones.MekanismTPforWaystones.LOGGER.warn("Failed to collect Waystone destinations for player '{}'", player.getName().getString(), e);
        }
    }

    private static void collectMekanismTargets(ServerPlayer player, List<TeleportTarget> targets, boolean isDisposable) {
        try {
            FrequencyManagerWrapper<TeleporterFrequency> wrapper = FrequencyType.TELEPORTER.getManagerWrapper();
            Set<String> processedFrequencies = new HashSet<>();

            // 1. 公開周波数 (PUBLIC)
            FrequencyManager<TeleporterFrequency> publicMgr = wrapper.getPublicManager();
            if (publicMgr != null) {
                addFrequencies(publicMgr.getFrequencies(), targets, processedFrequencies, AccessScope.PUBLIC);
            }

            // 使い捨てテレポーターの場合は公開周波数のみ利用可能
            if (isDisposable) {
                return;
            }

            // 2. プライベート周波数 (所有者一致)
            FrequencyManager<TeleporterFrequency> privateMgr = wrapper.getPrivateManager(player.getUUID());
            if (privateMgr != null) {
                addFrequencies(privateMgr.getFrequencies(), targets, processedFrequencies, AccessScope.PRIVATE);
            }

            // 3. 信頼・許可された周波数 (TRUSTED: 許可)
            FrequencyManager<TeleporterFrequency> trustedMgr = wrapper.getTrustedManager(player.getUUID());
            if (trustedMgr != null) {
                addFrequencies(trustedMgr.getFrequencies(), targets, processedFrequencies, AccessScope.TRUSTED);
            }
        } catch (Exception e) {
            net.kafgchan.mekanism_waystones.MekanismTPforWaystones.LOGGER.warn("Failed to collect Mekanism frequencies for player '{}'", player.getName().getString(), e);
        }
    }

    private static void addFrequencies(Collection<TeleporterFrequency> frequencies, List<TeleportTarget> targets, Set<String> processed, AccessScope scope) {
        if (frequencies == null) return;
        for (TeleporterFrequency freq : frequencies) {
            if (freq == null || !freq.isValid()) continue;
            String freqKey = freq.getSecurity() + ":" + freq.getName() + ":" + freq.getOwner();
            if (!processed.add(freqKey)) continue;

            Set<GlobalPos> coords = freq.getActiveCoords();
            if (coords != null) {
                int index = 1;
                for (GlobalPos globalPos : coords) {
                    String displayName = coords.size() > 1
                            ? freq.getName() + " #" + index
                            : freq.getName();
                    int rgb = freq.getColor() != null ? freq.getColor().getPackedColor() : 0x00E5FF;

                    targets.add(new TeleportTarget(
                            TargetType.MEKANISM,
                            freq.getName() + "@" + globalPos.pos().asLong(),
                            displayName,
                            globalPos.dimension(),
                            globalPos.pos(),
                            rgb,
                            scope
                    ));
                    index++;
                }
            }
        }
    }

    /**
     * クライアントから送信されたターゲット情報を、サーバー側で利用可能な正規の転送先と照合して再解決する
     */
    public static Optional<TeleportTarget> resolveTarget(ServerPlayer player, TargetType type, String targetId) {
        return resolveTarget(player, type, targetId, false);
    }

    public static Optional<TeleportTarget> resolveTarget(ServerPlayer player, TargetType type, String targetId, boolean isDisposable) {
        if (player == null || type == null || targetId == null) {
            return Optional.empty();
        }
        List<TeleportTarget> available = getAvailableTargets(player, isDisposable);
        for (TeleportTarget target : available) {
            if (target.type() == type && target.id().equals(targetId)) {
                return Optional.of(target);
            }
        }
        return Optional.empty();
    }
}
