package com.coal.projects.chat.domain;

import android.support.annotation.NonNull;
import com.coal.projects.chat.creation.ChatInstance;
import com.coal.projects.chat.data.FirebaseRepository;
import com.coal.projects.chat.data.PushApi;
import com.coal.projects.chat.data.SendPushAnswer;
import com.coal.projects.chat.data.push.PushBody;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PushManager implements PushApi {
    private static final String BASE_URL = "https://fcm.googleapis.com/fcm/";
    private final FirebaseRepository firebaseRepository;

    private final PushApi pushApi;
    private Disposable disposable;
    private String displayName;
    private String SERVER_KEY;

    public PushManager(FirebaseRepository firebaseRepository, String serverKey) {
        this.firebaseRepository = firebaseRepository;
        pushApi = restApiService().create(PushApi.class);
        SERVER_KEY = serverKey;
    }

    private Retrofit restApiService() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(
                        new OkHttpClient
                                .Builder()
                                .writeTimeout(20, TimeUnit.SECONDS)
                                .connectTimeout(20, TimeUnit.SECONDS)
                                .readTimeout(20, TimeUnit.SECONDS)
                                .addInterceptor(httpLoggingInterceptor)
                                .addInterceptor(new AddCookiesInterceptor())
                                .build()
                )
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public void sendPush(String message, String chatId) {
        displayName = firebaseRepository.getChatUser().getDisplayName();
        disposable = firebaseRepository
                //Получаем все логины в чате
                .getLogins(chatId)
                //Перебираем по 1 логину
                .map(Observable::fromIterable)
                .flatMap(observable -> observable)
                //Если встретился логин текущего юзера, отсеиваем
                .filter(login -> !login.equals(firebaseRepository.getChatUser().getLogin()))
                //Получаем токен, связанный с логином
                .flatMap(firebaseRepository::getToken)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                //Отправляем пуш
                .flatMap(token -> sendPush(PushBody.create(token, message, chatId, displayName)))
                .subscribe(this::whenPushSend, this::failure);
    }

    private void whenPushSend(SendPushAnswer sendPushAnswer) {

    }

    private void failure(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public Observable<SendPushAnswer> sendPush(PushBody body) {
        return pushApi.sendPush(body);
    }

    private class AddCookiesInterceptor implements Interceptor {

        @Override
        @SuppressWarnings("all")
        public Response intercept(@NonNull Interceptor.Chain chain) throws IOException {
            Request.Builder builder = chain.request().newBuilder();

            builder.addHeader("Content-Type", "application/json");
            builder.addHeader("Authorization", "key=" + SERVER_KEY);

            final Response response = chain.proceed(builder.build());

            return response;
        }
    }
}
