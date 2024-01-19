Retrofit query 参数问题：
1. 如果传递的是null，，最终的url不会带上去。
2. 如果传递的参数是空字符串（对于String类型参数来说）参数会带上，但是参数值是空字符串。
3. 非空参数正常携带

https://www.wanandroid.com/wxarticle/chapters/json?params1=&params2=1


ApiService.kt 声明的方法

```kotlin
 @GET("wxarticle/chapters/json")
    suspend fun getWxarticle2Temp(
        @Query("params1") params1: String?,
        @Query("params2") params2: Int?,
        @Query("params3") params3: String?,
    ): WxArticleResponse
```

调用

```kotlin
private fun coroutineRequest2_6() {
        launch(exceptionHandler) {
            val response = apiService.getWxarticle2Temp("", 1, null)
            val sb = StringBuilder("Retrofit2.6配合协程请求：\n")
            response.data.forEach { sb.append(it.name).append("\n") }
            tvResult.text = sb.toString()
        }
    }
```

最终的url `https://www.wanandroid.com/wxarticle/chapters/json?params2=1` 。


### 源码分析
ParameterHandler.Query 类 解析 query参数
```java
static final class Query<T> extends ParameterHandler<T> {
    private final String name;
    private final Converter<T, String> valueConverter;
    private final boolean encoded;

    Query(String name, Converter<T, String> valueConverter, boolean encoded) {
      this.name = Objects.requireNonNull(name, "name == null");
      this.valueConverter = valueConverter;
      this.encoded = encoded;
    }

    @Override
    void apply(RequestBuilder builder, @Nullable T value) throws IOException {
      //注释1处，如果值为null，直接返回，不会添加到url中
      if (value == null) return; // Skip null values.

      String queryValue = valueConverter.convert(value);
      //注释2处，如果值为null，直接返回，不会添加到url中
      if (queryValue == null) return; // Skip converted but null values
      //注释3处，如果值为""，会添加到url中，但是值是空字符串 ，正常值也会添加到url中  
      builder.addQueryParam(name, queryValue, encoded);
    }
  }
```