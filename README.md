# CommonModal

Spigot プラグインと Fabric クライアントのモーダル UI 関連実装を管理します。Spigot 側の使い方は [Spigot README](Spigot/README.md)、API は [API_USAGE](Spigot/API_USAGE.md) を参照してください。

## 構成と検証

- `Spigot/`: Java 21 / Maven のプラグイン。CI は `mvn -B -ntp -f Spigot/pom.xml verify` を実行します。
- `fabric/`: Fabric クライアント。CI は Java 25 と Gradle 9.8.1 を使い、同ディレクトリで `gradle --no-daemon build` を実行します。

Minecraft・Fabric の対応バージョンは [fabric.mod.json](fabric/src/main/resources/fabric.mod.json) と `fabric/gradle.properties` を確認してください。実サーバーでの動作は別途確認が必要です。

## 作業ルールと課題

- 共通の作業方針: [リポジトリの保護・更新・公開方針](https://github.com/acecore-systems/.github/blob/main/REPOSITORY_POLICY.md)。
- 共通の参加方法: [コントリビューションガイド](https://github.com/acecore-systems/.github/blob/main/CONTRIBUTING.md)。
- 未完了の課題・提案: [Issues](https://github.com/acecore-systems/CommonModal/issues)。
- 進行中の変更: [Pull Requests](https://github.com/acecore-systems/CommonModal/pulls)。
