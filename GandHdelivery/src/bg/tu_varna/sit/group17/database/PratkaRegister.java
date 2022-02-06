package bg.tu_varna.sit.group17.database;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedList;

import bg.tu_varna.sit.group17.application.Load;
import bg.tu_varna.sit.group17.application.LoggerApp;
import bg.tu_varna.sit.group17.application.MessageBox;
import bg.tu_varna.sit.group17.application.Property;
import bg.tu_varna.sit.group17.controllers.PratkaRegisterController;
import bg.tu_varna.sit.group17.database.property.Company;
import bg.tu_varna.sit.group17.database.users.Consumer;
import bg.tu_varna.sit.group17.validation.Valid;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public final class PratkaRegister {
	private final LoggerApp logger = new LoggerApp(getClass().getName());
	private final MessageBox message = new MessageBox(logger);

	private LinkedList<Company> companies;
	private Company company;

	private Consumer consumer;
	private Load load;

	private ComboBox<String> category, officeSender, officeReceiver, companySender;
	private Button notificationBell;
	private TextField phoneSender, phoneReceiver, sendPrice, address;
	private DatePicker receiveDate, clientReceiveDate;
	private CheckBox fragile, sendToAddress, isPaid;

	public PratkaRegister(PratkaRegisterController contr) {
		this.companies = new LinkedList<>();
		this.company = new Company();
		this.consumer = contr.consumer;
		this.load = contr.load;

		this.category = contr.category;
		this.officeSender = contr.officeSender;
		this.officeReceiver = contr.officeReceiver;
		this.companySender = contr.companySender;
		this.notificationBell = contr.notificationBell;
		this.phoneSender = contr.phoneSender;
		this.phoneReceiver = contr.phoneReceiver;
		this.sendPrice = contr.sendPrice;
		this.address = contr.address;
		this.receiveDate = contr.receiveDate;
		this.clientReceiveDate = contr.clientReceiveDate;
		this.fragile = contr.fragile;
		this.sendToAddress = contr.sendToAddress;
		this.isPaid = contr.isPaid;
	}

	public void prepareForm() {
		try {
			courierNotification(notificationBell);

			companies = TableQuery.allCompanies();

			if (companySender.getItems().size() == 0) {
				for (Company c : companies)
					if (c.offices.size() != 0)
						companySender.getItems().add(c.getName());

				logger.info("Changed firma combobox");
				companySender.valueProperty().addListener(firmaListener());
			}

			String sql = "select category from categories";
			ResultSet rs = TableQuery.execute(sql);
			if (rs == null) {
				message.alert("Не е избрана категория");
				return;
			}

			logger.info("Changed category combobox");
			this.category.valueProperty().addListener(categoryListener());

			do {
				this.category.getItems().add(rs.getString("category"));
			} while (rs.next());
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	private void courierNotification(Button notificationBell) {
		try {
			load.notification.courier(consumer, notificationBell);
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	private void validFields(double price, String phoneSender, String phoneReceiver, String address,
			boolean sendToAddress, LocalDate receiveDate, LocalDate clientLocalDate) {

		if (this.sendPrice == null) {
			throw new IllegalArgumentException("Полето за цена не може да е празно");
		}
		try {
			price = Double.parseDouble(this.sendPrice.getText());
		} catch (NullPointerException e) {
			logger.error(e.getMessage());
			throw new IllegalArgumentException("Невалидна цена");
		}
		if (!Valid.order(phoneSender, phoneReceiver, this.receiveDate.getValue().toString(),
				this.clientReceiveDate.getValue().toString())) {
			throw new IllegalArgumentException("Невалиден телефон на клиент");
		}

		if (!sendToAddress && officeSender.getSelectionModel().getSelectedItem()
				.equals(officeReceiver.getSelectionModel().getSelectedItem())) {
			throw new IllegalArgumentException("Не може да се достави до същия офис");
		}
		if (sendToAddress && address.equals("")) {
			throw new IllegalArgumentException("Полето адрес не може да е празно");
		}

		if (Date.valueOf(receiveDate).after(Date.valueOf(clientLocalDate))) {
			throw new IllegalArgumentException("Не може да се изпрати, преди дата на получаване");
		}
	}

	public void registerOrder() {

		String phoneSender = this.phoneSender.getText(), phoneReceiver = this.phoneReceiver.getText(),
				address = this.address.getText();
		LocalDate receiveDate = this.receiveDate.getValue(), clientLocalDate = this.clientReceiveDate.getValue();
		try {
			if (receiveDate == null || clientLocalDate == null) {
				throw new IllegalArgumentException("Полетата за дати не може да са празни");
			}

			boolean fragile = this.fragile.isSelected(), sendToAddress = this.sendToAddress.isSelected(),
					isPaid = this.isPaid.isSelected();
			double price = 0;

			validFields(price, phoneSender, phoneReceiver, address, sendToAddress, receiveDate, clientLocalDate);

			int id_category = TableQuery.getCategoryId(category.getSelectionModel().getSelectedItem());
			int id_office_sender = TableQuery.getOfficeId(officeSender.getSelectionModel().getSelectedItem()),
					id_office_recipient = TableQuery.getOfficeId(officeReceiver.getSelectionModel().getSelectedItem()),
					id_customer_sender = TableQuery.getCustomerId(phoneSender),
					id_customer_recipient = TableQuery.getCustomerId(phoneReceiver), id_courier = consumer.getId();

			final int id_status = 1;

			if (id_category == 0)
				return;
			Add.order(id_category, id_office_sender, id_office_recipient, id_customer_sender, id_customer_recipient,
					id_courier, id_status, fragile, isPaid, price, sendToAddress, address, Date.valueOf(receiveDate),
					Date.valueOf(clientLocalDate));

			message.alert("Успешно добавена поръчка");
			logger.info("Successful added order");
		} catch (SQLException e) {
			logger.error(e.getMessage());
		} catch (IllegalArgumentException e) {
			message.alert(e.getMessage());
			logger.error(e.getMessage());
		}
	}

	private ChangeListener<String> firmaListener() {
		return new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

				officeSender.getItems().clear();
				officeReceiver.getItems().clear();

				company = new Company();

				if (newValue == null)
					return;

				company.setId(Property.companiesMap.get(newValue));
				company.setName(newValue);

				try {
					for (int i = 0; i < companies.size(); ++i) {
						for (int j = 0; j < companies.get(i).offices.size(); ++j)
							officeSender.getItems().add(companies.get(i).offices.get(j).getFullAddress());
					}
				} catch (SQLException e) {
					logger.error(e.getMessage());
				}
				officeReceiver.getItems().addAll(officeSender.getItems());
			}
		};
	}

	private ChangeListener<String> categoryListener() {
		return new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				int id_category = category.getSelectionModel().getSelectedIndex() + 1;

				try {
					String sql = "select price from price_list where id_company='" + company.getId()
							+ "' and id_category='" + id_category + "'";
					ResultSet rs = TableQuery.execute(sql);
					if (rs == null) {
						throw new IllegalArgumentException("Фирмата няма цени");
					}

					sendPrice.setText(Double.toString(rs.getDouble("price")));

				} catch (SQLException e) {
					logger.error(e.getMessage());
				} catch (IllegalArgumentException e) {
					message.alert(e.getMessage());
					logger.error(e.getMessage());
				}
			}
		};
	}
}