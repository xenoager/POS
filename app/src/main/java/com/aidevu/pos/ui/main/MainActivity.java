package com.aidevu.pos.ui.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.aidevu.pos.R;
import com.aidevu.pos.base.BaseActivity;
import com.aidevu.pos.base.BaseViewModelFactory;
import com.aidevu.pos.databinding.ActivityMainBinding;
import com.aidevu.pos.interfaces.DialogOnClickListener;
import com.aidevu.pos.utils.Log;
import com.aidevu.pos.viewmodels.main.MainViewModel;
import java.util.List;
import dagger.hilt.android.AndroidEntryPoint;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

@AndroidEntryPoint
public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> implements EasyPermissions.PermissionCallbacks {

    private static final int WRITE_EXTERNAL_STORAGE_PERM = 123;
    public static Context context;

    @NonNull
    @Override
    protected MainViewModel createViewModel() {
        BaseViewModelFactory factory = new BaseViewModelFactory(this);
        return ViewModelProviders.of(this, factory).get(MainViewModel.class);
    }

    @NonNull
    @Override
    protected ActivityMainBinding createViewBinding(LayoutInflater layoutInflater) {
        return ActivityMainBinding.inflate(layoutInflater);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        init();

        // Observe data in the ViewModel
        observeData();

        new Handler().postDelayed(this::writeExternalStorageTask, 1000);
    }

    public void init() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d("권한 없음");
    }

    @AfterPermissionGranted(WRITE_EXTERNAL_STORAGE_PERM)
    public void writeExternalStorageTask() {
        if (hasWriteExternalStoragePermission()) {
            Log.d("시작");
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "requestPermissions",
                    WRITE_EXTERNAL_STORAGE_PERM,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private boolean hasWriteExternalStoragePermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void observeData() {
        super.observeData();
        viewModel.getErrorState().observe(this, errorStr -> {
            showErrorDialog("Error", errorStr.getErrorMessage(), new DialogOnClickListener() {
                @Override
                public void onClick(String str) {
                    Log.d("시작");
                }

                @Override
                public void onCancel() {}
            });
            hideDialog();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WRITE_EXTERNAL_STORAGE_PERM) {
            finish();
            if (hasWriteExternalStoragePermission()) {
                Log.d("시작");
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onNfcResult(String nfcId) {
    }
}