package net.kafgchan.mekanism_waystones.target;

import net.kafgchan.mekanism_waystones.compat.curios.CuriosCompat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * テレポート端末の参照元（スロット場所）を抽象化するモデル。
 * 手持ち（MAIN_HAND / OFF_HAND）、Curiosスロット、一般インベントリに対応。
 */
public class TeleportLocationSource {

    public enum SourceType {
        HAND,
        CURIOS,
        INVENTORY
    }

    private final SourceType type;
    private final int slotIndex; // HANDの場合: 0=MAIN_HAND, 1=OFF_HAND. CURIOS/INVENTORYの場合: スロット番号
    private final String curiosIdentifier; // CURIOSの場合の識別子（例: "curio", "charm" 等）

    public TeleportLocationSource(SourceType type, int slotIndex, @Nullable String curiosIdentifier) {
        this.type = type;
        this.slotIndex = slotIndex;
        this.curiosIdentifier = curiosIdentifier != null ? curiosIdentifier : "";
    }

    public static TeleportLocationSource ofHand(InteractionHand hand) {
        return new TeleportLocationSource(SourceType.HAND, hand == InteractionHand.MAIN_HAND ? 0 : 1, null);
    }

    public static TeleportLocationSource ofCurios(String identifier, int slotIndex) {
        return new TeleportLocationSource(SourceType.CURIOS, slotIndex, identifier);
    }

    public static TeleportLocationSource ofInventory(int slotIndex) {
        return new TeleportLocationSource(SourceType.INVENTORY, slotIndex, null);
    }

    public SourceType getType() {
        return type;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public String getCuriosIdentifier() {
        return curiosIdentifier;
    }

    public InteractionHand getHand() {
        return slotIndex == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeVarInt(slotIndex);
        buf.writeUtf(curiosIdentifier);
    }

    public static TeleportLocationSource decode(FriendlyByteBuf buf) {
        SourceType type = buf.readEnum(SourceType.class);
        int slotIndex = buf.readVarInt();
        String identifier = buf.readUtf();
        return new TeleportLocationSource(type, slotIndex, identifier);
    }

    /**
     * プレイヤーの該当スロットから現在の ItemStack を取得する。
     */
    @NotNull
    public ItemStack findItem(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }

        switch (type) {
            case HAND:
                return player.getItemInHand(getHand());
            case CURIOS:
                if (CuriosCompat.isLoaded()) {
                    ItemStack found = CuriosCompat.getItem(player, curiosIdentifier, slotIndex);
                    if (!found.isEmpty()) {
                        return found;
                    }
                }
                return ItemStack.EMPTY;
            case INVENTORY:
                if (slotIndex >= 0 && slotIndex < player.getInventory().getContainerSize()) {
                    return player.getInventory().getItem(slotIndex);
                }
                return ItemStack.EMPTY;
            default:
                return ItemStack.EMPTY;
        }
    }
}
