# IntelliJ Plugin Development introduction: PersistStateComponent

* [为IntelliJ插件保存值](#为intellij插件保存值)
* [使类实现PersistentStateComponent](#使类实现persistentstatecomponent)
* [@State注解–指定要保存的存储位置](#指定要保存的存储位置)
* [在plugin.xml中声明PersistentStateComponent](#声明PersistentStateComponent)
* [从其他模块使用PersistentStateComponent](#从其他模块使用persistentstatecomponent)

## <a name="为intellij插件保存值">为IntelliJ插件保存值</a>

如果您的插件想要保留一些配置值，并且希望将这些值保存在存储器中，则可以在IntelliJ IDEA插件开发中使用`PersistentStateComponent`。

* [Persisting State of Components](https://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html)

## <a name="使类实现persistentstatecomponent">使类实现PersistentStateComponent</a>

创建新的Java类`SingleFileExecutionConfig`实现`PersistentStateComponent<T>`。

该类实现了`PersistentStateComponent<SingleFileExecutionConfig>`。因此，状态类型T与创建的类相同。

要实现此接口`PersistentStateComponent<T>`，我们需要重写

* **getState()** 每次保存设置时调用。如果从`getState()`返回的状态不同于默认构造函数获得的默认状态，则返回的状态将以XML序列化并存储。
* **loadState(T)** 在创建组件时以及在外部更改具有持久状态的XML文件之后调用。

并且实现`getInstance`方法。

对于这三种方法的实现，您无需记住上述行为。 只需实现如下模板即可：
```java
/**
 * PersistentStateComponent keeps project config values.
 */
@State(
        name="SingleFileExecutionConfig",
        storages = {
                @Storage("SingleFileExecutionConfig.xml")}
)
public class SingleFileExecutionConfig implements PersistentStateComponent<SingleFileExecutionConfig> {
 
    @Nullable
    @Override
    public SingleFileExecutionConfig getState() {
        return this;
    }
 
    @Override
    public void loadState(SingleFileExecutionConfig singleFileExecutionConfig) {
        XmlSerializerUtil.copyBean(singleFileExecutionConfig, this);
    }
 
    @Nullable
    public static SingleFileExecutionConfig getInstance(Project project) {
        return ServiceManager.getService(project, SingleFileExecutionConfig.class);
    }
}
```

> 请注意，当`PersistentStateComponent`为项目级别时，对于`getInstance`中的`getService`方法，`project`变量是必需的。如果您的Service是应用程序级别的，则不需要`project`实例。

* [Plugin Services](https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_services.html)

## <a name="指定要保存的存储位置">@State注解–指定要保存的存储位置</a>

您可能会注意到，@State注解写在顶部。这是用来指定将存储持久值的位置。

For the fields：
* name (required) – specifies the name of the state.
* storages – specify the storage locations

Example:
@Storage("yourName.xml") If component is project-level
@Storage(StoragePathMacros.WORKSPACE_FILE) for values stored in the workspace file.

有关更多详细信息，请参见官方文档的[“定义存储位置”](https://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html)

之后，您只需声明将要保存的变量，以及这些变量的Getter和Setter。 例如，要声明一个String变量executableName，请将以下代码添加到此类。
```java
    String executableName;
 
    public String getExecutableName() {
        return executableName;
    }
 
    public void setExecutableName(String executableName) {
        this.executableName = executableName;
    }
```

## <a name="声明PersistentStateComponent">在plugin.xml中声明PersistentStateComponent</a>

要使用此PersistentStateComponent，需要在plugin.xml中进行声明
```xml
  <extensions defaultExtensionNs="com.intellij">
    <!-- Add your extensions here -->
    ...
    <projectService
            serviceInterface="com.lkl.plugin.configurable.SingleFileExecutionConfig"
            serviceImplementation="com.lkl.plugin.configurable.SingleFileExecutionConfig"/>
  </extensions>
```

## <a name="从其他模块使用persistentstatecomponent">从其他模块使用PersistentStateComponent</a>

可以通过调用getInstance方法获得实例。例如：
```java
    private final SingleFileExecutionConfig mConfig;
 
    @SuppressWarnings("FieldCanBeLocal")
    private final Project mProject;
 
    public SingleFileExecutionConfigurable(@NotNull Project project) {
        mProject = project;
        mConfig = SingleFileExecutionConfig.getInstance(project);
    }
```
要更新值，您可以直接更新此实例的字段变量`（mConfig）`。不需要显式的`“save”`方法调用！下次获取值时，该值将自动保存。

下面的代码是变量更新部分的示例。 当用户在“设置”对话框中更改配置时，将调用此`apply()`方法。
```java
    public void apply() {
        mConfig.setExecutableName(exeNameTextField.getText());
        mConfig.notShowOverwriteConfirmDialog = notShowDialogCheckBox.isSelected();
    }
```
