package co.mobilemakers.githubrepos;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * A placeholder fragment containing a simple view.
 */
public class GithubReposFragment extends Fragment {

    private final static String LOG_TAG = GithubReposFragment.class.getSimpleName();

    EditText mEditTextUsername;
    TextView mTextViewRepos;

    public GithubReposFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_github_repos, container, false);
        Button buttonGetRepos = (Button) rootView.findViewById(R.id.button_get_repos);
        mTextViewRepos = (TextView)rootView.findViewById(R.id.text_view_repos);
        mEditTextUsername = (EditText)rootView.findViewById(R.id.edit_text_username);
        buttonGetRepos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mEditTextUsername.getText().toString();
                String message = String.format(getString(R.string.getting_repos_for_user),username);
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                new FetchReposTask().execute(username);
            }
        });
        return rootView;
    }

    private String readFullResponse(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String response ="";
        String line;
        while((line = bufferedReader.readLine()) != null){
            stringBuilder.append(line).append("\n");
        }
        if(stringBuilder.length() > 0){
            response = stringBuilder.toString();
        }
        return response;
    }

    private String parseResponse(String response){
        final String REPO_NAME = "name";
        List<String> repos = new ArrayList<>();
        try {
            JSONArray responseJsonArray = new JSONArray(response);
            JSONObject object;
            for(int i=0;i<responseJsonArray.length();i++){
                object = responseJsonArray.getJSONObject(i);
                repos.add(object.getString(REPO_NAME));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TextUtils.join(", ",repos);
    }

    class FetchReposTask extends AsyncTask<String, Void, String>{

        String username="";
        String listOfRepos ="";

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            mTextViewRepos.setText(response);
        }

        @Override
        protected String doInBackground(String... params) {
            String username;
            if(params.length > 0){
                username = params[0];
            }else{
                username="octocat";
            }

            try {
                URL url = ConstructURLQuery(username);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                try {
                     String response = readFullResponse(httpsURLConnection.getInputStream());
                    listOfRepos = parseResponse(response);
                } catch(IOException e){
                    e.printStackTrace();
                }finally{
                    httpsURLConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return listOfRepos;
        }
    }

    private URL ConstructURLQuery(String username) throws MalformedURLException {
        final String GITHUB_BASE_URL ="api.github.com";
        final String USERS_PATH ="users";
        final String REPOS_ENDPOINT = "repos";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https").authority(GITHUB_BASE_URL).
                appendPath(USERS_PATH).
                appendPath(username).
                appendPath(REPOS_ENDPOINT);
        Uri uri = builder.build();
        Log.d(LOG_TAG, "Build URI: " + uri.toString());

        return new URL(uri.toString());
    }
}
