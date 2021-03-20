/*
 * Copyright 2015 LinkedIn Corp. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.example.qark_test.fileborwser;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.qark_test.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileBrowserFragment extends Fragment {

    private final int REQUEST_CODE_PICK_DIR = 1;
    private final int REQUEST_CODE_PICK_FILE = 2;

    // Intent Action Constants
    public static final String INTENT_ACTION_SELECT_DIR = "com.secbro.qark.SELECT_DIRECTORY_ACTION";
    public static final String INTENT_ACTION_SELECT_FILE = "com.secbro.qark.SELECT_FILE_ACTION";

    // Intent parameters names constants
    public static final String startDirectoryParameter = "com.secbro.qark.directoryPath";
    public static final String returnDirectoryParameter = "com.secbro.qark.directoryPathRet";
    public static final String returnFileParameter = "com.secbro.qark.filePathRet";
    public static final String showCannotReadParameter = "com.secbro.qark.showCannotRead";
    public static final String filterExtension = "com.secbro.qark.filterExtension";

    // Stores names of traversed directories
    ArrayList<String> pathDirsList = new ArrayList<String>();

    // Check if the first level of the directory structure is the one showing
    // private Boolean firstLvl = true;

    private static final String LOGTAG = "F_PATH";

    private List<Item> fileList = new ArrayList<Item>();
    private File path = null;
    private String chosenFile;
    // private static final int DIALOG_LOAD_FILE = 1000;

    ArrayAdapter<Item> adapter;

    private boolean showHiddenFilesAndDirs = true;

    private boolean directoryShownIsEmpty = false;

    private String filterFileExtension = null;

    // Action constants
    private static int currentAction = -1;
    private static final int SELECT_DIRECTORY = 1;
    private static final int SELECT_FILE = 2;

    public static FileBrowserFragment newInstance() {
        FileBrowserFragment fragment = new FileBrowserFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View retVal = inflater.inflate(R.layout.fragment_file_browser, container, false);

//        // Set action for this activity
//        Intent thisInt = this.getActivity().getIntent();
//        currentAction = SELECT_DIRECTORY;// This would be a default action in
//        // case not set by intent
        if (getArguments() != null ) {
            if (getArguments().getString(FileBrowserFragment.INTENT_ACTION_SELECT_FILE).equalsIgnoreCase(INTENT_ACTION_SELECT_FILE)) {
                Log.d(LOGTAG, "SELECT ACTION - SELECT FILE");
                currentAction = SELECT_FILE;
            }
        }

//        showHiddenFilesAndDirs = thisInt.getBooleanExtra(
//                showCannotReadParameter, true);
//
//        filterFileExtension = thisInt.getStringExtra(filterExtension);


       return retVal;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FileBrowserFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setInitialDirectory();
        parseDirectoryPath();
        loadFileList();
        createFileListAdapter();
        initializeButtons();
        initializeFileListView();
        updateCurrentDirectoryTextView();
        Log.d(LOGTAG, path.getAbsolutePath());

    }

    private void setInitialDirectory() {
        Intent thisInt = this.getActivity().getIntent();
        String requestedStartDir = thisInt
                .getStringExtra(startDirectoryParameter);

        if (requestedStartDir != null && requestedStartDir.length() > 0) {// if(requestedStartDir!=null
            File tempFile = new File(requestedStartDir);
            if (tempFile.isDirectory())
                this.path = tempFile;
        }// if(requestedStartDir!=null

        if (this.path == null) {// No or invalid directory supplied in intent
            // parameter
            if (Environment.getExternalStorageDirectory().isDirectory()
                    && Environment.getExternalStorageDirectory().canRead())
                path = Environment.getExternalStorageDirectory();
            else
                path = new File("/");
        }// if(this.path==null) {//No or invalid directory supplied in intent
        // parameter
    }// private void setInitialDirectory() {

    private void parseDirectoryPath() {
        pathDirsList.clear();
        String pathString = path.getAbsolutePath();
        String[] parts = pathString.split("/");
        int i = 0;
        while (i < parts.length) {
            pathDirsList.add(parts[i]);
            i++;
        }
    }

    private void initializeButtons() {
        Button upDirButton = (Button) getView().findViewById(R.id.upDirectoryButton);
        upDirButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(LOGTAG, "onclick for upDirButton");
                loadDirectoryUp();
                loadFileList();
                adapter.notifyDataSetChanged();
                updateCurrentDirectoryTextView();
            }
        });// upDirButton.setOnClickListener(

        Button selectFolderButton = (Button) getView().findViewById(R.id.selectCurrentDirectoryButton);
        if (currentAction == SELECT_DIRECTORY) {
            selectFolderButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.d(LOGTAG, "onclick for selectFolderButton");
                    returnDirectoryFinishActivity();
                }
            });
        } else {// if(currentAction == this.SELECT_DIRECTORY) {
            selectFolderButton.setVisibility(View.GONE);
        }// } else {//if(currentAction == this.SELECT_DIRECTORY) {
    }// private void initializeButtons() {

    private void loadDirectoryUp() {
        // present directory removed from list
        String s = pathDirsList.remove(pathDirsList.size() - 1);
        // path modified to exclude present directory
        path = new File(path.toString().substring(0,
                path.toString().lastIndexOf(s)));
        fileList.clear();
    }

    private void updateCurrentDirectoryTextView() {
        int i = 0;
        String curDirString = "";
        while (i < pathDirsList.size()) {
            curDirString += pathDirsList.get(i) + "/";
            i++;
        }
        if (pathDirsList.size() == 0) {
            ((Button) getView().findViewById(R.id.upDirectoryButton))
                    .setEnabled(false);
            curDirString = "/";
        } else
            ((Button) getView().findViewById(R.id.upDirectoryButton))
                    .setEnabled(true);
        long freeSpace = getFreeSpace(curDirString);
        String formattedSpaceString = formatBytes(freeSpace);
        if (freeSpace == 0) {
            Log.d(LOGTAG, "NO FREE SPACE");
            File currentDir = new File(curDirString);
            if (!currentDir.canWrite())
                formattedSpaceString = "NON Writable";
        }

        ((Button) getView().findViewById(R.id.selectCurrentDirectoryButton))
                .setText("Select\n[" + formattedSpaceString
                        + "]");

        ((TextView) getView().findViewById(R.id.currentDirectoryTextView))
                .setText("Current directory: " + curDirString);
    }// END private void updateCurrentDirectoryTextView() {

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void initializeFileListView() {
        ListView lView = (ListView) getView().findViewById(R.id.fileListView);
        lView.setBackgroundColor(Color.LTGRAY);
        LinearLayout.LayoutParams lParam = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        lParam.setMargins(15, 5, 15, 5);
        lView.setAdapter(this.adapter);
        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                chosenFile = fileList.get(position).file;
                File sel = new File(path + "/" + chosenFile);
                Log.d(LOGTAG, "Clicked:" + chosenFile);
                if (sel.isDirectory()) {
                    if (sel.canRead()) {
                        // Adds chosen directory to list
                        pathDirsList.add(chosenFile);
                        path = new File(sel + "");
                        Log.d(LOGTAG, "Just reloading the list");
                        loadFileList();
                        adapter.notifyDataSetChanged();
                        updateCurrentDirectoryTextView();
                        Log.d(LOGTAG, path.getAbsolutePath());
                    } else {// if(sel.canRead()) {
                        showToast("Path does not exist or cannot be read");
                    }// } else {//if(sel.canRead()) {
                }// if (sel.isDirectory()) {
                // File picked or an empty directory message clicked
                else {// if (sel.isDirectory()) {
                    Log.d(LOGTAG, "item clicked");
                    if (!directoryShownIsEmpty) {
                        Log.d(LOGTAG, "File selected:" + chosenFile);
                        returnFileFinishActivity(sel.getAbsolutePath());
                    }
                }// else {//if (sel.isDirectory()) {
            }// public void onClick(DialogInterface dialog, int which) {
        });// lView.setOnClickListener(
    }// private void initializeFileListView() {

    private void returnDirectoryFinishActivity() {
        Intent retIntent = new Intent();
        retIntent.putExtra(returnDirectoryParameter, path.getAbsolutePath());
        getActivity().setResult(getActivity().RESULT_OK, retIntent);
        getActivity().finish();
    }// END private void returnDirectoryFinishActivity() {

    private void returnFileFinishActivity(String filePath) {
        Intent retIntent = new Intent();
        retIntent.putExtra(returnFileParameter, filePath);
        getActivity().setResult(getActivity().RESULT_OK, retIntent);
        getActivity().finish();
    }// END private void returnDirectoryFinishActivity() {

    private void loadFileList() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            Log.e(LOGTAG, "unable to write on the sd card ");
        }
        fileList.clear();

        if (path.exists() && path.canRead()) {
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    boolean showReadableFile = showHiddenFilesAndDirs
                            || sel.canRead();
                    // Filters based on whether the file is hidden or not
                    if (currentAction == SELECT_DIRECTORY) {
                        return (sel.isDirectory() && showReadableFile);
                    }
                    if (currentAction == SELECT_FILE) {

                        // If it is a file check the extension if provided
                        if (sel.isFile() && filterFileExtension != null) {
                            return (showReadableFile && sel.getName().endsWith(
                                    filterFileExtension));
                        }
                        return (showReadableFile);
                    }
                    return true;
                }// public boolean accept(File dir, String filename) {
            };// FilenameFilter filter = new FilenameFilter() {

            String[] fList = path.list(filter);
            this.directoryShownIsEmpty = false;
            for (int i = 0; i < fList.length; i++) {
                // Convert into file path
                File sel = new File(path, fList[i]);
                Log.d(LOGTAG,
                        "File:" + fList[i] + " readable:"
                                + (Boolean.valueOf(sel.canRead())).toString());
                int drawableID = R.drawable.file_icon;
                boolean canRead = sel.canRead();
                // Set drawables
                if (sel.isDirectory()) {
                    if (canRead) {
                        drawableID = R.drawable.folder_icon;
                    } else {
                        drawableID = R.drawable.folder_icon_light;
                    }
                }
                fileList.add(i, new Item(fList[i], drawableID, canRead));
            }// for (int i = 0; i < fList.length; i++) {
            if (fileList.size() == 0) {
                // Log.d(LOGTAG, "This directory is empty");
                this.directoryShownIsEmpty = true;
                fileList.add(0, new Item("Directory is empty", -1, true));
            } else {// sort non empty list
                Collections.sort(fileList, new ItemFileNameComparator());
            }
        } else {
            Log.e(LOGTAG, "path does not exist or cannot be read");
        }
        // Log.d(TAG, "loadFileList finished");
    }// private void loadFileList() {

    private void createFileListAdapter() {
        adapter = new ArrayAdapter<Item>(getActivity(),
                android.R.layout.select_dialog_item, android.R.id.text1,
                fileList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // creates view
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view
                        .findViewById(android.R.id.text1);
                // put the image on the text view
                int drawableID = 0;
                if (fileList.get(position).icon != -1) {
                    // If icon == -1, then directory is empty
                    drawableID = fileList.get(position).icon;
                }
                textView.setCompoundDrawablesWithIntrinsicBounds(drawableID, 0,
                        0, 0);

                textView.setEllipsize(null);

                // add margin between image and text (support various screen
                // densities)
                // int dp5 = (int) (5 *
                // getResources().getDisplayMetrics().density + 0.5f);
                int dp3 = (int) (3 * getResources().getDisplayMetrics().density + 0.5f);
                // TODO: change next line for empty directory, so text will be
                // centered
                textView.setCompoundDrawablePadding(dp3);
                textView.setBackgroundColor(Color.LTGRAY);
                return view;
            }// public View getView(int position, View convertView, ViewGroup
        };// adapter = new ArrayAdapter<Item>(this,
    }// private createFileListAdapter(){

    private class Item {
        public String file;
        public int icon;
        public boolean canRead;

        public Item(String file, Integer icon, boolean canRead) {
            this.file = file;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return file;
        }
    }// END private class Item {

    private class ItemFileNameComparator implements Comparator<Item> {
        public int compare(Item lhs, Item rhs) {
            return lhs.file.toLowerCase().compareTo(rhs.file.toLowerCase());
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(LOGTAG, "ORIENTATION_LANDSCAPE");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(LOGTAG, "ORIENTATION_PORTRAIT");
        }
        // Layout apparently changes itself, only have to provide good onMeasure
        // in custom components
        // TODO: check with keyboard
        // if(newConfig.keyboard == Configuration.KEYBOARDHIDDEN_YES)
    }// END public void onConfigurationChanged(Configuration newConfig) {

    public static long getFreeSpace(String path) {
        StatFs stat = new StatFs(path);
        long availSize = (long) stat.getAvailableBlocks()
                * (long) stat.getBlockSize();
        return availSize;
    }// END public static long getFreeSpace(String path) {

    public static String formatBytes(long bytes) {
        // TODO: add flag to which part is needed (e.g. GB, MB, KB or bytes)
        String retStr = "";
        // One binary gigabyte equals 1,073,741,824 bytes.
        if (bytes > 1073741824) {// Add GB
            long gbs = bytes / 1073741824;
            retStr += (new Long(gbs)).toString() + "GB ";
            bytes = bytes - (gbs * 1073741824);
        }
        // One MB - 1048576 bytes
        if (bytes > 1048576) {// Add GB
            long mbs = bytes / 1048576;
            retStr += (new Long(mbs)).toString() + "MB ";
            bytes = bytes - (mbs * 1048576);
        }
        if (bytes > 1024) {
            long kbs = bytes / 1024;
            retStr += (new Long(kbs)).toString() + "KB";
            bytes = bytes - (kbs * 1024);
        } else
            retStr += (new Long(bytes)).toString() + " bytes";
        return retStr;
    }
}