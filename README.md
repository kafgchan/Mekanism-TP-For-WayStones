# Mekanism: TP for Waystones

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.259-orange.svg)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Mekanism: TP for Waystones** は、工業化 Mod の代表格である **Mekanism** のテレポート周波数と、探索・拠点間移動の標準である **Waystones** のワープ地点を単一の高機能携帯端末からシームレスに操作・テレポート可能にする Minecraft 1.21.1 / NeoForge 向け連携 Mod です。

---

## 🌟 主な特徴

- 📱 **統合携帯端末「Portable Teleporter Plus」**:
  - 単一の洗練された GUI から、Waystones の解放済み地点と Mekanism の周波数テレポート先を一括管理。
- 🔒 **所有者管理システム (Ownership Security)**:
  - 初回使用者の UUID をアイテムに永続記録。
  - 所有者以外のプレイヤーによる操作を安全にブロック。
- ⚡ **エネルギー管理システム (FE / Forge Energy)**:
  - 基本容量 **800,000 FE (800 kFE)**。
  - Mekanism の充電設備（Chargepad や Energy Cube 等）でそのまま充電互換。
  - シアンカラー（Mekanism Cyan）のリアルタイム残量バー表示。
- 📏 **スマートなテレポートコスト計算**:
  - 同一ディメンション内: $\lceil \text{直線距離}/1000 \rceil\text{ FE}$（固定基本料金 0 FE）。
  - 異なるディメンション間: 一律 500 FE。
  - **安全確定消費**: 転送が成功した瞬間のみエネルギーを確定消費。
- 🎨 **Mekanism 調のハイテック GUI**:
  - `ALL` / `MEK` / `WAY` の 3 タブ切り替え。
  - 地点名・周波数名のインクリメンタル検索。
  - スクロールリスト、詳細情報パネル、エネルギーゲージ、Teleport ボタン。

---

## 🛠 クラフトレシピ

3×3 作業台で以下の通りクラフト可能です：

```
[ HDPE Pellet ]   [ Redstone Torch ]       [ HDPE Pellet ]
[ Energy Tablet ] [ Teleportation Core ]   [ Energy Tablet ]
[ HDPE Pellet ]   [ Warp Stone (Waystones)] [ HDPE Pellet ]
```

| 材料 | 内部 ID | 数量 | 所属 Mod |
| :--- | :--- | :---: | :--- |
| **HDPE Pellet** | `mekanism:hdpe_pellet` | 4 | Mekanism |
| **Redstone Torch** | `minecraft:redstone_torch` | 1 | Vanilla Minecraft |
| **Energy Tablet** | `mekanism:energy_tablet` | 2 | Mekanism |
| **Teleportation Core** | `mekanism:teleportation_core` | 1 | Mekanism |
| **Warp Stone** | `waystones:warp_stone` | 1 | Waystones |

> `Teleportation Core` または `Warp Stone` を入手すると、レシピブックに自動的にレシピが登録されます。

---

## 📋 動作要件

- **Minecraft**: 1.21.1
- **Mod Loader**: NeoForge 21.1.259+
- **Java**: 21
- **必須前提 Mod**:
  - [Mekanism](https://modrinth.com/mod/mekanism) (1.21.1-10.7.19.85+)
  - [Waystones](https://modrinth.com/mod/waystones) (21.1.7+neoforge-1.21.1+)
  - [Balm](https://modrinth.com/mod/balm) (21.0.21+neoforge-1.21.1+)

---

## 🔨 ビルド方法

```bash
# リポジトリのクローン
git clone https://github.com/kafgchan/Mekanism-TP-For-WayStones.git
cd Mekanism-TP-For-WayStones

# ビルドの実行
./gradlew build
```

ビルド完了後、`build/libs/` 配下に Mod JAR ファイルが生成されます。

---

## 📖 ライセンス

MIT License
