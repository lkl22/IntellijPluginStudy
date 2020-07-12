# PSI介绍

The Program Structure Interface, commonly referred to as just PSI, is the layer in the IntelliJ Platform that is responsible for parsing files and creating the syntactic and semantic code model that powers so many of the platform’s features.
> 程序结构接口（通常称为PSI）是IntelliJ平台中的一层，负责解析文件并创建语法和语义代码模型，以支持该平台的许多功能。

* [常用对象介绍](#常用对象介绍)
  * [Virtual File](#VirtualFile)
  * [PSI File](#PSIFile)
* [参考文献](#参考文献)

## <a name="常用对象介绍">常用对象介绍</a>

> 先树立一个概念，AS里项目的一切都可以视为对象，比如整个项目，项目里的每个文件，文件里的每个方法、每行语句等等都是一个对象。我们插件SDK的开发，主要工作就是针对这一个个的对象的分析和处理

## <a name="VirtualFile">Virtual File</a>

虚拟文件类。可以当做Java开发中的File对象理解，概念比较类似

**获取方法**：
* 通过Action获取: `event.getData(PlatformDataKeys.VIRTUAL_FILE)`
* 通过本地文件路径获取: `LocalFileSystem.getInstance().findFileByIoFile()`
* 通过PSI file获取: `psiFile.getVirtualFile()`
* 通过document获取: `FileDocumentManager.getInstance().getFile()`

**用处**：

传统的文件操作方法这个对象都支持，比如获取文件内容，重命名，移动，删除等

## <a name="PSIFile">PSI File</a>

PSI系统下的文件类。

**获取方法**：

* 通过Action获取: `e.getData(LangDataKeys.PSI_FILE)`
* 通过VirtualFile获取: `PsiManager.getInstance(project).findFile()`
* 通过document获取: `PsiDocumentManager.getInstance(project).getPsiFile()`
* 通过文件中的Element元素获取: `psiElement.getContainingFile()`如果要通过名字获取，请使用 `FilenameIndex.getFilesByName(project, name, scope)`

**用处**：
作为PSI系统中的一个元素，可以使用PSI Element的各种具体方法

### 什么是PSI系统？

PSI 是 `Program Structure Interface` 的简写。从名字可以看出来它是一个接口，相当于把项目中的一切都封装了起来，比如类、方法、语句等，让他们都成为了同一个系统内的实现。封装的对象类都统一加了个前缀比如`PsiClass`、`PsiMethod`等。

### Virtual File 和 PSI File的区别？

如果学过Dom和Parse解析就很好理解了，`Virtual File`就是xml文件本身的一个抽象对象。而`PSI File`就类似于Dom下xml文件解析成的Document对象，虽然也是“文件”，但是特殊封装过的 **这个PsiFile是整个PSI系统下的文件对象，和PSI下的其他Element元素相通**

### PSI Element是什么？
`PSI Element`是PSI系统下不同类型对象的一个统称，是基类。比如之前提到的PsiMethod、PsiClass等等都是一个个具体的PsiElement实现。

## <a name="参考文献">参考文献</a>

[Android Studio Plugin 插件开发教程（二） —— 插件SDK中的常用对象介绍](https://juejin.im/post/59a3ea156fb9a024903ab1c8)
