package net.kafgchan.mekanism_waystones.target;

import mekanism.api.Coord4D;
import mekanism.api.security.SecurityMode;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyManagerWrapper;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityFrequency;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.kafgchan.mekanism_waystones.MekanismTPforWaystones;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * プレイヤーが利用可能な Waystones および Mekanism の転送先を収集するサービス
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
                Stream<IWaystone> allWaystones = WaystonesAPI.getAllWaystones(server);
                allWaystones.forEach(ws -> {
                    if (ws != null && ws.isValid()) {
                        boolean isSharestone = ws.getWaystoneType() != null && ws.getWaystoneType().getPath().contains("sharestone");
                        boolean isShared = ws.isGlobal() || isSharestone;
                        if (isShared && addedUids.add(ws.getWaystoneUid().toString())) {
                            targets.add(new TeleportTarget(
                                    TargetType.WAYSTONE,
                                    ws.getWaystoneUid().toString(),
                                    ws.getName(),
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
            Collection<IWaystone> activated = WaystonesAPI.getActivatedWaystones(player);
            for (IWaystone ws : activated) {
                if (ws != null && ws.isValid()) {
                    if (addedUids.add(ws.getWaystoneUid().toString())) {
                        boolean isSharestone = ws.getWaystoneType() != null && ws.getWaystoneType().getPath().contains("sharestone");
                        boolean isShared = ws.isGlobal() || isSharestone;
                        targets.add(new TeleportTarget(
                                TargetType.WAYSTONE,
                                ws.getWaystoneUid().toString(),
                                ws.getName(),
                                ws.getDimension(),
                                ws.getPos(),
                                isShared ? 0xFFFFB300 : 0xFFAA55FF, // パープル / ゴールド
                                isShared ? AccessScope.SHARED : AccessScope.PERSONAL
                        ));
                    }
                }
            }
        } catch (Throwable e) {
            MekanismTPforWaystones.LOGGER.warn("Failed to collect Waystone destinations for player '{}'", player.getName().getString(), e);
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

            // 使い捨てテレポーター時は公開のみ
            if (isDisposable) {
                return;
            }

            // 2. プレイヤー自身のプライベート周波数 (所有者設定に応じて PRIVATE または TRUSTED)
            SecurityMode selfSecurityMode = getPlayerSecurityMode(player.getUUID());
            AccessScope selfScope = (selfSecurityMode == SecurityMode.TRUSTED) ? AccessScope.TRUSTED : AccessScope.PRIVATE;

            FrequencyManager<TeleporterFrequency> privateMgr = wrapper.getPrivateManager(player.getUUID());
            if (privateMgr != null) {
                addFrequencies(privateMgr.getFrequencies(), targets, processedFrequencies, selfScope);
            }

            // 3. 他プレイヤー所有で、自分が Trusted に登録されている周波数 (TRUSTED: 許可)
            MinecraftServer server = player.getServer();
            if (server != null) {
                for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                    UUID otherUUID = other.getUUID();
                    if (otherUUID.equals(player.getUUID())) continue;

                    if (isPlayerTrustedBy(otherUUID, player.getUUID())) {
                        FrequencyManager<TeleporterFrequency> otherMgr = wrapper.getPrivateManager(otherUUID);
                        if (otherMgr != null) {
                            addFrequencies(otherMgr.getFrequencies(), targets, processedFrequencies, AccessScope.TRUSTED);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            MekanismTPforWaystones.LOGGER.warn("Failed to collect Mekanism frequencies for player '{}'", player.getName().getString(), e);
        }
    }

    private static SecurityMode getPlayerSecurityMode(UUID ownerUUID) {
        try {
            FrequencyManager<SecurityFrequency> secMgr = FrequencyType.SECURITY.getManagerWrapper().getPrivateManager(ownerUUID);
            if (secMgr != null) {
                for (SecurityFrequency freq : secMgr.getFrequencies()) {
                    if (freq != null) {
                        return freq.getSecurityMode();
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return SecurityMode.PRIVATE;
    }

    private static boolean isPlayerTrustedBy(UUID ownerUUID, UUID targetUUID) {
        try {
            FrequencyManager<SecurityFrequency> secMgr = FrequencyType.SECURITY.getManagerWrapper().getPrivateManager(ownerUUID);
            if (secMgr != null) {
                for (SecurityFrequency freq : secMgr.getFrequencies()) {
                    if (freq != null && freq.getSecurityMode() == SecurityMode.TRUSTED) {
                        return freq.getTrustedUUIDs().contains(targetUUID);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static void addFrequencies(Collection<TeleporterFrequency> frequencies, List<TeleportTarget> targets, Set<String> processed, AccessScope scope) {
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
    public static Optional<TeleportTarget> resolveTarget(ServerPlayer player, TargetType type, String id) {
        return resolveTarget(player, type, id, false);
    }

    public static Optional<TeleportTarget> resolveTarget(ServerPlayer player, TargetType type, String id, boolean isDisposable) {
        if (player == null || type == null || id == null || id.isEmpty()) {
            return Optional.empty();
        }

        List<TeleportTarget> available = getAvailableTargets(player, isDisposable);
        for (TeleportTarget target : available) {
            if (target.type() == type && target.id().equals(id)) {
                return Optional.of(target);
            }
        }

        return Optional.empty();
    }
}
