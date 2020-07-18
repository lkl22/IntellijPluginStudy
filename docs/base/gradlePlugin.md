# Building Plugins with Gradle

* [简介](#简介)
* [Getting Started with Gradle](#GettingStartedwithGradle)
  * [Creating a Gradle-Based IntelliJ Platform Plugin with New Project Wizard](#CreatingGradleBasedIntelliJPlatformPluginwithNewProjectWizard)
  * [向现有的基于DevKit的IntelliJ平台插件添加Gradle支持](#向现有的基于DevKit的IntelliJ平台插件添加Gradle支持)
  * [运行基于简单Gradle的IntelliJ平台插件](#运行基于简单Gradle的IntelliJ平台插件)
* [Configuring Gradle Projects](#ConfiguringGradleProjects)
  * [配置Gradle插件以构建IntelliJ平台插件项目](#配置Gradle插件以构建IntelliJ平台插件项目)
  * [配置Gradle插件以运行IntelliJ Platform插件项目](#配置Gradle插件以运行IntelliJPlatform插件项目)
  * [Gradle插件使用的管理目录](#Gradle插件使用的管理目录)
  * [通过Gradle插件控制下载](#通过Gradle插件控制下载)
  * [Patching the Plugin Configuration File](#PatchingthePluginConfigurationFile)
  * [用于开发的通用Gradle插件配置](#用于开发的通用Gradle插件配置)
* [参考文献](#参考文献)

## <a name="简介">简介</a>
`gradle-intellij-plugin` Gradle插件是构建IntelliJ插件的推荐解决方案。插件负责插件项目的依赖项-基本IDE和其他插件依赖项。

`gradle-intellij-plugin`提供了一些tasks，可通过您的插件运行IDE并将您的插件发布到JetBrains插件存储库。 为确保您的插件不受平台主要版本之间可能发生的API更改的影响，您可以轻松地针对多种版本的基本IDE构建您的插件。

> 在将其他存储库添加到Gradle构建脚本时，请确保始终使用HTTPS协议。

请确保始终升级到`gradle-intellij-plugin`的最新版本。 关注[GitHub](https://github.com/JetBrains/gradle-intellij-plugin/releases)上的发布。

## <a name="GettingStartedwithGradle">Getting Started with Gradle</a>

`Gradle`是用于创建`IntelliJ Platform`插件的首选解决方案。 IntelliJ IDEA Ultimate和Community版本捆绑了必要的插件以支持基于Gradle的开发。 这些`IntelliJ IDEA`插件是`Gradle`和`Plugin DevKit`，默认情况下启用。

### <a name="CreatingGradleBasedIntelliJPlatformPluginwithNewProjectWizard">Creating a Gradle-Based IntelliJ Platform Plugin with New Project Wizard</a>

使用“**新建项目向导**”执行基于`Gradle`的新`IntelliJ Platform`插件项目的创建。 该向导根据一些模板输入创建所有必要的项目文件。

> **请注意**，Gradle 6.1有一个已知的[bug](https://github.com/gradle/gradle/issues/11966)，阻止它用于开发插件，请升级到6.1.1或更高版本。

启动[New Project Wizard](https://www.jetbrains.com/help/idea/gradle.html#project_create_gradle)。 它通过两个屏幕指导您完成Gradle项目的创建过程。

#### New Project Configuration Screen

在第一个screen上，配置项目类型：

* 在左侧的项目类型窗格中，选择`Gradle`。
* 指定基于Java 8 JDK的`Project SDK`。 该SDK将是用于运行Gradle的默认JRE，并且是用于编译插件Java源代码的JDK版本。
* 在“`Additional Libraries and Frameworks`”面板中，选择“`Java`和`IntelliJ Platform Plugin`”。 这些设置将在本教程的其余部分中使用。

可选地：

* 要在插件中包含对Kotlin语言的支持，请选中`Kotlin/JVM`框（下面的绿色圆圈）。可以使用或不使用Java语言来选择此选项。 有关更多信息，请参见[Kotlin for Plugin Developers](https://jetbrains.org/intellij/sdk/docs/tutorials/kotlin.html)。
* 要将`build.gradle`文件创建为`Kotlin`构建脚本（`build.gradle.kts`）而不是`Groovy`，请选中`Kotlin DSL build script`框（在下面的洋红色圆圈中）。

Then click Next:

![](./imgs/gradlePlugin/step1_new_gradle_project.png)

#### Project Naming/Artifact Coordinates Screen

展开“`Artifact Coordinates`”部分，并使用Maven命名约定指定`GroupId`，`ArtifactId`和`Version`。

* `GroupId`通常是Java程序包名称，用于项目的`build.gradle`文件中的Gradle属性`project.group`值。 对于此示例，输入com.your.company。
* `ArtifactId`是项目JAR文件的默认名称（无版本）。 它也用于项目的`settings.gradle`文件中的Gradle属性`rootProject.name`值。 对于此示例，输入my_gradle_plugin。
* 版本用于`build.gradle`文件中的Gradle属性`project.version`值。 对于此示例，输入1.0。

“`Name`”字段将与指定的`ArtifactId`自动同步。

在“`Location`”中指定新项目的路径，然后单击“完成”以继续并生成项目。

#### Components of a Wizard-Generated Gradle IntelliJ Platform Plugin

For the example my_gradle_plugin, the New Project Wizard creates the following directory content:

```
my_gradle_plugin
├── build.gradle
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── settings.gradle
└── src
    ├── main
    │   ├── java
    │   └── resources
    │       └── META-INF
    │           └── plugin.xml
    └── test
        ├── java
        └── resources
```

* 默认的IntelliJ Platform `build.gradle`文件。
* `Gradle Wrapper`文件，尤其是`gradle-wrapper.properties`文件，该文件指定了用于构建插件的Gradle版本。 如果需要，`IntelliJ IDEA Gradle`插件将下载此文件中指定的Gradle版本。
* `settings.gradle`文件，其中包含`rootProject.name`的定义。
* 默认主`SourceSet`下的`META-INF`目录包含插件配置文件。

The generated `my_gradle_plugin` project `build.gradle` file:

```groovy
plugins {
    id 'java'
    id 'org.jetbrains.intellij' version '0.4.21'
}

group 'com.your.company'
version '1.0' 
sourceCompatibility = 1.8

repositories {
    mavenCentral()
} 
dependencies {
    testImplementation group: 'junit', name: 'junit', version: '4.12'
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version '2020.1'
}
patchPluginXml {
    changeNotes """
      Add change notes here.<br>
      <em>most HTML tags may be used</em>"""
}
```

* 显式声明了Gradle的两个插件：
  * Gradle `Java`插件。
  * `gradle-intellij-plugin`。
* Project Naming/Artifact Coordinates Screen中的**GroupId**是`project.group`值。
* Project Naming/Artifact Coordinates Screen中的**Version**是`project.version`值。
* 注入`sourceCompatibility`行以使用Java 8 JDK强制编译Java源代码。
* 该文件中唯一的注释是`gradle-intellij-plugin`的README.md链接，该链接是其配置DSL的参考。
* 设置DSL属性`intellij.version`的值指定用于构建插件的**IntelliJ平台的版本**。 它默认为用于运行“新建项目向导”的IntelliJ IDEA版本。
* Patching DSL属性`patchPluginXml.changeNotes`的值设置为占位符文本。


#### 插件Gradle属性和插件配置文件元素

通常，Gradle属性`rootProject.name`和`project.group`不会与相应的插件配置文件`plugin.xml`元素`<name>`和`<id>`匹配。 由于它们具有不同的功能，因此没有与IntelliJ平台相关的原因。

* `<name>`元素（用作插件的显示名称）通常与`rootProject.name`相同，但更具解释性。
* `<id>`值必须是所有插件上的唯一标识符，通常是指定的`GroupId`和`ArtifactId`的串联。 

> 请注意，在不丢失现有安装的自动更新的情况下，更改已发布插件的<id>是不可能的。

### <a name="向现有的基于DevKit的IntelliJ平台插件添加Gradle支持">向现有的基于DevKit的IntelliJ平台插件添加Gradle支持</a>

可以使用“New Project Wizard”将基于DevKit的插件项目转换为基于Gradle的插件项目，以围绕现有的基于DevKit的项目创建基于Gradle的项目：

* 确保如有必要，可以完全恢复包含基于DevKit的IntelliJ Platform插件项目的目录。
* 删除基于DevKit的项目的所有artifacts：
  * `.idea`目录
  * `[modulename].iml` 文件
  * `out` 目录
* 将现有的源文件以`Gradle SourceSet`格式排列在项目目录中。
* 使用“`New Project Wizard`”，就像从头开始创建新的Gradle项目一样。
* 在`Project Naming/Artifact Coordinates Screen` 上，将值设置为：
  * `GroupId`现有插件的源码集的包名。
  * `ArtifactId`为现有插件的名称。
  * `Version`与现有插件相同。
  * `Name`为现有插件的名称。 （应从ArtifactId中预先填写）
  * 将`Location`设置为现有插件的目录。
* 单击完成以创建新的基于Gradle的插件。
* 根据需要使用Gradle `Source Set`添加更多模块。

### <a name="运行基于简单Gradle的IntelliJ平台插件">运行基于简单Gradle的IntelliJ平台插件</a>

从IDE的`Gradle Tool window`运行Gradle项目。

#### Adding Code to the Project

在运行my_gradle_project之前，可以添加一些代码以提供简单的功能。

#### Executing the Plugin

打开`Gradle Tool window`并搜索`runIde`任务：

* 如果不在列表中，请点击Gradle窗口顶部的Refresh按钮。
* 或创建一个新的Gradle运行配置。

![](./imgs/gradlePlugin/gradle_tasks_in_tool_window.png)

双击`runIde`任务以执行它。 有关[使用Gradle任务](https://www.jetbrains.com/help/idea/gradle.html#96bba6c3)的更多信息，请参见IntelliJ IDEA帮助。

最后，在IDE开发实例中启动`my_gradle_plugin`时，“工具”菜单下应有一个新菜单。

## <a name="ConfiguringGradleProjects">Configuring Gradle Projects</a>

### <a name="配置Gradle插件以构建IntelliJ平台插件项目">配置Gradle插件以构建IntelliJ平台插件项目</a>

默认情况下，Gradle插件将针对由IntelliJ IDEA社区版的最新EAP快照定义的IntelliJ平台构建一个插件项目。

> 使用IntelliJ Platform的EAP版本需要将Snapshots存储库添加到build.gradle文件中（请参阅[IntelliJ Platform Artifacts存储库](https://jetbrains.org/intellij/sdk/docs/reference_guide/intellij_artifacts.html)）。

如果本地计算机上没有可用的指定IntelliJ Platform的匹配版本，则Gradle插件将下载正确的版本和类型。然后，IntelliJ IDEA会索引构建以及任何相关的源代码和JetBrains Java Runtime。

#### IntelliJ平台配置

显示设置 [Setup DSL](https://github.com/JetBrains/gradle-intellij-plugin#setup-dsl) 属性`intellij.version`和`intellij.type`会告诉Gradle插件使用`IntelliJ Platform`的配置来构建插件项目。

可以在 [IntelliJ Platform Artifacts Repositories](https://jetbrains.org/intellij/sdk/docs/reference_guide/intellij_artifacts.html) 中浏览所有可用的平台版本。

如果存储库中没有可用的所选平台版本，或者目标IDE的本地安装是IntelliJ Platform的所需类型和版本，请使用`intellij.localPath`指向该安装。 **如果设置了`intellij.localPath`属性，请不要设置`intellij.version`和`intellij.type`属性**，因为这可能导致未定义的行为。

#### 插件依赖

IntelliJ Platform插件项目可能取决于捆绑的插件或第三方插件。 在这种情况下，应根据与用于构建插件项目的`IntelliJ Platform`版本相匹配的那些插件的版本来构建项目。 Gradle插件将获取`intellij.plugins`定义的列表中的所有插件。 有关指定插件和版本的信息，请参见Gradle插件自述文件。

请注意，此属性描述了一个依赖项，因此Gradle插件可以获取所需的`artifacts`。 如[插件依赖](https://jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_dependencies.html#3-dependency-declaration-in-pluginxml)关系中所述，必须将运行时依赖关系添加到[插件配置](https://jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_configuration_file.html)（`plugin.xml`）文件中。

### <a name="配置Gradle插件以运行IntelliJPlatform插件项目">配置Gradle插件以运行IntelliJ Platform插件项目</a>

默认情况下，Gradle插件将为IDE开发实例使用与构建插件相同版本的IntelliJ平台。默认情况下也使用相应的`JetBrains Runtime`。

#### 在基于IntelliJ平台的IDE的替代版本和类型上运行

用于开发实例的IntelliJ Platform IDE可以与用于构建插件项目的IntelliJ Platform IDE不同。 设置“Running DSL”属性`runIde.ideDirectory`将定义要用于开发实例的IDE。 在替代的基于IntelliJ平台的IDE中运行或调试插件时，通常使用此属性。

#### 在JetBrains运行时的替代版本上运行

IntelliJ Platform的每个版本都有对应的`JetBrains Runtime`版本。 通过指定`runIde.jbrVersion`属性，可以使用不同版本的Runtime，该属性描述了IDE开发实例应使用的JetBrains运行时的版本。 Gradle插件将根据需要获取指定的`JetBrains Runtime`。

### <a name="Gradle插件使用的管理目录">Gradle插件使用的管理目录</a>

有几个属性可以控制Gradle插件在何处放置目录以供下载以及供IDE开发实例使用。

可以使用Gradle插件属性控制[沙箱主目录](https://jetbrains.org/intellij/sdk/docs/basics/ide_development_instance.html#sandbox-home-location-for-gradle-based-plugin-projects)及其子目录的位置。 `intellij.sandboxDirectory`属性用于设置在IDE开发实例中运行插件时要使用的沙箱目录的路径。 可以使用`runIde.configDirectory`，`runIde.pluginsDirectory`和`runIde.systemDirectory`属性控制沙箱子目录的位置。 如果显式设置了`intellij.sandboxDirectory`路径，则子目录属性默认为新的沙箱目录。

下载的IDE版本和组件的存储位置默认为**Gradle缓存目录**。 但是，可以通过设置`intellij.ideaDependencyCachePath`属性来控制它。

### <a name="通过Gradle插件控制下载">通过Gradle插件控制下载</a>

如有关配置用于构建插件项目的IntelliJ Platform的部分所述，Gradle插件将获取默认值或intellij属性指定的IntelliJ Platform版本。 **跨项目标准化Gradle插件和Gradle系统的版本将最大程度减少下载版本所花费的时间**。

有一些用于管理`gradle-intellij-plugin`版本和`Gradle`本身版本的控件。 插件版本在项目的`build.gradle`文件的`plugins {}`部分中定义。 Gradle的版本在`<PROJECT ROOT>/gradle/wrapper/gradle-wrapper.properties`中定义。

### <a name="PatchingthePluginConfigurationFile">Patching the Plugin Configuration File</a>

插件项目的plugin.xml文件具有在构建时从`patchPluginXml task`（Patching DSL）的属性中“`patched`”的元素值。`Patching DSL`中的尽可能多的属性将替换为插件项目的`plugin.xml`文件中的相应元素值：

* 如果定义了`patchPluginXml`属性默认值，则无论`patchPluginXml` task是否出现在`build.gradle`文件中，都会在`plugin.xml`中patched该属性值。
  * 例如，属性`patchPluginXml.sinceBuild`和`patchPluginXml.untilBuild`的默认值是基于`intellij.version`的声明值（或默认值）定义的。 因此，默认情况下，`patchPluginXml.sinceBuild`和`patchPluginXml.untilBuild`被替换为plugin.xml文件中`<idea-version>`元素的`since-build`和`until-build`属性。
* 如果显式定义了`patchPluginXml`属性值，则该属性值将替换在plugin.xml中。
  * 如果显式设置了`patchPluginXml.sinceBuild`和`patchPluginXml.untilBuild`属性，则将两者都替换在`plugin.xml`中。
  * 如果显式设置了一个属性（例如`patchPluginXml.sinceBuild`）而没有显式设置另一个（例如`patchPluginXml.untilBuild`具有默认值），则这两个属性将按各自的值（显式和默认）进行修补。
* 为了不替换`<idea-version>`元素的`since-build`和`until-build`属性，必须在`build.gradle`文件中出现以下之一：
  * 设置`intellij.updateSinceUntilBuild = false`将禁用替换`since-build`和`until-build`属性，
  * 或者，独立控制，请根据是否要禁用一个或两个替换来设置`patchPluginXml.sinceBuild(null)`和`patchPluginXml.untilBuild(null)`。

避免混淆的最佳实践是用注释替换Gradle插件要修补的`plugin.xml`中的元素。 这样，这些参数的值不会出现在源代码的两个位置。 Gradle插件将在patching过程中添加必要的元素。 对于包含说明（例如`changeNotes`和`pluginDescription`）的那些`patchPluginXml`属性，使用HTML元素时不需要`CDATA`块。

> Tip: 要维护并生成最新的变更日志，请尝试使用[Gradle变更日志插件](https://github.com/JetBrains/gradle-changelog-plugin)

如向导生成的Gradle IntelliJ平台插件的组件中所述，Gradle属性`project.version`，`project.group`和`rootProject.name`均基于向导的输入生成。 但是，`gradle-intellij-plugin`不会将那些Gradle属性组合并替换为`plugin.xml`文件中的默认`<id>`和`<name>`元素。

> 最佳做法是使`project.version`保持最新。 默认情况下，如果您在`build.gradle`中修改`project.version`，则Gradle插件将自动更新`plugin.xml`文件中的`<version>`值。 这种做法使所有版本声明保持同步。

### <a name="用于开发的通用Gradle插件配置">用于开发的通用Gradle插件配置</a>

需要使用Gradle插件属性的不同组合来创建所需的内部版本或IDE开发实例环境。

#### 面向IntelliJ IDEA的插件

针对IntelliJ IDEA的IntelliJ Platform插件具有最直接的Gradle插件配置。

* 确定用于构建插件项目的IntelliJ IDEA版本； 这是IntelliJ平台的理想版本。 这可以是EAP（默认），也可以由[内部版本号范围](https://jetbrains.org/intellij/sdk/docs/basics/getting_started/build_number_ranges.html)确定。
  * 如果需要IntelliJ IDEA的生产版本，请相应地设置`intellij version`属性。
  * 设置必要的插件依赖项（如果有）。
* 如果应在基于相同IntelliJ IDEA版本的IDE开发实例中运行或调试插件项目，则无需为IDE开发实例设置其他属性。 这是默认行为，也是最常见的用例。
  * 如果应基于IntelliJ Platform的替代版本在IDE开发实例中运行或调试插件项目，请相应地设置“`Running DSL`”属性。
  * 如果应使用IDE开发实例的默认值以外的其他JetBrains运行时来运行插件项目，请指定JetBrains运行时版本。
* 设置用于修补plugin.xml文件的适当属性。

#### 针对替代基于IntelliJ平台的IDE的插件

Gradle还支持开发插件以在基于IntelliJ平台的IDE中运行。 有关更多信息，请参见“[Developing for Multiple Products](https://jetbrains.org/intellij/sdk/docs/products/dev_alternate_products.html)”页面。




## <a name="参考文献">参考文献</a>

[Building Plugins with Gradle](https://jetbrains.org/intellij/sdk/docs/tutorials/build_system.html)

[intellij-platform-plugin-template](https://github.com/JetBrains/intellij-platform-plugin-template)

[Plugin for building plugins for IntelliJ IDEs](https://plugins.gradle.org/plugin/org.jetbrains.intellij#groovy-usage)

[https://github.com/JetBrains/gradle-intellij-plugin](https://github.com/JetBrains/gradle-intellij-plugin)
