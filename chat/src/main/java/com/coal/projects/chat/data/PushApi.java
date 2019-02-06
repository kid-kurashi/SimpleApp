package com.coal.projects.chat.data;

import com.coal.projects.chat.data.push.PushBody;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public interface PushApi {
    @POST("send")
    Observable<SendPushAnswer> sendPush(@Body PushBody body);
}
