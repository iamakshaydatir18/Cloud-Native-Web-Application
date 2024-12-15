output "vpc_id" {
  value       = aws_vpc.main.id
  description = "The ID of the VPC"
}

output "public_subnet_ids" {
  value       = aws_subnet.public[*].id
  description = "List of IDs of public subnets"
}

output "private_subnet_ids" {
  value       = aws_subnet.private[*].id
  description = "List of IDs of private subnets"
}

output "internet_gateway_id" {
  value       = aws_internet_gateway.main.id
  description = "The ID of the Internet Gateway"
}

output "public_route_table_id" {
  value       = aws_route_table.public.id
  description = "The ID of the public route table"
}

output "private_route_table_id" {
  value       = aws_route_table.private.id
  description = "The ID of the private route table"
}

output "db_sg_id" {
  value = aws_security_group.db_sg.id
}

output "rds_endpoint" {
  value = aws_db_instance.csye6225_rds.endpoint
}

output "rds_db_name" {
  value = aws_db_instance.csye6225_rds.db_name
}

output "bucket_name" {
  value = aws_s3_bucket.my_bucket.bucket
}

output "bucket_arn" {
  value = aws_s3_bucket.my_bucket.arn
}


output "sns_topic_arn" {
  value       = aws_sns_topic.email_verify_topic.arn
  description = "The ARN of the SNS topic"
}


output "launch_template_id" {
  value       = aws_launch_template.web_app_launch_template.id
  description = "Launch Tempalte ID"
}

output "db_password" {
  value     = random_password.db_password_random.result
  sensitive = true
}

output "db_password_arn" {
  value = aws_secretsmanager_secret.db_password.arn
}

output "ec2_kms_key_arn" {
  value = aws_kms_key.ec2_key.arn
}