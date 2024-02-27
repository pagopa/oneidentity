output "iac_github_role_arn" {
  value = aws_iam_role.githubiac.arn
  description = "Role github can assume to build the infrastructure."
}