package com.teamchief.petergok.teamchief.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.SQLException;
import android.util.Log;

import com.google.gson.Gson;
import com.teamchief.petergok.teamchief.Constants;
import com.teamchief.petergok.teamchief.activities.BaseActivity;
import com.teamchief.petergok.teamchief.activities.MessageListActivity;
import com.teamchief.petergok.teamchief.gson.GsonMessage;
import com.teamchief.petergok.teamchief.gson.GsonTeam;
import com.teamchief.petergok.teamchief.model.ConversationContentProvider;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Peter on 2015-01-10.
 */
public class GetTeamTask extends BaseTask {
    private BaseActivity mActivity;
    private String mUserName;
    private String mPassword;
    private String mTeamId;
    private long mAfter;
    private long mBefore;
    private String mLastMessageId;

    public GetTeamTask(BaseActivity activity, String userName,
                       String password, String teamId, long after, long before, String lastMessageId) {
        super(0, false);
        mActivity = activity;
        mUserName = userName;
        mPassword = password;
        mTeamId = teamId;
        mAfter = after;
        mBefore = before;
        mLastMessageId = lastMessageId;
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = "none";
        String uri = Constants.SERVER_URL + "/team/" + mTeamId;
        uri = addParamsToUrl(uri);
        try {
            HttpGet method = new HttpGet(uri);
            response = httpclient.execute(method);
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
            mActivity.checkNetworkConnection();
        }
        return responseString;
    }

    protected String addParamsToUrl(String url){
        if(!url.endsWith("?"))
            url += "?";

        List<NameValuePair> nameValuePairs = new LinkedList<>();

        nameValuePairs.add(new BasicNameValuePair("password", mPassword));
        nameValuePairs.add(new BasicNameValuePair("username", mUserName));

        if (mAfter > 0) {
            nameValuePairs.add(new BasicNameValuePair("after", "" + mAfter));
        } else if (mBefore > 0) {
            nameValuePairs.add(new BasicNameValuePair("before", "" + mBefore));
        }

        String paramString = URLEncodedUtils.format(nameValuePairs, "utf-8");

        url += paramString;
        return url;
    }

    @Override
    public Runnable getRetryRunnable() {
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Gson gson = new Gson();
        GsonTeam team = null;
        try {
            team = gson.fromJson(result, GsonTeam.class);
        } catch (Exception e) {
            Log.d("RESPONSE", e.toString());
        }

        if (team != null) {
            ContentResolver cr = mActivity.getContentResolver();
            List<ContentValues> messages = new ArrayList<>();
            for (GsonMessage message : team.messages) {
                if (!message.id.equals(mLastMessageId)) {
                    ContentValues newValues = new ContentValues();
                    newValues.put("messageId", message.id);
                    newValues.put("sender", message.sender.username);
                    newValues.put("sendTime", message.sendTime);
                    newValues.put("text", message.text);
                    newValues.put("teamId", mTeamId);
                    messages.add(newValues);
                }
            }
            ContentValues[] contentValues = new ContentValues[messages.size()];
            cr.bulkInsert(ConversationContentProvider.CONTENT_URI, messages.toArray(contentValues));
        }

        Log.d("RESPONSE", result);
    }
}