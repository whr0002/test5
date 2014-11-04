package com.examples.gg.feedManagers;


import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;
import android.widget.GridView;

import com.examples.gg.data.Video;

/**
 * This class is used to parse JSON from Youtube server
 * */
public class FeedManager_Base_v3 extends FeedManager_Base {

	protected String mediaType;
	protected JSONArray items;
	protected ArrayList<Video> videos;
	protected GridView gv;
	protected String numOfResults;
	protected String browserKey;
	protected String baseAPI;
	protected String ids;
	protected JSONObject wholeJson;

	public FeedManager_Base_v3(String mediaType, String api, String browserKey,
			GridView gv, String numOfResults) {
		this.mediaType = mediaType;
		this.baseAPI = api;
		this.browserKey = browserKey;
		this.gv = gv;
		this.numOfResults = numOfResults;
	}

	@Override
	public ArrayList<Video> getVideoPlaylist() {

		processJSON(mJSON);

		videos = new ArrayList<Video>();

		try {

			for (int i = 0; i < items.length(); i++) {
				Video video = new Video();
				// get a video in the playlist
				JSONObject snippet = items.getJSONObject(i).getJSONObject(
						"snippet");
				// get the title of this video
				String videoTitle = snippet.getString("title");

				String videoId = snippet.getJSONObject("id").getString(
						mediaType + "Id");
				String author = "";
				String videoDesc = "";
				String thumbUrl = "";
				if (!snippet.isNull("channelTitle")) {
					author = snippet.getString("channelTitle");
				}

				if (!snippet.isNull("description")) {
					videoDesc = snippet.getString("description");
				}

				if (!snippet.isNull("thumbnails")) {
					thumbUrl = snippet.getJSONObject("thumbnails")
							.getJSONObject("medium").getString("url");
				}
				String updateTime = snippet.getString("publishedAt");

				String recentAPI = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults="
						+ numOfResults
						+ "&playlistId="
						+ videoId
						+ "&key="
						+ browserKey;

				String playlistAPI = "https://www.googleapis.com/youtube/v3/playlists?part=snippet&channelId="
						+ videoId + "&maxResults=10&key=" + browserKey;

				String formatedDate = handleDate(updateTime);
				setMediaType(video);
				video.setTitle(videoTitle);
				video.setVideoId(videoId);
				video.setThumbnailUrl(thumbUrl);
				video.setAuthor(author);
				video.setRecentVideoUrl("");
				video.setViewCount("");
				video.setDuration("");
				video.setVideoDesc(videoDesc);
				video.setUpdateTime(formatedDate);
				video.setRecentVideoUrl(recentAPI);
				video.setPlaylistsUrl(playlistAPI);
				// System.out.println(video.getTitle());
				// push it to the list
				videos.add(video);
				// System.out.println(videoTitle+"***"+videoLink);

				if (i != items.length() - 1) {
					ids += videoId + ",";

				} else {
					ids += videoId;
				}
			}
			doSecondTask();
		} catch (Exception ex) {
			// ex.printStackTrace();
		}

		Log.d("debug", ""+videos.size());
		return videos;
	}
	
	
	
	public void doSecondTask(){
		
	}
	
	public void parseChannels(int i) {
		try {
			JSONObject oneVideo = items.getJSONObject(i);
			JSONObject snippet = oneVideo.getJSONObject("contentDetails");
			String recentUploadsId = snippet.getJSONObject("relatedPlaylists")
					.getString("uploads");
			String playlistAPI = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=10&playlistId="
					+ recentUploadsId + "&key=" + browserKey;
			videos.get(i).setRecentVideoUrl(playlistAPI);

		} catch (Exception e) {
		}
	}
	
	
	public void parseVideos(int i) {
		try {
			JSONObject oneVideo = items.getJSONObject(i);
			JSONObject snippet = oneVideo
					.getJSONObject("contentDetails");
			JSONObject statistics = oneVideo.getJSONObject("statistics");
			String duration = snippet.getString("duration");
			String viewCount = statistics.getString("viewCount");
			videos.get(i).setDuration(formatDuration(duration));
			videos.get(i).setViewCount(viewCount+" views");
		} catch (Exception exception) {
		}
	}

	public String formatDuration(String d){
		String hours = "0";
		String minutes = "0";
		String seconds = "0";

		if (d.contains("H")) {
			hours = d.substring(d.indexOf("T") + 1, d.indexOf("H"));

			if (d.contains("M")) {
				minutes = d.substring(d.indexOf("H") + 1, d.indexOf("M"));

				if (d.contains("S")) {
					seconds = d.substring(d.indexOf("M") + 1, d.indexOf("S"));
				}
			} else if (d.contains("S")) {
				seconds = d.substring(d.indexOf("H") + 1, d.indexOf("S"));
			}
		}else if(d.contains("M")){
			minutes = d.substring(d.indexOf("T") + 1, d.indexOf("M"));
			
			if(d.contains("S")){
				seconds = d.substring(d.indexOf("M") + 1, d.indexOf("S"));
			}
		}else if(d.contains("S")){
			seconds = d.substring(d.indexOf("T") + 1, d.indexOf("S"));
		}
		
		int s = Integer.parseInt(hours)*3600 + Integer.parseInt(minutes)*60 +Integer.parseInt(seconds);
		
		
		
		return formatSecondsAsTime(Integer.toString(s));
	}
	
	public String getNextApi() {
		if (wholeJson.isNull("nextPageToken")) {
			return null;
		} else {
			String pageToken = "";
			try {
				pageToken = wholeJson.getString("nextPageToken");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return baseAPI+"&pageToken="+pageToken;
		}
	}

	@Override
	protected void processJSON(String json) {
		try {
			JSONTokener jsonParser = new JSONTokener(json);
			wholeJson = (JSONObject) jsonParser.nextValue();
			items = wholeJson.getJSONArray("items");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setMediaType(Video video) {
		if (mediaType.equals("video")) {
			video.setAsVideo();
		} else {
			if (mediaType.equals("playlist")) {
				video.setAsPlaylist();
				return;
			}
			if (mediaType.equals("channel")) {
				video.setAsChannel();
				return;
			}
		}
	}

}
