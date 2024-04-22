variable "registries" {
  type = list(object({
    name                     = string
    number_of_images_to_keep = number
  }))
  description = "ECR image repositories"

}