# Fabric Towny 26.2

Towny Advancedのゲームプレイ概念を、Minecraft 26.2のFabricサーバー向けに再実装するプロジェクトです。Bukkit/Townyのコードは直接流用せず、Fabricイベント、Brigadier、JSONストレージ、BlueMap APIを使用します。

## 動作環境

- Minecraft 26.2
- Fabric Loader 0.19.3以降
- Fabric API 0.155.2+26.2
- Java 25
- BlueMapは任意

## 実装済み

- Resident → Town → Nation階層
- 町・国家の作成と削除
- 町銀行と内部経済
- 町作成費、国家作成費、claim費、outpost費
- 失敗時の自動返金
- 人口連動claim上限、隣接claim、outpost上限
- TownBlockと個人所有Plot
- Plot type: default/residential/commercial/arena/embassy/farm/inn/jail/wilds
- build/destroy/switch/item-use権限
- resident/nation/ally/outsider権限グループ
- 個別Plot権限の町権限に対する優先
- ブロック破壊、ブロック攻撃、ブロック操作、アイテム使用の保護
- PVPと同一町・同一国家のfriendly-fire保護
- 管理者バイパス
- 住民税、Plot税、国家税、維持費、New Day処理
- 招待・同盟・敵対・戦争データモデル
- BlueMap領地レイヤー
- 原子的JSON保存
- schemaVersion 3と旧データ自動補修
- 設定ファイル自動生成
- JUnit回帰テスト
- GitHub Actionsによるビルド、テスト、Dedicated Server起動試験

## 主なコマンド

- `/town`
- `/town new <name>`
- `/town claim [outpost]`
- `/town unclaim`
- `/town join <town>`
- `/town leave`
- `/town delete`
- `/town deposit <amount>`
- `/town withdraw <amount>`
- `/town set taxes <amount>`
- `/town set plottax <amount>`
- `/town toggle open|public|pvp|fire|explosion|mobs|taxpercent`
- `/nation`
- `/nation new <name>`
- `/nation delete`
- `/plot claim`
- `/plot unclaim`
- `/plot set type <type>`
- `/resident`
- `/townyadmin newday`
- `/townyadmin save`

## 設定

初回起動時に次のファイルを生成します。

```text
config/fabric-towny/config.json
config/fabric-towny/data.json
```

主な設定項目:

- `townCreationCost`
- `nationCreationCost`
- `claimCost`
- `outpostCost`
- `baseTownBlocks`
- `blocksPerResident`
- `maxOutposts`
- `inviteLifetimeMillis`
- `newDayIntervalMillis`
- `requireAdjacentClaims`
- `allowFriendlyFire`
- `wildernessBuild`

## ビルド

```bash
gradle clean build
```

生成JARは`build/libs/`に出力されます。

Dedicated Serverの開発起動:

```bash
gradle runServer
```

## 未完了

- ブロック設置専用フック
- 爆発、火災、流体、ピストンによる越境保護
- 招待accept/denyコマンド
- 町長交代とrank管理
- spawn teleport
- Plot売買、embassy、jail、regen
- チャットチャンネル
- 外部Economyと権限Mod連携
- BlueMapを導入した状態でのCI起動試験
- SiegeWar互換ルール
- 既存Townyデータのインポート

## 注意

これはTowny Advanced公式版ではありません。現時点では開発版であり、運用前にバックアップとテスト環境での検証が必要です。
