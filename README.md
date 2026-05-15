# Javathon

基于Java ASM实现Python静态转译与动态加载的跨语言交互框架。

---

### 使用
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

### 关于

目前还不够稳定，关于`lib`模块开放的接口将被修改。

---
