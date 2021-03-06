package com.example.demo.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.CompanyDAO;
import com.example.demo.dto.CompanyDTO;
import com.example.demo.dto.CompanyProfileDTO;
import com.example.demo.dto.EditOfferDTO;
import com.example.demo.dto.EditProfileCompanyDTO;
import com.example.demo.dto.EditUserProfileDTO;
import com.example.demo.dto.OfferDTO;
import com.example.demo.dto.UserProfileDTO;
import com.example.demo.exceptions.InvalidInputException;
import com.example.demo.exceptions.InvalidOfferOwnerException;
import com.example.demo.exceptions.NoSuchElementException;
import com.example.demo.exceptions.NotOfferFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.interfaces.IRegistrationLogin;

@RestController
public class CompanyController implements IRegistrationLogin{

	@Autowired
	private CompanyDAO companyDao;
	/**
	 * Add offer only if you are logged as company
	 */
	
	@PostMapping("/companyProfile/addOffer")
	public long addOffer(@RequestBody OfferDTO offer,HttpServletRequest request, HttpServletResponse response) {
		
		try {
			HttpSession session = request.getSession();
			
			long id = (long) session.getAttribute("userId");
			CompanyProfileDTO companyCheck = companyDao.getCompanyById(id);
			int bulstat = 0;
			
			bulstat = companyCheck.getBulstat();
			System.out.println("BULSTAT" + bulstat);
			
			if(!isLogged(session) || bulstat==0 ) {
				response.setStatus(401);
				throw new UnauthorizedException("Unauthorized");
			}
			return this.companyDao.addNewOffer(offer,id);
		} catch (Exception e) {
			System.out.println("Exception in CompanyController addOffer method");
			response.setStatus(401);
			return 0;
		}
	}
	/**
	 * Edit offer by offer ID from Postman (only if you are logged as company)
	 * 
	 */
	
	@PutMapping("/companyProfile/editOffer/{offerId}")
	public void editOffer(@PathVariable Long offerId, @RequestBody EditOfferDTO offer, HttpServletRequest request, HttpServletResponse response) throws NoSuchElementException, NotOfferFoundException, SQLException, InvalidOfferOwnerException, InvalidInputException{
		try {
			HttpSession session = request.getSession();
			
			if(!isLogged(session)) {
				response.setStatus(401);
				return;
			}
			
			if(!companyDao.isValidOfferOwning(offerId).equals(session.getAttribute("userId"))) {
				throw new InvalidOfferOwnerException("Offer edition is unabled");
			}
			companyDao.editOffer(offer,offerId);
			
			
		} catch(NullPointerException e) {
			throw new NoSuchElementException("Session expired");
		} catch (NotOfferFoundException e) {
			throw new NotOfferFoundException("Not offer with this id");
		}
	}
	/**
	 * Get all companies in jobsBg
	 */
	@GetMapping("/companies")
	public List<CompanyDTO> getAllCompanies(){
		try {
			return this.companyDao.getAllCompanies();
		} catch (SQLException e) {
			e.printStackTrace();
			return new LinkedList<>();
		}
	}
	/**
	 * Get company by ID
	 */
	@GetMapping("/companies/{companyId}")
	public CompanyProfileDTO getCompanyDetails(@PathVariable long companyId) throws SQLException, NoSuchElementException {
			return companyDao.getCompanyById(companyId);
	}
	/**
	 * Get current logged company's profile details
	 */
	@GetMapping("/companyProfile")
	public CompanyProfileDTO getCompanyProfile(HttpServletRequest request, HttpServletResponse response) throws NoSuchElementException, IOException {
		response.getWriter().println("Login Successful");	
		try {
			HttpSession session = request.getSession();
			long id = (long) session.getAttribute("userId");
			CompanyProfileDTO companyCheck = companyDao.getCompanyById(id);
			int bulstat = 0;
			bulstat = companyCheck.getBulstat();
		
			if(!isLogged(session) || bulstat==0) {
				response.setStatus(401);
				throw new NoSuchElementException("Not allowed");
			}
			//long id = (long) session.getAttribute("userId");
			System.out.println(id);
			return companyDao.getCompanyProfile(id);
		}
		catch (SQLException e) {
			System.out.println("SQL exception in getCompanyProfile in CompanyController");
			return null;
		}  catch(NullPointerException e) {
			throw new NoSuchElementException("Session expired");
		}
	}
	
	/**
	 * Editing your company profile (only if you are logged as this company)
	 */
	
	@PutMapping("/editCompanyProfile")
	public void editProfile(@RequestBody EditProfileCompanyDTO company, HttpServletRequest request, HttpServletResponse response) throws NoSuchElementException  {
		try {
			HttpSession session = request.getSession();
			
			if(!isLogged(session)) {
				response.setStatus(401);
				return;
			}
			
			long id = (long) session.getAttribute("userId");
			
			companyDao.editProfileCompany(id, company);
			
		} catch(NullPointerException e) {
			throw new NoSuchElementException("Session expired");
		} catch (Exception e) {
			System.out.println("Exception in editProfile method in CompanyController");
			return;
		}
	}
	/**
	 * Delete offer by id (only if you are logged as a company and you have an offer with this id
	 */
	@DeleteMapping("/companyProfile/deleteOffer/{offerId}")
	public void deleteOffer(@PathVariable Long offerId, HttpServletRequest request, HttpServletResponse response) throws NoSuchElementException, NotOfferFoundException, SQLException, InvalidOfferOwnerException {
		try {
			HttpSession session = request.getSession();
			
			if(!isLogged(session)) {
				response.setStatus(401);
				return;
			}
			
			if(!companyDao.isValidOfferOwning(offerId).equals(session.getAttribute("userId"))) {
				throw new InvalidOfferOwnerException("Offer edition is unabled");
				
			}
			companyDao.deleteOffer(offerId);
			
			
			
		} catch(NullPointerException e) {
			throw new NoSuchElementException("Session expired");
		} catch (NotOfferFoundException e) {
			throw new NotOfferFoundException("Not offer with this id");
		}
	}
	
	@GetMapping("/companyProfile/offers")
	public List<OfferDTO> getCompanyOffers(HttpServletRequest request) throws SQLException, UnauthorizedException{
		try {
		HttpSession session = request.getSession();
		long id = (long) session.getAttribute("userId");
		if(!isLogged(session)) {
			throw new UnauthorizedException("Not allowed");
		}
		return companyDao.getCompanyOffers(id);
		}catch(NullPointerException e) {
			throw new UnauthorizedException("Not allowed");
		}
	}
	
	
	
}
