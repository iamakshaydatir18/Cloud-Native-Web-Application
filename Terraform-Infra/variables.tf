variable "region" {
  description = "AWS region"
}

variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "subnet_count" {
  description = "Enter Number of public and private subnets"
  type        = number
}

variable "public_subnet_cidrs" {
  description = "Enter CIDR blocks for public subnets"
  type        = list(string)

}

variable "private_subnet_cidrs" {
  description = "Enter CIDR blocks for private subnets"
  type        = list(string)

}


###RDS Parameters 
variable "db_engine" {
  description = "Database engine type: mysql, postgres, or mariadb"
  type        = string
  default     = "mysql"
}

variable "db_engine_version" {
  description = "Version of the DB engine to use"
  type        = string
  default     = "8.0" # Change this to the version you need (example for MySQL 8.0)
}

variable "db_instance_class" {
  description = "The DB instance class to use for RDS"
  type        = string
  default     = "db.t3.micro" # Cheapest DB instance class
}

variable "db_master_username" {
  description = "Master username for RDS"
  type        = string
  default     = "csye6225" # As per the requirement
}


variable "db_name" {
  description = "Db Name"
  type        = string
  default     = "csye6225"
}

# variable "db_master_password" {
#   description = "Master password for RDS"
#   type        = string
#   default     = "rootroot" # Replace with a strong password
#   sensitive   = true       # Keeps it secure
# }

variable "domain_name" {
  description = "Domain Name"
  type        = string
}

variable "subdomain_env" {
  description = "Sub Domain Name"
  type        = string
}

# variable "zone_id" {
#   description = "Sub Domain Name"
#   type        = string
# }

variable "server_port" {
  description = "Server port"
  type        = string
}

variable "desired_capacity" {
  description = "Desired Capacity"
  type        = number
}

variable "max_size" {
  description = "Max Size"
  type        = number
}

variable "min_size" {
  description = "Min Size"
  type        = number
}

variable "high_cpu_alarm" {
  description = "High Cpu alerm percentange"
  type        = number
}

variable "low_cpu_alarm" {
  description = "Low Cpu alerm percentange"
  type        = number
}


variable "SENDGRID_API_KEY" {
  description = "Send Grid APi key"
  type        = string
}