# commonModal Spigot プラグイン API 利用ガイド

このドキュメントは、**commonModal Spigot プラグインを依存関係に追加した開発者**向けに、
プレイヤーが commonModal Fabric Mod を導入しているかを判別する方法をまとめたものです。

## 前提

- commonModal Spigot プラグインがサーバーに導入されていること。
- 対象プレイヤーは、対応する commonModal Fabric Mod を導入したクライアントでログインしていること。
- プレイヤー参加時（`PlayerJoinEvent`）に commonModal 側が自動的に導入チェックパケットを送信します。

## 導入判定 API

`ModalCheckerAPI` を使用して、対象プレイヤーが commonModal 対応クライアントかを判定できます。

```java
import dev.acecore.common_modal.api.ModalCheckerAPI;
import org.bukkit.entity.Player;

public class YourPluginListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 即座に判定しても、まだ応答が返っていない可能性があるため注意
        if (ModalCheckerAPI.isModalPlayer(player)) {
            player.sendMessage("commonModal 対応クライアントです");
        } else {
            player.sendMessage("commonModal 非対応クライアント、または未応答です");
        }
    }
}
```

### 注意点

- `PlayerJoinEvent` 時点では、Fabric Mod からの応答がまだ返っていない場合があります。
- 確実に判定したい場合は、参加から数 tick（例: 1 秒程度）遅延させて確認するか、
  実際にフォームを送信するタイミングで `ModalCheckerAPI.isModalPlayer(player)` を使用してください。

## 使い方の例：フォーム送信前の判定

```java
import dev.acecore.common_modal.api.CommonModalAPI;
import dev.acecore.common_modal.api.ModalCheckerAPI;
import dev.acecore.common_modal.api.form.SimpleForm;
import org.bukkit.entity.Player;

public class YourCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!ModalCheckerAPI.isModalPlayer(player)) {
            player.sendMessage("commonModal Mod が必要です。");
            return true;
        }

        SimpleForm form = SimpleForm.builder()
                .title("メニュー")
                .content("項目を選んでください")
                .button("ボタン 1", null)
                .build();

        form.send(player);
        return true;
    }
}
```

## 提供されているメソッド

| メソッド | 説明 |
| --- | --- |
| `ModalCheckerAPI.isModalPlayer(Player)` | 指定プレイヤーが commonModal 導入済みかを返す |
| `ModalCheckerAPI.getModalPlayers()` | 導入済みプレイヤーの UUID を含む読み取り専用セットを返す |
| `ModalCheckerAPI.API_VERSION` | 現在の commonModal API バージョン（`1`） |

## API バージョンについて

- 現行の API バージョンは `1` です。
- Spigot 側は、`commonmodal:check_response` チャネルで返却された version が
  `ModalCheckerAPI.API_VERSION` と一致する場合のみ、プレイヤーを導入済みとして登録します。
