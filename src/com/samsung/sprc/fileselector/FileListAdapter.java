package com.samsung.sprc.fileselector;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter used to display a files list
 */
public class FileListAdapter extends BaseAdapter {

	/** Array of FileData objects that will be used to display a list */
	private final ArrayList<FileData> mFileDataArray;

	private final Context mContext;
	private int mode;

	public FileListAdapter(Context context, List<FileData> aFileDataArray, int mode) {
		mFileDataArray = (ArrayList<FileData>) aFileDataArray;
		mContext = context;
		this.mode = mode;
	}

	@Override
	public int getCount() {
		return mFileDataArray.size();
	}

	@Override
	public Object getItem(int position) {
		return mFileDataArray.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FileData tempFileData = mFileDataArray.get(position);
		int imgRes = -1;
		switch (mode) {
		case 0:
			if (convertView == null) convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_file_item, null);
			TextView label = (TextView) convertView.findViewById(R.id.tv_grid_item_label);
			label.setText(tempFileData.getFileName());
			ImageView icon = (ImageView) convertView.findViewById(R.id.iv_grid_item_image);
			switch (tempFileData.getFileType()) {
			case FileData.UP_FOLDER: {
				imgRes = R.drawable.thumb_toparent;
				break;
			}
			case FileData.DIRECTORY: {
				imgRes = R.drawable.thumb_folder;
				break;
			}
			case FileData.FILE: {
				imgRes = R.drawable.thumb_other;
				break;
			}
			}
			icon.setImageResource(imgRes);
			convertView.setTag(tempFileData);
			return convertView;
		case 1:
			TextViewWithImage tempView;
			if (convertView == null) tempView = new TextViewWithImage(mContext);
			else tempView = (TextViewWithImage) convertView;
			tempView.setText(tempFileData.getFileName());
			switch (tempFileData.getFileType()) {
			case FileData.UP_FOLDER: {
				imgRes = R.drawable.up_folder;
				break;
			}
			case FileData.DIRECTORY: {
				imgRes = R.drawable.folder;
				break;
			}
			case FileData.FILE: {
				imgRes = R.drawable.file;
				break;
			}
			}
			tempView.setImageResource(imgRes);
			convertView.setTag(tempFileData);
			return tempView;
		case 2:
			break;
		default:
			break;
		}
		convertView.setTag(tempFileData);
		return convertView;
	}

}
