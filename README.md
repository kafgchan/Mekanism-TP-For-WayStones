# Mekanism: TP for Waystones (Forge 1.20.1)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.4.10+-orange.svg)](https://files.minecraftforge.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/kafgchan/Mekanism-TP-For-WayStones?include_prereleases&label=Release&color=brightgreen)](https://github.com/kafgchan/Mekanism-TP-For-WayStones/releases)

**Mekanism: TP for Waystones** は、工業化 Mod の金字塔 **Mekanism** のテレポート周波数と、拠点間・探索ワープの標準である **Waystones** の登録地点を、単一のハイテク携帯端末から統合管理・テレポート可能にする Minecraft 1.20.1 / Forge 向け連携 Mod です。

---

## 🌟 主な特徴 / Features

- 📱 **統合携帯端末「Portable Teleporter Plus」 (ポータブルテレポーター・プラス)**:
  - 洗練された専用 GUI から、Waystones の解放済みワープ地点と Mekanism の周波数テレポート先を一括一覧表示・選択テレポート可能。
- 💍 **Curios API オプション連携 & 専用スロット新設 (v1.0.2)**:
  - Curios 導入時、アクセサリーインベントリに専用スロット「**Portable Teleporter** (`portable_teleporter`)」が 1 枠追加されます。
  - 専用スロットには **Mekanism 純正 Portable Teleporter** と **Portable Teleporter Plus** の両方が共用装備可能（排他）。
  - 汎用スロット（`curio`, `charm`, `belt`, `body`）にも装備可能。
  - Curios 未導入環境でもクラッシュせず 100% 動作する完全ソフト依存。
- ⌨️ **スマートなショートカットキー起動 (デフォルト `V` キー)**:
  - 手に持っていなくても、Curios スロットやインベントリからワンキーで GUI を即座にオープン。
  - Plus 版だけでなく、Mekanism 純正 Portable Teleporter を装備・所持している場合でも純正テレポート画面を呼び出し可能。
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
- ⚡ **クイックテレポート (Quick Teleport / v1.1.0)**:
  - 事前に設定した目的地へ、GUIを開かずに「Shift + 右クリック」で即時テレポート。
  - GUI内の「設定」ボタン、または目的地のダブルクリックで簡単に設定可能。
  - 次回GUIオープン時も自動的に選択状態が維持され即テレポート可能。
- ⭐ **お気に入り機能 (Favorites / v1.1.0)**:
  - 目的地をお気に入りに登録（プレイヤー単位で永続保存）。
  - すべてのタブで最上位に金色の「★」マーク付きで優先ソート。
  - 専用タブ「★」で登録済み目的地のみを瞬時にフィルタリング可能。
- 🔋 **使い捨てテレポーター & 使い捨て電池 (Disposable Series / v1.1.0)**:
  - **使い捨てテレポーター (`disposable_teleporter`)**: 所有者登録不要・最大16個スタック可能な1回使い切り端末。公開Mekanism周波数および利用者のWaystones地点へワープ可能（成功時に1個消費）。
  - **使い捨て電池 (`disposable_battery`)**: 25,000 FE相当のエネルギーが封入された中間素材（1クラフトで16個生成・最大16個スタック可能）。
- 🎨 **Mekanism テイストの拡張 GUI (v1.1.0)**:
  - 下部インベントリ36枠を完全撤廃し、目的地一覧を **5行から8行** へ大幅拡張。
  - 縦スクロールバーを新設。
  - スコープバッジ（`Way個人`, `Way共有`, `Mek公開`, `Mek非公開`, `Mek許可`）を標準表示。
  - `ALL` / `MEK` / `WAY` / `★` の4タブ切り替え、インクリメンタル検索、詳細情報パネル、リアルタイムFEゲージ。
- 🌐 **多言語対応 (Multi-Language / i18n)**:
  - 英語 (`en_us`), 日本語 (`ja_jp`), 簡体字中国語 (`zh_cn`), 繁体字中国語・台湾 (`zh_tw`), 繁体字中国語・香港 (`zh_hk`), 韓国語 (`ko_kr`) に完全対応。バッジやボタン表記も自動ローカライズ。

---

## 🛠 クラフトレシピ / Crafting Recipe

### 1. ポータブルテレポーター・プラス (`portable_teleporter_plus`)
3×3 作業台で以下の通りクラフト可能です：

```text
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

### 2. 使い捨て電池 (`disposable_battery` / 1クラフト: 16個)
```text
[ 銅 ] [ レッドストーン ] [ 銅 ]
[ 銅 ] [エネルギータブレット] [ 銅 ]
[ 銅 ] [ レッドストーン ] [ 銅 ]
```
- 銅インゴット × 6 / レッドストーン × 2 / エネルギータブレット (Mekanism) × 1

### 3. 使い捨てテレポーター (`disposable_teleporter` / 1クラフト: 1個)
```text
[ 銅 ] [ 赤石トーチ ] [ 銅 ]
[ 銅 ] [ アメジスト ] [ 銅 ]
[ 銅 ] [ 使い捨て電池 ] [ 銅 ]
```
- 銅インゴット × 6 / レッドストーントーチ × 1 / アメジストの欠片 × 1 / 使い捨て電池 × 1

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

[integration]
    # Curios API との連携を有効化するかどうか
    enableCurios = true

    # ショートカットキーバインドによる端末GUI起動を有効化するかどうか
    enableKeybind = true

    # ショートカットキー押下時にインベントリ内も検索対象に含めるかどうか
    keybindSearchInventory = true
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
- **任意連携 Mod (オプション)**:
  - [Curios API](https://modrinth.com/mod/curios) (`5.14.1+1.20.1+`)

---

## 🔨 ビルド手順 / Building from Source

```bash
# Windows
.\gradlew.bat build

# Linux / macOS
./gradlew build
```

ビルドが完了すると、`build/libs/mekanism-tp-for-waystone-forge-1.20.1-1.1.0.jar` が生成されます。

---

## 📝 更新履歴 / Changelog

### v1.1.0

- **クイックテレポート機能**:
  - 事前に設定した目的地へ Shift+右クリックで即時テレポート。
  - リスト行のダブルクリックまたは「設定」ボタンで目的地を素早く登録。
- **お気に入り機能**:
  - 目的地をお気に入りに登録可能（プレイヤー単位で永続保存）。
  - 一覧最上位へ優先ソート＆専用「★」タブを新設。
- **GUIの大幅改修**:
  - 下部インベントリ枠を削除し、目的地一覧を5行から8行へ拡張。縦スクロールバーを新設。
  - スコープバッジ（Way個人, Way共有, Mek公開, Mek非公開, Mek許可）の表示。
- **使い捨てシリーズの追加**:
  - 「使い捨てテレポーター」（最大16個スタック・消費型ワープ端末）
  - 「使い捨て電池」（最大16個スタック・25,000 FE封入中間素材）
  - 各種新規クラフトレシピの追加。
- **多言語対応の強化 (i18n)**:
  - 全UIラベル、バッジ、詳細表示の完全言語ファイル化（日・英・簡中・繁中・香港・韓）。
- **コードベース改善**:
  - 非推奨コンストラクタ `ResourceLocation(String)` の廃止と `ResourceLocation.tryParse` への置換。

### v1.0.2

- **Curios API オプション連携**:
  - アクセサリーインベントリに専用スロット「**Portable Teleporter** (`portable_teleporter`)」を新設。
  - Mekanism 純正 Portable Teleporter と Portable Teleporter Plus の共用装備に対応。
  - 汎用スロット（`curio`, `charm`, `belt`, `body`）への装備に対応。
- **ショートカットキー起動機能**:
  - デフォルト `V` キーで Curios スロット / 手持ち / インベントリから即座に GUI 起動。
  - Mekanism 純正テレポーターのホットキー呼び出しおよびテレポート連携に対応。
- **コンフィグ追加**:
  - `enableCurios`, `enableKeybind`, `keybindSearchInventory` の切り替え設定を追加。

---

## 📄 ライセンス / License

本プロジェクトは [MIT License](LICENSE) の下で公開されています。
Copyright (c) 2024-2026 [kafgchan](https://github.com/kafgchan)

