# Javathon

基于Java ASM实现Python静态转译与动态加载的跨语言交互框架。

---

### 开发
使用核心可以进行自定义的编译和加载。
```gradle
dependencies {
    implementation 'com.github.Chi-Re.Javathon:core:0.1.2'
}
```
如果你只希望加载编译后的字节码，只需要导入`lib`模块并自己编写加载器即可。
```gradle
dependencies {
    implementation 'com.github.Chi-Re.Javathon:lib:0.1.2'
}
```

---

### 构建

_Building:_ `gradlew core:dist`

构建完成后在，生成结果在`~/core/build/libs/Javathon.jar`

---

### 使用

将构建结果`Javathon.jar`文件以下面的方式运行。

如果你希望将python项目构建，那么运行：
```cmd
java -cp Javathon.jar chire.python.PyCompiler input output
```

如果你只希望运行py项目，那么运行：
```cmd
java -cp Javathon.jar chire.python.PyInterpreter input classPath
```

注意：
- `input`一定是py项目源代码根目录src。
- `output`会自动检测是否为文件或文件夹，但文件一定是jar结尾。
- `classPath`是入口路径，要求`xxx.xxx.xxx`格式，内部类请使用`$`连接(和java的Class.forName语法一致)

---

### 关于

目前还不够稳定，关于`lib`模块开放的接口将被修改。

---
