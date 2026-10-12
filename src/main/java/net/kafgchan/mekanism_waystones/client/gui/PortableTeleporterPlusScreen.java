package net.kafgchan.mekanism_waystones.client.gui;

import net.kafgchan.mekanism_waystones.item.ItemPortableTeleporterPlus;
import net.kafgchan.mekanism_waystones.menu.PortableTeleporterPlusMenu;
import net.kafgchan.mekanism_waystones.network.ModNetwork;
import net.kafgchan.mekanism_waystones.network.PacketTeleportRequest;
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
import java.util.List;
import java.util.Locale;

public class PortableTeleporterPlusScreen extends AbstractContainerScreen<PortableTeleporterPlusMenu> {
    private static final int BG_COLOR = 0xFF181B20;
    private static final int BORDER_COLOR = 0xFF2F3947;
    private static final int ACCENT_CYAN = 0xFF00E5FF;
    private static final int PANEL_BG = 0xFF111317;
    private static final int SELECTED_BG = 0xFF0B3447;
    private static final int HOVER_BG = 0xFF202936;
    private static final int TEXT_DIM = 0xFF8A9BA8;
    private static final int TEXT_WHITE = 0xFFEAEAEA;
    private static final int COST_RED = 0xFFFF5555;
    private static final int FE_GREEN = 0xFF38D648;

    private static final int VISIBLE_ROWS = 5;
    private static final int ROW_HEIGHT = 15;

    private TargetType filterType = null;
    private String searchQuery = "";
    private EditBox searchBox;
    private Button btnAll;
    private Button btnMek;
    private Button btnWay;
    private Button teleportButton;

    private final List<TeleportTarget> filteredTargets = new ArrayList<>();
    private TeleportTarget selectedTarget = null;
    private int scrollOffset = 0;

    public PortableTeleporterPlusScreen(PortableTeleporterPlusMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 250;
        this.imageHeight = 222;
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos;
        int y = this.topPos;

        // Tabs
        btnAll = Button.builder(Component.literal("ALL"), b -> setFilter(null))
                .bounds(x + 10, y + 18, 36, 14).build();
        btnMek = Button.builder(Component.literal("MEK"), b -> setFilter(TargetType.MEKANISM))
                .bounds(x + 48, y + 18, 42, 14).build();
        btnWay = Button.builder(Component.literal("WAY"), b -> setFilter(TargetType.WAYSTONE))
                .bounds(x + 92, y + 18, 42, 14).build();

        this.addRenderableWidget(btnAll);
        this.addRenderableWidget(btnMek);
        this.addRenderableWidget(btnWay);

        // Search Box
        searchBox = new EditBox(this.font, x + 10, y + 34, 124, 14, Component.literal("Search"));
        searchBox.setResponder(text -> {
            this.searchQuery = text.trim().toLowerCase(Locale.ROOT);
            this.scrollOffset = 0;
            updateFilteredList();
        });
        searchBox.setBordered(true);
        this.addRenderableWidget(searchBox);

        // Teleport Button
        teleportButton = Button.builder(Component.translatable("gui.mekanism_waystones.teleport"), b -> onTeleport())
                .bounds(x + 142, y + 112, 98, 20).build();
        teleportButton.active = false;
        this.addRenderableWidget(teleportButton);

        updateFilteredList();
    }

    private void setFilter(TargetType type) {
        this.filterType = type;
        this.scrollOffset = 0;
        updateFilteredList();
    }

    private void updateFilteredList() {
        filteredTargets.clear();
        for (TeleportTarget target : menu.getTargets()) {
            if (filterType != null && target.type() != filterType) {
                continue;
            }
            if (!searchQuery.isEmpty() && !target.name().toLowerCase(Locale.ROOT).contains(searchQuery)) {
                continue;
            }
            filteredTargets.add(target);
        }

        if (selectedTarget != null && !filteredTargets.contains(selectedTarget)) {
            selectedTarget = null;
        }

        updateTeleportButton();
    }

    private void updateTeleportButton() {
        net.minecraft.client.Minecraft mc = this.minecraft;
        if (selectedTarget == null || mc == null || mc.player == null) {
            teleportButton.active = false;
            return;
        }
        int cost = TeleportCostCalculator.calculateCost(mc.player, selectedTarget);
        teleportButton.active = menu.getEnergy() >= cost;
    }

    private void onTeleport() {
        if (selectedTarget == null || !teleportButton.active) {
            return;
        }
        ModNetwork.sendToServer(new PacketTeleportRequest(menu.getHand(), selectedTarget));
        this.onClose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int listX = leftPos + 10;
        int listY = topPos + 50;
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
        int listY = topPos + 50;
        int listW = 124;

        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int targetIdx = scrollOffset + i;
            if (targetIdx >= filteredTargets.size()) {
                break;
            }
            int rowY = listY + i * ROW_HEIGHT;
            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                selectedTarget = filteredTargets.get(targetIdx);
                updateTeleportButton();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Main Background & Border
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, BG_COLOR);
        guiGraphics.renderOutline(x, y, imageWidth, imageHeight, BORDER_COLOR);
        guiGraphics.renderOutline(x + 1, y + 1, imageWidth - 2, imageHeight - 2, 0xFF1F242C);

        // Header Line
        guiGraphics.fill(x + 6, y + 15, x + imageWidth - 6, y + 16, ACCENT_CYAN);

        // Left Panel (List Box)
        int listX = x + 10;
        int listY = y + 50;
        int listW = 124;
        int listH = VISIBLE_ROWS * ROW_HEIGHT;
        guiGraphics.fill(listX, listY, listX + listW, listY + listH, PANEL_BG);
        guiGraphics.renderOutline(listX - 1, listY - 1, listW + 2, listH + 2, BORDER_COLOR);

        // Render List Items
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int targetIdx = scrollOffset + i;
            if (targetIdx >= filteredTargets.size()) {
                break;
            }
            TeleportTarget target = filteredTargets.get(targetIdx);
            int rowY = listY + i * ROW_HEIGHT;
            boolean isSelected = target.equals(selectedTarget);
            boolean isHovered = mouseX >= listX && mouseX <= listX + listW && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;

            int rowBg = isSelected ? SELECTED_BG : (isHovered ? HOVER_BG : PANEL_BG);
            guiGraphics.fill(listX, rowY, listX + listW, rowY + ROW_HEIGHT, rowBg);
            if (isSelected) {
                guiGraphics.renderOutline(listX, rowY, listW, ROW_HEIGHT, ACCENT_CYAN);
            }

            // Target Type Badge
            String badge = target.type() == TargetType.MEKANISM ? "[M]" : "[W]";
            int badgeColor = target.type() == TargetType.MEKANISM ? ACCENT_CYAN : 0xFFAA55FF;
            guiGraphics.drawString(this.font, badge, listX + 2, rowY + 3, badgeColor, false);

            // Name
            int nameColor = target.type() == TargetType.MEKANISM ? (target.color() | 0xFF000000) : TEXT_WHITE;
            String trimmedName = this.font.plainSubstrByWidth(target.name(), listW - 24);
            guiGraphics.drawString(this.font, trimmedName, listX + 20, rowY + 3, nameColor, false);
        }

        // Right Panel (Details Area)
        int rightX = x + 140;
        int rightY = y + 18;
        int rightW = 100;
        int rightH = 90;
        guiGraphics.fill(rightX, rightY, rightX + rightW, rightY + rightH, PANEL_BG);
        guiGraphics.renderOutline(rightX - 1, rightY - 1, rightW + 2, rightH + 2, BORDER_COLOR);

        net.minecraft.client.Minecraft mc = this.minecraft;
        if (selectedTarget != null && mc != null && mc.player != null) {
            int cost = TeleportCostCalculator.calculateCost(mc.player, selectedTarget);
            boolean canAfford = menu.getEnergy() >= cost;

            guiGraphics.drawString(this.font, "Destination:", rightX + 4, rightY + 4, TEXT_DIM, false);
            String nameDisp = this.font.plainSubstrByWidth(selectedTarget.name(), rightW - 8);
            guiGraphics.drawString(this.font, nameDisp, rightX + 4, rightY + 14, ACCENT_CYAN, false);

            String dimDisp = selectedTarget.dimension().location().getPath();
            guiGraphics.drawString(this.font, "Dim: " + dimDisp, rightX + 4, rightY + 28, TEXT_WHITE, false);

            boolean sameDim = mc.player.level().dimension().equals(selectedTarget.dimension());
            if (sameDim) {
                double dist = Math.sqrt(mc.player.blockPosition().distSqr(selectedTarget.pos()));
                guiGraphics.drawString(this.font, String.format(Locale.ROOT, "Dist: %.0fm", dist), rightX + 4, rightY + 40, TEXT_DIM, false);
            } else {
                guiGraphics.drawString(this.font, "Dist: Interdim", rightX + 4, rightY + 40, TEXT_DIM, false);
            }

            guiGraphics.drawString(this.font, "Cost: " + cost + " FE", rightX + 4, rightY + 54, canAfford ? ACCENT_CYAN : COST_RED, false);
        } else {
            guiGraphics.drawCenteredString(this.font, "Select target", rightX + rightW / 2, rightY + 38, TEXT_DIM);
        }

        // Energy Bar Area
        int barX = x + 142;
        int barY = rightY + 68;
        int barW = rightW - 4;
        int barH = 16;
        guiGraphics.fill(barX, barY, barX + barW, barY + barH, 0xFF0E1013);
        guiGraphics.renderOutline(barX, barY, barW, barH, BORDER_COLOR);

        float energyRatio = (float) menu.getEnergy() / Math.max(1, ItemPortableTeleporterPlus.getMaxEnergy());
        int fillW = (int) (barW * Mth.clamp(energyRatio, 0.0f, 1.0f));
        if (fillW > 0) {
            guiGraphics.fill(barX + 1, barY + 1, barX + fillW - 1, barY + barH - 1, FE_GREEN);
        }

        String energyText = (menu.getEnergy() / 1000) + "k FE";
        guiGraphics.drawCenteredString(this.font, energyText, barX + barW / 2, barY + 4, TEXT_WHITE);

        // Player Inventory Slots Backgrounds
        renderSlotGrid(guiGraphics, x + 44, y + 138, 9, 3);
        renderSlotGrid(guiGraphics, x + 44, y + 196, 9, 1);
    }

    private void renderSlotGrid(GuiGraphics guiGraphics, int startX, int startY, int cols, int rows) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int sx = startX + c * 18 - 1;
                int sy = startY + r * 18 - 1;
                guiGraphics.fill(sx, sy, sx + 18, sy + 18, 0xFF101216);
                guiGraphics.renderOutline(sx, sy, 18, 18, 0xFF2A313C);
            }
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
