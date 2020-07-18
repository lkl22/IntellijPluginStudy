# Kotlin for Plugin Developers

* [Why Kotlin?](#WhyKotlin?)
* [Adding Kotlin Support](#AddingKotlinSupport)
* [Kotlin Gradle Plugin](#KotlinGradlePlugin)
  * [Use Kotlin to Write Gradle Script](#UseKotlintoWriteGradleScript)
* [UI in Kotlin](#UIinKotlin)
* [Handling Kotlin Code](#HandlingKotlinCode)
* [警告](#警告)
* [Examples](#Examples)
* [参考文献](#参考文献)

## <a name="WhyKotlin?">Why Kotlin?</a>

使用Kotlin为IntelliJ平台编写插件与使用Java编写插件非常相似。 现有的插件开发人员可以通过使用与IntelliJ平台捆绑在一起的[J2K编译器](https://kotlinlang.org/docs/tutorials/mixing-java-kotlin-intellij.html#converting-an-existing-java-file-to-kotlin-with-j2k)（版本143. +）将样板Java类转换为Kotlin等效类来开始使用，并且开发人员可以轻松地将Kotlin类与现有Java代码混合和匹配。

除了[null安全](https://kotlinlang.org/docs/reference/null-safety.html)和[类型安全构建器](https://kotlinlang.org/docs/reference/type-safe-builders.html)之外，Kotlin语言还为插件开发提供了许多方便的功能，这些功能使插件更易于阅读和维护。 就像Android的Kotlin一样，IntelliJ平台大量使用了回调，这些回调很容易在Kotlin中表示为[lambda](https://kotlinlang.org/docs/reference/lambdas.html)。

同样，通过扩展在IntelliJ IDEA中自定义内部类的行为很容易。 例如，通常的做法是保护日志记录语句以避免参数构造的开销，从而在使用日志时导致以下情况：
```java
if (logger.isDebugEnabled()) {
  logger.debug("...");
}
```
通过声明以下扩展方法，我们可以在Kotlin中更简洁地实现相同的结果：
```kotlin
inline fun Logger.debug(lazyMessage: () -> String) {
  if (isDebugEnabled) {
    debug(lazyMessage())
  }
}
```

现在，我们可以直接编写`logger.debug { "..." }`来获得轻量级日志记录的所有好处，而无需赘述。 通过实践，您将能够识别IntelliJ平台中的许多惯用法，这些惯用法可以用Kotlin简化。

## <a name="AddingKotlinSupport">Adding Kotlin Support</a>

> Tip: [GitHub模板](https://jetbrains.org/intellij/sdk/docs/tutorials/github_template.html)使用Kotlin提供了一个预配置的项目。

面向IntelliJ平台143及更高版本的插件易于迁移：只需开始编写Kotlin。 IDE已经捆绑了必要的Kotlin插件和库，无需进一步配置。 有关详细说明，请参阅[Kotlin文档](https://kotlinlang.org/docs/tutorials/getting-started.html)。

## <a name="KotlinGradlePlugin">Kotlin Gradle Plugin</a>

对于已经使用Gradle构建系统的插件，或者需要对Kotlin构建过程进行精确控制的插件，我们建议使用`kotlin-gradle-plugin`。 这个Gradle插件以可控和可复制的方式极大地简化了Kotlin项目的构建。

您的build.gradle文件可能如下所示：
```groovy
plugins {
    id "java"
    id "org.jetbrains.kotlin.jvm" version "1.3.72"
    id "org.jetbrains.intellij" version "0.4.21"
}

apply plugin: "kotlin"
apply plugin: "org.jetbrains.intellij"

group "com.example"
version "0.0.1"

sourceCompatibility = 1.8
targetCompatibility = 1.8

compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.72")
}

intellij {
    version = "2020.1"
    pluginName = "Example"
    updateSinceUntilBuild = false
}
```

### <a name="UseKotlintoWriteGradleScript">Use Kotlin to Write Gradle Script</a>

从4.4开始，Gradle支持build.gradle.kts，这是用Kotlin编写的build.gradle的替代方案。

有很多不错的资源可供您学习如何使用Kotlin脚本为IntelliJ插件编写构建脚本，例如intellij-rust，julia-intellij，covscript-intellij或zig-intellij。

build.gradle.kts基本上看起来像：
```groovy
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    id("org.jetbrains.intellij") version "0.4.21"
}

group = "com.your.company.name"
version = "0.1-SNAPSHOT"

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}
listOf("compileKotlin", "compileTestKotlin").forEach {
    tasks.getByName<KotlinCompile>(it) {
        kotlinOptions.jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

intellij {
    version = "2020.1"
    pluginName = 'Example'
    updateSinceUntilBuild = false
}
```

## <a name="UIinKotlin">UI in Kotlin</a>

用Kotlin创建用户界面的最佳方法是使用[类型安全的DSL](https://jetbrains.org/intellij/sdk/docs/user_interface_components/kotlin_ui_dsl.html)来构建forms。 当前不支持与Kotlin一起使用GUI设计器

## <a name="HandlingKotlinCode">Handling Kotlin Code</a>

如果插件处理Kotlin代码（例如提供检查），则需要添加对Kotlin插件（Plugin ID `org.jetbrains.kotlin`）的依赖。

## <a name="警告">警告</a>

插件必须使用Kotlin类在插件配置文件中实现声明。 在注册extension时，平台使用依赖项注入框架来实例化这些类。 因此，插件不得使用Kotlin对象来实现任何`plugin.xml`声明。

## <a name="Examples">Examples</a>

有许多基于IntelliJ平台构建的开源Kotlin项目。 有关可利用IntelliJ平台构建开发人员工具的Kotlin语言的最新示例和应用程序的现成资源，开发人员可以从以下项目中获得启发：

[IntelliJ-presentation-assistant](https://github.com/chashnikov/IntelliJ-presentation-assistant)

[Rust](https://github.com/intellij-rust/intellij-rust)

[HashiCorp Terraform / HCL language support](https://github.com/VladRassokhin/intellij-hcl)

[TeXiFy IDEA](https://github.com/Hannah-Sten/TeXiFy-IDEA)

[Makefile support](https://github.com/kropp/intellij-makefile)

## <a name="参考文献">参考文献</a>

[Kotlin for Plugin Developers](https://jetbrains.org/intellij/sdk/docs/tutorials/kotlin.html)
