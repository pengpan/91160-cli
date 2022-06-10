package com.github.pengpan.common.retrofit;

import cn.hutool.core.lang.Assert;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author pengpan
 */
public final class ResponseCallAdapterFactory extends CallAdapter.Factory {

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (Response.class.isAssignableFrom(getRawType(returnType))) {
            return new ResponseCallAdapter(returnType);
        }
        return null;
    }

    final class ResponseCallAdapter<R> implements CallAdapter<R, Response<R>> {

        private Type returnType;

        ResponseCallAdapter(Type returnType) {
            this.returnType = returnType;
        }

        @Override
        public Type responseType() {
            ParameterizedType parameterizedType = (ParameterizedType) returnType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Assert.notEmpty(actualTypeArguments, "Response must specify generic parameters!");
            return actualTypeArguments[0];
        }


        @Override
        public Response<R> adapt(Call<R> call) {
            try {
                return call.execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

