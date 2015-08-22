package il.ac.technion.touricity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A login screen that offers login via email/password.
 */
public class SignUpActivity extends Activity {

    private AppCompatDelegate mDelegate;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mNicknameView;
    private EditText mPhoneView;
    private EditText mPasswordView;
    private EditText mRePasswordView;
    private Button mEmailSignInButton;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setupActionBar(savedInstanceState);

        // Set up the login form.
        mEmailView = (EditText)findViewById(R.id.signup_email);
        mNicknameView = (EditText)findViewById(R.id.signup_nickname);
        mPhoneView = (EditText)findViewById(R.id.signup_phone);
        mPasswordView = (EditText)findViewById(R.id.signup_password);
        mRePasswordView = (EditText)findViewById(R.id.signup_re_password);

        mRePasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.action_login_id || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    // Return true if action was consumed, false otherwise.
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.signup_submit_btn);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.signup_scrollview_form);
        mProgressView = findViewById(R.id.signup_progressbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Activate when pressing the action bar's back button.
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up the {@link android.support.v7.app.ActionBar}.
     */
    private void setupActionBar(Bundle savedInstanceState) {
        // Show the Up button in the action bar.
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        getDelegate().getSupportActionBar().show();
        getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mNicknameView.setError(null);
        mPhoneView.setError(null);
        mPasswordView.setError(null);
        mRePasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String nickname = mNicknameView.getText().toString();
        String phone = mPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();
        String rePassword = mRePasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid nickname.
        if (TextUtils.isEmpty(nickname)) {
            mNicknameView.setError(getString(R.string.error_field_required));
            focusView = (focusView != null) ? focusView : mNicknameView;
            cancel = true;
        }
        else if (!isNicknameLengthValid(nickname)) {
            mNicknameView.setError((getString(R.string.error_short_nickname)));
            focusView = (focusView != null) ? focusView : mNicknameView;
            cancel = true;
        }
        else if (!isNicknameValid(nickname)) {
            mNicknameView.setError(getString(R.string.error_invalid_nickname));
            focusView = (focusView != null) ? focusView : mNicknameView;
            cancel = true;
        }
        else {
            // TODO: check if the nickname already exists in the sever db
        }

        if (!password.equals(rePassword)) {
            mPasswordView.setError(getString(R.string.error_mismatch_password));
            focusView = (focusView != null) ? focusView : mPasswordView;
            cancel = true;
        }

        // TODO: change SERVER password length restriction to 6
        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = (focusView != null) ? focusView : mPasswordView;
            cancel = true;
        }
        else if (!isPasswordLengthValid(password)) {
            mPasswordView.setError((getString(R.string.error_short_password)));
            focusView = (focusView != null) ? focusView : mPasswordView;
            cancel = true;
        }
        else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = (focusView != null) ? focusView : mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(rePassword)) {
            mRePasswordView.setError(getString(R.string.error_field_required));
            focusView = (focusView != null) ? focusView : mRePasswordView;
            cancel = true;
        }
        else if (!isPasswordLengthValid(rePassword)) {
            mRePasswordView.setError((getString(R.string.error_short_password)));
            focusView = (focusView != null) ? focusView : mRePasswordView;
            cancel = true;
        }
        else if (!isPasswordValid(rePassword)) {
            mRePasswordView.setError(getString(R.string.error_invalid_password));
            focusView = (focusView != null) ? focusView : mRePasswordView;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        // Email address cannot start or end with @ symbol.
        // Send a confirmation email to validate the given address.
        if (email.length() >= 3) {
            return email.substring(1, email.length() - 1).contains("@");
        }
        else {
            return false;
        }
    }

    private boolean isNicknameLengthValid(String nickname) {
        return nickname.length() >= 2;
    }

    private boolean isNicknameValid(String nickname) {
        return nickname.matches("^[[a-z][A-Z]]+.*$");
    }

    private boolean isPasswordLengthValid(String password) {
        return password.length() >= 6;
    }

    private boolean isPasswordValid(String password) {
        return password.matches("^(?=.*[[a-z][A-Z]]+)(?=.*[0-9]+).*$");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

