package com.example.traveldiaries;
import android.app.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.content.Intent;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * A login screen that offers login/registration via username/password.
 */
public class LoginActivity extends Activity {

    // UI references.
    private EditText usernameView;
    private EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(ParseUser.getCurrentUser() != null) {
            SignedIn();
        }

        setContentView(R.layout.activity_login);

        usernameView = (EditText) findViewById(R.id.username);
        passwordView = (EditText) findViewById(R.id.password);

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(validate()) {
                    login();
                }
            }
        });

        Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()) {
                    register();
                }
            }
        });

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public boolean validate() {
        // Reset errors.
        usernameView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        } else if(!isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        } else if (!isUserIdValid(username)) {
            usernameView.setError(getString(R.string.error_invalid_username));
            focusView = usernameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        }

        return !cancel;
    }

    public void SignedIn() {
        Intent intent = new Intent(LoginActivity.this, PreviousTrip.class);
        startActivity(intent);
        finish();
    }

    private void login() {
        final String userID = usernameView.getText().toString();
        final String password = passwordView.getText().toString();

        ParseUser.logInInBackground(userID, password, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if(parseUser != null) {
                    Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                    SignedIn();
                } else {
                    int errorCode = e.getCode();
                    Log.d("LOGIN ERROR", e.getCode()+"");
                    if(errorCode == ParseException.OBJECT_NOT_FOUND) {
                        Log.d("LOGIN ERROR", "OBJECT_NOT_FOUND");
                        usernameView.setError(getString(R.string.error_incorrect_username_or_password));
                        passwordView.setError(getString(R.string.error_incorrect_username_or_password));
                        usernameView.requestFocus();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    private void register() {
        final String userID = usernameView.getText().toString();
        final String password = passwordView.getText().toString();

        ParseUser user = new ParseUser();
        user.setUsername(userID);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    Toast.makeText(LoginActivity.this, "Registration Success!", Toast.LENGTH_SHORT).show();
                    SignedIn();
                } else {
                    int errorCode = e.getCode();
                    Log.d("REGISTER ERROR", e.getCode()+"");
                    if(errorCode == ParseException.USERNAME_TAKEN) {
                        Log.d("REGISTER ERROR", "USERNAME_TAKEN");
                        usernameView.setError(getString(R.string.error_username_taken));
                        usernameView.requestFocus();
                    } else {
                        Toast.makeText(LoginActivity.this, "Registration Failed!", Toast.LENGTH_SHORT).show();
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isUserIdValid(String username) {
        return !username.contains(" ");
    }

    private boolean isPasswordValid(String password) {
        return (password.length() > 4 && !password.contains(" "));
    }
}