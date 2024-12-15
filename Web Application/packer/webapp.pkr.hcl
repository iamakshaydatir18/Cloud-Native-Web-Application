packer {
  required_plugins {
    amazon = {
      version = ">= 1.2.8"
      source  = "github.com/hashicorp/amazon"
    }
  }
}


# Define source image from AWS (Ubuntu 24.04 LTS)
source "amazon-ebs" "my-ami" {
  ami_name        = "webapp-${formatdate("YYYY_MM_DD-HHmmss", timestamp())}"
  instance_type   = "t2.small"
  region          = var.region # Specify your region
  ami_description = "AMI for CSYE 6225"
  ami_regions     = [var.region]

  aws_polling {
    delay_seconds = 120
    max_attempts  = 50
  }

  source_ami                  = var.source_ami
  ssh_username                = var.ssh_username
  subnet_id                   = var.default_subnet
  vpc_id                      = var.default_vpc
  associate_public_ip_address = true
}

# Build and provisioners
build {
  sources = ["source.amazon-ebs.my-ami"]

  # Copy the WAR file to the instance
  provisioner "file" {
    source      = "/home/runner/work/webapp/webapp/ROOT.jar" # Ensure path matches the runner’s file location
    destination = "/tmp/ROOT.jar"
  }

  # Install dependencies, create user, and set permissions
  provisioner "shell" {
    inline = [
      # Create non-login user csye6225
      "sudo useradd -r -s /sbin/nologin ${var.csye6225}",
      "sudo apt-get update",
      "sudo apt-get upgrade -y",

      # Install Java 17
      "sudo apt-get install -y openjdk-17-jdk",

      # Move the JAR file to /opt
      "sudo mv /tmp/ROOT.jar /opt/ROOT.jar",
      "sudo chown ${var.csye6225}:${var.csye6225} /opt/ROOT.jar",
      "sudo chmod 755 /opt/ROOT.jar",

      # Create systemd service
      "echo '[Unit]' | sudo tee /etc/systemd/system/springboot-app.service",
      "echo 'Description=Spring Boot Application' | sudo tee -a /etc/systemd/system/springboot-app.service",
      "echo 'After=network.target' | sudo tee -a /etc/systemd/system/springboot-app.service",

      "echo '[Service]' | sudo tee -a /etc/systemd/system/springboot-app.service",
      "echo 'User=${var.csye6225}' | sudo tee -a /etc/systemd/system/springboot-app.service",
      "echo 'Type=simple' | sudo tee -a /etc/systemd/system/springboot-app.service",
      "echo 'EnvironmentFile=/etc/environment' | sudo tee -a /etc/systemd/system/springboot-app.service",
      "echo 'ExecStart=/usr/bin/java -jar /opt/ROOT.jar' | sudo tee -a /etc/systemd/system/springboot-app.service",
      "echo 'SuccessExitStatus=143' | sudo tee -a /etc/systemd/system/springboot-app.service",
      "echo 'Restart=always' | sudo tee -a /etc/systemd/system/springboot-app.service",
      "echo 'RestartSec=10' | sudo tee -a /etc/systemd/system/springboot-app.service",
      "echo 'TimeoutStartSec=180' | sudo tee -a /etc/systemd/system/springboot-app.service",
      "echo 'WorkingDirectory=/opt' | sudo tee -a /etc/systemd/system/springboot-app.service",

      "echo '[Install]' | sudo tee -a /etc/systemd/system/springboot-app.service",
      "echo 'WantedBy=multi-user.target' | sudo tee -a /etc/systemd/system/springboot-app.service",

      # Enable and start Spring Boot service
      "sudo systemctl daemon-reload",
      "sudo systemctl enable springboot-app",
      "sudo systemctl start springboot-app",

      # Clean up unnecessary files
      "sudo apt-get clean"
    ]
  }

  # Install the CloudWatch Agent
  provisioner "file" {
    source      = "packer/scripts/setup-cloudwatch-agent.sh"
    destination = "/tmp/setup-cloudwatch-agent.sh"
  }

  # Run the CloudWatch setup script
  provisioner "shell" {
    inline = [
      "chmod +x /tmp/setup-cloudwatch-agent.sh",
      "sudo /tmp/setup-cloudwatch-agent.sh"

    ]
  }

  #for app.log file
  provisioner "shell" {
    inline = [
      # Existing commands

      # Create the log directory and log file
      "sudo mkdir -p /var/log",
      "sudo touch /var/log/springboot-app.log",
      "sudo chown ${var.csye6225}:${var.csye6225} /var/log/springboot-app.log",
      "sudo chmod 644 /var/log/springboot-app.log",

    ]
  }


  # Clean up
  provisioner "shell" {
    inline = [
      # Restart the application to ensure everything is set up properly
      "sudo systemctl restart springboot-app"
    ]
  }
}
