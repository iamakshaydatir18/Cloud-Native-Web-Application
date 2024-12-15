# Create the IAM Role for EC2
resource "aws_iam_role" "ec2_instance_role" {
  name = "EC2InstanceRoleWithS3AndCloudWatch"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Service = "ec2.amazonaws.com"
        },
        Action = "sts:AssumeRole"
      }
    ]
  })
}


resource "aws_iam_policy" "sns_publish_policy" {
  name        = "SNSPublishPolicy"
  description = "Policy to allow publishing messages to SNS topic"
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow",
        Action   = "sns:Publish",
        Resource = aws_sns_topic.email_verify_topic.arn
      }
    ]
  })
}

resource "aws_iam_policy" "secrets_manager_policy" {
  name        = "SecretsManagerAccessPolicy"
  description = "Policy to allow EC2 to access Secrets Manager"
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "secretsmanager:GetSecretValue",
          "kms:Decrypt",
          "kms:GenerateDataKey"
        ],
        Resource = [aws_secretsmanager_secret.db_password.arn, aws_kms_key.db_password_key.arn]
      }
    ]
  })
}

resource "aws_iam_policy" "kms_access_policy" {
  name = "KMSAccessPolicy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "kms:Encrypt",
          "kms:Decrypt",
          "kms:ReEncrypt*",
          "kms:GenerateDataKey*",
          "kms:DescribeKey",
          "kms:CreateGrant"
        ]
        Resource = [aws_kms_key.ec2_key.arn, aws_kms_key.s3_key.arn, aws_kms_key.db_password_key.arn]
      }
    ]
  })
}


#Topic Policy
resource "aws_iam_role_policy_attachment" "sns_publish_policy_attachment" {
  role       = aws_iam_role.ec2_instance_role.name
  policy_arn = aws_iam_policy.sns_publish_policy.arn
}


# Attach the S3 access policy to the IAM role
resource "aws_iam_role_policy_attachment" "attach_s3_policy_to_role" {
  role       = aws_iam_role.ec2_instance_role.name
  policy_arn = aws_iam_policy.s3_access_policy.arn
}

# Attach the CloudWatch policy to the IAM role
resource "aws_iam_role_policy_attachment" "attach_cloudwatch_policy_to_role" {
  role       = aws_iam_role.ec2_instance_role.name
  policy_arn = aws_iam_policy.cloudwatch_agent_policy.arn
}


#Access Secret manager policy for IAM Roles
resource "aws_iam_role_policy_attachment" "attach_secrets_manager_policy" {
  role       = aws_iam_role.ec2_instance_role.name
  policy_arn = aws_iam_policy.secrets_manager_policy.arn
}

#Access KMS keysfor IAM Roles
resource "aws_iam_role_policy_attachment" "attach_kms_key_policy" {
  role       = aws_iam_role.ec2_instance_role.name
  policy_arn = aws_iam_policy.kms_access_policy.arn
}

# Create the Instance Profile
resource "aws_iam_instance_profile" "ec2_instance_profile" {
  name = "EC2InstanceProfile"
  role = aws_iam_role.ec2_instance_role.name
}