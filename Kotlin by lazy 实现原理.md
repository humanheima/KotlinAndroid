
```kotlin
class LazyInitActivity : AppCompatActivity() {


    private val messageView: TextView by lazy {
        findViewById<TextView>(R.id.tvByLazy)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lazy_init)
    }

    fun onSayHello() {
        //当我们第一次引用messageView的时候，会执行 by lazy 的lambda表达式初始化变量 messageView
        messageView.text = "Hello"
    }

}

```

```kotlin
class LazyInitDemo {
    val myName: String by lazy { "John" }
}
```
查看反编译的Kotlin字节码
```java
public final class LazyInitDemo {
    
   @NotNull
   private final Lazy myName$delegate;

   @NotNull
   public final String getMyName() {
      Lazy var1 = this.myName$delegate;
      return (String)var1.getValue();
   }

   public LazyInitDemo() {
      this.myName$delegate = LazyKt.lazy((Function0)null.INSTANCE);
   }
}

```
* 生成了一个`Lazy`类型的变量`myName$delegate`，就是我们的属性名加上后缀`$delegate`。
* `LazyKt.lazy()`负责执行`by lazy`后面的lambda表达式。
* 在LazyInitDemo的构造函数中，将`LazyKt.lazy()`的执行结果赋值给`myName$delegate`变量。


调用`getMyName()`方法，内部会调用`myName$delegate`的`getValue()`方法。

LazyJVM.kt中定义的
```kotlin
public actual fun <T> lazy(initializer: () -> T): Lazy<T> = SynchronizedLazyImpl(initializer)

```

```kotlin 
public actual fun <T> lazy(mode: LazyThreadSafetyMode, initializer: () -> T): Lazy<T> =
    when (mode) {
        LazyThreadSafetyMode.SYNCHRONIZED -> SynchronizedLazyImpl(initializer)
        LazyThreadSafetyMode.PUBLICATION -> SafePublicationLazyImpl(initializer)
        LazyThreadSafetyMode.NONE -> UnsafeLazyImpl(initializer)
}

```
参考链接：
* [How Kotlin’s delegated properties and lazy-initialization work](https://medium.com/til-kotlin/how-kotlins-delegated-properties-and-lazy-initialization-work-552cbad8be60)
* [[译]带你揭开Kotlin中属性代理和懒加载语法糖衣](https://zhuanlan.zhihu.com/p/65914552)

