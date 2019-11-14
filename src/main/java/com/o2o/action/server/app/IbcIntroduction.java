package com.o2o.action.server.app;

import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.api.services.actions_fulfillment.v2.model.BasicCard;
import com.google.api.services.actions_fulfillment.v2.model.Button;
import com.google.api.services.actions_fulfillment.v2.model.CarouselBrowseItem;
import com.google.api.services.actions_fulfillment.v2.model.Image;
import com.google.api.services.actions_fulfillment.v2.model.OpenUrlAction;
import com.google.api.services.actions_fulfillment.v2.model.SimpleResponse;
import com.google.actions.api.response.helperintent.SelectionCarousel;
import com.google.api.services.actions_fulfillment.v2.model.OptionInfo;
import com.google.api.services.actions_fulfillment.v2.model.CarouselSelectCarouselItem;
import com.o2o.action.server.util.CommonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class IbcIntroduction extends DialogflowApp {
	@ForIntent("Default Fallback Intent")
	public ActionResponse defaultFallback(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		SimpleResponse simpleResponse = new SimpleResponse();
		BasicCard basicCard = new BasicCard();

		List<String> suggestions = new ArrayList<String>();
		Map<String, Object> data = rb.getConversationData();

		CommonUtil.printMapData(data);
		List<CarouselSelectCarouselItem> items = new ArrayList<>();
		CarouselSelectCarouselItem listSelectListItem1, listSelectListItem2, listSelectListItem3, listSelectListItem4, listSelectListItem5, listSelectListItem6, listSelectListItem7;

		SelectionCarousel selectionCarousel1 = new SelectionCarousel();

		simpleResponse.setTextToSpeech("Please say again. Here are the most important companies in IBC 2019")
				.setDisplayText("Please say again. Here are the most important companies in IBC 2019");

		List<String> synonyms1 = new ArrayList<String>();
		synonyms1.add("O2O");

		List<String> synonyms2 = new ArrayList<String>();
		synonyms2.add("Google");

		List<String> synonyms3 = new ArrayList<String>();
		synonyms3.add("Telechips");

		List<String> synonyms4 = new ArrayList<String>();
		synonyms4.add("UEI");

		List<String> synonyms5 = new ArrayList<String>();
		synonyms5.add("Skyworth");

		List<String> synonyms6 = new ArrayList<String>();
		synonyms6.add("SEI Robotics");

		List<String> synonyms7 = new ArrayList<String>();
		synonyms7.add("ASKEY");

		listSelectListItem1 = new CarouselSelectCarouselItem().setTitle("O2O").setOptionInfo(new OptionInfo().setKey("item1").setSynonyms(synonyms1))
				.setImage(new Image().setUrl("http://drive.google.com/uc?export=view&id=11-ucltPfW96zoewZkpgvix4Duu4r2feY").setAccessibilityText("O2O"));
		listSelectListItem2 = new CarouselSelectCarouselItem().setTitle("Google").setOptionInfo(new OptionInfo().setKey("item2").setSynonyms(synonyms2))
				.setImage(new Image().setUrl("http://drive.google.com/uc?export=view&id=1VOxNo-S-8XMpbn1onEpxaxSLTAIE6w7w").setAccessibilityText("Google"));
		listSelectListItem3 = new CarouselSelectCarouselItem().setTitle("Telechips").setOptionInfo(new OptionInfo().setKey("item3").setSynonyms(synonyms3))
				.setImage(new Image().setUrl("http://drive.google.com/uc?export=view&id=1p8ygow5zmi8QcA1s0ryLBEVP2HmoJJlA").setAccessibilityText("Telechips"));
		listSelectListItem4 = new CarouselSelectCarouselItem().setTitle("UEI").setOptionInfo(new OptionInfo().setKey("item4").setSynonyms(synonyms4))
				.setImage(new Image().setUrl("http://drive.google.com/uc?export=view&id=1bkEjZvi1q-8OsAsxq72c46KnwDTQv1yf").setAccessibilityText("UEI"));
		listSelectListItem6 = new CarouselSelectCarouselItem().setTitle("SEI Robotics").setOptionInfo(new OptionInfo().setKey("item6").setSynonyms(synonyms6))
				.setImage(new Image().setUrl("http://drive.google.com/uc?export=view&id=1UAF5C1eAPdM6M3SIZJQQ_0KqBlVPWFik").setAccessibilityText("SEI Robotics"));
		listSelectListItem7 = new CarouselSelectCarouselItem().setTitle("ASKEY").setOptionInfo(new OptionInfo().setKey("item7").setSynonyms(synonyms7))
				.setImage(new Image().setUrl("http://drive.google.com/uc?export=view&id=1ovidrma3z3edu7kf_bYkWMldyyo7sI2R").setAccessibilityText("ASKEY"));
		listSelectListItem5 = new CarouselSelectCarouselItem().setTitle("Skyworth").setOptionInfo(new OptionInfo().setKey("item5").setSynonyms(synonyms5))
				.setImage(new Image().setUrl("http://drive.google.com/uc?export=view&id=1wBEvJ7aT6aYOsbQ_pThPEBmaWIywYZfW").setAccessibilityText("Skyworth"));

		items.add(listSelectListItem1);
		items.add(listSelectListItem2);
		items.add(listSelectListItem3);
		items.add(listSelectListItem4);
		items.add(listSelectListItem5);
		items.add(listSelectListItem6);
		items.add(listSelectListItem7);

		suggestions.add("Home");
		suggestions.add("O2O");
		suggestions.add("Google");
		suggestions.add("Telechips");
		suggestions.add("UEI");
		suggestions.add("Skyworth");
		suggestions.add("SEI Robotics");
		suggestions.add("ASKEY");

		selectionCarousel1.setItems(items);

		rb.add(simpleResponse);
		rb.add(selectionCarousel1);

		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		return rb.build();
	}
	@ForIntent("welcomeOption")
	public ActionResponse processCheckStatus(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();

		String itemOptions = request.getSelectedOption();
		data.clear();

		if (itemOptions == null)
			itemOptions = CommonUtil.makeSafeString(request.getParameter("itemOptions"));

		if (!itemOptions.isEmpty())
			data.put("itemOptions", itemOptions);

		CommonUtil.printMapData(data);
		return genSupport(rb);
	}

	private ActionResponse genSupport(ResponseBuilder rb) {
		Map<String, Object> data = rb.getConversationData();

		String itemOptions = CommonUtil.makeSafeString(data.get("itemOptions"));

		// Solution은 0~3값을 가져야 한다.
		// int solution = CommonUtil.makeSafeInt(data.get("solution"));


		CommonUtil.printMapData(data);

		List<String> suggestions = new ArrayList<String>();
		SimpleResponse simpleResponse = new SimpleResponse();

		// ConnectionType 이 없으면 Symptom 부터

		BasicCard basicCard = new BasicCard();
		CarouselBrowseItem carouselBrowseItem1, carouselBrowseItem2, carouselBrowseItem3;
		List<CarouselBrowseItem> items = new ArrayList<>();
		List<Button> buttons = new ArrayList<>();
		Button button1;


		switch (itemOptions) {
			case "item1":

				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("http://drive.google.com/uc?export=view&id=18e7hSL8c-0ttIBr8guhYyUH31z8HgCNA")).setTitle("Location in Detail");
				buttons.add(button1);

				simpleResponse.setTextToSpeech("O2O booth located in hall number 2,  C27 area. If you want to search another company, press the home button.").setDisplayText("O2O booth located in hall number 2,  C27 area.  \nIf you want to search another company, press the home button.");
				basicCard.setFormattedText("O2O in hall 2");
				basicCard
						.setImage(new Image().setUrl("http://drive.google.com/uc?export=view&id=18e7hSL8c-0ttIBr8guhYyUH31z8HgCNA")
								.setAccessibilityText("item1")).setImageDisplayOptions("DEFAULT").setButtons(buttons);
				rb.add(simpleResponse);
				rb.add(basicCard);
				suggestions.add("Home");
				suggestions.add("Google");
				suggestions.add("Telechips");
				suggestions.add("UEI");
				suggestions.add("Skyworth");
				suggestions.add("SEI Robotics");
				suggestions.add("ASKEY");
				break;
			case "item2":

				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("http://drive.google.com/uc?export=view&id=1V5UNLjraJ5pOrx-iWTW82lXJ_vy7pKPR")).setTitle("Location in Detail");
				buttons.add(button1);

				simpleResponse.setTextToSpeech(
						"Google booth located in hall number 14,  E01 area. If you want to search another company, press the home button.").setDisplayText("Google booth located in hall number 14,  E01 area.  \nIf you want to search another company, press the home button.");
				basicCard.setFormattedText("Google in Hall 14");
				basicCard
						.setImage(new Image().setUrl("http://drive.google.com/uc?export=view&id=1V5UNLjraJ5pOrx-iWTW82lXJ_vy7pKPR")
								.setAccessibilityText("item2")).setImageDisplayOptions("DEFAULT").setButtons(buttons);
				rb.add(simpleResponse);
				rb.add(basicCard);
				suggestions.add("Home");
				suggestions.add("O2O");
				suggestions.add("Telechips");
				suggestions.add("UEI");
				suggestions.add("Skyworth");
				suggestions.add("SEI Robotics");
				suggestions.add("ASKEY");
				break;
			case "item3":
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("http://drive.google.com/uc?export=view&id=18e7hSL8c-0ttIBr8guhYyUH31z8HgCNA")).setTitle("Location in Detail");
				buttons.add(button1);

				simpleResponse.setTextToSpeech("Telechips booth located in hall number 2,  C27 area. If you want to search another company, press the home button.").setDisplayText("Telechips booth located in hall number 2,  C27 area.  \nIf you want to search another company, press the home button.");
				basicCard.setFormattedText("Telechips in Hall 2");
				basicCard
						.setImage(new Image().setUrl("http://drive.google.com/uc?export=view&id=18e7hSL8c-0ttIBr8guhYyUH31z8HgCNA")
								.setAccessibilityText("item3")).setImageDisplayOptions("DEFAULT").setButtons(buttons);
				rb.add(simpleResponse);
				rb.add(basicCard);
				suggestions.add("Home");
				suggestions.add("O2O");
				suggestions.add("Google");
				suggestions.add("UEI");
				suggestions.add("Skyworth");
				suggestions.add("SEI Robotics");
				suggestions.add("ASKEY");
				break;
			case "item4":
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("http://drive.google.com/uc?export=view&id=185pw40GzFwO0T5InnYCMMnnc3cmCATdC")).setTitle("Location in Detail");
				buttons.add(button1);

				simpleResponse.setTextToSpeech("Universal Electronics booth located in hall number 1,  C41 area. If you want to search another company, press the home button.")
						.setDisplayText("Universal Electronics booth located in hall number 1,  C41 area.  \nIf you want to search another company, press the home button.");
				basicCard.setFormattedText("Universal Electronics in hall 14");
				basicCard
						.setImage(new Image().setUrl("http://drive.google.com/uc?export=view&id=185pw40GzFwO0T5InnYCMMnnc3cmCATdC")
								.setAccessibilityText("item4")).setImageDisplayOptions("DEFAULT").setButtons(buttons);
				rb.add(simpleResponse);
				rb.add(basicCard);
				suggestions.add("Home");
				suggestions.add("O2O");
				suggestions.add("Google");
				suggestions.add("Telechips");
				suggestions.add("Skyworth");
				suggestions.add("SEI Robotics");
				suggestions.add("ASKEY");
				break;
			case "item5":
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("http://drive.google.com/uc?export=view&id=1bAOUqwiYN2P7rD6ilzn7S6wrc682nfUk")).setTitle("Location in Detail");
				buttons.add(button1);

				simpleResponse.setTextToSpeech("Skyworth Digital Technology booth located in hall number 1,  D15 area. If you want to search another company, press the home button.")
						.setDisplayText("Skyworth Digital Technology booth located in hall number 1,  D15 area.  \nIf you want to search another company, press the home button.");
				basicCard.setFormattedText("Skyworth in hall 1");
				basicCard
						.setImage(new Image().setUrl("http://drive.google.com/uc?export=view&id=1bAOUqwiYN2P7rD6ilzn7S6wrc682nfUk")
								.setAccessibilityText("item5")).setImageDisplayOptions("DEFAULT").setButtons(buttons);
				rb.add(simpleResponse);
				rb.add(basicCard);
				suggestions.add("Home");
				suggestions.add("O2O");
				suggestions.add("Google");
				suggestions.add("Telechips");
				suggestions.add("UEI");
				suggestions.add("SEI Robotics");
				suggestions.add("ASKEY");
				break;
			case "item6":
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("http://drive.google.com/uc?export=view&id=1dcxLAyjCdJvFLCEK6g6yXmY1qCsmF4Xz")).setTitle("Location in Detail");
				buttons.add(button1);

				simpleResponse.setTextToSpeech("SEI Robotics booth located in hall number 14,  E18 area.  If you want to search another company, press the home button.")
						.setDisplayText("SEI Robotics booth located in hall number 14,  E18 area.  \nIf you want to search another company, press the home button.");
				basicCard.setFormattedText("SEI Robotics in hall 14");
				basicCard
						.setImage(new Image().setUrl("http://drive.google.com/uc?export=view&id=1dcxLAyjCdJvFLCEK6g6yXmY1qCsmF4Xz")
								.setAccessibilityText("item6")).setImageDisplayOptions("DEFAULT").setButtons(buttons);
				rb.add(simpleResponse);
				rb.add(basicCard);
				suggestions.add("Home");
				suggestions.add("O2O");
				suggestions.add("Google");
				suggestions.add("Telechips");
				suggestions.add("UEI");
				suggestions.add("Skyworth");
				suggestions.add("ASKEY");
				break;
			case "item7":
				button1 = new Button().setOpenUrlAction(new OpenUrlAction().setUrl("http://drive.google.com/uc?export=view&id=18x91IeCFwPF7nfUVT4ehv3pAYU4BpghS")).setTitle("Location in Detail");
				buttons.add(button1);

				simpleResponse.setTextToSpeech("Askey Computer booth located in hall number 1,  F07 area. If you want to search another company, press the home button.")
						.setDisplayText("Askey Computer booth located in hall number 1,  F07 area.  \nIf you want to search another company, press the home button.");
				basicCard.setFormattedText("Askey in hall 1");
				basicCard
						.setImage(new Image().setUrl("http://drive.google.com/uc?export=view&id=18x91IeCFwPF7nfUVT4ehv3pAYU4BpghS")
								.setAccessibilityText("item7")).setImageDisplayOptions("DEFAULT").setButtons(buttons);
				rb.add(simpleResponse);
				rb.add(basicCard);
				suggestions.add("Home");
				suggestions.add("O2O");
				suggestions.add("Google");
				suggestions.add("Telechips");
				suggestions.add("UEI");
				suggestions.add("Skyworth");
				suggestions.add("SEI Robotics");
				break;
		}
		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		return rb.build();
	}

}
