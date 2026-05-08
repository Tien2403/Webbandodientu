package com.shopme.admin;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.shopme.admin.security.ShopmeUserDetails;


@Controller
public class MainController {

	@GetMapping("")
	public String viewHomePage(Model model, @AuthenticationPrincipal ShopmeUserDetails loggedUser) {	
		
		if(loggedUser.hasRole("Admin") || loggedUser.hasRole("Salesperson")) {
			return "redirect:/reports";
		} else if(loggedUser.hasRole("Assistant")) {
			return "redirect:/reviews";
		} else {
			return "redirect:/products";
		}		
	}
	
	@GetMapping("/login")
	public String viewLoginPage() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "login";
		}
		
		return "redirect:/";
	}
	
}
