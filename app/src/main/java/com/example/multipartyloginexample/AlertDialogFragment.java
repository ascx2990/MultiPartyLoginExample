package com.example.multipartyloginexample;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;

public class AlertDialogFragment extends DialogFragment {

    private String Title;
    private String Message;
    private DialogInterface.OnClickListener posListener;
    private DialogInterface.OnClickListener negativeListener;
    private DialogInterface.OnClickListener neutralListener;
    private int posId;
    private int negativeId;
    private int neutralId;
    private AlertDialog dialog;

    public AlertDialogFragment(String Title, String Message,
                               int posId, DialogInterface.OnClickListener posListener, int negativeId, DialogInterface.OnClickListener negativeListener,
                               int neutralId, DialogInterface.OnClickListener neutralListener) {
        this.Title = Title;
        this.Message = Message;

        this.posListener = posListener;
        this.negativeListener = negativeListener;
        this.neutralListener = neutralListener;
        this.posId = posId;
        this.negativeId = negativeId;
        this.neutralId = neutralId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_MaterialComponents_Light_Dialog);
        AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
        builder.setTitle(Title);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_normal_textview, null);
        TextView tv = (TextView) view.findViewById(R.id.tv_message);
        tv.setVerticalScrollBarEnabled(true);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv.setText(Message);
        builder.setView(view);
        if (posListener != null) {
            builder.setPositiveButton(posId, posListener);
        }
        if (negativeListener != null) {
            builder.setNegativeButton(negativeId, negativeListener);
        }
        if (neutralListener != null) {
            builder.setNeutralButton(neutralId, neutralListener);
        }
        dialog = builder.show();
        //set buttion of dialog size
//        Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//        Button btnNeutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
//        Button btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//        btnPositive.setTextSize(getResources().getDimension(R.dimen.dialog_button_size));
//        btnNeutral.setTextSize(getResources().getDimension(R.dimen.dialog_button_size));
//        btnNegative.setTextSize(getResources().getDimension(R.dimen.dialog_button_size));
        return dialog;
    }

}
