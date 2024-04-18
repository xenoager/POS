package com.aidevu.pos.ui.common.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.aidevu.pos.base.BaseDialog;
import com.aidevu.pos.databinding.DialogAlertBinding;
import com.aidevu.pos.interfaces.DialogOnClickListener;

public class AlertDialogView extends BaseDialog<DialogAlertBinding> {

    private Context context;
    private DialogOnClickListener dialogOnClickListener;
    private String date;
    private static final int CALENDAR = 0;
    private static final int SPINNER = 1;

    private int CALENDAR_MODE = SPINNER;

    public static final int TYPE_ONE_BUTTON = 1;
    public static final int TYPE_TWO_BUTTON = 2;
    public static int TYPE_BUTTON = TYPE_ONE_BUTTON;

    private boolean setCancelBtnDismiss = true;

    @NonNull
    @Override
    protected DialogAlertBinding createViewBinding(LayoutInflater layoutInflater) {
        return DialogAlertBinding.inflate(layoutInflater);
    }

    public AlertDialogView(@NonNull Context context, DialogOnClickListener dialogOnClickListener, int type) {
        super(context);
        this.context = context;
        this.dialogOnClickListener = dialogOnClickListener;
        TYPE_BUTTON = type;

        if (TYPE_BUTTON == TYPE_ONE_BUTTON) {
            binding.tvCancel.setVisibility(View.GONE);
            binding.tvApply.setVisibility(View.GONE);
        } else if (TYPE_BUTTON == TYPE_TWO_BUTTON) {
            binding.tvCancel.setVisibility(View.VISIBLE);
            binding.tvApply.setVisibility(View.VISIBLE);
        }
    }

    public AlertDialogView(@NonNull Context context, DialogOnClickListener dialogOnClickListener, int type, boolean setCancelBtnDismiss) {
        super(context);
        this.context = context;
        this.dialogOnClickListener = dialogOnClickListener;
        TYPE_BUTTON = type;
        this.setCancelBtnDismiss = setCancelBtnDismiss;

        if (TYPE_BUTTON == TYPE_ONE_BUTTON) {
            binding.tvCancel.setVisibility(View.GONE);
            binding.tvApply.setVisibility(View.GONE);
        } else if (TYPE_BUTTON == TYPE_TWO_BUTTON) {
            binding.tvCancel.setVisibility(View.VISIBLE);
            binding.tvApply.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding.tvApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != dialogOnClickListener) dialogOnClickListener.onClick(date);
                dismiss();
            }
        });

        binding.tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != dialogOnClickListener) dialogOnClickListener.onCancel();
                if (setCancelBtnDismiss) dismiss();
            }
        });
    }

    public void setTitle(String title) {
        binding.tvTitle.setText(title);
    }

    public void setContent(String content) {
        binding.tvBody.setText(content);
    }

    public void setButtonNameConfirm(String btnName) {
        binding.tvApply.setText(btnName);
    }

    public void setButtonNameCancel(String btnName) {
        binding.tvCancel.setText(btnName);
    }
}