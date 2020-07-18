# PSI使用

* [导航PSI](#导航PSI)
  * [Top-Down Navigation](#Top-DownNavigation)
  * [Bottom-Up Navigation](#Bottom-UpNavigation)
  * [PSI References](#PSIReferences)
    * [Contributed References](#ContributedReferences)
    * [References with Optional or Multiple Resolve Results](#ReferenceswithOptionalMultipleResolveResults)
    * [Searching for References](#SearchingforReferences)
* [Modifying the PSI](#ModifyingthePSI)
  * [Creating the New PSI](#CreatingtheNewPSI)
  * [维护树结构一致性](#维护树结构一致性)
  * [Whitespaces and Imports](#WhitespacesandImports)
  * [结合PSI和文档修改](#结合PSI和文档修改)
* [参考文献](#参考文献)

## <a name="导航PSI">导航PSI</a>
导航PSI的主要方法有三种：**自顶向下，自底向上和使用引用**。
* 在第一种情况下，您有一个PSI文件或另一个更高级别的元素（例如，一个方法），并且需要查找与指定条件匹配的所有元素（例如，所有变量声明）。
* 在第二种情况下，您在PSI树中有一个特定的点（例如，插入符号处的元素），并且需要查找有关其上下文的某些信息（例如，已在其中声明它的元素）。
* 最后，引用允许您从元素的使用（例如方法调用）导航到声明（被调用的方法）并返回。 参考在单独的主题中描述。

### <a name="Top-DownNavigation">Top-Down Navigation</a>

执行自顶向下导航的最常见方法是使用visitor。 要使用visitor，您可以创建一个类（通常是一个匿名内部类）来扩展visitor基类，覆盖处理您感兴趣元素的方法，并将visitor实例传递给`PsiElement.accept()`。

visitors的基类是特定于语言的。 例如，如果您需要处理Java文件中的元素，则可以扩展`JavaRecursiveElementVisitor`并override您感兴趣的Java元素类型相对应的方法。

以下代码段显示了使用visitor查找所有Java局部变量声明的过程：
```java
file.accept(new JavaRecursiveElementVisitor() {
  @Override
  public void visitLocalVariable(PsiLocalVariable variable) {
    super.visitLocalVariable(variable);
    System.out.println("Found a variable at offset " + variable.getTextRange().getStartOffset());
  }
});
```

在许多情况下，您还可以使用更特定的API进行自顶向下的导航。 例如，如果需要获取Java类中所有方法的列表，则可以使用visitor来实现，但是更简单的方法是调用`PsiClass.getMethods()`。

[PsiTreeUtil]([PsiTreeUtil.java](https://upsource.jetbrains.com/idea-ce/file/idea-ce-40e5005d02df57f58ac2d498867446c43d61101f/platform/core-api/src/com/intellij/psi/util/PsiTreeUtil.java))包含许多用于PSI树导航的通用，独立于语言的功能，其中一些功能（例如`findChildrenOfType()`）执行自上而下的导航。

### <a name="Bottom-UpNavigation">Bottom-Up Navigation</a>

自下而上导航的起点是PSI树中的特定元素（例如，解析reference的结果）或偏移量。 如果有偏移，则可以通过调用 `PsiFile.findElementAt()` 找到相应的PSI元素。 此方法返回树最低层的元素（例如，**标识符**），如果要确定更广泛的上下文，则需要向上导航树。

在大多数情况下，通过调用 `PsiTreeUtil.getParentOfType()` 执行自底向上导航。 此方法沿树前进，直到找到您指定的类型的元素。 例如，要查找包含方法，请调用 `PsiTreeUtil.getParentOfType(element，PsiMethod.class)`。

在某些情况下，您还可以使用特定的导航方法。 例如，要查找包含方法的类，请调用 `PsiMethod.getContainingClass()`。

以下代码段显示了如何将这些调用一起使用：
```java
PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
PsiElement element = psiFile.findElementAt(offset);
PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
PsiClass containingClass = containingMethod.getContainingClass();
```

要查看navigation在实际中是如何工作的，请参考[代码示例](https://github.com/JetBrains/intellij-sdk-docs/blob/master/code_samples/psi_demo/src/main/java/org/intellij/sdk/psi/PsiNavigationDemoAction.java)

### <a name="PSIReferences">PSI References</a>

PSI树中的引用是一个对象，表示从代码中某个元素的用法到相应声明的链接。 解析引用意味着找到特定用法所引用的声明。

引用的最常见类型是由语言语义定义的。 例如，考虑一个简单的Java方法：
```java
public void hello(String message) {
    System.out.println(message);
}
```
这个简单的代码片段包含五个引用。 可以将标识符`String`，`System`，`out`和`println`创建的引用解析为JDK中的相应声明：`String`和`System`类，`out`字段和`println`方法。 由`println(message)`中第二次出现的 `message` 标识符创建的引用可以解析为`message`参数，该参数由方法header中的`String message`声明。

> 请注意，字符串`message`不是引用，无法解析，而是声明。 它没有引用在其他地方定义的任何名称； 相反，它自己定义了一个名称。

引用是实现 `PsiReference` 接口的类的实例。 请注意，引用与PSI元素不同。 由PSI元素创建的引用从 `PsiElement.getReferences()` 返回，该引用的基础PSI元素可以从 `PsiReference.getElement()` 获得。

要resolve引用（以找到要引用的声明），请调用 `PsiReference.resolve()`。 了解 `PsiReference.getElement()` 和 `PsiReference.resolve()` 之间的区别非常重要。 **前一种方法返回引用的源，而后者返回其目标**。 在上面的示例中，对于`message`引用，`getElement()` 将在代码段的第二行返回消息标识符，`resolve()` 将在第一行（参数列表内部）返回消息标识符。

resolving引用的过程与parsing不同，并且不会同时执行。 而且，它并不总是成功的。 如果当前在IDE中打开的代码无法编译或在其他情况下，则 `PsiReference.resolve()` 返回`null`是正常的-**必须使用引用处理所有代码**。

> 请参阅[有效使用PSI](https://jetbrains.org/intellij/sdk/docs/reference_guide/performance/performance.html#working-with-psi-efficiently)中的繁重计算的缓存结果。

#### <a name="ContributedReferences">Contributed References</a>

除了由编程语言的语义定义的引用之外，IDE还会识别许多引用，这些引用是由代码中使用的API和框架的语义确定的。 考虑以下示例：
```java
File f = new File("foo.txt");
```
从Java语法的角度来看，“`foo.txt`”没有特殊含义-只是字符串文字。 但是，在IntelliJ IDEA中打开此示例，并在同一目录中有一个名为“`foo.txt`”的文件，可以按住 `Ctrl/Cmd + click` “`foo.txt`”并导航到该文件。 之所以可行，是因为IDE可以识别 `new File(...)` 的语义，并且可以对作为参数传递给方法的字符串文字提供引用。

通常，引用可以贡献给没有自己的引用的元素，例如字符串文字和注释。 引用通常还有助于非代码文件，例如XML或JSON。

Contributing引用是扩展现有语言的最常见方法之一。 例如，即使Java PSI是平台的一部分并且未在插件中定义，您的插件也可以contribute对Java代码的引用。

要contribute references，请参阅[reference contributor教程](https://jetbrains.org/intellij/sdk/docs/tutorials/custom_language_support/reference_contributor.html)。

#### <a name="ReferenceswithOptionalMultipleResolveResults">References with Optional or Multiple Resolve Results</a>

在最简单的情况下，引用解析为单个元素，如果解析失败，则代码不正确，IDE需要将其突出显示为错误。但是，有时情况会有所不同。
* 第一种情况是软引用。考虑上面的 `new File("foo.txt")`示例。如果IDE找不到文件 `"foo.txt"`，则并不意味着需要突出显示错误-也许该文件仅在运行时可用。此类引用从 `PsiReference.isSoft()` 方法返回true。
* 第二种情况是多变量引用。考虑JavaScript程序的情况。 JavaScript是一种动态类型化的语言，因此IDE不能总是精确地确定在特定位置正在调用哪种方法。为了解决这个问题，它提供了一个可以解析为多个可能元素的reference。这样的引用实现了 `PsiPolyVariantReference` 接口。

要解析 `PsiPolyVariantReference`，请调用其 `multiResolve()` 方法。该调用返回 `ResolveResult` 对象的数组。每个对象标识一个PSI元素，还指定结果是否有效。例如，如果您有多个Java方法重载，并且调用的参数与任何重载都不匹配，则将为所有重载返回 `ResolveResult` 对象，而 `isValidResult()` 会为所有重载返回false。

#### <a name="SearchingforReferences">Searching for References</a>

如您所知，解析引用意味着从用法转到相应的声明。 要从相反的方向（从声明到用法）执行导航，请执行 `references search`。

要使用 `ReferencesSearch` 执行搜索，请指定要搜索的元素以及其他可选参数，例如需要在其中搜索reference的scope。 创建的 `Query` 允许一次获得所有结果，或者一次又一次地遍历结果。 后者允许在找到第一个（匹配）结果后立即停止处理。

## <a name="ModifyingthePSI">Modifying the PSI</a>

PSI是源代码的可读写表示，表示为与源文件的结构相对应的元素树。 **您可以通过添加，替换和删除PSI元素来修改PSI**。

要执行这些操作，请使用 `PsiElement.add()`，`PsiElement.delete()` 和 `PsiElement.replace()` 之类的方法，以及 `PsiElement` 接口中定义的其他方法，这些方法使您可以在单个操作中处理多个元素，或在树中指定需要添加元素的确切位置。

> 与 `document` 操作一样，PSI修改也需要包装在写操作和命令中（因此只能在事件分发线程中执行）。 有关[命令和写入操作](https://jetbrains.org/intellij/sdk/docs/basics/architectural_overview/documents.html#what-are-the-rules-of-working-with-documents)的更多信息，请参见文档文章。

### <a name="CreatingtheNewPSI">Creating the New PSI</a>

通常添加到树中或替换现有PSI元素的PSI元素通常是根据文本创建的。在一般的情况下，可以使用 `PsiFileFactory` 的 `createFileFromText()` 方法创建一个新文件，该文件包含需要添加到树中或用作现有元素的替换的代码构造，并遍历结果树以查找您需要的特定元素，然后将该元素传递给 `add()` 或 `replace()`。

大多数语言提供了工厂方法，使您可以更轻松地创建特定的代码构造。例如，`PsiJavaParserFacade` 类包含诸如 `createMethodFromText()` 之类的方法，该方法从给定的文本创建Java方法。

当您实现与现有代码一起使用的重构，意图或检查快速修复程序时，传递给各种 `createFromText()` 方法的文本将合并硬编码的片段和从现有文件获取的代码片段。对于较小的代码片段（单个标识符），您只需将现有代码中的文本附加到要构建的代码片段的文本中即可。在这种情况下，您需要确保结果文本在语法上是正确的，否则 `createFromText()` 方法将引发异常。

对于较大的代码片段，最好分几步进行修改：
* 从文本创建替换树片段，为用户代码片段保留占位符；
* 用用户代码片段替换占位符；
* 用替换树替换原始源文件中的元素。

这样可以确保保留用户代码的格式，并且所做的修改不会引起任何不必要的空格更改。

作为此方法的示例，请参见 `ComparingReferencesInspection` 示例中的quickfix：
```java
// binaryExpression holds a PSI expression of the form "x == y", which needs to be replaced with "x.equals(y)"
PsiBinaryExpression binaryExpression = (PsiBinaryExpression) descriptor.getPsiElement();
IElementType opSign = binaryExpression.getOperationTokenType();
PsiExpression lExpr = binaryExpression.getLOperand();
PsiExpression rExpr = binaryExpression.getROperand();

// Step 1: Create a replacement fragment from text, with "a" and "b" as placeholders
PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
PsiMethodCallExpression equalsCall =
    (PsiMethodCallExpression) factory.createExpressionFromText("a.equals(b)", null);

// Step 2: replace "a" and "b" with elements from the original file
equalsCall.getMethodExpression().getQualifierExpression().replace(lExpr);
equalsCall.getArgumentList().getExpressions()[0].replace(rExpr);

// Step 3: replace a larger element in the original file with the replacement tree
PsiExpression result = (PsiExpression) binaryExpression.replace(equalsCall);
```

> 就像 `IntelliJ Platform API` 中的其他地方一样，传递给 `createFileFromText()` 和其他 `createFromText()` 方法的文本必须仅使用 `\n` 作为行分隔符。

### <a name="维护树结构一致性">维护树结构一致性</a>

PSI修改方法不会限制您构建结果树结构的方式。例如，当使用Java类时，即使Java解析器永远不会产生表示方法主体的结构，也可以将for语句（for语句始终是 `PsiCodeBlock` 的子级）添加为 `PsiMethod` 元素的直接子代。产生不正确的树结构的修改似乎可以起作用，但是稍后将导致问题和异常。 因此，您始终需要确保使用PSI修改操作构建的结构与解析所构建的代码时解析器所生成的结构相同。

为了确保您不会引入不一致之处，可以调用 `PsiTestUtil.checkFileStructure()` 来测试修改PSI的操作。此方法可确保您构建的结构与解析器生成的结构相同。

### <a name="WhitespacesandImports">Whitespaces and Imports</a>

使用PSI修改功能时，切勿从文本创建单个空格节点（空格或换行符）。 取而代之的是，**所有空格修改均由格式化程序执行，该格式化程序遵循用户选择的代码样式设置**。 格式化会在每个命令的末尾自动执行，并且如果需要，您还可以使用 `CodeStyleManager` 类中的 `Reformat(PsiElement)` 方法手动执行格式化。

另外，在使用Java代码（或使用具有类似 `import` 机制的其他语言的代码，例如`Groovy`或`Python`）时，切勿手动创建 `imports`。 相反，您应该在生成的代码中插入标准名称，然后在 `JavaCodeStyleManager`（或所用语言的等效API）中调用 `shortClassReferences()` 方法。 这样可以确保根据用户的代码样式设置创建 `imports` 并将其插入文件的正确位置。

### <a name="结合PSI和文档修改">结合PSI和文档修改</a>

在某些情况下，您需要执行PSI修改，然后对刚通过PSI修改过的文档执行操作（例如，启动实时模板）。 在这种情况下，您需要调用一个特殊的方法来完成基于PSI的后处理（例如格式化）并将更改提交给文档。 您需要调用的方法为 `doPostponedOperationsAndUnblockDocument()`，它是在 `PsiDocumentManager` 类中定义的。

## <a name="参考文献">参考文献</a>

[Navigating the PSI](https://jetbrains.org/intellij/sdk/docs/basics/architectural_overview/navigating_psi.html)
