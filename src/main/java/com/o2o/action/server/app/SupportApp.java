package com.o2o.action.server.app;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.actions.api.ActionContext;
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
import com.o2o.action.server.util.CommonUtil;
import com.o2o.action.server.util.CommonWord;

public class SupportApp extends DialogflowApp {
    @ForIntent("support-find.symptom")
    public ActionResponse processFindSymptom(ActionRequest request) throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);
        String symptom = CommonUtil.makeSafeString(request.getParameter("symptom"));

        rb.removeContext("guide-find-option");
        rb.removeContext("guide-find");
        Map<String, Object> data = rb.getConversationData();
        data.clear();
        data.put("symptom", symptom);

        CommonUtil.printMapData(data);

        return genSupport(rb);
    }

    @ForIntent("support-check.status")
    public ActionResponse processCheckStatus(ActionRequest request) throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);
        Map<String, Object> data = rb.getConversationData();

        String connectionType = request.getSelectedOption();


        if(connectionType == null)
            CommonUtil.makeSafeString(request.getParameter("connectionType"));


        data.put("connectionType", connectionType);
        data.put("solution", "1");

        return genSupport(rb);
    }

    @ForIntent("support-resolution.notwork")
    public ActionResponse processResolutionNotwork(ActionRequest request)
            throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);

        Map<String, Object> data = rb.getConversationData();
        int solution = CommonUtil.makeSafeInt(data.get("solution"));

        solution++;
        if (solution > 3 || solution <= 0) {
            solution = 0;
        }
        data.put("solution", Integer.toString(solution));

        return genSupport(rb);
    }

    private ActionResponse genSupport(ResponseBuilder rb) {
        Map<String, Object> data = rb.getConversationData();

        String symptom = CommonUtil.makeSafeString(data.get("symptom"));
        String connectionType = CommonUtil.makeSafeString(data.get("connectionType"));
        // Solution은 0~3값을 가져야 한다.
        int solution = CommonUtil.makeSafeInt(data.get("solution"));

        CommonUtil.printMapData(data);

        List<String> suggestions = new ArrayList<String>();
        SimpleResponse simpleResponse = new SimpleResponse();

        // ConnectionType 이 없으면 Symptom 부터
        if (CommonUtil.isEmptyString(connectionType)) {
            List<CarouselSelectCarouselItem> items = new ArrayList<>();
            BasicCard basicCard = new BasicCard();
            if (CommonUtil.isEmptyString(symptom)) {


                simpleResponse.setTextToSpeech(
                        "<speak><sub alias=''>현재 화면 상태를 추천 키워드 중 선택하셔서  말씀해 주시면 됩니다.</sub><audio src='https://actions.o2o.kr/content/servicecenter/aispeak1.mp3'></audio></speak>");
                basicCard.setFormattedText("  \n현재 화면 상태를 추천 키워드 중 선택하셔서  말씀해 주시면 됩니다.");
                basicCard
                        .setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
                                .setAccessibilityText("Skylife 서비스센터 AI 상담원 이미지"));

                rb.add(simpleResponse);
                rb.add(basicCard);
                suggestions.add("깜박여요");
                suggestions.add("안나와요");
                suggestions.add(CommonWord.SUGGEST_TO_MOBILE);
                suggestions.add(CommonWord.SUGGEST_GUIDE_FIND);
            } else {
                // check status로 유도
                if (symptom.equalsIgnoreCase("sym1")) {
                    simpleResponse.setTextToSpeech(
                            "<speak><sub alias='그러셨군요. 혹시 티비와 연결되어 있는 케이블 종류는 에이치디엠아이나 컴포넌트 케이블 중 무엇을 사용하고 계신가요'>TV와 셋탑박스를 연결하고있는 케이블 (HDMI / 컴포넌트 케이블)을 아래의 그림을 참고하시고 선택해서 말씀해 주시면 됩니다.</sub></speak>");
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

                    rb.add(simpleResponse);
                    rb.add(basicCard);
                    rb.add(new SelectionCarousel().setItems(items));

                    suggestions.add("컴포넌트");
                    suggestions.add("HDMI");
                    suggestions.add(CommonWord.SUGGEST_TO_MOBILE);
                    suggestions.add(CommonWord.SUGGEST_GUIDE_FIND);
                }

                if (symptom.equalsIgnoreCase("sym2")) {

                }
            }

            rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
        } else {
            BasicCard basicCard = new BasicCard();

            switch (solution) {
                case 1:
                    if (connectionType.equalsIgnoreCase("con2")) {
                        simpleResponse.setTextToSpeech(
                                "<speak><sub alias='네 고객님. 그럼 몇 가지 확인을 부탁 드리겠습니다. 다음과 같이 컴포넌트 케이블 연결을 다시 한번 확인해 보시겠습니까?'>1. 컴포넌트 케이블 구성 확인  \n	(영상 3개 - 빨강/파랑/초록 + 음성 2개 - 하양/빨강)   \n2. TV와 셋탑박스 뒷 면의 컴포넌으 입력 단자에 색상에 맞게 케이블 연결   \n3. TV 외부입력 - 컴포넌트 확인</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/componentcheck.mp3'></audio></speak>");
                        basicCard.setFormattedText("  \n1. 컴포넌트 케이블 구성 확인  \n	(영상 3개 - 빨강/파랑/초록 + 음성 2개 - 하양/빨강)   \n2. TV와 셋탑박스 뒷 면의 컴포넌으 입력 단자에 색상에 맞게 케이블 연결   \n3. TV 외부입력 - 컴포넌트 확인").setImage(
                                new Image().setUrl("https://actions.o2o.kr/content/servicecenter/componentcheck.gif")
                                        .setAccessibilityText("컴포넌트연결확인방법 이미지"));
                    } else { // if (connectionType.equalsIgnoreCase("con1")
                        simpleResponse.setTextToSpeech(
                                "<speak><sub alias='네 고객님. 그럼 몇 가지 확인을 부탁 드리겠습니다. 다음과 같이 HDMI 케이블 연결을 다시 한번 확인해 보시겠습니까?'>1. 셋탑박스 뒷 면의 HDMI 단자 위치   \n2. HDMI 케이블 연결상태 확인   \n3. TV외부입력 - HDMI1 / HDMI2 확인</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/hdmicheck.mp3'></audio></speak>");
                        basicCard.setTitle("HDMI 연결확인 방법").setFormattedText("  \n1. 셋탑박스 뒷 면의 HDMI 단자 위치   \n2. HDMI 케이블 연결상태 확인   \n3. TV외부입력 - HDMI1 / HDMI2 확인");
                        basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/hdmicheck.gif")
                                .setAccessibilityText("HDMI연결확인방법 이미지"));
                    }
                    suggestions.add(CommonWord.SUGGEST_NOT_WORK);

                    rb.add(simpleResponse);
                    rb.add(basicCard);
                    break;
                case 2:
                    simpleResponse.setTextToSpeech(
                            "<speak><sub alias='네 알겠습니다. 번거로우시겠지만 보여지는 화면처럼 TV 전원을 다시 껏다 켜보시겠습니까?'>1. 셋탑박스 뒷 면의 전원 버튼 OFF   \n2. 셋탑박스 뒷 면의 전원 버튼 ON</sub><audio src ='https://actions.o2o.kr/content/servicecenter/suggesttvonoff.mp3'></audio></speak>");

                    basicCard.setTitle("TV 전원 껏다 켜는 방법").setFormattedText("  \n1. 셋탑박스 뒷 면의 전원 버튼 OFF  \n2. 셋탑박스 뒷 면의 전원 버튼 ON");
                    basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/suggesttvonoff.gif")
                            .setAccessibilityText("TV 전원껏다켜는 방법 이미지"));
                    suggestions.add(CommonWord.SUGGEST_NOT_WORK);

                    rb.add(simpleResponse);
                    rb.add(basicCard);
                    break;
                case 3:
                    simpleResponse.setTextToSpeech(
                            "<speak><sub alias='네 알게습니다. 화면에 보이는것처럼 RF 케이블 연결을 다시 한번 확인 부탁드릴게요.'>1. 셋탑박스 뒷 면의 위성 입력에 꽂혀있는 RF 케이블 확인   \n2. RF 케이블 뺏다 끼기</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/suggestrfcable.mp3'></audio></speak>");

                    basicCard.setTitle("RF 케이블 연결 확인하는 방법").setFormattedText("  \n1. 셋탑박스 뒷 면의 위성 입력에 꽂혀있는 RF 케이블 확인  \n2. RF 케이블 뺏다 끼기");
                    basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/suggestrfcable.gif")
                            .setAccessibilityText("RF 케이블 연결 확인하는 방법 이미지"));
                    suggestions.add(CommonWord.SUGGEST_NOT_WORK);

                    rb.add(simpleResponse);
                    rb.add(basicCard);
                    break;
                default:
                    simpleResponse.setTextToSpeech(
                            "<speak><sub alias=''>증상 해결에 도움을 드리지 못해 죄송합니다.   \nSkylife AS 접수 : 1588-3022   \n원격진단이나 콜센터 연결 방법을 듣길 원하신다면 해당 항목을 말씀해주세요.</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/aispeak3.mp3'></audio></speak>");

                    basicCard.setFormattedText("  \n증상 해결에 도움을 드리지 못해 죄송합니다.  \nSkylife AS 접수 : 1588-3022  \n원격진단이나 콜센터 연결 방법을 듣길 원하신다면 해당 항목을 말씀해주세요.");
                    basicCard
                            .setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
                                    .setAccessibilityText("Skylife 서비스센터 AI 상담원 이미지"));
                    suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_ASCENTER);
                    suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_REMOTE);

                    rb.add(simpleResponse);
                    rb.add(basicCard);
                    break;
            }
            suggestions.add(CommonWord.SUGGEST_TO_MOBILE);
            suggestions.add(CommonWord.SUGGEST_GUIDE_FIND);

            rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
        }
        return rb.build();
    }

    @ForIntent("support-fallback")
    public ActionResponse processSupportFallback(ActionRequest request)
            throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);
        Map<String, Object> data = rb.getConversationData();

        BasicCard basicCard = new BasicCard();
        SimpleResponse simpleResponse = new SimpleResponse();
        List<String> suggestions = new ArrayList<String>();

        CommonUtil.printMapData(data);

        simpleResponse.setTextToSpeech("<speak><sub alis=''>죄송합니다. Skylife AI 상담원에게 다시 한번 정확한 발음으로 말씀해 주십시오.</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/aispeak5.mp3'></audio></speak>");
        basicCard.setFormattedText("  \n죄송합니다. Skylife AI 상담원에게 다시 한번 정확한 발음으로 말씀해 주십시오.");
        basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
                .setAccessibilityText("Skylife 서비스센터 AI 상담원 이미지"));

        suggestions.add(CommonWord.SUGGEST_RETRY_SPEAK);
        suggestions.add(CommonWord.SUGGEST_TO_MOBILE);

        String symptom = CommonUtil.makeSafeString(data.get("symptom"));

        CommonUtil.printMapData(data);
        if (!CommonUtil.isEmptyString(symptom) && symptom.equalsIgnoreCase("sym1")) {
            // SUGGEST_SUPPORT_FIND 없는 유일한 경우
        } else {
            suggestions.add(CommonWord.SUGGEST_SUPPORT_FIND);
        }
        suggestions.add(CommonWord.SUGGEST_GUIDE_FIND);

        rb.add(simpleResponse);
        rb.add(basicCard);
        rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));

        return rb.build();
    }

    @ForIntent("support-retry")
    public ActionResponse processSupportRetry(ActionRequest request) throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);

        return genSupport(rb);
    }

    @ForIntent("support-mobile")
    public ActionResponse processSupportToMobile(ActionRequest request)
            throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);
        List<String> suggestions = new ArrayList<String>();

        Map<String, Object> data = rb.getConversationData();

        String symptom = CommonUtil.makeSafeString(data.get("symptom"));
        String connectionType = CommonUtil.makeSafeString(data.get("connectionType"));
        int solution = CommonUtil.makeSafeInt(data.get("solution"));

        CommonUtil.printMapData(data);

        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(
                    "https://assistant.google.com/services/invoke/uid/000000ed4bb85dee?intent=support-resume&param.pa1=" // 000000ed4bb85dee
                            + symptom + "&param.pa2=" + connectionType + "&param.pa3=" + solution, //00000091d64b60fe -o2odev1
                    StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        rb.add(new SimpleResponse().setTextToSpeech("<speak><sub alias=''>다음 QR코드를 모바일에서 읽을 경우 Actions를 모바일에서 계속 하실 수 있습니다.</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/aispeak4.mp3'></audio></speak>"))
                .add(new BasicCard()//.setTitle("QR코드를 통한 모바일 링크")
                        .setFormattedText("  \n다음 QR코드를 모바일에서 읽을 경우 Actions를 모바일에서 계속 하실 수 있습니다.  \n1. Google Assisatant가 가능한 휴대폰  \n(ios - 카메라 실행 / android - QR인식 카메라모듈 실행)  \n2. QR 이미지 인식  \n 3.스카이 고객센터 화면 이어서 진행 시작 ")
                        .setImage(new Image().setUrl("https://actions.o2o.kr/skylife/api/1.0/qrcode?url=" + encodedUrl)
                                .setAccessibilityText("모바일 장치 연결을 위한 QR코드"))
                        .setImageDisplayOptions("DEFAULT"));

        suggestions.add(CommonWord.SUGGEST_RETRY_SPEAK);
        suggestions.add(CommonWord.SUGGEST_GUIDE_FIND);

        rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
        return rb.build();
    }

    @ForIntent("support-resume")
    public ActionResponse processSupportResume(ActionRequest request) throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);

        String symptom = CommonUtil.makeSafeString(request.getParameter("pa1"));
        String connectionType = CommonUtil.makeSafeString(request.getParameter("pa2"));
        int solution = CommonUtil.makeSafeInt(request.getParameter("pa3"));

        ActionContext context = new ActionContext("support-find-symptom", 5);
        rb.add(context);
        if (!CommonUtil.isEmptyString(connectionType)) {
            context = new ActionContext("support-check-status", 5);
            rb.add(context);
        }

        Map<String, Object> data = rb.getConversationData();
        data.clear();
        data.put("symptom", symptom);
        data.put("connectionType", connectionType);
        data.put("solution", solution);

        return genSupport(rb);
    }

    @ForIntent("guide-find")
    public ActionResponse processGuideFind(ActionRequest request) throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);

        rb.removeContext("support-check-status");
        rb.removeContext("support-find-symptom");

        String guideType = CommonUtil.makeSafeString(request.getParameter("guideType"));
        String guideCategoryType = CommonUtil.makeSafeString(request.getParameter("guideCategoryType"));

        Map<String, Object> data = rb.getConversationData();
        data.clear();
        data.put("guideCategoryType", guideCategoryType);
        data.put("guideType", guideType);

        return genGuideFind(rb);
    }

    private ActionResponse genGuideFind(ResponseBuilder rb) {
        Map<String, Object> data = rb.getConversationData();
        String guideType = CommonUtil.makeSafeString(data.get("guideType"));
        String guideCategoryType = CommonUtil.makeSafeString(data.get("guideCategoryType"));

        CommonUtil.printMapData(data);

        if (!CommonUtil.isEmptyString(guideCategoryType)) {
            return genGuideCategoryType(rb, guideCategoryType);
        } else {
            return genGuideFindType(rb, guideType);
        }
    }

    private ActionResponse genGuideCategoryType(ResponseBuilder rb, String guideCategoryType) {
        List<String> suggestions = new ArrayList<String>();
        SimpleResponse simpleResponse = new SimpleResponse();
        List<CarouselSelectCarouselItem> items = new ArrayList<>();
        CarouselSelectCarouselItem item;

        if (guideCategoryType.equalsIgnoreCase("internet")) {
            simpleResponse.setTextToSpeech("<speak><sub alias=''>원하시는 인터넷 연결 방법을 선택해서 말씀해 주시면 됩니다.</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/aispeak6.mp3'></audio></speak>");
            item = new CarouselSelectCarouselItem().setTitle("와이파이 연결 방법").setDescription("무선(WIFI) 인터넷 연결 방법입니다.")
                    .setOptionInfo(new OptionInfo().setKey("wifi"))
                    .setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/wifi.jpg")
                            .setAccessibilityText("wifi jpg"));
            items.add(item);

            item = new CarouselSelectCarouselItem().setTitle("유선 연결 방법").setDescription("유선(LAN) 인터넷 연결 방법입니다.")
                    .setOptionInfo(new OptionInfo().setKey("lan"))
                    .setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/lan.jpg")
                            .setAccessibilityText("lan jpg"));
            items.add(item);

            item = new CarouselSelectCarouselItem().setTitle("WPS 연결 방법").setDescription("WPS 연결 방법입니다.")
                    .setOptionInfo(new OptionInfo().setKey("wps"))
                    .setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/wps.jpg")
                            .setAccessibilityText("wps jpg"));
            items.add(item);

            suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_WIFI);
            suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_LAN);
            suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_WPS);
        }
        suggestions.add(CommonWord.SUGGEST_TO_MOBILE);
        suggestions.add(CommonWord.SUGGEST_SUPPORT_FIND);

        rb.add(simpleResponse);
        rb.add(new SelectionCarousel().setItems(items));
        rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));

        return rb.build();
    }

    private ActionResponse genGuideFindType(ResponseBuilder rb, String guideType) {
        BasicCard basicCard = new BasicCard();
        SimpleResponse simpleResponse = new SimpleResponse();
        List<String> suggestions = new ArrayList<String>();

        switch (guideType) {
            case "ascontrol":
                simpleResponse.setTextToSpeech(
                        "<speak><sub alias = '네 알겠습니다. 원격진단 연결하는 방법을 설명해 드릴게요. 다음에 보여지는 화면을 따라 시도해 주세요.'>1. 1588-3022  전화 연결   \n2. 2번 고장문의   \n3. 2번 원격진단   \n4. 스마트카드번호 앞 2자리를 제외한 10자리와 # 입력   \n5. 개인고객 - 생년월일 6자리 / 사업자고객 - 사업자번호 앞 6자리   \n6. 셋탑박스의 빨간 LED 확인</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/ascontrol.mp3'></audio></speak>");
                basicCard.setTitle("원격진단 방법").setFormattedText("  \n1. 1588-3022  전화 연결  \n2. 2번 고장문의  \n3. 2번 원격진단  \n4. 스마트카드번호 앞 2자리를 제외한 10자리와 # 입력  \n5. 개인고객 - 생년월일 6자리 / 사업자고객 - 사업자번호 앞 6자리  \n6. 셋탑박스의 빨간 LED 확인");
                basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/ascontrol.gif")
                        .setAccessibilityText("원격진단 방법 이미지"));
                suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_ASCENTER);
                suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_SMARTCARD);
                break;
            case "callcenter":
                simpleResponse.setTextToSpeech(
                        "<speak><sub alias = '네 알겠습니다. 콜센터 연결하는 방법을 설명해 드릴게요. 다만 스카이라이프가 아닌 고객님 소유 장비 또는 타사 서비스 문제인 경우는 기사 출동비 만 천원이 다음달에 발생될 수 있으니 참고 부탁드립니다.'>1. 1588-3002 전화 연결   \n2. 2번 고장문의</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/callcenter.mp3'></audio></speak>");
                basicCard.setTitle("콜센터 연결방법").setFormattedText("  \n1. 1588-3002 전화 연결  \n2. 2번 고장문의");
                basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/callcenter.gif")
                        .setAccessibilityText("콜센터 연결방법 이미지"));
                suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_REMOTE);
                suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_SMARTCARD);
                break;
            case "smartcardno":
                simpleResponse.setTextToSpeech(
                        "<speak><sub alias='네 고객님. 스마트카드번호를 확인하는 방법을 설명해 드릴게요. 다음과 같이 스마트카드번호 확인 후 ARS 콜센터로 연락 주시면, 안전하게 고객님의 소중한 정보를 확인하여 신속한 상담을 도와드리겠습니다.'>1. 리모컨 버튼의 홈 버튼 클릭   \n2. 화면 상의 마이페이지 클릭   \n3. 우측 상단의 나의 스마트카드 번호 확인</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/smartcard.mp3'></audio></speak>");
                basicCard.setTitle("스마트카드번호 확인방법").setFormattedText("  \n1. 리모컨 버튼의 홈 버튼 클릭  \n2. 화면 상의 마이페이지 클릭  \n3. 우측 상단의 나의 스마트카드 번호 확인");
                basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/smartcard.gif")
                        .setAccessibilityText("스마트카드번호 확인방법 이미지"));
                suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_ASCENTER);
                suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_REMOTE);
                break;
            case "wifi":
                simpleResponse.setTextToSpeech(
                        "<speak><sub alias='네 알겠습니다. 화면에 보이는것처럼 무선 와이파이 연결을 시도해 보시기 바랍니다.'>1.리모컨 버튼의 홈버튼 클릭   \n2. 화면 상의  설정 클릭   \n3. 인터넷   \n4. 인터넷 연결 가이드   \n5. 무선인터넷 연결(WiFi)   \n6. 해당 WiFi 비밀번호 입력   \n7. 무선인터넷 연결</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/wifi.mp3'></audio></speak>");
                basicCard.setTitle("무선(WIFI) 인터넷 연결 방법").setFormattedText("  \n1.리모컨 버튼의 홈버튼 클릭  \n2. 화면 상의  설정 클릭  \n3. 인터넷  \n4. 인터넷 연결 가이드  \n5. 무선인터넷 연결(WiFi)  \n6. 해당 WiFi 비밀번호 입력  \n7. 무선인터넷 연결");
                basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/wifi.gif")
                        .setAccessibilityText("무선(WIFI) 인터넷 연결 방법 이미지"));
                suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_LAN);
                suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_WPS);
                break;
            case "lan":
                simpleResponse.setTextToSpeech(
                        "<speak>><sub alias='네 알겠습니다. 다음과 같이 유선 랜으로 인터넷 연결을 시도해 보시기 바랍니다.'>1.리모컨 버튼의 홈버튼 클릭   \n2. 화면 상의  설정 클릭    \n3. 인터넷   \n4. 인터넷 연결 가이드   \n5. 유선 인터넷 연결(이더넷)</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/lan.mp3'></audio></speak>");
                basicCard.setTitle("유선(LAN) 인터넷 연결 방법").setFormattedText("  \n1.리모컨 버튼의 홈버튼 클릭  \n2. 화면 상의  설정 클릭   \n3. 인터넷  \n4. 인터넷 연결 가이드  \n5. 유선 인터넷 연결(이더넷)");
                basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/lan.gif")
                        .setAccessibilityText("유선(LAN) 인터넷 연결 방법 이미지"));
                suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_WIFI);
                suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_WPS);
                break;
            case "wps":
                simpleResponse.setTextToSpeech(
                        "<speak><sub alias='네 알겠습니다. 무선 WPS 연결은 비밀번호 없이도  손쉽게 와이파이 연결이 가능한 방법이오니, 다음 설명을 잘 듣고 따라해 보시기 바랍니다.'>1.리모컨 버튼의 홈버튼 클릭   \n2. 화면 상의  설정 클릭   \n3. 인터넷   \n4. 안드로이드 설정 바로가기   \n5. WPS를 통해 연결    \n6. 공유기의 WPS버튼 클릭</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/wps.mp3'></audio></speak>");
                basicCard.setTitle("무선(WPS) 연결 방법").setFormattedText("  \n1.리모컨 버튼의 홈버튼 클릭  \n2. 화면 상의  설정 클릭  \n3. 인터넷  \n4. 안드로이드 설정 바로가기  \n5. WPS를 통해 연결   \n6. 공유기의 WPS버튼 클릭");
                basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/wps.gif")
                        .setAccessibilityText("WPS 연결 방법 이미지"));
                suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_WIFI);
                suggestions.add(CommonWord.SUGGEST_GUIDE_TYPE_LAN);
                break;
            default:
                break;
        }

        suggestions.add(CommonWord.SUGGEST_TO_MOBILE);
        suggestions.add(CommonWord.SUGGEST_SUPPORT_FIND);

        rb.add(simpleResponse);
        rb.add(basicCard);
        rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));

        return rb.build();
    }

    @ForIntent("guide-find.option")
    public ActionResponse processGuideFindOption(ActionRequest request)
            throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);
        String selectedItem = request.getSelectedOption();
        Map<String, Object> data = rb.getConversationData();
        data.put("guideCategoryType", "");
        data.put("guideType", selectedItem);

        return genGuideFindType(rb, selectedItem);
    }

    @ForIntent("guide-fallback")
    public ActionResponse processGuideFallback(ActionRequest request) throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);
        Map<String, Object> data = rb.getConversationData();

        BasicCard basicCard = new BasicCard();
        SimpleResponse simpleResponse = new SimpleResponse();
        List<String> suggestions = new ArrayList<String>();

        CommonUtil.printMapData(data);

        simpleResponse.setTextToSpeech("<speak><sub alias='죄송합니다. 무슨 말씀을 하시는지 못알아 들었어요. 다시 한번 말씀해 주시겠습니까?'>죄송합니다. Skylife AI 상담원에게 다시 한번 정확한 발음으로 말씀해 주십시오.</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/aispeak5.mp3'></audio></speak>");
        basicCard.setFormattedText("죄송합니다. Skylife AI 상담원에게 다시 한번 정확한 발음으로 말씀해 주십시오.");
        basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
                .setAccessibilityText("Skylife 서비스센터 AI 상담원 이미지"));


        if (!CommonUtil.isEmptyString(data.get("guideCategoryType"))
                || !CommonUtil.isEmptyString(data.get("guideType"))) {
            suggestions.add(CommonWord.SUGGEST_RETRY_SPEAK);
            suggestions.add(CommonWord.SUGGEST_TO_MOBILE);
        }
        suggestions.add(CommonWord.SUGGEST_SUPPORT_FIND);

        rb.add(simpleResponse);
        rb.add(basicCard);
        rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
        return rb.build();
    }

    @ForIntent("guide-retry")
    public ActionResponse processGuideRetry(ActionRequest request) throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);

        return genGuideFind(rb);
    }

    @ForIntent("guide-mobile")
    public ActionResponse processGuideToMobile(ActionRequest request) throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);
        List<String> suggestions = new ArrayList<String>();

        Map<String, Object> data = rb.getConversationData();

        String guideCategoryType = CommonUtil.makeSafeString(data.get("guideCategoryType"));
        String guideType = CommonUtil.makeSafeString(data.get("guideType"));

        CommonUtil.printMapData(data);

        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(
                    "https://assistant.google.com/services/invoke/uid/000000ed4bb85dee?intent=guide-resume&param.pa1="
                            + guideCategoryType + "&param.pa2=" + guideType,
                    StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        rb.add(new SimpleResponse().setTextToSpeech("<speak><sub alias=''>다음 QR코드를 모바일에서 읽을 경우 Actions를 모바일에서 계속 하실 수 있습니다.</sub><audio src = 'https://actions.o2o.kr/content/servicecenter/aispeak4.mp3'></audio></speak>"))
                .add(new BasicCard()//.setTitle("QR코드를 통한 모바일 링크")
                        .setFormattedText("  \n다음 QR코드를 모바일에서 읽을 경우 Actions를 모바일에서 계속 하실 수 있습니다.  \n1. Google Assisatant가 가능한 휴대폰   \n(ios - 카메라 실행 / android - QR인식 카메라모듈 실행)  \n2. QR 이미지 인식  \n 3.스카이 고객센터 화면 이어서 진행 시작 ")
                        .setImage(new Image().setUrl("https://actions.o2o.kr/skylife/api/1.0/qrcode?url=" + encodedUrl)
                                .setAccessibilityText("모바일 장치 연결을 위한 QR코드"))
                        .setImageDisplayOptions("DEFAULT"));

        suggestions.add(CommonWord.SUGGEST_RETRY_SPEAK);
        suggestions.add(CommonWord.SUGGEST_GUIDE_FIND);

        rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));

        return rb.build();
    }

    @ForIntent("guide-resume")
    public ActionResponse processGuideResume(ActionRequest request) throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);

        String guideCategoryType = CommonUtil.makeSafeString(request.getParameter("pa1"));
        String guideType = CommonUtil.makeSafeString(request.getParameter("pa2"));

        ActionContext context = new ActionContext("guide-find", 5);
        rb.add(context);

        Map<String, Object> data = rb.getConversationData();
        data.clear();
        data.put("guideCategoryType", guideCategoryType);
        data.put("guideType", guideType);

        return genGuideFind(rb);
    }

    @ForIntent("Default Fallback Intent")
    public ActionResponse defaultFallback(ActionRequest request) throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);

        List<String> suggestions = new ArrayList<String>();
        BasicCard basicCard = new BasicCard();

        rb.add(new SimpleResponse().setTextToSpeech("<p><s>  \n죄송합니다. 무슨 말씀을 하시는지 못알아 들었어요.  \n</s><s>추천 키워드를 참고하시고 다시 한번 말씀해 주시겠습니까?</s></p>"));

        basicCard.setFormattedText("죄송합니다. Skylife AI 상담원에게 추천 키워드를 참고하시고 다시 말씀해 주세요.");
        basicCard.setImage(new Image().setUrl("https://actions.o2o.kr/content/servicecenter/skylifeaiperson.gif")
                .setAccessibilityText("Skylife 서비스센터 AI 상담원 이미지"));

        suggestions.add(CommonWord.SUGGEST_SUPPORT_FIND);
        suggestions.add(CommonWord.SUGGEST_GUIDE_FIND);

        rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));
        rb.add(basicCard);

        return rb.build();
    }

    @ForIntent("Default Welcome Intent")
    public ActionResponse defaultWelcome(ActionRequest request) throws ExecutionException, InterruptedException {
        ResponseBuilder rb = getResponseBuilder(request);

        List<String> suggestions = new ArrayList<String>();

        rb.add("<p><s>  \n안녕하세요, 스카이 서비스입니다.  \n</s><s>무엇을 도와드릴까요?</s></p>");

        suggestions.add(CommonWord.SUGGEST_SUPPORT_FIND);
        suggestions.add(CommonWord.SUGGEST_GUIDE_FIND);

        rb.addSuggestions(suggestions.toArray(new String[suggestions.size()]));

        return rb.build();
    }
}