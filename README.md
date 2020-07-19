# IntellijPluginStudy
Intellij plugin开发研究

open第三方开源plugin项目时，需要在项目根目录下的*.iml文件修改一下module type为`<module type="PLUGIN_MODULE" version="4">`

## Intellij plugin基础

* [编写你的第一个plugin Hello World](./docs/base/helloWorld.md)
* [Building Plugins with Gradle](./docs/base/gradlePlugin.md)
* [Kotlin for Plugin Developers](./docs/base/kotlinPlugin.md)
* [Action机制](./docs/base/action.md)
* [插件开发之Editor](./docs/base/editor.md)
* [打印日志，查看日志](./docs/base/log.md)

## Program Structure Interface (PSI)

程序结构接口（通常称为PSI）是IntelliJ平台中的一层，负责解析文件并创建语法和语义代码模型，以支持该平台的许多功能。

* [介绍](./docs/psi/introduction.md)
* [PSI使用](./docs/psi/use.md)
* [PsiAugmentProvider](./docs/psi/psiAugmentProvider.md)
* [StructureViewExtension](./docs/psi/structureViewExtension.md)

## Intellij plugin进阶

* [IntelliJ Plugin Development introduction: PersistStateComponent](./docs/advanced/persistStateComponent.md)
* [IntelliJ Plugin Development introduction: ApplicationConfigurable, ProjectConfigurable](./docs/advanced/applicationConfigurable.md)
* [generate-tostring](./docs/advanced/generateTostring.md)
* [代码检查 Code Inspections](./docs/advanced/codeInspections.md)

## 参考文献

[[Intellij Idea 插件 番外] Api讲解（类、方法等）](https://blog.csdn.net/guohaiyang1992/article/details/79019094)

[AndroidStudio插件开发（进阶篇之Action机制）](https://blog.csdn.net/huachao1001/article/details/53883500)

[IntelliJ IDEA插件开发](https://blog.csdn.net/O4dC8OjO7ZL6/article/details/79722289)

[AS插件开发：根据特定格式的文本自动生成Java Bean文件或字段](https://blog.csdn.net/qq_27258799/article/details/79295251)

[https://www.cnblogs.com/liqiking/p/6792991.html](https://www.cnblogs.com/liqiking/p/6792991.html)

[震惊！！！编码速度提高10倍的秘诀是....](https://blog.csdn.net/y4x5M0nivSrJaY3X92c/article/details/106131947)

[https://github.com/longforus/MvpAutoCodePlus](https://github.com/longforus/MvpAutoCodePlus)

[lombok-intellij-plugin](https://github.com/mplushnikov/lombok-intellij-plugin)

[IntelliJ Platform SDK](https://www.jetbrains.org/intellij/sdk/docs/intro/welcome.html)

[https://www.jetbrains.com/help/idea/getting-started.html](https://www.jetbrains.com/help/idea/getting-started.html)

[https://www.jetbrains.org/intellij/sdk/docs/tutorials/editor_basics.html](https://www.jetbrains.org/intellij/sdk/docs/tutorials/editor_basics.html)

[http://velocity.apache.org/](http://velocity.apache.org/)
