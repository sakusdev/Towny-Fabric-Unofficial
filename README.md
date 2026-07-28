# Fabric Towny 26.2

Towny Advancedのゲームプレイ概念をFabricサーバー向けに再実装するプロジェクトです。Bukkitコードを直接流用せず、Fabricイベント・Brigadier・JSONストレージ・BlueMap APIで構成しています。

## 現在の実装

- Resident → Town → Nation階層
- 町・国家の作成/削除、公開参加、脱退
- 町銀行、内部経済、入出金
- 住民税、プロット税、国家税、維持費、New Day処理
- 人口連動のclaim上限、隣接claim、outpost
- TownBlockと個人所有Plot
- Plot type: default/residential/commercial/arena/embassy/farm/inn/jail/wilds
- build/destroy/switch/item-use × resident/nation/ally/outsider権限モデル
- PVP・火災・爆発・Mob設定を保持するデータモデル
- Town/Nation ranks、outlaws、friends、titles/surnamesのデータモデル
- 国家同盟・敵対、戦争・占領状態のデータモデル
- 招待データと期限切れ処理
- BlueMap領地レイヤー
- 原子的JSON保存とschemaVersion
- GitHub Actionsビルド

## 主なコマンド

- `/town`, `/town new`, `/town claim [outpost]`, `/town unclaim`
- `/town join`, `/town leave`, `/town delete`
- `/town deposit`, `/town withdraw`
- `/town set taxes`, `/town set plottax`
- `/town toggle open|public|pvp|fire|explosion|mobs|taxpercent`
- `/nation`, `/nation new`, `/nation delete`
- `/plot claim`, `/plot unclaim`, `/plot set type`
- `/resident`
- `/townyadmin newday`, `/townyadmin save`

## 未完了/次段階

本家のコマンド表面すべて、招待accept/deny、町長交代、町/国家rank操作、spawn teleport、plot売買、embassy、jail、regen、完全なentity/explosion/fire/fluid/piston保護、チャットチャンネル、外部Economy/LuckPerms連携、SiegeWar互換戦争ルール、Townyデータインポート、設定ファイル化、テストを順次追加します。

## ビルド

JDK 25 / Gradle 9.5.1:

```bash
gradle build
```
