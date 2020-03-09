Kotlin的android扩展：对findViewById说再见

首先要在应用的build文件中引入kotlin扩展插件，现在Android Studio应该会为我们自动引入。

```
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-android-extensions'
```
### Activity中使用

布局文件
```xml
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".findviewbyid.FindViewByIdActivity">

    <Button
        android:id="@+id/btn"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="button"
        android:textAllCaps="false"
        app:layout_constraintTop_toTopOf="parent" />

</android.support.constraint.ConstraintLayout>
```
Activity代码
```kotlin
class FindViewByIdActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_view_by_id)

        btn.setOnClickListener {
            // do nothing
        }
    }
}

```
查看反编译Kotlin字节码生成的java类
```java
public final class FindViewByIdActivity extends AppCompatActivity {

   private HashMap _$_findViewCache;

   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      this.setContentView(-1300032);
      ((Button)this._$_findCachedViewById(id.btn)).setOnClickListener((OnClickListener)null.INSTANCE);
   }

   //注释1处
   public View _$_findCachedViewById(int var1) {
      if (this._$_findViewCache == null) {
         this._$_findViewCache = new HashMap();
      }
        
      View var2 = (View)this._$_findViewCache.get(var1);
      if (var2 == null) {
         //调用findViewById
         var2 = this.findViewById(var1);
         this._$_findViewCache.put(var1, var2);
      }

      return var2;
   }

   public void _$_clearFindViewByIdCache() {
      if (this._$_findViewCache != null) {
         this._$_findViewCache.clear();
      }

   }
   
}
```
注释1处，还是调用了Activity的`findViewById`方法，并且当找到View以后帮我们缓存在了HashMap中了。

### Fragment中使用

布局文件
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".findviewbyid.FindViewByIdFragment">

    <TextView
        android:id="@+id/tvInFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:text="@string/hello_blank_fragment" />

</FrameLayout>
```

Fragment代码
```kotlin
class FindViewByIdFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        //tvInFragment.text = "fragment中的字符串"
        return inflater.inflate(R.layout.fragment_find_view_by_id, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //注释1处
        tvInFragment.text = "fragment中的字符串"
    }

    companion object {

        @JvmStatic
        fun newInstance() = FindViewByIdFragment()
    }
}


```
查看反编译的Kotlin字节码
```java
public final class FindViewByIdFragment extends Fragment {
   public static final FindViewByIdFragment.Companion Companion = new FindViewByIdFragment.Companion((DefaultConstructorMarker)null);
   private HashMap _$_findViewCache;

   @Nullable
   public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      Intrinsics.checkParameterIsNotNull(inflater, "inflater");
      return inflater.inflate(-1300006, container, false);
   }

   public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
      Intrinsics.checkParameterIsNotNull(view, "view");
      super.onViewCreated(view, savedInstanceState);
      //注释1处
      TextView var10000 = (TextView)this._$_findCachedViewById(id.tvInFragment);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "tvInFragment");
      var10000.setText((CharSequence)"fragment中的字符串");
   }

   public View _$_findCachedViewById(int var1) {
      if (this._$_findViewCache == null) {
         this._$_findViewCache = new HashMap();
      }

      View var2 = (View)this._$_findViewCache.get(var1);
      if (var2 == null) {
         //注释2处 
         View var10000 = this.getView();
         if (var10000 == null) {
            return null;
         }
         //注释3处   
         var2 = var10000.findViewById(var1);
         this._$_findViewCache.put(var1, var2);
      }

      return var2;
   }

   public void _$_clearFindViewByIdCache() {
      if (this._$_findViewCache != null) {
         this._$_findViewCache.clear();
      }

   }

   // $FF: synthetic method
   public void onDestroyView() {
      super.onDestroyView();
      //注释4处
      this._$_clearFindViewByIdCache();
   }
   
}
```
在fragment使用要注意一点，需要在`onViewCreated`获取控件。不能在`onCreateView`中获取控件。

我们可以看到注释2处，获取`mView`，`mView`就是`onCreateView`方法返回的View。
```java
public View getView() {
    return mView;
}
```

注释3处，我们是使用`mView`来获取控件。在`onCreateView`方法没有返回的时候`mView`还是null。所以不能在`onCreateView`中获取控件。

注释4处，当`onDestroyView`的时候，清空了所有缓存的View。

### 在自定义View中使用


### 实验功能


开启实验功能，在应用的build.gradle
```
androidExtensions {
    experimental = true
}
```
#### 布局容器支持（LayoutContainer support）
Kotlin的Android扩展插件支持不同类型的容器，基本的容器像Activity、Fragment、View。但是实际上你可以通过实现`LayoutContainer`接口，将任何一个转化成
Android扩展容器。

以RecyclerView的ViewHolder为例

```
class ViewHolder(view: View) : RecyclerView.ViewHolder(view), LayoutContainer {
    //给containerView赋值
    override val containerView: View = itemView
}
```
LayoutContainer接口
```
public interface LayoutContainer {
    /** Returns the root holder view. */
    public val containerView: View?
}

```
在RecyclerView.Adapter中使用
```
override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    //原来的使用方式
    //holder.itemView.tvInRv.text = data[position]
    //启用LayoutContainer扩展功能后的使用方式
    holder.tvInRv.text = data[position]
    holder.itemView.setOnClickListener {
        Toast.makeText(context, "点击了第$position 项", Toast.LENGTH_SHORT).show()
    }
}
```
查看以一下反编译的Kotlin字节码
```java
public void onBindViewHolder(@NotNull MyAdapter.ViewHolder holder, final int position) {
   Intrinsics.checkParameterIsNotNull(holder, "holder");
   //注释1处
   TextView var10000 = (TextView)holder._$_findCachedViewById(id.tvInRv);
   Intrinsics.checkExpressionValueIsNotNull(var10000, "holder.tvInRv");
   var10000.setText((CharSequence)this.data.get(position));
   holder.itemView.setOnClickListener((OnClickListener)(new OnClickListener() {
      public final void onClick(View it) {
         Toast.makeText(MyAdapter.this.getContext(), (CharSequence)("点击了第" + position + " 项"), 0).show();
      }
   }));
}
```
注释1处，调用holder的`_$_findCachedViewById`方法

```java
public static final class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder implements LayoutContainer {
   
    @NotNull
     private final View containerView;
    //缓存
     private HashMap _$_findViewCache;

     //获取containerView
     @NotNull
     public View getContainerView() { return this.containerView;
     }

     public ViewHolder(@NotNull View view) {
         Intrinsics.checkParameterIsNotNull(view, "view");
         super(view);
         View var10001 = this.itemView;
         Intrinsics.checkExpressionValueIsNotNull(var10001, "itemView");
         //将itemView赋值给containerView
         this.containerView = var10001;
     }

     public View _$_findCachedViewById(int var1) {
         if (this._$_findViewCache == null) {
            this._$_findViewCache = new HashMap();
         }

         View var2 = (View)this._$_findViewCache.get(var1);
         if (var2 == null) {
            //获取containerView即itemView
            View var10000 = this.getContainerView();
            if (var10000 == null) {
               return null;
            }
            //使用itemView获取控件
            var2 = var10000.findViewById(var1);
            this._$_findViewCache.put(var1, var2);
         }

         return var2;
     }

     public void _$_clearFindViewByIdCache() {
         if (this._$_findViewCache != null) {
            this._$_findViewCache.clear();
         }

     }
}
```



