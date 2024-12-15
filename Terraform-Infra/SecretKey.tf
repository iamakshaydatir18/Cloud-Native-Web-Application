resource "random_password" "db_password_random" {
  length           = 16
  special          = true
  override_special = "_%"
}


resource "aws_secretsmanager_secret" "db_password" {
  name                    = "db-password"
  kms_key_id              = aws_kms_key.db_password_key.arn
  recovery_window_in_days = 0
  description             = "Database password for RDS instance"
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = jsonencode({ "password" = random_password.db_password_random.result })
}

