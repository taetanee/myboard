package com.web.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.common.Const;
import com.web.common.MyException;
import com.web.common.util.CommonUtil;
import com.web.dto.wedding.ProductOrderDTO;
import com.web.dto.wedding.WeddingCustomerDTO;
import com.web.mapper.ProductOrderMapper;
import com.web.mapper.WeddingCustomerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WeddingPlatformService {

	@Autowired
	private WeddingCustomerMapper weddingCustomerMapper;

	@Autowired
	private ProductOrderMapper productOrderMapper;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${wedding.admin-key:}")
	private String configuredAdminKey;

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private static final String UPLOAD_DIR = "C:/uploads/wedding/";

	private static final long SESSION_TTL_SECONDS = TimeUnit.DAYS.toSeconds(30);

	private static final String SESSION_KEY_PREFIX = "wedding:session:";

	// ───────────────────────── 인증 ─────────────────────────

	public Map<String, Object> login(String loginId, String password) throws Exception {
		if (CommonUtil.isEmptyOrNull(loginId) || CommonUtil.isEmptyOrNull(password)) {
			throw new MyException(Const.NOT_INVALID_PARAM_ERROR);
		}

		WeddingCustomerDTO customer = weddingCustomerMapper.findByLoginId(loginId);
		if (customer == null || !passwordEncoder.matches(password, customer.getPassword())) {
			throw new MyException(Const.LOGIN_FAILED_ERROR);
		}
		if (!"ACTIVE".equals(customer.getStatus())) {
			throw new MyException(Const.LOGIN_FAILED_ERROR);
		}

		String token = UUID.randomUUID().toString();
		commonUtil.setCache(SESSION_KEY_PREFIX + token, String.valueOf(customer.getIdx()), SESSION_TTL_SECONDS);

		Map<String, Object> result = new HashMap<>();
		result.put("token", token);
		result.put("customer", toCustomerView(customer));
		return result;
	}

	public void logout(String token) {
		if (!CommonUtil.isEmptyOrNull(token)) {
			commonUtil.setCache(SESSION_KEY_PREFIX + token, "", 1);
		}
	}

	public Map<String, Object> getMe(String token) throws Exception {
		WeddingCustomerDTO customer = requireAuth(token);
		Map<String, Object> result = toCustomerView(customer);
		result.put("invite", parseJsonOrEmpty(customer.getInviteJson()));
		result.put("video", parseJsonOrEmpty(customer.getVideoJson()));
		return result;
	}

	private WeddingCustomerDTO requireAuth(String token) throws Exception {
		if (CommonUtil.isEmptyOrNull(token)) {
			throw new MyException(Const.UNAUTHORIZED_ERROR);
		}
		String idxStr = commonUtil.getCache(SESSION_KEY_PREFIX + token);
		if (CommonUtil.isEmptyOrNull(idxStr)) {
			throw new MyException(Const.UNAUTHORIZED_ERROR);
		}
		WeddingCustomerDTO customer = weddingCustomerMapper.findByIdx(Long.valueOf(idxStr));
		if (customer == null) {
			throw new MyException(Const.UNAUTHORIZED_ERROR);
		}
		return customer;
	}

	private void requireAdmin(String adminKey) throws Exception {
		if (CommonUtil.isEmptyOrNull(configuredAdminKey) || !configuredAdminKey.equals(adminKey)) {
			throw new MyException(Const.FORBIDDEN_ADMIN_ERROR);
		}
	}

	private Map<String, Object> toCustomerView(WeddingCustomerDTO customer) {
		Map<String, Object> view = new HashMap<>();
		view.put("idx", customer.getIdx());
		view.put("loginId", customer.getLoginId());
		view.put("slug", customer.getSlug());
		view.put("customerName", customer.getCustomerName());
		view.put("phone", customer.getPhone());
		view.put("status", customer.getStatus());
		return view;
	}

	private Map<String, Object> parseJsonOrEmpty(String json) throws Exception {
		if (CommonUtil.isEmptyOrNull(json)) {
			return new HashMap<>();
		}
		return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
	}

	// ───────────────────────── 모바일 청첩장 ─────────────────────────

	public void saveInvite(String token, Map<String, Object> content) throws Exception {
		WeddingCustomerDTO customer = requireAuth(token);
		weddingCustomerMapper.updateInviteJson(customer.getIdx(), objectMapper.writeValueAsString(content));
	}

	public Map<String, Object> getInvitePublic(String slug) throws Exception {
		WeddingCustomerDTO customer = weddingCustomerMapper.findBySlug(slug);
		if (customer == null || !"ACTIVE".equals(customer.getStatus())) {
			throw new MyException(Const.NO_DATA_API_ERROR);
		}
		Map<String, Object> result = parseJsonOrEmpty(customer.getInviteJson());
		result.put("slug", customer.getSlug());
		return result;
	}

	// ───────────────────────── 식전영상 ─────────────────────────

	public void saveVideo(String token, Map<String, Object> content) throws Exception {
		WeddingCustomerDTO customer = requireAuth(token);
		weddingCustomerMapper.updateVideoJson(customer.getIdx(), objectMapper.writeValueAsString(content));
	}

	public Map<String, Object> getVideoPublic(String slug) throws Exception {
		WeddingCustomerDTO customer = weddingCustomerMapper.findBySlug(slug);
		if (customer == null || !"ACTIVE".equals(customer.getStatus())) {
			throw new MyException(Const.NO_DATA_API_ERROR);
		}
		Map<String, Object> result = parseJsonOrEmpty(customer.getVideoJson());
		result.put("slug", customer.getSlug());
		return result;
	}

	// ───────────────────────── 이미지 업로드 ─────────────────────────

	public Map<String, Object> uploadImage(String token, MultipartFile file) throws Exception {
		WeddingCustomerDTO customer = requireAuth(token);
		if (file == null || file.isEmpty()) {
			throw new MyException(Const.NOT_INVALID_PARAM_ERROR);
		}

		String dirPath = UPLOAD_DIR + customer.getSlug() + "/";
		File uploadDir = new File(dirPath);
		if (!uploadDir.exists()) {
			uploadDir.mkdirs();
		}

		String originalName = file.getOriginalFilename();
		String ext = "";
		if (originalName != null && originalName.contains(".")) {
			ext = originalName.substring(originalName.lastIndexOf('.'));
		}
		String savedName = UUID.randomUUID().toString().substring(0, 12) + ext;
		file.transferTo(new File(dirPath + savedName));

		Map<String, Object> result = new HashMap<>();
		result.put("url", "/weddingPlatform/image?slug=" + customer.getSlug() + "&fileName=" + savedName);
		return result;
	}

	public File resolveImageFile(String slug, String fileName) throws Exception {
		File file = new File(UPLOAD_DIR + slug + "/" + fileName);
		if (!file.exists() || !file.isFile()) {
			throw new MyException(Const.NO_DATA_API_ERROR);
		}
		return file;
	}

	// ───────────────────────── 발주 신청 (종이청첩장/식권) ─────────────────────────

	public void createOrder(String token, Map<String, Object> param) throws Exception {
		WeddingCustomerDTO customer = requireAuth(token);

		String productType = (String) param.get("productType");
		if (CommonUtil.isEmptyOrNull(productType)
				|| !("PAPER_INVITATION".equals(productType) || "MEAL_TICKET".equals(productType))) {
			throw new MyException(Const.NOT_INVALID_PARAM_ERROR);
		}

		ProductOrderDTO dto = new ProductOrderDTO();
		dto.setCustomerIdx(customer.getIdx());
		dto.setProductType(productType);
		dto.setSampleCode((String) param.get("sampleCode"));
		Object quantity = param.get("quantity");
		dto.setQuantity(quantity == null ? 1 : Integer.valueOf(String.valueOf(quantity)));
		dto.setRecipientName((String) param.get("recipientName"));
		dto.setRecipientPhone((String) param.get("recipientPhone"));
		dto.setShippingAddr((String) param.get("shippingAddr"));
		dto.setMemo((String) param.get("memo"));
		dto.setStatus("REQUESTED");

		productOrderMapper.insertOrder(dto);
	}

	public List<ProductOrderDTO> listMyOrders(String token) throws Exception {
		WeddingCustomerDTO customer = requireAuth(token);
		return productOrderMapper.findByCustomerIdx(customer.getIdx());
	}

	// ───────────────────────── 관리자 ─────────────────────────

	public Map<String, Object> adminCreateCustomer(String adminKey, Map<String, Object> param) throws Exception {
		requireAdmin(adminKey);

		String loginId = (String) param.get("loginId");
		String password = (String) param.get("password");
		if (CommonUtil.isEmptyOrNull(loginId) || CommonUtil.isEmptyOrNull(password)) {
			throw new MyException(Const.NOT_INVALID_PARAM_ERROR);
		}
		if (weddingCustomerMapper.existsLoginId(loginId) > 0) {
			throw new MyException(Const.DUPLICATE_LOGIN_ID_ERROR);
		}

		String requestedSlug = (String) param.get("slug");
		String slug = CommonUtil.isEmptyOrNull(requestedSlug) ? loginId : requestedSlug;
		if (weddingCustomerMapper.existsSlug(slug) > 0) {
			slug = slug + "-" + commonUtil.getUUID(4);
		}

		WeddingCustomerDTO dto = new WeddingCustomerDTO();
		dto.setLoginId(loginId);
		dto.setPassword(passwordEncoder.encode(password));
		dto.setSlug(slug);
		dto.setCustomerName((String) param.get("customerName"));
		dto.setPhone((String) param.get("phone"));
		dto.setNaverOrderNo((String) param.get("naverOrderNo"));
		dto.setStatus("ACTIVE");

		weddingCustomerMapper.insertCustomer(dto);

		Map<String, Object> result = new HashMap<>();
		result.put("idx", dto.getIdx());
		result.put("loginId", dto.getLoginId());
		result.put("slug", dto.getSlug());
		return result;
	}

	public List<ProductOrderDTO> adminListOrders(String adminKey) throws Exception {
		requireAdmin(adminKey);
		return productOrderMapper.findAll();
	}
}
