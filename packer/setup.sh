#!/bin/bash

set -e

# Set non-interactive mode for apt installations
export DEBIAN_FRONTEND=noninteractive

# Update the package index
echo "Updating package index..."
sudo apt-get update -y

# Install OpenJDK 17
echo "Installing OpenJDK 17..."
sudo apt-get install -y openjdk-17-jdk

# Install Maven
echo "Installing Maven..."
sudo apt-get install -y maven

# Creating a no-login user
echo "Creating a no-login user 'csye6225'..."
sudo adduser csye6225 --shell /usr/sbin/nologin

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
ALTER USER 'root'@'localhost' IDENTIFIED BY 'Safari*@12345';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOF
else
  echo "Creating 'root'@'localhost' user..."
  sudo mysql -u root <<EOF
CREATE USER 'root'@'localhost' IDENTIFIED BY 'Safari*@12345';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOF
fi


# Create database 'webapp'
echo "Creating database 'webapp'..."
sudo mysql -u root <<EOF
CREATE DATABASE IF NOT EXISTS webapp;
EOF

# Restart MySQL service to apply changes
sudo systemctl restart mysql

echo "MySQL setup complete!"

# Move service file to systemd folder
sudo mv /tmp/csye6225.service /etc/systemd/system/

# Set ownership of the application artifact to the non-login user 'csye6225'
sudo chown -R csye6225:csye6225 /opt/CloudApplication-0.0.1-SNAPSHOT.jar

# Reload systemd to pick up new service
sudo systemctl daemon-reload
sudo systemctl enable csye6225.service
sudo systemctl start csye6225.service
sudo systemctl status csye6225.service
