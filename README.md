# WORK
Last State: Nov 2021

# WORKとは何か？

## 種類
Javaで記述したAndroidアプリです。

## 内容
以下、Google Pixel 3で動作確認済みです。
・「仕事一覧画面」では、仕事の編集や保存またはグラフ表示が可能です。
・「最近使用した仕事一覧画面」では、最近使用した仕事を最初に確認できます。
・［今週、週末、来週期限の仕事一覧画面」では、期限ごとの仕事を確認できます。
・「アーカイブ済みの仕事一覧画面」では、アーカイブした仕事を確認できます。
・「ゴミ箱にある仕事一覧画面」では、削除が可能です。

# だれが、「WORK」を使うか？

Androidアプリを作成したい方が参考として使用することを想定します。

# どのように、「WORK」を使うか？

以下のapi_keyの値(value)を取得し、設定してください。

・AndroidManifest.xml
```
         <meta-data
            android:name="com.google.android.backup.api_key"
            android:value="キーを設定してください" />
```

以下のAPPLICATION_IDの値(value)を取得し、設定してください。

・AndroidManifest.xml

```
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="キーを設定してください"/>

```

・AdMobFragmentImpl.java

```
    @Override
    protected String getUnitId() {
        return "キーを設定してください";
    }
```