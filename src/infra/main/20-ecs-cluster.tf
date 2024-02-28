module "ecr" {
  source  = "terraform-aws-modules/ecr/aws"
  version = "1.6.0"

  repository_name = format("%s-ecr", local.project)

  # repository_read_write_access_arns = ["arn:aws:iam::012345678901:role/terraform"]
  repository_lifecycle_policy = jsonencode({
    rules = [
      {
        rulePriority = 1,
        description  = "Keep last ${var.ecr_keep_images} images",
        selection = {
          "tagStatus" : "untagged",
          "countType" : "imageCountMoreThan",
          "countNumber" : var.ecr_keep_images
        },
        action = {
          type = "expire"
        }
      }
    ]
  })

  # Registry Replication Configuration
  # TODO: in production it might be replicated in another region.
  create_registry_replication_configuration = false
  registry_replication_rules                = []
}


module "ecs" {

  source  = "terraform-aws-modules/ecs/aws"
  version = "5.9.1"

  cluster_name = format("%s-ecs", local.project)

  cluster_settings = [{
    name  = "containerInsights"
    value = var.ecs_enable_container_insights ? "enabled" : "disabled"
    }
  ]

  # Capacity provider
  fargate_capacity_providers = {
    FARGATE = {
      default_capacity_provider_strategy = {
        weight = 50
        base   = 20
      }
    }
  }
}


module "ecs_service" {
  source  = "terraform-aws-modules/ecs/aws//modules/service"
  version = "5.9.1"

  name = "oneidentity"

  cluster_arn = module.ecs.cluster_arn

  cpu    = 512
  memory = 1024

  enable_execute_command = true

  container_definitions = {
    oneidentity = {
      cpu    = 512
      memory = 1024

      essential = true
      image     = "${module.ecr.repository_url}:1.0",

      port_mappings = [
        {
          name          = "oneidentity"
          containerPort = 8080
          hostPort      = 8080
          protocol      = "tcp"
        }
      ]
    }
  }

  subnet_ids       = module.vpc.private_subnets
  assign_public_ip = false

  /*
  security_group_rules = {
    alb_ingress_3000 = {
      type        = "ingress"
      from_port   = 8080
      to_port     = 8080
      protocol    = "tcp"
      description = "Service port"
      #source_security_group_id = "sg-12345678"
    }
    egress_all = {
      type        = "egress"
      from_port   = 0
      to_port     = 0
      protocol    = "-1"
      cidr_blocks = ["0.0.0.0/0"]
    }
  }
  */

}


resource "aws_security_group_rule" "allow_all_https" {
  type        = "egress"
  from_port   = 443
  to_port     = 443
  protocol    = "tcp"
  description = "Allow outbound https traffic."
  # TODO it might be to open.
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = module.ecs_service.security_group_id
}
