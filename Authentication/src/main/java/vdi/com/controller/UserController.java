package vdi.com.controller;


import vdi.com.entity.AuthRequest;
import vdi.com.entity.UserInfo;
import vdi.com.logout.BlackList;
import vdi.com.repository.UserInfoRepository;
import vdi.com.service.JwtService;
import vdi.com.service.UserInfoService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200/") 
@RequestMapping("/auth")
public class UserController {
  @Autowired
  private UserInfoService userInfoService;
  @Autowired
  private UserInfoRepository userInfoRepository;
  @Autowired
  private AuthenticationManager authenticationManager;
  @Autowired
  private JwtService jwtService;

  @Autowired
  private BlackList blackList;
  String jwtToken = " ";
  
 
 @PostMapping("/addUser")
  public ResponseEntity<Object> registerUser(@RequestBody UserInfo userInfo) {
      Map<String, Object> response = new HashMap<>();   
      try {
          if (userInfoService.existsByUsername(userInfo.getUserName())) {
              response.put("msg", "Username already exists");
              response.put("success", false);
              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
          }
          if (userInfoService.existsByEmail(userInfo.getEmail())) {
              response.put("msg", "Email already exists");
              response.put("success", false);
              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
          }
          UserInfo registeredUser = userInfoService.addUser(userInfo);
          response.put("msg", "User registered successfully");
          response.put("success", true);
          response.put("user", registeredUser);
          return ResponseEntity.ok(response);
      } catch (IllegalArgumentException e) {
          response.put("msg", e.getMessage());
          response.put("success", false);
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      } catch (Exception e) {
          response.put("msg", "Internal Server Error");
          response.put("success", false);
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
      }
  }
 

  @PostMapping("/login")
  public Map<String,Object> login(@RequestBody AuthRequest authRequest){
  	Map<String,Object> response = new HashMap<>();        
      try {
       authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword()));

//      if(authenticate.isAuthenticated()){
           jwtToken= jwtService.generateToken(authRequest.getUserName());
           response.put("success",true);
           response.put("token", jwtToken);
           response.put("msg","login ok");
//           UserDetails userInfo = userInfoService.loadUserByUsername(authRequest.getUserName());
           response.put("user",userInfoRepository.findByUserName(authRequest.getUserName()));

      }catch(Exception e) {
      	System.out.println("Invalid user request");
      	response.put("success",false);
      	response.put("msg","Invalid user request");
      }
		return response;
  }
 
 
 
 
  @PostMapping("/logout")
  @PreAuthorize("hasAuthority('USER_ROLES') or hasAuthority('ADMIN_ROLES')")
  public String logoutUser(HttpServletRequest request){
      String authHeader = request.getHeader("Authorization");
      String token= null;
      if(authHeader !=null && authHeader.startsWith("Bearer")){
          token = authHeader.substring(7);
      }
      blackList.blacKListToken(token);
      return "You have successfully logged out !!";
  }

  @GetMapping("/getUsers")
  @PreAuthorize("hasAuthority('ADMIN_ROLES') or hasAuthority('USER_ROLES')")
  public List<UserInfo> getAllUsers(){
      return userInfoService.getAllUser();
  }
  @GetMapping("/getUsers/{id}")
  @PreAuthorize("hasAuthority('USER_ROLES')")
  public UserInfo getAllUsers(@PathVariable Integer id){
      return userInfoService.getUser(id);
  }
  

//update user details in profile page
@PutMapping("/{id}")
public ResponseEntity<Object> updateUser(@PathVariable("id") Integer id, @RequestBody UserInfo userInfo) {

//public ResponseEntity<Object> updateUser(@PathVariable Integer id, @RequestBody UserInfo userInfo) {
    Map<String, Object> response = new HashMap<>();
    try {
        UserInfo existingUser = userInfoService.getUser(id);
        if (existingUser == null) {
            response.put("msg", "User not found");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Update the existing user's details
        existingUser.setAssociateId(userInfo.getAssociateId());
        existingUser.setUserName(userInfo.getUserName());
        existingUser.setEmail(userInfo.getEmail());
        existingUser.setContactNo(userInfo.getContactNo());
        // Add other fields as necessary

        UserInfo updatedUser = userInfoService.updateUser(existingUser);
        response.put("msg", "User updated successfully");
        response.put("success", true);
        response.put("user", updatedUser);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        response.put("msg", "Internal Server Error");
        response.put("success", false);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
   
  
 

}