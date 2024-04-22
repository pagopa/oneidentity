module "backend" {
  source = "../modules/backend"

  registries = [
    {
      name                     = format("%s-ecr", local.project)
      number_of_images_to_keep = 3
  }]

}