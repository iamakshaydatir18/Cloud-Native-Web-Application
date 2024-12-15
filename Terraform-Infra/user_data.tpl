#!/bin/bash
exec > >(tee /var/log/user-data.log | logger -t user-data -s 2>/dev/console) 2>&1
echo "Starting user data script execution..."


# Update system and install dependencies
echo "Updating package lists and installing dependencies..." >> /tmp/user_data.log
sudo apt-get update -y
sudo apt-get install -y jq unzip curl || {
    echo "Failed to install jq, unzip, or curl. Exiting." >> /tmp/user_data.log
    exit 1
}


# Set environment variables for the application
echo "DB_HOST=${db_host}" | sudo tee -a /etc/environment
echo "SERVER_PORT=${server_port}" | sudo tee -a /etc/environment
echo "DB_NAME=${db_name}" | sudo tee -a /etc/environment
echo "DB_USER=${db_user}" | sudo tee -a /etc/environment
# SNS Topic
echo "TOPIC_ARN=${TOPIC_ARN}" | sudo tee -a /etc/environment
# Environment variables for S3 access
echo "S3_BUCKET_NAME=${s3_bucket_name}" | sudo tee -a /etc/environment
echo "AWS_REGION=${aws_region}" | sudo tee -a /etc/environment

echo "Secret ARN: ${secrets_manager_db_password_arn}" >> /tmp/user_data.log

# Install AWS CLI
echo "Installing AWS CLI..." >> /tmp/user_data.log
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "/tmp/awscliv2.zip" || {
    echo "Failed to download AWS CLI installer. Exiting." >> /tmp/user_data.log
    exit 1
}
unzip /tmp/awscliv2.zip -d /tmp || {
    echo "Failed to unzip AWS CLI installer. Exiting." >> /tmp/user_data.log
    exit 1
}
sudo /tmp/aws/install || {
    echo "AWS CLI installation failed. Exiting." >> /tmp/user_data.log
    exit 1
}


# Verify AWS CLI installation
if ! command -v aws &> /dev/null; then
    echo "AWS CLI installation verification failed. Exiting." >> /tmp/user_data.log
    exit 1
fi  

echo "AWS CLI installed successfully." >> /tmp/user_data.log


# Fetch database password from AWS Secrets Manager
echo "Attempting to fetch database password..." >> /tmp/user_data.log
DB_PASSWORD=$(aws secretsmanager get-secret-value \
  --secret-id "${secrets_manager_db_password_arn}" \
  --query SecretString \
  --output text 2>> /tmp/user_data_error.log | jq -r '.password')

# Log the output to see what the command returns
echo "AWS CLI returned: $DB_PASSWORD" >> /tmp/user_data.log

if [ -z "$DB_PASSWORD" ]; then
  echo "Error: Failed to retrieve DB password. Check the AWS CLI command output." >> /tmp/user_data.log
  exit 1
else
  echo "Database password retrieved successfully." >> /tmp/user_data.log
fi

echo "Database_password = $DB_PASSWORD." >> /tmp/user_data.log

echo "Attempting to add DB_PASSWORD to /etc/environment..." >> /tmp/user_data.log
echo "DB_PASSWORD=$DB_PASSWORD" | sudo tee -a /etc/environment >> /tmp/user_data.log
echo "DB_PASSWORD added to /etc/environment." >> /tmp/user_data.log

source /etc/environment

# Update the systemd service file to load environment variables
sudo mkdir -p /etc/systemd/system/springboot-app.service.d  # Ensure the directory exists
cat <<EOT | sudo tee /etc/systemd/system/springboot-app.service.d/override.conf
[Service]
EnvironmentFile=/etc/environment
EOT

# Add after setting environment variables
echo "Verifying environment variables..."
source /etc/environment
env | grep -E "DB_|SERVER_PORT|TOPIC_ARN|S3_BUCKET_NAME|AWS_REGION"

# CloudWatch Agent configuration
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json -s

# Reload systemd and restart the application
sudo systemctl daemon-reload
sudo systemctl restart springboot-app
