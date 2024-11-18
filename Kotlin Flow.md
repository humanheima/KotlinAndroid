### 共享的 MutableSharedFlow

```kotlin

// 创建一个 MutableSharedFlow
class EventBus {
    private val _events = MutableSharedFlow<String>() // 私有的可变共享流

    // 发射事件
    suspend fun sendEvent(event: String) {
        _events.emit(event)
    }

    // 获取共享流
    val events = _events
}
```

收集

```kotlin

binding.btnSharedFlow.setOnClickListener {

    //启动一个协程来收集事件
    lifecycleScope.launch {

        eventBus.events.collect(object : FlowCollector<String> {
            override suspend fun emit(value: String) {
                Log.d(TAG, "emit2:  $value ")
            }
        })
    }

    //启动一个协程来收集事件
    lifecycleScope.launch {

        eventBus.events.collect(object : FlowCollector<String> {
            override suspend fun emit(value: String) {
                Log.d(TAG, "emit1:  $value ")
            }
        })
    }
}

//改变值
binding.btnSendSharedFlowEvent.setOnClickListener {
    lifecycleScope.launch {
        eventBus.sendEvent("Hello")
        delay(1000) // 延迟 1 秒()
        eventBus.sendEvent("World")
    }
}
```

### 疑问

这样可以启动多个协程来收集事件

```kotlin   

repeat(3) { index ->
    lifecycleScope.launch {
        eventBus.events.collect(object : FlowCollector<String> {
            override suspend fun emit(value: String) {
                Log.d(TAG, "emit $index: 收到的值 $value ")
            }
        })
    }
}

```

但是这样不行

这个写法是不是不对呀？
```kotlin

lifecycleScope.launch {
    repeat(3) { index ->
        Log.d(TAG, "onCreate: index = $index")
        eventBus.events.collect(object : FlowCollector<String> {
            override suspend fun emit(value: String) {
                Log.d(TAG, "内部repeat emit$index:收到的值  $value ")
            }
        })
    }
}
```

或者这样也不行

```kotlin
 lifecycleScope.launch {
    //只有第一个收集器有效
    eventBus.events.collect(object : FlowCollector<String> {
        override suspend fun emit(value: String) {
            Log.d(TAG, "emit1:  $value ")
        }
    })

    //这个不会执行
    eventBus.events.collect(object : FlowCollector<String> {
        override suspend fun emit(value: String) {
            Log.d(TAG, "emit2:  $value ")
        }
    })
}
```