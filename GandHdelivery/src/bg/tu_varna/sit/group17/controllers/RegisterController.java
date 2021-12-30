package bg.tu_varna.sit.group17.controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import bg.tu_varna.sit.group17.application.Launch;
import bg.tu_varna.sit.group17.application.Logger;
import bg.tu_varna.sit.group17.application.Property;
import bg.tu_varna.sit.group17.database.Add;
import bg.tu_varna.sit.group17.database.users.Courier;
import bg.tu_varna.sit.group17.validation.Valid;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public final class RegisterController implements Initializable { 
	
	private final Logger logger = new Logger(RegisterController.class.getName());
	@FXML
	private TextField name, phone, address;
	@FXML
	private ComboBox<String> cBox0;
	@FXML
	private PasswordField password, repeatPassword;
	
	public static Courier courier;
	public static boolean registerCustomer = false;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		logger.info("In register form");
		cBox0.getItems().addAll(Property.citiesMap.keySet());
	}
	
    @FXML
    public void register(ActionEvent e) throws IOException, SQLException {
    	logger.info("Clicked register");
    	String username = this.name.getText(), 
    			phoneNumber = this.phone.getText(),
    			city = this.cBox0.getValue(),
    			location = this.address.getText(),
    			password = this.password.getText(),
    	    	repeatPassword = this.repeatPassword.getText();
    	String err = "";
    	err = Valid.user(username, phoneNumber, password, repeatPassword);
    	if(!err.equals("")) {
    		Launch.alert(err);
    		return;
    	}
    	
    	if(city.equals("град")) {
    		city = "";
    	}
    	
    	Add.customer(username, phoneNumber, Property.citiesMap.get(city), location, password);
    	
    	Launch.alert("Успешна регистрация.");
    	logger.info("Successful register");
    }
    
    @FXML
    public void login(ActionEvent e) throws SQLException, IOException {
    	RegisterController.registerCustomer = false;
    	Launch.launch.loginForm();
    }
    
    @FXML
    private ResourceBundle resources;
    
    @FXML
    private URL location;
}