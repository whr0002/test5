package com.examples.gg.loadMore;

import java.util.Collections;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.examples.gg.feedManagers.FeedManager_Base_v3_PlaylistItem;
import com.rs.playlist.R;

public class PlaylistFragment extends LoadMore_Base {

	private String mPlaylistID;
	private String mTitle;

	public PlaylistFragment(String title, String playlistId) {
		this.mPlaylistID = playlistId;
		this.mTitle = title;
	}

	@Override
	public void Initializing() {
		abTitle = mTitle;
		String cAPI = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults="
				+ numOfResults
				+ "&playlistId="
				+ mPlaylistID
				+ "&key="
				+ browserKey;

//		Log.d("debug",cAPI);
		API.add(cAPI);

		// set a feed manager
		feedManager = new FeedManager_Base_v3_PlaylistItem("video", cAPI,
				browserKey, gv, numOfResults);

		// Show menu
		setHasOptionsMenu(true);
		if(abTitle.equals("Youtube Mix")){
			setOptionMenu(true, false);
		}else{
			setOptionMenu(false, true);
		}
	}

	@Override
	public void setDropdown() {
		if (hasDropDown) {
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

			final String[] catagory = { "Order: default", "Order: reversed",
					"Order: shuffled" };

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					ab.getThemedContext(), R.layout.sherlock_spinner_item,
					android.R.id.text1, catagory);

			adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

			ab.setListNavigationCallbacks(adapter, this);

			ab.setSelectedNavigationItem(currentPosition);
		} else {
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}
	}

	@Override
	protected void setGridViewItemClickListener() {

		gv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Playlist activity
				Intent i = new Intent(sfa, YoutubeActionBarActivity.class);
				// i.putExtra("isfullscreen", true);
				// i.putExtra("videoId", videolist.get(position).getVideoId());
				i.putExtra("video", videolist.get(position));
				i.putExtra("videoDate", videolist.get(position).getUpdateTime());
				i.putParcelableArrayListExtra("videoList", videolist);
				i.putExtra("playlistID", mPlaylistID);
				i.putExtra("positionOfList", position);
				startActivity(i);
			}
		});

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		
		if (firstTime) {
			firstTime = false;
			return true;
		}
		
		if (videolist != null) {

			if (itemPosition == 0) {
				// Redo request to get its default order
				String firstApi = API.get(0);
				API.clear();
				API.add(firstApi);
				isMoreVideos = true;
				titles.clear();
				videolist.clear();
				setListView();

			} else if (itemPosition == 1) {
				Collections.reverse(videolist);

			} else if (itemPosition == 2) {
				Collections.shuffle(videolist);

			}
			gv.invalidateViews();
		}
		return false;

	}
	@Override
	public void refreshFragment() {
		ab.setSelectedNavigationItem(0);
	}
}
