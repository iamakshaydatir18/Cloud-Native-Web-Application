##RDS Sequrity Group
resource "aws_security_group" "db_sg" {
  name        = "db-security-group"
  description = "Security group for the RDS database instance"
  vpc_id      = aws_vpc.main.id

  # Ingress rule for MySQL/MariaDB on port 3306 (Remove if using PostgreSQL)
  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.application_sg.id]
  }

  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.lambda_sg.id] # Allow Lambda's security group
  }

  # Egress rule - Allow all outbound traffic (modify as per requirements)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1" # Allows all protocols
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "db-security-group"
  }
}


# Create an RDS Parameter Group
resource "aws_db_parameter_group" "db_param_group" {
  name        = "csye6225-db-parameter-group"
  family      = "${var.db_engine}${var.db_engine_version}" # Family should match engine and version
  description = "Custom parameter group for ${var.db_engine}"

  # Define your custom parameters here if needed
  parameter {
    name  = "max_connections"
    value = "100" # Example parameter
  }

  tags = {
    Name = "csye6225-db-parameter-group"
  }
}

# Create a DB Subnet Group for RDS
resource "aws_db_subnet_group" "csye6225_db_subnet_group" {
  name       = "csye6225-db-subnet-group"
  subnet_ids = aws_subnet.private[*].id # Reference all private subnets

  tags = {
    Name = "csye6225-db-subnet-group"
  }
}

#data for retriving secrets
data "aws_secretsmanager_secret" "data_db_password" {
  name = aws_secretsmanager_secret.db_password.name
}

data "aws_secretsmanager_secret_version" "data_db_password_version" {
  secret_id = data.aws_secretsmanager_secret.data_db_password.id
}


# Create the RDS Instance
resource "aws_db_instance" "csye6225_rds" {
  identifier             = "csye6225"
  engine                 = var.db_engine
  engine_version         = var.db_engine_version
  instance_class         = var.db_instance_class
  allocated_storage      = 20
  username               = var.db_master_username
  password               = jsondecode(data.aws_secretsmanager_secret_version.data_db_password_version.secret_string).password
  db_subnet_group_name   = aws_db_subnet_group.csye6225_db_subnet_group.name
  vpc_security_group_ids = [aws_security_group.db_sg.id]
  multi_az               = false
  publicly_accessible    = false
  db_name                = var.db_name
  skip_final_snapshot    = true
  deletion_protection    = false

  # Reference the custom parameter group created earlier
  parameter_group_name = aws_db_parameter_group.db_param_group.name

  storage_encrypted = true
  kms_key_id        = aws_kms_key.rds_key.arn

  # Backup configuration (optional, can adjust as needed)
  backup_retention_period = 7             # Keep backups for 7 days
  backup_window           = "07:00-09:00" # Daily backup window

  tags = {
    Name = "csye6225-rds-instance"
  }
}