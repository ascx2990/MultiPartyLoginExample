package com.example.multipartyloginexample.login;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.multipartyloginexample.LogUnit;
import com.example.multipartyloginexample.R;
import com.example.multipartyloginexample.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    private EditText _nameText, _addressText, _emailText, _mobileText, _passwordText, _reEnterPasswordText;
    private Button _signupButton;
    private TextView _loginLink;
    private FirebaseAuth mAuth;
    //private FirebaseFirestore mDb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUnit.d(TAG, "onCreate");
        setContentView(R.layout.activity_signup);
        findView();
        initial();
        onListener();
    }

    private void findView() {
        _nameText = findViewById(R.id.input_name);
        _addressText = findViewById(R.id.input_address);
        _emailText = findViewById(R.id.input_email);
        _mobileText = findViewById(R.id.input_mobile);
        _passwordText = findViewById(R.id.input_password);
        _reEnterPasswordText = findViewById(R.id.input_reEnterPassword);
        _signupButton = findViewById(R.id.btn_signup);
        _loginLink = findViewById(R.id.link_login);
    }

    private void initial() {
        mAuth = FirebaseAuth.getInstance();
        //mDb = FirebaseFirestore.getInstance();
    }

    private void onListener() {
        _signupButton.setOnClickListener(view -> signup());
        _loginLink.setOnClickListener(view -> {
            // Finish the registration screen and return to the Login activity
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        });
    }

    public void signup() {
        LogUnit.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.signup_creat_account_loading));
        progressDialog.show();

        String name = _nameText.getText().toString();
        String address = _addressText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        // TODO: Implement your own signup logic here.
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (!task.isSuccessful()) {
                            // If sign in fails, display a message to the user.
                            LogUnit.e(TAG, "createUserWithEmail:failure" + task.getException());
                            if (task.getException().toString().contains("email address is already")) {
                                Toast.makeText(SignupActivity.this, getString(R.string.signup_text_email_duplicate),
                                        Toast.LENGTH_SHORT).show();
                                //if you wnat to check the user's information ,you can query data form firebase.

//                                FirebaseUser user = mAuth.getCurrentUser();
//                                LogUnit.v(TAG, "UID:" + user.getUid());
//                                getUserInfo(user.getUid());
                            } else {
                                Toast.makeText(SignupActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            _signupButton.setEnabled(true);

                            //updateUI(user);
                            return;
                        }
                        // Sign in success, update UI with the signed-in user's information
                        LogUnit.v(TAG, "createUserWithEmail:success");
                        //send verify Email.
                        FirebaseUser user = mAuth.getCurrentUser();
                        user.sendEmailVerification();
                        ShowSendEmailVerifyDialog(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent it = new Intent();
                                it.putExtra(LoginActivity.USER_EMAIL, user.getEmail());
                                _signupButton.setEnabled(true);
                                setResult(RESULT_OK, it);
                                finish();
                            }
                        });

//                        Map<String, Object> userMap = new HashMap<>();
//                        userMap.put("name", name);
//                        userMap.put("address", address);
//                        userMap.put("email", email);
//                        userMap.put("mobile", mobile);
//                        mDb.collection("contacts").document(user.getUid()).set(userMap).
//                                addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//                                        Log.w(TAG, "onSuccess");
//                                        Intent it = new Intent();
//                                        it.putExtra(LoginActivity.USER_EMAIL, user.getEmail());
//                                        _signupButton.setEnabled(true);
//                                        setResult(RESULT_OK, it);
//                                        finish();
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.w(TAG, "Error adding document", e);
//                            }
//                        });

//
//                                addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                                    @Override
//                                    public void onSuccess(DocumentReference documentReference) {
//                                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Log.w(TAG, "Error adding document", e);
//                                    }
//                                });

                    }
                });
//        new android.os.Handler().postDelayed(
//                new Runnable() {
//                    public void run() {
//                        // On complete call either onSignupSuccess or onSignupFailed
//                        // depending on success
//                        onSignupSuccess();
//                        // onSignupFailed();
//                        progressDialog.dismiss();
//                    }
//                }, 3000);
    }

    //    private void getUserInfo(String uId) {
//        DocumentReference docRef = mDb.collection("contacts").document(uId);
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
//                    } else {
//                        Log.d(TAG, "No such document");
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });
//    }
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

    private void ShowSendEmailVerifyDialog(DialogInterface.OnClickListener confirmListener) {
        FragmentTransaction mFragTransaction = checkDialogFragment(AlertDialogFragment.class.getName());

        AlertDialogFragment dialog = new AlertDialogFragment(getString(R.string.dialog_title_signup_email_verify), getString(R.string.dialog_message_signup_email_verify),
                R.string.dialog_text_confirm, confirmListener, 0, null, 0, null);
        mFragTransaction.add(dialog, AlertDialogFragment.class.getName());
        mFragTransaction.commitAllowingStateLoss();
    }

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);

        setResult(RESULT_OK, null);
        finish();
    }


    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), getString(R.string.signup_login_failed), Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String address = _addressText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError(getString(R.string.signup_enter_valid_name));
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (address.isEmpty()) {
            _addressText.setError(getString(R.string.signup_enter_valid_address));
            valid = false;
        } else {
            _addressText.setError(null);
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError(getString(R.string.signup_enter_valid_email_address));
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length() != 10) {
            _mobileText.setError(getString(R.string.signup_enter_valid_mobile_number));
            valid = false;
        } else {
            _mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 20) {
            _passwordText.setError(getString(R.string.signup_enter_valid_password_number));
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 20 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError(getString(R.string.signup_enter_valid_password_match));
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }

}