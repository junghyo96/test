package com.o2o.action.server.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.o2o.action.server.app.temp.GogumaApp;
import com.o2o.action.server.db.Category;
import com.o2o.action.server.repo.CategoryRepository;
import com.o2o.action.server.util.CommonUtil;

@RestController
public class DefaultController {
	@Autowired
	private CategoryRepository categoryRepository;

	private GogumaApp gogumaApp = new GogumaApp(); 
	public DefaultController() {
	}
	
	@RequestMapping(value = "/test1", method = RequestMethod.POST)
	public @ResponseBody String processNo1(@RequestBody String body, HttpServletRequest request,
			HttpServletResponse response) {
		String jsonResponse = null;
		try {
			System.out.println("request : " + body);
			jsonResponse = gogumaApp.handleRequest(body, CommonUtil.getHttpHeadersMap(request)).get();
			System.out.println("response : " + jsonResponse);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return jsonResponse;
	}

	@RequestMapping(value = "/api/1.0/login", method = RequestMethod.POST)
	public void login(@RequestParam(value = "inputID", required = true) String id,
			@RequestParam(value = "inputPassword", required = true) String passwd, HttpServletRequest request,
			HttpServletResponse response) {
		if (id != null && passwd != null && id.length() > 0 && passwd.length() > 0) {
			if (id.trim().equalsIgnoreCase("admin") && passwd.trim().equalsIgnoreCase("1234")) {
				request.getSession().setAttribute("userId", "admin");
				try {
					response.sendRedirect(request.getContextPath() + "/");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@RequestMapping(value = "/api/1.0/logout", method = RequestMethod.POST)
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		HttpSession httpSession = request.getSession();
		if (httpSession != null) {
			httpSession.removeAttribute("userId");
		}
		try {
			response.sendRedirect(request.getContextPath() + "/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Transactional
	@RequestMapping(value = "/api/1.0/category", method = RequestMethod.GET)
	public @ResponseBody Category getCategory(@RequestParam(value = "id", required = false) Long id) {
		if (id != null) {
			Category category = categoryRepository.findById(id).get();
			return category;
		}

		return null;
	}

	@Transactional
	@RequestMapping(value = "/api/1.0/category/child", method = RequestMethod.GET)
	public @ResponseBody List<Category> getCategory(@RequestParam(value = "parentId", required = false) Long id,
			@RequestParam(value = "keycode", required = false) String keycode) {
		List<Category> categories = null;

		if (id != null) {
			Category category = categoryRepository.findById(id).get();
			categories = category.getChildren();
		} else if (keycode != null) {
			List<Category> tmpCategories = categoryRepository.findByKeycodeOrderByDispOrderAsc(keycode);
			if (tmpCategories != null && tmpCategories.size() > 0) {
				categories = tmpCategories.get(0).getChildren();
			}
		} else {
			categories = categoryRepository.findByParentOrderByDispOrderAsc(null);
		}

		if (categories != null) {
			// for (Category category : categories) {
			// category.setChildren(null);
			// }

			return categories;
		}
		return null;
	}

	@RequestMapping(value = "/api/1.0/qrcode", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	public @ResponseBody byte[] generateQRCode(@RequestParam(value = "url", required = true) String url) {
		try {
			BitMatrix matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 720, 480);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(matrix, "png", outputStream);

			return outputStream.toByteArray();
		} catch (WriterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
