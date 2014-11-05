package com.examples.gg.loadMore;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.examples.gg.adapters.ListViewAdapter;
import com.examples.gg.data.CustomSearchView;
import com.examples.gg.data.MyAsyncTask;
import com.examples.gg.feedManagers.FeedManager_Base_v3;
import com.examples.gg.feedManagers.FeedManager_Suggestion;
import com.rs.playlist.R;

public class SearchFragment extends LoadMore_Base implements
		SearchView.OnQueryTextListener, ListView.OnItemClickListener {

	private int mediaType;
	private int sortType;
	private boolean spin2FirstTime = true;
	private boolean isSelected = false;
	private String curQuery;
	protected String nextFragmentAPI;
	private final String suggestionBaseAPI = "http://suggestqueries.google.com/complete/search?hl=en&ds=yt&client=youtube&hjson=t&q=";

	private ListView suggestedList;
	private ListViewAdapter suggestedListAdapter;
	private ArrayList<String> suggestedKeywords;
	private GetSuggestedWordsTask mTask;
	private FeedManager_Suggestion mFeedManager;

	private Spinner spin1;
	private Spinner spin2;

	private HashMap<String, String> queryHash;
	private final String[] catagory = { "Videos", "Channels", "Playlists" };
	private final String[] sort = { "Relevance", "Date", "Rating", "Title",
			"View count", "Video count" };
	private String playlistAPI;
	private String queryHint;
	private CustomSearchView searchView;

	@Override
	public void Initializing() {
		// Give a title for the action bar
		abTitle = "Search Youtube";

		ab.setTitle(abTitle);

		// Get the query
		// Intent i = getIntent();
		// mQuery = i.getStringExtra("query");
		curQuery = "";
		// Log.i("debug", mQuery);

		mediaType = 0;
		sortType = 0;
		// Store the mappings
		queryHash = new HashMap<String, String>();
		queryHash.put("Playlists", "playlist");
		queryHash.put("Videos", "video");
		queryHash.put("Channels", "channel");
		queryHash.put("Relevance", "relevance");
		queryHash.put("Date", "date");
		queryHash.put("View count", "viewCount");
		queryHash.put("Video count", "videoCount");
		queryHash.put("Title", "title");
		queryHash.put("Rating", "rating");

		// encoding the query
		try {
			// playlistAPI =
			// "http://gdata.youtube.com/feeds/api/playlists/snippets?q="
			// + URLEncoder.encode(curQuery, "UTF-8")
			// + "&orderby=relevance&start-index=1&max-results=10&v=2&alt=json";
			playlistAPI = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=10&order="
					+ queryHash.get(sort[sortType])
					+ "&q="
					+ URLEncoder.encode(curQuery, "UTF-8")
					+ "&type="
					+ queryHash.get(catagory[mediaType]) + "&key=" + browserKey;

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		// Give API URLs
		API.clear();
		API.add(playlistAPI);

		// set a feed manager
		feedManager = new FeedManager_Base_v3(
				queryHash.get(catagory[mediaType]), playlistAPI, browserKey,
				gv, numOfResults);

		// set text in search field
		queryHint = "Search Youtube";

		// Default search type is 0 (Playlist)
		// 2 (Channel)
		// 1 (video)

//		setDropdown();

		// Pass results to ListViewAdapter Class
		suggestedKeywords = new ArrayList<String>();
		setHasOptionsMenu(true);
		setOptionMenu(true, true);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(0, 20, 0, "Search")
				.setIcon(R.drawable.abs__ic_search)
				.setActionView(searchView)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		menu.add(0, 0, 0, "Refresh").setIcon(R.drawable.ic_refresh)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		suggestedList = (ListView) sfa.findViewById(R.id.suggested_listview);
		if (suggestedList != null) {
			suggestedList.setOnItemClickListener(this);
			searchView = new CustomSearchView(sfa.getSupportActionBar()
					.getThemedContext());
			searchView.setListView(suggestedList);
		}

	}

	@Override
	public void setDropdown() {

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				ab.getThemedContext(), R.layout.sherlock_spinner_item,
				android.R.id.text1, catagory);
		adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(
				ab.getThemedContext(), R.layout.sherlock_spinner_item,
				android.R.id.text1, sort);
		adapter2.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		ab.setCustomView(R.layout.actionbar_item);
		// ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayShowCustomEnabled(true);
		// ab.setDisplayUseLogoEnabled(false);
		// ab.setDisplayShowHomeEnabled(false);

		spin1 = (Spinner) sfa.findViewById(R.id.spinner1);
		spin2 = (Spinner) sfa.findViewById(R.id.spinner2);

		spin1.setAdapter(adapter);
		spin2.setAdapter(adapter2);

		spin1.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int itemPosition, long id) {
				if (firstTime) {
					firstTime = false;
					return;
				}

				mediaType = itemPosition;
				refreshFragment();

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		spin2.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int itemPosition, long id) {
				if (spin2FirstTime) {
					spin2FirstTime = false;
					return;
				}

				sortType = itemPosition;
				refreshFragment();

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

	}

	@Override
	public void refreshFragment() {

		try {
			playlistAPI = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=10&order="
					+ queryHash.get(sort[sortType])
					+ "&q="
					+ URLEncoder.encode(curQuery, "UTF-8")
					+ "&type="
					+ queryHash.get(catagory[mediaType]) + "&key=" + browserKey;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		API.set(0, playlistAPI);
		feedManager = new FeedManager_Base_v3(
				queryHash.get(catagory[mediaType]), playlistAPI, browserKey,
				gv, numOfResults);
		super.refreshFragment();
		// redoRequest(playlistAPI,
		// new FeedManager_Base_v3(queryHash.get(catagory[mediaType]),
		// playlistAPI, browserKey, gv, numOfResults));

	}

	@Override
	protected void setGridViewItemClickListener() {

		gv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				switch (mediaType) {

				case 0:
					// Search Playlist, type 0
					Intent i1 = new Intent(sfa, LoadMore_Activity_Base.class);
					// Log.d("debug", videolist.get(position)
					// .getRecentVideoUrl());
					i1.putExtra("API", videolist.get(position)
							.getRecentVideoUrl());
					i1.putExtra("PLAYLIST_API", videolist.get(position)
							.getPlaylistsUrl());
					i1.putExtra("title", videolist.get(position).getTitle());
					i1.putExtra("thumbnail", videolist.get(position)
							.getThumbnailUrl());
					i1.putExtra("playlistID", videolist.get(position)
							.getVideoId());
					startActivity(i1);
					break;

				case 1:
					// Search videos, type 1
					Intent i = new Intent(sfa, YoutubeActionBarActivity.class);
					i.putExtra("video", videolist.get(position));
					i.putExtra("videoId", videolist.get(position).getVideoId());
					// i.putExtra("isfullscreen", true);
					startActivity(i);
					break;

				case 2:
					// Search channels, type 2
					nextFragmentAPI = videolist.get(position)
							.getRecentVideoUrl();
					String title = videolist.get(position).getTitle();
					String url = videolist.get(position).getThumbnailUrl();

					Intent i2 = new Intent(sfa, LoadMore_Activity_Channel.class);
					i2.putExtra("API", nextFragmentAPI);
					i2.putExtra("PLAYLIST_API", videolist.get(position)
							.getPlaylistsUrl());
					i2.putExtra("title", title);
					i2.putExtra("thumbnail", url);
					startActivity(i2);
					break;
				}
			}
		});

	}

	public class GetSuggestedWordsTask extends MyAsyncTask {

		@Override
		protected void onPostExecute(String result) {
			// result = "{\"feed\":"+result+"}";
			// Log.d("debug", result);
			if (!taskCancel && result != null) {
				// Do anything with response..
				try {
					suggestedKeywords.clear();
					mFeedManager = new FeedManager_Suggestion();
					mFeedManager.setmJSON(result);
					suggestedKeywords = mFeedManager.getSuggestionList();
					// Log.d("debug", "size s: "+suggestedKeywords.size());
				} catch (Exception e) {

				}

				if (suggestedKeywords != null && !suggestedKeywords.isEmpty()) {
					// Show suggestion list
					if (suggestedList.getVisibility() == View.VISIBLE
							&& isSelected) {
						suggestedList.setVisibility(View.GONE);
						isSelected = false;
					} else {
						suggestedList.setVisibility(View.VISIBLE);
						suggestedListAdapter = new ListViewAdapter(sfa,
								suggestedKeywords);
						// Binds the Adapter to the ListView
						suggestedList.setAdapter(suggestedListAdapter);
						suggestedListAdapter.notifyDataSetChanged();
					}
					// Log.d("debug", "size: "+suggestedListAdapter.getCount());
				} else {
					// Binds the Adapter to the ListView
					suggestedList.setAdapter(null);
					suggestedList.setVisibility(View.GONE);
				}

			}

		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		// User clicks a search keyword
		// searchView.setQuery(suggestedKeywords.get(index), true);
		// isSelected = true;
		onQueryTextSubmit(suggestedKeywords.get(index));
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// Hide the listview when submit
		// suggestedList.setVisibility(View.GONE);
		isSelected = true;
		curQuery = query;
		SearchFragment.hideSoftKeyboard(sfa);
		refreshFragment();
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
//		if (curQuery.equals(newText))
//			return true;

		// Get suggestion from google
		if (newText != null && newText.length() > 0) {

			mTask = new GetSuggestedWordsTask();
			String fullAPI = "";
			try {
				fullAPI = suggestionBaseAPI
						+ URLEncoder.encode(newText, "UTF-8") + "&key="
						+ browserKey;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			mTask.execute(fullAPI);

		} else {
			suggestedList.setVisibility(View.GONE);
		}

		return true;
	}

	public static void hideSoftKeyboard(Activity context) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);

	}
	
	@Override
	public void onDestroy() {
		ab.setDisplayShowCustomEnabled(false);
		
		super.onDestroy();
	}
	// @Override
	// public void onConfigurationChanged(Configuration newConfig) {
	// super.onConfigurationChanged(newConfig);
	//
	// if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) //To
	// fullscreen
	// {
	// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	// }
	// else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
	// {
	// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	// }
	// }
}
