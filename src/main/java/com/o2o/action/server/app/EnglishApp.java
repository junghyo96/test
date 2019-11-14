package com.o2o.action.server.app;

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
import com.google.api.services.actions_fulfillment.v2.model.BasicCard;
import com.google.api.services.actions_fulfillment.v2.model.CarouselSelectCarouselItem;
import com.google.api.services.actions_fulfillment.v2.model.Image;
import com.google.api.services.actions_fulfillment.v2.model.OptionInfo;
import com.google.api.services.actions_fulfillment.v2.model.SimpleResponse;

public class EnglishApp extends DialogflowApp {
	public static final String SUGGEST_RESPONSE_NO = "no";
	public static final String SUGGEST_RESPONSE_YES = "yes";
	public static final String SUGGEST_RESPONSE_STOP = "stop talking";
	public static final String SUGGEST_RESPONSE_TOPIC1 = "daily routine";
	public static final String SUGGEST_RESPONSE_TOPIC2 = "personality";
	public static final String SUGGEST_RESPONSE_TOPIC3 = "free time";
	public static final String SUGGEST_RESPONSE_TOPIC4 = "pop culture";

	public List<String> setSuggestions_is(int key) {
		List<String> suggestions_is = new ArrayList<String>();

		switch (key) {
		case 1:
			suggestions_is.add(SUGGEST_RESPONSE_YES);
			suggestions_is.add(SUGGEST_RESPONSE_NO);
			suggestions_is.add(SUGGEST_RESPONSE_STOP);
			break;
		case 2:
			suggestions_is.add(SUGGEST_RESPONSE_TOPIC1);
			suggestions_is.add(SUGGEST_RESPONSE_TOPIC2);
			suggestions_is.add(SUGGEST_RESPONSE_TOPIC3);
			suggestions_is.add(SUGGEST_RESPONSE_TOPIC4);
			suggestions_is.add(SUGGEST_RESPONSE_STOP);
			break;
		default:
			suggestions_is.add(SUGGEST_RESPONSE_STOP);
			break;

		}

		return suggestions_is;
	}

	@ForIntent("English - topic")
	public ActionResponse processEnglishTopic(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();

		List<String> suggestions = new ArrayList<String>();

		Object oTopic = (Object) request.getParameter("topic");
		Object oSuggestKey = (Object) data.get("suggestkey");

		int suggestkey = 0;
		String selectTopic = null;
		selectTopic = request.getSelectedOption();

		if (selectTopic == null) {
			if (oTopic != null && oTopic instanceof String) {
				selectTopic = (String) oTopic;
				System.out.println(selectTopic);
			}
		}
		if (oSuggestKey != null && oSuggestKey instanceof String) {
			suggestkey = Integer.parseInt((String) oSuggestKey);
		}

		data.put("topicType", selectTopic);

		SimpleResponse simpleResponse = new SimpleResponse();
		BasicCard basicCard = new BasicCard();

		if (selectTopic.equals("daily routine") || data.containsValue("daily routine")) {
			simpleResponse.setTextToSpeech(
					"<speak><p><s>Well... I respect your choice.</s><s>Let's start talk about daily routine.</s><s>Did you wake up early today?</s></p></speak>");
			basicCard.setTitle("Daily routine").setFormattedText("Did you wake up early today?  \n오늘은 일찍 일어나셨나요?");
			basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
					.setAccessibilityText("daily routine image"));
			suggestkey = 1;
		} else if (selectTopic.equals("personality")) {
			simpleResponse.setTextToSpeech(
					"<speak><p><s>Well... I respect your choice.</s><s>Let's start talk about daily routine.</s><s>Did you wake up early today?</s></p></speak>");
			basicCard.setTitle("personality").setFormattedText("오늘은 일찍 일어나셨나요?");
			basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
					.setAccessibilityText("daily routine image"));
			suggestkey = 1;
		} else if (selectTopic.equalsIgnoreCase("free time")) {
			simpleResponse.setTextToSpeech(
					"<speak><p><s>Well... I respect your choice.</s><s>Let's start talk about daily routine.</s><s>Did you wake up early today?</s></p></speak>");
			basicCard.setTitle("free time").setFormattedText("오늘은 일찍 일어나셨나요?");
			basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
					.setAccessibilityText("daily routine image"));
			suggestkey = 1;
		} else if (selectTopic.equalsIgnoreCase("pop culture")) {
			simpleResponse.setTextToSpeech(
					"<speak><p><s>Well... I respect your choice.</s><s>Let's start talk about daily routine.</s><s>Did you wake up early today?</s></p></speak>");
			basicCard.setTitle("pop culture").setFormattedText("오늘은 일찍 일어나셨나요?");
			basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
					.setAccessibilityText("daily routine image"));
			suggestkey = 1;
		} else {
			simpleResponse.setTextToSpeech("<speak>Sorry, I don't understand. Please, correctly answer again!</speak>");
			basicCard.setTitle("No Topic").setFormattedText(
					"Sorry, I don't understand. Please, correctly answer again!  \n죄송해요, 이해하지 못했어요. 다시 정확하게 답변 해주세요!");
			basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
					.setAccessibilityText("daily routine image"));
			suggestkey = 1;
		}

		suggestions = setSuggestions_is(suggestkey);
		data.remove("suggestkey");
		data.put("suggestkey", suggestkey);

		rb.add(simpleResponse);
		rb.add(basicCard);
		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));

		return rb.build();
	}

	@ForIntent("English - topic - say")
	public ActionResponse englishTopicYes(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();
		// boolean userConfirmation = request.getUserConfirmation();

		String topic = null;
		Object oTopic = (Object) data.get("topicType");
		// Object oTime = (Object) request.getParameter("time");

		int sceen = 0;
		Object osceen = (Object) data.get("sceen");

		if (oTopic != null && oTopic instanceof String) {
			topic = (String) oTopic;
		}
		if (osceen != null && osceen instanceof String) {
			sceen = Integer.parseInt((String) osceen);
		}
		System.out.println(request.getRawText());
		System.out.println(topic);
		System.out.println(sceen);
		sceen++;
		return getEnglishsceentext(request, rb, topic, sceen, request.getRawText());
	}

	public ActionResponse getEnglishsceentext(ActionRequest request, ResponseBuilder rb, String topic, int sceen,
			String answer) {
		List<String> suggestions = new ArrayList<String>();
		BasicCard basicCard = new BasicCard();

		SimpleResponse simpleResponse = new SimpleResponse();
		Map<String, Object> data = rb.getConversationData();

		Object oSuggestKey = (Object) data.get("suggestkey");

		int suggestkey = 0;

		data.put("sceen", Integer.toString(sceen));

		System.out.println(topic);
		System.out.println(sceen);
		System.out.println(answer);

		if (oSuggestKey != null && oSuggestKey instanceof String) {
			suggestkey = Integer.parseInt((String) oSuggestKey);
		}

		if (topic.contains("daily routine")) {
			if (sceen == 1) {
				if (answer.contains("Yes") || answer.contains("yes") || answer.contains("early")) {
					simpleResponse.setTextToSpeech(
							"<speak><p><s>Oh really?</s><s>Don't you get up early and don't get tired?</s></p></speak>");
					basicCard.setTitle("Daily routine").setFormattedText(
							"Oh really? Don't you get up early and don't get tired?  \n정말요? 일찍 일어나서 피곤하시진 않으세요?");
					basicCard.setImage(
							new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
									.setAccessibilityText("Did you wake up early today1 image"));
				} else {
					simpleResponse.setTextToSpeech(
							"<speak><p><s>Are you sure?</s><s>Did you mind, if you overslept?</s></p></speak>");
					basicCard.setTitle("Daily routine")
							.setFormattedText("Are you sure? Did you mind, if you overslept?  \n정말인가요? 늦잠자도 괜찮으신거에요?");
					basicCard.setImage(
							new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
									.setAccessibilityText("Did you wake up early today2 image"));
				}
				rb.add(simpleResponse);
				rb.add(basicCard);
				suggestkey = 1;
			} else if (sceen == 2) {
				if (answer.contains("Yes") || answer.contains("yes") || !answer.contains("not")) {
					simpleResponse.setTextToSpeech(
							"<speak><p><s>I'm glad you're okay.</s><s>Do you like rain?</s></p></speak>");
					basicCard.setTitle("Daily routine").setFormattedText(
							"I'm glad you're okay. Do you like rain?  \n괜찮다니 다행이네요. 혹시 비 내리는거 좋아하세요?");
					basicCard.setImage(
							new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
									.setAccessibilityText("Did you wake up early today3 image"));
				} else {
					simpleResponse.setTextToSpeech(
							"<speak><p><s>I'm sorry to hear that.</s><s>Do you like rain?'</s></p></speak>");
					basicCard.setTitle("Daily routine")
							.setFormattedText("I'm sorry to hear that. Do you like rain?  \n유감이네요. 혹시 비 내리는거 좋아하세요?");
					basicCard.setImage(
							new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
									.setAccessibilityText("Did you wake up early today4 image"));
				}
				rb.add(simpleResponse);
				rb.add(basicCard);
				suggestkey = 1;
			} else if (sceen == 3) {
				if ((answer.contains("Yes") || answer.contains("yes")) || answer.equalsIgnoreCase("I like it")) {
					simpleResponse.setTextToSpeech(
							"<speak><p><s>That's great! I personally like the sound of rain drops.</s><s>Do you wear any rain boots?'</s></p></speak>");
					basicCard.setTitle("Daily routine").setFormattedText(
							"That's great! I personally like the sound of rain drops. Do you wear any rain boots?  \n멋져요! 저는 개인적으로 빗 방울소리 듣는걸 좋아해요. 혹시 장화를 신기도 하나요?");
					basicCard.setImage(
							new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
									.setAccessibilityText("Did you wake up early today5 image"));

				} else {
					simpleResponse.setTextToSpeech(
							"<speak><p><s>That's too bad.. I personally like the sound of rain drops.</s><s>Do you wear any rain boots?'</s><s><sub alias = ''>아쉽네요.. 저는 개인적으로 빗 방울소리 듣는걸 좋아해요. 혹시 장화를 신기도 하나요?</sub></s></p></speak>");
					basicCard.setTitle("Daily routine").setFormattedText(
							"That's too bad.. I personally like the sound of rain drops. Do you wear any rain boots?  \n아쉽네요.. 저는 개인적으로 빗 방울소리 듣는걸 좋아해요. 혹시 장화를 신기도 하나요?");
					basicCard.setImage(
							new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
									.setAccessibilityText("Did you wake up early today6 image"));
				}
				rb.add(simpleResponse);
				rb.add(basicCard);
				suggestkey = 1;
			} else if (sceen == 4) {
				if ((answer.contains("No") || answer.contains("no")) || answer.equalsIgnoreCase("I don't have")) {
					simpleResponse.setTextToSpeech(
							"<speak><p><s>Oh really? You want need to worry about getting wet on rainy days then.</s><s>I'm afraid this is the last time we talk.</s></p></speak>");
					basicCard.setTitle("Daily routine").setFormattedText(
							"Oh really? You want need to worry about getting wet on rainy days then.  \nI'm afraid this is the last time we talk  \n아 정말요? 비오는 날 젖을 까봐 걱정 되겠어요.  \n아쉽지만 이번 대화는 여기가 마지막이에요.");
					basicCard.setImage(
							new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
									.setAccessibilityText("Did you wake up early today7 image"));
					suggestkey = 1;
				} else {
					simpleResponse.setTextToSpeech(
							"<speak><p><s>Nice, You won't need to worry about getting wet on rainy days then.</s><s>I'm afraid this is the last time we talk.</s></p></speak>");
					basicCard.setTitle("Daily routine").setFormattedText(
							"Nice, You won't need to worry about getting wet on rainy days then.  \nI'm afraid this is the last time we talk.  \n좋네요, 비오는 날 젖는건 걱정 하지 않아도 되겠어요.  \n아쉽지만 이번 대화는 여기가 마지막이에요.");
					basicCard.setImage(
							new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
									.setAccessibilityText("Did you wake up early today8 image"));
					suggestkey = 2;
				}
				rb.add(simpleResponse);
				rb.add(basicCard);
				data.clear();
			}
		}

		suggestions = setSuggestions_is(suggestkey);
		data.remove("suggestkey");
		data.put("suggestkey", suggestkey);

		// resolution으로 유도
		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));

		return rb.build();
	}

	@ForIntent("Default Fallback Intent")
	public ActionResponse defaultFallback(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();

		Object oSuggestKey = (Object) data.get("suggestkey");
		String topic = null;
		Object oTopic = (Object) data.get("topicType");

		int sceen = 0;
		Object osceen = (Object) data.get("sceen");
		List<String> suggestions = new ArrayList<String>();

		if (oTopic != null && oTopic instanceof String) {
			topic = (String) oTopic;
		}
		if (osceen != null && osceen instanceof String) {
			sceen = Integer.parseInt((String) osceen);
		}
		int suggestkey = 0;

		if (oSuggestKey != null && oSuggestKey instanceof String) {
			suggestkey = Integer.parseInt((String) oSuggestKey);
			System.out.println(suggestkey);
		}

		rb.add("Sorry, can you say that again?");

		if (sceen == 0 && topic != null) {
			SimpleResponse simpleResponse = new SimpleResponse();
			BasicCard basicCard = new BasicCard();
			simpleResponse.setTextToSpeech("<speak><p><s>Did you wake up early today?</s></p></speak>");
			basicCard.setTitle("Daily routine").setFormattedText("Did you wake up early today?  \n오늘은 일찍 일어나셨나요?");
			basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
					.setAccessibilityText("daily routine image"));

			suggestkey = 1;

			suggestions = setSuggestions_is(suggestkey);
			data.remove("suggestkey");
			data.put("suggestkey", suggestkey);

			rb.add("Sorry, can you say that again?  \n" + simpleResponse);
			rb.add(basicCard);
			rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));

			return rb.build();

		} else if (sceen == 0 && topic == null) {
			SimpleResponse simpleResponse = new SimpleResponse();
			List<CarouselSelectCarouselItem> items = new ArrayList<>();
			CarouselSelectCarouselItem item;
			simpleResponse.setTextToSpeech(
					"Sorry, I don't understand. Please, correctly answer again!  \n죄송해요, 이해하지 못했어요. 다시 주제를 선택 해주세요!");

			item = new CarouselSelectCarouselItem().setTitle("Select Topic - Daily Routine")
					.setDescription("주제 - 일상 생활")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
							.setAccessibilityText("daily routine image"))
					.setOptionInfo(new OptionInfo().setKey("daily routine"));
			items.add(item);

			item = new CarouselSelectCarouselItem().setTitle("Select Topic - Personality").setDescription("주제 - 성격")
					.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
							.setAccessibilityText("daily routine image"))
					.setOptionInfo(new OptionInfo().setKey("personality"));
			items.add(item);

			suggestkey = 2;

			suggestions = setSuggestions_is(suggestkey);
			data.remove("suggestkey");
			data.put("suggestkey", suggestkey);

			rb.add("Sorry, can you say that again?  \n" + simpleResponse);
			rb.add(new SelectionCarousel().setItems(items));
			rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));

			return rb.build();
		} else {
			return getEnglishsceentext(request, rb, topic, sceen, request.getRawText());
		}
	}
}
