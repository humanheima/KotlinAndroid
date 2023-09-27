package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_exception_test.*
import kotlinx.coroutines.*

/**
 * Created by dumingwei on 2020/9/21
 *
 * Desc: 这篇文章的测试例子
 * https://mp.weixin.qq.com/s?__biz=MzAwODY4OTk2Mg==&mid=2652060229&idx=1&sn=38b67881237ee411645c42248b9be2d4&chksm=808c9a00b7fb131624f169dc3c2b958e44980ab7118e97539b3ec42611fe3d1e72a277e1bad5&scene=178#rd}
 *
 */
class ExceptionTestActivity : AppCompatActivity() {


    private val TAG: String = "ExceptionTestActivity"

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, ExceptionTestActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exception_test)

        btnTest1.setOnClickListener {
            test1()
        }

        btnTest1Revolution.setOnClickListener {
            test1Revolution()
        }

        btnTest2.setOnClickListener {
            test2()
        }

        btnTest2Revolution.setOnClickListener {
            test2Revolution()
        }

        btnTestParentJob.setOnClickListener {
            testParentJob()
        }

        btnTestAsyncThrow.setOnClickListener {
            testAsyncBlockThrow()
        }
    }

    /**
     * 使用监督任务，在这个示例中如果 Child 1 失败了，无论是 scope 还是 Child 2 都会被取消。
     */
    private fun test1() {
        val scope = CoroutineScope(SupervisorJob())

        scope.launch {
            // Child 1
            delay(100)
            throw IllegalStateException("Child1 failed")
        }

        scope.launch {
            // Child 2
            delay(2000)

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

        scope.launch() {

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
     * 使用监督作用域，解决方案，每个子协程自己处理异常
     */
    private fun test2Revolution() {
        val scope = CoroutineScope(Job())

        scope.launch {

            supervisorScope {
                launch {
                    // Child 1
                    try {
                        delay(100)
                        throw IllegalStateException("Child1 failed")
                    } catch (e: Exception) {
                        Log.e(TAG, "test2: caught exception ${e.message}")
                    }
                }

                launch {
                    // Child 2
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

        val job = scope.launch(SupervisorJob()) {
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

            Log.e(TAG, "testParentJob: parent$isActive")

        }
    }

    /**
     * 测试async不调用wait，async代码块中是否会抛出异常
     */
    private fun testAsyncBlockThrow() {
        val scope = CoroutineScope(SupervisorJob())

        scope.launch {
            coroutineScope {
                try {
                    val deferred = async {
                        codeThatCanThrowExceptions()
                    }
                    //deferred.await()
                } catch (e: Exception) {
                    // Exception thrown in async WILL NOT be caught here
                    // but propagated up to the scope
                }
            }
        }

    }

    private fun codeThatCanThrowExceptions() {
        throw  java.lang.IllegalStateException("codeThatCanThrowExceptions")
    }


}
