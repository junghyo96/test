package com.o2o.action.server.rest;

import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.o2o.action.server.app.ShoppingApp;
import com.o2o.action.server.repo.CategoryRepository;
import com.o2o.action.server.repo.ChannelRepository;
import com.o2o.action.server.repo.ScheduleRepository;
import com.o2o.action.server.util.CommonUtil;

@RestController
public class ShoppingController {
	private final ShoppingApp shoppingApp;

	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private ChannelRepository channelRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;

	public ShoppingController() {
		shoppingApp = new ShoppingApp();
	}

	@RequestMapping(value = "/shopping", method = RequestMethod.POST)
	public @ResponseBody String processActions(@RequestBody String body, HttpServletRequest request,
			HttpServletResponse response) {
		String jsonResponse = null;
		shoppingApp.setCategoryRepository(categoryRepository);
		shoppingApp.setChannelRepository(channelRepository);
		shoppingApp.setScheduleRepository(scheduleRepository);

		try {
			System.out.println("request : " + body + "," + categoryRepository);
			jsonResponse = shoppingApp.handleRequest(body, CommonUtil.getHttpHeadersMap(request)).get();
			System.out.println("response : " + jsonResponse);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return jsonResponse;
	}

	@RequestMapping(value = "/shopping/naver", method = RequestMethod.GET)
	public void processNaverPay(@RequestParam(name = "id") int pId, HttpServletRequest request,
			HttpServletResponse response) {
		String jsonResponse = null;
		try {
			if (pId == 1)
				response.sendRedirect(
						"https://www.10boon.co.kr/goods/naver_pay.php?mode=cartIn&scmNo=10&cartMode=pa&set_goods_price=61600&set_goods_fixedPrice=88000.00&set_goods_mileage=0&set_goods_stock=%E2%88%9E&set_coupon_dc_price=61600.00&set_goods_total_price=0&set_option_price=0&set_option_text_price=0&set_add_goods_price=0&set_total_price=61600&mileageFl=c&mileageGoods=0.00&mileageGoodsUnit=percent&goodsDiscountFl=n&goodsDiscount=0.00&goodsDiscountUnit=percent&taxFreeFl=t&taxPercent=10.0&scmNo=10&brandCd=013&cateCd=007&optionFl=n&useBundleGoods=1&deliveryCollectFl=pre&deliveryMethodFl=delivery&goodsNo%5B%5D=1000000397&optionSno%5B%5D=1976&goodsPriceSum%5B%5D=61600&addGoodsPriceSum%5B%5D=0&couponApplyNo%5B%5D=&couponSalePriceSum%5B%5D=&couponAddPriceSum%5B%5D=&goodsCnt%5B%5D=1&optionPriceSum%5B%5D=0&option_price_0=0.00");
			else if (pId == 2)
				response.sendRedirect(
						"https://www.10boon.co.kr/goods/naver_pay.php?mode=cartIn&scmNo=10&cartMode=pa&set_goods_price=68600&set_goods_fixedPrice=98000.00&set_goods_mileage=0&set_goods_stock=%E2%88%9E&set_coupon_dc_price=68600.00&set_goods_total_price=0&set_option_price=0&set_option_text_price=0&set_add_goods_price=0&set_total_price=68600&mileageFl=c&mileageGoods=0.00&mileageGoodsUnit=percent&goodsDiscountFl=n&goodsDiscount=0.00&goodsDiscountUnit=percent&taxFreeFl=t&taxPercent=10.0&scmNo=10&brandCd=013&cateCd=007&optionFl=n&useBundleGoods=1&deliveryCollectFl=pre&deliveryMethodFl=delivery&goodsNo%5B%5D=1000000394&optionSno%5B%5D=1973&goodsPriceSum%5B%5D=68600&addGoodsPriceSum%5B%5D=0&couponApplyNo%5B%5D=&couponSalePriceSum%5B%5D=&couponAddPriceSum%5B%5D=&goodsCnt%5B%5D=1&optionPriceSum%5B%5D=0&option_price_0=0.00");
			else if (pId == 3)
				response.sendRedirect(
						"https://www.10boon.co.kr/goods/naver_pay.php?mode=cartIn&scmNo=21&cartMode=pa&set_goods_price=29000&set_goods_fixedPrice=0.00&set_goods_mileage=0&set_goods_stock=99&set_coupon_dc_price=29000.00&set_goods_total_price=0&set_option_price=0&set_option_text_price=0&set_add_goods_price=0&set_total_price=29000&mileageFl=c&mileageGoods=0.00&mileageGoodsUnit=percent&goodsDiscountFl=n&goodsDiscount=0.00&goodsDiscountUnit=percent&taxFreeFl=t&taxPercent=10.0&scmNo=21&brandCd=025&cateCd=007&optionFl=y&useBundleGoods=1&deliveryCollectFl=pre&deliveryMethodFl=delivery&optionSnoInput=7130%7C%7C0.00%7C%7C0%7C%7C99%5E%7C%5E%EC%8B%A4%EB%B2%84%2F%EC%84%A0%ED%83%9D%EC%95%88%ED%95%A8&optionCntInput=2&optionNo_0=&goodsNo%5B%5D=1000000605&optionSno%5B%5D=7130&goodsPriceSum%5B%5D=29000&addGoodsPriceSum%5B%5D=0&displayOptionkey%5B%5D=7130_1557911561625&couponApplyNo%5B%5D=&couponSalePriceSum%5B%5D=&couponAddPriceSum%5B%5D=&goodsCnt%5B%5D=1&option_price_7130_1557911561625=0.00&optionPriceSum%5B%5D=0");
			else
				response.sendRedirect(
						"https://www.10boon.co.kr/goods/naver_pay.php?mode=cartIn&scmNo=10&cartMode=pa&set_goods_price=68600&set_goods_fixedPrice=98000.00&set_goods_mileage=0&set_goods_stock=%E2%88%9E&set_coupon_dc_price=68600.00&set_goods_total_price=0&set_option_price=0&set_option_text_price=0&set_add_goods_price=0&set_total_price=68600&mileageFl=c&mileageGoods=0.00&mileageGoodsUnit=percent&goodsDiscountFl=n&goodsDiscount=0.00&goodsDiscountUnit=percent&taxFreeFl=t&taxPercent=10.0&scmNo=10&brandCd=013&cateCd=007&optionFl=n&useBundleGoods=1&deliveryCollectFl=pre&deliveryMethodFl=delivery&goodsNo%5B%5D=1000000394&optionSno%5B%5D=1973&goodsPriceSum%5B%5D=68600&addGoodsPriceSum%5B%5D=0&couponApplyNo%5B%5D=&couponSalePriceSum%5B%5D=&couponAddPriceSum%5B%5D=&goodsCnt%5B%5D=1&optionPriceSum%5B%5D=0&option_price_0=0.00");
		} catch (Exception e) {

		}

	}

}
