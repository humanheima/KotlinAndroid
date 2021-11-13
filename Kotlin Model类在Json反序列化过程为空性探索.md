定义一个JsonModel类。

### JsonModel类所有字段都声明为非空类型
```kotlin
data class JsonModel(
        var show: Boolean,
        var number: Int,
        var string: String
)
```

使用Gson类进行反序列化，Gson版本2.8.5

```kotlin
fun <T> toObject(json: String?, classOfT: Class<T>): T? {
    return try {
        gson.fromJson(json, classOfT)
    } catch (e: Exception) {
        Log.d(TAG, "toObject: error ${e.message}")
        null
    }
}
```

我们看一下反编译出来的Java类，省略不必要的部分。

```java
public final class JsonModel {

   private boolean show;
   private int number;
   @NotNull
   private String string;
   
   public JsonModel(boolean show, int number, @NotNull String string) {
         Intrinsics.checkParameterIsNotNull(string, "string");
         super();
         this.show = show;
         this.number = number;
         this.string = string;
   }

   public final boolean getShow() {
      return this.show;
   }

   public final void setShow(boolean var1) {
      this.show = var1;
   }

   public final int getNumber() {
      return this.number;
   }

   public final void setNumber(int var1) {
      this.number = var1;
   }

   @NotNull
   public final String getString() {
      return this.string;
   }

   public final void setString(@NotNull String var1) {
      Intrinsics.checkParameterIsNotNull(var1, "<set-?>");
      this.string = var1;
   }
}
```

Boolean类型和Int类型都被编译成了Java原始类型。

**这里注意一下**：Kotlin的Boolean、Byte、Short、Int、Long、Float、Double声明为非空类型的时候，最终反编译出来的Java类都会变成对应Java中原始类型：boolean、byte、short、int、long、float、double。而原始类型是都有默认值的，不会为null。

接下来开始探索：

完整的json字符串

```kotlin

val jsonString = "{\"show\": \"true\",\"number\": 10086,\"string\":\"hello world\"}"

```


#### json字符串中缺少Java中原始类型对应的字段

如果反序列化的Json字符串没有`show`字段和`number`字段，那么最后反序列化出来的JsonModel对象，`show = false `，`number = 0 `。

```kotlin

val jsonString = "{\"string\":\"hello world\"}"

```

ReflectiveTypeAdapterFactory.Adapter的read方法。

```java
@Override 
public T read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      T instance = constructor.construct();

      try {
        in.beginObject();
        //注释1处，循环判断是否还有下一个值需要处理。
        while (in.hasNext()) {
          String name = in.nextName();
          BoundField field = boundFields.get(name);
          if (field == null || !field.deserialized) {
            in.skipValue();
          } else {
            //注释2处，使用BoundField读字段值
            field.read(in, instance);
          }
        }
      } catch (IllegalStateException e) {
        throw new JsonSyntaxException(e);
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      }
      in.endObject();
      return instance;
}
```

注释1处，循环判断是否还有下一个值需要处理。处理完string字段以后，json字符串中就没有其他要处理的字段了，也就是说，在Json字符串没有`show`字段和`number`字段的时候，根本不会处理这两个字段，所以都是默认值，`show = false `，`number = 0 `。


#### json字符串中Java中原始类型对应的字段都为null。

如果反序列化的Json字符串`show`字段和`number`字段都为`null`，那么最后反序列化出来的JsonModel对象，`show = false `，`number = 0 `。

```kotlin

val jsonString1 = "{\"show\": null,\"number\":null,\"string\":\"hello world\"}"

```




ReflectiveTypeAdapterFactory.Adapter的read方法的注释2处，使用BoundField读取字段。


```java
private ReflectiveTypeAdapterFactory.BoundField createBoundField(
      final Gson context, final Field field, final String name,
      final TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
    final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
    // special casing primitives here saves ~5% on Android...
    JsonAdapter annotation = field.getAnnotation(JsonAdapter.class);
    TypeAdapter<?> mapped = null;
    if (annotation != null) {
      mapped = jsonAdapterFactory.getTypeAdapter(
          constructorConstructor, context, fieldType, annotation);
    }
    final boolean jsonAdapterPresent = mapped != null;
    if (mapped == null) mapped = context.getAdapter(fieldType);

    final TypeAdapter<?> typeAdapter = mapped;
    return new ReflectiveTypeAdapterFactory.BoundField(name, serialize, deserialize) {
      @SuppressWarnings({"unchecked", "rawtypes"}) // the type adapter and field type always agree
      @Override void write(JsonWriter writer, Object value)
          throws IOException, IllegalAccessException {
        Object fieldValue = field.get(value);
        TypeAdapter t = jsonAdapterPresent ? typeAdapter
            : new TypeAdapterRuntimeTypeWrapper(context, typeAdapter, fieldType.getType());
        t.write(writer, fieldValue);
      }
      @Override void read(JsonReader reader, Object value)
          throws IOException, IllegalAccessException {
        //注释1处
        Object fieldValue = typeAdapter.read(reader);
        if (fieldValue != null || !isPrimitive) {
          //注释2处
          field.set(value, fieldValue);
        }
      }
      @Override public boolean writeField(Object value) throws IOException, IllegalAccessException {
        if (!serialized) return false;
        Object fieldValue = field.get(value);
        return fieldValue != value; // avoid recursion for example for Throwable.cause
      }
    };
  }
```

注释1处，如果是boolean类型，对应的变量是TypeAdapters.BOOLEAN，如果值为null的话，TypeAdapters.BOOLEAN返回的值是null。如果是int类型，对应的变量是TypeAdapters.INTEGER，如果值为null的话，TypeAdapters.INTEGER返回的值是null。

注释2处，条件不满足，所以Java原始类型变量如果对应的json字符串为null的话，最终反序列化的结果就是默认值，`show = false`，`number = 0`。

```java
public static final TypeAdapter<Boolean> BOOLEAN = new TypeAdapter<Boolean>() {
    @Override
    public Boolean read(JsonReader in) throws IOException {
      JsonToken peek = in.peek();
      //注释1处，boolean类型变量，如果从json字符串中读取的值是null，返回null
      if (peek == JsonToken.NULL) {
        in.nextNull();
        return null;
      } else if (peek == JsonToken.STRING) {
        // support strings for compatibility with GSON 1.7
        return Boolean.parseBoolean(in.nextString());
      }
      return in.nextBoolean();
    }
    @Override
    public void write(JsonWriter out, Boolean value) throws IOException {
      out.value(value);
    }
};
```

注释1处，boolean类型变量，如果从json字符串中读取的值是null，返回null


int类型的适配器同理，如果从json字符串中读取的值是null，返回null，那么int类型的变量默认值就是0。

```java
public static final TypeAdapter<Number> INTEGER = new TypeAdapter<Number>() {
    @Override
    public Number read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      try {
        return in.nextInt();
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      }
    }
    @Override
    public void write(JsonWriter out, Number value) throws IOException {
      out.value(value);
    }
  };
```

#### Json字符串中引用类型缺失

如果反序列化的Json字符串`string`字段缺失，那么在反序列化过程中就不会处理`string`字段，那么`string`字段就是默认值，在这个例子中我们没有给`string`字段赋默认值，所以默认值就是null，那么最后反序列化出来的JsonModel对象，`string = null `。

注意：
注意：
注意：

如果我们如下所示，声明JsonModel类，给string字段默认赋值为"你好呀"。
```kotlin
data class JsonModel(
        var show: Boolean,
        var number: Int,
        var string: String="你好呀"
)
```
反编译后的Java类，省略无关部分。

```java
public final class JsonModel {
   private boolean show;
   private int number;
   @NotNull
   private String string;

   public JsonModel(boolean show, int number, @NotNull String string) {
      Intrinsics.checkParameterIsNotNull(string, "string");
      super();
      this.show = show;
      this.number = number;
      this.string = string;
   }

   // $FF: synthetic method
   public JsonModel(boolean var1, int var2, String var3, int var4, DefaultConstructorMarker var5) {
      if ((var4 & 4) != 0) {
         var3 = "你好呀";
      }

      this(var1, var2, var3);
   }

}
```

我们看到，JsonModel类没有默认的`无参构造函数`。并且只有当调用JsonModel三个参数的构造函数的时候，才会给string字段赋值。


当反序列化的Json字符串`string`字段缺失，反序列化后string字段会默认是"你好呀"吗？并不是。Gson在反序列化过程中要么通过调用`无参构造函数`来构造对象，或者通过`UnsafeAllocator`类，在不调用构造函数的情况下地分配对象。

所以如上声明方式，即使给string字段默认赋值为"你好呀"。在Json字符串string字段缺失的情况下，反序列化之后，string字段值依然为null。这里一定要注意！！！



#### json字符串中引用类型为null

如果反序列化的Json字符串`string`字段为`null`，那么最后反序列化出来的JsonModel对象，`string = null `。

TypeAdapters.STRING

```java
 public static final TypeAdapter<String> STRING = new TypeAdapter<String>() {
    @Override
    public String read(JsonReader in) throws IOException {
      JsonToken peek = in.peek();
      if (peek == JsonToken.NULL) {
        //注释1处，值为null，返回null
        in.nextNull();
        return null;
      }
      /* coerce booleans to strings for backwards compatibility */
      if (peek == JsonToken.BOOLEAN) {
        return Boolean.toString(in.nextBoolean());
      }
      return in.nextString();
    }
    @Override
    public void write(JsonWriter out, String value) throws IOException {
      out.value(value);
    }
  };
```

注释1处，值为null，返回null。

也就是说，对于一个引用类型的变量，如果Json字符串中该变量对应的值为null，那么反序列化出来的引用类型变量的值就是null。注意：并且会覆盖该变量的默认值。在这个例子中，我们如果在声明的时候为`string`字符指定一个默认值，但是当`json`字符串中`string`字段对应的值为`null`的时候，最后序列化出来的结果仍然为`null`。

所以正确的做法是把引用类型的变量声明为可空类型。如下所示：

```kotlin
data class JsonModel(
        var show: Boolean,
        var number: Int,
        var string: String?
)
```

### 把Java中对应的原始类型声明为可空类型

```kotlin
data class JsonModel(
        var show: Boolean?,
        var number: Int?,
        var string: String?
)
```

反编译出来的Java类，对应的原始类型都变成了相应的包装类，默认值都是null。所以使用的时候要注意判断是否为null。

```java
public final class JsonModel {
   @Nullable
   private Boolean show;
   @Nullable
   private Integer number;
   @Nullable
   private String string;

   @Nullable
   public final Boolean getShow() {
      return this.show;
   }

   public final void setShow(@Nullable Boolean var1) {
      this.show = var1;
   }

   @Nullable
   public final Integer getNumber() {
      return this.number;
   }

   public final void setNumber(@Nullable Integer var1) {
      this.number = var1;
   }

   @Nullable
   public final String getString() {
      return this.string;
   }

   public final void setString(@Nullable String var1) {
      this.string = var1;
   }

   public JsonModel(@Nullable Boolean show, @Nullable Integer number, @Nullable String string) {
      this.show = show;
      this.number = number;
      this.string = string;
   }
}
```

1. 如果反序列化的Json字符串没有`show`字段和`number`字段，那么最后反序列化出来的JsonModel对象，`show = null `，`number = null `。

2. 如果反序列化的Json字符串`show`字段和`number`字段都为`null`，那么最后反序列化出来的JsonModel对象，`show = null `，`number = null `。

这种声明类型是不合适的，将可以不为null的Java基本数据类型，变为了可空的包装类型，使用的时候会增加空判断的逻辑。


