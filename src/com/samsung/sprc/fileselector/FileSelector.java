package com.samsung.sprc.fileselector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Create the file selection dialog. This class will create a custom dialog for
 * file selection which can be used to save files.
 */
public class FileSelector {

	/** The list of files and folders which you can choose from */
	private ListView mFileListView;

	/** Button to save/load file */
	private Button mSaveLoadButton;
	/** Cancel Button - close dialog */
	private Button mCancelButton;
	/** Button to create a new folder */
	private Button mNewFolderButton;

	/** Spinner by which to select the file type filtering */
	private Spinner mFilterSpinner;
	
	private TextView mTitle;

	/**
	 * Indicates current location in the directory structure displayed in the
	 * dialog.
	 */
	private CurrentLocation mCurrentLocation;

	/**
	 * The file selector dialog.
	 */
	private final Dialog mDialog;

	private Context mContext;

	/** Save or Load file listener. */
	final OnHandleFileListener mOnHandleFileListener;

	/**
	 * Constructor that creates the file selector dialog.
	 * 
	 * @param context
	 *            The current context.
	 * @param operation
	 *            LOAD - to load file / SAVE - to save file
	 * @param onHandleFileListener
	 *            Notified after pressing the save or load button.
	 * @param fileFilters
	 *            Array with filters
	 */
	public FileSelector(final Context context, final FileOperation operation,
			final OnHandleFileListener onHandleFileListener, final String[] fileFilters) {
		mContext = context;
		mOnHandleFileListener = onHandleFileListener;
		mCurrentLocation = new CurrentLocation();

		Dialog d = mDialog = new Dialog(context, R.style.dialogStyle);
		try {
			int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | 0x80000000;
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.getWindow().setFlags(flags, flags);
			LayoutParams a = d.getWindow().getAttributes();
			a.gravity = Gravity.CENTER;
			a.dimAmount = 0.0f;
			d.getWindow().setAttributes(a);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mDialog.setContentView(R.layout.dialog);
		mTitle = (TextView)mDialog.findViewById(R.id.tv_title);

		final File sdCard = Environment.getExternalStorageDirectory();
		if (sdCard.canRead()) {
			mCurrentLocation.setLocation(sdCard);
		} else {
			mCurrentLocation.setLocation(Environment.getRootDirectory());
		}
		prepareFilterSpinner(fileFilters);
		prepareFilesList();

		setSaveLoadButton(operation);
		setNewFolderButton(operation);
		setCancelButton();

	}

	/**
	 * This method prepares a filter's list with the String's array
	 * 
	 * @param aFilesFilter
	 *            - array of filters, the elements of the array will be used as
	 *            elements of the spinner
	 */
	private void prepareFilterSpinner(String[] fitlesFilter) {
		mFilterSpinner = (Spinner) mDialog.findViewById(R.id.fileFilter);
		if (fitlesFilter == null || fitlesFilter.length == 0) {
			fitlesFilter = new String[] { FileUtils.FILTER_ALLOW_ALL };
			mFilterSpinner.setEnabled(false);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, fitlesFilter);

		mFilterSpinner.setAdapter(adapter);
		OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> aAdapter, View aView, int arg2, long arg3) {
				TextView textViewItem = (TextView) aView;
				String filtr = textViewItem.getText().toString();
				makeList(mCurrentLocation.getLocation(), filtr);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		};
		mFilterSpinner.setOnItemSelectedListener(onItemSelectedListener);
	}

	/**
	 * This method prepares the mFileListView
	 * 
	 */
	private void prepareFilesList() {
		mFileListView = (ListView) mDialog.findViewById(R.id.fileList);

		mFileListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				// Check if "../" item should be added.
				((EditText) mDialog.findViewById(R.id.fileName)).setText("");
				if (id == 0) {
					final String parentLocation = mCurrentLocation.getParent();
					if (parentLocation != null) { // text == "../"
						String fileFilter = ((TextView) mFilterSpinner.getSelectedView()).getText().toString();
						mCurrentLocation.setLocation(new File(parentLocation));
						makeList(mCurrentLocation.getLocation(), fileFilter);
					} else {
						onItemSelect(parent, position);
					}
				} else {
					onItemSelect(parent, position);
				}
			}
		});
		String filtr = mFilterSpinner.getSelectedItem().toString();
		makeList(mCurrentLocation.getLocation(), filtr);
	}

	/**
	 * The method that fills the list with a directories contents.
	 * 
	 * @param location
	 *            Indicates the directory whose contents should be displayed in
	 *            the dialog.
	 * @param fitlesFilter
	 *            The filter specifies the type of file to be displayed
	 */
	private void makeList(final File location, final String fitlesFilter) {
		final ArrayList<FileData> fileList = new ArrayList<FileData>();
		final String parentLocation = location.getParent();
		if (parentLocation != null) {
			// First item on the list.
			fileList.add(new FileData("../", FileData.UP_FOLDER));
		}
		File listFiles[] = location.listFiles();
		if (listFiles != null) {
			ArrayList<FileData> fileDataList = new ArrayList<FileData>();
			for (int index = 0; index < listFiles.length; index++) {
				File tempFile = listFiles[index];
				if (FileUtils.accept(tempFile, fitlesFilter)) {
					int type = tempFile.isDirectory() ? FileData.DIRECTORY : FileData.FILE;
					fileDataList.add(new FileData(listFiles[index].getName(), type));
				}
			}
			fileList.addAll(fileDataList);
			Collections.sort(fileList);
		}
		// Fill the list with the contents of fileList.
		if (mFileListView != null) {
			FileListAdapter adapter = new FileListAdapter(mContext, fileList, 1);
			mFileListView.setAdapter(adapter);
		}
	}

	/**
	 * Handle the file list item selection.
	 * 
	 * Change the directory on the list or change the name of the saved file if
	 * the user selected a file.
	 * 
	 * @param parent
	 *            First parameter of the onItemClick() method of
	 *            OnItemClickListener. It's a value of text property of the
	 *            item.
	 * @param position
	 *            Third parameter of the onItemClick() method of
	 *            OnItemClickListener. It's the index on the list of the
	 *            selected item.
	 */
	private void onItemSelect(final AdapterView<?> parent, final int position) {
		final String itemText = ((FileData) parent.getItemAtPosition(position)).getFileName();
		final String itemPath = mCurrentLocation.getAbsolutePath() + File.separator + itemText;
		final File itemLocation = new File(itemPath);

		if (!itemLocation.canRead()) {
			Toast.makeText(mContext, "Access denied!!!", Toast.LENGTH_SHORT).show();
		} else if (itemLocation.isDirectory()) {
			mCurrentLocation.setLocation(itemLocation);
			String fileFilter = ((TextView) mFilterSpinner.getSelectedView()).getText().toString();
			makeList(mCurrentLocation.getLocation(), fileFilter);
		} else if (itemLocation.isFile()) {
			final EditText fileName = (EditText) mDialog.findViewById(R.id.fileName);
			fileName.setText(itemText);
		}
	}

	/**
	 * Set button name and click handler for Save or Load button.
	 * 
	 * @param operation
	 *            Performed file operation.
	 */
	private void setSaveLoadButton(final FileOperation operation) {
		mSaveLoadButton = (Button) mDialog.findViewById(R.id.fileSaveLoad);
		switch (operation) {
		case SAVE:
			mSaveLoadButton.setText(R.string.saveButtonText);
			break;
		case LOAD:
			mSaveLoadButton.setText(R.string.loadButtonText);
			break;
		}
		mSaveLoadButton.setOnClickListener(new SaveLoadClickListener(operation, this, mContext));
	}

	/**
	 * Set button visibility and click handler for New folder button.
	 * 
	 * @param operation
	 *            Performed file operation.
	 */
	private void setNewFolderButton(final FileOperation operation) {
		mNewFolderButton = (Button) mDialog.findViewById(R.id.newFolder);
		OnClickListener newFolderListener = new OnClickListener() {
			@Override
			public void onClick(final View v) {
				openNewFolderDialog();
			}
		};
		switch (operation) {
		case SAVE:
			mNewFolderButton.setVisibility(View.VISIBLE);
			mNewFolderButton.setOnClickListener(newFolderListener);
			break;
		case LOAD:
			mNewFolderButton.setVisibility(View.GONE);
			break;
		}
	}

	/** Opens a dialog for creating a new folder. */
	private void openNewFolderDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		alert.setTitle(R.string.newFolderButtonText);
		alert.setMessage(R.string.newFolderDialogMessage);
		final EditText input = new EditText(mContext);
		alert.setView(input);
		alert.setPositiveButton(R.string.createButtonText, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int whichButton) {
				File file = new File(mCurrentLocation.getAbsolutePath() + File.separator + input.getText().toString());
				if (file.mkdir()) {
					Toast t = Toast.makeText(mContext, R.string.folderCreationOk, Toast.LENGTH_SHORT);
					t.setGravity(Gravity.CENTER, 0, 0);
					t.show();
				} else {
					Toast t = Toast.makeText(mContext, R.string.folderCreationError, Toast.LENGTH_SHORT);
					t.setGravity(Gravity.CENTER, 0, 0);
					t.show();
				}
				String fileFilter = ((TextView) mFilterSpinner.getSelectedView()).getText().toString();
				makeList(mCurrentLocation.getLocation(), fileFilter);
			}
		});
		alert.show();
	}

	/** Set onClick() event handler for the cancel button. */
	private void setCancelButton() {
		mCancelButton = (Button) mDialog.findViewById(R.id.fileCancel);
		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View view) {
				mDialog.cancel();
			}
		});
	}

	public String getSelectedFileName() {
		final EditText fileName = (EditText) mDialog.findViewById(R.id.fileName);
		return fileName.getText().toString();
	}

	public File getCurrentLocation() {
		return mCurrentLocation.getLocation();
	}

	/** Simple wrapper around the Dialog.show() method. */
	public void show() {
		mDialog.show();
	}

	/** Simple wrapper around the Dialog.dissmiss() method. */
	public void dismiss() {
		mDialog.dismiss();
	}
	
	private class CurrentLocation{
		private File location;
		public void setLocation(File location){
			this.location = location;
			//mDialog.setTitle(location.getAbsolutePath());
			mTitle.setText(location.getAbsolutePath());
		}
		public File getLocation(){
			return location;
		}
		public String getParent(){
			return location.getParent();
		}
		public File getParentFile(){
			return location.getParentFile();
		}
		public String getAbsolutePath(){
			return location.getAbsolutePath();
		}
	}
}
