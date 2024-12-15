# IAM Role for Lambda Function
resource "aws_iam_role" "lambda_exec_role" {
  name = "lambda-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}


# IAM Role Policy for Lambda to access VPC network interface actions
resource "aws_iam_role_policy" "lambda_vpc_access_policy" {
  name = "lambda-vpc-access-policy"
  role = aws_iam_role.lambda_exec_role.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = [
          "rds:Connect",                   # Permission to connect to RDS
          "secretsmanager:GetSecretValue", # Permission to access secrets manager
          "ec2:CreateNetworkInterface",    # Permission to create network interfaces
          "ec2:DescribeNetworkInterfaces", # Permission to describe network interfaces
          "ec2:DeleteNetworkInterface"     # Permission to delete network interfaces
        ]
        Effect   = "Allow"
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue",
          "kms:Decrypt"
        ]
        Resource = [
          aws_secretsmanager_secret.sendgrid_api_key.arn,
          aws_kms_key.sendgrid_key.arn
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue",
          "kms:Decrypt",
          "kms:DescribeKey"
        ]
        Resource = [
          aws_secretsmanager_secret.db_password.arn,
          aws_kms_key.db_password_key.arn
        ]
      }
    ]
  })
}


# Lambda Permission to allow SNS to invoke the Lambda function
resource "aws_lambda_permission" "allow_sns_invoke" {
  statement_id  = "AllowExecutionFromSNS"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.serverless_lambda.function_name
  principal     = "sns.amazonaws.com"
  source_arn    = aws_sns_topic.email_verify_topic.arn
}

# Security Group for Lambda Function
resource "aws_security_group" "lambda_sg" {
  name        = "lambda-security-group"
  description = "Security group for Lambda function to access RDS"
  vpc_id      = aws_vpc.main.id # Use the same VPC as your RDS instance

  # Allow Lambda to make outbound connections (default behavior)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1" # Allows all outbound traffic
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 443 # Allow outbound HTTPS traffic
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "lambda-security-group"
  }
}


# Create a security group for the VPC endpoint
resource "aws_security_group" "secretsmanager_endpoint_sg" {
  name        = "secretsmanager-endpoint-sg"
  description = "Security group for Secrets Manager VPC endpoint"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "secretsmanager-endpoint-sg"
  }

}


resource "aws_vpc_endpoint" "secretsmanager" {
  vpc_id             = aws_vpc.main.id
  service_name       = "com.amazonaws.${var.region}.secretsmanager"
  vpc_endpoint_type  = "Interface"
  subnet_ids         = aws_subnet.private[*].id
  security_group_ids = [aws_security_group.secretsmanager_endpoint_sg.id]
}





resource "aws_lambda_function" "serverless_lambda" {
  function_name = "serverless_lambda_function"
  role          = aws_iam_role.lambda_exec_role.arn
  handler       = "serverless.lambda.function.EmailVerificationFunction::handleRequest"
  runtime       = "java17" # Java runtime

  # Set the timeout and memory size here
  timeout     = 60  # Increase to 10 seconds or more if necessary
  memory_size = 512 # Increase memory size to 256 MB or higher for better performance

  # Reference the Lambda code from the local file
  filename = "${path.module}/serverless/serverless-lambda-function-0.0.1-SNAPSHOT.zip"

  environment {
    variables = {
      DB_USER = var.db_master_username
      #DB_PASSWORD      = var.db_master_password
      DB_NAME = var.db_name
      DB_HOST = aws_db_instance.csye6225_rds.endpoint
      #SENDGRID_API_KEY = var.SENDGRID_API_KEY
      ENVIRONMENT = var.subdomain_env
    }
  }

  # VPC configuration for Lambda to access RDS (using private subnets and security groups)
  vpc_config {
    subnet_ids         = aws_subnet.private[*].id          # Reference to the private subnets
    security_group_ids = [aws_security_group.lambda_sg.id] # Attach the Lambda security group
  }

  lifecycle {
    create_before_destroy = true
  }

  depends_on = [
    aws_db_instance.csye6225_rds,
    aws_sns_topic.email_verify_topic,
    aws_security_group.lambda_sg
  ]
}


