package com.o2o.action.server.rest;

import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.o2o.action.server.app.EnglishApp;
import com.o2o.action.server.util.CommonUtil;

@RestController
public class EnglishController {
	EnglishApp englishApp = null;

	public EnglishController() {
		englishApp = new EnglishApp();
	}

	@RequestMapping(value = "/englishtalk", method = RequestMethod.POST)
	public @ResponseBody String processNo1(@RequestBody String body, HttpServletRequest request,
			HttpServletResponse response) {
		String jsonResponse = null;
		try {
			System.out.println("request : " + body);
			jsonResponse = englishApp.handleRequest(body, CommonUtil.getHttpHeadersMap(request)).get();
			System.out.println("response : " + jsonResponse);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return jsonResponse;
	}
}
