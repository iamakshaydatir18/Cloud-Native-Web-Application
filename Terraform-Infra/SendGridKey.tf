# KMS Key for SendGrid API Key
resource "aws_kms_key" "sendgrid_key" {
  description             = "KMS key for SendGrid API key"
  deletion_window_in_days = 10
  enable_key_rotation     = true

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "Enable IAM User Permissions"
        Effect = "Allow"
        Principal = {
          AWS = "*"
        }
        Action   = "kms:*"
        Resource = "*"
      },
      {
        Sid    = "Allow Lambda service to use the key"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
        Action = [
          "kms:Decrypt",
          "kms:DescribeKey",
          "kms:GenerateDataKey"
        ]
        Resource = "*"
      }
    ]
  })
}

# Secret for SendGrid API Key
resource "aws_secretsmanager_secret" "sendgrid_api_key" {
  name                    = "sendgrid-api-key-secret"
  kms_key_id              = aws_kms_key.sendgrid_key.arn
  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret_version" "sendgrid_api_key" {
  secret_id     = aws_secretsmanager_secret.sendgrid_api_key.id
  secret_string = var.SENDGRID_API_KEY
}