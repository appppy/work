/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.osaka.cherry.work.addedittask;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import jp.osaka.cherry.work.R;

import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_FILE_NAME;

/**
 * 編集ダイアログ
 */
public class TaskEditDialog extends AppCompatDialogFragment {

    /**
     * @serial リスナ
     */
    private TaskEditUserActionsListener mListener;

    /**
     * @serial 項目
     */
    private String mItem;

    /**
     * インスタンス作成
     *
     * @param item 項目
     * @return インスタンス
     */
    public static TaskEditDialog newInstance(String item) {

        TaskEditDialog f = new TaskEditDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(EXTRA_FILE_NAME, item);
        f.setArguments(args);

        return f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mItem = getArguments().getString(EXTRA_FILE_NAME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mListener = (TaskEditUserActionsListener) getActivity();

        // カスタム表示を設定
        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        final View layout = inflater.inflate(R.layout.dialog_edit, null);

        final DialogInterface.OnClickListener positive = (dialog, which) -> {
            EditText id;
            id = layout.findViewById(R.id.edit1);
            mListener.onPositiveButtonClicked(id.getText().toString());
        };
        final DialogInterface.OnClickListener negative = (dialog, which) -> mListener.onNegativeButtonClicked("");

        // Setup EditText
        @SuppressLint("CutPasteId") EditText editText1 = layout.findViewById(R.id.edit1);
        if (editText1 != null) {
            editText1.setText(mItem);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog;
        builder.setIcon(R.drawable.ic_create_black_24dp);
        builder.setView(layout);
        builder.setPositiveButton(android.R.string.ok, positive);
        builder.setNegativeButton(android.R.string.cancel, negative);
        dialog = builder.create();

        return dialog;
    }

}
