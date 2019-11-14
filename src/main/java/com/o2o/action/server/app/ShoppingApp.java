package com.o2o.action.server.app;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.o2o.action.server.DBInit;
import com.o2o.action.server.db.Category;
import com.o2o.action.server.db.Category.Type;
import com.o2o.action.server.db.Channel;
import com.o2o.action.server.db.Schedule;
import com.o2o.action.server.repo.CategoryRepository;
import com.o2o.action.server.repo.ChannelRepository;
import com.o2o.action.server.repo.ScheduleRepository;
import com.o2o.action.server.util.CommonUtil;
import com.o2o.action.server.util.CommonWord;

public class ShoppingApp extends DialogflowApp {
	private CategoryRepository categoryRepository;
	private ChannelRepository channelRepository;
	private ScheduleRepository scheduleRepository;

	public void setCategoryRepository(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	public void setChannelRepository(ChannelRepository channelRepository) {
		this.channelRepository = channelRepository;
	}

	public void setScheduleRepository(ScheduleRepository scheduleRepository) {
		this.scheduleRepository = scheduleRepository;
	}

	@ForIntent("Find - channel.schedule")
	public ActionResponse processFindSchedule(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		List<String> suggestions = new ArrayList<String>();

		String channelName = CommonUtil.makeSafeString(request.getParameter("bcast-news-name"));

		System.out.println("bcast-news-name : " + channelName);

		List<Channel> channels = channelRepository.findByChName(channelName);
		if (channels != null && channels.size() > 0) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
			Calendar c = Calendar.getInstance();
			c.add(Calendar.HOUR, 9);
			String tmpStr = formatter.format(c.getTime()) + "00";
			long curTime = Long.parseLong(tmpStr);

			List<Schedule> schedules = scheduleRepository
					.findByChannelAndStartTimeLessThanEqualAndEndTimeGreaterThan(channels.get(0), curTime, curTime);
			if (schedules != null && schedules.size() > 0) {
				Schedule current = schedules.get(0);

				rb.add("현재 " + channelName + "에서는 " + current.getName() + "(가)이 방송 중입니다.");

				String desc = current.getDescription();
				if (desc == null || desc.length() == 0) {
					desc = "상세 정보 없음.";
				}

				BasicCard basicCard = new BasicCard().setTitle(channelName + " - " + current.getName())
						.setFormattedText(desc);
				rb.add(basicCard);

			} else {
				rb.add(channelName + " 채널의 방송 정보를 가져올 수 없습니다.");
			}
		} else {
			rb.add("요청한 " + channelName + " 채널의 정보를 가져올 수 없습니다.");
		}

		suggestions.add(CommonWord.SUGGEST_SEARCH_SHOPPINGMALL_FIND);
		suggestions.add(CommonWord.SUGGEST_SEARCH_PRODUCT_FIND);

		if (suggestions.size() > 0)
			rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));

		return rb.build();
	}

	@ForIntent("Find - product")
	public ActionResponse processFindProduct(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		List<String> suggestions = new ArrayList<String>();

		String searchWord = CommonUtil.makeSafeString(request.getRawText());

		System.out.println("before : " + searchWord);
		searchWord = searchWord.replace("찾아줘", "");
		searchWord = searchWord.replace("검색해줘", "");
		searchWord = searchWord.trim();
		System.out.println("after : " + searchWord);

		String[] words = searchWord.split(" ");

		if (words.length > 0) {
			List<Category> categories = new ArrayList<Category>();
			for (String word : words) {
				List<Category> tmpCategories = categoryRepository.findByTitleContainingAndCatType(word, Type.ITEM);
				if (tmpCategories != null && tmpCategories.size() > 0) {
					for (Category category : tmpCategories) {
						if (!categories.contains(category)) {
							categories.add(category);
						}
					}
				}
			}

			if (categories.size() > 0) {
				processCategories(rb, suggestions, searchWord + "로 검색한 결과 입니다.", categories);
			} else {
				rb.add("요청한 [" + searchWord + "] 관련 정보를 가져올 수 없습니다.");
			}

		}

		return rb.build();
	}

	@ForIntent("Shopping")
	public ActionResponse processShop(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder responseBuilder = getResponseBuilder(request);
		List<String> suggestions = new ArrayList<String>();

		String shopWord = CommonUtil.makeSafeString(request.getParameter("shop-name"));

		if (!CommonUtil.isEmptyString(shopWord)) {
			processMidCategory(responseBuilder, suggestions, shopWord);
		} else {
			Category root = null;
			List<Category> roots = categoryRepository.findByKeycodeOrderByDispOrderAsc(DBInit.KEYCODE_SHOPPING_ROOT);

			if (roots != null && roots.size() > 0) {
				root = roots.get(0);
				System.out.println(root.getId() + "," + root.getKeycode());
			}
			if (root != null)
				processRootCategories(responseBuilder, suggestions, root);
			else
				processError(responseBuilder, suggestions, "쇼핑 정보를 읽어올 수 없습니다.");
		}

		return responseBuilder.build();
	}

	@ForIntent("Category - option")
	public ActionResponse processCategoryOption(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder responseBuilder = getResponseBuilder(request);
		List<String> suggestions = new ArrayList<String>();
		Map<String, Object> data = request.getConversationData();
		Object oSelectitem = (Object) data.get("selectitem");

		long parentId = 0;
		String selectedItem = request.getSelectedOption();

		if (selectedItem != null) {
			data.put("selectitem", selectedItem);
			parentId = Long.parseLong(selectedItem);
		} else {
			selectedItem = (String) oSelectitem;
			parentId = Long.parseLong(selectedItem);
		}

		System.out.println("selectedItem : " + parentId);

		Optional<Category> current = categoryRepository.findById(parentId);

		if (current.isPresent()) {
			Category parent = current.get();
			data.put("currentItem", Long.toString(parentId));

			if (parent.getChildren() == null || parent.getChildren().size() <= 1) {
				processLeafCategory(responseBuilder, suggestions, parent);
			} else {
				processRootCategories(responseBuilder, suggestions, parent);
			}
		} else {
			processError(responseBuilder, suggestions, "해당 내용을 찾을 수 없습니다.");
		}

		return responseBuilder.build();
	}

	@ForIntent("Category - option.connect")
	public ActionResponse processCategoryOptionConnect(ActionRequest request)
			throws ExecutionException, InterruptedException {
		ResponseBuilder responseBuilder = getResponseBuilder(request);
		List<String> suggestions = new ArrayList<String>();

		long parentId = 0;
		Map<String, Object> data = request.getConversationData();

		Object tmp = data.get("currentItem");

		if (tmp != null && tmp instanceof String) {
			parentId = Long.parseLong((String) tmp);
		}

		if (parentId == 0) {
			// DefaultFalback(request);
			processError(responseBuilder, suggestions, "기본 정보가 없습니다.");
		} else {
			Optional<Category> current = categoryRepository.findById(parentId);
			if (current.isPresent()) {
				Category parent = current.get();
				processCategoryQRCode(responseBuilder, suggestions, parent);
			} else {
				// DefaultFalback(request);
				processError(responseBuilder, suggestions, "기본 정보를 불러 올 수 없습니다.");
			}
		}

		System.out.println("selectedItem : " + parentId);

		return responseBuilder.build();
	}

	private void processError(ResponseBuilder builder, List<String> suggestions, String errorMessage) {
		builder.add("내부 오류 입니다." + errorMessage);
		builder.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
	}

	private void processRootCategories(ResponseBuilder builder, List<String> suggestions, Category parent) {
		processCategories(builder, suggestions, parent.getSpeach(), parent.getChildren());
	}

	private void processCategories(ResponseBuilder builder, List<String> suggestions, String speach,
			List<Category> categories) {
		List<CarouselSelectCarouselItem> items = new ArrayList<>();
		CarouselSelectCarouselItem item;

		if (categories == null || categories.size() == 0) {
			System.out.println(categories);
			processError(builder, suggestions, "데이터를 조회 할 수 없습니다.");
			return;
		} else if (categories.size() == 1) {
			processLeafCategory(builder, suggestions, categories.get(0));
		} else {
			int count = 0;
			for (Category category : categories) {
				List<String> synonyms = new ArrayList<String>();
				if (category.getSynonyms() != null) {
					String[] tmpStrs = category.getSynonyms().split(";");
					for (String tmp : tmpStrs) {
						synonyms.add(tmp);
					}
				}
				item = new CarouselSelectCarouselItem().setTitle(category.getTitle())
						.setDescription(category.getDescription())
						.setOptionInfo(new OptionInfo().setKey(Long.toString(category.getId())).setSynonyms(synonyms));
				if (!CommonUtil.isEmptyString(category.getImagePath())) {
					item.setImage(new Image().setUrl(category.getImagePath())
							.setAccessibilityText(category.getImageAltText()));
				}
				items.add(item);

				if (count > 8)
					break;
				count++;

			}

			suggestions.add(CommonWord.SUGGEST_SEARCH_SHOPPINGMALL_FIND);
			suggestions.add(CommonWord.SUGGEST_SEARCH_PRODUCT_FIND);
			suggestions.add(CommonWord.SUGGEST_SEARCH_CHANNEL_FIND);

			if (suggestions.size() > 0)
				builder.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
			if (speach == null)
				builder.add("선택해주십시요.");
			else
				builder.add(speach);

			builder.add(new SelectionCarousel().setItems(items));
		}

	}

	private void processLeafCategory(ResponseBuilder builder, List<String> suggestions, Category category) {
		switch (category.getCatType()) {
		case ITEM:
		case CATEGORY:
			if (category.getSpeach() == null)
				builder.add(category.getTitle());
			else
				builder.add(category.getSpeach());
			BasicCard basicCard = new BasicCard().setTitle(category.getTitle())
					.setFormattedText(category.getDescription());
			if (category.getImagePath() != null)
				basicCard.setImage(new Image().setUrl(category.getImagePath())
						.setAccessibilityText(category.getImageAltText() == null ? "이미지" : category.getImageAltText()));
			builder.add(basicCard);

			if (category.getDetail() != null && category.getDetail().getLinkURL() != null
					&& category.getDetail().getLinkURL().length() > 0) {
				Map<String, Object> data = builder.getConversationData();
				data.put("currentItem", Long.toString(category.getId()));

				suggestions.add(CommonWord.SUGGEST_TO_MOBILE);
			}

			break;
		default:
			builder.add(category.getTitle() + "," + category.getDescription());
			break;
		}

		suggestions.add(CommonWord.SUGGEST_SEARCH_SHOPPINGMALL_FIND);
		suggestions.add(CommonWord.SUGGEST_SEARCH_PRODUCT_FIND);
		suggestions.add(CommonWord.SUGGEST_SEARCH_CHANNEL_FIND);

		if (suggestions.size() > 0)
			builder.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
	}

	private void processCategoryQRCode(ResponseBuilder builder, List<String> suggestions, Category category) {
		if (category != null && category.getDetail() != null && category.getDetail().getLinkURL() != null) {
			String encodedUrl = null;
			try {
				encodedUrl = URLEncoder.encode("https://actions.o2o.kr/skylife/product?id=" + category.getId(),
						StandardCharsets.UTF_8.toString()); // category.getDetail().getLinkURL(),
				// StandardCharsets.UTF_8.toString()
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			builder.add("다음 QR 코드를 모바일 장치로 찍으면 됩니다.").add(new BasicCard().setTitle(category.getTitle())
					.setFormattedText(category.getDescription())
					.setImage(new Image().setUrl("https://actions.o2o.kr/skylife/api/1.0/qrcode?url=" + encodedUrl + "&"
							+ System.currentTimeMillis()).setAccessibilityText("모바일 장치 연결을 위한 QR코드"))
					.setImageDisplayOptions("DEFAULT"));

			suggestions.add(CommonWord.SUGGEST_SEARCH_SHOPPINGMALL_FIND);
			suggestions.add(CommonWord.SUGGEST_SEARCH_PRODUCT_FIND);
			suggestions.add(CommonWord.SUGGEST_SEARCH_CHANNEL_FIND);

			if (suggestions.size() > 0)
				builder.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
		} else {
			processError(builder, suggestions, "내부 오류 입니다. 링크 정보가 없는 아이템 입니다.");
		}
	}

	private void processMidCategory(ResponseBuilder builder, List<String> suggestions, String keycord) {
		Category root = null;
		List<Category> roots = categoryRepository.findByKeycodeOrderByDispOrderAsc(keycord);

		if (roots != null && roots.size() > 0) {
			root = roots.get(0);
			System.out.println(root.getId() + "," + root.getKeycode());
		}

		if (root != null) {
			if (root.getCatType().equals(Category.Type.ITEM)) {
				processLeafCategory(builder, suggestions, root);
			} else {
				processRootCategories(builder, suggestions, root);
			}
		} else
			processError(builder, suggestions, "정보를 읽어올 수 없습니다.");
	}

	@ForIntent("Default Fallback Intent")
	public ActionResponse defaultFallback(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);

		List<String> suggestions = new ArrayList<String>();

		rb.add("죄송합니다. 무슨 말씀을 하시는지 못알아 들었어요.");

		suggestions.add(CommonWord.SUGGEST_SEARCH_SHOPPINGMALL_FIND);
		suggestions.add(CommonWord.SUGGEST_SEARCH_PRODUCT_FIND);
		suggestions.add(CommonWord.SUGGEST_SEARCH_CHANNEL_FIND);

		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));

		return rb.build();
	}

	@ForIntent("Default Welcome Intent")
	public ActionResponse defaultWelcome(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);

		List<String> suggestions = new ArrayList<String>();

		rb.add("안녕하세요, 스카이 검색입니다. 무엇을 도와드릴까요?");

		suggestions.add(CommonWord.SUGGEST_SEARCH_SHOPPINGMALL_FIND);
		suggestions.add(CommonWord.SUGGEST_SEARCH_PRODUCT_FIND);
		suggestions.add(CommonWord.SUGGEST_SEARCH_CHANNEL_FIND);

		rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));

		return rb.build();
	}
}