package com.o2o.action.server.app;

import com.google.actions.api.ActionContext;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.actions.api.response.helperintent.SelectionList;
import com.google.api.services.actions_fulfillment.v2.model.BasicCard;
import com.google.api.services.actions_fulfillment.v2.model.Button;
import com.google.api.services.actions_fulfillment.v2.model.CarouselBrowse;
import com.google.api.services.actions_fulfillment.v2.model.CarouselBrowseItem;
import com.google.api.services.actions_fulfillment.v2.model.Image;
import com.google.api.services.actions_fulfillment.v2.model.ListSelectListItem;
import com.google.api.services.actions_fulfillment.v2.model.OpenUrlAction;
import com.google.api.services.actions_fulfillment.v2.model.OptionInfo;
import com.google.api.services.actions_fulfillment.v2.model.SimpleResponse;
import com.google.api.services.actions_fulfillment.v2.model.TableCard;
import com.google.api.services.actions_fulfillment.v2.model.TableCardCell;
import com.google.api.services.actions_fulfillment.v2.model.TableCardColumnProperties;
import com.google.api.services.actions_fulfillment.v2.model.TableCardRow;
import com.o2o.action.server.util.CommonUtil;
import com.o2o.action.server.util.CommonWord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Honcheontour extends DialogflowApp {

	@ForIntent("find-options")
	public ActionResponse processFindDate(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		String during = CommonUtil.makeSafeString(request.getParameter("during"));
		String together = CommonUtil.makeSafeString(request.getParameter("together"));
		String traffic = CommonUtil.makeSafeString(request.getParameter("traffic"));
		String traveltype = CommonUtil.makeSafeString(request.getParameter("traveltype"));

		rb.removeContext("choose-travel");
		rb.removeContext("choose-hotplace");
		rb.removeContext("notice-news");

		Map<String, Object> data = rb.getConversationData();

		if (!during.isEmpty()) {
			data.clear();
			data.put("during", during);
		}
		if (!together.isEmpty())
			data.put("together", together);
		if (!traffic.isEmpty())
			data.put("traffic", traffic);

		CommonUtil.printMapData(data);

		return genTravelOption(rb);
	}

	@ForIntent("find-options - fallback")
	public ActionResponse processFindDateFallback(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);


		return tourFallback(rb);
	}

	@ForIntent("choose-travel")
	public ActionResponse processChooseTravel(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();

		String travelType = request.getSelectedOption();

		data.remove("travelType");

		if (travelType == null)
			travelType = CommonUtil.makeSafeString(request.getParameter("travelType"));

		if (!travelType.isEmpty())
			data.put("travelType", travelType);

		CommonUtil.printMapData(data);

		String url = "";
		String[] loc = new String[10];

		loc[0] = "127.659222,37.667306";
		loc[1] = "128.371500,37.831750";
		loc[2] = "128.462833,37.867222";
		loc[3] = "127.880528,37.679333";
		loc[4] = "128.231917,37.711528";
		loc[5] = "127.683945,37.649056";
		loc[6] = "128.469308,37.831136";
		loc[7] = "127.959332,37.698747";
		loc[8] = "127.695536,37.702273";
		loc[9] = "127.629327,37.652071";

		for (int i = 0; i < 10; i++) {
			if (!CommonUtil.isEmptyString(loc[i])) {
				loc[i].replace(",", " ");
				naverGeoApi(loc[i], "cosbasic" + (i + 1));
			}
		}
		return genTravelOption(rb);
	}

	@ForIntent("choose-travel - fallback")
	public ActionResponse processChooseTravelFallback(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();

		String travelType = request.getSelectedOption();
		if (travelType == null)
			travelType = CommonUtil.makeSafeString(request.getParameter("travelType"));

		if (!travelType.isEmpty())
			data.put("travelType", travelType);

		CommonUtil.printMapData(data);

		return tourFallback(rb);
	}

	@ForIntent("choose-hotplace")
	public ActionResponse processChooseHotplace(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();
		String travelType = CommonUtil.makeSafeString(request.getParameter("travelType"));
		data.clear();
		if (!travelType.isEmpty()) {
			data.put("travelType", travelType);
		}
		rb.removeContext("choose-travel");
		rb.removeContext("find-options");
		rb.removeContext("notice-news");

		return genHotplace(rb, false);
	}

	@ForIntent("choose-hotplace - fallback")
	public ActionResponse processChooseHotplaceFallback(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		return genHotplace(rb, true);
	}

	@ForIntent("notice-news")
	public ActionResponse processNoticeNews(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();

		rb.removeContext("choose-travel");
		rb.removeContext("find-options");
		rb.removeContext("choose-hotplace");

		String noticeType = CommonUtil.makeSafeString(request.getParameter("noticeType"));
		String noticePlace = CommonUtil.makeSafeString(request.getParameter("noticePlace"));
		String noticeDoc = CommonUtil.makeSafeString(request.getParameter("noticeDoc"));

		data.clear();

		if (!noticeType.isEmpty()) {
			data.put("noticeType", noticeType);
		}
		if (!noticePlace.isEmpty())
			data.put("noticePlace", noticePlace);
		if (!noticeDoc.isEmpty())
			data.put("noticeDoc", noticeDoc);

		return genNotice(rb);
	}

	@ForIntent("notice-news - fallback")
	public ActionResponse processNoticeNewsFallback(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();

		String noticeType = CommonUtil.makeSafeString(data.get("noticeType"));
		String noticePlace = CommonUtil.makeSafeString(data.get("noticePlace"));
		String noticeDoc = CommonUtil.makeSafeString(data.get("noticeDoc"));

		CommonUtil.printMapData(data);
		List<String> suggestions = new ArrayList<String>();
		SimpleResponse simpleResponse = new SimpleResponse();

		TableCard tableCard = new TableCard();

		if (CommonUtil.isEmptyString(noticeType)) {
			BasicCard basicCard = new BasicCard();

			simpleResponse.setTextToSpeech("알아들을 수 없는 단어입니다.  \n무인민원 발급기 안내를 원하시거나 민원서류 안내를 원하시면 해당 키워드를 포함해서 다시 말씀해 주세요.");
			basicCard.setFormattedText("무인민원 발급기 안내를 원하시거나 민원서류 안내를 원하시면 해당 키워드를 포함해서 말씀해 주세요.");
			basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/tourhome.gif")
					.setAccessibilityText("홍천 투어 상담원 이미지"))
					.setImageDisplayOptions("DEFAULT");

			suggestions.add("홍천 무인민원 발급기 안내");
			suggestions.add("민원서류 안내");

			rb.add(simpleResponse);
			rb.add(basicCard);
		} else {
			if (noticeType.equalsIgnoreCase("noti1")) {
				suggestions.add("민원서류 안내");
				if (CommonUtil.isEmptyString(noticePlace)) {
					suggestions.add("홍천읍");
					suggestions.add("화촌면");
					suggestions.add("두촌면");
					suggestions.add("내촌면");
					suggestions.add("서석면");
					simpleResponse.setTextToSpeech("죄송합니다 말씀하신 지역은 홍천 무인민원 발급기가 설치되어 있지 않습니다.  \n홍천읍, 화촌면, 두촌면, 내촌면 등 원하시는 홍천시내에 있는 읍(면)을 포함하여 다시 말씀해 주세요.");
					tableCard.setTitle("홍천 무인민원발급기안내")
							.setSubtitle("  \n원하시는 지역명을 추천 키워드를 참고하셔서 말씀 해 주시면 해당 지역의 무인민원발급기를 알려드리겠습니다.  \n  \n홍천읍 / 화촌면 / 두촌면 / 내촌면 / 서석면 / 동면 / 남면 / 서면/ 북방면 / 내면  \n  \n※ 발급기 운영시간은 기타 사정(시스템점검 등)에 의해 변동될 수 있습니다.")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("소재지")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 평일")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 주말")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 석화로 93  \n홍천군청 본관 1층 입구"),
																	new TableCardCell().setText("07:00~22:00"),
																	new TableCardCell().setText("07:00~22:00")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 홍천로 351  \n홍천읍사무소 현관"),
																	new TableCardCell().setText("24시간 이용"),
																	new TableCardCell().setText("24시간 이용")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 홍천로 351  \n홍천읍사무소 내"),
																	new TableCardCell().setText("09:00-18:00"),
																	new TableCardCell().setText("09:00-18:00")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 너브내길 123  \n홍천군종합사회복지관"),
																	new TableCardCell().setText("09:00-18:00"),
																	new TableCardCell().setText("운영안함")))
													.setDividerAfter(true)));
				}
			} else if (noticeType.equalsIgnoreCase("noti2")) {
				suggestions.add("홍천 무인민원 발급기 안내");
				if (CommonUtil.isEmptyString(noticeDoc)) {
					suggestions.add("주민등록");
					suggestions.add("토지지적건축");
					suggestions.add("보건복지");
					suggestions.add("지방세");
					suggestions.add("가족관계등록부");
					simpleResponse.setTextToSpeech("죄송하지만 말씀하신 민원 서류는 관내 무인민원발급기에서 이용이 불가능한 서류입니다.  \n주민등록, 토지 지적 건축 및 차량, 보건복지 관련 등 추천 키워드를 참고하셔서 다시 말씀해 주십시오.");
					tableCard.setTitle("관내 무인민원발급기 이용가능 민원서류")
							.setSubtitle("  \n원하시는 서류 명을 추천 키워드를 참고하셔서 말씀 해 주시면 관내 무인민원발급기 이용가능 민원서류를 알려드리겠습니다.  \n<주민등록 / 토지 지적 건축 / 차량 / 보건복지 / 농촌 / 병적 / 지방세 / 부동산(법원) / 가족관계등록부 (법원) / 제적 / 교육>  \n  \n※ 무인민원발급기에서 본인 확인이 필요한 증명은 개인정보보호를 위해 본인에 한해서만 발급받을 수 있습니다.  \n※ 본인확인은 오른손 엄지 지문으로 확인합니다.")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("증명서종류")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관내")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관외")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("주민등록등본"),
																	new TableCardCell().setText("200원"),
																	new TableCardCell().setText("200원")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("주민등록초본"),
																	new TableCardCell().setText("200원"),
																	new TableCardCell().setText("200원")))
													.setDividerAfter(true)));
				}
			}
			rb.add(simpleResponse);
			rb.add(tableCard);
		}
		suggestions.add(CommonWord.SUGGEST_WELCOME_2);
		suggestions.add(CommonWord.SUGGEST_WELCOME_3);
		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		return rb.build();
	}

	private ActionResponse genNotice(ResponseBuilder rb) {
		Map<String, Object> data = rb.getConversationData();
		String noticeType = CommonUtil.makeSafeString(data.get("noticeType"));
		String noticePlace = CommonUtil.makeSafeString(data.get("noticePlace"));
		String noticeDoc = CommonUtil.makeSafeString(data.get("noticeDoc"));

		CommonUtil.printMapData(data);
		List<String> suggestions = new ArrayList<String>();
		SimpleResponse simpleResponse = new SimpleResponse();

		TableCard tableCard = new TableCard();

		if (CommonUtil.isEmptyString(noticeType) && !CommonUtil.isEmptyString(noticePlace) && !CommonUtil.isEmptyString(noticeDoc)) {
			BasicCard basicCard = new BasicCard();
			simpleResponse.setTextToSpeech("홍천 민원 안내 서비스입니다. 무인민원 발급기 안내를 원하시거나 민원서류 안내를 원하시면 해당 키워드를 포함해서 말씀해 주세요.");
			basicCard.setFormattedText("무인민원 발급기 안내를 원하시거나 민원서류 안내를 원하시면 해당 키워드를 포함해서 말씀해 주세요.");
			basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/tourhome.gif")
					.setAccessibilityText("홍천 투어 상담원 이미지"))
					.setImageDisplayOptions("DEFAULT");

			suggestions.add("홍천 무인민원 발급기 안내");
			suggestions.add("민원서류 안내");

			rb.add(simpleResponse);
		} else {
			if (noticeType.equalsIgnoreCase("noti1") || !CommonUtil.isEmptyString(noticePlace)) {
				suggestions.add("민원서류 안내");
				if (CommonUtil.isEmptyString(noticePlace)) {
					suggestions.add("홍천읍");
					suggestions.add("화촌면");
					suggestions.add("두촌면");
					suggestions.add("내촌면");
					suggestions.add("서석면");
					simpleResponse.setTextToSpeech("홍천 무인민원 발급기 안내입니다.");
					tableCard.setTitle("홍천 무인민원발급기안내")
							.setSubtitle("  \n원하시는 지역명을 추천 키워드를 참고하셔서 말씀 해 주시면 해당 지역의 무인민원발급기를 알려드리겠습니다.  \n  \n홍천읍 / 화촌면 / 두촌면 / 내촌면 / 서석면 / 동면 / 남면 / 서면/ 북방면 / 내면  \n  \n※ 발급기 운영시간은 기타 사정(시스템점검 등)에 의해 변동될 수 있습니다.")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("소재지")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 평일")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 주말")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 석화로 93  \n홍천군청 본관 1층 입구"),
																	new TableCardCell().setText("07:00~22:00"),
																	new TableCardCell().setText("07:00~22:00")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 홍천로 351  \n홍천읍사무소 현관"),
																	new TableCardCell().setText("24시간 이용"),
																	new TableCardCell().setText("24시간 이용")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 홍천로 351  \n홍천읍사무소 내"),
																	new TableCardCell().setText("09:00-18:00"),
																	new TableCardCell().setText("09:00-18:00")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 너브내길 123  \n홍천군종합사회복지관"),
																	new TableCardCell().setText("09:00-18:00"),
																	new TableCardCell().setText("운영안함")))
													.setDividerAfter(true)));
				} else if (noticePlace.equalsIgnoreCase("place1")) {
					simpleResponse.setTextToSpeech("홍천읍 무인민원 발급기 안내입니다.");
					tableCard.setTitle("홍천읍 무인민원발급기안내")
							.setSubtitle("  \n※ 발급기 운영시간은 기타 사정(시스템점검 등)에 의해 변동될 수 있습니다.")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("소재지")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 평일")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 주말")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 석화로 93  \n홍천군청 본관 1층 입구"),
																	new TableCardCell().setText("07:00~22:00"),
																	new TableCardCell().setText("07:00~22:00")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 홍천로 351  \n홍천읍사무소 현관"),
																	new TableCardCell().setText("24시간 이용"),
																	new TableCardCell().setText("24시간 이용")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 홍천로 351  \n홍천읍사무소 내"),
																	new TableCardCell().setText("09:00-18:00"),
																	new TableCardCell().setText("09:00-18:00")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 너브내길 123  \n홍천군종합사회복지관"),
																	new TableCardCell().setText("09:00-18:00"),
																	new TableCardCell().setText("운영안함")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 연봉중앙로 11-10  \n홍천연봉도서관"),
																	new TableCardCell().setText("09:00-18:00"),
																	new TableCardCell().setText("운영안함")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 생명과학관길 50  \n홍천세무서 내"),
																	new TableCardCell().setText("09:00-18:00"),
																	new TableCardCell().setText("운영안함")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 꽃뫼로 118  \n농협은행 홍천군지부"),
																	new TableCardCell().setText("24시간이용"),
																	new TableCardCell().setText("24시간이용")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 연봉로6길 2  \n연봉 하나로마트 내"),
																	new TableCardCell().setText("08:00-22:00"),
																	new TableCardCell().setText("08:00-22:00")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 갈마로 23  \n동부지점 하나로마트 내"),
																	new TableCardCell().setText("09:00-22:00"),
																	new TableCardCell().setText("09:00-19:00")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("홍천읍 홍천로 645  \n국민건강보험공단 민원실"),
																	new TableCardCell().setText("09:00~18:00"),
																	new TableCardCell().setText("운영안함")))
													.setDividerAfter(true)));
				} else if (noticePlace.equalsIgnoreCase("place2")) {
					simpleResponse.setTextToSpeech("화촌면 무인민원 발급기 안내입니다.");
					tableCard.setTitle("화촌면 무인민원발급기안내")
							.setSubtitle("  \n※ 발급기 운영시간은 기타 사정(시스템점검 등)에 의해 변동될 수 있습니다.")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("소재지")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 평일")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 주말")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("화촌면 성산로 143  \n화촌면사무소 민원실"),
																	new TableCardCell().setText("09:00-18:00"),
																	new TableCardCell().setText("09:00-18:00")))
													.setDividerAfter(true)));
				} else if (noticePlace.equalsIgnoreCase("place3")) {
					simpleResponse.setTextToSpeech("두촌면 무인민원 발급기 안내입니다.");
					tableCard.setTitle("두촌면 무인민원발급기안내")
							.setSubtitle("  \n※ 발급기 운영시간은 기타 사정(시스템점검 등)에 의해 변동될 수 있습니다.")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("소재지")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 평일")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 주말")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("두촌면 자은로 349  \n두촌면사무소 민원실"),
																	new TableCardCell().setText("09:00-18:00"),
																	new TableCardCell().setText("09:00-18:00")))
													.setDividerAfter(true)));
				} else if (noticePlace.equalsIgnoreCase("place4")) {
					simpleResponse.setTextToSpeech("내촌면 무인민원 발급기 안내입니다.");
					tableCard.setTitle("내촌면 무인민원발급기안내")
							.setSubtitle("  \n※ 발급기 운영시간은 기타 사정(시스템점검 등)에 의해 변동될 수 있습니다.")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("소재지")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 평일")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 주말")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("내촌면 내촌길 27  \n내촌면사무소 민원실"),
																	new TableCardCell().setText("09:00-18:00"),
																	new TableCardCell().setText("09:00-18:00")))
													.setDividerAfter(true)));
				} else if (noticePlace.equalsIgnoreCase("place5")) {
					simpleResponse.setTextToSpeech("서석면 무인민원 발급기 안내입니다.");
					tableCard.setTitle("서석면 무인민원발급기안내")
							.setSubtitle("  \n※ 발급기 운영시간은 기타 사정(시스템점검 등)에 의해 변동될 수 있습니다.")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("소재지")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 평일")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 주말")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("서석면 풍암길 10  \n서석면사무소 민원실"),
																	new TableCardCell().setText("24시간이용"),
																	new TableCardCell().setText("24시간이용")))
													.setDividerAfter(true)));
				} else if (noticePlace.equalsIgnoreCase("place6")) {
					simpleResponse.setTextToSpeech("동면 무인민원 발급기 안내입니다.");
					tableCard.setTitle("동면 무인민원발급기안내")
							.setSubtitle("  \n※ 발급기 운영시간은 기타 사정(시스템점검 등)에 의해 변동될 수 있습니다.")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("소재지")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 평일")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 주말")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("동면 공작산로 497  \n동면사무소 민원실"),
																	new TableCardCell().setText("09:00-18:00"),
																	new TableCardCell().setText("09:00-18:00")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("동면 금계로 1542  \n동면농협 좌운지소 내"),
																	new TableCardCell().setText("09:00-17:00"),
																	new TableCardCell().setText("운영안함")))
													.setDividerAfter(true)));
				} else if (noticePlace.equalsIgnoreCase("place7")) {
					simpleResponse.setTextToSpeech("남면 무인민원 발급기 안내입니다.");
					tableCard.setTitle("남면 무인민원발급기안내")
							.setSubtitle("  \n※ 발급기 운영시간은 기타 사정(시스템점검 등)에 의해 변동될 수 있습니다.")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("소재지")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 평일")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 주말")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("남면 양덕원로 97  \n남면사무소 민원실"),
																	new TableCardCell().setText("24시간 이용"),
																	new TableCardCell().setText("24시간 이용")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("남면 시동안로 227  \n남면농협 시동지점 내"),
																	new TableCardCell().setText("09:00-17:00"),
																	new TableCardCell().setText("운영안함")))
													.setDividerAfter(true)));
				} else if (noticePlace.equalsIgnoreCase("place8")) {
					simpleResponse.setTextToSpeech("서면 무인민원 발급기 안내입니다.");
					tableCard.setTitle("서면 무인민원발급기안내")
							.setSubtitle("  \n※ 발급기 운영시간은 기타 사정(시스템점검 등)에 의해 변동될 수 있습니다.")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("소재지")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 평일")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 주말")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("서면 팔봉산로 617  \n서면사무소 민원실"),
																	new TableCardCell().setText("09:00-18:00"),
																	new TableCardCell().setText("09:00-18:00")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("서면 설밀길 5  \n서홍천농협 모곡지점 내"),
																	new TableCardCell().setText("09:00-17:00"),
																	new TableCardCell().setText("운영안함")))
													.setDividerAfter(true)));
				} else if (noticePlace.equalsIgnoreCase("place9")) {
					simpleResponse.setTextToSpeech("북방면 무인민원 발급기 안내입니다.");
					tableCard.setTitle("북방면 무인민원발급기안내")
							.setSubtitle("  \n※ 발급기 운영시간은 기타 사정(시스템점검 등)에 의해 변동될 수 있습니다.")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("소재지")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 평일")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 주말")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("북방면 영서로 2698  \n북방면사무소 민원실"),
																	new TableCardCell().setText("09:00-18:00"),
																	new TableCardCell().setText("09:00-18:00")))
													.setDividerAfter(true)));
				} else if (noticePlace.equalsIgnoreCase("place10")) {
					simpleResponse.setTextToSpeech("내면 무인민원 발급기 안내입니다.");
					tableCard.setTitle("내면 무인민원발급기안내")
							.setSubtitle("  \n※ 발급기 운영시간은 기타 사정(시스템점검 등)에 의해 변동될 수 있습니다.")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("소재지")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 평일")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("운영시간 - 주말")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("내면 창촌로 59  \n내면사무소 민원실"),
																	new TableCardCell().setText("24시간 이용"),
																	new TableCardCell().setText("24시간 이용")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("내면 구룡령로 5251  \n내면농협 본점 내"),
																	new TableCardCell().setText("09:00-17:00"),
																	new TableCardCell().setText("운영안함")))
													.setDividerAfter(true),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("내면 방내시장길 25-4  \n내면농협 방내지점 내"),
																	new TableCardCell().setText("09:00-17:00"),
																	new TableCardCell().setText("운영안함")))
													.setDividerAfter(true)));
				}
			} else if (noticeType.equalsIgnoreCase("noti2") || !CommonUtil.isEmptyString(noticeDoc)) {
				suggestions.add("홍천 무인민원 발급기 안내");
				if (CommonUtil.isEmptyString(noticeDoc)) {
					suggestions.add("주민등록");
					suggestions.add("토지지적건축");
					suggestions.add("보건복지");
					suggestions.add("지방세");
					suggestions.add("가족관계등록부");
					simpleResponse.setTextToSpeech("관내 무인민원발급기 이용가능 민원서류 안내입니다.");
					tableCard.setTitle("관내 무인민원발급기 이용가능 민원서류")
							.setSubtitle("  \n원하시는 서류 명을 추천 키워드를 참고하셔서 말씀 해 주시면 관내 무인민원발급기 이용가능 민원서류를 알려드리겠습니다.  \n<주민등록 / 토지 지적 건축 / 차량 / 보건복지 / 농촌 / 병적 / 지방세 / 부동산(법원) / 가족관계등록부 (법원) / 제적 / 교육>  \n  \n※ 무인민원발급기에서 본인 확인이 필요한 증명은 개인정보보호를 위해 본인에 한해서만 발급받을 수 있습니다.  \n※ 본인확인은 오른손 엄지 지문으로 확인합니다.")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("증명서종류")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관내")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관외")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("주민등록등본"),
																	new TableCardCell().setText("200원"),
																	new TableCardCell().setText("200원")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("주민등록초본"),
																	new TableCardCell().setText("200원"),
																	new TableCardCell().setText("200원")))
													.setDividerAfter(true)));
				} else if (noticeDoc.equalsIgnoreCase("doc1")) {
					simpleResponse.setTextToSpeech("관내 무인민원서류 주민등록 관련 발급 안내입니다.");
					tableCard.setTitle("주민등록 발급 안내")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("증명서종류")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관내")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관외")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("주민등록등본"),
																	new TableCardCell().setText("200원"),
																	new TableCardCell().setText("200원")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("주민등록초본"),
																	new TableCardCell().setText("200원"),
																	new TableCardCell().setText("200원")))
													.setDividerAfter(true)));
				} else if (noticeDoc.equalsIgnoreCase("doc2")) {
					simpleResponse.setTextToSpeech("관내 무인민원서류 토지 지적 건축 관련 발급 안내입니다.");
					tableCard.setTitle("토지 지적 건축 관련 발급 안내")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("증명서종류")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관내")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관외")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("개별공시지가 확인서"),
																	new TableCardCell().setText("800원"),
																	new TableCardCell().setText("800원")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("토지이용계획 확인서"),
																	new TableCardCell().setText("1,000원"),
																	new TableCardCell().setText("1,000원")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("토지(임야) 대장등본 대지권등록부"),
																	new TableCardCell().setText("500원"),
																	new TableCardCell().setText("(20장 초과시 장당 100원)")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("건축물대장"),
																	new TableCardCell().setText("500원"),
																	new TableCardCell().setText("500원")))
													.setDividerAfter(true)));
				} else if (noticeDoc.equalsIgnoreCase("doc3")) {
					simpleResponse.setTextToSpeech("관내 무인민원서류 차량 관련 발급 안내입니다.");
					tableCard.setTitle("차량 관련 발급 안내")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("증명서종류")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관내")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관외")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("건설기계등록원부"),
																	new TableCardCell().setText("500원"),
																	new TableCardCell().setText("500원")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("건설기계등록원부(갑,을)"),
																	new TableCardCell().setText("500원"),
																	new TableCardCell().setText("500원")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("자동차등록원부"),
																	new TableCardCell().setText("300원"),
																	new TableCardCell().setText("300원")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("자동차등록원부(갑,을)"),
																	new TableCardCell().setText("300원"),
																	new TableCardCell().setText("300원")))
													.setDividerAfter(true)));
				} else if (noticeDoc.equalsIgnoreCase("doc4")) {
					simpleResponse.setTextToSpeech("관내 무인민원서류 보건복지 관련 발급 안내입니다.");
					tableCard.setTitle("보건복지 관련 발급 안내")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("증명서종류")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관내")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관외")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("수급자증명서"),
																	new TableCardCell().setText("무료"),
																	new TableCardCell().setText("무료")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("장애인증명서"),
																	new TableCardCell().setText("무료"),
																	new TableCardCell().setText("무료")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("한부모가족증명서"),
																	new TableCardCell().setText("무료"),
																	new TableCardCell().setText("무료")))
													.setDividerAfter(true)));
				} else if (noticeDoc.equalsIgnoreCase("doc5")) {
					simpleResponse.setTextToSpeech("관내 무인민원서류 농촌 관련 발급 안내입니다.");
					tableCard.setTitle("농촌 관련 발급 안내")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("증명서종류")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관내")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관외")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("농지원부"),
																	new TableCardCell().setText("1000원"),
																	new TableCardCell().setText("발급불가")))
													.setDividerAfter(true)));
				} else if (noticeDoc.equalsIgnoreCase("doc6")) {
					simpleResponse.setTextToSpeech("관내 무인민원서류 병적 관련 발급 안내입니다.");
					tableCard.setTitle("병적 관련 발급 안내")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("증명서종류")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관내")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관외")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("병적증명서"),
																	new TableCardCell().setText("무료"),
																	new TableCardCell().setText("무료")))
													.setDividerAfter(true)));
				} else if (noticeDoc.equalsIgnoreCase("doc7")) {
					simpleResponse.setTextToSpeech("지방세 관련 무인민원 서류 안내입니다.");
					tableCard.setTitle("지방세 관련 무인민원서류안내")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("증명서종류")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관내")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관외")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("지방세세목별과세증명서"),
																	new TableCardCell().setText("500원"),
																	new TableCardCell().setText("발급불가")))
													.setDividerAfter(true)));
				} else if (noticeDoc.equalsIgnoreCase("doc8")) {
					simpleResponse.setTextToSpeech("부동산(법원) 관련 무인민원 서류 안내입니다.");
					tableCard.setTitle("부동산(법원) 관련 무인민원서류안내")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("증명서종류")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관내")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관외")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("등기부등본"),
																	new TableCardCell().setText("1,000원"),
																	new TableCardCell().setText("1,000원")))
													.setDividerAfter(true)));
				} else if (noticeDoc.equalsIgnoreCase("doc9")) {
					simpleResponse.setTextToSpeech("관내 무인민원서류 가족관계등록부 관련 발급 안내입니다.");
					tableCard.setTitle("가족관계등록부 (법원) 관련 발급 안내")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("증명서종류")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관내")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관외")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("가족관계증명서(폐쇄포함)"),
																	new TableCardCell().setText("500원"),
																	new TableCardCell().setText("500원")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("기본증명서(폐쇄포함)"),
																	new TableCardCell().setText("500원"),
																	new TableCardCell().setText("500원")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("혼인관계증명서(폐쇄포함)"),
																	new TableCardCell().setText("500원"),
																	new TableCardCell().setText("500원")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("입양관계증명서(폐쇄포함)"),
																	new TableCardCell().setText("500원"),
																	new TableCardCell().setText("500원")))
													.setDividerAfter(true)));
				} else if (noticeDoc.equalsIgnoreCase("doc10")) {
					simpleResponse.setTextToSpeech("제적 관련 무인민원 서류 안내입니다.");
					tableCard.setTitle("제적 관련 무인민원서류안내")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("증명서종류")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관내")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관외")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("제적등본"),
																	new TableCardCell().setText("500원"),
																	new TableCardCell().setText("500원")))
													.setDividerAfter(true)));
				} else if (noticeDoc.equalsIgnoreCase("doc11")) {
					simpleResponse.setTextToSpeech("교육 관련 무인민원 서류 안내입니다.");
					tableCard.setTitle("교육 관련 무인민원서류안내")
							.setColumnProperties(
									Arrays.asList(//LEADING,TRAILING
											new TableCardColumnProperties()
													.setHeader("증명서종류")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관내")
													.setHorizontalAlignment("CENTER"),
											new TableCardColumnProperties()
													.setHeader("수수료 - 관외")
													.setHorizontalAlignment("CENTER")))
							.setRows(
									Arrays.asList(
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("졸업증명서"),
																	new TableCardCell().setText("무료"),
																	new TableCardCell().setText("무료")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("성적증명서"),
																	new TableCardCell().setText("무료"),
																	new TableCardCell().setText("무료")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("생활기록부"),
																	new TableCardCell().setText("무료"),
																	new TableCardCell().setText("무료")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("검정고시 합격증명서(국/영문)"),
																	new TableCardCell().setText("무료"),
																	new TableCardCell().setText("무료")))
													.setDividerAfter(false),
											new TableCardRow()
													.setCells(
															Arrays.asList(
																	new TableCardCell().setText("검정고시 성적증명서"),
																	new TableCardCell().setText("무료"),
																	new TableCardCell().setText("무료")))
													.setDividerAfter(true)));
				}
			}
			rb.add(simpleResponse);
			rb.add(tableCard);
		}
		suggestions.add(CommonWord.SUGGEST_WELCOME_2);
		suggestions.add(CommonWord.SUGGEST_WELCOME_3);
		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		return rb.build();
	}

	private ActionResponse genHotplace(ResponseBuilder rb, Boolean isFallback) {
		Map<String, Object> data = rb.getConversationData();
		String travelType = CommonUtil.makeSafeString(data.get("travelType"));

		String replay = "";
		CommonUtil.printMapData(data);

		List<String> suggestions = new ArrayList<String>();
		SimpleResponse simpleResponse = new SimpleResponse();

		CarouselBrowseItem CarouselBrowseItem1 = null;
		CarouselBrowseItem CarouselBrowseItem2 = null;
		CarouselBrowseItem CarouselBrowseItem3 = null;
		CarouselBrowseItem CarouselBrowseItem4 = null;
		List<CarouselBrowseItem> items = new ArrayList<>();

		List<String> synonyms1 = new ArrayList<String>();
		synonyms1.add("hotplace1");
		List<String> synonyms2 = new ArrayList<String>();
		synonyms2.add("hotplace2");
		List<String> synonyms3 = new ArrayList<String>();
		synonyms3.add("hotplace3");
		List<String> synonyms4 = new ArrayList<String>();
		synonyms4.add("hotplace4");

		if (isFallback) {
			replay = "죄송하지만 추천 키워드를 포함하여 다시 말씀해 주세요.  \n";
		}

		if (CommonUtil.isEmptyString(travelType)) {
			data.clear();
			int random = (int) (Math.random() * 4) + 1;
			travelType = "cos" + Integer.toString(random);
			simpleResponse.setTextToSpeech(replay + "홍천 맛집 리스트 중 원하시는 음식점을 클릭해주시면 해당 업체의 자세한 정보를 알 수 있습니다.");
			suggestions.add("다른 맛집 추천");
		} else {
			switch (travelType.toString()) {
				case "cos1":
					simpleResponse.setTextToSpeech(replay + "양떼목장 근처에 있는 홍천 맛집 리스트 중 원하시는 음식점을 클릭해주시면 해당 업체의 자세한 정보를 알 수 있습니다.");
					break;
				case "cos2":
					simpleResponse.setTextToSpeech(replay + "살둔마을 근처에 있는 홍천 맛집 리스트 중 원하시는 음식점을 클릭해주시면 해당 업체의 자세한 정보를 알 수 있습니다.");
					break;
				case "cos3":
					simpleResponse.setTextToSpeech(replay + "가칠봉삼봉약수 근처에 있는 홍천 맛집 리스트 중 원하시는 음식점을 클릭해주시면 해당 업체의 자세한 정보를 알 수 있습니다.");
					break;
				case "cos4":
					simpleResponse.setTextToSpeech(replay + "생명건강과학관 근처에 있는 홍천 맛집 리스트 중 원하시는 음식점을 클릭해주시면 해당 업체의 자세한 정보를 알 수 있습니다.");
					break;
				case "cos5":
					simpleResponse.setTextToSpeech(replay + "모둘자리관광농원 근처에 있는 홍천 맛집 리스트 중 원하시는 음식점을 클릭해주시면 해당 업체의 자세한 정보를 알 수 있습니다.");
					break;
				case "cos6":
					simpleResponse.setTextToSpeech(replay + "비발디 파크 근처에 있는 홍천 맛집 리스트 중 원하시는 음식점을 클릭해주시면 해당 업체의 자세한 정보를 알 수 있습니다.");
					break;
				case "cos7":
					simpleResponse.setTextToSpeech(replay + "은행나무숲 근처에 있는 홍천 맛집 리스트 중 원하시는 음식점을 클릭해주시면 해당 업체의 자세한 정보를 알 수 있습니다.");
					break;
				case "cos8":
					simpleResponse.setTextToSpeech(replay + "공작산 수타사 근처에 있는 홍천 맛집 리스트 중 원하시는 음식점을 클릭해주시면 해당 업체의 자세한 정보를 알 수 있습니다.");
					break;
				case "cos9":
					simpleResponse.setTextToSpeech(replay + "팔봉산 관광지 근처에 있는 홍천 맛집 리스트 중 원하시는 음식점을 클릭해주시면 해당 업체의 자세한 정보를 알 수 있습니다.");
					break;
				case "cos10":
					simpleResponse.setTextToSpeech(replay + "힐리언스 선마을 근처에 있는 홍천 맛집 리스트 중 원하시는 음식점을 클릭해주시면 해당 업체의 자세한 정보를 알 수 있습니다.");
					break;
			}
		}

		if (travelType.equalsIgnoreCase("cos1")) {

			CarouselBrowseItem1 = new CarouselBrowseItem().setTitle("홍뚝이네원조뼈다귀해장국")
					.setDescription("두미리 480-3 / 강원 홍천군 서면 팔봉산로 412-3")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=38007243"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos1hotplacelist1.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace1"));

			CarouselBrowseItem2 = new CarouselBrowseItem().setTitle("함흥집")
					.setDescription("두미리 457 /  강원 홍천군 서면 팔봉산로 541")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=15499575"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos1hotplacelist2.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace2"));

			CarouselBrowseItem3 = new CarouselBrowseItem().setTitle("은수정")
					.setDescription("두미리 436-1 / 강원 홍천군 서면 팔봉산로 577")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=21098421"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos1hotplacelist3.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace3"));

			CarouselBrowseItem4 = new CarouselBrowseItem().setTitle("개미반점")
					.setDescription("창촌리 1588-3/강원도 홍천군 내면 창촌로 55")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=13124574"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos1hotplacelist4.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace4"));

		} else if (travelType.equalsIgnoreCase("cos2")) {

			CarouselBrowseItem1 = new CarouselBrowseItem().setTitle("유내손두부")
					.setDescription("창촌리 1573-52 / 강원 홍천군 내면 창촌로 62")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=31259483"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos2hotplacelist1.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace1"));

			CarouselBrowseItem2 = new CarouselBrowseItem().setTitle("계방산숯불갈비")
					.setDescription("창촌리 1573-60 / 강원 홍천군 내면 구룡령로 5279")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=15502375"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos2hotplacelist2.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace2"));

			CarouselBrowseItem3 = new CarouselBrowseItem().setTitle("소리산참숯굽는마을")
					.setDescription("석산리 36 황토숯가마찜질 / 경기 양평군 단월면 석산돌고갯길 12-29")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=18916456"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos2hotplacelist3.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace3"));

			CarouselBrowseItem4 = new CarouselBrowseItem().setTitle("소리산쉼터")
					.setDescription("광원리 1436-17 / 강원 홍천군 내면 구룡령로 6029")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=19162546"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos2hotplacelist4.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace4"));
		} else if (travelType.equalsIgnoreCase("cos3")) {
			CarouselBrowseItem1 = new CarouselBrowseItem().setTitle("오대산내고향")
					.setDescription("광원리 676-1 / 강원 홍천군 내면 구룡령로 6898")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=21372094"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos3hotplacelist1.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace1"));

			CarouselBrowseItem2 = new CarouselBrowseItem().setTitle("삼봉통나무산장")
					.setDescription("광원리 662-4/강원도 홍천군 내면 삼봉휴양길 42")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=20331470"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos3hotplacelist2.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace2"));

			CarouselBrowseItem3 = new CarouselBrowseItem().setTitle("강내천")
					.setDescription("광원리 730-1 / 강원 홍천군 내면 직소마을길 6")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=15502886"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos3hotplacelist3.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace3"));

			CarouselBrowseItem4 = new CarouselBrowseItem().setTitle("달뜨는 언덕")
					.setDescription("광원리 579-11 / 강원도 홍천군 내면 구룡령로 6707")
					.setOpenUrlAction(new OpenUrlAction().setUrl("http://store.naver.com/accommodations/detail?id=15502809"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos3hotplacelist4.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace4"));
		} else if (travelType.equalsIgnoreCase("cos4")) {
			CarouselBrowseItem1 = new CarouselBrowseItem().setTitle("청와삼대 홍천점")
					.setDescription("연봉리 507-6 / 강원 홍천군 홍천읍 남산마을길3길 20")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=37817620"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos4hotplacelist1.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace1"));

			CarouselBrowseItem2 = new CarouselBrowseItem().setTitle("덕바우")
					.setDescription("연봉리 458-1 / 강원 홍천군 홍천읍 남산강변로2길 5")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=19856528"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos4hotplacelist2.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace2"));

			CarouselBrowseItem3 = new CarouselBrowseItem().setTitle("짬뽕전문점")
					.setDescription("연봉리 259-6 / 강원 홍천군 홍천읍 무궁화로 92")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=15485942"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos4hotplacelist3.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace3"));

			CarouselBrowseItem4 = new CarouselBrowseItem().setTitle("한림정")
					.setDescription("연봉리 250-3 / 강원 홍천군 홍천읍 송학로 20")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=11574953"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos4hotplacelist4.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace4"));
		} else if (travelType.equalsIgnoreCase("cos5")) {
			CarouselBrowseItem1 = new CarouselBrowseItem().setTitle("한옥정")
					.setDescription("검산리 327-5 / 강원 홍천군 서석면 용오름길 263")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=32322984"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos5hotplacelist1.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace1"));

			CarouselBrowseItem2 = new CarouselBrowseItem().setTitle("검산막국수")
					.setDescription("검산리 614-10 / 강원 홍천군 서석면 검산길 10")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=15494786"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos5hotplacelist2.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace2"));

			CarouselBrowseItem3 = new CarouselBrowseItem().setTitle("생곡막국수")
					.setDescription("생곡리 694 / 강원 홍천군 서석면 군두리길 310")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=1909115860"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos5hotplacelist3.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace3"));
			CarouselBrowseItem4 = new CarouselBrowseItem().setTitle("산촌닭갈비")
					.setDescription("검산리 616-9 / 강원 홍천군 서석면 구룡령로 2896")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=1794009418"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos5hotplacelist4.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace4"));
		} else if (travelType.equalsIgnoreCase("cos6")) {
			CarouselBrowseItem1 = new CarouselBrowseItem().setTitle("식객")
					.setDescription("팔봉리 1278-10 / 강원도 홍천군 서면 한치골길 262")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=20601513"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos6hotplacelist1.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace1"));

			CarouselBrowseItem2 = new CarouselBrowseItem().setTitle("미채원")
					.setDescription("팔봉리 1290-14 / 강원도 홍천군 서면 한치골길 262 ")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=31676314"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos6hotplacelist2.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace2"));

			CarouselBrowseItem3 = new CarouselBrowseItem().setTitle("시골장터")
					.setDescription("팔봉리 1290-14 / 강원도 홍천군 서면 한치골길 262")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=11722366"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos6hotplacelist3.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace3"));

			CarouselBrowseItem4 = new CarouselBrowseItem().setTitle("아궁이 화로구이")
					.setDescription("팔봉리 679-9 / 강원도 홍천군 서면 한치골길 606")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=32646253"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos6hotplacelist4.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace4"));
		} else if (travelType.equalsIgnoreCase("cos7")) {
			CarouselBrowseItem1 = new CarouselBrowseItem().setTitle("개미반점")
					.setDescription("창촌리 1588-3/강원도 홍천군 내면 창촌로 55")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=13124574"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos7hotplacelist1.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace1"));

			CarouselBrowseItem2 = new CarouselBrowseItem().setTitle("그리운두부깊은산골")
					.setDescription("광원리 668 / 강원도 홍천군 내면 구룡령로 6928")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=31750010"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos7hotplacelist2.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace2"));

			CarouselBrowseItem3 = new CarouselBrowseItem().setTitle("만나산장가든")
					.setDescription("창촌리 1417-2 / 강원 홍천군 내면 백성동길 37")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=15748477"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos7hotplacelist3.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace3"));

			CarouselBrowseItem4 = new CarouselBrowseItem().setTitle("칡소폭포도원식당")
					.setDescription("광원리 551-4 / 강원도 홍천군 내면 을수길 27")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=36600035"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos7hotplacelist4.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace4"));
		} else if (travelType.equalsIgnoreCase("cos8")) {
			CarouselBrowseItem1 = new CarouselBrowseItem().setTitle("종점식당")
					.setDescription("덕치리 24 / 강원도 홍천군 동면 수타사로 427")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=15495626"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos8hotplacelist1.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace1"));

			CarouselBrowseItem2 = new CarouselBrowseItem().setTitle("수타계곡돌집")
					.setDescription("덕치리 24 / 강원도 홍천군 동면 수타사로 425")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=15495630"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos8hotplacelist2.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace2"));

			CarouselBrowseItem3 = new CarouselBrowseItem().setTitle("칡사랑 메밀사랑")
					.setDescription("덕치리 56-2 / 강원 홍천군 동면 수타사로 372-5")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=11683877"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos8hotplacelist3.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace3"));

			CarouselBrowseItem4 = new CarouselBrowseItem().setTitle("느티나무집")
					.setDescription("덕치리 52-10 / 강원도 홍천군 동면 수타사로 380")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=1268811387"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos8hotplacelist4.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace4"));
		} else if (travelType.equalsIgnoreCase("cos9")) {
			CarouselBrowseItem1 = new CarouselBrowseItem().setTitle("팔봉호남식당")
					.setDescription("어유포리 272-10 / 강원도 홍천군 서면 한치골길 1122-33 ")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=11640764"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos9hotplacelist1.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace1"));

			CarouselBrowseItem2 = new CarouselBrowseItem().setTitle("팔봉산윤정이네")
					.setDescription("어유포리 272-10 / 강원도 홍천군 서면 한치골길 1122-35")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?entry=plt&id=30824324&query=팔봉산윤정이네"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos9hotplacelist2.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace2"));

			CarouselBrowseItem3 = new CarouselBrowseItem().setTitle("강가촌")
					.setDescription("어유포리 272-14 / 강원도 홍천군 서면 한치골길 1122-54")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=735241771"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos9hotplacelist3.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace3"));

			CarouselBrowseItem4 = new CarouselBrowseItem().setTitle("오뚜기식당")
					.setDescription("어유포리 272-15 / 강원도 홍천군 서면 한치골길 1122-68")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=13035617"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos9hotplacelist4.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace4"));
		} else if (travelType.equalsIgnoreCase("cos10")) {
			CarouselBrowseItem1 = new CarouselBrowseItem().setTitle("수연 가든")
					.setDescription("대곡리 49-3 / 강원도 홍천군 서면 한치골길 48")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?entry=plt&id=35136286&query=수연가든"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos10hotplacelist1.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace1"));

			CarouselBrowseItem2 = new CarouselBrowseItem().setTitle("규민골")
					.setDescription("한서로 2113 / 강원도 홍천군 서면 굴업리 338-1")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=21862120"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos10hotplacelist2.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace2"));

			CarouselBrowseItem3 = new CarouselBrowseItem().setTitle("송곡가든농장")
					.setDescription("굴업리 317 / 강원도 홍천군 서면 굴업솔골길 41")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?id=38300484"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos10hotplacelist3.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace3"));

			CarouselBrowseItem4 = new CarouselBrowseItem().setTitle("금수강산막국수")
					.setDescription("팔봉리 1302-101 / 강원도 홍천군 서면 한치골길 785 ")
					.setOpenUrlAction(new OpenUrlAction().setUrl("https://store.naver.com/restaurants/detail?entry=plt&id=20437663&query=금수강산막국수"))
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/cos10hotplacelist4.jpg").setHeight(900).setWidth(1600)
							.setAccessibilityText("hotplace4"));
		}

		items.add(CarouselBrowseItem1);
		items.add(CarouselBrowseItem2);
		items.add(CarouselBrowseItem3);
		items.add(CarouselBrowseItem4);

		suggestions.add(CommonWord.SUGGEST_WELCOME_1);
		suggestions.add(CommonWord.SUGGEST_WELCOME_2);

		rb.add(simpleResponse);
		rb.add(new CarouselBrowse().setItems(items));
		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		return rb.build();
	}

	private ActionResponse genTravelOption(ResponseBuilder rb) {
		Map<String, Object> data = rb.getConversationData();

		String during = CommonUtil.makeSafeString(data.get("during"));
		String together = CommonUtil.makeSafeString(data.get("together"));
		String traffic = CommonUtil.makeSafeString(data.get("traffic"));
		String travelType = CommonUtil.makeSafeString(data.get("travelType"));
		String resoponse1 = CommonUtil.makeSafeString(data.get("response1"));
		String resoponse2 = CommonUtil.makeSafeString(data.get("response2"));

		// Solution은 0~3값을 가져야 한다.
		// int solution = CommonUtil.makeSafeInt(data.get("solution"));

		CommonUtil.printMapData(data);

		List<String> suggestions = new ArrayList<String>();
		SimpleResponse simpleResponse = new SimpleResponse();
		BasicCard basicCard = new BasicCard();

		// ConnectionType 이 없으면 Symptom 부터

		if (CommonUtil.isEmptyString(during)) {
			simpleResponse.setTextToSpeech("몇가지 질문에 답해주시면 최적화된 여행 루트를 소개해드릴게요.  \n며칠동안 홍천에 방문하실 예정인가요?");
			basicCard.setFormattedText("홍천 여행 기간을 말씀 해 주세요.");
			basicCard
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/during.gif")
							.setAccessibilityText("여행 기간"));
			suggestions.add("당일치기");
			suggestions.add("1박2일");
			suggestions.add("2박3일");
			suggestions.add("맛집 추천");
			rb.add(simpleResponse);
			rb.add(basicCard);

		} else if (CommonUtil.isEmptyString(together)) {
			// check status로 유도
			if (during.equalsIgnoreCase("during1")) {
				data.remove("response1");
				data.put("response1", "하루 동안");
				simpleResponse.setTextToSpeech("하루만으로도 알찬 홍천 여행이 될 수 있게 도와드릴게요.  \n함께 여행하실 분은 어떻게 되나요?");
				basicCard.setFormattedText("같이 여행할 사람을 말씀 해 주세요.");
				basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/with.gif")
						.setAccessibilityText("동행자"))
						.setImageDisplayOptions("DEFAULT");

			} else if (during.equalsIgnoreCase("during2")) {
				data.remove("response1");
				data.put("response1", "1박 2일 동안");
				simpleResponse.setTextToSpeech("이틀은 홍천을 즐기기에 딱 좋은 기간이에요.  \n함께 여행하실 분은 어떻게 되나요?");
				basicCard.setFormattedText("같이 여행할 사람을 말씀 해 주세요.");
				basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/with.gif")
						.setAccessibilityText("동행자"))
						.setImageDisplayOptions("DEFAULT");
			} else if (during.equalsIgnoreCase("during3")) {
				data.remove("response1");
				data.put("response1", "2박 3일 동안");
				simpleResponse.setTextToSpeech("2박 3일은 홍천을 즐기기에 매우 넉넉한 시간이네요.  \n함께 여행하실 분은 어떻게 되나요?");
				basicCard.setFormattedText("같이 여행할 사람을 말씀 해 주세요.");
				basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/with.gif")
						.setAccessibilityText("동행자"))
						.setImageDisplayOptions("DEFAULT");
			} else {
				data.put("during", "during4");
				data.remove("response1");
				data.put("response1", "여행기간 동안");
				simpleResponse.setTextToSpeech("홍천 여행을 즐기기엔 딱 좋은 기간이네요.  \n함께 여행하실 분은 어떻게 되나요?");
				basicCard.setFormattedText("같이 여행할 사람을 말씀 해 주세요.");
				basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/with.gif")
						.setAccessibilityText("동행자"))
						.setImageDisplayOptions("DEFAULT");
			}
			suggestions.add("가족");
			suggestions.add("친구들");
			suggestions.add("연인");
			suggestions.add(CommonWord.SUGGEST_WELCOME_3);

			rb.add(simpleResponse);
			rb.add(basicCard);
		} else if (CommonUtil.isEmptyString(traffic)) {
			if (together.equalsIgnoreCase("with1")) {
				data.remove("response2");
				data.put("response2", "연인과");
				simpleResponse.setTextToSpeech("연인과 함께 여행한다니 아름다운 추억을 남길 수 있게 도와드릴게요.  \n어떤 교통 수단을 사용하실 건가요?");
				basicCard.setFormattedText("여행기간 동안 이동할 때 사용하실 교통수단을 선택해 주세요.");
				basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/traffic.gif")
						.setAccessibilityText("교통 수단"))
						.setImageDisplayOptions("DEFAULT");
			} else if (together.equalsIgnoreCase("with2")) {
				data.remove("response2");
				data.put("response2", "가족과");
				simpleResponse.setTextToSpeech("가족과 함께 여행한다니 따뜻한 추억을 남길 수 있게 도와드릴게요.  \n어떤 교통 수단을 사용하실 건가요?");
				basicCard.setFormattedText("여행기간 동안 이동할 때 사용하실 교통수단을 선택해 주세요.");
				basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/traffic.gif")
						.setAccessibilityText("교통 수단"))
						.setImageDisplayOptions("DEFAULT");
			} else if (together.equalsIgnoreCase("with3")) {
				data.put("response2", "친구들과");
				simpleResponse.setTextToSpeech("친구들과 함께 여행한다니 재밌고 신나는 여행이 되도록 도와드릴게요.  \n어떤 교통 수단을 사용하실 건가요?");
				basicCard.setFormattedText("여행기간 동안 이동할 때 사용하실 교통수단을 선택해 주세요.");
				basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/traffic.gif")
						.setAccessibilityText("교통 수단"))
						.setImageDisplayOptions("DEFAULT");
			} else {
				data.put("together", "with4");
				data.remove("response2");
				data.put("response2", "즐겁게");
				simpleResponse.setTextToSpeech("평생 잊지 못할 추억을 남길 수 있는 여행이 되게 도와드릴게요.  \n어떤 교통 수단을 사용하실 건가요?");
				basicCard.setFormattedText("여행기간 동안 이동할 때 사용하실 교통수단을 선택해 주세요.");
				basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/traffic.gif")
						.setAccessibilityText("교통 수단"))
						.setImageDisplayOptions("DEFAULT");
			}
			suggestions.add("대중교통");
			suggestions.add("자동차");
			suggestions.add("걸어서");
			suggestions.add("자전거");
			suggestions.add(CommonWord.SUGGEST_WELCOME_3);

			rb.add(simpleResponse);
			rb.add(basicCard);
		} else if (CommonUtil.isEmptyString(travelType)) {
			String responseText = "";

			if (traffic.equalsIgnoreCase("traffic1")) {
				responseText = "자동차로 " + resoponse1 + " " + resoponse2 + " 여행 할 수 있는 코스를 추천 해 드렸어요.";
			} else if (traffic.equalsIgnoreCase("traffic2")) {
				responseText = "대중교통으로 " + resoponse1 + " " + resoponse2 + " 여행 할 수 있는 코스를 추천 해 드렸어요.";
			} else if (traffic.equalsIgnoreCase("traffic3")) {
				responseText = "자전거로 " + resoponse1 + " " + resoponse2 + " 여행 할 수 있는 코스를 추천 해 드렸어요.";
			} else if (traffic.equalsIgnoreCase("traffic4")) {
				responseText = "걸어서 " + resoponse1 + " " + resoponse2 + " 여행 할 수 있는 코스를 추천 해 드렸어요.";
			} else {
				data.put("traffic", "traffic5");
				responseText = "자유롭게" + resoponse1 + " " + resoponse2 + " 여행 할 수 있는 코스를 추천 해 드렸어요.";
			}

			simpleResponse.setTextToSpeech(responseText);
			ListSelectListItem listSelectListItem1, listSelectListItem2, listSelectListItem3, listSelectListItem4, listSelectListItem5, listSelectListItem6, listSelectListItem7, listSelectListItem8, listSelectListItem9, listSelectListItem10;
			List<ListSelectListItem> items = new ArrayList<>();

			List<String> synonyms1 = new ArrayList<String>();
			synonyms1.add("cos1");
			List<String> synonyms2 = new ArrayList<String>();
			synonyms2.add("cos2");
			List<String> synonyms3 = new ArrayList<String>();
			synonyms3.add("cos3");
			List<String> synonyms4 = new ArrayList<String>();
			synonyms4.add("cos4");
			List<String> synonyms5 = new ArrayList<String>();
			synonyms5.add("cos5");
			List<String> synonyms6 = new ArrayList<String>();
			synonyms5.add("cos6");
			List<String> synonyms7 = new ArrayList<String>();
			synonyms5.add("cos7");
			List<String> synonyms8 = new ArrayList<String>();
			synonyms5.add("cos8");
			List<String> synonyms9 = new ArrayList<String>();
			synonyms5.add("cos9");
			List<String> synonyms10 = new ArrayList<String>();
			synonyms5.add("cos10");

			listSelectListItem1 = new ListSelectListItem().setTitle("양떼목장")
					.setDescription("휘바핀란드 양떼목장")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist1.jpg")
							.setAccessibilityText("양떼목장"))
					.setOptionInfo(new OptionInfo().setKey("cos1").setSynonyms(synonyms1));

			listSelectListItem2 = new ListSelectListItem().setTitle("살둔마을")
					.setDescription("통나무펜션이 유명한 살둔마을")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist2.jpg")
							.setAccessibilityText("살둔마을"))
					.setOptionInfo(new OptionInfo().setKey("cos2").setSynonyms(synonyms2));

			listSelectListItem3 = new ListSelectListItem().setTitle("가칠봉삼봉약수")
					.setDescription("우리나라 명수 100선에 선정된 이름있는 약수")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist3.jpg")
							.setAccessibilityText("가칠봉삼봉약수"))
					.setOptionInfo(new OptionInfo().setKey("cos3").setSynonyms(synonyms3));

			listSelectListItem4 = new ListSelectListItem().setTitle("생명건강과학관")
					.setDescription("다양한 전시물 관람이 가능한 과학관")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist4.jpg")
							.setAccessibilityText("생명건강과학관"))
					.setOptionInfo(new OptionInfo().setKey("cos4").setSynonyms(synonyms4));

			listSelectListItem5 = new ListSelectListItem().setTitle("모둘자리관광농원")
					.setDescription("휴가철 휴가지로 속색 없는 장소")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist5.jpg")
							.setAccessibilityText("모둘자리관광농원"))
					.setOptionInfo(new OptionInfo().setKey("cos5").setSynonyms(synonyms5));

			listSelectListItem6 = new ListSelectListItem().setTitle("홍천 비발디 파크")
					.setDescription("사개절 내내 다양한 즐길거리와 볼거리가 많은 대표적인 관광지")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist6.jpg")
							.setAccessibilityText("홍천 비발디 파크"))
					.setOptionInfo(new OptionInfo().setKey("cos6").setSynonyms(synonyms6));

			listSelectListItem7 = new ListSelectListItem().setTitle("은행나무숲")
					.setDescription("은행나무가 노랗게 져서 장관을 이룬 아름다운 풍경")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist7.jpg")
							.setAccessibilityText("은행나무숲"))
					.setOptionInfo(new OptionInfo().setKey("cos7").setSynonyms(synonyms7));

			listSelectListItem8 = new ListSelectListItem().setTitle("공작산 수타사")
					.setDescription("100대 명산 중 하나에 속하는 공작산 안에 있는 절")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist8.jpg")
							.setAccessibilityText("공작산 수타사"))
					.setOptionInfo(new OptionInfo().setKey("cos8").setSynonyms(synonyms8));

			listSelectListItem9 = new ListSelectListItem().setTitle("팔봉산 관광지")
					.setDescription("야영장에 대형텐트를 빌려 여행하기 좋은장소")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist9.jpg")
							.setAccessibilityText("팔봉산 관광지"))
					.setOptionInfo(new OptionInfo().setKey("cos9").setSynonyms(synonyms9));

			listSelectListItem10 = new ListSelectListItem().setTitle("힐리언스 선마을")
					.setDescription("자연과 벗한 선마을 내 리조트에서 머물 수 있는 힐링여행")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist10.jpg")
							.setAccessibilityText("힐리언스 선마을"))
					.setOptionInfo(new OptionInfo().setKey("cos10").setSynonyms(synonyms10));

			items.add(listSelectListItem1);
			items.add(listSelectListItem2);
			items.add(listSelectListItem3);
			items.add(listSelectListItem4);
			items.add(listSelectListItem5);
			items.add(listSelectListItem6);
			items.add(listSelectListItem7);
			items.add(listSelectListItem8);
			items.add(listSelectListItem9);
			items.add(listSelectListItem10);

			rb.add(simpleResponse);
			rb.add(new SelectionList().setItems(items));
			suggestions.add(CommonWord.SUGGEST_WELCOME_3);

		} else {
			List<Button> buttons = new ArrayList<>();
			Button button1;
			if (travelType.equalsIgnoreCase("cos1")) {
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=OK8AaNap6nM")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(new SimpleResponse().setTextToSpeech("휘바핀란드 양떼목장 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요.")
						.setDisplayText("휘바핀란드 양떼목장 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요."))
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("양떼목장 내에 있는 펜션을 이용하면 모든 체험을 무료로 이용 가능하다.  \n펜션은 스파펜션, 독채, 복층 등 종류도 다양해서 가족단위나 커플로도 이용할 수 있다.  \n펜션의 지붕이 열려 밤에는 누워서 별빛도 감상 가능하다.  \n수영장에서 수영도 가능하고 야외취사나 바베큐파티도 할 수 있다는 장점이 있다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic1.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));

			} else if (travelType.equalsIgnoreCase("cos2")) {
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=RP1YTyEjeJI")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(new SimpleResponse().setTextToSpeech("살둔마을 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요.")
						.setDisplayText("살둔마을 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요."))
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("살둔마을 내 통나무펜션이 유명하다. 통나무펜션은 홈페이지에서 사전예약을 해야 한다.  \n가족들과 함께 통나무펜션에서 머물면서 자연과 벗하고 물놀이도 하면서 즐길 수 있다.  \n여름철 레프팅 코스로 각광받고 있고, 봄에는 내린천변을 따라서 핀 철쭉이 장관을 이룬다.  \n마을에 위치한 생둔분교는 외관이 근래 보기드문 나무벽이 아름답다.  \n친구들끼리 또는 가족끼리 함께 놀러가서 추억만들기에는 딱이다.  \n주차시설도 잘되어 있고 농가체험도 가능해 아이들과 함께하는 부부에게도 최적의 장소다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic2.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));

			} else if (travelType.equalsIgnoreCase("cos3")) {
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=VnB5yuEgHP8")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(new SimpleResponse().setTextToSpeech("가칠봉삼봉약수 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요.")
						.setDisplayText("가칠봉삼봉약수 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요."))
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("우리나라 명수 100선에 선정된 이름있는 약수다. 15가지의 약수성분이 함유돼 있어 약수에 들르면 꼭 마셔보기를 권장한다. 특히 빈혈이나 신경통,당뇨병들에 효험이 있다고 해서 유명해진 약수터다.  \n산행코스에 급경사가 있어 평탄한 길은 아니니 산행복을 입고 가는게 좋다.  \n삼봉자연휴양림도 같이 있어 휴양차 갔다가 약수를 들르는 것도 좋은 방법이다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic3.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));

			} else if (travelType.equalsIgnoreCase("cos4")) {
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=O2mA1icWM2Q")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(new SimpleResponse().setTextToSpeech("생명건강과학관 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요.")
						.setDisplayText("생명건강과학관 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요."))
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("홍천터미널에서 시내버스를 타면 금방이다. 택시를 이용할 경우에는 택시비가 약3,000원정도 드니 여러명이서 왔을 경우엔 택시를 타는 것도 괜찮은 방법이다. 전시물들이 다양하게 있는데 그 중 스파이더맨 전시물은 과학관 내에서 사진찍기 좋은 장소 중 하나이다.   \n주로 생명과 건강에 관련한 전시물들이 전시돼 있다.  \n오감으로 느끼고 체험할 수 있어서 아이들의 교육을 위해서 오기에도 좋은 장소이며, 홍천어트렉션은 누구나 즐겁게 즐길 수 있는 코너로 가족단위 방문객이 많은 편이다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic4.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));

			} else if (travelType.equalsIgnoreCase("cos5")) {
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=b4YC67KdLpo")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(new SimpleResponse().setTextToSpeech("모둘자리관광노원 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요.")
						.setDisplayText("모둘자리관광노원 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요."))
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("계절마다 패키지 프로그램의 형태로 공식 홈페이지에서 예약구매해서 이용가능하다.  \n패키지를 구매하면 식사를 제공해주고, 짚라인이나 호수쪽배타기 등 다양한 활동도 체험할 수 있다.  \n기업연수나 행사를 위해서도 많이들 예약하며 가족끼리 또는 연인끼리 놀러와서 즐기기에 좋다.  \n한옥을 개조한 숙박객실도 있어서 1박2일 패키지로 이용하는 것도 좋은 방법이다.  \n휴가철 휴가지로도 속색이 없는 장소로 휴가철에 가장 사람들이 많다. 봄이나 가을이 가장 놀러가기 좋은 시기다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic5.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));

			} else if (travelType.equalsIgnoreCase("cos6")) {
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=lKywjjENPrY")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(new SimpleResponse().setTextToSpeech("홍천 비발디 파크 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요.")
						.setDisplayText("홍천 비발디 파크 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요."))
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("사계절 내내 다양한 즐길거리와 볼거리가 많은 대표 관광지이다.  \n매년 여름 많은 사람들이 물놀이를 위해 오션월드를 찾아오며 겨울에는 스키를 즐기기 위한 사람들로 북적북적 하다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic6.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));

			} else if (travelType.equalsIgnoreCase("cos7")) {
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=awzks49B2io")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(new SimpleResponse().setTextToSpeech("은행나무 숲 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요.")
						.setDisplayText("은행나무 숲 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요."))
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("가을 단풍이 들 때 사람들로 붐빈다. 은행나무가 노랗게 져서 장관을 이룬다." +
										"그 길을 연인들이 걸으면서 대화나누기엔 더할 나위 없이 좋은 곳이다.  \n비밀의화원이라고도 불리는 이곳은 사유지이지만, 일반인들에게 무료로 제공된다." +
										"근처에 달둔길 트레킹 코스가 있어 함께 들러보면 좋을 듯 하다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic7.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));

			} else if (travelType.equalsIgnoreCase("cos8")) {
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=E9F-mQqtLxk")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(new SimpleResponse().setTextToSpeech("공작산 수타사 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요.")
						.setDisplayText("공작산 수타사 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요."))
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("100대 명산 중 하나에 속하는 공작산 안에 있는 절이다.  \n" +
										"주차요금은 무료이기 때문에 차량 이동이 쉽다." +
										"수타사의 박물관이라 불리는 보장각에는 다양한 불교 문화재가 전시돼 있다." +
										"동종각은 보물로도 지정돼 있다고 하니 절에 들리면 꼭 기념사진을 찍는다." +
										"수타사 생태숲공원이 조성돼어 있어 걷기에도 좋다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic8.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));

			} else if (travelType.equalsIgnoreCase("cos9")) {
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=5OJtR2zckho")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(new SimpleResponse().setTextToSpeech("팔봉산 관광지 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요.")
						.setDisplayText("팔봉산 관광지 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요."))
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("야영장에 대형텐트를 빌려서 1박2일로 여행하기에도 좋은 장소다.  \n" +
										"야영장 데크를 사용할 수도 있고 풋살경기장이 있어 경기를 펼칠 수도 있다." +
										"야외공연장에서는 공연을 하는데 아이들과 함께 공연 구경을 하면 좋을 듯 하다.  \n" +
										"근처에 다양한 맛집들과 카페가 있어 차를 타고 가서 바로 먹을 수 있다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic9.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));

			} else if (travelType.equalsIgnoreCase("cos10")) {
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=FP1oZS-XRYA")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(new SimpleResponse().setTextToSpeech("힐리언스 선마을린 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요.")
						.setDisplayText("힐리언스 선마을 코스의 상세 정보입니다.  \n다른 코스를 원하시거나 해당 코스의 홍천 맛집을 알고 싶으시면 추천 키워드를 포함하여 말씀해 주세요."))
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("자연과 벗한 선마을 내 리조트에서 머물면서 힐링여행을 즐기기에 좋다.  \n" +
										"힐리언스 선마을은 자연속에서 온전한 나를 마주하고 치유받는 국내 최초의 웰 에이징 리조트로, 가족단위로 혹은 개인이 자연에서 여유롭게 쉬고 싶을 때 가면 좋은 곳이다." +
										"식습관 개선부터 명상과 요가,숲 체험 등 갖가지 힐링 프로그램을 배울 수 있다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic10.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));
			}
			data.remove("travelType");
			suggestions.add("근처 맛집 추천");
			suggestions.add("다른 관광지 추천");
		}
		suggestions.add(CommonWord.SUGGEST_WELCOME_1);
		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		return rb.build();
	}


	@ForIntent("travel-retry")
	public ActionResponse processSupportRetry(ActionRequest request) throws
			ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);

		return genTravelOption(rb);
	}


	@ForIntent("travel-mobile")
	public ActionResponse processSupportToMobile(ActionRequest request)
			throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		List<String> suggestions = new ArrayList<String>();

		Map<String, Object> data = rb.getConversationData();

		String during = CommonUtil.makeSafeString(data.get("during"));
		String together = CommonUtil.makeSafeString(data.get("together"));
		String traffic = CommonUtil.makeSafeString(data.get("traffic"));
		String traveltype = CommonUtil.makeSafeString(data.get("traveltype"));


		CommonUtil.printMapData(data);

		String encodedUrl = null;
		try {
			encodedUrl = URLEncoder.encode("https://assistant.google.com/services/invoke/uid/000000909ea7c0f0?intent=travel-resume&param.pa1=" // 000000ed4bb85dee
							+ during + "&param.pa2=" + together + "&param.pa3=" + traffic + "&param.pa4=" + traveltype,//00000091d64b60fe -o2odev1
					StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		rb.add(new SimpleResponse().setTextToSpeech("다음 QR코드를 모바일에서 읽을 경우 Actions를 모바일에서 계속 하실 수 있습니다."))
				.add(new BasicCard()//.setTitle("QR코드를 통한 모바일 링크")
						.setFormattedText("  \n다음 QR코드를 모바일에서 읽을 경우 Actions를 모so바일에서 계속 하실 수 있습니다.  \n1. Google Assisatant가 가능한 휴대폰  \n(ios - 카메라 실행 / android - QR인식 카메라모듈 실행)  \n2. QR 이미지 인식  \n 3.홍천 투어 화면 이어서 진행 시작 ")
						.setImage(new Image().setUrl("https://actions.o2o.kr/skylife/api/1.0/qrcode?url=" + encodedUrl)
								.setAccessibilityText("모바일 장치 연결을 위한 QR코드"))
						.setImageDisplayOptions("DEFAULT"));

		suggestions.add(CommonWord.SUGGEST_RETRY_SPEAK);


		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		return rb.build();
	}

	@ForIntent("travel-resume")
	public ActionResponse processSupportResume(ActionRequest request) throws
			ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);

		String during = CommonUtil.makeSafeString(request.getParameter("pa1"));
		String together = CommonUtil.makeSafeString(request.getParameter("pa2"));
		String traffic = CommonUtil.makeSafeString(request.getParameter("pa3"));
		String traveltype = CommonUtil.makeSafeString(request.getParameter("pa4"));


		ActionContext context = new ActionContext("find-options", 5);
		rb.add(context);
		if (!CommonUtil.isEmptyString(traveltype)) {
			context = new ActionContext("choose-travel", 5);
			rb.add(context);
		}

		Map<String, Object> data = rb.getConversationData();
		data.clear();

		if (!during.isEmpty())
			data.put("during", during);
		if (!together.isEmpty())
			data.put("together", together);
		if (!traffic.isEmpty())
			data.put("traffic", traffic);

		return genTravelOption(rb);
	}

	@ForIntent("Default Fallback Intent")
	public ActionResponse defaultFallback(ActionRequest request) throws
			ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);

		List<String> suggestions = new ArrayList<String>();
		BasicCard basicCard = new BasicCard();

		rb.add(new SimpleResponse().setTextToSpeech("죄송하지만 말씀하신 요청은 지원할 수 없는 서비스입니다.  \n홍천 투어는 홍천의 관광지 추천과 무인민원 발급기 안내 및 민원서류 안내 서비스만 가능합니다.  \n아래 추천 키워드를 참고하시고 다시 말씀해 주세요."));
		basicCard.setFormattedText("죄송하지만 말씀하신 요청은 지원할 수 없는 서비스입니다.  \n홍천 투어는 홍천의 관광지 추천과 무인민원 발급기 안내 및 민원서류 안내 서비스만 가능합니다.  \n아래 추천 키워드를 참고하시고 다시 말씀해 주세요.");
		basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/tourhome.gif")
				.setAccessibilityText("홍천 투어 상담원 이미지"))
				.setImageDisplayOptions("DEFAULT");
		suggestions.add(CommonWord.SUGGEST_WELCOME_1);
		suggestions.add(CommonWord.SUGGEST_WELCOME_2);
		suggestions.add(CommonWord.SUGGEST_WELCOME_3);

		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		rb.add(basicCard);

		return rb.build();
	}

	@ForIntent("Default Welcome Intent")
	public ActionResponse defaultWelcome(ActionRequest request) throws
			ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		BasicCard basicCard = new BasicCard();
		List<String> suggestions = new ArrayList<String>();
		Map<String, Object> data = rb.getConversationData();

		data.clear();
		rb.add(new SimpleResponse()
				//.setTextToSpeech("<speak><sub alias=''>안녕하세요, 홍천 여행입니다.  \n무엇을 도와드릴까요?"));
				.setTextToSpeech("안녕하세요, 홍천 여행입니다.  \\n무엇을 도와드릴까요?"));

		basicCard.setFormattedText("홍천 투어 안내를 도와드릴게요.  \n상담원에게 추천 키워드를 참고하셔서 무엇을 원하는지 말씀해 주세요.");
		basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/tourhome.gif")
				.setAccessibilityText("홍천 투어 상담원 이미지"))
				.setImageDisplayOptions("DEFAULT");

		suggestions.add(CommonWord.SUGGEST_WELCOME_1);
		suggestions.add(CommonWord.SUGGEST_WELCOME_2);
		suggestions.add(CommonWord.SUGGEST_WELCOME_3);

		rb.add(basicCard);
		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));

		return rb.build();
	}

	private ActionResponse tourFallback(ResponseBuilder rb) {
		Map<String, Object> data = rb.getConversationData();

		String during = CommonUtil.makeSafeString(data.get("during"));
		String together = CommonUtil.makeSafeString(data.get("together"));
		String traffic = CommonUtil.makeSafeString(data.get("traffic"));
		String travelType = CommonUtil.makeSafeString(data.get("travelType"));
		String resoponse1 = CommonUtil.makeSafeString(data.get("response1"));
		String resoponse2 = CommonUtil.makeSafeString(data.get("response2"));

		CommonUtil.printMapData(data);

		List<String> suggestions = new ArrayList<String>();
		SimpleResponse simpleResponse = new SimpleResponse();
		BasicCard basicCard = new BasicCard();

		// ConnectionType 이 없으면 Symptom 부터

		if (CommonUtil.isEmptyString(during)) {
			simpleResponse.setTextToSpeech("죄송하지만 총 여행 기간을 추천 키워드를 참고하셔서 다시 한번 말씀해 주시겠습니까?  \n당일치기는 1일, 1박 2일이면 2일 식으로 말씀해 주시고 답변은 최대 3일까지 가능합니다.");
			basicCard.setFormattedText("  \n<1일, 2일, 3일>");
			basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/during.gif")
					.setAccessibilityText("여행 기간"))
					.setImageDisplayOptions("DEFAULT");
			suggestions.add("1일");
			suggestions.add("2일");
			suggestions.add("3일");
			suggestions.add(CommonWord.SUGGEST_WELCOME_1);
			suggestions.add(CommonWord.SUGGEST_WELCOME_3);
			rb.add(simpleResponse);
			rb.add(basicCard);

		} else if (CommonUtil.isEmptyString(together)) {
			// check status로 유도
			simpleResponse.setTextToSpeech("누구와 같이 여행을 가시는지 다시 한번 말씀해 주시겠어요?  \n연인, 친구들, 가족, 혼자서 4가지 추천 키워드 안에서 말씀해 주세요.");
			basicCard.setFormattedText("  \n<연인, 친구들, 가족, 혼자서>");
			basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/with.gif")
					.setAccessibilityText("동행자"))
					.setImageDisplayOptions("DEFAULT");
			suggestions.add("연인");
			suggestions.add("친구들");
			suggestions.add("가족");
			suggestions.add("혼자서");
			suggestions.add(CommonWord.SUGGEST_WELCOME_1);
			suggestions.add(CommonWord.SUGGEST_WELCOME_3);
			rb.add(simpleResponse);
			rb.add(basicCard);
		} else if (CommonUtil.isEmptyString(traffic)) {
			simpleResponse.setTextToSpeech("여행하실 때 주로 이용하실 교통수단을 다시 말씀해 주십시오.  \n추천 키워드에 보이는 자가용, 대중교통, 자전거, 걸어서 중에서 말씀해 주세요.");
			basicCard.setFormattedText("  \n<자가용, 대중교통, 자전거, 걸어서>");
			basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/traffic.gif")
					.setAccessibilityText("교통 수단"))
					.setImageDisplayOptions("DEFAULT");

			suggestions.add("대중교통");
			suggestions.add("자동차");
			suggestions.add("걸어서");
			suggestions.add("자전거");
			suggestions.add(CommonWord.SUGGEST_WELCOME_1);
			suggestions.add(CommonWord.SUGGEST_WELCOME_3);
			rb.add(simpleResponse);
			rb.add(basicCard);
		} else if (CommonUtil.isEmptyString(travelType)) {

			simpleResponse.setTextToSpeech("죄송하지만 리스트에 있는 코스 중 선택해서 다시 말씀해 주세요.  \n");
			ListSelectListItem listSelectListItem1, listSelectListItem2, listSelectListItem3, listSelectListItem4, listSelectListItem5, listSelectListItem6, listSelectListItem7, listSelectListItem8, listSelectListItem9, listSelectListItem10;
			List<ListSelectListItem> items = new ArrayList<>();

			List<String> synonyms1 = new ArrayList<String>();
			synonyms1.add("cos1");
			List<String> synonyms2 = new ArrayList<String>();
			synonyms2.add("cos2");
			List<String> synonyms3 = new ArrayList<String>();
			synonyms3.add("cos3");
			List<String> synonyms4 = new ArrayList<String>();
			synonyms4.add("cos4");
			List<String> synonyms5 = new ArrayList<String>();
			synonyms5.add("cos5");
			List<String> synonyms6 = new ArrayList<String>();
			synonyms5.add("cos6");
			List<String> synonyms7 = new ArrayList<String>();
			synonyms5.add("cos7");
			List<String> synonyms8 = new ArrayList<String>();
			synonyms5.add("cos8");
			List<String> synonyms9 = new ArrayList<String>();
			synonyms5.add("cos9");
			List<String> synonyms10 = new ArrayList<String>();
			synonyms5.add("cos10");

			listSelectListItem1 = new ListSelectListItem().setTitle("양떼목장")
					.setDescription("휘바핀란드 양떼목장")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist1.jpg")
							.setAccessibilityText("양떼목장"))
					.setOptionInfo(new OptionInfo().setKey("cos1").setSynonyms(synonyms1));

			listSelectListItem2 = new ListSelectListItem().setTitle("살둔마을")
					.setDescription("통나무펜션이 유명한 살둔마을")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist2.jpg")
							.setAccessibilityText("살둔마을"))
					.setOptionInfo(new OptionInfo().setKey("cos2").setSynonyms(synonyms2));

			listSelectListItem3 = new ListSelectListItem().setTitle("가칠봉삼봉약수")
					.setDescription("우리나라 명수 100선에 선정된 이름있는 약수")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist3.jpg")
							.setAccessibilityText("가칠봉삼봉약수"))
					.setOptionInfo(new OptionInfo().setKey("cos3").setSynonyms(synonyms3));

			listSelectListItem4 = new ListSelectListItem().setTitle("생명건강과학관")
					.setDescription("다양한 전시물 관람이 가능한 과학관")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist4.jpg")
							.setAccessibilityText("생명건강과학관"))
					.setOptionInfo(new OptionInfo().setKey("cos4").setSynonyms(synonyms4));

			listSelectListItem5 = new ListSelectListItem().setTitle("모둘자리관광농원")
					.setDescription("휴가철 휴가지로 속색 없는 장소")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist5.jpg")
							.setAccessibilityText("모둘자리관광농원"))
					.setOptionInfo(new OptionInfo().setKey("cos5").setSynonyms(synonyms5));

			listSelectListItem6 = new ListSelectListItem().setTitle("홍천 비발디 파크")
					.setDescription("사개절 내내 다양한 즐길거리와 볼거리가 많은 대표적인 관광지")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist6.jpg")
							.setAccessibilityText("홍천 비발디 파크"))
					.setOptionInfo(new OptionInfo().setKey("cos6").setSynonyms(synonyms6));

			listSelectListItem7 = new ListSelectListItem().setTitle("은행나무숲")
					.setDescription("은행나무가 노랗게 져서 장관을 이룬 아름다운 풍경")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist7.jpg")
							.setAccessibilityText("은행나무숲"))
					.setOptionInfo(new OptionInfo().setKey("cos7").setSynonyms(synonyms7));

			listSelectListItem8 = new ListSelectListItem().setTitle("공작산 수타사")
					.setDescription("100대 명산 중 하나에 속하는 공작산 안에 있는 절")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist8.jpg")
							.setAccessibilityText("공작산 수타사"))
					.setOptionInfo(new OptionInfo().setKey("cos8").setSynonyms(synonyms8));

			listSelectListItem9 = new ListSelectListItem().setTitle("팔봉산 관광지")
					.setDescription("야영장에 대형텐트를 빌려 여행하기 좋은장소")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist9.jpg")
							.setAccessibilityText("팔봉산 관광지"))
					.setOptionInfo(new OptionInfo().setKey("cos9").setSynonyms(synonyms9));

			listSelectListItem10 = new ListSelectListItem().setTitle("힐리언스 선마을")
					.setDescription("자연과 벗한 선마을 내 리조트에서 머물 수 있는 힐링여행")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/tour/coslist10.jpg")
							.setAccessibilityText("힐리언스 선마을"))
					.setOptionInfo(new OptionInfo().setKey("cos10").setSynonyms(synonyms10));
			data.remove("travelType");

			items.add(listSelectListItem1);
			items.add(listSelectListItem2);
			items.add(listSelectListItem3);
			items.add(listSelectListItem4);
			items.add(listSelectListItem5);
			items.add(listSelectListItem6);
			items.add(listSelectListItem7);
			items.add(listSelectListItem8);
			items.add(listSelectListItem9);
			items.add(listSelectListItem10);

			rb.add(simpleResponse);
			rb.add(new SelectionList().setItems(items));
			suggestions.add(CommonWord.SUGGEST_WELCOME_1);
			suggestions.add(CommonWord.SUGGEST_WELCOME_3);

		} else {
			List<Button> buttons = new ArrayList<>();
			Button button1;

			if (travelType.equalsIgnoreCase("cos1")) {
				simpleResponse.setTextToSpeech("죄송하지만 알아듣지 못했습니다.  \n리스트를 다시 보고 싶으시면 다른 코스 추천이라고 말씀해 주시고 선택했던 휘바핀란드 코스 근처에 있는 맛집을 알고 싶으시면 근처 맛집 추천이라고 말씀 하시면 됩니다.");
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=OK8AaNap6nM")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(simpleResponse)
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("양떼목장 내에 있는 펜션을 이용하면 모든 체험을 무료로 이용 가능하다.  \n펜션은 스파펜션, 독채, 복층 등 종류도 다양해서 가족단위나 커플로도 이용할 수 있다.  \n펜션의 지붕이 열려 밤에는 누워서 별빛도 감상 가능하다.  \n수영장에서 수영도 가능하고 야외취사나 바베큐파티도 할 수 있다는 장점이 있다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic1.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));

			} else if (travelType.equalsIgnoreCase("cos2")) {
				simpleResponse.setTextToSpeech("죄송하지만 알아듣지 못했습니다.  \n리스트를 다시 보고 싶으시면 다른 코스 추천이라고 말씀해 주시고 선택했던 살둔마을 통나무펜션 코스 근처에 있는 맛집을 알고 싶으시면 근처 맛집 추천이라고 말씀 하시면 됩니다.");
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=RP1YTyEjeJI")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(simpleResponse)
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("살둔마을 내 통나무펜션이 유명하다. 통나무펜션은 홈페이지에서 사전예약을 해야 한다.  \n가족들과 함께 통나무펜션에서 머물면서 자연과 벗하고 물놀이도 하면서 즐길 수 있다.  \n여름철 레프팅 코스로 각광받고 있고, 봄에는 내린천변을 따라서 핀 철쭉이 장관을 이룬다.  \n마을에 위치한 생둔분교는 외관이 근래 보기드문 나무벽이 아름답다.  \n친구들끼리 또는 가족끼리 함께 놀러가서 추억만들기에는 딱이다.  \n주차시설도 잘되어 있고 농가체험도 가능해 아이들과 함께하는 부부에게도 최적의 장소다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic2.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));
			} else if (travelType.equalsIgnoreCase("cos3")) {
				simpleResponse.setTextToSpeech("죄송하지만 알아듣지 못했습니다.  \n리스트를 다시 보고 싶으시면 다른 코스 추천이라고 말씀해 주시고 선택했던 가칠봉삼봉약수 코스 근처에 있는 맛집을 알고 싶으시면 근처 맛집 추천이라고 말씀 하시면 됩니다.");
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=VnB5yuEgHP8")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(simpleResponse)
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("우리나라 명수 100선에 선정된 이름있는 약수다. 15가지의 약수성분이 함유돼 있어 약수에 들르면 꼭 마셔보기를 권장한다. 특히 빈혈이나 신경통,당뇨병들에 효험이 있다고 해서 유명해진 약수터다.  \n산행코스에 급경사가 있어 평탄한 길은 아니니 산행복을 입고 가는게 좋다.  \n삼봉자연휴양림도 같이 있어 휴양차 갔다가 약수를 들르는 것도 좋은 방법이다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic3.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));
			} else if (travelType.equalsIgnoreCase("cos4")) {
				simpleResponse.setTextToSpeech("죄송하지만 알아듣지 못했습니다.  \n리스트를 다시 보고 싶으시면 다른 코스 추천이라고 말씀해 주시고 선택했던 생명건강과학관 코스 근처에 있는 맛집을 알고 싶으시면 근처 맛집 추천이라고 말씀 하시면 됩니다.");
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=O2mA1icWM2Q")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(simpleResponse)
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("홍천터미널에서 시내버스를 타면 금방이다. 택시를 이용할 경우에는 택시비가 약3,000원정도 드니 여러명이서 왔을 경우엔 택시를 타는 것도 괜찮은 방법이다. 전시물들이 다양하게 있는데 그 중 스파이더맨 전시물은 과학관 내에서 사진찍기 좋은 장소 중 하나이다.   \n주로 생명과 건강에 관련한 전시물들이 전시돼 있다.  \n오감으로 느끼고 체험할 수 있어서 아이들의 교육을 위해서 오기에도 좋은 장소이며, 홍천어트렉션은 누구나 즐겁게 즐길 수 있는 코너로 가족단위 방문객이 많은 편이다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic4.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));
			} else if (travelType.equalsIgnoreCase("cos5")) {
				simpleResponse.setTextToSpeech("죄송하지만 알아듣지 못했습니다.  \n리스트를 다시 보고 싶으시면 다른 코스 추천이라고 말씀해 주시고 선택했던 모둘자리관광농원 코스 근처에 있는 맛집을 알고 싶으시면 근처 맛집 추천이라고 말씀 하시면 됩니다.");
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=b4YC67KdLpo")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(simpleResponse)
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("계절마다 패키지 프로그램의 형태로 공식 홈페이지에서 예약구매해서 이용가능하다.  \n패키지를 구매하면 식사를 제공해주고, 짚라인이나 호수쪽배타기 등 다양한 활동도 체험할 수 있다.  \n기업연수나 행사를 위해서도 많이들 예약하며 가족끼리 또는 연인끼리 놀러와서 즐기기에 좋다.  \n한옥을 개조한 숙박객실도 있어서 1박2일 패키지로 이용하는 것도 좋은 방법이다.  \n휴가철 휴가지로도 속색이 없는 장소로 휴가철에 가장 사람들이 많다. 봄이나 가을이 가장 놀러가기 좋은 시기다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic5.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));
			} else if (travelType.equalsIgnoreCase("cos6")) {
				simpleResponse.setTextToSpeech("죄송하지만 알아듣지 못했습니다.  \n리스트를 다시 보고 싶으시면 다른 코스 추천이라고 말씀해 주시고 선택했던 홍천 비발디파크 코스 근처에 있는 맛집을 알고 싶으시면 근처 맛집 추천이라고 말씀 하시면 됩니다.");
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=lKywjjENPrY")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(simpleResponse)
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("사계절 내내 다양한 즐길거리와 볼거리가 많은 대표 관광지이다.  \n매년 여름 많은 사람들이 물놀이를 위해 오션월드를 찾아오며 겨울에는 스키를 즐기기 위한 사람들로 북적북적 하다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic6.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));
			} else if (travelType.equalsIgnoreCase("cos7")) {
				simpleResponse.setTextToSpeech("죄송하지만 알아듣지 못했습니다.  \n리스트를 다시 보고 싶으시면 다른 코스 추천이라고 말씀해 주시고 선택했던 은행나무숲 코스 근처에 있는 맛집을 알고 싶으시면 근처 맛집 추천이라고 말씀 하시면 됩니다.");
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=awzks49B2io")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(simpleResponse)
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("가을 단풍이 들 때 사람들로 붐빈다. 은행나무가 노랗게 져서 장관을 이룬다." +
										"그 길을 연인들이 걸으면서 대화나누기엔 더할 나위 없이 좋은 곳이다.  \n비밀의화원이라고도 불리는 이곳은 사유지이지만, 일반인들에게 무료로 제공된다." +
										"근처에 달둔길 트레킹 코스가 있어 함께 들러보면 좋을 듯 하다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic7.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));
			} else if (travelType.equalsIgnoreCase("cos8")) {
				simpleResponse.setTextToSpeech("죄송하지만 알아듣지 못했습니다.  \n리스트를 다시 보고 싶으시면 다른 코스 추천이라고 말씀해 주시고 선택했던 공작산 수타사 코스 근처에 있는 맛집을 알고 싶으시면 근처 맛집 추천이라고 말씀 하시면 됩니다.");
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=E9F-mQqtLxk")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(simpleResponse)
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("100대 명산 중 하나에 속하는 공작산 안에 있는 절이다.  \n" +
										"주차요금은 무료이기 때문에 차량 이동이 쉽다." +
										"수타사의 박물관이라 불리는 보장각에는 다양한 불교 문화재가 전시돼 있다." +
										"동종각은 보물로도 지정돼 있다고 하니 절에 들리면 꼭 기념사진을 찍는다." +
										"수타사 생태숲공원이 조성돼어 있어 걷기에도 좋다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic8.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));
			} else if (travelType.equalsIgnoreCase("cos9")) {
				simpleResponse.setTextToSpeech("죄송하지만 알아듣지 못했습니다.  \n리스트를 다시 보고 싶으시면 다른 코스 추천이라고 말씀해 주시고 선택했던 팔봉산 관광지 코스 근처에 있는 맛집을 알고 싶으시면 근처 맛집 추천이라고 말씀 하시면 됩니다.");
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=5OJtR2zckho")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(simpleResponse)
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("야영장에 대형텐트를 빌려서 1박2일로 여행하기에도 좋은 장소다.  \n" +
										"야영장 데크를 사용할 수도 있고 풋살경기장이 있어 경기를 펼칠 수도 있다." +
										"야외공연장에서는 공연을 하는데 아이들과 함께 공연 구경을 하면 좋을 듯 하다.  \n" +
										"근처에 다양한 맛집들과 카페가 있어 차를 타고 가서 바로 먹을 수 있다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic9.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));

			} else if (travelType.equalsIgnoreCase("cos10")) {
				simpleResponse.setTextToSpeech("죄송하지만 알아듣지 못했습니다.  \n리스트를 다시 보고 싶으시면 다른 코스 추천이라고 말씀해 주시고 선택했던 힐리언스 선마을 코스 근처에 있는 맛집을 알고 싶으시면 근처 맛집 추천이라고 말씀 하시면 됩니다.");
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://www.youtube.com/watch?v=FP1oZS-XRYA")).setTitle("YouTube영상");
				buttons.add(button1);
				rb.add(simpleResponse)
						.add(new BasicCard().setButtons(buttons)
								.setFormattedText("자연과 벗한 선마을 내 리조트에서 머물면서 힐링여행을 즐기기에 좋다.  \n" +
										"힐리언스 선마을은 자연속에서 온전한 나를 마주하고 치유받는 국내 최초의 웰 에이징 리조트로, 가족단위로 혹은 개인이 자연에서 여유롭게 쉬고 싶을 때 가면 좋은 곳이다." +
										"식습관 개선부터 명상과 요가,숲 체험 등 갖가지 힐링 프로그램을 배울 수 있다.")
								.setImage(new Image().setUrl("https://actions.o2o.kr/content/cosbasic10.png")
										.setAccessibilityText("코스위치"))
								.setImageDisplayOptions("DEFAULT"));
			}

			suggestions.add("근처 맛집 추천");
			suggestions.add("다른 관광지 추천");
			suggestions.add(CommonWord.SUGGEST_WELCOME_1);
		}
		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		return rb.build();
	}

	private void naverGeoApi(String locate, String fileName) {
		String clientId = "u0z7i7lig3 ";  //clientId
		String clientSecret = "p8SiiEayxGneN3Wr4ZOmNEANRw4AaUJnVC21hO7b ";  //clientSecret

		String marker = locate.replaceAll(",", "%20");

		try {
			//String addr = URLEncoder.encode("https://naveropenapi.apigw.ntruss.com/map-static/v2/raster?w=300&h=300&markers=type:d|size:mid|pos:"+locate+"&level=16&format=png", "UTF-8");  //주소입력
			String apiURL = "https://naveropenapi.apigw.ntruss.com/map-static/v2/raster?w=400&h=300"
					+ "&markers=type:d|size:mid|pos:" + marker + "|viewSizeRatio:1.0&center=" + locate + "&level=15&scale=2"; //json
			//String apiURL = "https://openapi.naver.com/v1/map/geocode.xml?query=" + addr; // xml
			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
			con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
			int responseCode = con.getResponseCode();
			BufferedReader br;
			if (responseCode == 200) {
				InputStream input = con.getInputStream();
				File file = new File("/var/www/html/content/" + fileName + ".png");
				OutputStream outputStream = new FileOutputStream(file, true);
				int read;
				byte[] bytes = new byte[1024];
				while ((read = input.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}
				input.close();

			} else {
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while ((inputLine = br.readLine()) != null) {
					response.append(inputLine);
				}
				br.close();
				System.out.println(response.toString());
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
}
