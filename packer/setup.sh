#!/bin/bash

set -e

# Set non-interactive mode for apt installations
export DEBIAN_FRONTEND=noninteractive

# Ensure environment variables (DB credentials and other secrets) are set from GitHub Secrets
DB_USERNAME=${DB_USERNAME:-}
DB_PASSWORD=${DB_PASSWORD:-}
DATABASE_NAME=${DATABASE_NAME:-}
DATABASE_ENDPOINT=${DATABASE_ENDPOINT:-}

echo "DB_USERNAME: $DB_USERNAME"
echo "DB_PASSWORD: $DB_PASSWORD"
echo "DATABASE_NAME: $DATABASE_NAME"
echo "DATABASE_ENDPOINT: $DATABASE_ENDPOINT"

if [[ -z "$DB_USERNAME" || -z "$DB_PASSWORD" || -z "$DATABASE_NAME" || -z "$DATABASE_ENDPOINT" ]]; then
  echo "Error: One or more required environment variables are not set."
  exit 1
fi

# Update the package index
echo "Updating package index..."
sudo apt-get update -y

# Install OpenJDK 17
echo "Installing OpenJDK 17..."
sudo apt-get install -y openjdk-17-jdk

# Install Maven
echo "Installing Maven..."
sudo apt-get install -y maven

# Verify Java installation
echo "Verifying Java installation..."
java -version

# Install MySQL Server
sudo apt-get install -y mysql-server

# Start MySQL service and enable it to start on boot
sudo systemctl start mysql
sudo systemctl enable mysql

# Check if 'root'@'localhost' user exists
user_exists=$(sudo mysql -u root -e "SELECT EXISTS(SELECT 1 FROM mysql.user WHERE user = 'root' AND host = 'localhost');" | tail -n 1)

if [ "$user_exists" == "1" ]; then
  echo "User 'root'@'localhost' already exists. Updating password and privileges..."
  sudo mysql -u root <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOF
else
  echo "Creating 'root'@'localhost' user..."
  sudo mysql -u root <<EOF
CREATE USER 'root'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOF
fi

# Create database
echo "Creating database '${DATABASE_NAME}'..."
sudo mysql -u root <<EOF
CREATE DATABASE IF NOT EXISTS ${DATABASE_NAME};
EOF

# Restart MySQL service to apply changes
sudo systemctl restart mysql

echo "MySQL setup complete!"

# Move service file to systemd folder
sudo mv /tmp/csye6225.service /etc/systemd/system/

# Creating a no-login user (disabled-password)
echo "Creating a no-login user 'csye6225'..."
sudo adduser --disabled-password --gecos "" --shell /usr/sbin/nologin csye6225

# Ensure /opt/cloudApp directory exists and set permissions
sudo mkdir -p /opt/cloudApp

# Move the application JAR file and set ownership
sudo mv /tmp/CloudApplication-0.0.1-SNAPSHOT.jar /opt/cloudApp/
sudo chown -R csye6225:csye6225 /opt/cloudApp
sudo chmod 755 /opt/cloudApp/CloudApplication-0.0.1-SNAPSHOT.jar

# Ensure the log file is writable by csye6225
sudo touch /var/log/CloudApplication.log
sudo chown csye6225:csye6225 /var/log/CloudApplication.log

# Set environment variables in /etc/environment (update these with real values)
echo "Setting environment variables..."
echo "DATABASE_ENDPOINT=${DATABASE_ENDPOINT}" | sudo tee -a /etc/environment
echo "DATABASE_NAME=${DATABASE_NAME}" | sudo tee -a /etc/environment
echo "DB_USERNAME=${DB_USERNAME}" | sudo tee -a /etc/environment
echo "DB_PASSWORD=${DB_PASSWORD}" | sudo tee -a /etc/environment

# Reload systemd to pick up new service
sudo systemctl daemon-reload
sudo systemctl enable csye6225.service
sudo systemctl start csye6225.service
sudo systemctl status csye6225.service
