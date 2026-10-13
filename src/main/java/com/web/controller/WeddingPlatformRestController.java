package com.web.controller;

import com.web.common.CommonResponse;
import com.web.common.MyException;
import com.web.dto.wedding.ProductOrderDTO;
import com.web.service.WeddingPlatformService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

@Api(tags = "웨딩 플랫폼 마이페이지")
@RestController
@RequestMapping("/weddingPlatform")
@Slf4j
public class WeddingPlatformRestController {

	@Autowired
	private WeddingPlatformService weddingPlatformService;

	// ───────────────────────── 인증 ─────────────────────────

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, Object> param) throws Exception {
		CommonResponse response = new CommonResponse();
		try {
			response.setResult(weddingPlatformService.login(
					(String) param.get("loginId"), (String) param.get("password")));
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
		weddingPlatformService.logout(token);
		return ResponseEntity.ok(new CommonResponse());
	}

	@GetMapping("/me")
	public ResponseEntity<?> me(@RequestHeader("X-Auth-Token") String token) throws Exception {
		CommonResponse response = new CommonResponse();
		try {
			response.setResult(weddingPlatformService.getMe(token));
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		return ResponseEntity.ok(response);
	}

	// ───────────────────────── 모바일 청첩장 ─────────────────────────

	@PutMapping("/invite")
	public ResponseEntity<?> saveInvite(@RequestHeader("X-Auth-Token") String token,
										 @RequestBody Map<String, Object> content) throws Exception {
		try {
			weddingPlatformService.saveInvite(token, content);
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		return ResponseEntity.ok(new CommonResponse());
	}

	@GetMapping("/invite/public")
	public ResponseEntity<?> getInvitePublic(@RequestParam String slug) throws Exception {
		CommonResponse response = new CommonResponse();
		try {
			response.setResult(weddingPlatformService.getInvitePublic(slug));
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		return ResponseEntity.ok(response);
	}

	// ───────────────────────── 식전영상 ─────────────────────────

	@PutMapping("/video")
	public ResponseEntity<?> saveVideo(@RequestHeader("X-Auth-Token") String token,
										@RequestBody Map<String, Object> content) throws Exception {
		try {
			weddingPlatformService.saveVideo(token, content);
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		return ResponseEntity.ok(new CommonResponse());
	}

	@GetMapping("/video/public")
	public ResponseEntity<?> getVideoPublic(@RequestParam String slug) throws Exception {
		CommonResponse response = new CommonResponse();
		try {
			response.setResult(weddingPlatformService.getVideoPublic(slug));
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		return ResponseEntity.ok(response);
	}

	// ───────────────────────── 이미지 ─────────────────────────

	@PostMapping("/upload")
	public ResponseEntity<?> upload(@RequestHeader("X-Auth-Token") String token,
									 @RequestPart("file") MultipartFile file) throws Exception {
		CommonResponse response = new CommonResponse();
		try {
			response.setResult(weddingPlatformService.uploadImage(token, file));
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/image")
	public ResponseEntity<Resource> image(@RequestParam String slug, @RequestParam String fileName) throws Exception {
		File file = weddingPlatformService.resolveImageFile(slug, fileName);
		return ResponseEntity.ok().body(new FileSystemResource(file));
	}

	// ───────────────────────── 발주 신청 ─────────────────────────

	@PostMapping("/orders")
	public ResponseEntity<?> createOrder(@RequestHeader("X-Auth-Token") String token,
										  @RequestBody Map<String, Object> param) throws Exception {
		try {
			weddingPlatformService.createOrder(token, param);
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		return ResponseEntity.ok(new CommonResponse());
	}

	@GetMapping("/orders")
	public ResponseEntity<?> myOrders(@RequestHeader("X-Auth-Token") String token) throws Exception {
		CommonResponse response = new CommonResponse();
		try {
			response.setResult(weddingPlatformService.listMyOrders(token));
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		return ResponseEntity.ok(response);
	}

	// ───────────────────────── 관리자 (계정 발급) ─────────────────────────

	@PostMapping("/admin/customers")
	public ResponseEntity<?> adminCreateCustomer(@RequestHeader("X-Admin-Key") String adminKey,
												  @RequestBody Map<String, Object> param) throws Exception {
		CommonResponse response = new CommonResponse();
		try {
			response.setResult(weddingPlatformService.adminCreateCustomer(adminKey, param));
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/admin/orders")
	public ResponseEntity<?> adminListOrders(@RequestHeader("X-Admin-Key") String adminKey) throws Exception {
		CommonResponse response = new CommonResponse();
		try {
			List<ProductOrderDTO> result = weddingPlatformService.adminListOrders(adminKey);
			response.setResult(result);
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		return ResponseEntity.ok(response);
	}
}
