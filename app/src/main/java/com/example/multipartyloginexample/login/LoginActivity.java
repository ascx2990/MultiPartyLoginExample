package com.example.multipartyloginexample.login;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.multipartyloginexample.AlertDialogFragment;
import com.example.multipartyloginexample.BuildConfig;
import com.example.multipartyloginexample.LogUnit;
import com.example.multipartyloginexample.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    public final static String USER_EMAIL = "USER_EMAIL";
    public static final int REQUEST_SIGNUP = 0;
    public static final int REQUEST_FORGET_PASSWORD = 1;
    public static final int REQUEST_CODE_SIGN_IN = 9001;
    public static final int REQUEST_CODE_SIGN_IN_TEST = 9101;
    private static final String TAG = "LoginActivity";
    private EditText _emailText;
    private EditText _passwordText;
    private Button _loginButton;
    private TextView _signupLink, linkForgetPwd;
    private SignInButton btGoogleSignIn;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressDialog mProgressDialog;
    private LoginButton btFBSignIn;
    private CallbackManager mCallbackManager;
    private Button btFBLogin;
    private LoginManager fbLoginManager;
    private Button btNormalLogin;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findView();
        initial();
        setListener();
        hashKey();
    }

    private void findView() {
        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        _loginButton = findViewById(R.id.btn_login);
        _signupLink = findViewById(R.id.link_signup);
        btGoogleSignIn = findViewById(R.id.sign_in_button);
        btFBSignIn = findViewById(R.id.sign_in_fb_button);
        btFBLogin = findViewById(R.id.sign_in_fb);
        btNormalLogin=findViewById(R.id.sign_in_normal);
        linkForgetPwd = findViewById(R.id.link_forget_pwd);
    }

    private void initial() {
        //firebase
        mAuth = FirebaseAuth.getInstance();
        //facebook
        fbLoginManager = LoginManager.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
        btFBSignIn.setReadPermissions("email", "public_profile");
        //google
        GoogleSignInOptions mSignInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, mSignInOptions);
    }

    private void setListener() {
        btNormalLogin.setOnClickListener(view-> loginTest());
        linkForgetPwd.setOnClickListener(view -> {

            Intent intent = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
            startActivityForResult(intent, REQUEST_FORGET_PASSWORD);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        });
        _loginButton.setOnClickListener(view -> login());
        btGoogleSignIn.setOnClickListener(view -> signInGoogle());
        _signupLink.setOnClickListener(view -> {
            // Start the Signup activity
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
            //finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        });
        btFBLogin.setOnClickListener(view -> loginFB());
        btFBSignIn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
                ShowAccountConnectProblemTip("facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                ShowAccountConnectProblemTip(error.toString());
            }
        });

    }

    private void loginTest() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(), new AuthUI.IdpConfig.FacebookBuilder().build()

        );

// Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.ic_launcher_background)
                        .setTheme(R.style.AppTheme_Dark)
                        .build(),
                REQUEST_CODE_SIGN_IN_TEST);
    }

    private void loginFB() {
        // 設定FB login的顯示方式 ; 預設是：NATIVE_WITH_FALLBACK
        fbLoginManager.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
        // 設定要跟用戶取得的權限，以下3個是基本可以取得，不需要經過FB的審核
        List<String> permissions = new ArrayList<>();
        permissions.add("public_profile");
        permissions.add("email");


        // 設定要讀取的權限
        fbLoginManager.logInWithReadPermissions(this, permissions);
        fbLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                // 登入成功

                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                // 用戶取消
                Log.d(TAG, "facebook:onCancel");
                // ...
                ShowAccountConnectProblemTip("facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                ShowAccountConnectProblemTip(error.toString());
                //mAuth.getCurrentUser().linkWithCredential()
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void login() {
        LogUnit.d(TAG, "Login");
        if (!validate()) {
            onLoginFailed();
            return;
        }
        _loginButton.setEnabled(false);
        showProgressDialog();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Implement your own authentication logic here.
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            LogUnit.v(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user == null || !user.isEmailVerified()) {
//
//                                Toast.makeText(LoginActivity.this, getString(R.string.signip_text_vaild_email),
//                                        Toast.LENGTH_SHORT).show();
                                ShowSendEmailVerifyDialog(new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        user.sendEmailVerification().
                                                addOnCompleteListener(LoginActivity.this,
                                                        (mTask) -> {
                                                            // Re-enable button
                                                            _loginButton.setEnabled(true);
                                                            if (mTask.isSuccessful()) {
                                                                Toast.makeText(LoginActivity.this,
                                                                        "Verification email sent to " + user.getEmail(),
                                                                        Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Log.e(TAG, "sendEmailVerification", mTask.getException());
                                                                Toast.makeText(LoginActivity.this,
                                                                        "Failed to send verification email.",
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                    }
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                            } else {
                                finish();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            LogUnit.e(TAG, "signInWithEmail:failure" + task.getException());
                            if (task.getException().toString().contains("password")) {
                                Toast.makeText(LoginActivity.this, getString(R.string.signin_authenticating_password_not_correct),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            _loginButton.setEnabled(true);
                        }
                    }
                });
    }


    private FragmentTransaction checkDialogFragment(String dialogTag) {
        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction mFragTransaction = mFragmentManager.beginTransaction();
        Fragment fragment = mFragmentManager.findFragmentByTag(dialogTag);
        if (fragment != null) {
            // 為了不重複Dialog,並在顯示之前移除舊Dialog
            mFragTransaction.remove(fragment);
        }
        return mFragTransaction;
    }


    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), getString(R.string.signup_login_failed), Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError(getString(R.string.signip_enter_valid_email_address));
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 20) {
            _passwordText.setError(getString(R.string.signup_enter_valid_password_number));
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        LogUnit.v(TAG, "requestCode: " + requestCode);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN_TEST:
                IdpResponse response = IdpResponse.fromResultIntent(resultData);

                if (resultCode == RESULT_OK) {
                    // Successfully signed in
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    // ...
                } else {
                    // Sign in failed. If response is null the user canceled the
                    // sign-in flow using the back button. Otherwise check
                    // response.getError().getErrorCode() and handle the error.
                    // ...
                }

                break;
            case REQUEST_CODE_SIGN_IN:
                LogUnit.v(TAG, "REQUEST_CODE_SIGN_IN");
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(resultData);
                hideProgressDialog();
                try {
                    // handleSignInResult(task);
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    LogUnit.e(TAG, "Google sign in failed " + e.toString());
                    // ...

                    ShowAccountConnectProblemTip(e.toString());
                }

                break;
            case REQUEST_SIGNUP:
                if (resultCode == RESULT_OK) {
                    String userEmail = resultData.getStringExtra(LoginActivity.USER_EMAIL);
                    _emailText.setText(userEmail);
                    // TODO: Implement successful signup logic here
                    // By default we just finish the Activity and log them in automatically
                    // this.finish();
                }
                break;
            default:
                LogUnit.e(TAG, "default requestCode: " + requestCode);
                mCallbackManager.onActivityResult(requestCode, resultCode, resultData);
                break;

        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /********Google Login********/
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        LogUnit.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        LogUnit.v(TAG, "IdToken:" + acct.getIdToken());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            LogUnit.v(TAG, "signInWithCredential:success");
                            finish();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            LogUnit.e(TAG, "signInWithCredential:failure " + task.getException());
                            ShowAccountConnectProblemTip(task.getException().toString());
//                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void signInGoogle() {
        LogUnit.v(TAG, "signInGoogle");
        showProgressDialog();
        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }
/********Google Login********/
    /*********showProgressDialog********/
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
            mProgressDialog.setMessage(getString(R.string.signin_authenticating));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    /*********hideProgressDialog********/
    /*********MessageDialog********/
    private void ShowAccountDuplicateTip() {

        FragmentTransaction mFragTransaction = checkDialogFragment(AlertDialogFragment.class.getName());
        AlertDialogFragment dialog = new AlertDialogFragment(getString(R.string.dialog_title_account_duplicate), getString(R.string.dialog_message_account_duplicate),
                R.string.dialog_text_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }, 0, null, 0, null);
        mFragTransaction.add(dialog, AlertDialogFragment.class.getName());
        mFragTransaction.commitAllowingStateLoss();
    }

    //
    private void ShowAccountConnectProblemTip(String message) {

        if (BuildConfig.DEBUG) {
            message = getString(R.string.dialog_message_account_connect_failure) + "\n" + message;
        } else {
            message = getString(R.string.dialog_message_account_connect_failure);
        }
        FragmentTransaction mFragTransaction = checkDialogFragment(AlertDialogFragment.class.getName());
        AlertDialogFragment dialog = new AlertDialogFragment(getString(R.string.dialog_title_account_connect_failure), message,
                R.string.dialog_text_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }, 0, null, 0, null);
        mFragTransaction.add(dialog, AlertDialogFragment.class.getName());
        mFragTransaction.commitAllowingStateLoss();
    }

    private void ShowSendEmailVerifyDialog(DialogInterface.OnClickListener reverifyListener, DialogInterface.OnClickListener confirmListener) {
        FragmentTransaction mFragTransaction = checkDialogFragment(AlertDialogFragment.class.getName());
        AlertDialogFragment dialog = new AlertDialogFragment(getString(R.string.dialog_title_signin_email_verify), getString(R.string.dialog_message_signup_email_verify),
                R.string.dialog_text_reverify, reverifyListener, R.string.dialog_text_confirm, confirmListener, 0, null);
        mFragTransaction.add(dialog, AlertDialogFragment.class.getName());
        mFragTransaction.commitAllowingStateLoss();
    }

    /*********MessageDialog********/

    /********Facebook Login********/
    private void handleFacebookAccessToken(AccessToken token) {
        LogUnit.d(TAG, "handleFacebookAccessToken");
        if (token == null || token.getToken() == null) {
            LogUnit.v(TAG, "token is null or Token is null");
            Toast.makeText(LoginActivity.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        LogUnit.v(TAG, "handleFacebookAccessToken:" + token);
        LogUnit.v(TAG, "FacebookAccessTokenKey:" + token.getToken());
//        String email = mAuth.getCurrentUser().getEmail();
//        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
//            @Override
//            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
//                SignInMethodQueryResult s = task.getResult();
//                LogUnit.v(TAG,s.toString());
//
//
//            }
//        });
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            LogUnit.v(TAG, "Facebook signInWithCredential:success");
                            finish();
                        } else {


                            // If sign in fails, display a message to the user.
                            LogUnit.e(TAG, "Facebook signInWithCredential:failure" + task.getException());
                            String result = task.getException().toString();
                            if (result != null && result.contains("account")
                                    && result.contains("already") && result.contains("exists")
                                    && result.contains("same") && result.contains("email")
                                    && result.contains("different") && result.contains("credentials")) {
                                ShowAccountDuplicateTip();
                            } else {
                                ShowAccountConnectProblemTip(task.getException().toString());
                            }
                        }
                    }
                });
    }

    /********Facebook Login********/

    //get hash key and you can you can register for FACEBOOK.
    private void hashKey() {
        LogUnit.d(TAG, "KeyHash");
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.example.emailview", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                LogUnit.v(TAG, "KeyHash:" + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "NameNotFoundException:" + e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "NoSuchAlgorithmException:" + e.toString());
        }
    }

    //show dialog for testing
    private void testCodeShowDialog() {
        FragmentTransaction mFragTransaction = checkDialogFragment(AlertDialogFragment.class.getName());
        AlertDialogFragment dialog = new AlertDialogFragment(getString(R.string.dialog_title_signin_email_verify), getString(R.string.dialog_message_signup_email_verify),
                R.string.dialog_text_reverify, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }, R.string.dialog_text_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }, 0, null);
        mFragTransaction.add(dialog, AlertDialogFragment.class.getName());
        mFragTransaction.commitAllowingStateLoss();
    }
}
