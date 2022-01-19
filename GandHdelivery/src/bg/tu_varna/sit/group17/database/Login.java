package bg.tu_varna.sit.group17.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import bg.tu_varna.sit.group17.application.Load;
import bg.tu_varna.sit.group17.application.LoggerApp;
import bg.tu_varna.sit.group17.application.MessageBox;
import bg.tu_varna.sit.group17.database.users.Consumer;
import bg.tu_varna.sit.group17.database.users.User;
import bg.tu_varna.sit.group17.database.users.UserCreator;
import bg.tu_varna.sit.group17.validation.Valid;

public final class Login {
	
	private final LoggerApp logger = new LoggerApp(getClass().getName());
	private final MessageBox message = new MessageBox(logger);
	private Load load;
	
	public Login(Load load) {
		this.load = load;
	}

	public void user(String phoneNumber, String password) {
		ResultSet record = null;
		try {
			if (!Valid.phoneNumber(phoneNumber) || !Valid.password(password)) {
				throw new IllegalArgumentException("Invalid phone or password " + phoneNumber + " " + password);
			}
			
			for (User user : User.values()) {
				if (user == User.Guest)
					continue;
				String tableName = user.getTableName();
				record = TableQuery.execute("select * from " + tableName + " where phone='" + phoneNumber
						+ "' and password='" + password + "'");

				if (record != null) {
					loadForm(user, record);
					return;
				}
			}
			if (record == null)
				throw new IllegalArgumentException("Invalid phone or password " + phoneNumber + " " + password);
			
		} catch (SQLException | IllegalArgumentException e) {
			message.alert("Невалидни телефон и/или парола");
			logger.error(e.getMessage());
		}
	}
	
	public void loadForm(User user, ResultSet userResult) throws SQLException {
		
		final Consumer consumer = UserCreator.create(user, userResult);
		load.form(user.getFormName(), consumer);
	}
}