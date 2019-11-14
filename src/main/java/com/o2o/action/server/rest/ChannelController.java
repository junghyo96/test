package com.o2o.action.server.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Lists;
import com.o2o.action.server.db.Channel;
import com.o2o.action.server.db.Schedule;
import com.o2o.action.server.json.ChannelInfo;
import com.o2o.action.server.json.ChannelJSON;
import com.o2o.action.server.json.ScheduleInfo;
import com.o2o.action.server.json.ScheduleJSON;
import com.o2o.action.server.repo.ChannelRepository;
import com.o2o.action.server.repo.ScheduleRepository;

@RestController
public class ChannelController {
	@Autowired
	private ChannelRepository channelRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;

	@RequestMapping(value = "/api/1.0/channel", method = RequestMethod.GET)
	public @ResponseBody List<Channel> getChannel() {
		List<Channel> channels = Lists.newArrayList(channelRepository.findAll());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.HOUR, 9);
		String tmpStr = formatter.format(c.getTime()) + "00";

		long curTime = Long.parseLong(tmpStr);

		for (Channel channel : channels) {
			List<Schedule> schedules = scheduleRepository
					.findByChannelAndStartTimeLessThanEqualAndEndTimeGreaterThan(channel, curTime, curTime);
			for (Schedule schedule : schedules) {
				System.out.println(schedule);
			}
		}

		return channels;
	}

	@RequestMapping(value = "/api/1.0/channel", method = RequestMethod.POST)
	public @ResponseBody Channel updateChannel(@RequestBody Channel channelJson) {
		List<Channel> channels = channelRepository.findByChCode(channelJson.getChCode());

		if (channels != null && channels.size() > 0) {
			Channel channel = channels.get(0);
			// channel.update(channelJson);

			return channelRepository.save(channel);
		} else
			return channelRepository.save(channelJson);
	}

	@RequestMapping(value = "/api/1.0/schedule", method = RequestMethod.GET)
	public @ResponseBody List<Schedule> getSchedule(@RequestParam(value = "channelId") Long channelId) {
		
		Channel channel = new Channel();
		channel.setId(channelId);
		
		return Lists.newArrayList(scheduleRepository.findByChannelOrderByStartTimeDesc(channel));
	}

	@RequestMapping(value = "/api/1.0/schedule/current", method = RequestMethod.GET)
	public @ResponseBody List<Schedule> getScheduleCurrent() {
		
		List<Channel> channels = Lists.newArrayList(channelRepository.findAll());
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.HOUR, 9);
		String tmpStr = formatter.format(c.getTime()) + "00";

		long curTime = Long.parseLong(tmpStr);

		for (Channel channel : channels) {
			List<Schedule> schedules = scheduleRepository
					.findByChannelAndStartTimeLessThanEqualAndEndTimeGreaterThan(channel, curTime, curTime);
			for (Schedule schedule : schedules) {
				System.out.println(channel.getChName() + "," + schedule.getName());
			}
		}

		return null;//Lists.newArrayList(scheduleRepository.findByChannelOrderByStartTimeDesc(channel));
	}

	@RequestMapping(value = "/api/1.0/schedule", method = RequestMethod.POST)
	public @ResponseBody Schedule updateSchedule(@RequestBody Schedule scheduleJson) {
		List<Schedule> schedules = scheduleRepository.findByScheduleId(scheduleJson.getScheduleId());

		if (schedules != null && schedules.size() > 0) {
			Schedule schedule = schedules.get(0);
			// schedule.update(scheduleJson);

			return scheduleRepository.save(schedule);
		} else
			return scheduleRepository.save(scheduleJson);
	}

	@RequestMapping(value = "/api/1.0/channel/sync", method = RequestMethod.GET)
	public void syncChannel() {
		BasicCookieStore cookieStore = new BasicCookieStore();

		List<String> categories = new ArrayList<String>();
		List<String> dates = new ArrayList<String>();

		categories.add("4003");
		categories.add("4008");
		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, -1);
		dates.add(new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));
		c.add(Calendar.DATE, 1);
		dates.add(new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));
		c.add(Calendar.DATE, 1);
		dates.add(new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));
		c.add(Calendar.DATE, 1);
		dates.add(new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));

		for (String cd : categories) {
			for (String date : dates) {
				try {
					CloseableHttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
					HttpGet get = new HttpGet("https://www.skylife.co.kr/channel/epg/channelChart.do");
					HttpResponse response = client.execute(get);

					System.out.println("Initial set of cookies:");
					for (Cookie cookie : cookieStore.getCookies()) {
						System.out.println(cookie);
					}

					((CloseableHttpResponse) response).close();

					HttpPost post = new HttpPost("https://www.skylife.co.kr/channel/epg/channelScheduleListInfo.do");
					List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
					urlParameters.add(new BasicNameValuePair("area", "out"));
					urlParameters.add(new BasicNameValuePair("date_type", "now"));
					urlParameters.add(new BasicNameValuePair("airdate", date));
					urlParameters.add(new BasicNameValuePair("pk_epg_mapp", ""));
					urlParameters.add(new BasicNameValuePair("fd_mapp_cd", cd));
					urlParameters.add(new BasicNameValuePair("searchColumn", ""));
					urlParameters.add(new BasicNameValuePair("searchString", ""));
					urlParameters.add(new BasicNameValuePair("selectString", ""));

					post.setEntity(new UrlEncodedFormEntity(urlParameters));

					HttpContext localContext = new BasicHttpContext();
					localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

					response = client.execute(post, localContext);
					System.out.println("\nPost parameters : " + post.getEntity());
					System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

					ObjectMapper mapper = new ObjectMapper();
					ChannelJSON channelJSON = mapper.readValue(response.getEntity().getContent(), ChannelJSON.class);

					for (ChannelInfo channelInfo : channelJSON.getChannelListInfo()) {
						List<Channel> channels = channelRepository.findByChCode(channelInfo.getFd_channel_id());

						Channel eChannel = null;
						if (channels != null && channels.size() > 0) {
							Channel channel = channels.get(0);
							channel.update(channelInfo);
							eChannel = channelRepository.save(channel);
						} else {
							Channel channel = new Channel();
							channel.update(channelInfo);
							eChannel = channelRepository.save(channel);
						}

						processSchedule(cookieStore, eChannel, Integer.toString(channelInfo.getFd_channel_id()), date,
								cd);
					}

					System.out.println(channelJSON.getChannelListInfo().size());

					client.close();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (ProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void processSchedule(BasicCookieStore cookieStore, Channel channel, String channelId, String date,
			String cd) {
		try {
			CloseableHttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

			HttpPost post = new HttpPost("https://www.skylife.co.kr/channel/epg/channelScheduleListInfo.do");
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlParameters.add(new BasicNameValuePair("area", "detail"));
			urlParameters.add(new BasicNameValuePair("date_type", "now"));
			urlParameters.add(new BasicNameValuePair("airdate", date));
			urlParameters.add(new BasicNameValuePair("pk_epg_mapp", ""));
			urlParameters.add(new BasicNameValuePair("fd_mapp_cd", cd));
			urlParameters.add(new BasicNameValuePair("searchColumn", ""));
			urlParameters.add(new BasicNameValuePair("searchString", ""));
			urlParameters.add(new BasicNameValuePair("selectString", ""));
			urlParameters.add(new BasicNameValuePair("fd_channel_id", channelId));

			System.out.println(channelId);

			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			HttpContext localContext = new BasicHttpContext();
			localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

			HttpResponse response = client.execute(post, localContext);
			System.out.println("\nPost parameters : " + post.getEntity());
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

			ObjectMapper mapper = new ObjectMapper();
			ScheduleJSON scheduleJSON = mapper.readValue(response.getEntity().getContent(), ScheduleJSON.class);
			for (ScheduleInfo scheduleInfo : scheduleJSON.getScheduleListIn()) {
				List<Schedule> schedules = scheduleRepository.findByScheduleId(scheduleInfo.getSchedule_id());

				if (schedules != null && schedules.size() > 0) {
					Schedule schedule = schedules.get(0);
					schedule.update(scheduleInfo);
					scheduleRepository.save(schedule);
				} else {
					Schedule schedule = new Schedule();
					schedule.setChannel(channel);
					schedule.update(scheduleInfo);
					scheduleRepository.save(schedule);
				}
			}

			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
