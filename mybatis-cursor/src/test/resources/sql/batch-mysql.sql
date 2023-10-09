CREATE TABLE user_mysql (
	id INT PRIMARY KEY,
	username VARCHAR(50),
	email VARCHAR(100),
	password VARCHAR(100),
	first_name VARCHAR(50),
	last_name VARCHAR(50),
	address VARCHAR(200),
	city VARCHAR(50),
	state VARCHAR(50),
	zip_code VARCHAR(10),
	country VARCHAR(50),
	phone_number VARCHAR(50),
	date_of_birth DATE,
	gender VARCHAR(10),
	occupation VARCHAR(100),
	education_level VARCHAR(50),
	registration_date DATETIME,
	last_login DATETIME,
	is_active TINYINT(1),
	is_admin TINYINT(1),
	additional_field1 VARCHAR(100),
	additional_field2 VARCHAR(100)
);