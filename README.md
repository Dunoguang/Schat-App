# RootWebViewDemo

Android 8.1+（API 27）单机 WebView Demo：

- WebView 加载 `assets/index.html`
- JS 暴露全局函数 `root_cmd(mycmd)`
- 原生使用 `su -c` 执行命令
- 返回数组：`[stdout, stderr, statusCode]`
- 默认 Demo 执行 `whoami`

## 关键文件
- `app/src/main/java/com/example/rootwebviewdemo/MainActivity.java`
- `app/src/main/java/com/example/rootwebviewdemo/JsBridge.java`
- `app/src/main/java/com/example/rootwebviewdemo/RootShellExecutor.java`
- `app/src/main/assets/index.html`
- `.github/workflows/android-pr-build.yml`

## 构建与产物位置
本仓库 CI 在 PR 上执行：

```bash
gradle :app:assembleRelease
```

构建成功后的 APK 默认产物路径：

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

如果你在 Android Studio 点 **Run**：
- 主要是安装到设备/模拟器，不一定会把 APK 自动展示给你。
- 可以在 `Build > Build Bundle(s) / APK(s) > Build APK(s)` 后，在提示里点 `locate`。

GitHub Actions 会自动上传 APK 工件（artifact）为 `app-release-apk`，可在对应 PR 的 Actions 运行页下载。

> 说明：命令执行依赖设备 ROOT 授权；未 ROOT 设备会返回错误信息与非 0 状态码。
