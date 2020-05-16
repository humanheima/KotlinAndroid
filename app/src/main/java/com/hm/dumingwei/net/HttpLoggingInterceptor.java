package com.hm.dumingwei.net;


import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;


/**
 * 参考自官方3.11.0版本的HttpLoggingInterceptor
 */
public final class HttpLoggingInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final String TAG = "HttpLoggingInterceptor";

    public enum Level {
        /**
         * No logs.
         */
        NONE,
        BODY
    }

    private volatile Level level = Level.NONE;


    public HttpLoggingInterceptor(Level level) {
        this.level = level;
    }

    /**
     * Change the level at which this interceptor logs.
     */
    public HttpLoggingInterceptor setLevel(Level level) {
        if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
        this.level = level;
        return this;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Level level = this.level;

        Request request = chain.request();
        //日志级别为NODE，则不进行处理
        if (level == Level.NONE) {
            return chain.proceed(request);
        }

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        /*
         * 处理打印请求信息
         */
        StringBuilder requestInfoBuilder = new StringBuilder("Request: ");
        requestInfoBuilder.append(request.url());
        requestInfoBuilder.append("\n");

        if (hasRequestBody && !bodyHasUnknownEncoding(request.headers())) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);

            if (isPlaintext(buffer)) {
                requestInfoBuilder.append("{");
                requestInfoBuilder.append(buffer.readString(UTF8));
                requestInfoBuilder.append("}");
            } else {
                requestInfoBuilder.append(" binary request body omitted");
            }
        }

        Log.d(TAG, requestInfoBuilder.toString());
        /*
         * 处理打印响应信息
         */
        StringBuilder responseInfoBuilder = new StringBuilder("Response: ");
        responseInfoBuilder.append(request.url());

        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            //出现异常就将异常重新抛出，不要把异常吃掉
            Log.d(TAG, "intercept " + request.url() + "出现异常：" + e.getMessage());
            throw e;
        }

        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            long contentLength = responseBody.contentLength();
            if (!bodyHasUnknownEncoding(response.headers())) {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.buffer();

                if (!isPlaintext(buffer)) {
                    //如果响应不是字符串的话，直接返回响应
                    return response;
                }
                if (contentLength != 0) {
                    responseInfoBuilder.append("\n");
                    responseInfoBuilder.append(buffer.clone().readString(UTF8));
                }
            }
        }

        sliceLog(TAG, responseInfoBuilder.toString());

        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null
                && !contentEncoding.equalsIgnoreCase("identity")
                && !contentEncoding.equalsIgnoreCase("gzip");
    }

    /**
     * 分片打印日志
     *
     * @param tag tag
     * @param msg 日志信息
     */
    private void sliceLog(String tag, String msg) {
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，把4*1024的MAX字节打印长度改为2001字符数
        //UTF-8中，一个汉字占 1-4字节，根据存储大小选择。常用的中文基本都是3个字节
        int maxStrLength = 2001 - tag.length();
        boolean printTag = true;
        //只打印一次tag
        if (msg.length() > maxStrLength) {
            Log.d(tag, msg.substring(0, maxStrLength));
            printTag = false;
            msg = msg.substring(maxStrLength);
        }
        maxStrLength = 2001;

        while (msg.length() > maxStrLength) {
            Log.d("", msg.substring(0, maxStrLength));
            msg = msg.substring(maxStrLength);
        }
        //剩余部分
        if (printTag) {
            Log.d(tag, msg);
        } else {
            Log.d("", msg);
        }
    }
}
