```kotlin
data class JsonModel(
        var show: Boolean,
        var number: Int,
        var string: String
)

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

1. 如果反序列化的Json字符串没有`show`字段和`number`字段，那么最后反序列化出来的JsonModel对象，`show = false `，`number = 0 `。

Json字符串没有`show`字段和`number`字段，那么在反序列化的时候就不会处理这两个字段。

ReflectiveTypeAdapterFactory.Adapter

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
        while (in.hasNext()) {
          String name = in.nextName();
          //注释1处，boundFields中就没有`show`字段和`number`字段，所以不会处理。
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




2. 如果反序列化的Json字符串`show`字段和`number`字段都为`null`，那么最后反序列化出来的JsonModel对象，`show = false `，`number = 0 `。


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

注释1处，如果是boolean类型，对应的变量是TypeAdapters.BOOLEAN，如果值为null的话，TypeAdapters.BOOLEAN返回的值是null。

注释2处，条件不满足，所以boolean类型变量如果对应的json字符串为null的话，最终反序列化的结果是false。

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

3. 如果反序列化的Json字符串`string`字段缺失，或者为`null`，那么最后反序列化出来的JsonModel对象，`string = false `。
也就是说，对于一个引用类型的变量，如果Json字符串中没有改变量对应的值，或者值为null，那么反序列化出来的引用类型变量的值就是null。
所以我们应该把引用类型的变量声明为可空类型。如下所示：

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

