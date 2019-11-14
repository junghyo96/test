package com.o2o.action.server.app;

import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.actions.api.response.helperintent.SelectionCarousel;
import com.google.api.services.actions_fulfillment.v2.model.BasicCard;
import com.google.api.services.actions_fulfillment.v2.model.Button;
import com.google.api.services.actions_fulfillment.v2.model.CarouselSelectCarouselItem;
import com.google.api.services.actions_fulfillment.v2.model.Image;
import com.google.api.services.actions_fulfillment.v2.model.OpenUrlAction;
import com.google.api.services.actions_fulfillment.v2.model.OptionInfo;
import com.google.api.services.actions_fulfillment.v2.model.SimpleResponse;
import com.o2o.action.server.util.CommonUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TelechipsEn extends DialogflowApp {
	@ForIntent("Default Fallback Intent")
	public ActionResponse defaultFallback(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		SimpleResponse simpleResponse = new SimpleResponse();
		BasicCard basicCard = new BasicCard();

		List<String> suggestions = new ArrayList<String>();
		Map<String, Object> data = rb.getConversationData();

		CommonUtil.printMapData(data);
		simpleResponse.setTextToSpeech("죄송합니다. 다시 한번 정확한 발음으로 말씀해 주세요.  텔레칩스 회사에대해 궁굼 하시면  텔레칩스 소개라고 말씀하시거나 텔레칩스 제품에 대해 궁금하시면 텔레칩스 제품이라고 말씀하세요.")
				.setDisplayText("죄송합니다. 다시 한번 정확한 발음으로 말씀해 주세요.  \n텔레칩스 회사에대해 궁굼 하시면  텔레칩스 소개라고 말씀하시거나 텔레칩스 제품에 대해 궁금하시면 텔레칩스 제품이라고 말씀하세요.");
		basicCard
				.setFormattedText("죄송합니다. 다시 한번 정확한 발음으로 말씀해 주세요.  \n텔레칩스 회사에대해 궁굼 하시면  텔레칩스 소개라고 말씀하시거나 텔레칩스 제품에 대해 궁금하시면 텔레칩스 제품이라고 말씀하세요.")
				.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/telechipsaiperson.gif")
						.setAccessibilityText("retry"));

		suggestions.add("텔레칩스 소개");
		suggestions.add("텔레칩스 제품");
		suggestions.add("모바일로");
		suggestions.add("영어로");
		suggestions.add("다시 말해줘");

		rb.add(simpleResponse);
		rb.add(basicCard);

		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		return rb.build();
	}

	@ForIntent("Default Welcome Intent")
	public ActionResponse defaultWelcome(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		List<String> suggestions = new ArrayList<String>();
		SimpleResponse simpleResponse = new SimpleResponse();
		BasicCard basicCard = new BasicCard();

		rb.removeContext("Descript-Options");
		rb.removeContext("Category-Options");
		Map<String, Object> data = rb.getConversationData();


		data.clear();
		CommonUtil.printMapData(data);

		simpleResponse.setTextToSpeech("안녕하세요, 텔레칩스입니다. 텔레칩스 회사에대해 궁굼 하시면  텔레칩스 소개라고 말씀하시거나 텔레칩스 제품에 대해 궁금하시면 텔레칩스 제품이라고 말씀하세요.")
				.setDisplayText("안녕하세요, 텔레칩스입니다. 텔레칩스 회사에대해 궁굼 하시면  텔레칩스 소개라고 말씀하시거나 텔레칩스 제품에 대해 궁금하시면 텔레칩스 제품이라고 말씀하세요.")
		;
		basicCard
				.setFormattedText("안녕하세요, 텔레칩스입니다. 텔레칩스 회사에대해 궁굼 하시면  텔레칩스 소개라고 말씀하시거나 텔레칩스 제품에 대해 궁금하시면 텔레칩스 제품이라고 말씀하세요.")
				.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/telechipsaiperson.gif")
						.setAccessibilityText("home"));

		suggestions.add("텔레칩스 소개");
		suggestions.add("텔레칩스 제품");
		suggestions.add("모바일로");
		suggestions.add("영어로");
		suggestions.add("다시 말해줘");

		rb.add(simpleResponse);
		rb.add(basicCard);

		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		return rb.build();
	}

	@ForIntent("Category-Options")
	public ActionResponse processCheckCategory(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();
		String WelcomeOptionType = CommonUtil.makeSafeString(request.getParameter("WelcomeOptionType"));

		rb.removeContext("Category-Options");
		rb.removeContext("Descript-Options");
		data.clear();

		data.put("WelcomeOptionType", WelcomeOptionType);


		CommonUtil.printMapData(data);

		return genSupport(rb);
	}

	@ForIntent("Descript-Options")
	public ActionResponse processDescriptOptions(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();

		if (!(request.getRawText().contains("제품") || request.getRawText().contains("제품설명") || request.getRawText().contains("텔레칩스제품설명") || request.getRawText().contains("텔레칩스 제품"))) {
			String DescriptOptionType = request.getSelectedOption();
			rb.removeContext("Category-Options");

			if (DescriptOptionType == null) {
				DescriptOptionType = CommonUtil.makeSafeString(request.getParameter("DescriptOptionType"));
			}

			data.clear();
			if (!DescriptOptionType.isEmpty())
				data.put("DescriptOptionType", DescriptOptionType);
		} else {
			data.clear();
			data.put("WelcomeOptionType", "Welcome2");
		}
		CommonUtil.printMapData(data);
		return genSupport(rb);
	}

	@ForIntent("telechips-mobile")
	public ActionResponse processSupportToMobile(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		List<String> suggestions = new ArrayList<String>();

		Map<String, Object> data = rb.getConversationData();

		String WelcomeOptionType = CommonUtil.makeSafeString(data.get("WelcomeOptionType"));
		String DescriptOptionType = CommonUtil.makeSafeString(data.get("DescriptOptionType"));

		CommonUtil.printMapData(data);

		String encodedUrl = null;
		List<Button> buttons = new ArrayList<>();
		Button button1;

		try {
			encodedUrl = URLEncoder.encode(
					"https://assistant.google.com/services/invoke/uid/00000005858e52ad?intent=telechips-resume&param.pa1="
							+ WelcomeOptionType + "&param.pa2=" + DescriptOptionType, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
/*
        button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://assistant.google.com/services/invoke/uid/00000005858e52ad?intent=telechips-resume&param.pa1="
                + WelcomeOptionType + "&param.pa2=" + DescriptOptionType)).setTitle("Korean Telechips info").setTitle("텔레칩스");
        buttons.add(button1);
*/
		rb.add(new SimpleResponse().setTextToSpeech("다음 QR코드를 모바일에서 읽을 경우 텔레칩스 서비스를 모바일에서 계속 하실 수 있습니다.")
				.setDisplayText("다음 QR코드를 모바일에서 읽을 경우 텔레칩스 서비스를 모바일에서 계속 하실 수 있습니다.  \n1. Google Assisatant가 가능한 휴대폰  \n  (ios - 카메라 실행 / android - QR인식 카메라모듈 실행)  \n2. QR 이미지 인식  \n3. 텔레칩스 사 화면 이어서 진행 시작 ")
		)
				.add(new BasicCard()
						//.setButtons(buttons)
						.setFormattedText("다음 QR코드를 모바일에서 읽을 경우 텔레칩스 서비스를 모바일에서 계속 하실 수 있습니다.  \n1. Google Assisatant가 가능한 휴대폰  \n  (ios - 카메라 실행 / android - QR인식 카메라모듈 실행)  \n2. QR 이미지 인식  \n3. 텔레칩스 사 화면 이어서 진행 시작 ")
						.setImage(new Image().setUrl("https://actions.o2o.kr/skylife/api/1.0/qrcode?url=" + encodedUrl)
								.setAccessibilityText("모바일 장치 연결을 위한 QR코드"))
						.setImageDisplayOptions("DEFAULT"));

		suggestions.add("텔레칩스 소개");
		suggestions.add("텔레칩스 제품");
		suggestions.add("영어로");
		suggestions.add("다시 말해줘");

		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		return rb.build();
	}

	@ForIntent("telechips-english")
	public ActionResponse processSupportToEnglish(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		List<String> suggestions = new ArrayList<String>();

		Map<String, Object> data = rb.getConversationData();

		String WelcomeOptionType = CommonUtil.makeSafeString(data.get("WelcomeOptionType"));
		String DescriptOptionType = CommonUtil.makeSafeString(data.get("DescriptOptionType"));

		CommonUtil.printMapData(data);

		String encodedUrl = null;
		List<Button> buttons = new ArrayList<>();
		Button button1;

		try {
			encodedUrl = URLEncoder.encode(
					"https://assistant.google.com/services/invoke/uid/000000b6c0857b2a?intent=telechips-resume&param.pa1="
							+ WelcomeOptionType + "&param.pa2=" + DescriptOptionType, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("https://assistant.google.com/services/invoke/uid/000000b6c0857b2a?intent=telechips-resume&param.pa1="
				+ WelcomeOptionType + "&param.pa2=" + DescriptOptionType)).setTitle("");
		buttons.add(button1);
		rb.add(new SimpleResponse().setTextToSpeech("다음 QR코드를 모바일에서 읽을 경우 텔레칩스 인포 서비스를 모바일에서 계속 하실 수 있습니다.")
				.setDisplayText("If you read the following QR codes on mobile, you can continue with Telechips info Actions on mobile."))
				.add(new BasicCard().setButtons(buttons)
						.setFormattedText("If you read the following QR codes on mobile, you can continue with Telechips info Actions on mobile.  \nThen QR code is read on mobile, you can continue with Actions on mobile.  \n1. Google Assistant-enabled mobile phone  \n  (ios - camera run / android - QR recognition camera module run)  \n2. QR image recognition  \n3. Telechips Information screen followed by progress ")
						.setImage(new Image().setUrl("https://actions.o2o.kr/skylife/api/1.0/qrcode?url=" + encodedUrl)
								.setAccessibilityText("모바일 장치 연결을 위한 QR코드"))
						.setImageDisplayOptions("DEFAULT"));

		suggestions.add("텔레칩스 소개");
		suggestions.add("텔레칩스 제품");
		suggestions.add("모바일로");
		suggestions.add("다시 말해줘");


		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		return rb.build();
	}

	@ForIntent("telechips-resume")
	public ActionResponse processSupportResume(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);

		String WelcomeOptionType = CommonUtil.makeSafeString(request.getParameter("pa1"));
		String DescriptOptionType = CommonUtil.makeSafeString(request.getParameter("pa2"));

		Map<String, Object> data = rb.getConversationData();
		data.clear();
		data.put("WelcomeOptionType", WelcomeOptionType);
		data.put("DescriptOptionType", DescriptOptionType);

		if(WelcomeOptionType.isEmpty() && DescriptOptionType.isEmpty()){
			List<String> suggestions = new ArrayList<String>();
			SimpleResponse simpleResponse = new SimpleResponse();
			BasicCard basicCard = new BasicCard();

			rb.removeContext("Descript-Options");
			rb.removeContext("Category-Options");

			data.clear();
			CommonUtil.printMapData(data);

			simpleResponse.setTextToSpeech("안녕하세요, 텔레칩스입니다. 텔레칩스 회사에대해 궁굼 하시면  텔레칩스 소개라고 말씀하시거나 텔레칩스 제품에 대해 궁금하시면 텔레칩스 제품이라고 말씀하세요.")
					.setDisplayText("안녕하세요, 텔레칩스입니다. 텔레칩스 회사에대해 궁굼 하시면  텔레칩스 소개라고 말씀하시거나 텔레칩스 제품에 대해 궁금하시면 텔레칩스 제품이라고 말씀하세요.")
			;
			basicCard
					.setFormattedText("안녕하세요, 텔레칩스입니다. 텔레칩스 회사에대해 궁굼 하시면  텔레칩스 소개라고 말씀하시거나 텔레칩스 제품에 대해 궁금하시면 텔레칩스 제품이라고 말씀하세요.")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/telechipsaiperson.gif")
							.setAccessibilityText("home"));

			suggestions.add("텔레칩스 소개");
			suggestions.add("텔레칩스 제품");
			suggestions.add("모바일로");
			suggestions.add("영어로");
			suggestions.add("다시 말해줘");
			rb.add(simpleResponse);
			rb.add(basicCard);

			rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
			return rb.build();
		}else {
			return genSupport(rb);
		}
	}

	@ForIntent("telechips-retry")
	public ActionResponse processSupportRetry(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();

		String WelcomeOptionType = CommonUtil.makeSafeString(data.get("WelcomeOptionType"));
		String DescriptOptionType = CommonUtil.makeSafeString(data.get("DescriptOptionType"));

		if (CommonUtil.isEmptyString(WelcomeOptionType)&&CommonUtil.isEmptyString(DescriptOptionType)) {
			List<String> suggestions = new ArrayList<String>();
			SimpleResponse simpleResponse = new SimpleResponse();
			BasicCard basicCard = new BasicCard();

			rb.removeContext("Descript-Options");
			rb.removeContext("Category-Options");

			data.clear();
			CommonUtil.printMapData(data);

			simpleResponse.setTextToSpeech("텔레칩스 회사에대해 궁굼 하시면  텔레칩스 소개라고 말씀하시거나 텔레칩스 제품에 대해 궁금하시면 텔레칩스 제품이라고 말씀하세요.")
					.setDisplayText("텔레칩스 회사에대해 궁굼 하시면  텔레칩스 소개라고 말씀하시거나 텔레칩스 제품에 대해 궁금하시면 텔레칩스 제품이라고 말씀하세요.")
			;
			basicCard
					.setFormattedText("텔레칩스 회사에대해 궁굼 하시면  텔레칩스 소개라고 말씀하시거나 텔레칩스 제품에 대해 궁금하시면 텔레칩스 제품이라고 말씀하세요.")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/telechipsaiperson.gif")
							.setAccessibilityText("home"));

			suggestions.add("텔레칩스 소개");
			suggestions.add("텔레칩스 제품");
			suggestions.add("모바일로");
			suggestions.add("영어로");
			suggestions.add("다시 말해줘");

			rb.add(simpleResponse);
			rb.add(basicCard);

			rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
			return rb.build();
		}else {
			return genSupport(rb);
		}
	}

	private ActionResponse genSupport(ResponseBuilder rb) {
		Map<String, Object> data = rb.getConversationData();

		String WelcomeOptionType = CommonUtil.makeSafeString(data.get("WelcomeOptionType"));
		String DescriptOptionType = CommonUtil.makeSafeString(data.get("DescriptOptionType"));
		CommonUtil.printMapData(data);

		// Solution은 0~3값을 가져야 한다.
		// int solution = CommonUtil.makeSafeInt(data.get("solution"));

		List<String> suggestions = new ArrayList<String>();
		SimpleResponse simpleResponse = new SimpleResponse();

		// ConnectionType 이 없으면 Symptom 부터


		if (CommonUtil.isEmptyString(DescriptOptionType)) {

			if (WelcomeOptionType.equalsIgnoreCase("Welcome1")) {
				List<CarouselSelectCarouselItem> items = new ArrayList<>();
				CarouselSelectCarouselItem listSelectListItem1, listSelectListItem2, listSelectListItem3, listSelectListItem4;

				SelectionCarousel selectionCarousel1 = new SelectionCarousel();

				simpleResponse.setTextToSpeech("텔레칩스는 한국 유일의 반도체 회사입니다. 더 알고 싶으시면 추천 키워드 중 선택하셔서 말씀해주시면 됩니다.")
						.setDisplayText("텔레칩스는 한국 유일의 반도체 회사입니다. 더 알고 싶으시면 추천 키워드 중 선택하셔서 말씀해주시면 됩니다.");

				List<String> synonyms1 = new ArrayList<String>();
				synonyms1.add("텔레칩스란");

				List<String> synonyms2 = new ArrayList<String>();
				synonyms2.add("매출 동향");

				List<String> synonyms3 = new ArrayList<String>();
				synonyms3.add("글로벌 네트워크");

				List<String> synonyms4 = new ArrayList<String>();
				synonyms4.add("참고");

				listSelectListItem1 = new CarouselSelectCarouselItem().setTitle("텔레칩스란").setOptionInfo(new OptionInfo().setKey("descript1").setSynonyms(synonyms1))
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript00.png").setAccessibilityText("텔레칩스란"));
				listSelectListItem2 = new CarouselSelectCarouselItem().setTitle("매출 동향").setOptionInfo(new OptionInfo().setKey("descript2").setSynonyms(synonyms2))
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript01.png").setAccessibilityText("매출 동향"));
				listSelectListItem3 = new CarouselSelectCarouselItem().setTitle("글로벌 네트워크").setOptionInfo(new OptionInfo().setKey("descript3").setSynonyms(synonyms3))
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript02.png").setAccessibilityText("글로벌 네트워크"));
				listSelectListItem4 = new CarouselSelectCarouselItem().setTitle("참고").setOptionInfo(new OptionInfo().setKey("descript4").setSynonyms(synonyms4))
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript03.png").setAccessibilityText("참고"));

				items.add(listSelectListItem1);
				items.add(listSelectListItem2);
				items.add(listSelectListItem3);
				items.add(listSelectListItem4);

				suggestions.add("텔레칩스란");
				suggestions.add("매출 동향");
				suggestions.add("글로벌 네트워크");
				suggestions.add("참고");
				suggestions.add("텔레칩스 제품");

				selectionCarousel1.setItems(items);

				rb.add(simpleResponse);
				rb.add(selectionCarousel1);

			} else if (WelcomeOptionType.equalsIgnoreCase("Welcome2")) {
				List<CarouselSelectCarouselItem> items = new ArrayList<>();
				CarouselSelectCarouselItem listSelectListItem1, listSelectListItem2, listSelectListItem3, listSelectListItem4, listSelectListItem5;
				simpleResponse.setTextToSpeech("텔레칩스에서는 현재 UHD, 풀HD 셋텁박스 개발이 가능한 칩셋을 출시하고 있고 추후 저가형 UHD, 고성능 UHD 등의 다양한 제품군을 출시해나갈 예정입니다. 아래 키워드 중에 선택해 주세요")
						.setDisplayText("텔레칩스에서는 현재 UHD, FHD STB 개발이 가능한 칩셋을 출시하고 있고 추후 저가형 UHD, 고성능 UHD 등의 다양한 제품군을 출시해나갈 예정입니다. 아래 키워드 중에 선택해 주세요.");

				SelectionCarousel selectionCarousel2 = new SelectionCarousel();

				List<String> synonyms1 = new ArrayList<String>();
				synonyms1.add("라이언");

				List<String> synonyms2 = new ArrayList<String>();
				synonyms2.add("레오");

				List<String> synonyms3 = new ArrayList<String>();
				synonyms3.add("소비량");

				List<String> synonyms4 = new ArrayList<String>();
				synonyms4.add("GPU 성능");

				List<String> synonyms5 = new ArrayList<String>();
				synonyms5.add("준비 상태");

				listSelectListItem1 = new CarouselSelectCarouselItem().setTitle("라이언").setOptionInfo(new OptionInfo().setKey("descript5").setSynonyms(synonyms1))
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript004.gif").setAccessibilityText("라이언"));
				listSelectListItem2 = new CarouselSelectCarouselItem().setTitle("레오").setOptionInfo(new OptionInfo().setKey("descript6").setSynonyms(synonyms2))
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript005.gif").setAccessibilityText("레오"));
				listSelectListItem3 = new CarouselSelectCarouselItem().setTitle("소비량").setOptionInfo(new OptionInfo().setKey("descript7").setSynonyms(synonyms3))
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript06.png").setAccessibilityText("소비량"));
				listSelectListItem4 = new CarouselSelectCarouselItem().setTitle("GPU 성능").setOptionInfo(new OptionInfo().setKey("descript8").setSynonyms(synonyms4))
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript07.png").setAccessibilityText("GPU 성능"));
				listSelectListItem5 = new CarouselSelectCarouselItem().setTitle("준비 상태").setOptionInfo(new OptionInfo().setKey("descript9").setSynonyms(synonyms5))
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript08.png").setAccessibilityText("준비 상태"));

				items.add(listSelectListItem1);
				items.add(listSelectListItem2);
				items.add(listSelectListItem3);
				items.add(listSelectListItem4);
				items.add(listSelectListItem5);

				suggestions.add("라이언");
				suggestions.add("레오");
				suggestions.add("소비량");
				suggestions.add("GPU 성능");
				suggestions.add("준비 상태");
				suggestions.add("텔레칩스 소개");

				selectionCarousel2.setItems(items);

				rb.add(simpleResponse);
				rb.add(selectionCarousel2);
			}
		} else {
			BasicCard basicCard = new BasicCard();

			if (DescriptOptionType.equalsIgnoreCase("descript1")) {
				simpleResponse.setTextToSpeech(
						"텔레칩스는 1999년 설립되었고 현재 직원 323명 중 70 퍼센트가 R 앤 D 입니다.  \n주요 제품은 어플리케이션 프로세서이며, 사업분야는 오토모티브와 소비자 시장입니다.")
						.setDisplayText("텔레칩스는 1999년 설립되었고 현재 직원 323명 중 70%가 R&D 입니다.  \n주요 제품은 application Processor이며, 사업분야는 Automotive와 Consumer 시장입니다.")
				;
				basicCard
						.setFormattedText("텔레칩스는 1999년 설립되었고 현재 직원 323명 중 70%가 R&D 입니다.  \n주요 제품은 application Processor이며, 사업분야는 Automotive와 Consumer 시장입니다.")
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript00.png")
								.setAccessibilityText("텔레칩스란")).setTitle("텔레칩스란");

				suggestions.add("매출 동향");
				suggestions.add("글로벌 네트워크");
				suggestions.add("참고");
				suggestions.add("텔레칩스 제품");

			} else if (DescriptOptionType.equalsIgnoreCase("descript2")) {
				simpleResponse.setTextToSpeech(
						"텔레칩스는 2018년 매출 1000억을 돌파하였고 2019년 1500억 달성을 예상하고 있습니다.")
						.setDisplayText("텔레칩스는 2018년 매출 1000억을 돌파하였고 2019년 1500억 달성을 예상하고 있습니다.")
				;
				basicCard
						.setFormattedText("텔레칩스는 2018년 매출 1000억을 돌파하였고 2019년 1500억 달성을 예상하고 있습니다.")
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript01.png")
								.setAccessibilityText("매출 동향")).setTitle("매출 동향");

				suggestions.add("텔레칩스란");
				suggestions.add("글로벌 네트워크");
				suggestions.add("참고");
				suggestions.add("텔레칩스 제품");

			} else if (DescriptOptionType.equalsIgnoreCase("descript3")) {
				simpleResponse.setTextToSpeech(
						"한국에 본사를 둔 텔레칩스는 유럽, 미국, 아시아 전역에 지사를 두고 글로벌 비즈니스에 적합한 환경을 제공해드리고 있습니다.")
						.setDisplayText("한국에 본사를 둔 텔레칩스는 유럽, 미국, 아시아 전역에 지사를 두고 글로벌 비즈니스에 적합한 환경을 제공해드리고 있습니다.")
				;
				basicCard
						.setFormattedText("한국에 본사를 둔 텔레칩스는 유럽, 미국, 아시아 전역에 지사를 두고 글로벌 비즈니스에 적합한 환경을 제공해드리고 있습니다.")
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript02.png")
								.setAccessibilityText("글로벌 네트워크")).setTitle("글로벌 네트워크");

				suggestions.add("텔레칩스란");
				suggestions.add("매출 동향");
				suggestions.add("참고");
				suggestions.add("텔레칩스 제품");

			} else if (DescriptOptionType.equalsIgnoreCase("descript4")) {
				simpleResponse.setTextToSpeech("텔레칩스 다양한 고객사들과 여러 과제를 해오고 있으며, 제품 출시를 위해서 최선을 다하는 회사입니다.")
						.setDisplayText("텔레칩스 다양한 고객사들과 여러 과제를 해오고 있으며, 제품 출시를 위해서 최선을 다하는 회사입니다.")
				;
				basicCard
						.setFormattedText("텔레칩스 다양한 고객사들과 여러 과제를 해오고 있으며, 제품 출시를 위해서 최선을 다하는 회사입니다.")
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript03.png")
								.setAccessibilityText("참고")).setTitle("참고");

				suggestions.add("텔레칩스란");
				suggestions.add("매출 동향");
				suggestions.add("글로벌 네트워크");
				suggestions.add("텔레칩스 제품");

			} else if (DescriptOptionType.equalsIgnoreCase("descript5")) {
				simpleResponse.setTextToSpeech("라이언은 UHD 셋텁박스를 지원하는 최신 칩셋입니다. 고성능 CPU, GPU 와 나그라, 시나미디어 등의 HW 카스를 지원하여 아이피티비, 디브이비 제품을 모두 개발할 수 있습니다.")
						.setDisplayText("Lion은 UHD STB을 지원하는 최신 칩셋입니다. 고성능 CPU, GPU와 Nagra, Synamedia 등의 HW CAS를 지원하여 IPTV, DVB 제품을 모두 개발할 수 있습니다.");
				basicCard.setTitle("라이언")
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript004.gif")
								.setAccessibilityText("라이언"));

				suggestions.add("레오");
				suggestions.add("소비량");
				suggestions.add("GPU 성능");
				suggestions.add("준비 상태");
				suggestions.add("텔레칩스 소개");

			} else if (DescriptOptionType.equalsIgnoreCase("descript6")) {
				simpleResponse.setTextToSpeech("레오는 가격 경쟁이 심한 시장을 겨냥한 저가형 UHD 칩셋입니다. 성능은 최대한 유지하고 가격을 낮춰 좀 더 다양한 고객들을 만족해줄 것입니다.")
						.setDisplayText("Leo는 가격 경쟁이 심한 시장을 겨냥한 Low cost UHD 칩셋입니다. 성능은 최대한 유지하고 가격을 낮춰 좀 더 다양한 고객들을 만족해줄 것입니다.");
				basicCard
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript005.gif")
								.setAccessibilityText("레오")).setTitle("레오");

				suggestions.add("라이언");
				suggestions.add("소비량");
				suggestions.add("GPU 성능");
				suggestions.add("준비 상태");
				suggestions.add("텔레칩스 소개");

			} else if (DescriptOptionType.equalsIgnoreCase("descript7")) {
				simpleResponse.setTextToSpeech("텔레칩스 제품들은 모두 울트라 저전력형으로 설계되어 있습니다. 경쟁사들보다 고객들의 다양한 요구를 효율적으로 지원할 수 있습니다.")
						.setDisplayText("텔레칩스 제품들은 모두 Ultra low power로 설계되어 있습니다. 경쟁사들보다 고객들의 다양한 요구를 효율적으로 지원할 수 있습니다.");
				basicCard
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript06.png")
								.setAccessibilityText("소비량")).setTitle("소비량");

				suggestions.add("라이언");
				suggestions.add("레오");
				suggestions.add("GPU 성능");
				suggestions.add("준비 상태");
				suggestions.add("텔레칩스 소개");

			} else if (DescriptOptionType.equalsIgnoreCase("descript8")) {
				simpleResponse.setTextToSpeech("텔레칩스 최신제품에 적용되어 있는 GPU는 더욱 더 강력한 기능을 제공할 것입니다. 고객사가 원하는 고성능 기능을 무리없이 구현해 줄 수 있습니다.")
						.setDisplayText("텔레칩스 최신제품에 적용되어 있는 GPU는 더욱 더 강력한 기능을 제공할 것입니다. 고객사가 원하는 고성능 기능을 무리없이 구현해 줄 수 있습니다.");
				basicCard
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript07.png")
								.setAccessibilityText("GPU 성능")).setTitle("GPU 성능");

				suggestions.add("라이언");
				suggestions.add("레오");
				suggestions.add("소비량");
				suggestions.add("준비 상태");
				suggestions.add("텔레칩스 소개");

			} else if (DescriptOptionType.equalsIgnoreCase("descript9")) {
				simpleResponse.setTextToSpeech("텔레칩스 최신제품에서 시나미디어와 나그라 카스가  준비되어 있습니다. 또한 안드로이드 최신버전을 누구보다도 빠르게 준비하고 있습니다. 텔레칩스는 구글의 조기 접속 파트너 입니다")
						.setDisplayText("텔레칩스 최신제품에서 Synamedia와 NagraCAS가  준비되어 있습니다. 또한 Android 최신버전을 누구보다도 빠르게 준비하고 있습니다. 텔레칩스는 Google의 Early access partner 입니다.");
				basicCard
						.setImage(new Image().setUrl("https://actions.o2o.kr/content/telechips/descript08.png")
								.setAccessibilityText("준비 상태")).setTitle("준비 상태");

				suggestions.add("라이언");
				suggestions.add("레오");
				suggestions.add("소비량");
				suggestions.add("GPU 성능");
				suggestions.add("텔레칩스 소개");

			}
			rb.add(simpleResponse);
			rb.add(basicCard);
		}

		suggestions.add("모바일로");
		suggestions.add("영어로");

		if(suggestions.size() <8)
			suggestions.add("다시 말해줘");


		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		return rb.build();
	}
}

