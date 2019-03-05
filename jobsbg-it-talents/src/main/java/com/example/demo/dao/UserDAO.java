package com.example.demo.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.Registration;
import com.example.demo.model.User;

@Component
public class UserDAO {
	private static final String SELECT_ALL_USERS_QUERRY = "select u.use_reg_id, u.first_name, u.last_name, r.email, r.password\r\n" + 
			"from users u \r\n" + 
			"left join registrations r on(u.use_reg_id = r.registration_id)";
	private JdbcTemplate jdbcTemplate;
		
	public List<UserDTO> getAllUsers() throws SQLException{
		Connection con = jdbcTemplate.getDataSource().getConnection();		
		ResultSet rs = con.createStatement()
				.executeQuery(SELECT_ALL_USERS_QUERRY + ";");
		
		List<UserDTO> users = new LinkedList<>();
		while(rs.next()) {
			users.add(new UserDTO(rs.getLong(1), rs.getString(2),
					rs.getString(3), rs.getString(4), rs.getString(5)));
		}		
		
		return users;
	}
	
	public UserDTO getUserById(long id) throws SQLException {
		Connection con = jdbcTemplate.getDataSource().getConnection();		
		ResultSet rs = con.createStatement()
				.executeQuery(SELECT_ALL_USERS_QUERRY
						+ "where u.use_reg_id = '"+id+"';");
		rs.next();
		return new UserDTO(rs.getLong(1), rs.getString(2),
				rs.getString(3), rs.getString(4),rs.getString(5));
	}
	
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	

}