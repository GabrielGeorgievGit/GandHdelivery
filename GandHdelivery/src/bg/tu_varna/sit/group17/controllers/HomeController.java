package bg.tu_varna.sit.group17.controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import bg.tu_varna.sit.group17.application.Avatar;
import bg.tu_varna.sit.group17.application.FormName;
import bg.tu_varna.sit.group17.application.Load;
import bg.tu_varna.sit.group17.application.LoggerApp;
import bg.tu_varna.sit.group17.database.Home;
import bg.tu_varna.sit.group17.database.queries.Query;
import bg.tu_varna.sit.group17.database.users.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public final class HomeController extends ControllerParent {

	private final LoggerApp logger = new LoggerApp(getClass().getName());

	private Home home;

	@FXML
	private MenuButton userName;
	@FXML
	public ComboBox<String> functions;

	@FXML
	public DatePicker dateFrom, dateTo;

	@FXML
	public TextField phone, IdOrder;

	@FXML
	public TableView<Query> table;

	@FXML
	private ImageView avatar;

	@FXML
	public Button notificationBell, cancelOrderButton;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@Override
	public void initData(Load load, Consumer consumer) {
		this.load = load;
		this.consumer = consumer;
		this.home = new Home(this);

		this.avatar.setImage(Avatar.get());
		this.userName.setText(consumer.getName());
		home.prepareForm();
	}

	@FXML
	void initialize() {
		logger.info("In home form");
	}

	@FXML
	private void changeAvatar() {
		this.avatar.setImage(Avatar.next());
	}

	@FXML
	private void logOut() {
		load.form(FormName.login, consumer);
	}

	@FXML
	private void notificationBellClick() {
		load.notification.apply(this.notificationBell);
	}
	/*
	 * private void noNotification() {
	 * this.notificationBell.setStyle(Notification.izv); }
	 */

	@FXML
	private void registerPratka() throws SQLException, IOException {
		load.form(FormName.pratkaRegister, consumer);
	}

	@FXML
	private void firma() throws SQLException, IOException {
		load.form(FormName.firma, consumer);
	}

	@FXML
	private void cancelOrder() {
		logger.info("Clicked cancel order");
		final int index = functions.getSelectionModel().getSelectedIndex();
		home.cancelOrder(index);
	}

	@FXML
	private void filter() {
		logger.info("Clicked filter");
		this.table.getColumns().clear();
		final int index = functions.getSelectionModel().getSelectedIndex();
		home.filter(index);
	}
}