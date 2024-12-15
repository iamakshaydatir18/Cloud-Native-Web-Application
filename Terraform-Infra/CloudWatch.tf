# Define IAM Policy for CloudWatch Agent
resource "aws_iam_policy" "cloudwatch_agent_policy" {
  name = "CloudWatchAgentPolicy"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams",
          "cloudwatch:PutMetricData",
          "cloudwatch:GetMetricStatistics",
          "cloudwatch:ListMetrics",
          "ec2:DescribeTags",      # Add this line
          "ec2:DescribeInstances", # Optionally, add this if you need instance details
          "ec2:DescribeRegions"
        ],
        Resource = "*"
      }
    ]
  })
}
