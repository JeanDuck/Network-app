package rest;

import android.app.Activity;
import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ApiClient extends Activity {
    public static final  String BASE_URL="http://10.0.2.2/friendster/public/app/";
    public static final  String BASE_URL_1="http://10.0.2.2/friendster/public/";

    //use your own ip address for testing in mobile
    //public static final  String BASE_URL="http://192.168.0.104/friendster/public/app/";
    private static Retrofit retrofit=null;


    public static Retrofit getApiClient(){
        //Http记录拦截器，用于记录应用中的网络 请求的信息
        HttpLoggingInterceptor httpLoggingInterceptor=new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient=new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true)
                .build();

        Log.d(TAG, "six test");



        if(retrofit==null){
            ////新建一个Retrofit对象
            retrofit=new Retrofit.Builder().baseUrl(BASE_URL)//创建Retrofit实例时需要通过Retrofit.Builder,并调用baseUrl方法设置URL
                    .client(httpClient)//其中client(mClient)这个方法指定一个OkHttpClient客户端作为请求的执行器，需要传入一个OkHttpClient对象作为参数
                    .addConverterFactory(new NullOnEmptyConverterFactory())
                    .addConverterFactory(GsonConverterFactory.create())//GsonConverterFactory作为解析Json数据的Converter
                    .build();

        }
        Log.d(TAG, "seven test");
        return retrofit;

    }
    public static class NullOnEmptyConverterFactory extends Converter.Factory {
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
            return new Converter<ResponseBody, Object>() {
                @Override
                public Object convert(ResponseBody body) throws IOException {
                    long contentLength = body.contentLength();
                    if (contentLength == 0) {
                        return null;
                    }
                    return delegate.convert(body);
                }
            };
        }
    }


}
