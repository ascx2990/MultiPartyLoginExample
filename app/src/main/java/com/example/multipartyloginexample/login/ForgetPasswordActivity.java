package com.example.multipartyloginexample.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.multipartyloginexample.AlertDialogFragment;
import com.example.multipartyloginexample.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {
    private EditText _emailText;
    private Button btSendRestPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        findView();
        initial();
        listener();

    }

    private void findView() {
        _emailText = findViewById(R.id.input_email);
        btSendRestPassword = findViewById(R.id.bt_send_rest_email);
    }

    private void initial() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void listener() {
        btSendRestPassword.setOnClickListener(view -> sendRestEmail());
    }

    public void onSendFailed() {
        Toast.makeText(getBaseContext(), getString(R.string.forget_password_send_failed), Toast.LENGTH_LONG).show();
        btSendRestPassword.setEnabled(true);
    }

    private void sendRestEmail() {
        showProgressDialog();
        btSendRestPassword.setEnabled(false);
        if (!validate()) {
            onSendFailed();
            hideProgressDialog();
            return;
        }
        String email = _emailText.getText().toString();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                showProgressDialog();
                btSendRestPassword.setEnabled(true);
                if (task.isSuccessful()) {
                    hasSendRestEmailTip();
                } else {
                    Toast.makeText(getBaseContext(), getString(R.string.forget_password_send_failed), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean validate() {
        boolean valid = true;
        String email = _emailText.getText().toString();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError(getString(R.string.signip_enter_valid_email_address));
            valid = false;
        } else {
            _emailText.setError(null);
        }
        return valid;
    }

    /*********MessageDialog********/
    private void hasSendRestEmailTip() {

        FragmentTransaction mFragTransaction = checkDialogFragment(AlertDialogFragment.class.getName());
        AlertDialogFragment dialog = new AlertDialogFragment(getString(R.string.forget_password_title_already_send), getString(R.string.forget_password_message_already_send),
                R.string.dialog_text_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
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
    /*********MessageDialog********/
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
}
