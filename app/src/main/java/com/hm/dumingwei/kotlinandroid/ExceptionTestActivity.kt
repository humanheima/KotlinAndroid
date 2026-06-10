package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hm.dumingwei.kotlinandroid.databinding.ActivityExceptionTestBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.time.Duration.Companion.milliseconds

/**
 * Created by dumingwei on 2020/9/21
 *
 * Desc: 这篇文章的测试例子
 * https://mp.weixin.qq.com/s?__biz=MzAwODY4OTk2Mg==&mid=2652060229&idx=1&sn=38b67881237ee411645c42248b9be2d4&chksm=808c9a00b7fb131624f169dc3c2b958e44980ab7118e97539b3ec42611fe3d1e72a277e1bad5&scene=178#rd}
 *
 * 参考  Kotlin协程异常处理.md
 */
class ExceptionTestActivity : AppCompatActivity() {


    private val TAG: String = "ExceptionTestActivity"

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, ExceptionTestActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityExceptionTestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExceptionTestBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnTest0.setOnClickListener {
            test0()
        }

        binding.btnTest1.setOnClickListener {
            test1()
        }

        binding.btnTest1Revolution.setOnClickListener {
            test1Revolution()
        }

        binding.btnTest2.setOnClickListener {
            test2()
        }

        binding.btnTest2Revolution.setOnClickListener {
            test2Revolution()
        }

        binding.btnTestParentJob.setOnClickListener {
            testParentJob()
        }

        binding.btnTestAsyncThrow.setOnClickListener {
            testAsyncBlockThrow()
        }
    }

    private fun test0() {
        lifecycleScope.launch {
            /**
             * 在外层 launch {} 外围 try/catch）捕获不到内层 launch {} 的异常；
             * 因为子协程异步执行，异常不会回到那段同步的 try/catch。
             */
//            try {
//                launch {
//                    delay(100)
//                    throw IllegalStateException("Child1 failed")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "test0: " + e.message)
//            }

            //异常可以捕获
            launch {
                try {
                    delay(100)
                    throw IllegalStateException("Child1 failed")
                } catch (e: Exception) {
                    Log.e(TAG, "test0: " + e.message)
                    toastSHortly("抓住了异常，test0,${e.message}")
                }
            }
        }
    }

    /**
     * 使用监督任务，在这个示例中如果 Child 1 失败了，无论是 scope 还是 Child 2 都会被取消。
     *
     * SupervisorJob 阻止的是「异常导致兄弟协程被取消」，但不阻止「未处理异常导致 App 崩溃」。
     * 要让 test1 不崩，必须额外提供异常处理：要么每个子协程自己 try/catch（即 test1Revolution），要么给 scope 装一个 CoroutineExceptionHandler。
     */
    private fun test1() {
        val scope = CoroutineScope(SupervisorJob())

        scope.launch {
            // Child 1
            delay(100.milliseconds)
            throw IllegalStateException("Child1 failed")
        }

        scope.launch {
            // Child 2
            delay(2000.milliseconds)

            Log.e(TAG, "test1: Child finished")
        }
    }

    /**
     * 使用监督任务，解决方案，每个子协程自己处理异常
     */
    private fun test1Revolution() {
        val scope = CoroutineScope(SupervisorJob())

        scope.launch {
            // Child 1
            try {
                delay(100)
                throw IllegalStateException("Child1 failed")
            } catch (e: Exception) {
                Log.e(TAG, "test1: Child1 caught exception ${e.message}")

            }
        }

        scope.launch {
            // Child 2
            delay(2000)

            Log.e(TAG, "test1: Child2 finished")
        }
    }

    /**
     * 使用监督作用域
     */
    private fun test2() {
        val scope = CoroutineScope(Job())

        scope.launch {

            supervisorScope {
                launch {
                    // Child 1
                    delay(100)
                    throw IllegalStateException("Child1 failed")
                }

                launch {
                    // Child 2
                    delay(2000)

                    Log.e(TAG, "test2: Child finished")
                }
            }
        }
    }

    /**
     * 使用监督作用域，对比版：让 Child 1 真的失败（异常未被自己捕获），
     * 通过 CoroutineExceptionHandler 处理它，从而体现 supervisorScope 的隔离作用：
     * Child 1 失败既不会让 App crash，也不会取消兄弟协程 Child 2，Child 2 依然完成。
     */
    private fun test2Revolution() {
        val handler = CoroutineExceptionHandler { _, e ->
            Log.e(TAG, "test2: handler caught ${e.message}")
        }
        val scope = CoroutineScope(Job())

        scope.launch {

            supervisorScope {
                launch(handler) {
                    // Child 1 失败，但被 handler 处理，且不影响 Child 2
                    delay(100)
                    throw IllegalStateException("Child1 failed")
                }

                launch {
                    // Child 2 依然完成 —— 这才体现 supervisorScope 的隔离作用
                    delay(2000)

                    Log.e(TAG, "test2: Child2 finished")
                }
            }
        }
    }

    /**
     * 给您下面一段代码，您能指出 Child 1 是用哪种 Job 作为父级的吗？
     * Child 1 的父级 Job 就只是 Job 类型，SupervisorJob 在这段代码中完全没用！
     *
     * 这个例子还不是太明白
     */
    private fun testParentJob() {
        val scope = CoroutineScope(Job())

        scope.launch(SupervisorJob()) {
            launch {
                // Child 1
                delay(100)

                //codeThatCanThrowExceptions()

                Log.e(TAG, "testParentJob: Child finished")
            }

            launch {
                // Child 2
                delay(2000)

                Log.e(TAG, "testParentJob: Child finished")
            }

            Log.e(TAG, "testParentJob: parent isActive = $isActive")

        }
    }

    /**
     * 测试async不调用wait，async代码块中是否会抛出异常
     */
    private fun testAsyncBlockThrow() {
        val scope = CoroutineScope(SupervisorJob())

        scope.launch {
            try {
                val deferred = async {
                    throw java.lang.IllegalStateException("codeThatCanThrowExceptions")
                }
                // TODO: 如果不调用 await，async代码块中是否会抛出异常，没有人处理，最终导致crash
                deferred.await()
            } catch (e: Exception) {
                Log.e(TAG, "testAsyncBlockThrow: ")
            }
        }
    }


}
