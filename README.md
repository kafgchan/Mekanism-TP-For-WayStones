# Mekanism: TP for Waystones (Forge 1.20.1)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.4.10+-orange.svg)](https://files.minecraftforge.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/kafgchan/Mekanism-TP-For-WayStones?include_prereleases&label=Release&color=brightgreen)](https://github.com/kafgchan/Mekanism-TP-For-WayStones/releases)

**Mekanism: TP for Waystones** は、工業化 Mod の金字塔 **Mekanism** のテレポート周波数と、拠点間・探索ワープの標準である **Waystones** の登録地点を、単一のハイテク携帯端末から統合管理・テレポート可能にする Minecraft 1.20.1 / Forge 向け公式連携 Mod です。

---

## 🌟 主な特徴 / Features

- 📱 **統合携帯端末「Portable Teleporter Plus」 (ポータブルテレポーター・プラス)**:
  - 洗練された専用 GUI から、Waystones の解放済みワープ地点と Mekanism の周波数テレポート先を一括一覧表示・選択テレポート可能。
- 🔒 **所有者保護システム (Ownership Security)**:
  - 初回使用時にプレイヤーの UUID と名前をアイテムへ永続登録。
  - 所有者以外のプレイヤーによる不正な操作・アクセスを自動防御。
- ⚡ **FE エネルギー管理 & Mekanism 完全充電互換**:
  - Forge Energy (FE) 対応。基本容量 **800,000 FE (800 kFE)**。
  - Mekanism の充電設備（Chargepad や Energy Cube 等）でそのまま急速充電が可能。
  - 耐久ゲージには視認性の高い FE グリーン (`#38D648`) カラーを採用。
- 📏 **スマートなコスト計算 & 安全テレポート**:
  - 同一ディメンション内: $\lceil \text{直線距離} / 1000 \rceil\text{ FE}$（基本 0 FE）。
  - 異なるディメンション間: 一律 500 FE。
  - **二重検証**: 着地点の窒息・奈落・溶岩・チャンクロード状態をサーバー側で厳密に検証し、安全が確認された場合のみエネルギーを確定消費。
- 🎨 **Mekanism テイストの多機能 GUI**:
  - `ALL`（すべて） / `MEK`（Mekanism 周波数） / `WAY`（Waystones 地点）の 3 タブ切り替え。
  - インクリメンタル文字列検索フィルター。
  - スムーズスクロールリスト、詳細情報表示（ディメンション・直線距離・消費 FE）、リアルタイム FE バー。
- 🌐 **多言語対応 (Multi-Language / i18n)**:
  - 英語 (`en_us`), 日本語 (`ja_jp`), 簡体字中国語 (`zh_cn`), 繁体字中国語・台湾 (`zh_tw`), 繁体字中国語・香港 (`zh_hk`), 韓国語 (`ko_kr`) に完全対応。

---

## 🛠 クラフトレシピ / Crafting Recipe

3×3 作業台で以下の通りクラフト可能です：

```
[ HDPE Pellet ]   [ Redstone Torch ]        [ HDPE Pellet ]
[ Energy Tablet ] [ Teleportation Core ]    [ Energy Tablet ]
[ HDPE Pellet ]   [ Warp Stone (Waystones)] [ HDPE Pellet ]
```

| スロット | アイテム名 | 内部 ID | 数量 | 所属 Mod |
| :--- | :--- | :--- | :---: | :--- |
| **四隅** | HDPE Pellet | `mekanism:hdpe_pellet` | 4 | Mekanism |
| **上中央** | Redstone Torch | `minecraft:redstone_torch` | 1 | Vanilla Minecraft |
| **左右中央** | Energy Tablet | `mekanism:energy_tablet` | 2 | Mekanism |
| **中央** | Teleportation Core | `mekanism:teleportation_core` | 1 | Mekanism |
| **下中央** | Warp Stone | `waystones:warp_stone` | 1 | Waystones |

> `Teleportation Core` または `Warp Stone` を入手すると、レシピブック（Advancement）に自動解放されます。

---

## ⚙️ 設定 / Configuration

Mod の各種数値は `config/mekanism_waystones-common.toml` から自由にカスタマイズ可能です。

```toml
[energy]
    # 最大エネルギー容量 (FE)
    # 範囲: 1,000 ~ 100,000,000, デフォルト: 800000
    maxEnergy = 800000

    # 1tickあたりの最大受給・転送レート (FE/t)
    # 範囲: 100 ~ 100,000,000, デフォルト: 800000
    maxTransfer = 800000

[cost]
    # 異次元間テレポートに必要なエネルギー (FE)
    # 範囲: 0 ~ 10,000,000, デフォルト: 500
    crossDimensionCost = 500

    # 1 FE あたりに移動できる直線ブロック距離
    # 範囲: 0.1 ~ 100,000.0, デフォルト: 1000.0
    distancePerFe = 1000.0

    # 同一ディメンション内の基本固定消費エネルギー (FE)
    # 範囲: 0 ~ 1,000,000, デフォルト: 0
    baseCost = 0

    # 1回のテレポートでの最大消費エネルギー上限 (FE)
    # 範囲: 0 ~ 100,000,000, デフォルト: 800000
    maxCost = 800000
```

---

## 📋 動作要件 / Requirements

- **Minecraft**: `1.20.1`
- **Mod Loader**: `Forge 47.4.10+`
- **Java**: `17`
- **前提 Mod**:
  - [Mekanism](https://modrinth.com/mod/mekanism) (`1.20.1-10.4.16.80+`)
  - [Waystones](https://modrinth.com/mod/waystones) (`14.1.21+1.20.1+`)
  - [Balm](https://modrinth.com/mod/balm) (`7.3.44+1.20.1+`)

---

## 🔨 ビルド手順 / Building from Source

```bash
# Windows
.\gradlew.bat build

# Linux / macOS
./gradlew build
```

ビルドが完了すると、`build/libs/` フォルダ配下に Mod の JAR ファイルが出力されます。
