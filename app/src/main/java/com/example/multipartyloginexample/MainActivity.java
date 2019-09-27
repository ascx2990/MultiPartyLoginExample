package com.example.multipartyloginexample;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.multipartyloginexample.login.LoginActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private final String TAG = "MainActivity";
    //Firebase
    public static FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    //FB
    private CallbackManager mCallbackManager;
    private LoginManager fbLoginManager;
    //Google
    private GoogleSignInClient mGoogleSignInClient;
    //View
    private AppBarConfiguration mAppBarConfiguration;
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ProgressDialog mProgressDialog;
    private TextView tv_user_name, tv_user_account;
    private ImageView iv_user_icon;
    //
    private String uEmail;
    private boolean LoginStateForFB = false, LoginStateForGoogle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findView();
        initial();
        setListener();
    }

    private void findView() {
        fab = findViewById(R.id.fab);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        View lnNavHeader = navigationView.getHeaderView(0);
        tv_user_name = lnNavHeader.findViewById(R.id.tv_user_name);
        tv_user_account = lnNavHeader.findViewById(R.id.tv_user_account);
        iv_user_icon = lnNavHeader.findViewById(R.id.imageView);
    }

    private void initial() {
        LogUnit.d(TAG, "initial");
        mAuth = FirebaseAuth.getInstance();
        fbLoginManager = LoginManager.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
        //Google
        GoogleSignInOptions mSignInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, mSignInOptions);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void setListener() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });
    }

    //get login method
    private void setFetchSignIMethods() {
        if (uEmail == null || mAuth == null) {
            return;
        }
        mAuth.fetchSignInMethodsForEmail(uEmail).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    SignInMethodQueryResult providerResult = task.getResult();
                    List<String> methods = providerResult.getSignInMethods();
                    for (String method : methods) {
                        if (method.contains("facebook")) {
                            LoginStateForFB = true;
                        }
                        if (method.contains("google")) {
                            LoginStateForGoogle = true;
                        }
                        if (method.contains("password")) {
                            if (!mAuth.getCurrentUser().isEmailVerified()) {
                                //Please Verified Email
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                return;
                            }
                        }
                        LogUnit.v(TAG, "LoginMethod:" + method);
                    }
                } else {
                    LogUnit.e(TAG, "LoginMethod:" + task.getException().toString());
                    //Manage error }
                }


            }
        });
    }

    private void signInGoogle() {
        LogUnit.v(TAG, "signInGoogle");
        showProgressDialog();
        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), LoginActivity.REQUEST_CODE_SIGN_IN);
    }




    @Override
    protected void onStart() {
        super.onStart();
        LogUnit.d(TAG, "onStart");
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            return;
        }
        user.reload().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                LogUnit.v(TAG, "Email:" + user.getEmail());
                LogUnit.v(TAG, "PhoneNumber:" + user.getPhoneNumber());
                LogUnit.v(TAG, "DisplayName:" + user.getDisplayName());
                LogUnit.v(TAG, "ProviderId:" + user.getProviderId());
                LogUnit.v(TAG, "PhotoUrl:" + user.getPhotoUrl());

                uEmail = user.getEmail();
                tv_user_name.setText(user.getDisplayName());
                tv_user_account.setText(uEmail);
                tv_user_account.setVisibility(View.VISIBLE);
                setFetchSignIMethods();
                if (LoginStateForFB) {
                    Profile profile = Profile.getCurrentProfile();
                    // 取得用戶大頭照
                    Uri userPhoto = profile.getProfilePictureUri(300, 300);
                    String id = profile.getId();
                    String name = profile.getName();
                    tv_user_name.setText(name);
                    Log.d(TAG, "Facebook userPhoto: " + userPhoto);
                    Log.d(TAG, "Facebook id: " + id);
                    Log.d(TAG, "Facebook name: " + name);
                    if (userPhoto != null) {
                        Glide.with(this)
                                .load(userPhoto)
                                .into(iv_user_icon);
                    }
                }

            } else {
                tv_user_name.setText(getString(R.string.nav_title_user));
                tv_user_account.setText("");
                tv_user_account.setVisibility(View.INVISIBLE);


            }
        });
    }

    @Override
    protected void onStop() {

//        if (authStateListener != null) {
//            mAuth.removeAuthStateListener(authStateListener);
//        }
        super.onStop();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void sendEmail() {
        Intent it = new Intent(Intent.ACTION_SEND);
        String[] tos = {"xxxxx@abc.com"};
        String[] ccs = {"ascx2990@gmail.com"};
        it.putExtra(Intent.EXTRA_EMAIL, tos);
        it.putExtra(Intent.EXTRA_CC, ccs);
        it.putExtra(Intent.EXTRA_TEXT, "The email body text");
        it.putExtra(Intent.EXTRA_SUBJECT, "The email subject text");
        it.setType("message/rfc822");
        startActivity(Intent.createChooser(it, "Choose Email Client"));
    }

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
    /********Dialog********/
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

    /********Dialog********/
}
