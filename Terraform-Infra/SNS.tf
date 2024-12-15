# SNS Topic
resource "aws_sns_topic" "email_verify_topic" {
  name = "email_sns_topic"
}

resource "aws_sns_topic_subscription" "lambda_subscription" {
  topic_arn  = aws_sns_topic.email_verify_topic.arn
  protocol   = "lambda"
  endpoint   = aws_lambda_function.serverless_lambda.arn
  depends_on = [aws_lambda_function.serverless_lambda]
}
