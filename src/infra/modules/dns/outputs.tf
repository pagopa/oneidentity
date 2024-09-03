output "dns_zone_id" {
  value = module.r53_zones.route53_zone_zone_id[
  keys(module.r53_zones.route53_zone_zone_id)[0]]
}

output "dns_zone_arn" {
  value = module.r53_zones.route53_zone_zone_arn
}

output "dns_zone_name" {
  value = keys(module.r53_zones.route53_zone_zone_id)[0]
}

output "dns_zone_name_servers" {
  value = module.r53_zones.route53_zone_name_servers
}
