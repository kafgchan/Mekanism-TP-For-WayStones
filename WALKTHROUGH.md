# 📖 Mekanism: TP for Waystones - 取扱説明書 & 技術仕様書 (Walkthrough)

---

## 1. プロジェクト概要

| 項目 | 内容 |
| :--- | :--- |
| **Mod 名称** | Mekanism: TP for Waystones |
| **内部 Mod ID** | `mekanism_waystones` |
| **対応環境** | Minecraft 1.21.1 / NeoForge 21.1.259 |
| **前提 Mod** | Mekanism 1.21.1-10.7.19.85<br>Waystones 21.1.7+neoforge-1.21.1<br>Balm 21.0.21+neoforge-1.21.1 |
| **追加アイテム** | ポータブルテレポーター・プラス (`portable_teleporter_plus`) |

> [!NOTE]
> 本 Mod は、工業化 Mod の代表格である **Mekanism** のテレポート周波数と、探索・拠点間移動の標準である **Waystones** のワープ地点を単一の高機能携帯端末からシームレスに操作・テレポート可能にする連携 Mod です。

---

## 2. アイテム仕様

### 2-1. 所有者管理システム (Ownership)
- **初回登録**: 未登録の状態で右クリック使用すると、使用したプレイヤーの UUID とプレイヤー名が `TeleporterOwner` コンポーネントとしてアイテムデータに永続記録されます。
- **セキュリティ保護**: 登録された所有者以外のプレイヤーが右クリックした場合、「アクセス拒否: この端末は [所有者名] の所有物です。」と赤字で通知され、GUI は開きません。
- **ツールチップ**: アイテムにカーソルを合わせると、水色で所有者名が表示されます（未登録時は「未登録」表示）。

### 2-2. エネルギー管理システム (Energy Storage)
- **最大容量**: **800,000 FE (800 kFE)**
- **最大受給速度**: 800,000 FE/t
- **互換性**: NeoForge 標準の `Capabilities.EnergyStorage.ITEM` をアタッチしているため、Mekanism の充電器（Chargepad、Energy Cube、各種発電機）や他 Mod の FE 対応充電設備でそのまま給電可能です。
- **耐久バー表示**: アイテムアイコン下部の耐久バーが、Mekanism シアンカラー (`#00E5FF`) のエネルギー残量ゲージとしてリアルタイムに機能します。

### 2-3. テレポート消費コスト計算ロジック
| 転送パターン | 消費 FE 計算式 | 備考 |
| :--- | :--- | :--- |
| **同一ディメンション内** | $\lceil \text{3次元直線距離 [block]} / 1000 \rceil\text{ FE}$ | 固定基本料金 0 FE。<br>1000mまでは 1 FE、1001m〜2000mは 2 FE。 |
| **異なるディメンション間** | **一律 500 FE** | オーバーワールド ⇔ ネザー ⇔ エンド間など。 |

> [!IMPORTANT]
> **エネルギー消費の確定タイミング**:
> テレポートボタン押下時ではなく、サーバー側で目的地へのエンティティ移動が**成功した瞬間のみ確定消費**されます。障害物による失敗やイベントキャンセル時はエネルギーが一切消費されません。

---

## 3. 操作方法 & GUI ガイド

### 3-1. 画面の開き方
所有者として登録された状態で、メインハンドまたはオフハンドに持って右クリックすると専用 GUI が開きます。

### 3-2. GUI レイアウトと各機能
画面は Mekanism 風のハイテック＆スレートダークなプロシージャルデザイン（枠線シアンカラー）で構成されています。

```
+-------------------------------------------------------------+
| [Portable Teleporter Plus]                                  |
| [ALL] [MEK] [WAY]            | Destination: Home Base       |
| [ Search...                ] | Dim: minecraft:overworld     |
| +--------------------------+ | Dist: 1,420m                 |
| | [W] Home Base     2 FE   | | Cost: 2 FE (Cyan)            |
| | [M] Mining Quarry 3 FE   | +------------------------------+
| | [W] Village West  1 FE   | | [ 350k / 800k FE ] (Bar)     |
| | [M] Nether Outpost 500 FE| +------------------------------+
| +--------------------------+ | [      TELEPORT      ] (Btn) |
|                                                             |
| Player Inventory (36 Slots)                                 |
+-------------------------------------------------------------+
```

1. **タブ絞り込み (`ALL` / `MEK` / `WAY`)**:
   - `ALL`: 全ての転送先を表示。
   - `MEK`: Mekanism の周波数テレポート先のみ表示。
   - `WAY`: Waystones の解放済み地点のみ表示。
2. **インクリメンタル検索ボックス**:
   - 文字を入力すると、地点名や周波数名をリアルタイムに部分一致検索。
3. **転送先リスト（スクロール対応）**:
   - マウスホイールによる上下スクロール対応。
   - 種別バッジ（`[M]` シアン / `[W]` パープル）および周波数固有カラーで表示。
   - クリックして目的地を選択。
4. **詳細情報パネル**:
   - 選択された地点名、ディメンション、距離、必要消費 FE を表示。
   - エネルギーが不足している場合、コスト表示が赤色に変化。
5. **エネルギーゲージ**:
   - 現在端末に蓄えられているエネルギー残量をシアンのプログレスバーと数値で表示。
6. **Teleport ボタン**:
   - 目的地が未選択、またはエネルギーが不足している場合は自動で**無効化（Disabled）**。
   - 条件を満たすとアクティブになり、クリックするとサーバーへ要求パケットを送信しテレポートを実行。

---

## 4. クラフトレシピ

### 4-1. 3×3 作業台レシピ
- **クラフト結果**: `mekanism_waystones:portable_teleporter_plus` × 1

```
+------------------+------------------+------------------+
|   HDPE Pellet    |  Redstone Torch  |   HDPE Pellet    |
| (Mekanism)       | (Minecraft)      | (Mekanism)       |
+------------------+------------------+------------------+
|  Energy Tablet   |Teleportation Core|  Energy Tablet   |
| (Mekanism)       | (Mekanism)       | (Mekanism)       |
+------------------+------------------+------------------+
|   HDPE Pellet    |    Warp Stone    |   HDPE Pellet    |
| (Mekanism)       | (Waystones)      | (Mekanism)       |
+------------------+------------------+------------------+
```

### 4-2. 必要素材一覧
| アイテム名 | 内部 ID | 数量 | 配布元 Mod |
| :--- | :--- | :---: | :--- |
| **HDPE Pellet** | `mekanism:hdpe_pellet` | 4 | Mekanism |
| **Redstone Torch** | `minecraft:redstone_torch` | 1 | Vanilla Minecraft |
| **Energy Tablet** | `mekanism:energy_tablet` | 2 | Mekanism |
| **Teleportation Core** | `mekanism:teleportation_core` | 1 | Mekanism |
| **Warp Stone** | `waystones:warp_stone` | 1 | Waystones |

### 4-3. レシピ解放条件 (Advancement)
プレイヤーが `mekanism:teleportation_core` または `waystones:warp_stone` をインベントリに入手した時点で、レシピブックに自動的にクラフトレシピが登録されます。

---

## 5. プロジェクト・アーキテクチャ

```
src/main/java/net/kafgchan/mekanism_waystones/
├── MekanismTPforWaystones.java         # メイン Mod クラス (共通セットアップ・Capability)
├── MekanismTPforWaystonesClient.java   # クライアント Mod クラス (Screen 登録)
├── item/
│   ├── ItemPortableTeleporterPlus.java # アイテム本体 (使用判定・エネルギーバー・ツールチップ)
│   └── TeleporterOwner.java            # 所有者データレコード (Codec / StreamCodec)
├── registration/
│   ├── ModItems.java                   # アイテム登録 (DeferredRegister)
│   ├── ModCreativeTabs.java            # クリエイティブタブ登録
│   ├── ModDataComponents.java          # OWNER / ENERGY コンポーネント登録
│   └── ModMenuTypes.java               # メニューコンテナ登録
├── menu/
│   └── PortableTeleporterPlusMenu.java # コンテナメニュー (スロット配置・同期データ保持)
├── client/gui/
│   └── PortableTeleporterPlusScreen.java # GUI スクリーン (タブ・検索・リスト・ゲージ・ボタン)
├── network/
│   ├── PacketTeleportRequest.java      # C2S テレポート要求パケット
│   └── ModPayloads.java                # NeoForge パケット登録ハンドラ
├── target/
│   ├── TargetType.java                 # MEKANISM / WAYSTONE 列挙型
│   ├── TeleportTarget.java             # 転送先統一モデルレコード
│   ├── TeleportDestinationService.java # 地点収集サービス (Waystones/Mekanism API)
│   └── TeleportExecutionService.java   # サーバー側転送確定・FE確定消費サービス
└── util/
    └── TeleportCostCalculator.java     # 距離・ディメンション間消費 FE 計算器
```

---

## 6. 今後の移植・展開ロードマップ

楓さんとの初期合意に基づき、今回完成した **Minecraft 1.21.1 / NeoForge** 版をリファレンス実装として、他環境への移植を検討可能です：

1. **Minecraft 1.21.1 / Forge**:
   - 1.21.1 における Forge の Mekanism / Waystones 配布状況を調査し、API 差異（Network、DataComponent、Capabilities）を吸収して移植。
2. **Minecraft 1.20.1 / NeoForge & Forge**:
   - 1.20.1 では Data Components が存在しないため、`ItemStack.getTag()` (NBT) への置き換え。
   - ネットワークが `SimpleChannel` 方式になるため、パケットハンドラの適合。
