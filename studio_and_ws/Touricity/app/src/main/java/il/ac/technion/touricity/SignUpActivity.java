package il.ac.technion.touricity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import il.ac.technion.touricity.service.SignUpService;

/**
 * A login screen that offers login via email/password.
 */
public class SignUpActivity extends Activity {

    public static final String INTENT_EXTRA_EMAIL = "extra_email";
    public static final String INTENT_EXTRA_NICKNAME = "extra_nickname";
    public static final String INTENT_EXTRA_PHONE = "extra_phone";
    public static final String INTENT_EXTRA_PASSWORD = "extra_password";
    public static final String INTENT_EXTRA_GUIDE = "extra_guide";

    public static final String BROADCAST_INTENT_CANCEL_EMAIL = "cancel_email";
    public static final String BROADCAST_INTENT_CANCEL_NICKNAME = "cancel_nickname";
    public static final String BROADCAST_INTENT_RESULT_SUCCESS =  "result_success";

    public static final String BROADCAST_SIGNUP_SERVICE_DONE = "broadcast_signup_service_done";

    private AppCompatDelegate mDelegate;

    // UI references.
    private EditText mEmailView;
    private EditText mNicknameView;
    private EditText mPhoneView;
    private EditText mPasswordView;
    private EditText mRePasswordView;
    private CheckBox mGuideCheckboxView;
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
        mGuideCheckboxView = (CheckBox)findViewById(R.id.signup_checkbox);

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

        Button emailSignInButton = (Button) findViewById(R.id.signup_submit_btn);
        emailSignInButton.setOnClickListener(new OnClickListener() {
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
        boolean isGuide = mGuideCheckboxView.isChecked();

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

        if (!password.equals(rePassword)) {
            mPasswordView.setError(getString(R.string.error_mismatch_password));
            focusView = (focusView != null) ? focusView : mPasswordView;
            cancel = true;
        }

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
            Intent intent = new Intent(this, SignUpService.class);
            intent.putExtra(INTENT_EXTRA_EMAIL, email);
            intent.putExtra(INTENT_EXTRA_NICKNAME, nickname);
            intent.putExtra(INTENT_EXTRA_PHONE, phone);
            intent.putExtra(INTENT_EXTRA_PASSWORD, password);
            intent.putExtra(INTENT_EXTRA_GUIDE, isGuide);

            startService(intent);
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

    @Override
    public void onResume() {
        super.onResume();
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(BROADCAST_SIGNUP_SERVICE_DONE));
    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean cancelEmail = intent.getBooleanExtra(BROADCAST_INTENT_CANCEL_EMAIL, false);
            boolean cancelNickname = intent.getBooleanExtra(BROADCAST_INTENT_CANCEL_NICKNAME, false);
            // This variable determines if the user insertion itself completed successfully, after
            // all the constraints have been checked.
            boolean success = intent.getBooleanExtra(BROADCAST_INTENT_RESULT_SUCCESS, false);

            boolean cancel = false;
            View focusView = null;

            if (cancelEmail) {
                mEmailView.setError((getString(R.string.error_taken_email)));
                focusView = mEmailView;
                cancel = true;
            }
            if (cancelNickname) {
                mNicknameView.setError(getString(R.string.error_taken_nickname));
                focusView = (focusView != null) ? focusView : mNicknameView;
                cancel = true;
            }

            if (cancel) {
                // There was an error; user should choose a different email/nickname, according to
                // the given instructions.
                focusView.requestFocus();
                showProgress(false);
            } else {
                // TODO: add email confirmation logic
                finish();
            }
        }
    };

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }
}

