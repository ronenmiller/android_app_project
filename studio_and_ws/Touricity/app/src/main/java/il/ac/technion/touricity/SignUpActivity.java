package il.ac.technion.touricity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
 * A sign up screen.
 */
public class SignUpActivity extends Activity {

    public static final String INTENT_EXTRA_EMAIL = "extra_email";
    public static final String INTENT_EXTRA_USERNAME = "extra_username";
    public static final String INTENT_EXTRA_PHONE = "extra_phone";
    public static final String INTENT_EXTRA_PASSWORD = "extra_password";
    public static final String INTENT_EXTRA_GUIDE = "extra_guide";

    public static final String BROADCAST_INTENT_CANCEL_EMAIL = "cancel_email";
    public static final String BROADCAST_INTENT_CANCEL_USERNAME = "cancel_username";
    public static final String BROADCAST_INTENT_RESULT =  "result";

    public static final String BROADCAST_SIGNUP_SERVICE_DONE = "broadcast_signup_service_done";

    private AppCompatDelegate mDelegate;

    // UI references.
    private EditText mEmailView;
    private EditText mUsernameView;
    private EditText mPhoneView;
    private IconifiedEditText mPasswordView;
    private IconifiedEditText mRePasswordView;
    private CheckBox mGuideCheckboxView;
    private View mProgressView;
    private View mSignupFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setupActionBar(savedInstanceState);

        // Set up the sign up form.
        mEmailView = (EditText)findViewById(R.id.signup_email);
        mUsernameView = (EditText)findViewById(R.id.signup_username);
        mPhoneView = (EditText)findViewById(R.id.signup_phone);
        mPasswordView = (IconifiedEditText)findViewById(R.id.signup_password);
        mRePasswordView = (IconifiedEditText)findViewById(R.id.signup_re_password);
        mGuideCheckboxView = (CheckBox)findViewById(R.id.signup_checkbox);

        // TODO: when edit text loses focus, popup error if needed

        mPasswordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().matches(mRePasswordView.getText().toString()) &&
                        !s.toString().isEmpty()) {
                    Drawable checkMark = getResources()
                            .getDrawable(R.drawable.ic_check_circle_green_24dp);
                    mPasswordView.displayIconWithoutErrorMsg(checkMark);
                    mRePasswordView.displayIconWithoutErrorMsg(checkMark);
                }
                else {
                    mPasswordView.clearIcon();
                    mRePasswordView.clearIcon();
                }
            }
        });

        mRePasswordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().matches(mPasswordView.getText().toString()) &&
                        !s.toString().isEmpty()) {
                    Drawable checkMark = getResources()
                            .getDrawable(R.drawable.ic_check_circle_green_24dp);
                    mPasswordView.displayIconWithoutErrorMsg(checkMark);
                    mRePasswordView.displayIconWithoutErrorMsg(checkMark);
                }
                else {
                    mPasswordView.clearIcon();
                    mRePasswordView.clearIcon();
                }
            }
        });

        mRePasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.signup_submit_btn || id == EditorInfo.IME_NULL) {
                    attemptSignup();
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
                attemptSignup();
            }
        });

        mSignupFormView = findViewById(R.id.signup_scrollview_form);
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
     * Attempts to sign in or register the account specified by the sign up form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual sign up attempt is made.
     */
    public void attemptSignup() {
        // Reset errors.
        mEmailView.setError(null);
        mUsernameView.setError(null);
        mPhoneView.setError(null);
        mPasswordView.setError(null);
        mRePasswordView.setError(null);

        // Store values at the time of the sign up attempt.
        String email = mEmailView.getText().toString();
        String username = mUsernameView.getText().toString();
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

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = (focusView != null) ? focusView : mUsernameView;
            cancel = true;
        }
        else if (!isUsernameLengthValid(username)) {
            mUsernameView.setError((getString(R.string.error_short_username)));
            focusView = (focusView != null) ? focusView : mUsernameView;
            cancel = true;
        }
        else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = (focusView != null) ? focusView : mUsernameView;
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
            // There was an error; don't attempt to sign up and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user sign up attempt.
            showProgress(true);
            Intent intent = new Intent(this, SignUpService.class);
            intent.putExtra(INTENT_EXTRA_EMAIL, email);
            intent.putExtra(INTENT_EXTRA_USERNAME, username);
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

    private boolean isUsernameLengthValid(String username) {
        return username.length() >= 2;
    }

    private boolean isUsernameValid(String username) {
        return username.matches("^[[a-z][A-Z]]+.*$");
    }

    private boolean isPasswordLengthValid(String password) {
        return password.length() >= 6;
    }

    private boolean isPasswordValid(String password) {
        return password.matches("^(?=.*[[a-z][A-Z]]+)(?=.*[0-9]+).*$");
    }

    /**
     * Shows the progress UI and hides the sign up form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mSignupFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mSignupFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSignupFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mSignupFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            boolean cancelUsername = intent.getBooleanExtra(BROADCAST_INTENT_CANCEL_USERNAME, false);
            // This variable determines if the user insertion itself completed successfully, after
            // all the constraints have been checked.
            boolean success = intent.getBooleanExtra(BROADCAST_INTENT_RESULT, false);

            boolean cancel = false;
            View focusView = null;

            if (cancelEmail) {
                mEmailView.setError((getString(R.string.error_taken_email)));
                focusView = mEmailView;
                cancel = true;
            }
            if (cancelUsername) {
                mUsernameView.setError(getString(R.string.error_taken_username));
                focusView = (focusView != null) ? focusView : mUsernameView;
                cancel = true;
            }

            if (cancel || !success) {
                // There was an error; user should choose a different email/username, according to
                // the given instructions.
                if (focusView != null) {
                    focusView.requestFocus();
                }
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

