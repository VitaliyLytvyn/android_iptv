package com.app.tv.mediacast.retrofit;

import com.app.tv.mediacast.retrofit.data.DataAccess;
import com.app.tv.mediacast.retrofit.data.DataAppLanguages;
import com.app.tv.mediacast.retrofit.data.DataAppTimeZones;
import com.app.tv.mediacast.retrofit.data.DataAuthToken;
import com.app.tv.mediacast.retrofit.data.DataChannelsByLang;
import com.app.tv.mediacast.retrofit.data.DataLoginUser;
import com.app.tv.mediacast.retrofit.data.DataLoginUserByFb;
import com.app.tv.mediacast.retrofit.data.DataPlan;
import com.app.tv.mediacast.retrofit.data.DataProgrammsByChannelDay;
import com.app.tv.mediacast.retrofit.data.DataSubscriptionId;
import com.app.tv.mediacast.retrofit.data.DataTermsOfUseLink;
import com.app.tv.mediacast.retrofit.data.DataTranslateLanguages;
import com.app.tv.mediacast.retrofit.data.DataUrl;
import com.app.tv.mediacast.retrofit.data.DataUserCard;
import com.app.tv.mediacast.retrofit.data.DataUserDetails;
import com.app.tv.mediacast.retrofit.data.DataUserPlanData;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;

import static com.app.tv.mediacast.util.Constant.*;

/**
 * Created by skyver on 3/19/17.
 */

public interface Restapi {

    @GET(GET_LIST_OF_PLANS)
    Call<List<DataPlan>> getListOfPlans(@HeaderMap Map<String, String> headers);

    @GET(GET_TERMS_OF_USE_LINK)
    Call<DataTermsOfUseLink> getTermsOfUseLink(@HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @POST(REGISTER_USER)
    Call<DataSubscriptionId> registerUser(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST(REGISTER_USER_BY_FACEBOOK)
    Call<DataSubscriptionId> registerUserByFB(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> options);

    @GET(GET_USER_CREDIT_CARD)
    Call<DataUserCard> getUserCreditCard(@HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @POST(RENEW_PLAN_BY_STRIPE)
    Call<Void> renewPlanByStripe(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST(APPROVAL_REGISTER_USER_BY_STRIPE)
    Call<DataAuthToken> approvalRegisterUserByStripe(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST(RENEW_PLAN_BY_EXIST_CREDIT_CARD)
    Call<Void> renewPlanByExistsCreditCard(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST(LOGIN_USER)
    Call<DataLoginUser> loginUser(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST(LOGIN_USER_BY_FACEBOOK)
    Call<DataLoginUserByFb> loginUserByFb(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> options);

    @GET(GET_PROGRAMMS_BY_CHANNEL_AND_DAY)
    Call<DataProgrammsByChannelDay> getProgrammsByChannelAndDay(@HeaderMap Map<String, String> headers,
                                                                @Path("channel_id") String id,
                                                                @Path("day") String day);

    @GET(GET_PROGRAMMS_STREAM_URL)
    Call<DataUrl> getProgrammStreamUrl(@HeaderMap Map<String, String> headers, @Path("id") String id);

    @GET(GET_TRANSLATE_URL)
    Call<DataUrl> getTranslateUrl(@HeaderMap Map<String, String> headers, @Path("id") String id);

    @FormUrlEncoded
    @POST(RESET_USER_PASSWORD)
    Call<Void> resetUserPassword(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> options);

    @GET(CHECK_USER_ACCESS)
    Call<DataAccess> checkUserAccess(@HeaderMap Map<String, String> headers);

    @GET(GET_APP_TRANSLATE_LANGUAGES)
    Call<DataTranslateLanguages> getTranslateLanguages(@HeaderMap Map<String, String> headers);

    @GET(GET_BY_LANG)
    Call<DataChannelsByLang> getChannelsByLang(@HeaderMap Map<String, String> headers, @Path("lang") String lang);

    @FormUrlEncoded
    @POST(CHANGE_USER_PASSWORD)
    Call<DataAuthToken> changeUserPassword(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> options);

    @GET(GET_LIVE_URL)
    Call<DataUrl> getLiveUrl(@HeaderMap Map<String, String> headers, @Path("id") String id);

    @FormUrlEncoded
    @POST(UPDATE_USER)
    Call<Void> updateUser(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> options);

    @GET(SHOW_USER_DETAILS)
    Call<DataUserDetails> showUserDetails(@HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @POST(SET_USER_LANGUAGE)
    Call<Void> setUserLanguage(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> options);

    @GET(GET_APP_LANGUAGES)
    Call<DataAppLanguages> getAppLanguages(@HeaderMap Map<String, String> headers);

    @GET(SHOW_USER_PLAN_DATA)
    Call<DataUserPlanData> showUserPlanData(@HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @POST(CANCEL_SUBSCRIPTION)
    Call<Void> cancelSubscription(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST(SET_USER_TIMEZONE)
    Call<Void> setUserTimeZone(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> options);

    @GET(GET_APP_TIMEZONES)
    Call<DataAppTimeZones> getAppTimeZones(@HeaderMap Map<String, String> headers);

}












