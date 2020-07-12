# PsiAugmentProvider

* [简介](#简介)
* [重写getAugments方法](#重写getaugments方法)
* [参考文献](#参考文献)

## <a name="简介">简介</a>
对于这个类，IDEA中提供的说明是：有些代码不是它看起来的样子！这种扩展允许插件增强现实，改变Java PSI元素的行为。

IDEA中所有文件以及文件中的内容都是用PSI树来表示的，比如类表示为PsiClass，方法表示为PsiMethod，字段表示为PsiField。我们可以通过更改PSI来做到动态的添加字段，方法等。

上面说的比较抽象，我们来举一个简单的例子，还是以Lombok为例。了解Lombok原理的同学都知道，Lombok利用的是javac技术，就是在java文件编译的时候动态的将getter，setter等方法生成到class文件中。

这种方式就出带来一个问题，由于getter和setter方法在编译的时候才会动态生成，实际运行时不会有任务问题；但是在IDEA中，编译检查时会发现没有对应的setter或者getter方法，就会出现“标红”，提示没有该字段或者方法。

其实这时就应该清楚了，我们可以通过继承PsiAugmentProvider这个类，通过扩展PSI，动态的为该类中新增相应的“虚拟”的getter或者setter方法。以此来解决“标红”问题。

这时在编辑框中是没有通过PsiAugmentProvider扩展的这些方法，但是实际调用的时候却有，这也就是上述所说的：有些代码不是它看起来的样子！

> 动态扩展的PSI方法或者字段，在编译时不会生成。在进行扩展方法时，只需要生成具体的方法签名即可~不需要关注具体的实现细节。

## <a name="重写getaugments方法">重写getAugments方法</a>

通过重写`getAugments`方法可以扩展每个文件的`PSI`，即实现动态的扩展相应的方法或者字段。

`getAugments`中会传入两个参数，分别为`PsiElement psiElement`和`Class type`：

* **PsiElement**是PSI系统下不同类型对象的一个统称，是基类；比如之前提到的`PsiMethod`、`PsiClass`等等都是一个个具体的`PsiElement`的实现。
* **Class type**，type类型可以用于判断具体是`PsiMethod`还是`PsiClass`，进行分开处理。

在getAugments中进行扩展相应方法时，需要借助IDEA中提供的`LightMethodBuilder`类。通过设置方法名，方法的修饰符，方法的返回值，方法的传入即可添加一个方法。

比如现在需要为`mobile`字段，添加set方法，那么具体的代码为：
```java
PsiManager manager = psiField.getManager(); 
LightMethodBuilder method =  new LightMethodBuilder(manager, JavaLanguage.INSTANCE, methodName); 
method.addModifier(PsiModifier.PUBLIC); 
method.setContainingClass(psiClass); 
method.setNavigationElement(psiField);
method.addParameter(psiField.getName(), psiField.getType());
method.setMethodReturnType(PsiType.VOID);
```

在getAugments中进行扩展相应字段时，需要借助IDEA中提供的`LightFieldBuilder`类。同样通过设置字段名，字段的修饰符即可添加一个字段。

比如现在需要添加 `private static final Logger logger`字段，那么具体的代码为：
```java
PsiType psiLoggerType = psiElementFactory.createTypeFromText(LOGGER_TYPE, psiClass);
LightFieldBuilder loggerField = new LightFieldBuilder(manager, LOGGER_NAME, psiLoggerType); 
LightModifierList modifierList = (LightModifierList) loggerField.getModifierList(); 
modifierList.addModifier(PsiModifier.PRIVATE); 
modifierList.addModifier(PsiModifier.STATIC); 
modifierList.addModifier(PsiModifier.FINAL); 
loggerField.setContainingClass(psiClass); 
loggerField.setNavigationElement(psiAnnotation);
```

在`PsiAugmentProvider`中，对于每个生成的`method`和`field`，都需要加入到`Cache`当中，以此来保证每次获取时候的性能~在IDEA插件开发当中可以选择其提供的`CachedValuesManager`~

## <a name="参考文献">参考文献</a>

[IntelliJ IDEA插件开发指南(二)](https://blog.csdn.net/ExcellentYuXiao/article/details/80273347)

[Class PsiAugmentProvider](https://dploeger.github.io/intellij-api-doc/com/intellij/psi/augment/PsiAugmentProvider.html)

[Java Code Examples for com.intellij.psi.augment.PsiAugmentProvider](https://www.programcreek.com/java-api-examples/index.php?api=com.intellij.psi.augment.PsiAugmentProvider)
