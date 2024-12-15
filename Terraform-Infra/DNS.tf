
# Fetch the hosted zone data
data "aws_route53_zone" "selected" {
  name         = "${var.subdomain_env}.${var.domain_name}"
  private_zone = false
}


# Route 53 DNS Record
resource "aws_route53_record" "app_dns_record" {
  zone_id = data.aws_route53_zone.selected.zone_id
  name    = "${var.subdomain_env}.${var.domain_name}"
  type    = "A"
  alias {
    name                   = aws_lb.app_load_balancer.dns_name
    zone_id                = aws_lb.app_load_balancer.zone_id
    evaluate_target_health = true
  }
}