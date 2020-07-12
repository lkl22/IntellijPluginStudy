# PSI介绍

The Program Structure Interface, commonly referred to as just PSI, is the layer in the IntelliJ Platform that is responsible for parsing files and creating the syntactic and semantic code model that powers so many of the platform’s features.
> 程序结构接口（通常称为PSI）是IntelliJ平台中的一层，负责解析文件并创建语法和语义代码模型，以支持该平台的许多功能。

* [常用对象介绍](#常用对象介绍)
  * [Virtual File](#VirtualFile)
  * [PSI File](#PSIFile)
  * [File View Providers](#FileViewProviders)
  * [PSI Elements](#PSIElements)
* [参考文献](#参考文献)

## <a name="常用对象介绍">常用对象介绍</a>

> 先树立一个概念，AS里项目的一切都可以视为对象，比如整个项目，项目里的每个文件，文件里的每个方法、每行语句等等都是一个对象。我们插件SDK的开发，主要工作就是针对这一个个的对象的分析和处理

### <a name="VirtualFile">Virtual File</a>

虚拟文件类。可以当做Java开发中的File对象理解，概念比较类似

**获取方法**：
* 通过Action获取: `event.getData(PlatformDataKeys.VIRTUAL_FILE)`
* 通过本地文件路径获取: `LocalFileSystem.getInstance().findFileByIoFile()`
* 通过PSI file获取: `psiFile.getVirtualFile()`
* 通过document获取: `FileDocumentManager.getInstance().getFile()`

**用处**：

传统的文件操作方法这个对象都支持，比如获取文件内容，重命名，移动，删除等

### <a name="PSIFile">PSI File</a>

PSI系统下的文件类。

> PsiFile类是所有PSI文件的通用基类，而特定语言的文件通常由其子类表示。 例如，`PsiJavaFile`类表示一个Java文件，而`XmlFile`类表示一个XML文件。

修改操作是在单个PSI元素而非整个文件上执行的。要遍历文件中的元素，请使用`psiFile.accept(new PsiRecursiveElementWalkingVisitor()...);`

由于PSI与语言有关，因此使用`LanguageParserDefinitions.INSTANCE.forLanguage(language).createFile(fileViewProvider)` 方法通过`Language`对象创建PSI文件。像`documents`一样，当访问特定文件的PSI时，可以根据需要创建PSI文件。

**获取方法**：

* 通过Action获取: `e.getData(LangDataKeys.PSI_FILE)`
* 通过VirtualFile获取: `PsiManager.getInstance(project).findFile()`
* 通过document获取: `PsiDocumentManager.getInstance(project).getPsiFile()`
* 通过文件中的Element元素获取: `psiElement.getContainingFile()`
* 通过名字获取，请使用 `FilenameIndex.getFilesByName(project, name, scope)`

**用处**：
作为PSI系统中的一个元素，可以使用PSI Element的各种具体方法

#### 什么是PSI系统？

PSI 是 `Program Structure Interface` 的简写。从名字可以看出来它是一个接口，相当于把项目中的一切都封装了起来，比如类、方法、语句等，让他们都成为了同一个系统内的实现。封装的对象类都统一加了个前缀比如`PsiClass`、`PsiMethod`等。

像`documents`一样，从相应的`VirtualFile`实例中弱引用PSI文件，如果没有任何人引用，则可以对其进行垃圾回收。

#### Virtual File 和 PSI File的区别？

如果学过Dom和Parse解析就很好理解了，`Virtual File`就是xml文件本身的一个抽象对象。而`PSI File`就类似于Dom下xml文件解析成的Document对象，虽然也是“文件”，但是特殊封装过的 **这个PsiFile是整个PSI系统下的文件对象，和PSI下的其他Element元素相通**

与具有应用程序范围的`VirtualFile`和`Document`不同（即使打开了多个项目，每个文件也由相同的`VirtualFile`实例表示），PSI具有项目scope（如果该文件属于同时打开的多个项目，则该文件由多个PsiFile实例表示）。

#### How do I create a PSI file?

[PsiFileFactory](https://upsource.jetbrains.com/idea-ce/file/idea-ce-40e5005d02df57f58ac2d498867446c43d61101f/platform/core-api/src/com/intellij/psi/PsiFileFactory.java) `createFileFromText()`方法创建具有指定内容的内存中PSI文件。

要将PSI文件保存到磁盘，请使用[PsiDirectory](https://upsource.jetbrains.com/idea-ce/file/idea-ce-40e5005d02df57f58ac2d498867446c43d61101f/platform/core-api/src/com/intellij/psi/PsiDirectory.java) `add()`方法。

#### How do I get notified when PSI files change?

`PsiManager.getInstance(project).addPsiTreeChangeListener()` 允许您接收有关项目的PSI树的所有更改的通知。

#### How do I extend PSI?

通过自定义语言插件，可以扩展PSI以支持其他语言。 有关开发自定义语言插件的更多详细信息，请参见[《自定义语言支持》](https://www.jetbrains.org/intellij/sdk/docs/reference_guide/custom_language_support.html)参考指南。

#### What are the rules for working with PSI?

对PSI文件内容所做的任何更改都会反映在documents中，因此[所有处理documents的规则](https://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/documents.html#what-are-the-rules-of-working-with-documents)（读/写操作，命令，只读状态处理）都将生效。

### <a name="FileViewProviders">File View Providers</a>

`A file view provider (FileViewProvider)`管理对单个文件中多个PSI树的访问。

例如，JSPX页面在其中具有用于Java代码的单独的PSI树`（PsiJavaFile）`，用于XML代码的单独的树`（XmlFile）`和用于整体JSP的单独的树`（JspFile）`。

每个PSI树覆盖文件的全部内容，并且在可以找到其他语言内容的地方包含特殊的“**外部语言元素**”。

`FileViewProvider`实例对应于单个`VirtualFile`，单个`Document`，并可用于检索多个`PsiFile`实例。

**获取方法**：
* From a `VirtualFile`: `PsiManager.getInstance(project).findViewProvider()`
* From a `PsiFile`: `psiFile.getViewProvider()`

**用处**：
* 要获取文件中存在PSI树的所有语言的集合，请执行以下操作：`fileViewProvider.getLanguages()`
* 要获取特定语言的PSI树：`fileViewProvider.getPsi(language)`。 例如，要获取XML的PSI树，请使用`fileViewProvider.getPsi(XMLLanguage.INSTANCE)`。
* 要在文件中的指定偏移处查找特定语言的元素，请执行以下操作：`fileViewProvider.findElementAt(offset, language)`

**扩展FileViewProvider**：

要创建具有用于不同语言的多个穿插树的文件类型，插件必须包含extension: `com.intellij.fileType.fileViewProviderProvider` extension point。

实现`FileViewProviderFactory`并从`createFileViewProvider()`方法返回您的`FileViewProvider`实现。

在plugin.xml中注册如下：
```xml
<extensions defaultExtensionNs="com.intellij">
  <fileType.fileViewProviderFactory filetype="%file_type%" implementationClass="com.plugin.MyFileViewProviderFactory" />
</extensions>
```
其中`％file_type％`是指要创建的文件的类型（例如，`“JFS”`）。

### <a name="PSIElements">PSI Elements</a>

PSI（程序结构接口）文件表示PSI元素的层次结构（所谓的PSI树）。 单个PSI文件（本身就是PSI元素）可能包含特定编程语言中的多个PSI树。 而PSI元素可以具有子PSI元素。

PSI元素和单个PSI元素级别的操作用于探究IntelliJ平台解释的源代码的内部结构。 例如，您可以使用PSI元素执行代码分析，例如代码检查或意图动作。

`PsiElement`类是PSI元素的通用基类。`PSI Element`是PSI系统下不同类型对象的一个统称，是基类。比如之前提到的PsiMethod、PsiClass等等都是一个个具体的PsiElement实现。

**获取方法**：
* 通过Action获取: `e.getData(LangDataKeys.PSI_ELEMENT)`，注意：如果当前打开了一个编辑器，并且插入光标下的元素是引用，则将返回解析该引用的结果。 这可能是您需要的，也可能不是。
* 通过文件偏移量获取: `PsiFile.findElementAt()`，注意：这将返回指定偏移量处的最低级别元素（“leaf”），该偏移量通常是词法分析器标记。 您很可能应该使用`PsiTreeUtil.getParentOfType()`来查找您真正需要的元素。
* 通过遍历PSI文件获取: [`PsiRecursiveElementWalkingVisitor`](https://upsource.jetbrains.com/idea-ce/file/idea-ce-40e5005d02df57f58ac2d498867446c43d61101f/platform/core-api/src/com/intellij/psi/PsiRecursiveElementWalkingVisitor.java)
* 通过解析引用获取: `PsiReference.resolve()`


## <a name="参考文献">参考文献</a>

[Android Studio Plugin 插件开发教程（二） —— 插件SDK中的常用对象介绍](https://juejin.im/post/59a3ea156fb9a024903ab1c8)
