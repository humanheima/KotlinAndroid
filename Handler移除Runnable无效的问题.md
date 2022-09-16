### 使用lambda表达式

```kotlin

//定义handler
val handler: Handler = object : Handler() {

    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)
    }
}

//定义发送的延迟消息，用来展示loading，注意这是一个lambda表达式，并没有显式的声明类型
val showDialogRunnable = {
    Log.i(TAG, "在runnable里面展示弹窗")
    showLoading()
}

```

```kotlin
fun onClick(v: View) {
    when (v.id) {
        R.id.btn_send_msg -> {
            //点击按钮后，延迟5秒发送消息展示loading
            Log.i(TAG, "onClick: 发送延迟消息")
            handler.postDelayed(showDialogRunnable, 5000)
        }
        R.id.btn_remove_msg -> {
            //在5秒之内移除消息，让loading展示不出来
            Log.i(TAG, "onClick: 移除消息")
            handler.removeCallbacks(showDialogRunnable)
        }
    }
}
```

结果发现loading还是展示出来了。这是什么原因呢？我们看一下Kotlin反编译出来的java代码。

定义的 showDialogRunnable 对象，可以看到其类型是 Function0 类型，并不是Runnable。

```java
@NotNull
private final Function0 showDialogRunnable = (Function0)(new Function0() {
    // $FF: synthetic method
    // $FF: bridge method
    public Object invoke() {
        this.invoke();
        return Unit.INSTANCE;
    }
    public final void invoke() {
        Log.i("TestFuncActivity", "在runnable里面展示弹窗");
        TestFuncActivity.this.showLoading();
    }
});
```

```java
public final void onClick(@NotNull View v) {
    Intrinsics.checkNotNullParameter(v, "v");
    Handler var10000;
    Object var10001;
    Object var2;
    switch(v.getId()) {
        case 1000028:
            Log.i("TestFuncActivity", "onClick: 发送延迟消息");
            var10000 = this.handler;
            //注释1处，获取 showDialogRunnable 对象
            var10001 = this.showDialogRunnable;
            if(var10001 != null) {
                var2 = var10001;
                //注释2处，利用 showDialogRunnable 对象创建了一个新对象 一个 TestFuncActivity$sam$java_lang_Runnable$0 对象。
                var10001 = new TestFuncActivity$sam$java_lang_Runnable$0((Function0) var2);
            }
            //注释3处，将包装后的对象传递给Handler的 postDelayed 方法。
            var10000.postDelayed((Runnable) var10001, 5000 L);
            break;
        case 1000172:
            Log.i("TestFuncActivity", "onClick: 移除消息");
            var10000 = this.handler;
            var10001 = this.showDialogRunnable;
            if(var10001 != null) {
                var2 = var10001;
                //注释4处，这里有重新 new 了一个  TestFuncActivity$sam$java_lang_Runnable$0 对象。
                var10001 = new TestFuncActivity$sam$java_lang_Runnable$0((Function0) var2);
            }
            var10000.removeCallbacks((Runnable) var10001);
    }
}
```

注释2处，利用 showDialogRunnable 对象创建了一个新对象 一个 TestFuncActivity$sam$java_lang_Runnable$0 对象。


TestFuncActivity$sam$java_lang_Runnable$0 这个类是什么东西呢，其实是继承了 Runnable 的一个类。

```java
final class TestFuncActivity$sam$java_lang_Runnable$0 implements Runnable {
    // $FF: synthetic field
    private final Function0
    function;
    TestFuncActivity$sam$java_lang_Runnable$0(Function0 var1) {
            this.function = var1;
        }
        // $FF: synthetic method
    public final void run() {
        Intrinsics.checkNotNullExpressionValue(this.function.invoke(), "invoke(...)");
    }
}
```

//注释4处，这里有重新 new 了一个  TestFuncActivity$sam$java_lang_Runnable$0 对象。

通过注释2处和注释4处，我们知道了 postDelay 的对象 和 removeCallbacks 的对象是两个不同的对象，所以移除不起作用。


### 解决方法 ： 显式使用Runnable 

```kotlin
val showDialogRunnable: Runnable = Runnable {
    Log.i(TAG, "在runnable里面展示弹窗")
    showLoading()
}

```

我们看下反编译出来的代码，这个时候就是一个 Runnable 对象。

```java
 @NotNull
 private final Runnable showDialogRunnable = (Runnable)(new Runnable() {
     public final void run() {
         Log.i("TestFuncActivity", "在runnable里面展示弹窗");
         TestFuncActivity.this.showLoading();
     }
 });
```



```java
public final void onClick(@NotNull View v) {
    Intrinsics.checkNotNullParameter(v, "v");
    switch(v.getId()) {
        case 1000028:
            Log.i("TestFuncActivity", "onClick: 发送延迟消息");
            //注释1处， postDelayed showDialogRunnable
            this.handler.postDelayed(this.showDialogRunnable, 5000 L);
            break;
        case 1000172:
            Log.i("TestFuncActivity", "onClick: 移除消息");
            //注释2处， removeCallbacks showDialogRunnable
            this.handler.removeCallbacks(this.showDialogRunnable);
    }
}
```

注释1处， postDelayed showDialogRunnable。
注释2处， removeCallbacks showDialogRunnable。

现在 postDelayed 和 removeCallbacks 的就是同一个对象了，可以正常移除。