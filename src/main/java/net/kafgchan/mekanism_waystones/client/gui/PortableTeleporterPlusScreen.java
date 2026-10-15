package net.kafgchan.mekanism_waystones.client.gui;

import net.kafgchan.mekanism_waystones.item.ItemPortableTeleporterPlus;
import net.kafgchan.mekanism_waystones.menu.PortableTeleporterPlusMenu;
import net.kafgchan.mekanism_waystones.network.ModNetwork;
import net.kafgchan.mekanism_waystones.network.PacketSetQuickTarget;
import net.kafgchan.mekanism_waystones.network.PacketTeleportRequest;
import net.kafgchan.mekanism_waystones.network.PacketToggleFavorite;
import net.kafgchan.mekanism_waystones.target.AccessScope;
import net.kafgchan.mekanism_waystones.target.PlayerFavoritesService;
import net.kafgchan.mekanism_waystones.target.TargetType;
import net.kafgchan.mekanism_waystones.target.TeleportTarget;
import net.kafgchan.mekanism_waystones.util.TeleportCostCalculator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * テレポート端末画面 (Forge 1.20.1)
 * - 下部インベントリ表示を廃止し、目的地一覧を8行へ拡張。
 * - お気に入り(★)タブおよびお気に入り優先ソート機能。
 * - クイックテレポート先の登録ボタンおよび表示。
 * - 使い捨てテレポーターモード対応。
 */
public class PortableTeleporterPlusScreen extends AbstractContainerScreen<PortableTeleporterPlusMenu> {
    private static final int BG_COLOR = 0xFF181B20;
    private static final int BORDER_COLOR = 0xFF2F3947;
    private static final int ACCENT_CYAN = 0xFF00E5FF;
    private static final int ACCENT_GOLD = 0xFFFFD700;
    private static final int PANEL_BG = 0xFF111317;
    private static final int SELECTED_BG = 0xFF0B3447;
    private static final int HOVER_BG = 0xFF202936;
    private static final int TEXT_DIM = 0xFF8A9BA8;
    private static final int TEXT_WHITE = 0xFFEAEAEA;
    private static final int COST_RED = 0xFFFF5555;
    private static final int FE_GREEN = 0xFF38D648;

    private static final int VISIBLE_ROWS = 8;
    private static final int ROW_HEIGHT = 15;

    private enum TabFilter {
        ALL, MEK, WAY, FAVORITE
    }

    private TabFilter currentTab = TabFilter.ALL;
    private String searchQuery = "";
    private EditBox searchBox;

    private Button btnAll;
    private Button btnMek;
    private Button btnWay;
    private Button btnFav;

    private Button teleportButton;
    private Button favoriteButton;
    private Button setQuickButton;

    private final Set<String> localFavorites = new HashSet<>();
    private final List<TeleportTarget> filteredTargets = new ArrayList<>();
    private TeleportTarget selectedTarget = null;
    private TeleportTarget currentQuickTarget = null;
    private int scrollOffset = 0;
    private long lastClickTime = 0;

    public PortableTeleporterPlusScreen(PortableTeleporterPlusMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 250;
        this.imageHeight = 176;
        this.localFavorites.addAll(menu.getFavorites());
        this.currentQuickTarget = menu.getQuickTarget();
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos;
        int y = this.topPos;

        // タブボタン (ALL, MEK, WAY, ★)
        btnAll = Button.builder(Component.translatable("gui.mekanism_waystones.tab_all"), b -> setTab(TabFilter.ALL))
                .bounds(x + 10, y + 17, 30, 14).build();
        btnMek = Button.builder(Component.translatable("gui.mekanism_waystones.tab_mek"), b -> setTab(TabFilter.MEK))
                .bounds(x + 42, y + 17, 34, 14).build();
        btnWay = Button.builder(Component.translatable("gui.mekanism_waystones.tab_way"), b -> setTab(TabFilter.WAY))
                .bounds(x + 78, y + 17, 34, 14).build();
        btnFav = Button.builder(Component.literal("★"), b -> setTab(TabFilter.FAVORITE))
                .bounds(x + 114, y + 17, 20, 14).build();

        this.addRenderableWidget(btnAll);
        this.addRenderableWidget(btnMek);
        this.addRenderableWidget(btnWay);
        this.addRenderableWidget(btnFav);

        // 検索ボックス
        searchBox = new EditBox(this.font, x + 10, y + 33, 124, 13, Component.translatable("gui.mekanism_waystones.search"));
        searchBox.setResponder(text -> {
            this.searchQuery = text.trim().toLowerCase(Locale.ROOT);
            this.scrollOffset = 0;
            updateFilteredList();
        });
        searchBox.setHint(Component.translatable("gui.mekanism_waystones.search"));
        searchBox.setBordered(true);
        this.addRenderableWidget(searchBox);

        // 右パネルのアクションボタン群
        int rightX = x + 140;
        int rightY = y + 17;

        // お気に入りトグルボタン
        favoriteButton = Button.builder(Component.translatable("gui.mekanism_waystones.favorite"), b -> onToggleFavorite())
                .bounds(rightX, rightY + 60, 100, 15).build();
        favoriteButton.active = false;
        this.addRenderableWidget(favoriteButton);

        // クイック先に設定ボタン (通常端末のみ)
        if (!menu.isDisposable()) {
            setQuickButton = Button.builder(Component.translatable("gui.mekanism_waystones.set_quick"), b -> onSetQuickTarget())
                    .bounds(rightX, rightY + 78, 100, 15).build();
            setQuickButton.active = false;
            this.addRenderableWidget(setQuickButton);
        }

        // テレポート実行ボタン
        int teleportBtnY = menu.isDisposable() ? rightY + 86 : rightY + 96;
        int teleportBtnH = menu.isDisposable() ? 22 : 18;
        teleportButton = Button.builder(Component.translatable("gui.mekanism_waystones.teleport"), b -> onTeleport())
                .bounds(rightX, teleportBtnY, 100, teleportBtnH).build();
        teleportButton.active = false;
        this.addRenderableWidget(teleportButton);

        // クイック先が登録されている場合、初期状態で自動選択して即テレポート可能状態にする
        if (this.currentQuickTarget != null && this.selectedTarget == null) {
            for (TeleportTarget t : menu.getTargets()) {
                if (t.type() == this.currentQuickTarget.type() && t.id().equals(this.currentQuickTarget.id())) {
                    this.selectedTarget = t;
                    break;
                }
            }
            if (this.selectedTarget == null) {
                this.selectedTarget = this.currentQuickTarget;
            }
        }

        updateFilteredList();
    }



    private void setTab(TabFilter tab) {
        this.currentTab = tab;
        this.scrollOffset = 0;
        updateFilteredList();
    }

    private void onToggleFavorite() {
        if (selectedTarget == null) return;
        String key = PlayerFavoritesService.getTargetKey(selectedTarget);
        if (localFavorites.contains(key)) {
            localFavorites.remove(key);
        } else {
            localFavorites.add(key);
        }
        ModNetwork.sendToServer(new PacketToggleFavorite(key));
        updateFilteredList();
    }

    private void onSetQuickTarget() {
        if (selectedTarget == null || menu.isDisposable()) return;
        this.currentQuickTarget = selectedTarget;
        ModNetwork.sendToServer(new PacketSetQuickTarget(menu.getSource(), selectedTarget));
    }

    private void updateFilteredList() {
        filteredTargets.clear();
        List<TeleportTarget> allTargets = menu.getTargets();

        // 1. フィルタリング
        List<TeleportTarget> pool = new ArrayList<>();
        for (TeleportTarget target : allTargets) {
            String key = PlayerFavoritesService.getTargetKey(target);
            boolean isFav = localFavorites.contains(key);

            if (currentTab == TabFilter.FAVORITE && !isFav) {
                continue;
            }
            if (currentTab == TabFilter.MEK && target.type() != TargetType.MEKANISM) {
                continue;
            }
            if (currentTab == TabFilter.WAY && target.type() != TargetType.WAYSTONE) {
                continue;
            }
            if (!searchQuery.isEmpty() && !target.name().toLowerCase(Locale.ROOT).contains(searchQuery)) {
                continue;
            }
            pool.add(target);
        }

        // 2. お気に入り優先ソート (お気に入りを先頭へ)
        pool.sort((a, b) -> {
            boolean aFav = localFavorites.contains(PlayerFavoritesService.getTargetKey(a));
            boolean bFav = localFavorites.contains(PlayerFavoritesService.getTargetKey(b));
            if (aFav != bFav) {
                return aFav ? -1 : 1;
            }
            return a.name().compareToIgnoreCase(b.name());
        });

        filteredTargets.addAll(pool);

        if (selectedTarget != null && !filteredTargets.contains(selectedTarget)) {
            selectedTarget = null;
        }

        if (selectedTarget != null) {
            int targetIdx = filteredTargets.indexOf(selectedTarget);
            if (targetIdx >= 0) {
                if (targetIdx < scrollOffset) {
                    scrollOffset = targetIdx;
                } else if (targetIdx >= scrollOffset + VISIBLE_ROWS) {
                    scrollOffset = Math.max(0, targetIdx - VISIBLE_ROWS + 1);
                }
            }
        }

        updateActionButtons();
    }


    private void updateActionButtons() {
        net.minecraft.client.Minecraft mc = this.minecraft;
        if (selectedTarget == null || mc == null || mc.player == null) {
            teleportButton.active = false;
            favoriteButton.active = false;
            if (setQuickButton != null) setQuickButton.active = false;
            favoriteButton.setMessage(Component.translatable("gui.mekanism_waystones.favorite"));
            return;
        }

        favoriteButton.active = true;
        String key = PlayerFavoritesService.getTargetKey(selectedTarget);
        boolean isFav = localFavorites.contains(key);
        favoriteButton.setMessage(isFav ? Component.translatable("gui.mekanism_waystones.favorite_remove") : Component.translatable("gui.mekanism_waystones.favorite_add"));

        if (setQuickButton != null) {
            setQuickButton.active = true;
        }

        if (menu.isDisposable()) {
            teleportButton.active = true;
        } else {
            int cost = TeleportCostCalculator.calculateCost(mc.player, selectedTarget);
            teleportButton.active = menu.getEnergy() >= cost;
        }
    }

    private void onTeleport() {
        if (selectedTarget == null || !teleportButton.active) {
            return;
        }
        ModNetwork.sendToServer(new PacketTeleportRequest(menu.getSource(), selectedTarget));
        this.onClose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int listX = leftPos + 10;
        int listY = topPos + 48;
        int listW = 124;
        int listH = VISIBLE_ROWS * ROW_HEIGHT;

        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            int maxOffset = Math.max(0, filteredTargets.size() - VISIBLE_ROWS);
            if (maxOffset > 0) {
                scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta), 0, maxOffset);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listX = leftPos + 10;
        int listY = topPos + 48;
        int listW = 124;

        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int targetIdx = scrollOffset + i;
            if (targetIdx >= filteredTargets.size()) {
                break;
            }
            int rowY = listY + i * ROW_HEIGHT;
            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                TeleportTarget clicked = filteredTargets.get(targetIdx);
                long currentTime = System.currentTimeMillis();
                if (button == 0 && clicked.equals(this.selectedTarget) && (currentTime - this.lastClickTime < 350)) {
                    // ダブルクリック：設定先として登録
                    onSetQuickTarget();
                    this.lastClickTime = 0;
                } else {
                    this.selectedTarget = clicked;
                    this.lastClickTime = currentTime;
                    updateActionButtons();
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // メイン背景 & 枠線
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, BG_COLOR);
        guiGraphics.renderOutline(x, y, imageWidth, imageHeight, BORDER_COLOR);
        guiGraphics.renderOutline(x + 1, y + 1, imageWidth - 2, imageHeight - 2, 0xFF1F242C);

        // ヘッダー境界ライン
        guiGraphics.fill(x + 6, y + 15, x + imageWidth - 6, y + 16, ACCENT_CYAN);

        // 左側リストパネル
        int listX = x + 10;
        int listY = y + 48;
        int listW = 124;
        int listH = VISIBLE_ROWS * ROW_HEIGHT; // 120px
        guiGraphics.fill(listX, listY, listX + listW, listY + listH, PANEL_BG);
        guiGraphics.renderOutline(listX - 1, listY - 1, listW + 2, listH + 2, BORDER_COLOR);

        // 目的地の描画 (8行)
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int targetIdx = scrollOffset + i;
            if (targetIdx >= filteredTargets.size()) {
                break;
            }
            TeleportTarget target = filteredTargets.get(targetIdx);
            int rowY = listY + i * ROW_HEIGHT;
            boolean isSelected = target.equals(selectedTarget);
            boolean isHovered = mouseX >= listX && mouseX <= listX + listW && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            boolean isFav = localFavorites.contains(PlayerFavoritesService.getTargetKey(target));

            int rowBg = isSelected ? SELECTED_BG : (isHovered ? HOVER_BG : PANEL_BG);
            guiGraphics.fill(listX, rowY, listX + listW, rowY + ROW_HEIGHT, rowBg);
            if (isSelected) {
                guiGraphics.renderOutline(listX, rowY, listW, ROW_HEIGHT, ACCENT_CYAN);
            }

            // バッジ [Mek公開] [Mek非公開] [Mek許可] [Way個人] [Way共有]
            AccessScope scope = target.scope() != null ? target.scope()
                    : (target.type() == TargetType.MEKANISM ? AccessScope.PUBLIC : AccessScope.PERSONAL);
            String badge = scope.getBadgeText();
            int badgeColor = scope.getColor();
            guiGraphics.drawString(this.font, badge, listX + 2, rowY + 3, badgeColor, false);

            int badgeW = this.font.width(badge);

            // お気に入りマーク (★)
            int currentX = listX + 2 + badgeW + 2;
            if (isFav) {
                guiGraphics.drawString(this.font, "★", currentX, rowY + 3, ACCENT_GOLD, false);
                currentX += 8;
            }

            // 名前描画
            int remainingW = listW - (currentX - listX) - 2;
            int nameColor = target.type() == TargetType.MEKANISM ? (target.color() | 0xFF000000) : TEXT_WHITE;
            String trimmedName = this.font.plainSubstrByWidth(target.name(), remainingW);
            guiGraphics.drawString(this.font, trimmedName, currentX, rowY + 3, nameColor, false);
        }

        // スクロールバーインジケータ
        if (filteredTargets.size() > VISIBLE_ROWS) {
            int scrollBarH = Math.max(10, listH * VISIBLE_ROWS / filteredTargets.size());
            int maxOffset = filteredTargets.size() - VISIBLE_ROWS;
            int scrollBarY = listY + (listH - scrollBarH) * scrollOffset / maxOffset;
            guiGraphics.fill(listX + listW - 3, scrollBarY, listX + listW - 1, scrollBarY + scrollBarH, ACCENT_CYAN);
        }

        // 右側パネル (詳細エリア)
        int rightX = x + 140;
        int rightY = y + 17;
        int rightW = 100;
        int rightH = 44;
        guiGraphics.fill(rightX, rightY, rightX + rightW, rightY + rightH, PANEL_BG);
        guiGraphics.renderOutline(rightX - 1, rightY - 1, rightW + 2, rightH + 2, BORDER_COLOR);

        net.minecraft.client.Minecraft mc = this.minecraft;
        if (selectedTarget != null && mc != null && mc.player != null) {
            String nameDisp = this.font.plainSubstrByWidth(selectedTarget.name(), rightW - 6);
            guiGraphics.drawString(this.font, nameDisp, rightX + 3, rightY + 2, ACCENT_CYAN, false);

            AccessScope scope = selectedTarget.scope() != null ? selectedTarget.scope()
                    : (selectedTarget.type() == TargetType.MEKANISM ? AccessScope.PUBLIC : AccessScope.PERSONAL);
            String typeName = selectedTarget.type() == TargetType.MEKANISM
                    ? Component.translatable("gui.mekanism_waystones.type_mekanism").getString()
                    : Component.translatable("gui.mekanism_waystones.type_waystone").getString();
            String scopeDisp = typeName + " (" + scope.getDisplayName().getString() + ")";
            guiGraphics.drawString(this.font, scopeDisp, rightX + 3, rightY + 11, scope.getColor(), false);

            String dimDisp = selectedTarget.dimension().location().getPath();
            guiGraphics.drawString(this.font, Component.translatable("gui.mekanism_waystones.dimension_short", dimDisp).getString(), rightX + 3, rightY + 19, TEXT_WHITE, false);

            boolean sameDim = mc.player.level().dimension().equals(selectedTarget.dimension());
            if (sameDim) {
                double dist = Math.sqrt(mc.player.blockPosition().distSqr(selectedTarget.pos()));
                guiGraphics.drawString(this.font, Component.translatable("gui.mekanism_waystones.dist_short", String.format(Locale.ROOT, "%.0f", dist)).getString(), rightX + 3, rightY + 27, TEXT_DIM, false);
            } else {
                guiGraphics.drawString(this.font, Component.translatable("gui.mekanism_waystones.dist_interdim").getString(), rightX + 3, rightY + 27, TEXT_DIM, false);
            }

            if (menu.isDisposable()) {
                guiGraphics.drawString(this.font, Component.translatable("gui.mekanism_waystones.cost_item").getString(), rightX + 3, rightY + 35, ACCENT_GOLD, false);
            } else {
                int cost = TeleportCostCalculator.calculateCost(mc.player, selectedTarget);
                boolean canAfford = menu.getEnergy() >= cost;
                guiGraphics.drawString(this.font, Component.translatable("gui.mekanism_waystones.cost_fe", cost).getString(), rightX + 3, rightY + 35, canAfford ? ACCENT_CYAN : COST_RED, false);
            }
        } else {
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.mekanism_waystones.select_target").getString(), rightX + rightW / 2, rightY + 18, TEXT_DIM);
        }

        // 設定先ラベル表示 (通常端末のみ)
        if (!menu.isDisposable()) {
            String quickName = currentQuickTarget != null ? currentQuickTarget.name() : Component.translatable("gui.mekanism_waystones.not_set").getString();
            String prefix = Component.translatable("gui.mekanism_waystones.quick_label").getString() + ": ";
            String qDisp = this.font.plainSubstrByWidth(prefix + quickName, rightW);
            guiGraphics.drawString(this.font, qDisp, rightX, rightY + 48, ACCENT_GOLD, false);
        } else {
            String dispLabel = Component.translatable("gui.mekanism_waystones.disposable_label").getString();
            guiGraphics.drawCenteredString(this.font, dispLabel, rightX + rightW / 2, rightY + 48, ACCENT_GOLD);
        }

        // エネルギーバーエリア (通常端末のみ)
        if (!menu.isDisposable()) {
            int barX = rightX;
            int barY = rightY + 118;
            int barW = rightW;
            int barH = 14;
            guiGraphics.fill(barX, barY, barX + barW, barY + barH, 0xFF0E1013);
            guiGraphics.renderOutline(barX, barY, barW, barH, BORDER_COLOR);

            float energyRatio = (float) menu.getEnergy() / Math.max(1, ItemPortableTeleporterPlus.getMaxEnergy());
            int fillW = (int) (barW * Mth.clamp(energyRatio, 0.0f, 1.0f));
            if (fillW > 0) {
                guiGraphics.fill(barX + 1, barY + 1, barX + fillW - 1, barY + barH - 1, FE_GREEN);
            }

            String energyText = (menu.getEnergy() / 1000) + "k FE";
            guiGraphics.drawCenteredString(this.font, energyText, barX + barW / 2, barY + 3, TEXT_WHITE);
        }
    }


    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 5, ACCENT_CYAN, false);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
