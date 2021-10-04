package com.nestor87.swords.data.network;

import com.nestor87.swords.data.models.VersionInfo;
import com.nestor87.swords.data.models.Word;
import com.nestor87.swords.data.models.Player;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface SWordsApi {
    @GET("/SWordsApi/user.php")
    Call<List<Player>> getAllUsers(@Header("Authorization") String bearerToken);

    @PUT("/SWordsApi/user.php")
    Call<Void> registerPlayer(@Header("Authorization") String bearerToken, @Body Player player);

    @PATCH("/SWordsApi/user.php")
    Call<Void> updateUser(@Header("Authorization") String bearerToken, @Body Player player);

    @GET("/SWordsApi/meaning.php")
    Call<Word> getWordMeaning(@Header("Authorization") String bearerToken, @Query("word") String word);

    @PUT("/SWordsApi/addWord.php")
    Call<Void> addWordRequest(@Header("Authorization") String bearerToken, @Body HashMap<String, String> body);

    @POST("/SWordsApi/user.php")
    Call<Void> setStatusOnline(@Header("Authorization") String bearerToken, @Body Player player);

    @POST("/SWordsApi/version.php")
    Call<VersionInfo> getLatestAppVersion(@Header("Authorization") String bearerToken);
}
