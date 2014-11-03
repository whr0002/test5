package com.examples.gg.loadMore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.examples.gg.adapters.EndlessScrollListener;
import com.examples.gg.adapters.VideoArrayAdapter;
import com.examples.gg.data.Video;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.rs.playlist2.R;

public class LoadMore_WorkoutNews extends LoadMore_Base {
	private getMatchInfo mgetMatchInfo;
	private int pageNum;
	private String baseUri;
	private String mAPI;

	public LoadMore_WorkoutNews(String api, int curPosition) {
		this.mAPI = api;
		this.currentPosition = curPosition;
	}

	public LoadMore_WorkoutNews(String api, String baseUri, int curPosition) {
		this.mAPI = api;
		this.baseUri = baseUri;
		this.currentPosition = curPosition;
	}

	@Override
	public void Initializing() {
		// Inflating view

		// Give a title for the action bar
		abTitle = "Lastest News";

		// Give API URLs
		API.add(mAPI);

		pageNum = 0;

		// Show menu
		setHasOptionsMenu(true);
		setOptionMenu(true, true);

//		currentPosition = 0;

	}
	@Override
	public void setListView() {
//		myLoadMoreListView = (LoadMoreListView) this.getListView();
//		myLoadMoreListView.setDivider(null);
		
		//setBannerInHeader();
		boolean pauseOnScroll = false; // or true
		boolean pauseOnFling = true; // or false
		PauseOnScrollListener listener = new PauseOnScrollListener(imageLoader, pauseOnScroll, pauseOnFling);
		gv.setOnScrollListener(listener);
		vaa = new VideoArrayAdapter(sfa, videolist, imageLoader);
		gv.setAdapter(vaa);

		if (isMoreVideos) {
			gv.setOnScrollListener(new EndlessScrollListener(){

				@Override
				public void onLoadMore(int page, int totalItemsCount) {
//					// Do the work to load more items at the end of
//					// list

					if (isMoreVideos == true) {
						// new LoadMoreTask().execute(API.get(0));
						getMatchInfo newTask = (getMatchInfo) new getMatchInfo(
								LoadMoreTask.LOADMORETASK, myLoadMoreListView,
								fullscreenLoadingView, mRetryView);
						newTask.execute(API.get(API.size() - 1));
						mLoadMoreTasks.add(newTask);}}
					
				});

		} else {
			gv.setOnScrollListener(null);
		}
		// sending Initial Get Request to Youtube
		if (!API.isEmpty()) {
			// show loading screen
			// DisplayView(fullscreenLoadingView, myLoadMoreListView,
			// mRetryView) ;
			doRequest();
		}

	}
	
	@Override
	public void setDropdown() {
		if (hasDropDown) {

			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

			final String[] catagory = { "NA", "EUW", "EUE" };

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					mActionBar.getThemedContext(),
					R.layout.sherlock_spinner_item, android.R.id.text1,
					catagory);

			adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

			mActionBar.setListNavigationCallbacks(adapter, this);

			mActionBar.setSelectedNavigationItem(currentPosition);

		} else {
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

		if (firstTime) {
			firstTime = false;
			return true;
		}

		FragmentTransaction ft = getFragmentManager().beginTransaction();

		switch (itemPosition) {

		case 0:
			// Menu option 1
			ft.replace(R.id.content_frame, new LoadMore_WorkoutNews(
					"http://beta.na.leagueoflegends.com/en/news/",
					"http://beta.na.leagueoflegends.com", 0));
			break;

		case 1:
			// Menu option 2
			ft.replace(R.id.content_frame, new LoadMore_WorkoutNews(
					"http://beta.euw.leagueoflegends.com/en/news/",
					"http://beta.euw.leagueoflegends.com", 1));
			break;

		case 2:
			// Menu option 3
			ft.replace(R.id.content_frame, new LoadMore_WorkoutNews(
					"http://beta.eune.leagueoflegends.com/en/news/",
					"http://beta.eune.leagueoflegends.com", 2));
			break;
		}

		ft.commit();

		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void refreshFragment() {
		String firstApi = API.get(0);
		API.clear();
		API.add(firstApi);
		pageNum = 0;
		titles.clear();
		videolist.clear();
		setListView();
	}

	@Override
	protected void setGridViewItemClickListener() {
		gv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String url = videolist.get(position).getVideoId();
//				Intent i = new Intent(Intent.ACTION_VIEW);
//				i.setData(Uri.parse(url));
				Intent i = new Intent(sfa,NewsViewerActivity.class);
				i.putExtra("uri", url);
				startActivity(i);
			}
		});
	}

	@SuppressLint("NewApi")
	@Override
	protected void doRequest() {
		// TODO Auto-generated method stub

		// System.out.println("DO!!!!!");
		for (String s : API) {
			mgetMatchInfo = new getMatchInfo(getMatchInfo.INITTASK, gv,
					fullscreenLoadingView, mRetryView);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				mgetMatchInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						s);
			} else {
				mgetMatchInfo.execute(s);
			}
		}
	}

	private class getMatchInfo extends LoadMoreTask {

		public getMatchInfo(int type, View contentView, View loadingView,
				View retryView) {
			super(type, contentView, loadingView, retryView);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void setRetryListener(final int type) {
			mRetryButton = (Button) retryView.findViewById(R.id.mRetryButton);

			mRetryButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					mgetMatchInfo = (getMatchInfo) new getMatchInfo(type,
							contentView, loadingView, retryView);
					mgetMatchInfo.DisplayView(loadingView, contentView,
							retryView);
					mgetMatchInfo.execute(API.get(API.size() - 1));

				}
			});

		}

		@Override
		public String doInBackground(String... uri) {

			super.doInBackground(uri[0]);

			if (!taskCancel && responseString != null) {
				try {
					pull(responseString);
				} catch (Exception e) {

				}
			}
			// pullNews();
			return responseString;
		}

		private void pull(String responseString) {
			Document doc = Jsoup.parse(responseString);
			// get all links
			Elements links = new Elements();
			links = doc.select("div.white-stone");

			if (!links.isEmpty()) {
				Elements imageElements = new Elements();
				Elements newsUriElements = new Elements();
				Elements newsTitleElements = new Elements();
				Elements newsSubtitleElements = new Elements();
				Elements dateElements = new Elements();

				String imageUri = "";
				String newsUri = "";
				String newsTitle = "";
				String newsSubtitle = "";
				String date = "";

				for (Element link : links) {
					imageElements = link.select("img");
					if (imageElements.size() > 0) {
						imageUri = imageElements.first().attr("src");
					}

					newsUriElements = link.select("a");
					if (newsUriElements.size() > 0) {
						newsUri = newsUriElements.first().attr("href");
					}

					newsTitleElements = link.select("a");
					if (newsTitleElements.size() > 0) {
						newsTitle = newsTitleElements.first().attr("title");
					}

					newsSubtitleElements = link.select("div.teaser-content");
					if (newsSubtitleElements.size() != 0) {
						newsSubtitle = newsSubtitleElements
								.first().select("div").first().text();
					}

					dateElements = link.select("span");
					if (dateElements.size() > 0) {
						date = dateElements.first().text();
					}

					Video v = new Video();
					v.setThumbnailUrl(baseUri + imageUri);
					// v.setRecentVideoUrl(newsUri);
					v.setTitle(newsTitle);
					v.setAuthor(newsSubtitle);
					v.setVideoId(baseUri + newsUri);
					v.setUpdateTime(date);
					v.setAsNews();

					titles.add(newsTitle);
					videolist.add(v);

				}

				Elements pages = new Elements();
				pages = doc.select("a[class=next disabled]");
				if (pages != null && pages.size() == 0) {
					
					isMoreVideos = true;
					pageNum++;
					API.add(API.get(0) + "?page=" + pageNum);
//					Log.d("debug", "Next API: " + API.get(0) + "?page=" + pageNum);

				} else {
					isMoreVideos = false;
				}
			} else {
				Toast.makeText(sfa, "Sorry, fail to get data.",
						Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onPostExecute(String result) {

			// Log.d("AsyncDebug", "Into onPostExecute!");

			if (!taskCancel && result != null) {

				vaa.notifyDataSetChanged();

				// loading done
				DisplayView(contentView, retryView, loadingView);

//				isMoreVideos = false;
				if (!isMoreVideos) {
//					Log.d("debug", "listener is null");
					gv.setOnScrollListener(null);
				}

			} else {

				handleCancelView();
			}

		}
	}

	@Override
	public void onDestroy() {

		super.onDestroy();

		if (mgetMatchInfo != null
				&& mgetMatchInfo.getStatus() == Status.RUNNING)
			mgetMatchInfo.cancel(true);

	}

}
