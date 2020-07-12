# IdeaPluginStudy
idea plugin开发研究

## idea plugin基础

* [编写你的第一个plugin Hello World](./docs/base/helloWorld.md)
* [Action机制](./docs/base/action.md)
* [插件开发之Editor](./docs/base/editor.md)
* [打印日志，查看日志](./docs/base/log.md)

## Program Structure Interface (PSI)

程序结构接口（通常称为PSI）是IntelliJ平台中的一层，负责解析文件并创建语法和语义代码模型，以支持该平台的许多功能。

* [介绍](./docs/psi/introduction.md)
* [PsiAugmentProvider](./docs/psi/psiAugmentProvider.md)

## idea plugin进阶

* [IntelliJ Plugin Development introduction: PersistStateComponent](./docs/advanced/persistStateComponent.md)
* [IntelliJ Plugin Development introduction: ApplicationConfigurable, ProjectConfigurable](./docs/advanced/applicationConfigurable.md)
* [generate-tostring](./docs/advanced/generateTostring.md)

## 参考文献

[[Intellij Idea 插件 番外] Api讲解（类、方法等）](https://blog.csdn.net/guohaiyang1992/article/details/79019094)

[AndroidStudio插件开发（进阶篇之Action机制）](https://blog.csdn.net/huachao1001/article/details/53883500)

[IntelliJ Platform SDK](https://www.jetbrains.org/intellij/sdk/docs/intro/welcome.html)

[https://www.jetbrains.com/help/idea/getting-started.html](https://www.jetbrains.com/help/idea/getting-started.html)

[https://www.jetbrains.org/intellij/sdk/docs/tutorials/editor_basics.html](https://www.jetbrains.org/intellij/sdk/docs/tutorials/editor_basics.html)
