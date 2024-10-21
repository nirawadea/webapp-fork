#!/bin/bash

set -e

# Set non-interactive mode for apt installations
export DEBIAN_FRONTEND=noninteractive

# Ensure environment variables (DB credentials and other secrets) are set from GitHub Secrets or passed in
#DB_USERNAME=${DB_USERNAME:-}
#DB_PASSWORD=${DB_PASSWORD:-}
#DATABASE_NAME=${DATABASE_NAME:-}
#DATABASE_ENDPOINT=${DATABASE_ENDPOINT:-}

# Log environment variables for debugging (hide DB_PASSWORD for security reasons)
#echo "DB_USERNAME: $DB_USERNAME"
#echo "DATABASE_NAME: $DATABASE_NAME"
#echo "DATABASE_ENDPOINT: $DATABASE_ENDPOINT"

# Ensure all required environment variables are set
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

# Install MySQL client tools instead of MySQL server (since you're using an RDS instance)
echo "Installing MySQL client tools..."
sudo apt-get install -y mysql-client

# Remove MySQL server installation section as you're using RDS, not a local MySQL server.

# Ensure the application runs as a no-login user
echo "Creating a no-login user 'csye6225'..."
sudo adduser --disabled-password --gecos "" --shell /usr/sbin/nologin csye6225

# Ensure /opt/cloudApp directory exists and set permissions
sudo mkdir -p /opt/cloudApp

# Move the application JAR file and set ownership
echo "Moving application JAR file..."
sudo mv /tmp/CloudApplication-0.0.1-SNAPSHOT.jar /opt/cloudApp/
sudo chown -R csye6225:csye6225 /opt/cloudApp
sudo chmod 755 /opt/cloudApp/CloudApplication-0.0.1-SNAPSHOT.jar

# Ensure the log file is writable by csye6225
echo "Setting up application log file..."
sudo touch /var/log/CloudApplication.log
sudo chown csye6225:csye6225 /var/log/CloudApplication.log

# Set environment variables in /etc/environment
#echo "Setting environment variables in /etc/environment..."
#echo "DATABASE_ENDPOINT=${DATABASE_ENDPOINT}" | sudo tee -a /etc/environment
#echo "DATABASE_NAME=${DATABASE_NAME}" | sudo tee -a /etc/environment
#echo "DB_USERNAME=${DB_USERNAME}" | sudo tee -a /etc/environment
#echo "DB_PASSWORD=${DB_PASSWORD}" | sudo tee -a /etc/environment

# Move service file to systemd folder
echo "Moving systemd service file..."
sudo mv /tmp/csye6225.service /etc/systemd/system/

# Reload systemd to pick up the new service
echo "Reloading systemd..."
sudo systemctl daemon-reload

# Enable and start the service
echo "Enabling and starting the service..."
sudo systemctl enable csye6225.service
sudo systemctl start csye6225.service

# Check the status of the service
echo "Checking service status..."
sudo systemctl status csye6225.service

echo "Setup complete!"