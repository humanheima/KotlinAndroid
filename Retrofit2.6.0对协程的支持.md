### Version 2.6.0 (2019-06-05)

比我们以前使用Retrofit时候的方法声明
只使用Retrofit
```
@GET("users/{id}")
fun user(@Path("id") id: Long): Call<User>
```
使用Retrofit+RxJava
```
@GET("users/{id}")
fun user(@Path("id") id: Long): Observable<User>
```

* 新特性 为Kotlin提供了`suspend`修饰符用在函数上。这允许您以一种惯用的方式来表示HTTP请求的异步性。
```
@GET("users/{id}")
suspend fun user(@Path("id") id: Long): User
```

使用
```kotlin
scope?.launch {
        try {

            val events: User = coroutineAPIService.user(1L)
                
        } catch (e: Exception) {
          //do nothing
        }
}
```