原因就是 androidx.lifecycle 这里声明的 viewModelScope 的扩展。

话术：
1. viewModelScope 中创建的协程是 CloseableCoroutineScope ，实现了 Closeable 接口。
2. LifecycleOwner 到达 生命周期事件 ON_DESTROY 的时候，会调用 ViewModelStore 的 clear 方法。

```java

private final HashMap<String, ViewModel> mMap = new HashMap<>();
/**
     *  Clears internal storage and notifies ViewModels that they are no longer used.
     */
    public final void clear() {
        for (ViewModel vm : mMap.values()) {
            //调用 ViewModel 的 clear 方法
            vm.clear();
        }
        mMap.clear();
    }
```

3. ViewModelStore 的 clear 方法会调用 ViewModel 的 clear 方法。

```java
@MainThread
final void clear() {
    mCleared = true;
    // Since clear() is final, this method is still called on mock objects
    // and in those cases, mBagOfTags is null. It'll always be empty though
    // because setTagIfAbsent and getTag are not final so we can skip
    // clearing it
    if (mBagOfTags != null) {
        synchronized (mBagOfTags) {
            for (Object value : mBagOfTags.values()) {
                // see comment for the similar call in setTagIfAbsent
                //调用close方法
                closeWithRuntimeException(value);
            }
        }
    }
    onCleared();
}
```

```java
private static void closeWithRuntimeException(Object obj) {
    if (obj instanceof Closeable) {
        try {
            //关闭，启动的协程实现了 Closeable 接口。
            ((Closeable) obj).close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```


```kotlin
/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

private const val JOB_KEY = "androidx.lifecycle.ViewModelCoroutineScope.JOB_KEY"

/**
 * [CoroutineScope] tied to this [ViewModel].
 * This scope will be canceled when ViewModel will be cleared, i.e [ViewModel.onCleared] is called
 *
 * This scope is bound to
 * [Dispatchers.Main.immediate][kotlinx.coroutines.MainCoroutineDispatcher.immediate]
 */
public val ViewModel.viewModelScope: CoroutineScope
    get() {
        val scope: CoroutineScope? = this.getTag(JOB_KEY)
        if (scope != null) {
            return scope
        }
        
        return setTagIfAbsent(
            JOB_KEY,
            CloseableCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        )
    }

internal class CloseableCoroutineScope(context: CoroutineContext) : Closeable, CoroutineScope {
    override val coroutineContext: CoroutineContext = context

    override fun close() {
        coroutineContext.cancel()
    }
}

```