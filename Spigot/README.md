# commonModal Spigot Plugin

commonModal は、Spigot/Paper サーバーから Mod（Fabric）側の commonModal クライアントに対してフォーム UI（Modal/Simple/Custom）を表示するための前提プラグインです。

## 前提条件

- Spigot / Paper 1.21+
- Java 21+
- クライアント側に commonModal Mod がインストールされている必要があります

## 導入方法

1. `Spigot/target/spigot-1.0.0.jar` をサーバーの `plugins/` フォルダに配置します。
2. サーバーを起動します。
3. `plugins/commonModal/` が生成され、プラグインが有効化されます。

## 外部プラグインからの使用方法

commonModal を前提プラグインとして利用する外部プラグインは、Maven 依存関係を追加するか、ビルド済み JAR を直接参照してください。

### Maven 依存関係

リポジトリに公開していない場合は、ローカル JAR を依存関係（`system` scope または `provided` scope）として追加してください。

```xml
<dependency>
    <groupId>dev.acecore</groupId>
    <artifactId>commonmodal-spigot</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/commonmodal-spigot-1.0.0.jar</systemPath>
</dependency>
```

また、`plugin.yml` に `depend: [commonModal]` を追加することを推奨します。

```yaml
name: MyPlugin
version: 1.0.0
main: com.example.myplugin.MyPlugin
api-version: '1.21'
depend: [commonModal]
```

## API 概要

主要な API エントリポイントは `dev.acecore.commonmodal.api.form.*` です。基本的な使用方法は [Cumulus / Floodgate API](https://github.com/GeyserMC/Cumulus) と同じような呼び出し方になっています。

```java
import dev.acecore.commonmodal.api.form.ModalForm;
import dev.acecore.commonmodal.api.form.SimpleForm;
import dev.acecore.commonmodal.api.form.CustomForm;
import dev.acecore.commonmodal.api.form.FormImage;
```

各フォームは `Builder` パターンで作成し、`Player#send()` のように `Form#send(Player)` で表示します。

### ModalForm（2 択）

```java
ModalForm.builder()
    .title("確認")
    .content("本当に実行しますか？")
    .button1("はい")
    .button2("いいえ")
    .resultHandler((player, result) -> {
        player.sendMessage("選択: " + (result ? "はい" : "いいえ"));
    })
    .closedResultHandler(player -> {
        player.sendMessage("ウィンドウを閉じました");
    })
    .build()
    .send(player);
```

### SimpleForm（ボタン一覧）

```java
SimpleForm.builder()
    .title("メニュー")
    .content("項目を選択してください")
    .button("設定", FormImage.Type.PATH, "textures/items/diamond")
    .button("Warp", FormImage.Type.URL, "https://example.com/icon.png")
    .resultHandler((player, index) -> {
        player.sendMessage("選択番号: " + index);
    })
    .closedResultHandler(player -> {
        player.sendMessage("キャンセルしました");
    })
    .build()
    .send(player);
```

### CustomForm（入力フォーム）

```java
CustomForm.builder()
    .title("ユーザー登録")
    .label("以下を入力してください")
    .input("名前", "Player Name", "")
    .toggle("通知を受け取る", true)
    .slider("レベル", 1, 100, 1, 10)
    .dropdown("職業", "戦士", "魔法使い", "弓使い")
    .validResultHandler((player, values) -> {
        String name = (String) values.get(0);
        boolean notify = (Boolean) values.get(1);
        double level = ((Number) values.get(2)).doubleValue();
        int jobIndex = (Integer) values.get(3);
        player.sendMessage("名前: " + name + ", 通知: " + notify + ", レベル: " + level + ", 職業番号: " + jobIndex);
    })
    .closedResultHandler(player -> {
        player.sendMessage("入力をキャンセルしました");
    })
    .build()
    .send(player);
```

## CustomForm の応答値について

`validResultHandler` に渡される `List<Object>` のインデックスは、追加したコンポーネントの順序に対応します。

| コンポーネント | 型 |
|---|---|
| `LabelComponent` | `null` |
| `InputComponent` | `String` |
| `ToggleComponent` | `Boolean` |
| `SliderComponent` | `Double` / `Integer` / `Long`（値が整数の場合 `Integer`） |
| `DropdownComponent` | `Integer`（選択されたインデックス） |

## 注意事項

- クライアント側に commonModal Mod がインストールされていない場合、フォームは表示されません。必要に応じて事前にチェックしてください。
- 同じプレイヤーに対して連続してフォームを送信した場合、前のフォームが閉じられる可能性があります。応答はそれぞれのハンドラで受け取ります。
- 通信には `commonmodal:form`（サーバー → クライアント）と `commonmodal:response`（クライアント → サーバー）の Plugin Message チャンネルを使用します。

## ライセンス

MIT License
