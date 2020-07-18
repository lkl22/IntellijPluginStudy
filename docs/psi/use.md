# PSI使用

* [导航PSI](#导航PSI)
  * [Top-Down Navigation](#Top-DownNavigation)
  * [Bottom-Up Navigation](#Bottom-UpNavigation)
  * [PSI References](#PSIReferences)
    * [Contributed References](#ContributedReferences)
    * [References with Optional or Multiple Resolve Results](#ReferenceswithOptionalMultipleResolveResults)
    * [Searching for References](#SearchingforReferences)
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


## <a name="参考文献">参考文献</a>

[Navigating the PSI](https://jetbrains.org/intellij/sdk/docs/basics/architectural_overview/navigating_psi.html)
