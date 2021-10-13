package com.nestor87.swords.data.network;

import com.nestor87.swords.data.models.UserRankResponse;
import com.nestor87.swords.data.models.UsernameAvailabilityResponse;
import com.nestor87.swords.data.models.VersionResponse;
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
    @GET("/words")
    Call<List<String>> getAllWords();

    @GET("/user")
    Call<List<Player>> getAllUsers(@Header("Authorization") String bearerToken);

    @GET("/best_players")
    Call<List<Player>> getBestPlayers(@Header("Authorization") String bearerToken);

    @PUT("/user")
    Call<Void> registerPlayer(@Header("Authorization") String bearerToken, @Body Player player);

    @PATCH("/user")
    Call<Void> updateUser(@Header("Authorization") String bearerToken, @Body Player player);

    @GET("/word_meaning")
    Call<Word> getWordMeaning(@Header("Authorization") String bearerToken, @Query("word") String word);

    @PUT("/add_word")
    Call<Void> addWordRequest(@Header("Authorization") String bearerToken, @Body HashMap<String, String> body);

    @POST("/user")
    Call<Void> setStatusOnline(@Header("Authorization") String bearerToken, @Body Player player);

    @GET("/last_version")
    Call<VersionResponse> getLatestAppVersion(@Header("Authorization") String bearerToken);

    @GET("/username_availability")
    Call<UsernameAvailabilityResponse> checkUsernameAvailability(@Header("Authorization") String bearerToken, @Query("username") String username);

    @GET("/user_rank")
    Call<UserRankResponse> getUserRank(@Header("Authorization") String bearerToken, @Query("name") String name);

}