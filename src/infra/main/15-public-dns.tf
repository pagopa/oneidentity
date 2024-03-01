# Create Route 53 Hosted Zone
resource "aws_route53_zone" "one_identity_dev" {
  name = "dev.oneidentity.pagopa.it"
}

# Output the name servers for the Route 53 hosted zone
output "name_servers" {
  value = aws_route53_zone.one_identity_dev.name_servers
}

# Create ACM Certificate

resource "aws_acm_certificate" "example_com" {
  domain_name       = "dev.oneidentiy.pagopa.it"
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }
}

# Output the DNS validation records for the ACM certificate
output "acm_certificate_validation_records" {
  value = aws_acm_certificate.example_com.domain_validation_options.*.resource_record_name
}

/*
# Optionally, you can add a Route 53 record set pointing to your resources
resource "aws_route53_record" "example_com_record" {
  zone_id = aws_route53_zone.example_com.zone_id
  name    = "www.example.com"
  type    = "A"
  ttl     = "300"
  records = ["your_resource_value"]
}

*/