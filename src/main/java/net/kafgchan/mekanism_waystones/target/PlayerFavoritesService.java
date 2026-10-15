package net.kafgchan.mekanism_waystones.target;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * プレイヤー単位のお気に入り目的地を永続管理するサービス (Player Persistent NBT)
 */
public final class PlayerFavoritesService {
    private static final String FAVORITES_KEY = "mekanism_waystones_favorites";

    private PlayerFavoritesService() {
    }

    public static String getTargetKey(TargetType type, String id) {
        return type.name() + ":" + id;
    }

    public static String getTargetKey(TeleportTarget target) {
        return getTargetKey(target.type(), target.id());
    }

    public static Set<String> getFavorites(ServerPlayer player) {
        CompoundTag persistentData = getPlayerPersistedData(player);
        if (!persistentData.contains(FAVORITES_KEY, Tag.TAG_LIST)) {
            return Collections.emptySet();
        }
        ListTag list = persistentData.getList(FAVORITES_KEY, Tag.TAG_STRING);
        Set<String> favorites = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            favorites.add(list.getString(i));
        }
        return Collections.unmodifiableSet(favorites);
    }

    public static boolean isFavorite(ServerPlayer player, TeleportTarget target) {
        return getFavorites(player).contains(getTargetKey(target));
    }

    public static boolean toggleFavorite(ServerPlayer player, String targetKey) {
        CompoundTag persistentData = getPlayerPersistedData(player);
        ListTag list;
        if (persistentData.contains(FAVORITES_KEY, Tag.TAG_LIST)) {
            list = persistentData.getList(FAVORITES_KEY, Tag.TAG_STRING);
        } else {
            list = new ListTag();
            persistentData.put(FAVORITES_KEY, list);
        }

        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(targetKey)) {
                list.remove(i);
                found = true;
                break;
            }
        }

        if (!found) {
            list.add(StringTag.valueOf(targetKey));
            return true; // Added
        }
        return false; // Removed
    }

    private static CompoundTag getPlayerPersistedData(ServerPlayer player) {
        CompoundTag customData = player.getPersistentData();
        if (!customData.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag persisted = new CompoundTag();
            customData.put(Player.PERSISTED_NBT_TAG, persisted);
            return persisted;
        }
        return customData.getCompound(Player.PERSISTED_NBT_TAG);
    }
}
