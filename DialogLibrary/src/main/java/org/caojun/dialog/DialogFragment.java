package org.caojun.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class DialogFragment extends BlurDialogFragment {

    private static String BUNDLE_KEY_RANDOM_STYLE = "BUNDLE_KEY_RANDOM_STYLE";
    private static DialogListener dialogListener;

    public static DialogFragment newInstance(DialogListener dialogListener, boolean cancelable) {
        return newInstance(dialogListener, cancelable, false);
    }

    public static DialogFragment newInstance(DialogListener dialogListener) {
        return newInstance(dialogListener, true, false);
    }

    /**
     * 实例化
     * @param cancelable 是否可取消
     * @param isRandomStyle 是否随机样式
     * @return DialogFragment
     */
    public static DialogFragment newInstance(DialogListener dialogListener, boolean cancelable, boolean isRandomStyle) {
        DialogFragment.dialogListener = dialogListener;
        DialogFragment fragment = new DialogFragment();
        fragment.setCancelable(cancelable);

        Bundle args = new Bundle();
        args.putBoolean(
                BUNDLE_KEY_RANDOM_STYLE,
                isRandomStyle
        );
        fragment.setArguments(args);

        return fragment;
    }

    private boolean isRandomStyle = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (DialogFragment.dialogListener != null) {
            return DialogFragment.dialogListener.onCreateDialog(savedInstanceState);
        }
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        readArguments();
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        readArguments();
//    }

    private void readArguments() {
        Bundle args = getArguments();
        if (args != null) {
            isRandomStyle = args.getBoolean(BUNDLE_KEY_RANDOM_STYLE);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (dialogListener != null) {
            dialogListener.onCancel();
        }
    }

    @Override
    public boolean isDimmingEnable() {
        if (isRandomStyle) {
            return RandomUtils.getRandom();
        } else {
            return super.isDimmingEnable();
        }
    }

    @Override
    public boolean isActionBarBlurred() {
        if (isRandomStyle) {
            return RandomUtils.getRandom();
        } else {
            return super.isActionBarBlurred();
        }
    }

    @Override
    public float getDownScaleFactor() {
        if (isRandomStyle) {
            return RandomUtils.getRandom(2, 22);
        } else {
            return super.getDownScaleFactor();
        }
    }

    @Override
    public int getBlurRadius() {
        if (isRandomStyle) {
            return RandomUtils.getRandom(1, 21);
        } else {
            return super.getBlurRadius();
        }
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        Dialog dialog = getDialog();
//        if (dialog != null) {
//
//            // 在 5.0 以下的版本会出现白色背景边框，若在 5.0 以上设置则会造成文字部分的背景也变成透明
//            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
//                // 目前只有这两个 dialog 会出现边框
//                if (dialog instanceof ProgressDialog || dialog instanceof DatePickerDialog) {
//                    getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                }
//            }
//
//            Window window = getDialog().getWindow();
//            WindowManager.LayoutParams windowParams = window.getAttributes();
//            windowParams.dimAmount = 0.0f;
//            window.setAttributes(windowParams);
//        }
//    }
}
