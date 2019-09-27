package com.example.multipartyloginexample.ui.home;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.example.multipartyloginexample.AlertDialogFragment;
import com.example.multipartyloginexample.BuildConfig;
import com.example.multipartyloginexample.LogUnit;
import com.example.multipartyloginexample.MainActivity;
import com.example.multipartyloginexample.R;
import com.example.multipartyloginexample.login.LoginActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private final String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;
    private Button btAddFB, btAddGoogle, btSignOut;
    private ProgressDialog mProgressDialog;
    //FB
    private CallbackManager mCallbackManager;
    private LoginManager fbLoginManager;
    //Google
    private GoogleSignInClient mGoogleSignInClient;
    private boolean LoginStateForFB = false, LoginStateForGoogle = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView();
        initial();
        setListener();

    }

    private void findView() {
        btAddFB = getView().findViewById(R.id.button2);
        btAddGoogle = getView().findViewById(R.id.button3);
        btSignOut = getView().findViewById(R.id.button4);
    }

    private void initial() {
        fbLoginManager = LoginManager.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
        //Google
        GoogleSignInOptions mSignInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), mSignInOptions);
    }

    private void setListener() {
        btAddFB.setOnClickListener(view ->{
            addSignInFB();
        });
        btAddGoogle.setOnClickListener(view ->{
            addSignInGoogle();
        });
        btSignOut.setOnClickListener(view -> {
            if (MainActivity.mAuth != null) {

                MainActivity.mAuth.signOut();
                startActivity(new Intent(getContext(), LoginActivity.class));
            } else {
                LogUnit.v(TAG, "mAuth is null");
                Toast.makeText(getContext(), "mAuth is null", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addSignInGoogle() {
        LogUnit.v(TAG, "signInGoogle");
        showProgressDialog();
        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), LoginActivity.REQUEST_CODE_SIGN_IN);
    }

    private void addSignInFB() {
        LogUnit.d(TAG, "loginFB");
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

                LogUnit.v(TAG, "facebook:onSuccess:" + loginResult);
                handleAdditionLoginWithCredential(0, loginResult.getAccessToken().getToken());

            }

            @Override
            public void onCancel() {
                // 用戶取消
                LogUnit.v(TAG, "facebook:onCancel");
                // ...
                ShowAccountConnectProblemTip("facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                LogUnit.v(TAG, "facebook:onError" + error.toString());
                ShowAccountConnectProblemTip(error.toString());
                //mAuth.getCurrentUser().linkWithCredential()
            }
        });
    }

    /**
     * Addition another credential  method
     * *@param  addLoginCredentialState if 0 is for FB, if 1 is for Google
     **/
    private void handleAdditionLoginWithCredential(int addLoginCredentialState, String token) {
        if (LoginStateForFB) return;
        if (token == null) {
            LogUnit.v(TAG, "token is null or Token is null");
            Toast.makeText(getContext(), "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        AuthCredential credential = null;
        switch (addLoginCredentialState) {
            case 0://for FB
                credential = FacebookAuthProvider.getCredential(token);
                break;
            case 1://for Google
                credential = GoogleAuthProvider.getCredential(token, null);
                break;
//            case 2:
//                 credential = EmailAuthProvider.getCredential(email, password);
//                break;
        }
        if (credential == null) {
            Toast.makeText(getContext(), "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        MainActivity.mAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    LoginStateForFB = true;
                    LogUnit.v(TAG, "Facebook signInWithCredential:success");

                } else {

                    // If sign in fails, display a message to the user.
                    LogUnit.e(TAG, "Facebook signInWithCredential:failure" + task.getException());
//                    String result = task.getException().toString();
//                    if (result != null && result.contains("account")
//                            && result.contains("already") && result.contains("exists")
//                            && result.contains("same") && result.contains("email")
//                            && result.contains("different") && result.contains("credentials")) {
//                        ShowAccountDuplicateTip();
//                    } else {
//                        ShowAccountConnectProblemTip(task.getException().toString());
//                    }
                }
            }

        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        LogUnit.v(TAG, "requestCode: " + requestCode);
        switch (requestCode) {
            case LoginActivity.REQUEST_CODE_SIGN_IN:
                LogUnit.v(TAG, "REQUEST_CODE_SIGN_IN");
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(resultData);
                hideProgressDialog();
                try {
                    // handleSignInResult(task);
                    GoogleSignInAccount account = task.getResult(ApiException.class);

                    handleAdditionLoginWithCredential(1, account.getIdToken());
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    LogUnit.e(TAG, "Google sign in failed " + e.toString());
                    // ...

                    ShowAccountConnectProblemTip(e.toString());
                }

                break;
            default:
                LogUnit.e(TAG, "default requestCode: " + requestCode);
                mCallbackManager.onActivityResult(requestCode, resultCode, resultData);
                break;

        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /*********showProgressDialog********/
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext(), R.style.AppTheme_Dark_Dialog);
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

        FragmentManager mFragmentManager = getFragmentManager();
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