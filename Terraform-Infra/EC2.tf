# Security Group for EC2
resource "aws_security_group" "application_sg" {
  name        = "application-sg"
  description = "Security group for web application"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port       = var.server_port
    to_port         = var.server_port
    protocol        = "tcp"
    security_groups = [aws_security_group.lb_security_group.id]
  }

  egress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "application-sg"
  }
}


data "aws_ami" "latest_webapp_ami" {
  most_recent = true

  filter {
    name   = "name"
    values = ["webapp-*"]
  }
}



# Launch Template for EC2
resource "aws_launch_template" "web_app_launch_template" {
  name          = "web_app_launch_template"
  image_id      = data.aws_ami.latest_webapp_ami.id
  instance_type = "t2.micro"

  iam_instance_profile {
    name = aws_iam_instance_profile.ec2_instance_profile.id
  }

  user_data = base64encode(templatefile("${path.module}/user_data.tpl", {
    db_host                         = aws_db_instance.csye6225_rds.endpoint
    db_name                         = var.db_name
    db_user                         = var.db_master_username
    secrets_manager_db_password_arn = aws_secretsmanager_secret.db_password.arn
    server_port                     = var.server_port
    s3_bucket_name                  = aws_s3_bucket.my_bucket.bucket
    aws_region                      = var.region
    TOPIC_ARN                       = aws_sns_topic.email_verify_topic.arn
  }))

  network_interfaces {
    associate_public_ip_address = true
    security_groups             = [aws_security_group.application_sg.id]
  }

  depends_on = [aws_kms_key.ec2_key]

  block_device_mappings {
    device_name = "/dev/xvda"
    ebs {
      volume_size = 20
      volume_type = "gp2"
      encrypted   = true
      kms_key_id  = aws_kms_key.ec2_key.arn
    }
  }
}
