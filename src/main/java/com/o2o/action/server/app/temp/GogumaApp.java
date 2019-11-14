package com.o2o.action.server.app.temp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.actions.api.response.helperintent.SelectionCarousel;
import com.google.actions.api.response.helperintent.SignIn;
import com.google.api.services.actions_fulfillment.v2.model.BasicCard;
import com.google.api.services.actions_fulfillment.v2.model.CarouselSelectCarouselItem;
import com.google.api.services.actions_fulfillment.v2.model.Image;
import com.google.api.services.actions_fulfillment.v2.model.OptionInfo;

public class GogumaApp extends DialogflowApp {
	@ForIntent("account-test")
	public ActionResponse processAccount(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);

		return rb.add("로그인 정보를 얻을 수 있을까요?").add(new SignIn().setContext("To get your account details")).build();
	}

	@ForIntent("account-test-process")
	public ActionResponse processAccountResult(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder responseBuilder = getResponseBuilder(request);
		if (request.isSignInGranted()) {
			String token = request.getUser().getIdToken();
			responseBuilder.add("I got your account details, " + token + ". What do you want to do next?");
		} else {
			responseBuilder.add("I won't be able to save your data, but what do you want to do next?");
		}
		return responseBuilder.build();
	}

	@ForIntent("to-mobile")
	public ActionResponse processToMobile(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder responseBuilder = getResponseBuilder(request);

		Map<String, Object> storage = request.getUserStorage();
		storage.put("testkey", "Good");

		String encodedUrl = null;
		try {
			encodedUrl = URLEncoder.encode(
					"https://assistant.google.com/services/invoke/uid/0000007ec4fe9129?intent=resume.link&param.pa1=good&param.pa2=bad",
					StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		System.out.println(encodedUrl);

		responseBuilder.add("다음 QR 코드를 모바일 장치로 찍으면 됩니다.")
				.add(new BasicCard().setTitle("QR코드를 통한 모바일 링크")
						.setFormattedText("다음 QR코드를 모바일에서 읽을 경우 Actions를 모바일에서 계속 하실 수 있습니다.")
						.setImage(new Image().setUrl("https://actions.o2o.kr/csnopy/api/1.0/qrcode?url=" + encodedUrl)
								.setAccessibilityText("모바일 장치 연결을 위한 QR코드"))
						.setImageDisplayOptions("DEFAULT"));

		return responseBuilder.build();
	}

	@ForIntent("resume.link")
	public ActionResponse processResumeLink(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder responseBuilder = getResponseBuilder(request);

		Map<String, Object> storage = request.getUserStorage();
		System.out.println(storage.get("testkey"));

		responseBuilder.add("계속 다시 하면 될듯 합니다.");
		return responseBuilder.build();
	}

	@ForIntent("support-find.symptom")
	public ActionResponse processFindSymptom(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();
		data.remove("sceen");
		data.put("sceen", "support-find.symptom");

		String symptom = null;
		Object oSymptom = (Object) request.getParameter("symptom");

		System.out.println("oSymptom : [" + oSymptom + "]");

		// 증상 파악을 위해서 단계별로 접근 할 수 있어야 한다.
		// 화면이 들어올 경우 깜박임까지 합쳐서 다음 단계로 넘어 가야 한다.
		if (oSymptom != null && oSymptom instanceof String) {
			symptom = (String) oSymptom;
		}

		return genFindSymptom(rb, symptom);
	}

	private ActionResponse genFindSymptom(ResponseBuilder rb, String symptom) {
		List<String> suggestions = new ArrayList<String>();
		List<CarouselSelectCarouselItem> items = new ArrayList<>();
		if (symptom == null || symptom.length() <= 0) {

			rb.add("화면이 어떻게 이상한가요?");

			BasicCard basicCard = new BasicCard();

			basicCard.setTitle("Skylife 서비스센터 AI 상담원").setFormattedText("화면이 어떻게 이상하신지 말씀해주세요.");
			basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
					.setAccessibilityText("Skylife 서비스센터 AI 상담원 이미지"));

			rb.add(basicCard);

		} else {
			Map<String, Object> data = rb.getConversationData();
			// data.clear();
			data.put("symptom", symptom);

			// check status로 유도
			if (symptom.equalsIgnoreCase("sym1")) {

				rb.add("TV 케이블 연결은 어떻게 되어 있나요?");

				CarouselSelectCarouselItem item1, item2;

				List<String> synonyms1 = new ArrayList<String>();
				synonyms1.add("컴포넌트");

				item1 = new CarouselSelectCarouselItem().setTitle("컴포넌트").setDescription("컴포넌트 케이블")
						.setOptionInfo(new OptionInfo().setKey("con2").setSynonyms(synonyms1));
				item1.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/cablecomponent.jpg")
						.setAccessibilityText("컴포넌트케이블이미지"));
				items.add(item1);

				List<String> synonyms2 = new ArrayList<String>();
				synonyms2.add("HDMI");

				item2 = new CarouselSelectCarouselItem().setTitle("HDMI").setDescription("HDMI 케이블")
						.setOptionInfo(new OptionInfo().setKey("con1").setSynonyms(synonyms2));

				item2.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/cablehdmi.jpg")
						.setAccessibilityText("hdmi케이블이미지"));

				items.add(item2);

				rb.add(new SelectionCarousel().setItems(items));


			}

			if (symptom.equalsIgnoreCase("sym2")) {

			}
		}

		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));

		return rb.build();
	}

}
