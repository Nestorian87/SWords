package com.nestor87.swords;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface SWordsApi {
    @Headers("Authorization: Bearer " + MainActivity.accountManagerPassword)
    @GET("/SWordsApi/user.php")
    Call<List<Player>> getAllUsers();

    @Headers("Authorization: Bearer " + MainActivity.accountManagerPassword)
    @PUT("/SWordsApi/user.php")
    Call<Void> registerPlayer(@Body Player player);

    @Headers("Authorization: Bearer " + MainActivity.accountManagerPassword)
    @PATCH("/SWordsApi/user.php")
    Call<Void> updateUser(@Body Player player);

    @Headers("Authorization: Bearer " + MainActivity.accountManagerPassword)
    @GET("/SWordsApi/meaning.php")
    Call<Word> getWordMeaning(@Query("word") String word);

    @Headers("Authorization: Bearer " + MainActivity.accountManagerPassword)
    @PUT("/SWordsApi/addWord.php")
    Call<Void> addWordRequest(@Body HashMap<String, String> body);

    @Headers("Authorization: Bearer " + MainActivity.accountManagerPassword)
    @POST("/SWordsApi/user.php")
    Call<Void> setStatusOnline(@Body Player player);

    @Headers("Authorization: Bearer " + MainActivity.accountManagerPassword)
    @POST("/SWordsApi/version.php")
    Call<VersionInfo> getLatestAppVersion();
}
